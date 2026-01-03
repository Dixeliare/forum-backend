package com.longdx.forum_backend.repository;

import com.longdx.forum_backend.model.Notification;
import com.longdx.forum_backend.model.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Find notifications by user
    Page<Notification> findByUser_InternalIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Find unread notifications by user
    @Query("SELECT n FROM Notification n WHERE n.user.internalId = :userId AND n.isRead = false ORDER BY n.createdAt DESC")
    Page<Notification> findUnreadByUserId(@Param("userId") Long userId, Pageable pageable);

    // Find notifications by type
    Page<Notification> findByUser_InternalIdAndType(Long userId, NotificationType type, Pageable pageable);

    // Count unread notifications
    long countByUser_InternalIdAndIsReadFalse(Long userId);

    // Mark all notifications as read for a user
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.internalId = :userId AND n.isRead = false")
    void markAllAsReadByUserId(@Param("userId") Long userId);
}

