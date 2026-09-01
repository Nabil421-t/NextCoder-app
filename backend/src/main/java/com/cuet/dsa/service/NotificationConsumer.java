package com.cuet.dsa.service;

import com.cuet.dsa.config.RabbitConfig;
import com.cuet.dsa.dto.request.NotificationRequest;
import com.cuet.dsa.dto.request.NotificationMessage;
import com.cuet.dsa.enums.NotificationSource;
import com.cuet.dsa.enums.NotificationType;
import com.cuet.dsa.service_implementation.NotificationServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationServiceImpl notificationService;

    @RabbitListener(queues = RabbitConfig.NOTIFICATION_QUEUE)
    public void consume(NotificationMessage message) {

        log.info("Received notification for exam {}", message.getExamId());

        NotificationRequest request = new NotificationRequest();

        request.setExternalId("EXAM_" + message.getExamId());

        request.setTitle(message.getTitle());

        request.setMessage(message.getDescription());

        request.setType(NotificationType.REMINDER ); // or EXAM if you have one

        request.setSource(NotificationSource.SYSTEM);

        request.setStartTime(message.getStartTime());

        request.setUrl("/exam/" + message.getExamId());

        notificationService.createAndAssign(request);
    }
}