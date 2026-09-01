package com.cuet.dsa.service;


import com.cuet.dsa.dto.PendingUserExam;
import com.cuet.dsa.dto.response.ExamDetailResponse;
import com.cuet.dsa.dto.response.ExamSummaryResponse;
import com.cuet.dsa.dto.response.StartExamResponse;
import com.cuet.dsa.entity.Exam;
import com.cuet.dsa.enums.ExamStatus;
import com.cuet.dsa.entity.UserExam;
import com.cuet.dsa.exception.ExamNotAvailableException;
import com.cuet.dsa.exception.ExamNotFoundException;
import com.cuet.dsa.repository.UserRepository;
import com.cuet.dsa.security.UserExamWriteBuffer;
import com.cuet.dsa.repository.ExamRepository;
import com.cuet.dsa.repository.UserExamRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles the student-facing "start exam" flow.
 *
 * THE CORE DESIGN:
 * Redis is the fast, atomic gatekeeper for "has this student already
 * started this exam". Postgres (user_exam table) is the durable archive,
 * written asynchronously in batches. Reading the exam itself is cheap and
 * safe to do directly from Postgres (or a cache) because reads don't race -
 * the only operation that needs atomic protection is the FIRST WRITE that
 * marks "this student has started".
 *
 * EDGE CASES HANDLED:
 * 1. Repeat clicks / double-submit / page refresh -> Redis SETNX makes
 *    this a no-op against the DB entirely (see startExam).
 * 2. Exam doesn't exist, is soft-deleted, still DRAFT, or already CLOSED
 *    -> rejected before we ever touch Redis.
 * 3. Exam has a future startTime (availability window hasn't opened yet)
 *    -> rejected with a clear message.
 * 4. Redis is completely unavailable -> falls back to a Postgres-only
 *    path using the UNIQUE(user_id, exam_id) constraint + ON CONFLICT,
 *    which is slower under load but still correct.
 */
@Service
public class ExamSessionService {

    private static final Logger log = LoggerFactory.getLogger(ExamSessionService.class);

    private static final long TTL_BUFFER_SECONDS = 5 * 60;

    private final ExamRepository examRepository;
    private final UserExamRepository userExamRepository;
    private final UserExamWriteBuffer userExamWriteBuffer;
    private final RedisTemplate<String, String> redisTemplate;
    private final DefaultRedisScript<List> startExamScript;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper ;
    private static final String RUNNING_EXAMS_CACHE_KEY = "running_exams";
    private static final Duration RUNNING_EXAMS_TTL = Duration.ofMinutes(5);


    public ExamSessionService(ExamRepository examRepository,
                              UserExamRepository userExamRepository,
                              UserExamWriteBuffer userExamWriteBuffer,
                              RedisTemplate<String, String> redisTemplate,
                              DefaultRedisScript<List> startExamScript,
                              UserRepository userRepository,
                              ObjectMapper objectMapper) {
        this.examRepository = examRepository;
        this.userExamRepository = userExamRepository;
        this.userExamWriteBuffer = userExamWriteBuffer;
        this.redisTemplate = redisTemplate;
        this.startExamScript = startExamScript;
        this.userRepository=userRepository;
        this.objectMapper=objectMapper;
    }
    @Transactional
    public StartExamResponse startExam(UUID examId, Long userId) {
        Exam exam = loadStartableExam(examId);

        try {
            return startExamViaRedis(exam, userId);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis unavailable during startExam for user={} exam={} - falling back to DB",
                    userId, examId, e);
            return startExamViaDbFallback(exam, userId);
        }
    }

    private Exam loadStartableExam(UUID examId) {
        Exam exam = examRepository.findActiveById(examId)
                .orElseThrow(() -> new ExamNotFoundException("Exam not found: " + examId));

        if (exam.getStatus() != ExamStatus.PUBLISHED) {
            throw new ExamNotAvailableException("This exam is not currently available");
        }

        if (exam.getStartTime() != null && LocalDateTime.now().isBefore(exam.getStartTime())) {
            throw new ExamNotAvailableException(
                    "This exam is not open yet. It becomes available at " + exam.getStartTime());
        }

        return exam;
    }

    private StartExamResponse startExamViaRedis(Exam exam, Long userId) {
        System.out.println("Entered startExamViaRedis");
        String userExamKey = "user_exam:" + userId + ":" + exam.getExamId();
        String deadlineKey = "deadline:" + userId + ":" + exam.getExamId();

        LocalDateTime deadline = exam.getCreatedAt()
                .plusMinutes(exam.getDurationMinutes());

        long ttlSeconds = Duration.between(
                LocalDateTime.now(),
                deadline
        ).getSeconds() + TTL_BUFFER_SECONDS;

        if (ttlSeconds <= 0) {
            throw new IllegalStateException("Exam has already ended.");
        }
        System.out.println("Before Redis Execute");

        List result = redisTemplate.execute(
                startExamScript,
                List.of(userExamKey, deadlineKey),
                String.valueOf(deadline),
                String.valueOf(ttlSeconds)
        );

        System.out.println("After Redis Execute");
        long created = ((Number) result.get(0)).longValue();

        System.out.println("created = " + created);

        boolean isFirstTime = created == 1L;
        long deadlineMs = Long.parseLong(String.valueOf(result.get(1)));
        if (!isFirstTime) {
            return StartExamResponse.resuming(exam.getExamId(), deadlineMs);
        }
        System.out.println("Before Queue Add");
        userExamWriteBuffer.add(new PendingUserExam(
                userId,
                exam.getExamId(),
                Instant.now(),
                Instant.ofEpochMilli(deadlineMs)
        ));

        return StartExamResponse.started(exam.getExamId(), deadlineMs);
    }

    private StartExamResponse startExamViaDbFallback(Exam exam, Long userId) {
        Instant now = Instant.now();
        Instant deadline = now.plus(exam.getDurationMinutes(), ChronoUnit.MINUTES);

        int rowsInserted = userExamRepository.insertIfAbsent(userId, exam.getExamId(), now, deadline);

        UserExam userExam = userExamRepository.findByUserIdAndExamId(userId, exam.getExamId())
                .orElseThrow(() -> new IllegalStateException(
                        "user_exam row missing immediately after insertIfAbsent - this should be impossible"));

        long deadlineMs = userExam.getDeadline().toEpochMilli();

        if (rowsInserted > 0) {
            return StartExamResponse.started(exam.getExamId(), deadlineMs);
        } else {
            return StartExamResponse.resuming(exam.getExamId(), deadlineMs);
        }
    }

    public List<ExamSummaryResponse> getAllExam() {

        // --- Step 1: Try Redis cache first ---
        try {
            String cached = redisTemplate.opsForValue().get(RUNNING_EXAMS_CACHE_KEY);
            if (cached != null) {
                log.debug("Cache HIT for running_exams");
                return objectMapper.readValue(
                        cached,
                        objectMapper.getTypeFactory()
                                .constructCollectionType(List.class, ExamSummaryResponse.class)
                );
            }
            log.debug("Cache MISS for running_exams — fetching from DB");
        } catch (Exception e) {
            // Redis is down or deserialization failed — degrade gracefully,
            // never surface a cache failure to the student as a 500.
            log.warn("Redis read failed for running_exams cache — falling back to DB", e);
        }

        // --- Step 2: DB fallback ---
        List<Exam> exams = examRepository.findAllByStatusOrderByStartTimeAsc(ExamStatus.PUBLISHED);
        List<ExamSummaryResponse> response = exams.stream()
                .map(ExamSummaryResponse::from)
                .collect(Collectors.toList());

        // --- Step 3: Write back to Redis (after DB read, not inside a TX) ---
        try {
            String serialized = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(RUNNING_EXAMS_CACHE_KEY, serialized, RUNNING_EXAMS_TTL);
            log.debug("Cached {} running exams in Redis (TTL={})", response.size(), RUNNING_EXAMS_TTL);
        } catch (Exception e) {
            // Cache write failure is non-fatal — student still gets the response
            log.warn("Redis write failed for running_exams cache — response served from DB", e);
        }

        return response;
    }

    @Transactional
    public ExamDetailResponse getExamDetail(UUID examId) {
        System.out.println(examId);
        Exam exam = examRepository.findActiveById(examId)
                .orElseThrow(() -> new ExamNotFoundException("Exam not found: " + examId));
        return ExamDetailResponse.from(exam);
    }


}