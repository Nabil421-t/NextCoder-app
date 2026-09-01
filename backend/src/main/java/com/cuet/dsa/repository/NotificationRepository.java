package com.cuet.dsa.repository;

import com.cuet.dsa.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByType(String type);
    Optional<Notification> findByExternalId(String externalId);

    List<Notification> findBySource(String source);
}