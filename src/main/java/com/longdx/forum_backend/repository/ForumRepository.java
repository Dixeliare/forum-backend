package com.longdx.forum_backend.repository;

import com.longdx.forum_backend.model.Forum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Forum entity
 * 
 * Pattern:
 * - Basic CRUD operations
 * - Custom queries for business logic
 */
@Repository
public interface ForumRepository extends JpaRepository<Forum, Long> {

    // Find by public_id
    Optional<Forum> findByPublicId(String publicId);

    // Find by slug and public_id (for URL resolution)
    Optional<Forum> findBySlugAndPublicId(String slug, String publicId);

    // Find searchable forums only
    @Query("SELECT f FROM Forum f WHERE f.isSearchable = true")
    List<Forum> findSearchableForums();

    // Find forums by owner
    List<Forum> findByOwner_InternalId(Long ownerId);
}

