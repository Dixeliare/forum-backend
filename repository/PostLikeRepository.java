package com.longdx.forum_backend.repository;

import com.longdx.forum_backend.model.PostLike;
import com.longdx.forum_backend.model.PostLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for PostLike entity (Composite Key)
 * 
 * Pattern:
 * - Use composite key ID class (PostLikeId)
 * - Custom methods for checking and counting
 * - Use native queries for performance if needed
 */
@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {

    // Check if user liked a post
    boolean existsByUserIdAndPostId(Long userId, Long postId);

    // Find like by user and post
    Optional<PostLike> findByUserIdAndPostId(Long userId, Long postId);

    // Count likes for a post
    long countByPostId(Long postId);

    // Find all likes for a post
    List<PostLike> findByPostId(Long postId);

    // Find all posts liked by a user
    @Query("SELECT pl.postId FROM PostLike pl WHERE pl.userId = :userId")
    List<Long> findPostIdsByUserId(@Param("userId") Long userId);

    // Delete like by user and post
    void deleteByUserIdAndPostId(Long userId, Long postId);
}

