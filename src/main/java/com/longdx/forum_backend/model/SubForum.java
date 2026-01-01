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
@Table(name = "sub_forums",
       uniqueConstraints = @UniqueConstraint(columnNames = {"category_id", "slug"}))
@Data
public class SubForum {

    @Id
    @Tsid
    @Column(name = "id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull
    private Category category;

    @Column(name = "name", nullable = false)
    @NotBlank
    @Size(max = 255)
    private String name; // Display name

    @Column(name = "slug", nullable = false)
    @NotBlank
    @Size(max = 255)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0; // Thứ tự hiển thị (nếu dùng manual sorting)

    @Column(name = "last_activity_at", nullable = false)
    private OffsetDateTime lastActivityAt; // Activity gần nhất (comment/thread mới)

    @Column(name = "last_thread_id")
    private Long lastThreadId; // Thread có activity gần nhất (FK sẽ thêm sau bằng ALTER TABLE)

    @Column(name = "last_comment_id")
    private Long lastCommentId; // Comment gần nhất (FK sẽ thêm sau bằng ALTER TABLE)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_activity_by_user_id")
    private User lastActivityByUser; // User tạo activity gần nhất (hiển thị tên)

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

