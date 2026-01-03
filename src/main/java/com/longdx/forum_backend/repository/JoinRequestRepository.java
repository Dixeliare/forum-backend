package com.longdx.forum_backend.repository;

import com.longdx.forum_backend.model.JoinRequest;
import com.longdx.forum_backend.model.JoinRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {

    // Find requests by user
    List<JoinRequest> findByUser_InternalId(Long userId);

    // Find requests by forum
    Page<JoinRequest> findByForum_Id(Long forumId, Pageable pageable);

    // Find requests by community
    Page<JoinRequest> findByCommunity_Id(Long communityId, Pageable pageable);

    // Find pending requests by forum
    @Query("SELECT jr FROM JoinRequest jr WHERE jr.forum.id = :forumId AND jr.status = 'PENDING'")
    Page<JoinRequest> findPendingByForumId(@Param("forumId") Long forumId, Pageable pageable);

    // Find pending requests by community
    @Query("SELECT jr FROM JoinRequest jr WHERE jr.community.id = :communityId AND jr.status = 'PENDING'")
    Page<JoinRequest> findPendingByCommunityId(@Param("communityId") Long communityId, Pageable pageable);

    // Find request by user and forum
    Optional<JoinRequest> findByUser_InternalIdAndForum_Id(Long userId, Long forumId);

    // Find request by user and community
    Optional<JoinRequest> findByUser_InternalIdAndCommunity_Id(Long userId, Long communityId);

    // Count pending requests by forum
    long countByForum_IdAndStatus(Long forumId, JoinRequestStatus status);

    // Count pending requests by community
    long countByCommunity_IdAndStatus(Long communityId, JoinRequestStatus status);
}

