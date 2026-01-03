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
@Table(name = "forums")
@Data
public class Forum {

    @Id
    @Tsid
    @Column(name = "id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Column(name = "name", nullable = false)
    @NotBlank
    @Size(max = 255)
    private String name;

    @Column(name = "slug", nullable = false)
    @NotBlank
    @Size(max = 255)
    private String slug; // URL slug (không unique, có thể trùng - chỉ dùng cho SEO)

    @Column(name = "public_id", unique = true, nullable = false, length = 10)
    @NotBlank
    @Size(max = 10)
    private String publicId; // Short ID cho URL (slug.public_id) - Dùng để query DB

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @NotNull
    private User owner; // Admin tạo forum

    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate = false; // Private forum

    @Column(name = "is_searchable", nullable = false)
    private Boolean isSearchable = true; // Có thể search (discoverable)

    @Column(name = "is_nsfw", nullable = false)
    private Boolean isNsfw = false; // NSFW flag

    @Column(name = "member_count", nullable = false)
    private Integer memberCount = 0; // Số thành viên

    @Column(name = "thread_count", nullable = false)
    private Integer threadCount = 0; // Số threads (denormalized)

    @Column(name = "category_count", nullable = false)
    private Integer categoryCount = 0; // Số categories (denormalized)

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

