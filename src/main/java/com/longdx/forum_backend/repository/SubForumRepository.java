package com.longdx.forum_backend.repository;

import com.longdx.forum_backend.model.SubForum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubForumRepository extends JpaRepository<SubForum, Long> {

    // Spring tự tạo query từ method name
    List<SubForum> findByCategory_IdOrderByDisplayOrderAsc(Long categoryId);

    List<SubForum> findByCategory_IdOrderByLastActivityAtDesc(Long categoryId);

    Optional<SubForum> findByCategory_IdAndSlug(Long categoryId, String slug);
}

