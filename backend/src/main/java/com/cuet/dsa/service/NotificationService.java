package com.cuet.dsa.service;

import com.cuet.dsa.dto.request.NotificationRequest;
import com.cuet.dsa.dto.response.NotificationResponse;

import java.util.List;

public interface NotificationService {

    NotificationResponse createNotification(
            NotificationRequest request
    );

    List<NotificationResponse> getAllNotifications();

    void deleteNotification(Long id);
    void assignNotificationToAllUsers(Long notificationId);
    void createAndAssign(NotificationRequest request);
}