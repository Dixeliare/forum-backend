package com.longdx.forum_backend.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @Tsid
    @Column(name = "internal_id")
    @JsonSerialize(using = ToStringSerializer.class) // Convert Long -> String cho JSON (tránh mất độ chính xác)
    private Long internalId;

    @Column(name = "public_id", unique = true, nullable = false, length = 20)
    @NotBlank
    @Size(max = 20)
    private String publicId; // NanoID Suffix (e.g., "Xy9zQ2mP")

    @Column(name = "display_name", nullable = false)
    @NotBlank
    @Size(max = 255)
    private String displayName; // Tên hiển thị (có thể chứa Emoji, CJK)

    @Column(name = "email", unique = true, nullable = false)
    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    @Column(name = "password_hash", nullable = false)
    @NotBlank
    @Size(max = 255)
    private String passwordHash; // Bcrypt hash

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "avatar_url", length = 255)
    @Size(max = 255)
    private String avatarUrl;

    @Column(name = "banner_id", length = 255)
    @Size(max = 255)
    private String bannerId;

    @Column(name = "settings_display_sensitive_media", nullable = false)
    private Boolean settingsDisplaySensitiveMedia = false; // NSFW setting

    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate = false; // Private account (cần approval khi follow)

    @Column(name = "account_status", nullable = false, length = 255)
    @NotBlank
    @Size(max = 255)
    private String accountStatus = "ACTIVE"; // ACTIVE, SUSPENDED, DELETED

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt; // Soft delete

    @Column(name = "deletion_reason", length = 255)
    @Size(max = 255)
    private String deletionReason;

    @Column(name = "timezone", length = 255)
    @Size(max = 255)
    private String timezone = "UTC";

    @Column(name = "last_public_id_changed_at")
    private OffsetDateTime lastPublicIdChangedAt; // Khi nào public_id thay đổi

    @Column(name = "is_searchable_by_public_id", nullable = false)
    private Boolean isSearchableByPublicId = true; // Có thể search bằng public_id

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}

