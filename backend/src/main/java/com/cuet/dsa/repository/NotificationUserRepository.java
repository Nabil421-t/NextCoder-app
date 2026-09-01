package com.cuet.dsa.repository;

import com.cuet.dsa.entity.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface NotificationUserRepository extends JpaRepository<UserNotification, Long> {

    // ==============================
    // Get all notifications of a user
    // ==============================
    List<UserNotification> findByUser_Id(Long userId);

    // ==============================
    // Get only unread notifications
    // ==============================
    List<UserNotification> findByUser_IdAndIsReadFalse(Long userId);

    // ==============================
    // Find specific notification for a user
    // (used for mark-as-read / hide)
    // ==============================
    Optional<UserNotification> findByNotification_IdAndUser_Id(
            Long notificationId,
            Long userId
    );
    @Query("""
        SELECT un.user.id
        FROM UserNotification un
        WHERE un.notification.id = :notificationId
    """)
    Set<Long> findExistingUserIds(
            @Param("notificationId")
            Long notificationId
    );
    List<UserNotification> findByUser_IdAndHiddenFalseAndDeletedFalseAndNotification_DeletedFalse(Long userId);

    List<UserNotification> findByUser_IdAndIsReadFalseAndHiddenFalseAndDeletedFalseAndNotification_DeletedFalse(Long userId);
}