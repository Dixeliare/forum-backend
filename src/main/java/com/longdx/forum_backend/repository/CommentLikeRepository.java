package com.longdx.forum_backend.repository;

import com.longdx.forum_backend.model.CommentLike;
import com.longdx.forum_backend.model.CommentLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, CommentLikeId> {

    // Check if user liked a comment
    boolean existsByUserIdAndCommentId(Long userId, Long commentId);

    // Find like by user and comment
    Optional<CommentLike> findByUserIdAndCommentId(Long userId, Long commentId);

    // Count likes for a comment
    long countByCommentId(Long commentId);

    // Find all likes for a comment
    List<CommentLike> findByCommentId(Long commentId);

    // Find all comments liked by a user
    @Query("SELECT cl.commentId FROM CommentLike cl WHERE cl.userId = :userId")
    List<Long> findCommentIdsByUserId(@Param("userId") Long userId);

    // Delete like by user and comment
    void deleteByUserIdAndCommentId(Long userId, Long commentId);
}

