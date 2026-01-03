package com.longdx.forum_backend.repository;

import com.longdx.forum_backend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Spring tự tạo query từ method name
    List<Category> findByForum_IdOrderByDisplayOrderAsc(Long forumId);

    Optional<Category> findByForum_IdAndSlug(Long forumId, String slug);
}

