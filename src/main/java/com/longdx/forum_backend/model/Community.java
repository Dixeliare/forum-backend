package com.longdx.forum_backend.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.OffsetDateTime;

@Entity
@Table(name = "communities")
@Data
public class Community {

    @Id
    @Tsid
    @Column(name = "id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Column(name = "name", nullable = false)
    @NotBlank
    @Size(max = 255)
    private String name; // Display name

    @Column(name = "slug", nullable = false)
    @NotBlank
    @Size(max = 255)
    private String slug; // Không unique (có thể trùng)

    @Column(name = "public_id", unique = true, nullable = false, length = 10)
    @NotBlank
    @Size(max = 10)
    private String publicId; // Short ID cho URL (slug.public_id)

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @NotNull
    private User owner;

    @Column(name = "avatar_url", length = 255)
    @Size(max = 255)
    private String avatarUrl;

    @Column(name = "cover_url", length = 255)
    @Size(max = 255)
    private String coverUrl;

    @Column(name = "is_nsfw", nullable = false)
    private Boolean isNsfw = false; // NSFW flag

    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate = false; // Private community

    @Column(name = "is_searchable", nullable = false)
    private Boolean isSearchable = true; // Có thể search

    @Column(name = "member_count", nullable = false)
    private Integer memberCount = 0;

    @Column(name = "post_count", nullable = false)
    private Integer postCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_user_id")
    private User updatedUser; // User cập nhật gần nhất

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}

