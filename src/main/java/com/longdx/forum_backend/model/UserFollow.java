package com.longdx.forum_backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_follows")
@Data
@IdClass(UserFollowId.class) // Composite Primary Key
public class UserFollow {

    @Id
    @Column(name = "follower_id", nullable = false)
    private Long followerId; // Người đi theo dõi (User A)

    @Id
    @Column(name = "target_id", nullable = false)
    private Long targetId; // Người được theo dõi (User B)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", insertable = false, updatable = false)
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id", insertable = false, updatable = false)
    private User target;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FollowStatus status = FollowStatus.ACCEPTED; // PENDING nếu target là private account

    @Column(name = "requested_at", nullable = false, updatable = false)
    private OffsetDateTime requestedAt; // Thời gian request

    @Column(name = "accepted_at")
    private OffsetDateTime acceptedAt; // Thời gian accept (nếu status = ACCEPTED)

    @Column(name = "rejected_at")
    private OffsetDateTime rejectedAt; // Thời gian reject (nếu status = REJECTED)

    @PrePersist
    protected void onCreate() {
        requestedAt = OffsetDateTime.now();
        if (status == FollowStatus.ACCEPTED) {
            acceptedAt = OffsetDateTime.now();
        }
    }
}

