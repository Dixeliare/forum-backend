package com.longdx.forum_backend.repository;

import com.longdx.forum_backend.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Find comments by post
    Page<Comment> findByPost_Id(Long postId, Pageable pageable);

    // Find comments by thread
    Page<Comment> findByThread_Id(Long threadId, Pageable pageable);

    // Find comments by author
    Page<Comment> findByAuthor_InternalId(Long authorId, Pageable pageable);

    // Find root comments (no parent) for a post
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.parentComment IS NULL ORDER BY c.createdAt ASC")
    Page<Comment> findRootCommentsByPost(@Param("postId") Long postId, Pageable pageable);

    // Find root comments (no parent) for a thread
    @Query("SELECT c FROM Comment c WHERE c.thread.id = :threadId AND c.parentComment IS NULL ORDER BY c.createdAt ASC")
    Page<Comment> findRootCommentsByThread(@Param("threadId") Long threadId, Pageable pageable);

    // Find replies to a comment
    @Query("SELECT c FROM Comment c WHERE c.parentComment.id = :parentCommentId ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentComment(@Param("parentCommentId") Long parentCommentId);

    // Count comments by post
    long countByPost_Id(Long postId);

    // Count comments by thread
    long countByThread_Id(Long threadId);
}

