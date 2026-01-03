package com.longdx.forum_backend.repository;

import com.longdx.forum_backend.model.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {

    // Find media by post
    List<Media> findByPost_IdOrderByDisplayOrderAsc(Long postId);

    // Find media by thread
    List<Media> findByThread_IdOrderByDisplayOrderAsc(Long threadId);

    // Find media by comment
    List<Media> findByComment_IdOrderByDisplayOrderAsc(Long commentId);

    // Find media by user
    List<Media> findByUser_InternalId(Long userId);

    // Find all media for a post ordered by position
    @Query("SELECT m FROM Media m WHERE m.post.id = :postId ORDER BY m.position ASC, m.displayOrder ASC")
    List<Media> findByPostIdOrdered(@Param("postId") Long postId);

    // Find all media for a thread ordered by position
    @Query("SELECT m FROM Media m WHERE m.thread.id = :threadId ORDER BY m.position ASC, m.displayOrder ASC")
    List<Media> findByThreadIdOrdered(@Param("threadId") Long threadId);
}

