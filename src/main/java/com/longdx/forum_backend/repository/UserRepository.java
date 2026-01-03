package com.longdx.forum_backend.repository;

import com.longdx.forum_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity
 * 
 * Pattern:
 * - Extends JpaRepository<Entity, ID>
 * - Use Optional for find methods
 * - Add custom query methods with @Query if needed
 * - Use method naming convention for simple queries
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find by public_id (NanoID) - used in public APIs
    Optional<User> findByPublicId(String publicId);

    // Find by email - used in authentication
    Optional<User> findByEmail(String email);

    // Check if email exists
    boolean existsByEmail(String email);

    // Check if public_id exists
    boolean existsByPublicId(String publicId);

    // Find active users only
    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.accountStatus = 'ACTIVE'")
    Optional<User> findActiveUserByPublicId(String publicId);

    // Search users by display name (case-insensitive, partial match)
    @Query("SELECT u FROM User u WHERE LOWER(u.displayName) LIKE LOWER(CONCAT('%', :keyword, '%')) AND u.isActive = true AND u.accountStatus = 'ACTIVE'")
    java.util.List<User> searchByDisplayName(@Param("keyword") String keyword);
}

