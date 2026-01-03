package com.longdx.forum_backend.repository;

import com.longdx.forum_backend.model.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {

    // Find by slug
    Optional<Topic> findBySlug(String slug);

    // Find by name
    Optional<Topic> findByName(String name);

    // Spring tự tạo query từ method name
    Page<Topic> findByIsFeaturedTrueOrderByFollowerCountDesc(Pageable pageable);

    // Spring tự tạo query từ method name
    Page<Topic> findAllByOrderByFollowerCountDesc(Pageable pageable);

    Page<Topic> findAllByOrderByPostCountDesc(Pageable pageable);

    // Search topics by name
    @Query("SELECT t FROM Topic t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Topic> searchByName(@Param("keyword") String keyword, Pageable pageable);
}

