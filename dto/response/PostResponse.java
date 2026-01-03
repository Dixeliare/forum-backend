package com.longdx.forum_backend.dto.response;

import com.longdx.forum_backend.model.Post;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Response DTO for Post entity
 * 
 * Pattern:
 * - Include nested objects (author, community, topic) as simplified DTOs
 * - Include statistics (likes, comments, etc.)
 * - Use factory method for mapping
 */
public record PostResponse(
        String publicId,
        String title,
        String content,
        String slug,
        Boolean isNsfw,
        
        // Nested objects (simplified)
        UserSummary author,
        CommunitySummary community,
        TopicSummary topic,
        
        // Statistics
        Integer likesCount,
        Integer commentsCount,
        Integer sharesCount,
        Integer savesCount,
        BigDecimal viralScore,
        
        // Timestamps
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    /**
     * Factory method to create PostResponse from Post entity
     */
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getPublicId(),
                post.getTitle(),
                post.getContent(),
                post.getSlug(),
                post.getIsNsfw(),
                post.getAuthor() != null ? UserSummary.from(post.getAuthor()) : null,
                post.getCommunity() != null ? CommunitySummary.from(post.getCommunity()) : null,
                post.getTopic() != null ? TopicSummary.from(post.getTopic()) : null,
                post.getLikesCount(),
                post.getCommentsCount(),
                post.getSharesCount(),
                post.getSavesCount(),
                post.getViralScore(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    // Nested summary DTOs
    public record UserSummary(String publicId, String displayName, String avatarUrl) {
        public static UserSummary from(com.longdx.forum_backend.model.User user) {
            return new UserSummary(user.getPublicId(), user.getDisplayName(), user.getAvatarUrl());
        }
    }

    public record CommunitySummary(String publicId, String name, String avatarUrl) {
        public static CommunitySummary from(com.longdx.forum_backend.model.Community community) {
            return new CommunitySummary(community.getPublicId(), community.getName(), community.getAvatarUrl());
        }
    }

    public record TopicSummary(String slug, String name) {
        public static TopicSummary from(com.longdx.forum_backend.model.Topic topic) {
            return new TopicSummary(topic.getSlug(), topic.getName());
        }
    }
}

