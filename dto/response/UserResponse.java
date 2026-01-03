package com.longdx.forum_backend.dto.response;

import com.longdx.forum_backend.model.User;

import java.time.OffsetDateTime;

/**
 * Response DTO for User entity
 * 
 * Pattern:
 * - Only expose necessary fields (not password, internal_id, etc.)
 * - Use public_id instead of internal_id for public APIs
 * - Include timestamps for client-side display
 * - Static factory method from entity
 */
public record UserResponse(
        String publicId,           // NanoID for public use
        String displayName,
        String email,
        String bio,
        String avatarUrl,
        Boolean isPrivate,
        Boolean isVerified,
        Boolean isActive,
        OffsetDateTime createdAt,
        OffsetDateTime lastLoginAt
) {
    /**
     * Factory method to create UserResponse from User entity
     * Pattern: Use static factory method for mapping
     */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getPublicId(),
                user.getDisplayName(),
                user.getEmail(),
                user.getBio(),
                user.getAvatarUrl(),
                user.getIsPrivate(),
                user.getIsVerified(),
                user.getIsActive(),
                user.getCreatedAt(),
                user.getLastLoginAt()
        );
    }
}

