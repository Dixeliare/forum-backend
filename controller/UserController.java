package com.longdx.forum_backend.controller;

import com.longdx.forum_backend.dto.request.CreateUserRequest;
import com.longdx.forum_backend.dto.response.UserResponse;
import com.longdx.forum_backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST Controller for User operations
 * 
 * Pattern:
 * - @RestController annotation
 * - @RequestMapping for base path
 * - Use DTOs for request/response
 * - @Valid for request validation
 * - Return ResponseEntity for status control
 * - Use proper HTTP status codes
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Create a new user
     * POST /api/v1/users
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        try {
            UserResponse user = userService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (IllegalArgumentException e) {
            // TODO: Use proper exception handler
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Get user by public ID
     * GET /api/v1/users/{publicId}
     */
    @GetMapping("/{publicId}")
    public ResponseEntity<UserResponse> getUserByPublicId(@PathVariable String publicId) {
        Optional<UserResponse> user = userService.getUserByPublicId(publicId);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update user profile
     * PATCH /api/v1/users/{publicId}
     */
    @PatchMapping("/{publicId}")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable String publicId,
            @RequestParam(required = false) String displayName,
            @RequestParam(required = false) String bio) {
        try {
            UserResponse user = userService.updateProfile(publicId, displayName, bio);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Check if email exists
     * GET /api/v1/users/check-email?email={email}
     */
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        boolean exists = userService.emailExists(email);
        return ResponseEntity.ok(exists);
    }
}

