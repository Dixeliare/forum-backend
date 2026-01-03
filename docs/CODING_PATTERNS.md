# Coding Patterns & Examples

Tài liệu này mô tả các pattern và ví dụ để bạn có thể code theo.

## 📁 Cấu trúc Project

```
src/main/java/com/longdx/forum_backend/
├── config/          # Configuration classes
├── controller/      # REST Controllers
├── dto/
│   ├── request/    # Request DTOs (input)
│   └── response/   # Response DTOs (output)
├── exception/       # Custom exceptions
├── model/          # JPA Entities
├── repository/     # JPA Repositories
├── security/       # Security configuration
├── service/        # Service interfaces
│   └── impl/      # Service implementations
└── utils/          # Utility classes
```

---

## 1. Repository Pattern

### Basic Repository (Simple Entity)

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Method naming convention (Spring Data JPA tự động tạo query)
    Optional<User> findByPublicId(String publicId);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

### Repository với Custom Query

```java
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // Custom JPQL query
    @Query("SELECT p FROM Post p WHERE p.community IS NULL AND p.author.internalId = :authorId")
    Page<Post> findPersonalPostsByAuthor(@Param("authorId") Long authorId, Pageable pageable);
    
    // Pagination với Pageable
    Page<Post> findByAuthor_InternalId(Long authorId, Pageable pageable);
}
```

### Repository với Composite Key

```java
@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {
    // Check existence
    boolean existsByUserIdAndPostId(Long userId, Long postId);
    
    // Count
    long countByPostId(Long postId);
    
    // Custom query
    @Query("SELECT pl.postId FROM PostLike pl WHERE pl.userId = :userId")
    List<Long> findPostIdsByUserId(@Param("userId") Long userId);
}
```

**Pattern:**
- Extends `JpaRepository<Entity, ID>`
- Use `Optional` for find methods
- Use `Pageable` for pagination
- Use `@Query` for complex queries
- Method naming: `findBy...`, `existsBy...`, `countBy...`

---

## 2. DTO Pattern

### Request DTO (Input)

```java
// Use Java Records (immutable, concise)
public record CreateUserRequest(
        @NotBlank(message = "Display name is required")
        @Size(max = 255)
        String displayName,
        
        @NotBlank
        @Email
        String email,
        
        @NotBlank
        @Size(min = 8, max = 100)
        String password
) {
}
```

**Pattern:**
- Use `record` for immutable DTOs
- Add validation annotations (`@NotBlank`, `@Email`, `@Size`, etc.)
- Keep it simple, only fields needed for the operation

### Response DTO (Output)

```java
public record UserResponse(
        String publicId,        // Use public_id, not internal_id
        String displayName,
        String email,
        String bio,
        String avatarUrl,
        Boolean isPrivate,
        OffsetDateTime createdAt
) {
    // Factory method to map from Entity
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getPublicId(),
                user.getDisplayName(),
                user.getEmail(),
                user.getBio(),
                user.getAvatarUrl(),
                user.getIsPrivate(),
                user.getCreatedAt()
        );
    }
}
```

**Pattern:**
- Only expose necessary fields (never expose password, internal_id)
- Use `public_id` instead of `internal_id` for public APIs
- Include static factory method `from(Entity)` for mapping
- Use nested records for related entities (e.g., `UserSummary`)

---

## 3. Service Pattern

### Service Interface

```java
public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    Optional<UserResponse> getUserByPublicId(String publicId);
    Optional<User> getUserByEmail(String email);  // Return entity for internal use
    UserResponse updateProfile(String publicId, String displayName, String bio);
    boolean emailExists(String email);
}
```

**Pattern:**
- Define business logic methods
- Return DTOs for public methods, entities for internal methods
- Use `Optional` for nullable returns
- Throw custom exceptions for error cases

### Service Implementation

```java
@Service
@Transactional
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    // Constructor injection
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public UserResponse createUser(CreateUserRequest request) {
        // 1. Validate business rules
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // 2. Create entity
        User user = new User();
        user.setDisplayName(request.displayName());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        // TODO: Generate public_id and internal_id
        
        // 3. Save
        User savedUser = userRepository.save(user);
        
        // 4. Return DTO
        return UserResponse.from(savedUser);
    }
    
    @Override
    @Transactional(readOnly = true)  // Read-only for query methods
    public Optional<UserResponse> getUserByPublicId(String publicId) {
        return userRepository.findByPublicId(publicId)
                .map(UserResponse::from);
    }
}
```

**Pattern:**
- `@Service` annotation
- `@Transactional` for write operations
- `@Transactional(readOnly = true)` for read operations
- Constructor injection (preferred over field injection)
- Handle business logic
- Map entities to DTOs before returning

---

## 4. Controller Pattern

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
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
    
    @GetMapping("/{publicId}")
    public ResponseEntity<UserResponse> getUserByPublicId(@PathVariable String publicId) {
        Optional<UserResponse> user = userService.getUserByPublicId(publicId);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
```

**Pattern:**
- `@RestController` annotation
- `@RequestMapping` for base path
- Use DTOs for request/response
- `@Valid` for request validation
- Return `ResponseEntity` for status control
- Use proper HTTP status codes:
  - `201 CREATED` for POST
  - `200 OK` for GET/PUT/PATCH
  - `204 NO_CONTENT` for DELETE
  - `404 NOT_FOUND` for not found
  - `400 BAD_REQUEST` for validation errors

---

## 5. Common Patterns Summary

### ✅ DO:
- Use `public_id` (NanoID) in public APIs, `internal_id` (TSID) internally
- Use DTOs for all API inputs/outputs
- Use `Optional` for nullable returns
- Use `Pageable` for pagination
- Use `@Transactional` appropriately
- Use constructor injection
- Use static factory methods for DTO mapping
- Use validation annotations on DTOs

### ❌ DON'T:
- Don't expose entities directly in APIs
- Don't expose `internal_id` or `password_hash` in responses
- Don't use field injection (use constructor injection)
- Don't forget `@Transactional` for write operations
- Don't forget `@Valid` on request DTOs
- Don't return entities from service public methods (use DTOs)

---

## 6. Examples Reference

Xem các file mẫu đã tạo:
- `UserRepository.java` - Basic repository
- `PostRepository.java` - Repository with pagination
- `PostLikeRepository.java` - Repository with composite key
- `CreateUserRequest.java` - Request DTO
- `UserResponse.java` - Response DTO
- `UserService.java` - Service interface
- `UserServiceImpl.java` - Service implementation
- `UserController.java` - REST Controller

---

## 7. Next Steps

1. Tạo các Repository còn lại theo pattern trên
2. Tạo DTOs cho các entities khác
3. Tạo Service interfaces và implementations
4. Tạo Controllers
5. Thêm Exception handling
6. Thêm Security (JWT, authentication)

