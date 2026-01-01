package com.longdx.forum_backend.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.OffsetDateTime;

@Entity
@Table(name = "topics")
@Data
public class Topic {

    @Id
    @Tsid
    @Column(name = "id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Column(name = "name", unique = true, nullable = false)
    @NotBlank
    @Size(max = 255)
    private String name; // Tên topic (e.g., "Technology", "Hà Nội")

    @Column(name = "slug", unique = true, nullable = false)
    @NotBlank
    @Size(max = 255)
    private String slug; // URL slug (e.g., "technology", "hanoi")

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // Mô tả topic

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl; // Ảnh đại diện topic (optional)

    @Column(name = "post_count", nullable = false)
    private Integer postCount = 0; // Số posts có topic này (denormalized)

    @Column(name = "follower_count", nullable = false)
    private Integer followerCount = 0; // Số users follow topic này (denormalized)

    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false; // Topic nổi bật (admin feature)

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

