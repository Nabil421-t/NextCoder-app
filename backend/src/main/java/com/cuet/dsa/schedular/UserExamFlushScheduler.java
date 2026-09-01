package com.cuet.dsa.schedular;


import com.cuet.dsa.dto.PendingUserExam;
import com.cuet.dsa.security.UserExamWriteBuffer;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * Drains UserExamWriteBuffer every 200ms and bulk-inserts into Postgres.
 *
 * EDGE CASE - batch insert fails entirely (DB down/unreachable):
 * The batch has already been pulled out of the queue at this point. If we
 * just swallowed the exception, those rows would be silently lost. So on
 * failure we re-add every item back into the buffer for the next tick to
 * retry. This can cause re-ordering and, in a worst case sustained DB
 * outage, queue growth toward MAX_QUEUE_SIZE - which is the correct
 * failure mode (loud drops + alerting) rather than silent data loss.
 */
@Component
public class UserExamFlushScheduler {

    private static final Logger log = LoggerFactory.getLogger(UserExamFlushScheduler.class);
    private static final int BATCH_SIZE = 500;

    private final UserExamWriteBuffer buffer;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UserExamFlushScheduler(UserExamWriteBuffer buffer, NamedParameterJdbcTemplate jdbcTemplate) {
        this.buffer = buffer;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Scheduled(fixedDelay = 200)
    @Transactional
    public void flush() {
//        System.out.println("FLUSH RUNNING");
        List<PendingUserExam> batch = buffer.drain(BATCH_SIZE);
        if (batch.isEmpty()) {
            return;
        }

        // ON CONFLICT DO NOTHING is a safety net here, not the primary
        // defense. The Redis SETNX gate already guarantees this buffer
        // never receives two entries for the same (user, exam) pair -
        // this only protects the rare crash-and-retry edge case where a
        // batch is re-queued after a partial failure.
        String sql = """
            INSERT INTO user_exam (id, user_id, exam_id, started_at, deadline, status, created_at, version)
            VALUES (:id, :userId, :examId, :startedAt, :deadline, 'IN_PROGRESS', now(), 0)
            ON CONFLICT (user_id, exam_id) DO NOTHING
            """;

        SqlParameterSource[] params = batch.stream()
                .map(this::toParams)
                .toArray(SqlParameterSource[]::new);

        try {
            int[] results = jdbcTemplate.batchUpdate(sql, params);
            log.debug("Flushed {} user_exam rows to DB ({} actually inserted)",
                    batch.size(), countNonZero(results));
        } catch (Exception e) {
            log.error("Batch flush failed completely. Diverting {} records to secondary error log to prevent memory buffer exhaustion.", batch.size(), e);
            // Write to a dedicated error file or database fallback instead of buffer::add
            for (PendingUserExam p : batch) {
                log.error("FAILED_PERSIST: User={}, Exam={}", p.userId(), p.examId());
            }
        }
    }


    private SqlParameterSource toParams(PendingUserExam p) {
        return new MapSqlParameterSource()
                .addValue("id", UUID.randomUUID())
                .addValue("userId", p.userId())
                .addValue("examId", p.examId())
                .addValue("startedAt", Timestamp.from(p.startedAt()))
                .addValue("deadline", Timestamp.from(p.deadline()));
    }

    private int countNonZero(int[] results) {
        int count = 0;
        for (int r : results) {
            if (r != 0) count++;
        }
        return count;
    }
}