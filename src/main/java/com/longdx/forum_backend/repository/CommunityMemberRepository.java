package com.longdx.forum_backend.repository;

import com.longdx.forum_backend.model.CommunityMember;
import com.longdx.forum_backend.model.CommunityMemberId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityMemberRepository extends JpaRepository<CommunityMember, CommunityMemberId> {

    // Check if user is member of community
    boolean existsByCommunityIdAndUserId(Long communityId, Long userId);

    // Find member by community and user
    Optional<CommunityMember> findByCommunityIdAndUserId(Long communityId, Long userId);

    // Find all members of a community
    @Query("SELECT cm.userId FROM CommunityMember cm WHERE cm.communityId = :communityId AND cm.status = 'ACTIVE'")
    Page<Long> findMemberIdsByCommunityId(@Param("communityId") Long communityId, Pageable pageable);

    // Find all members with details
    @Query("SELECT cm FROM CommunityMember cm WHERE cm.communityId = :communityId AND cm.status = 'ACTIVE'")
    Page<CommunityMember> findMembersByCommunityId(@Param("communityId") Long communityId, Pageable pageable);

    // Find all communities user is member of
    @Query("SELECT cm.communityId FROM CommunityMember cm WHERE cm.userId = :userId AND cm.status = 'ACTIVE'")
    List<Long> findCommunityIdsByUserId(@Param("userId") Long userId);

    // Find members by role
    @Query("SELECT cm FROM CommunityMember cm WHERE cm.communityId = :communityId AND cm.role = :role AND cm.status = 'ACTIVE'")
    List<CommunityMember> findMembersByRole(@Param("communityId") Long communityId, @Param("role") String role);

    // Count members of a community
    long countByCommunityIdAndStatus(Long communityId, String status);

    // Count communities user is member of
    long countByUserIdAndStatus(Long userId, String status);
}

