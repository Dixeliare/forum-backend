package com.longdx.forum_backend.service;

import com.longdx.forum_backend.dto.request.CreateUserRequest;
import com.longdx.forum_backend.dto.response.UserResponse;
import com.longdx.forum_backend.model.User;

import java.util.Optional;

/**
 * Service interface for User operations
 * 
 * Pattern:
 * - Define business logic methods
 * - Return DTOs, not entities
 * - Use Optional for nullable returns
 * - Throw custom exceptions for error cases
 */
public interface UserService {

    /**
     * Create a new user
     * @param request User creation request
     * @return Created user response
     * @throws IllegalArgumentException if email already exists
     */
    UserResponse createUser(CreateUserRequest request);

    /**
     * Get user by public ID
     * @param publicId User's public ID (NanoID)
     * @return User response if found
     */
    Optional<UserResponse> getUserByPublicId(String publicId);

    /**
     * Get user by email
     * @param email User's email
     * @return User entity (for internal use, e.g., authentication)
     */
    Optional<User> getUserByEmail(String email);

    /**
     * Update user profile
     * @param publicId User's public ID
     * @param displayName New display name
     * @param bio New bio
     * @return Updated user response
     * @throws IllegalArgumentException if user not found
     */
    UserResponse updateProfile(String publicId, String displayName, String bio);

    /**
     * Check if email exists
     * @param email Email to check
     * @return true if email exists
     */
    boolean emailExists(String email);
}

