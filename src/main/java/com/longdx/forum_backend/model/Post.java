package com.longdx.forum_backend.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "posts")
@Data
public class Post {

    @Id
    @Tsid
    @Column(name = "id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @NotNull
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id")
    private Community community; // NULL = Personal Post, NOT NULL = Community Post

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic; // CHỈ DÙNG CHO PERSONAL POSTS. NULL = post không có topic

    @Column(name = "title", length = 255)
    @Size(max = 255)
    private String title; // Title (optional, có thể NULL cho social posts)

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    @NotBlank
    private String content;

    @Column(name = "public_id", unique = true, nullable = false, length = 12)
    @NotBlank
    @Size(max = 12)
    private String publicId; // Short ID cho URL

    @Column(name = "slug", length = 350)
    @Size(max = 350)
    private String slug; // Slug (optional, cho SEO)

    @Column(name = "is_nsfw", nullable = false)
    private Boolean isNsfw = false; // NSFW flag (kế thừa từ community nếu có)

    // Stats cho Ranking Algorithm (Gravity Score)
    @Column(name = "likes_count", nullable = false)
    private Integer likesCount = 0;

    @Column(name = "comments_count", nullable = false)
    private Integer commentsCount = 0;

    @Column(name = "shares_count", nullable = false)
    private Integer sharesCount = 0;

    @Column(name = "saves_count", nullable = false)
    private Integer savesCount = 0;

    @Column(name = "tags_count", nullable = false)
    private Integer tagsCount = 0; // Số lượt tag bạn bè trong comment

    @Column(name = "caption_expands_count", nullable = false)
    private Integer captionExpandsCount = 0; // Số lượt bấm "Xem thêm"

    @Column(name = "media_clicks_count", nullable = false)
    private Integer mediaClicksCount = 0; // Số lượt click vào ảnh/video

    @Column(name = "dwell_7s_count", nullable = false)
    private Integer dwell7sCount = 0; // Số lượt ở lại > 7 giây

    @Column(name = "viral_score", nullable = false, precision = 20, scale = 10)
    private BigDecimal viralScore = BigDecimal.ZERO; // Điểm tính từ Gravity Algorithm

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}

