package com.longdx.forum_backend.repository;

import com.longdx.forum_backend.model.Community;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Long> {

    // Find by public_id
    Optional<Community> findByPublicId(String publicId);

    // Find by slug and public_id (for URL resolution)
    Optional<Community> findBySlugAndPublicId(String slug, String publicId);

    // Find searchable communities
    @Query("SELECT c FROM Community c WHERE c.isSearchable = true")
    Page<Community> findSearchableCommunities(Pageable pageable);

    // Find public communities (not private and searchable)
    @Query("SELECT c FROM Community c WHERE c.isPrivate = false AND c.isSearchable = true")
    Page<Community> findPublicCommunities(Pageable pageable);

    // Find private communities
    @Query("SELECT c FROM Community c WHERE c.isPrivate = true")
    Page<Community> findPrivateCommunities(Pageable pageable);

    // Find communities by owner
    List<Community> findByOwner_InternalId(Long ownerId);

    // Find communities by owner with pagination
    Page<Community> findByOwner_InternalId(Long ownerId, Pageable pageable);

    // Search communities by name (searchable only)
    @Query("SELECT c FROM Community c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND c.isSearchable = true")
    Page<Community> searchByName(@Param("keyword") String keyword, Pageable pageable);

    // Find communities by member count (popular)
    @Query("SELECT c FROM Community c WHERE c.isSearchable = true ORDER BY c.memberCount DESC")
    Page<Community> findPopularCommunities(Pageable pageable);

    // Find communities by post count (active)
    @Query("SELECT c FROM Community c WHERE c.isSearchable = true ORDER BY c.postCount DESC")
    Page<Community> findActiveCommunities(Pageable pageable);

    // Find communities by created date (newest)
    @Query("SELECT c FROM Community c WHERE c.isSearchable = true ORDER BY c.createdAt DESC")
    Page<Community> findNewestCommunities(Pageable pageable);

    // Check if public_id exists
    boolean existsByPublicId(String publicId);

    // Check if slug and public_id combination exists
    boolean existsBySlugAndPublicId(String slug, String publicId);

    // Count communities by owner
    long countByOwner_InternalId(Long ownerId);

    // Find NSFW communities
    @Query("SELECT c FROM Community c WHERE c.isNsfw = true AND c.isSearchable = true")
    Page<Community> findNsfwCommunities(Pageable pageable);

    // Find non-NSFW communities
    @Query("SELECT c FROM Community c WHERE c.isNsfw = false AND c.isSearchable = true")
    Page<Community> findNonNsfwCommunities(Pageable pageable);
}
