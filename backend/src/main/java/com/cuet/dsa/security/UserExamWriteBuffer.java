package com.cuet.dsa.security;



import com.cuet.dsa.dto.PendingUserExam;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe, bounded buffer holding "first-time exam starts" until the
 * scheduled UserExamFlushScheduler batch-writes them to Postgres.
 *
 * WHY THIS EXISTS:
 * Redis confirms a session start in under 1ms, but doing a single-row DB
 * INSERT per request defeats the purpose of putting Redis in front in
 * the first place. Instead we buffer writes here and flush in batches of
 * up to 500 every 200ms - turning 10,000 INSERTs into ~20.
 *
 * EDGE CASE - queue full:
 * If the queue fills up (DB flush can't keep pace with incoming starts),
 * add() does NOT block the student's request. It logs an error and drops
 * the write. The student is NOT blocked - their Redis session is already
 * valid - but we lose the durable DB row unless the reconciliation sweep
 * (see ExamReconciliationJob) backfills it later.
 */
@Component
public class UserExamWriteBuffer {

    private static final Logger log = LoggerFactory.getLogger(UserExamWriteBuffer.class);

    private static final int MAX_QUEUE_SIZE = 50_000;

    private final BlockingQueue<PendingUserExam> queue = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
    private final AtomicLong droppedCount = new AtomicLong(0);

    public void add(PendingUserExam pending) {
        System.out.println("Pending User is added");
        boolean accepted = queue.offer(pending); // never blocks

        if (!accepted) {
            long total = droppedCount.incrementAndGet();
            log.error(
                    "UserExamWriteBuffer full - dropped write for user={} exam={}. Total dropped so far: {}",
                    pending.userId(), pending.examId(), total
            );
        }
    }

    public List<PendingUserExam> drain(int maxBatchSize) {
        List<PendingUserExam> batch = new ArrayList<>(maxBatchSize);
        queue.drainTo(batch, maxBatchSize);
        return batch;
    }

    public int currentSize() {
        return queue.size();
    }

    public long droppedCount() {
        return droppedCount.get();
    }
}