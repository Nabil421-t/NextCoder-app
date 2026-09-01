package com.cuet.dsa.service;

import com.cuet.dsa.dto.response.NotificationResponse;

import java.util.List;

public interface NotificationUserService {

    List<NotificationResponse> getUserNotifications(Long userId);

    List<NotificationResponse> getUnreadNotifications(Long userId);

    void markAsRead(Long userId, Long notificationId);

    void markAllAsRead(Long userId);
    void hideNotification(Long userId, Long notificationId);
}