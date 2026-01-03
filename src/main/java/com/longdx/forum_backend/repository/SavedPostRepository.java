package com.longdx.forum_backend.repository;

import com.longdx.forum_backend.model.SavedPost;
import com.longdx.forum_backend.model.SavedPostId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SavedPostRepository extends JpaRepository<SavedPost, SavedPostId> {

    // Check if user saved a post
    boolean existsByUserIdAndPostId(Long userId, Long postId);

    // Find saved post by user and post
    Optional<SavedPost> findByUserIdAndPostId(Long userId, Long postId);

    // Find all posts saved by a user
    @Query("SELECT sp.postId FROM SavedPost sp WHERE sp.userId = :userId ORDER BY sp.savedAt DESC")
    Page<Long> findPostIdsByUserId(@Param("userId") Long userId, Pageable pageable);

    // Find all saved posts by user (with post entity)
    @Query("SELECT sp FROM SavedPost sp WHERE sp.userId = :userId ORDER BY sp.savedAt DESC")
    Page<SavedPost> findByUserId(@Param("userId") Long userId, Pageable pageable);

    // Count saved posts by user
    long countByUserId(Long userId);

    // Delete saved post by user and post
    void deleteByUserIdAndPostId(Long userId, Long postId);
}

