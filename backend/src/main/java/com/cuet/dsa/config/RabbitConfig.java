package com.cuet.dsa.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ═══════════════════════════════════════════════════════════════════════
 *  RabbitMQ Topology — Online Judge
 * ═══════════════════════════════════════════════════════════════════════
 *
 *  Topology diagram:
 *
 *   [SubmissionService]
 *         │  publish(submissionId)
 *         ▼
 *   judge.exchange ──(rk: judge)──► judge.queue ◄──────────────────────┐
 *         │                              │ worker consumes              │
 *         │                              │ NACK / requeue=false         │
 *         │                              ▼                              │
 *         │                   judge.retry.exchange                      │
 *         │                              │ rk: judge.retry              │
 *         │                              ▼                              │
 *         │                   judge.retry.queue (TTL = 5 s)             │
 *         │                     DLX ──────────────────────────(back)────┘
 *         │
 *         │  After MAX_RETRIES (x-death count ≥ 3):
 *         │  worker manually publishes to:
 *         ▼
 *   judge.dlq.exchange ──(rk: judge.dlq)──► judge.dlq.queue
 *         [DeadLetterHandler logs & marks submission INTERNAL_ERROR]
 *
 * ═══════════════════════════════════════════════════════════════════════
 *
 *  Edge cases handled:
 *
 *  1. Worker crashes BEFORE ACK
 *     → RabbitMQ re-delivers automatically (manual ACK mode).
 *
 *  2. Worker crashes AFTER ACK but BEFORE DB write
 *     → Submission stays RUNNING.
 *     → SubmissionRecoveryScheduler resets it to PENDING after
 *       STUCK_TIMEOUT_MINUTES and republishes to judge.queue.
 *
 *  3. User double-submits (frontend retry / race)
 *     → Idempotency key (SHA-256 of userId|problemId|sourceCode)
 *       stored with a UNIQUE constraint on submissions.idempotency_key.
 *     → Duplicate insert is caught; existing submission returned.
 *
 *  4. Two workers race on the same message (redelivery edge case)
 *     → Atomic single-SQL claim:
 *         UPDATE submissions SET status='RUNNING' WHERE id=? AND status='PENDING'
 *     → Only one UPDATE wins; the other worker sees 0 rows → skips.
 *
 *  5. Transient failure (e.g. DB temporarily unreachable)
 *     → NACK requeue=false → retry queue (5 s TTL) → back to judge.queue.
 *     → Retried up to MAX_RETRIES=3 times.
 *
 *  6. Permanent failure (bug, data integrity error)
 *     → After MAX_RETRIES exceeded, worker ACKs and publishes to DLQ.
 *     → DeadLetterHandler marks submission INTERNAL_ERROR.
 */
@Configuration
public class RabbitConfig {

    // ── Queue / Exchange names ────────────────────────────────────────────────

    public static final String JUDGE_EXCHANGE       = "judge.exchange.v3";
    public static final String JUDGE_RETRY_EXCHANGE = "judge.retry.exchange.v3";
    public static final String JUDGE_DLQ_EXCHANGE   = "judge.dlq.exchange.v3";

    public static final String JUDGE_QUEUE       = "judge.queue.v3";
    public static final String JUDGE_RETRY_QUEUE = "judge.retry.queue.v3";
    public static final String JUDGE_DLQ_QUEUE   = "judge.dlq.queue.v3";
    public static final String JUDGE_RK       = "judge";
    public static final String JUDGE_RETRY_RK = "judge.retry";
    public static final String JUDGE_DLQ_RK   = "judge.dlq";
    // Notification
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String NOTIFICATION_RK = "notification";
    public static final String NOTIFICATION_DLQ_RK = "notification.dlq";
    public static final String NOTIFICATION_DLQ_EXCHANGE = "notification.dlq.exchange";
    public static final String NOTIFICATION_DLQ_QUEUE = "notification.dlq.queue";
    public static final long NOTIFICATION_TTL = 60 * 60 * 1000L; // 1 hour

    /** Maximum number of times a message is retried before going to the DLQ. */
    public static final int MAX_RETRIES = 3;

    /** Delay (ms) before a failed message is returned to judge.queue. */
    public static final long RETRY_DELAY_MS = 5_000L;

    // ── Exchanges ─────────────────────────────────────────────────────────────

    /** Primary exchange — receives new submissions from SubmissionService. */
    @Bean
    public DirectExchange judgeExchange() {
        return ExchangeBuilder.directExchange(JUDGE_EXCHANGE)
                .durable(true)
                .build();
    }

    /** Retry exchange — messages land here after a failed processing attempt. */
    @Bean
    public DirectExchange judgeRetryExchange() {
        return ExchangeBuilder.directExchange(JUDGE_RETRY_EXCHANGE)
                .durable(true)
                .build();
    }

    /** Dead-letter exchange — permanent failures end up here. */
    @Bean
    public DirectExchange judgeDlqExchange() {
        return ExchangeBuilder.directExchange(JUDGE_DLQ_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange notificationExchange(){
        return ExchangeBuilder.directExchange(NOTIFICATION_EXCHANGE)
                .durable(true)
                .build();
    }
    @Bean
    public DirectExchange notificationDlqExchange(){
        return ExchangeBuilder.directExchange(NOTIFICATION_DLQ_EXCHANGE)
                .durable(true)
                .build();
    }

    // ── Queues ────────────────────────────────────────────────────────────────

    /**
     * Main judge queue.
     * DLX is set to the RETRY exchange so NACK(requeue=false) goes to retry,
     * NOT directly to the DLQ.
     */
    @Bean
    public Queue judgeQueue() {
        return QueueBuilder
                .durable(JUDGE_QUEUE)
                .withArgument("x-dead-letter-exchange",    JUDGE_RETRY_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", JUDGE_RETRY_RK)
                .build();
    }

    /**
     * Retry queue — holds messages for RETRY_DELAY_MS before returning them
     * to the main judge.queue via its own DLX.
     */
    @Bean
    public Queue judgeRetryQueue() {
        return QueueBuilder
                .durable(JUDGE_RETRY_QUEUE)
                .withArgument("x-dead-letter-exchange",    JUDGE_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", JUDGE_RK)
                .withArgument("x-message-ttl",             RETRY_DELAY_MS)
                .build();
    }
    @Bean
    public Queue notificationQueue(){
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange",    NOTIFICATION_DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", NOTIFICATION_DLQ_RK)
                .withArgument("x-message-ttl",              NOTIFICATION_TTL)
                .build();
    }

    /**
     * Dead-letter queue — messages that have exhausted all retries.
     * No DLX — they stay here for human inspection.
     */
    @Bean
    public Queue judgeDlqQueue() {
        return QueueBuilder.durable(JUDGE_DLQ_QUEUE).build();
    }
    @Bean
    public Queue notificationDlqQueue(){
        return QueueBuilder.durable(NOTIFICATION_DLQ_QUEUE)
                .build();
    }
    // ── Bindings ──────────────────────────────────────────────────────────────

    @Bean
    public Binding judgeBinding(Queue judgeQueue, DirectExchange judgeExchange) {
        return BindingBuilder.bind(judgeQueue).to(judgeExchange).with(JUDGE_RK);
    }

    @Bean
    public Binding judgeRetryBinding(Queue judgeRetryQueue, DirectExchange judgeRetryExchange) {
        return BindingBuilder.bind(judgeRetryQueue).to(judgeRetryExchange).with(JUDGE_RETRY_RK);
    }

    @Bean
    public Binding judgeDlqBinding(Queue judgeDlqQueue, DirectExchange judgeDlqExchange) {
        return BindingBuilder.bind(judgeDlqQueue).to(judgeDlqExchange).with(JUDGE_DLQ_RK);
    }

    // ── Message converter ─────────────────────────────────────────────────────
    /** Serialize messages to/from JSON so JudgeMessage DTO works cleanly. */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public Binding notificationBinding(DirectExchange notificationExchange, Queue notificationQueue){
        return BindingBuilder.bind(notificationQueue).to(notificationExchange).with(NOTIFICATION_RK);
    }
    @Bean
    public Binding notificationDlqBinding(DirectExchange notificationDlqExchange, Queue notificationDlqQueue){
        return BindingBuilder.bind(notificationDlqQueue).to(notificationDlqExchange).with(NOTIFICATION_DLQ_RK);
    }
    // ── RabbitTemplate ────────────────────────────────────────────────────────

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate tpl = new RabbitTemplate(cf);
        tpl.setMessageConverter(jsonMessageConverter());
        // Enable publisher confirms so we know if a message was accepted by the broker.
        tpl.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                // In production: alert / log / retry
                System.err.println("[RabbitMQ] Publisher confirm NACK: " + cause);
            }
        });
        return tpl;
    }

    // ── Listener container factory ────────────────────────────────────────────

    /**
     * MANUAL acknowledge mode:
     *  - Worker calls channel.basicAck()  on success.
     *  - Worker calls channel.basicNack() on failure.
     *  - If the worker JVM dies before ack, RabbitMQ re-delivers the message.
     *
     * prefetchCount=1 ensures each worker processes ONE message at a time,
     * preventing a slow worker from blocking all messages.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory cf) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(cf);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(1);                 // one at a time per worker
        factory.setConcurrentConsumers(2);           // min workers
        factory.setMaxConcurrentConsumers(10);       // scale up under load
        return factory;
    }
}