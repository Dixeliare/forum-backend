# DATABASE DESIGN DOCUMENTATION

**Project:** Hybrid Social Platform Backend  
**Version:** 1.0  
**Author:** LongDx  
**Last Updated:** December 2025

---

## 1. TỔNG QUAN (OVERVIEW)

Thiết kế database cho hệ thống Hybrid Social Platform, hỗ trợ cả **Forum** (Knowledge Base) và **Social Network** (Community & Personal Posts) với các tính năng:

- Dual-Key User Identity (TSID + NanoID)
- Forum 4 lớp (Forum → Category → Sub-forum → Thread)
- Social Communities & Posts
- Topics System (giống Threads - Meta)
- NSFW Content Control
- Interaction System (Likes, Comments, Saves, Shares, Follows)
- Ranking Algorithm Support (Gravity Score)

---

## 2. CHIẾN LƯỢC ID (ID STRATEGY)

### 2.1. Internal ID (TSID)
- **Kiểu:** `BIGINT` (64-bit)
- **Công nghệ:** TSID (Time-Sorted Unique Identifier)
- **Mục đích:** Dùng cho Join bảng, Index hiệu quả
- **Lợi ích:** Tương thích B-Tree Index, không gây phân mảnh, sắp xếp theo thời gian

### 2.2. Public ID (Short ID)
- **Kiểu:** `VARCHAR(8-12)`
- **Công nghệ:** Custom NanoID (bỏ ký tự dễ nhầm: l, 1, O, 0, -, _)
- **Mục đích:** Dùng cho URL thân thiện (slug.public_id)
- **Ví dụ:** `/c/yeu-meo.Xy9z` → Query bằng `Xy9z`

---

## 3. CẤU TRÚC BẢNG (SCHEMA STRUCTURE)

### 3.1. User Identity & Authentication

#### `users`
Bảng người dùng với Dual-Key Identity.

| Column | Type | Description |
|--------|------|-------------|
| `internal_id` | BIGINT (PK) | TSID - Dùng để Join |
| `public_id` | VARCHAR(20) UNIQUE | NanoID Suffix (chỉ lưu phần sau `#`) |
| `display_name` | VARCHAR(255) | Tên hiển thị (có thể Emoji, CJK) |
| `email` | VARCHAR(255) UNIQUE | Email đăng nhập |
| `password_hash` | VARCHAR(255) | Bcrypt hash |
| `settings_display_sensitive_media` | BOOLEAN | NSFW setting (default: FALSE) |
| `is_private` | BOOLEAN | Private account (cần approval khi follow, default: FALSE) |
| `created_at`, `updated_at`, `last_login_at` | TIMESTAMP | Timestamps |
| `is_active`, `is_verified` | BOOLEAN | Status flags |

**Indexes:**
- `idx_users_public_id` - Tìm kiếm bằng Public ID
- `idx_users_email` - Login
- `idx_users_display_name` - Search

**Note:** Prefix (Latinized) được tính động từ `display_name` bằng ICU4J, không lưu trong DB.

---

### 3.2. Forum System (4 Layers: Forum → Category → Sub-forum → Thread)

#### `forums`
Lớp Forum ngoài cùng - Cho phép nhiều forum độc lập trong cùng 1 app.

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT (PK) | TSID |
| `name` | VARCHAR(255) | Tên forum (VD: "Voz", "SpringBoot Việt Nam") |
| `slug` | VARCHAR(255) | URL slug (**không unique**, có thể trùng - chỉ dùng cho SEO) |
| `public_id` | VARCHAR(10) UNIQUE | Short ID cho URL (slug.public_id) - **Dùng để query DB** |
| `description` | TEXT | Mô tả forum |
| `owner_id` | BIGINT (FK) | Reference to `users` (Admin tạo forum) |
| `is_public` | BOOLEAN | Public hoặc Private forum |
| `member_count` | INTEGER | Số thành viên (denormalized) |
| `thread_count` | INTEGER | Số threads (denormalized) |
| `category_count` | INTEGER | Số categories (denormalized) |
| `created_at`, `updated_at` | TIMESTAMP | Timestamps |

**Indexes:**
- `idx_forums_slug` - Index cho slug (SEO)
- `idx_forums_public_id` - **Tìm bằng Short ID (query thực tế)**

**Note:** 
- Cho phép nhiều forum độc lập (Voz, SpringBoot VN, Java Community, etc.) trong cùng 1 app.
- Slug không unique vì chỉ dùng để SEO. Query thực tế dùng `public_id` (nhất quán với `communities`).

#### `categories`
Danh mục lớn trong Forum (VD: Công nghệ, Đời sống).

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT (PK) | TSID |
| `forum_id` | BIGINT (FK) | Reference to `forums` |
| `name` | VARCHAR(255) | Tên danh mục |
| `slug` | VARCHAR(255) | URL slug (unique trong forum, không phải global) |
| `description` | TEXT | Mô tả |
| `display_order` | INTEGER | Thứ tự hiển thị trong forum |

**Constraint:** `UNIQUE(forum_id, slug)` - Slug unique trong forum, không phải global.

**Indexes:**
- `idx_categories_forum` - Lấy categories của forum
- `idx_categories_forum_order` - Sắp xếp categories trong forum

#### `sub_forums`
Chủ đề cụ thể trong Category (VD: Java Backend, Chuyện trò linh tinh).

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT (PK) | TSID |
| `category_id` | BIGINT (FK) | Reference to `categories` |
| `name` | VARCHAR(255) | Tên sub-forum |
| `slug` | VARCHAR(255) | URL slug (unique trong category) |
| `description` | TEXT | Mô tả |
| `display_order` | INTEGER | Thứ tự hiển thị (nếu dùng manual sorting) |
| `last_activity_at` | TIMESTAMP | Activity gần nhất (comment/thread mới trong sub-forum) |
| `last_thread_id` | BIGINT (FK, NULL) | Thread có activity gần nhất (để jump đến khi click) |
| `last_comment_id` | BIGINT (FK, NULL) | Comment gần nhất (nếu activity là comment, để jump đến) |
| `last_activity_by_user_id` | BIGINT (FK, NULL) | User tạo activity gần nhất (hiển thị tên như Voz) |

**Constraint:** `UNIQUE(category_id, slug)` - Slug unique trong category.

**Indexes:**
- `idx_sub_forums_last_activity` - Sort theo activity (DESC)
- `idx_sub_forums_category_activity` - Sort sub-forums trong category theo activity
- `idx_sub_forums_last_thread` - Join với thread gần nhất
- `idx_sub_forums_last_comment` - Join với comment gần nhất

**Note:** 
- `last_activity_at`, `last_thread_id`, `last_comment_id`, `last_activity_by_user_id` được cập nhật tự động khi có comment mới hoặc thread mới.
- Query: `ORDER BY last_activity_at DESC` để hiển thị sub-forum có activity gần nhất lên đầu.
- Hiển thị như Voz: "Last activity: '[thread title]... 10 minutes ago - [user name]'"
- Khi click vào sub-forum → Jump đến thread/comment gần nhất (dùng `last_thread_id` hoặc `last_comment_id`)

#### `forum_threads`
Bài thảo luận sâu (Bắt buộc có Tiêu đề + Nội dung).

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT (PK) | TSID |
| `sub_forum_id` | BIGINT (FK) | Reference to `sub_forums` |
| `author_id` | BIGINT (FK) | Reference to `users` |
| `title` | VARCHAR(500) | **Bắt buộc** |
| `content` | TEXT | **Bắt buộc** |
| `public_id` | VARCHAR(12) UNIQUE | Short ID cho URL |
| `slug` | VARCHAR(350) | Slug từ title (SEO) |
| `view_count` | INTEGER | Số lượt xem |
| `last_activity_at` | TIMESTAMP | Thời gian hoạt động cuối |

**Indexes:**
- `idx_forum_threads_last_activity` - Sắp xếp theo hoạt động
- `idx_forum_threads_public_id` - Tìm bằng Short ID

---

### 3.3. Social System

#### `communities`
Nhóm cộng đồng (Groups).

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT (PK) | TSID |
| `name` | VARCHAR(255) | Tên nhóm |
| `slug` | VARCHAR(255) | URL slug (**không unique**, có thể trùng) |
| `public_id` | VARCHAR(10) UNIQUE | Short ID cho URL (query bằng cái này) |
| `description` | TEXT | Mô tả |
| `owner_id` | BIGINT (FK) | Reference to `users` |
| `is_nsfw` | BOOLEAN | NSFW flag (default: FALSE) |
| `member_count` | INTEGER | Số thành viên |
| `post_count` | INTEGER | Số bài viết |

**Note:** Slug không unique vì chỉ dùng để SEO. Query thực tế dùng `public_id`.

#### `posts`
Bài viết Social (Personal hoặc Community Post).

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT (PK) | TSID |
| `author_id` | BIGINT (FK) | Reference to `users` |
| `community_id` | BIGINT (FK, NULL) | Reference to `communities` (NULL = Personal Post) |
| `topic_id` | BIGINT (FK, NULL) | Reference to `topics` (CHỈ dùng cho Personal Posts, NULL = post không có topic) |
| `content` | TEXT | Nội dung bài viết |
| `public_id` | VARCHAR(12) UNIQUE | Short ID cho URL |
| `slug` | VARCHAR(350) | Slug (optional, SEO) |
| `is_nsfw` | BOOLEAN | NSFW flag (kế thừa từ community) |

**Stats cho Ranking Algorithm:**
| Column | Type | Description |
|--------|------|-------------|
| `likes_count` | INTEGER | Số lượt like (trọng số: 1) |
| `comments_count` | INTEGER | Số comment (trọng số: 5) |
| `shares_count` | INTEGER | Số share (trọng số: 10) |
| `saves_count` | INTEGER | Số lượt lưu (trọng số: 8) |
| `tags_count` | INTEGER | Số lượt tag bạn bè (trọng số: 6) |
| `caption_expands_count` | INTEGER | Số lượt bấm "Xem thêm" (trọng số: 1) |
| `media_clicks_count` | INTEGER | Số lượt click ảnh/video (trọng số: 2) |
| `dwell_7s_count` | INTEGER | Số lượt ở lại > 7s (trọng số: 4) |
| `viral_score` | DECIMAL(20,10) | Điểm tính từ Gravity Algorithm |

**Logic:**
- `community_id IS NULL` → **Personal Post**
- `community_id IS NOT NULL` → **Community Post**
- `topic_id` → CHỈ dùng cho Personal Posts (có thể NULL nếu post không có topic)
- **Mỗi post chỉ có 1 topic** (one-to-many: topic → posts)
- **Post không có topic** → `topic_id IS NULL` (không cần bảng riêng)

**Constraint:**
- `chk_personal_post_topic`: Đảm bảo `topic_id` chỉ dùng cho Personal Posts (`community_id IS NULL`)

**Indexes:**
- `idx_posts_viral_score` - Sắp xếp theo điểm viral
- `idx_posts_community_created` - Lấy bài trong community

---

### 3.4. Interaction System

#### `post_likes`
Likes cho Posts (Personal + Community). **Composite Primary Key** pattern cho junction table.

| Column | Type | Description |
|--------|------|-------------|
| `user_id` | BIGINT (FK, PK) | Reference to `users` |
| `post_id` | BIGINT (FK, PK) | Reference to `posts` |
| `created_at` | TIMESTAMP | Thời gian like |

**Primary Key:** `(user_id, post_id)` - Composite PK đảm bảo mỗi user chỉ like 1 post 1 lần.

**Indexes:**
- `idx_post_likes_post` - Query posts được like bởi ai
- `idx_post_likes_created` - Sort theo thời gian

**Note:** Không cần `id` riêng vì đây là junction table, không có bảng nào reference đến `post_likes.id`. Composite PK pattern tiết kiệm storage và đơn giản hơn.

#### `comment_likes`
Likes cho Comments. **Composite Primary Key** pattern cho junction table.

| Column | Type | Description |
|--------|------|-------------|
| `user_id` | BIGINT (FK, PK) | Reference to `users` |
| `comment_id` | BIGINT (FK, PK) | Reference to `comments` |
| `created_at` | TIMESTAMP | Thời gian like |

**Primary Key:** `(user_id, comment_id)` - Composite PK đảm bảo mỗi user chỉ like 1 comment 1 lần.

**Indexes:**
- `idx_comment_likes_comment` - Query comments được like bởi ai
- `idx_comment_likes_created` - Sort theo thời gian

#### `thread_likes`
Likes cho Forum Threads. **Composite Primary Key** pattern cho junction table.

| Column | Type | Description |
|--------|------|-------------|
| `user_id` | BIGINT (FK, PK) | Reference to `users` |
| `thread_id` | BIGINT (FK, PK) | Reference to `forum_threads` |
| `created_at` | TIMESTAMP | Thời gian like |

**Primary Key:** `(user_id, thread_id)` - Composite PK đảm bảo mỗi user chỉ like 1 thread 1 lần.

**Indexes:**
- `idx_thread_likes_thread` - Query threads được like bởi ai
- `idx_thread_likes_created` - Sort theo thời gian

**Design Decision:** Tách riêng 3 bảng likes thay vì unified table để:
- Có Foreign Key constraint đầy đủ (referential integrity)
- Dễ optimize index cho từng loại
- Dễ partition riêng (ví dụ: partition `post_likes` theo tháng khi scale)
- Type-safe hơn

#### `comments`
Bình luận unified (dùng chung cho Posts và Threads).

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT (PK) | TSID |
| `post_id` | BIGINT (FK, NULL) | Reference to `posts` |
| `thread_id` | BIGINT (FK, NULL) | Reference to `forum_threads` |
| `author_id` | BIGINT (FK) | Reference to `users` |
| `parent_comment_id` | BIGINT (FK, NULL) | Reference to `comments` (nested/reply) |
| `content` | TEXT | Nội dung comment |
| `likes_count` | INTEGER | Số lượt like comment (denormalized từ `comment_likes`) |

**Constraint:** `CHECK (post_id IS NOT NULL XOR thread_id IS NOT NULL)` - Chỉ comment 1 trong 2.

**Design Decision:** Dùng chung 1 bảng comments vì:
- Comments có cấu trúc giống nhau (content, author, timestamps, nested)
- Theo thiết kế của Facebook, Twitter, Reddit - họ đều dùng unified comments
- Đơn giản hơn, dễ maintain
- Query tổng hợp dễ dàng (tất cả comments của user)

#### `saved_posts`
Bookmark bài viết (trọng số cao: 8 điểm).

| Column | Type | Description |
|--------|------|-------------|
| `user_id` | BIGINT (FK) | Reference to `users` |
| `post_id` | BIGINT (FK) | Reference to `posts` |
| `saved_at` | TIMESTAMP | Thời gian lưu |

**Primary Key:** `(user_id, post_id)` - Mỗi user chỉ lưu 1 bài 1 lần.

#### `shares`
Chia sẻ bài viết (trọng số cao nhất: 10 điểm).

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT (PK) | TSID |
| `user_id` | BIGINT (FK) | Reference to `users` |
| `post_id` | BIGINT (FK) | Reference to `posts` |
| `shared_at` | TIMESTAMP | Thời gian share |

#### `user_follows`
Quan hệ follow giữa users (hỗ trợ cả Public và Private accounts).

| Column | Type | Description |
|--------|------|-------------|
| `follower_id` | BIGINT (FK) | Reference to `users` (người follow) |
| `target_id` | BIGINT (FK) | Reference to `users` (người được follow) |
| `status` | ENUM | **PENDING**, ACCEPTED, REJECTED |
| `requested_at` | TIMESTAMP | Thời gian request follow |
| `accepted_at` | TIMESTAMP | Thời gian accept (nếu status = ACCEPTED) |
| `rejected_at` | TIMESTAMP | Thời gian reject (nếu status = REJECTED) |

**Primary Key:** `(follower_id, target_id)` - Composite PK.

**Indexes:**
- `idx_follows_follower` - Lấy danh sách đang follow (Build Feed)
- `idx_follows_target` - Lấy danh sách người theo dõi (Count/Notify)
- `idx_follows_status` - Filter theo status
- `idx_follows_target_pending` - Lấy follow requests PENDING của user

**Workflow:**
1. **Public Account:** Follow ngay → `status = ACCEPTED`, `accepted_at = now()`
2. **Private Account:** Follow request → `status = PENDING`
3. **Accept:** `status = ACCEPTED`, `accepted_at = now()`
4. **Reject:** `status = REJECTED`, `rejected_at = now()` (hoặc xóa record)

**Note:** 
- Dùng Internal TSID để join nhanh
- Giống Threads/Instagram: Private accounts cần approval
- Query feed chỉ lấy `status = ACCEPTED`

#### `join_requests`
Yêu cầu tham gia Forum hoặc Community.

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT (PK) | TSID |
| `user_id` | BIGINT (FK) | Reference to `users` (người xin join) |
| `forum_id` | BIGINT (FK, NULL) | Reference to `forums` |
| `community_id` | BIGINT (FK, NULL) | Reference to `communities` |
| `status` | ENUM | PENDING, APPROVED, REJECTED |
| `message` | TEXT | Lời nhắn khi xin join (optional) |
| `reviewed_by` | BIGINT (FK, NULL) | Reference to `users` (người duyệt) |
| `reviewed_at` | TIMESTAMP | Thời gian duyệt |
| `created_at`, `updated_at` | TIMESTAMP | Timestamps |

**Constraint:** `CHECK (forum_id IS NOT NULL XOR community_id IS NOT NULL)` - Chỉ xin vào Forum HOẶC Community.

**Unique:** 
- `(user_id, forum_id)` - Mỗi user chỉ xin vào 1 forum 1 lần
- `(user_id, community_id)` - Mỗi user chỉ xin vào 1 community 1 lần

**Indexes:**
- `idx_join_requests_forum_status` - Lấy requests PENDING của forum
- `idx_join_requests_community_status` - Lấy requests PENDING của community
- `idx_join_requests_user` - Lấy requests của user

**Workflow:**
1. User tạo request với `status = PENDING`
2. Admin/Moderator duyệt → `status = APPROVED` hoặc `REJECTED`
3. Khi APPROVED → Tự động thêm vào `forum_members` hoặc `community_members`

---

### 3.5. Topics System (Giống Threads - Meta)

#### `topics`
Topics được gán cho Posts (giống Threads của Meta).

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT (PK) | TSID |
| `name` | VARCHAR(255) UNIQUE | Tên topic (e.g., "Technology", "Hà Nội") |
| `slug` | VARCHAR(255) UNIQUE | URL slug (e.g., "technology", "hanoi") |
| `description` | TEXT | Mô tả topic |
| `image_url` | TEXT | Ảnh đại diện topic (optional) |
| `post_count` | INTEGER | Số posts có topic này (denormalized) |
| `follower_count` | INTEGER | Số users follow topic này (denormalized) |
| `is_featured` | BOOLEAN | Topic nổi bật (admin feature) |
| `created_at`, `updated_at` | TIMESTAMP | Timestamps |

**Indexes:**
- `idx_topics_slug` - Tìm bằng slug
- `idx_topics_follower_count` - Sort topics theo popularity

**Note:** 
- Topics giống Threads - users có thể follow topics để xem posts về topic đó trong feed
- **Mỗi post chỉ có 1 topic** (one-to-many: topic → posts)
- **Post không có topic** → `topic_id IS NULL` trong bảng `posts` (không cần bảng riêng)
- **CHỈ dùng cho Personal Posts** (`community_id IS NULL`). Community Posts **KHÔNG** dùng topics

**Relationship:**
- `posts.topic_id` → FK to `topics.id` (nullable)
- Constraint `chk_personal_post_topic` đảm bảo chỉ Personal Posts mới có thể có topic

#### `user_topic_follows`
Users follow topics để xem posts về topic đó trong feed.

| Column | Type | Description |
|--------|------|-------------|
| `user_id` | BIGINT (FK) | Reference to `users` |
| `topic_id` | BIGINT (FK) | Reference to `topics` |
| `created_at` | TIMESTAMP | Thời gian follow |

**Primary Key:** `(user_id, topic_id)`.

**Use Case:** 
- User follow topic "Technology" → Feed sẽ hiển thị posts có topic "Technology"
- Giống Threads: Follow topics để personalize feed

---

### 3.6. Community Membership

#### `community_members`
Thành viên của communities.

| Column | Type | Description |
|--------|------|-------------|
| `community_id` | BIGINT (FK) | Reference to `communities` |
| `user_id` | BIGINT (FK) | Reference to `users` |
| `role` | VARCHAR(50) | MEMBER, MODERATOR, ADMIN |
| `joined_at` | TIMESTAMP | Thời gian tham gia |

**Primary Key:** `(community_id, user_id)`.

---

### 3.7. Media/Attachments

#### `media`
Ảnh/Video đính kèm (cho Posts hoặc Threads).

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT (PK) | TSID |
| `post_id` | BIGINT (FK, NULL) | Reference to `posts` |
| `thread_id` | BIGINT (FK, NULL) | Reference to `forum_threads` |
| `user_id` | BIGINT (FK) | Reference to `users` |
| `media_type` | VARCHAR(50) | IMAGE, VIDEO, GIF |
| `media_url` | TEXT | URL ảnh/video |
| `thumbnail_url` | TEXT | URL thumbnail |
| `file_size` | BIGINT | Kích thước file |
| `width`, `height` | INTEGER | Kích thước |
| `display_order` | INTEGER | Thứ tự hiển thị |

**Constraint:** `CHECK (post_id IS NOT NULL XOR thread_id IS NOT NULL)` - Chỉ attach 1 trong 2.

---

### 3.8. Notifications

#### `notifications`
Thông báo cho users.

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT (PK) | TSID |
| `user_id` | BIGINT (FK) | Reference to `users` |
| `type` | ENUM | LIKE, COMMENT, REPLY, FOLLOW, MENTION, COMMUNITY_INVITE, SYSTEM |
| `actor_id` | BIGINT (FK, NULL) | Reference to `users` (người thực hiện) |
| `post_id`, `thread_id`, `comment_id`, `community_id` | BIGINT (FK, NULL) | Reference tùy context |
| `content` | TEXT | Nội dung thông báo |
| `is_read` | BOOLEAN | Đã đọc chưa |
| `created_at` | TIMESTAMP | Thời gian tạo |

**Indexes:**
- `idx_notifications_user_unread` - Lấy thông báo chưa đọc

---

## 4. QUAN HỆ GIỮA CÁC BẢNG (RELATIONSHIPS)

### 4.1. User-Centric
```
users (1) ──< (N) posts
users (1) ──< (N) forum_threads
users (1) ──< (N) comments
users (1) ──< (N) forums (owner)          -- Admin tạo forum
users (1) ──< (N) communities (owner)
users (N) ──< (N) user_follows (follower/target)
users (N) ──< (N) community_members
users (N) ──< (N) join_requests           -- User xin vào forum/community
users (N) ──< (N) user_topic_follows ──< (N) topics  -- User follow topics
```

### 4.2. Forum Hierarchy
```
forums (1) ──< (N) categories (1) ──< (N) sub_forums (1) ──< (N) forum_threads
```

**Ví dụ:**
- Forum "Voz" (id: 1)
  - Category "Công nghệ" (forum_id: 1)
    - Sub-forum "Java Backend" (category_id: 1)
      - Thread "Hướng dẫn Spring Boot" (sub_forum_id: 1)
  - Category "Đời sống" (forum_id: 1)
    - Sub-forum "Chuyện trò linh tinh" (category_id: 2)
      - Thread "Hôm nay trời đẹp" (sub_forum_id: 2)

### 4.3. Social Network
```
communities (1) ──< (N) posts
topics (1) ──< (N) posts (topic_id)  -- One-to-Many: Mỗi post chỉ có 1 topic
users (N) ──< (N) user_topic_follows ──< (N) topics
```

**Topics Flow:**
- **Mỗi post chỉ có 1 topic** (one-to-many: topic → posts)
- **Post không có topic** → `topic_id IS NULL` (không cần bảng riêng)
- **CHỈ Personal Posts** (`community_id IS NULL`) mới có thể có topic
- Users follow topics → Feed hiển thị posts từ topics đã follow (giống Threads)

### 4.4. Interactions
```
posts (1) ──< (N) post_likes
posts (1) ──< (N) comments
posts (1) ──< (N) saved_posts
posts (1) ──< (N) shares
comments (1) ──< (N) comment_likes
comments (1) ──< (N) comments (nested/reply)
forum_threads (1) ──< (N) thread_likes
forum_threads (1) ──< (N) comments
```

---

## 5. INDEXING STRATEGY

### 5.1. Primary Indexes
- Hầu hết bảng dùng `BIGINT` PK (TSID) → B-Tree Index tự động
- Junction tables (likes, saves, shares) dùng **Composite Primary Key** → Index tự động trên cả 2 columns

### 5.2. Foreign Key Indexes
- Tất cả FK đều có index để tối ưu JOIN

### 5.3. Search Indexes
- `public_id` (Short ID) → Unique Index cho tất cả entities
- `slug` → Index cho SEO (không unique cho communities)
- `display_name`, `email` → Index cho search users

### 5.4. Ranking Indexes
- `viral_score DESC` → Sắp xếp bài viết hot
- `last_activity_at DESC` → Sắp xếp threads hot
- `created_at DESC` → Sắp xếp mới nhất

### 5.5. Composite Indexes
- `(community_id, created_at DESC)` → Lấy bài trong community
- `(user_id, is_read, created_at DESC)` → Lấy thông báo chưa đọc

---

## 6. CONSTRAINTS & VALIDATIONS

### 6.1. Unique Constraints
- `users.public_id` → Unique (NanoID)
- `users.email` → Unique
- `forums.public_id` → Unique (Short ID - dùng để query)
- `communities.public_id` → Unique (Short ID)
- `posts.public_id` → Unique (Short ID)
- `forum_threads.public_id` → Unique (Short ID)
- `topics.name` → Unique
- `topics.slug` → Unique
- `(user_id, post_id)` trong `post_likes` → Composite Primary Key
- `(user_id, comment_id)` trong `comment_likes` → Composite Primary Key
- `(user_id, thread_id)` trong `thread_likes` → Composite Primary Key
- `(user_id, post_id)` trong `saved_posts` → Unique
- `(follower_id, target_id)` trong `user_follows` → Unique
- `(user_id, topic_id)` trong `user_topic_follows` → Unique
- `posts.topic_id` → FK to `topics.id` (nullable, chỉ cho Personal Posts)

### 6.2. Check Constraints
- `comments`: Chỉ comment Post HOẶC Thread (XOR)
- `media`: Chỉ attach Post HOẶC Thread (XOR)
- `join_requests`: Chỉ xin vào Forum HOẶC Community (XOR)
- `user_follows`: Không được follow chính mình (`follower_id != target_id`)

### 6.3. Foreign Key Constraints
- Tất cả FK đều có `ON DELETE CASCADE` hoặc `ON DELETE SET NULL` tùy logic nghiệp vụ

---

## 7. TRIGGERS & FUNCTIONS

### 7.1. Auto-update Timestamps
Function `update_updated_at_column()` tự động cập nhật `updated_at` khi có UPDATE.

**Applied to:**
- `users`
- `posts`
- `communities`
- `comments`
- `forum_threads`
- `forums`
- `join_requests`
- `topics`

---

## 8. INITIAL DATA

### 8.1. Featured Topics
File migration tự động insert 6 Featured Topics mặc định (giống Threads):
- Technology
- News
- Entertainment
- Sports
- Lifestyle
- Education

---

## 9. MỞ RỘNG TƯƠNG LAI (FUTURE ENHANCEMENTS)

### 9.1. Partitioning
Khi đạt 1M+ users:
- `posts` → Range Partitioning theo `created_at` (theo tháng)
- `comments` → Range Partitioning theo `created_at` (theo tháng)

### 9.2. Read Replicas
- 1 Master (Write) + 2 Slaves (Read) cho PostgreSQL

### 9.3. Full-text Search
- Tích hợp Elasticsearch/Meilisearch cho full-text search phức tạp

---

## 10. LƯU Ý QUAN TRỌNG

1. **Không dùng UUID:** Dùng TSID để tối ưu B-Tree Index
2. **Public ID vs Internal ID:** Luôn dùng Internal ID để JOIN, chỉ dùng Public ID cho URL/API response
3. **Slug không unique:** Chỉ dùng cho SEO, query thực tế dùng `public_id`
4. **Stats denormalization:** Lưu stats trong `posts` để tránh COUNT() mỗi lần query
5. **Composite PK:** Dùng cho bảng Many-to-Many và bảng quan hệ (saved_posts, user_follows)

---

*End of Database Design Documentation.*

 