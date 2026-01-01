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
@Table(name = "forum_threads")
@Data
public class ForumThread {

    @Id
    @Tsid
    @Column(name = "id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_forum_id", nullable = false)
    @NotNull
    private SubForum subForum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @NotNull
    private User author;

    @Column(name = "title", nullable = false, length = 500)
    @NotBlank
    @Size(max = 500)
    private String title; // Bắt buộc có tiêu đề

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    @NotBlank
    private String content; // Bắt buộc có nội dung

    @Column(name = "public_id", unique = true, nullable = false, length = 12)
    @NotBlank
    @Size(max = 12)
    private String publicId; // Short ID cho URL (slug.public_id)

    @Column(name = "slug", length = 350)
    @Size(max = 350)
    private String slug; // Slug từ title (để SEO)

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "last_activity_at", nullable = false)
    private OffsetDateTime lastActivityAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        lastActivityAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}

