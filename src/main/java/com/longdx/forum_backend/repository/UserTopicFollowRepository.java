package com.longdx.forum_backend.repository;

import com.longdx.forum_backend.model.UserTopicFollow;
import com.longdx.forum_backend.model.UserTopicFollowId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTopicFollowRepository extends JpaRepository<UserTopicFollow, UserTopicFollowId> {

    // Check if user follows topic
    boolean existsByUserIdAndTopicId(Long userId, Long topicId);

    // Find follow by user and topic
    Optional<UserTopicFollow> findByUserIdAndTopicId(Long userId, Long topicId);

    // Find all topics followed by user
    @Query("SELECT utf.topicId FROM UserTopicFollow utf WHERE utf.userId = :userId")
    Page<Long> findTopicIdsByUserId(@Param("userId") Long userId, Pageable pageable);

    // Find all follows by user
    List<UserTopicFollow> findByUserId(Long userId);

    // Count topics followed by user
    long countByUserId(Long userId);

    // Count followers of a topic
    long countByTopicId(Long topicId);

    // Delete follow by user and topic
    void deleteByUserIdAndTopicId(Long userId, Long topicId);
}

