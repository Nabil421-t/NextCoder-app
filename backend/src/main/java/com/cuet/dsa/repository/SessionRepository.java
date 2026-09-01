package com.cuet.dsa.repository;

import com.cuet.dsa.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, String> {

    // Used in loginUser() to enforce the max-concurrent-sessions cap.
    List<Session> findByUserIdAndStatus(Long userId, Session.SessionStatus status);

    // Cheaper alternative to loading the full list just to check a count —
    // worth switching to once the cap check matters for scale.
    long countByUserIdAndStatus(Long userId, Session.SessionStatus status);

    // For a "log out everywhere" / list-my-devices endpoint.
    List<Session> findByUserId(Long userId);

    // Bulk-revoke, e.g. on password change or "log out all other devices".
    @Modifying
    @Query("UPDATE Session s SET s.status = 'REVOKED' " +
            "WHERE s.user.id = :userId AND s.status = 'ACTIVE'")
    int revokeAllActiveSessionsForUser(@Param("userId") Long userId);

    // For the scheduled cleanup job — batch-expire anything past expiresAt
    // that's still marked ACTIVE, instead of relying only on the filter's
    // lazy per-request check.
    @Modifying
    @Query("UPDATE Session s SET s.status = 'EXPIRED' " +
            "WHERE s.status = 'ACTIVE' AND s.expiresAt < :now")
    int expireStaleSessions(@Param("now") LocalDateTime now);
    @Query("SELECT s FROM Session s JOIN FETCH s.user WHERE s.sessionId = :sessionId")
    Optional<Session> findByIdWithUser(@Param("sessionId") String sessionId);
}