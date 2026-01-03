package com.longdx.forum_backend.repository;

import com.longdx.forum_backend.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Post entity
 * 
 * Pattern:
 * - Use Pageable for pagination
 * - Custom queries for complex filtering
 * - Join with related entities when needed
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // Find by public_id
    Optional<Post> findByPublicId(String publicId);

    // Find posts by author
    Page<Post> findByAuthor_InternalId(Long authorId, Pageable pageable);

    // Find posts by community
    Page<Post> findByCommunity_Id(Long communityId, Pageable pageable);

    // Find posts by topic
    Page<Post> findByTopic_Id(Long topicId, Pageable pageable);

    // Find personal posts (community_id IS NULL)
    @Query("SELECT p FROM Post p WHERE p.community IS NULL AND p.author.internalId = :authorId")
    Page<Post> findPersonalPostsByAuthor(@Param("authorId") Long authorId, Pageable pageable);

    // Find community posts
    @Query("SELECT p FROM Post p WHERE p.community.id = :communityId")
    Page<Post> findCommunityPosts(@Param("communityId") Long communityId, Pageable pageable);

    // Find posts by viral score (for ranking algorithm)
    @Query("SELECT p FROM Post p WHERE p.viralScore > 0 ORDER BY p.viralScore DESC")
    Page<Post> findTopViralPosts(Pageable pageable);

    // Find posts by topic with pagination
    @Query("SELECT p FROM Post p WHERE p.topic.id = :topicId ORDER BY p.createdAt DESC")
    Page<Post> findPostsByTopic(@Param("topicId") Long topicId, Pageable pageable);
}

