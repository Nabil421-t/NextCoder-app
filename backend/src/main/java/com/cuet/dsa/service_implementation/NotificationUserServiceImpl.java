package com.cuet.dsa.service_implementation;

import com.cuet.dsa.dto.response.NotificationResponse;
import com.cuet.dsa.entity.Notification;
import com.cuet.dsa.entity.User;
import com.cuet.dsa.entity.UserNotification;
import com.cuet.dsa.repository.NotificationRepository;
import com.cuet.dsa.repository.NotificationUserRepository;
import com.cuet.dsa.repository.UserRepository;
import com.cuet.dsa.service.NotificationUserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationUserServiceImpl implements NotificationUserService {

    private final NotificationUserRepository notificationUserRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // -------------------------------

    // -------------------------------
    @Transactional
    @Override
    public List<NotificationResponse> getUserNotifications(Long userId) {

        return notificationUserRepository.findByUser_IdAndHiddenFalseAndDeletedFalseAndNotification_DeletedFalse(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    // -------------------------------
    @Transactional
    @Override
    public List<NotificationResponse> getUnreadNotifications(
            Long userId
    ) {

        return notificationUserRepository
                .findByUser_IdAndIsReadFalseAndHiddenFalseAndDeletedFalseAndNotification_DeletedFalse(
                        userId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // -------------------------------
    @Override
    public void markAsRead(
            Long userId,
            Long notificationId
    ) {

        UserNotification nu =
                notificationUserRepository
                        .findByNotification_IdAndUser_Id(
                                notificationId,
                                userId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Not found"
                                ));

        if (nu.isDeleted()) {
            throw new RuntimeException(
                    "Notification is deleted"
            );
        }

        nu.markAsRead();

        notificationUserRepository.save(nu);
    }

    // -------------------------------
    @Override
    public void markAllAsRead(Long userId) {

        List<UserNotification> list =

                notificationUserRepository
                        .findByUser_IdAndIsReadFalseAndHiddenFalseAndDeletedFalseAndNotification_DeletedFalse(
                                userId
                        );

        for (UserNotification n : list) {
            n.markAsRead();
        }

        notificationUserRepository.saveAll(list);
    }

    // -------------------------------
    @Override
    public void hideNotification(Long userId, Long notificationId) {

        UserNotification nu = notificationUserRepository
                .findByNotification_IdAndUser_Id(notificationId, userId)
                .orElseThrow(() -> new RuntimeException("Not found"));

        nu.setHidden(true);
        notificationUserRepository.save(nu);
    }

    // -------------------------------
    private NotificationResponse mapToResponse(UserNotification un) {

        Notification n = un.getNotification();

        NotificationResponse res = new NotificationResponse();

        res.setNotificationId(n.getId()); // ✅ FIXED
        res.setUserId(un.getUser().getId());

        res.setTitle(n.getTitle());
        res.setMessage(n.getMessage());

        res.setType(String.valueOf(n.getType()));
        res.setSource(String.valueOf(n.getSource()));

        res.setExternalId(n.getExternalId()); // ✅ FIXED

        res.setReadStatus(un.isRead());
        res.setHidden(un.isHidden());

        res.setReadAt(un.getReadAt());

        res.setCreatedAt(n.getCreatedAt());

        return res;
    }
}