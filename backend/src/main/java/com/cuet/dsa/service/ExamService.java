package com.cuet.dsa.service;

import com.cuet.dsa.config.RabbitConfig;
import com.cuet.dsa.dto.request.CreateExamProblemRequest;
import com.cuet.dsa.dto.request.CreateExamRequest;
import com.cuet.dsa.dto.request.NotificationMessage;
import com.cuet.dsa.dto.response.ExamSummaryResponse;
import com.cuet.dsa.entity.Exam;
import com.cuet.dsa.entity.ExamProblem;
import com.cuet.dsa.entity.Problem;
import com.cuet.dsa.exception.ExamTitleConflictException;
import com.cuet.dsa.exception.ExamNotFoundException;
import com.cuet.dsa.exception.ResourceNotFoundException;
import com.cuet.dsa.repository.ExamRepository;
import com.cuet.dsa.repository.ProblemRepository; // Ensure this is imported
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExamService {

    private static final Logger log = LoggerFactory.getLogger(ExamService.class);
    private static final Duration IDEMPOTENCY_CACHE_TTL = Duration.ofHours(24);

    private final ExamRepository examRepository;
    private final ProblemRepository problemRepository; // Injected to avoid N+1 query
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitConfig rabbitConfig;


    public ExamService(ExamRepository examRepository,
                       ProblemRepository problemRepository,RedisTemplate<String, String> redisTemplate,ObjectMapper objectMapper,
                       RabbitTemplate rabbitTemplate,RabbitConfig rabbitConfig
                      ) {
        this.examRepository = examRepository;
        this.problemRepository = problemRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.rabbitTemplate=rabbitTemplate;
        this.rabbitConfig=rabbitConfig;
    }

    @Transactional
    public ExamSummaryResponse createExam(String idempotencyKey, CreateExamRequest request) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key header is required");
        }

//         --- Step 1: Redis Cache Check ---
        String cacheKey = "idempotency:createExam:" + idempotencyKey;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, ExamSummaryResponse.class);
            } catch (JsonProcessingException e) {
                log.warn("Failed to deserialize cached idempotency response for key {}", idempotencyKey, e);
            }
        }

        // --- Step 2: DB Idempotency Key Check ---
        var existingByKey = examRepository.findByIdempotencyKey(idempotencyKey);
        if (existingByKey.isPresent()) {
            return ExamSummaryResponse.from(existingByKey.get());
        }

        // --- Step 3: Title Conflict Pre-check ---
        var existingByTitle = examRepository.findByTitle(request.getTitle());
        if (existingByTitle.isPresent()) {
            throw new ExamTitleConflictException(
                    "An exam with title '" + request.getTitle() + "' already exists");
        }

        // --- Step 4: Validate Input Problems Payload ---
        List<CreateExamProblemRequest> incomingProblems = request.getProblems();
        if (incomingProblems == null || incomingProblems.size() < 3) {
            throw new IllegalArgumentException("Exam must contain at least 3 problems");
        }

        // FIX: Changed Set<UUID> to Set<Long> to support numeric IDs
        Set<Long> uniqueProblemIds = incomingProblems.stream()
                .map(CreateExamProblemRequest::getProblemId) // This should return Long now
                .collect(Collectors.toSet());

        if (uniqueProblemIds.size() != incomingProblems.size()) {
            throw new IllegalArgumentException("The request payload contains duplicate problem IDs.");
        }

        // --- Step 5: Batch Fetch Problems (Now mapping via Long IDs) ---
        List<Problem> dbProblems = problemRepository.findAllById(uniqueProblemIds);
        if (dbProblems.size() != uniqueProblemIds.size()) {
            throw new ResourceNotFoundException("One or more provided problem IDs do not exist in the database.");
        }

        // FIX: Change the Map key type from UUID to Long
        Map<Long, Problem> problemMap = dbProblems.stream()
                .collect(Collectors.toMap(Problem::getId, p -> p));
        // --- Step 6: Execute the Write Operation ---
        ExamSummaryResponse response = doCreateExam(request, incomingProblems, problemMap, idempotencyKey);

//         --- Step 7: Cache Response ---
        try {
            redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(response), IDEMPOTENCY_CACHE_TTL);
        } catch (JsonProcessingException e) {
            log.warn("Failed to cache idempotency response for key {}", idempotencyKey, e);
        }

        return response;
    }

    private ExamSummaryResponse doCreateExam(CreateExamRequest request,
                                             List<CreateExamProblemRequest> incomingProblems,
                                             Map<Long, Problem> problemMap,
                                             String idempotencyKey) {
        System.out.println("Inside doCreateExam");
        try {
            System.out.println("Inside try block");
            // Calculate total score dynamically from incoming problem settings
            int totalScore = incomingProblems.stream().mapToInt(CreateExamProblemRequest::getScore).sum();
            System.out.println("Total Score: " + totalScore);
            // Build base parent Exam entity
            Exam exam = Exam.builder()
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .durationMinutes(request.getDurationMinutes())
                    .startTime(request.getStartTime())
                    .passingMarks(request.getPassingMarks())
                    .totalScore(totalScore)
                    .idempotencyKey(idempotencyKey)
                    .build();
            System.out.println("Exam Created");
            // Transform DTOs into ExamProblem join entities with historical snapshots
            List<ExamProblem> examProblems = incomingProblems.stream().map(probRequest -> {
                Problem problemSnapshot = problemMap.get(probRequest.getProblemId());

                return ExamProblem.builder()
                        .exam(exam) // Establish parent relationship link
                        .problem(problemSnapshot)
                        .score(probRequest.getScore())
                        .build();
            }).collect(Collectors.toList());
            System.out.println("ExamProblems Created");
            // Bind the managed child items to the parent Exam
            exam.setExamProblems(examProblems);
            System.out.println("ExamProblems Bound");
            // CascadeType.ALL ensures saving the exam saves all associated ExamProblems in 1 atomic save transaction
            Exam saved = examRepository.saveAndFlush(exam);
            System.out.println("Exam Saved");
            NotificationMessage message = NotificationMessage.builder()
                    .examId(saved.getExamId())
                    .title(saved.getTitle())
                    .description(saved.getDescription())
                    .startTime(saved.getStartTime())
                    .durationMinutes(saved.getDurationMinutes())
                    .createdAt(LocalDateTime.now())
                    .type("EXAM_CREATED")
                    .build();
            System.out.println("Message Created" + message);

            rabbitTemplate.convertAndSend(rabbitConfig.NOTIFICATION_EXCHANGE,
                    rabbitConfig.NOTIFICATION_RK,message);
            System.out.println("RabbitMQ Message Sent:");
            return ExamSummaryResponse.from(saved);
        } catch (DataIntegrityViolationException e) {
            String message = e.getMostSpecificCause().getMessage();
            if (message != null && message.contains("uq_exam_idempotency_key")) {
                throw new IllegalArgumentException(
                        "Idempotency-Key has already been used for a different request");
            }
            throw new ExamTitleConflictException(
                    "An exam with title '" + request.getTitle() + "' already exists");
        }
    }
    @Cacheable(value = "exam", key = "#examId")
    @Transactional
    public ExamSummaryResponse getExam(UUID examId) {
        System.out.println(
                ">>> getExam() METHOD EXECUTED - CACHE MISS: " + examId
        );
        Exam exam = examRepository.findActiveById(examId)
                .orElseThrow(() -> new ExamNotFoundException("Exam not found: " + examId));
        return ExamSummaryResponse.from(exam);
    }
}
