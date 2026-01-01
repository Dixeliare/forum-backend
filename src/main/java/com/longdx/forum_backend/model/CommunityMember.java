package com.longdx.forum_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.OffsetDateTime;

@Entity
@Table(name = "community_members")
@Data
@IdClass(CommunityMemberId.class) // Composite Primary Key
public class CommunityMember {

    @Id
    @Column(name = "community_id", nullable = false)
    private Long communityId;

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id", insertable = false, updatable = false)
    private Community community;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "role", nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String role = "MEMBER"; // MEMBER, MODERATOR, ADMIN

    @Column(name = "joined_at", nullable = false, updatable = false)
    private OffsetDateTime joinedAt;

    @Column(name = "status", length = 255)
    @Size(max = 255)
    private String status = "ACTIVE"; // ACTIVE, BANNED, LEFT

    @PrePersist
    protected void onCreate() {
        joinedAt = OffsetDateTime.now();
    }
}

