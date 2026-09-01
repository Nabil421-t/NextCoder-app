package com.cuet.dsa.service_implementation;

import com.cuet.dsa.dto.request.NotificationRequest;
import com.cuet.dsa.dto.response.CodeforcesContest;
import com.cuet.dsa.dto.response.LeetcodeContest;
import com.cuet.dsa.dto.response.NotificationResponse;
import com.cuet.dsa.entity.Notification;
import com.cuet.dsa.entity.User;
import com.cuet.dsa.entity.UserNotification;
import com.cuet.dsa.exception.NotificationNotFoundException;
import com.cuet.dsa.repository.NotificationRepository;
import com.cuet.dsa.repository.NotificationUserRepository;
import com.cuet.dsa.repository.UserRepository;
import com.cuet.dsa.service.CodeforcesService;
import com.cuet.dsa.service.LeetcodeService;
import com.cuet.dsa.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationUserRepository notificationUserRepository;
    // =====================================================
    // CREATE NOTIFICATION + ASSIGN TO USER
    // =====================================================
    @Override
    public NotificationResponse createNotification(
            NotificationRequest request
    ) {

        // -------------------------
        // Create notification
        // -------------------------
        Optional<Notification> existing =
                notificationRepository.findByExternalId(
                        request.getExternalId()
                );
        // ====================================
        // If already exists → return existing
        // ====================================
        if (existing.isPresent()) {

            return mapToResponse(existing.get());
        }
        Notification notification = new Notification();

        notification.setExternalId(request.getExternalId());
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setType(request.getType());
        notification.setSource(request.getSource());
        notification.setUrl(request.getUrl());
        notification.setStartTime(request.getStartTime());

        Notification saved = notificationRepository.save(notification);

        return mapToResponse(saved);
    }

    @Transactional
    public void assignNotificationToAllUsers(Long notificationId) {

        Notification notification =
                notificationRepository.findById(notificationId)
                        .orElseThrow(() ->
                                new NotificationNotFoundException(
                                        "Notification not found"
                                ));
        List<User>users=userRepository.findAll();//1 query
        Set<Long> existingUserIds =
                notificationUserRepository
                        .findExistingUserIds(notificationId); //1 query

        List<UserNotification> mappings = new ArrayList<>();

        for (User user : users) {

            if (existingUserIds.contains(user.getId())) {
                continue;  //0(1)
            }

            UserNotification un =
                    UserNotification.builder()
                            .user(user)
                            .notification(notification)
                            .isRead(false)
                            .hidden(false)
                            .build();

            mappings.add(un);
        }

        if (!mappings.isEmpty()) {
            notificationUserRepository.saveAll(mappings);
        }
    }

    // =====================================================
    // GET USER NOTIFICATIONS
    // =====================================================
    @Override
    public List<NotificationResponse> getAllNotifications() {
        return notificationRepository
                .findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    @Override
    public void deleteNotification(Long notificationId) {

        Notification notification =
                notificationRepository.findById(notificationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Notification not found"
                                ));

        notification.setDeleted(true);

        notificationRepository.save(notification);
    }
    @Transactional
    public void createAndAssign(NotificationRequest request) {

        NotificationResponse saved = createNotification(request);

        assignNotificationToAllUsers(saved.getNotificationId());
    }
    // =====================================================
    // DTO MAPPER
    // =====================================================
    private NotificationResponse mapToResponse(Notification n) {

        NotificationResponse res =
                new NotificationResponse();

        res.setNotificationId(n.getId());

        res.setTitle(n.getTitle());
        res.setMessage(n.getMessage());

        res.setType(String.valueOf(n.getType()));
        res.setSource(String.valueOf(n.getSource()));

        res.setExternalId(n.getExternalId());

        res.setCreatedAt(n.getCreatedAt());

        return res;
    }
}