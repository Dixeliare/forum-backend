package com.longdx.forum_backend.repository;

import com.longdx.forum_backend.model.ForumThread;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForumThreadRepository extends JpaRepository<ForumThread, Long> {

    // Find by public_id
    Optional<ForumThread> findByPublicId(String publicId);

    // Find by sub-forum
    Page<ForumThread> findBySubForum_Id(Long subForumId, Pageable pageable);

    // Find by author
    Page<ForumThread> findByAuthor_InternalId(Long authorId, Pageable pageable);

    // Spring tự tạo query từ method name
    Page<ForumThread> findBySubForum_IdOrderByLastActivityAtDesc(Long subForumId, Pageable pageable);

    Page<ForumThread> findBySubForum_IdOrderByCreatedAtDesc(Long subForumId, Pageable pageable);

    // Find by slug and public_id (for URL resolution)
    Optional<ForumThread> findBySlugAndPublicId(String slug, String publicId);
}

