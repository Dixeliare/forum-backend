package com.longdx.forum_backend.repository;

import com.longdx.forum_backend.model.Share;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShareRepository extends JpaRepository<Share, Long> {

    // Find shares by post
    Page<Share> findByPost_Id(Long postId, Pageable pageable);

    // Find shares by user
    Page<Share> findByUser_InternalId(Long userId, Pageable pageable);

    // Count shares for a post
    long countByPost_Id(Long postId);

    // Find all users who shared a post
    @Query("SELECT s.user.internalId FROM Share s WHERE s.post.id = :postId")
    List<Long> findUserIdsByPostId(@Param("postId") Long postId);

    // Check if user shared a post
    boolean existsByUser_InternalIdAndPost_Id(Long userId, Long postId);
}

