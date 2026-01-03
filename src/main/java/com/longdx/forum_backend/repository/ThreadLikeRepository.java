package com.longdx.forum_backend.repository;

import com.longdx.forum_backend.model.ThreadLike;
import com.longdx.forum_backend.model.ThreadLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThreadLikeRepository extends JpaRepository<ThreadLike, ThreadLikeId> {

    // Check if user liked a thread
    boolean existsByUserIdAndThreadId(Long userId, Long threadId);

    // Find like by user and thread
    Optional<ThreadLike> findByUserIdAndThreadId(Long userId, Long threadId);

    // Count likes for a thread
    long countByThreadId(Long threadId);

    // Find all likes for a thread
    List<ThreadLike> findByThreadId(Long threadId);

    // Find all threads liked by a user
    @Query("SELECT tl.threadId FROM ThreadLike tl WHERE tl.userId = :userId")
    List<Long> findThreadIdsByUserId(@Param("userId") Long userId);

    // Delete like by user and thread
    void deleteByUserIdAndThreadId(Long userId, Long threadId);
}

