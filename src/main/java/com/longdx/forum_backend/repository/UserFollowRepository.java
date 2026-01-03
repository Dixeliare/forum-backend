package com.longdx.forum_backend.repository;

import com.longdx.forum_backend.model.FollowStatus;
import com.longdx.forum_backend.model.UserFollow;
import com.longdx.forum_backend.model.UserFollowId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, UserFollowId> {

    // Check if user follows target
    boolean existsByFollowerIdAndTargetId(Long followerId, Long targetId);

    // Find follow relationship
    Optional<UserFollow> findByFollowerIdAndTargetId(Long followerId, Long targetId);

    // Find all users that a user is following (following list)
    @Query("SELECT uf.targetId FROM UserFollow uf WHERE uf.followerId = :userId AND uf.status = 'ACCEPTED'")
    Page<Long> findFollowingIdsByUserId(@Param("userId") Long userId, Pageable pageable);

    // Find all users following a user (followers list)
    @Query("SELECT uf.followerId FROM UserFollow uf WHERE uf.targetId = :userId AND uf.status = 'ACCEPTED'")
    Page<Long> findFollowerIdsByUserId(@Param("userId") Long userId, Pageable pageable);

    // Count following
    long countByFollowerIdAndStatus(Long followerId, FollowStatus status);

    // Count followers
    long countByTargetIdAndStatus(Long targetId, FollowStatus status);

    // Find pending follow requests for a user
    @Query("SELECT uf FROM UserFollow uf WHERE uf.targetId = :userId AND uf.status = 'PENDING'")
    Page<UserFollow> findPendingRequestsByTargetId(@Param("userId") Long userId, Pageable pageable);

    // Find all accepted follows
    @Query("SELECT uf FROM UserFollow uf WHERE uf.followerId = :userId AND uf.status = 'ACCEPTED'")
    List<UserFollow> findAcceptedFollowsByFollowerId(@Param("userId") Long userId);
}

