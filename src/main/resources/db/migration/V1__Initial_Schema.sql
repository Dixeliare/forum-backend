-- =====================================================
-- INITIAL DATABASE SCHEMA
-- Hybrid Social Platform Backend
-- Version: 1.0
-- Author: LongDx
-- Based on: SocialMediaV1.sql (from DrawDB)
-- =====================================================
-- Note: Tất cả business logic được xử lý ở Backend
-- Database chỉ lưu schema thuần, không có triggers, functions, hoặc CHECK constraints
-- =====================================================

-- =====================================================
-- 1. USER IDENTITY & AUTHENTICATION
-- =====================================================

CREATE TABLE users (
    internal_id BIGINT PRIMARY KEY,                    -- TSID (Time-Sorted ID)
    public_id VARCHAR(20) NOT NULL UNIQUE,             -- NanoID Suffix (e.g., "Xy9zQ2mP")
    display_name VARCHAR(255) NOT NULL,                -- Tên hiển thị (có thể chứa Emoji, CJK)
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,               -- Bcrypt hash
    bio TEXT,                                          -- Bio của user
    avatar_url VARCHAR(255),                           -- Avatar URL
    banner_id VARCHAR(255),                            -- Banner ID
    settings_display_sensitive_media BOOLEAN DEFAULT FALSE, -- NSFW setting (display_nsfw)
    is_private BOOLEAN DEFAULT FALSE,                  -- Private account (cần approval khi follow)
    account_status VARCHAR(255) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, SUSPENDED, DELETED
    is_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    deleted_at TIMESTAMP WITH TIME ZONE,               -- Soft delete
    deletion_reason VARCHAR(255),                      -- Lý do xóa
    timezone VARCHAR(255) DEFAULT 'UTC',              -- Timezone
    last_public_id_changed_at TIMESTAMP WITH TIME ZONE, -- Khi nào public_id thay đổi
    is_searchable_by_public_id BOOLEAN DEFAULT TRUE,   -- Có thể search bằng public_id
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    last_login_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_users_public_id ON users(public_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_display_name ON users(display_name);
CREATE INDEX idx_users_account_status ON users(account_status);
CREATE INDEX idx_users_is_active ON users(is_active) WHERE is_active = TRUE;

COMMENT ON TABLE users IS 'Bảng người dùng với Dual-Key Identity (TSID internal + NanoID public)';


-- =====================================================
-- 2. FORUM SYSTEM (4 Layers: Forum -> Category -> Sub-forum -> Thread)
-- =====================================================

CREATE TABLE forums (
    id BIGINT PRIMARY KEY,                              -- TSID
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,                         -- URL slug (không unique, có thể trùng - chỉ dùng cho SEO)
    public_id VARCHAR(10) NOT NULL UNIQUE,              -- Short ID cho URL (slug.public_id) - Dùng để query DB
    description TEXT,
    owner_id BIGINT NOT NULL REFERENCES users(internal_id) ON DELETE RESTRICT, -- Admin tạo forum
    is_public BOOLEAN DEFAULT TRUE,                     -- Public hoặc Private forum (is_discoverable)
    is_private BOOLEAN DEFAULT FALSE,                   -- Private forum
    is_nsfw BOOLEAN DEFAULT FALSE,                      -- NSFW flag
    member_count INTEGER DEFAULT 0,                      -- Số thành viên
    thread_count INTEGER DEFAULT 0,                       -- Số threads (denormalized)
    category_count INTEGER DEFAULT 0,                    -- Số categories (denormalized) (categories_count)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_forums_slug ON forums(slug);
CREATE INDEX idx_forums_public_id ON forums(public_id);
CREATE INDEX idx_forums_owner ON forums(owner_id);
CREATE INDEX idx_forums_is_public ON forums(is_public);

COMMENT ON TABLE forums IS 'Lớp Forum ngoài cùng - Cho phép nhiều forum độc lập (Voz, SpringBoot VN, etc.) trong cùng 1 app';

CREATE TABLE categories (
    id BIGINT PRIMARY KEY,                             -- TSID (internal_id)
    forum_id BIGINT NOT NULL REFERENCES forums(id) ON DELETE CASCADE, -- Forum chứa category này
    name VARCHAR(255) NOT NULL,                         -- Display name (display_name)
    slug VARCHAR(255) NOT NULL,                         -- Slug (unique trong forum, không phải global)
    description TEXT,
    display_order INTEGER DEFAULT 0,                    -- Thứ tự hiển thị trong forum
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    UNIQUE(forum_id, slug)                              -- Slug unique trong forum
);

CREATE INDEX idx_categories_forum ON categories(forum_id);
CREATE INDEX idx_categories_slug ON categories(slug);
CREATE INDEX idx_categories_forum_order ON categories(forum_id, display_order);

COMMENT ON TABLE categories IS 'Danh mục lớn trong Forum. Slug unique trong forum, không phải global';

CREATE TABLE sub_forums (
    id BIGINT PRIMARY KEY,                              -- TSID (không phải INTEGER)
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,                         -- Display name (display_name)
    slug VARCHAR(255) NOT NULL,
    description TEXT,
    display_order INTEGER DEFAULT 0,                    -- Thứ tự hiển thị (nếu dùng manual sorting)
    last_activity_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP, -- Activity gần nhất (comment/thread mới)
    last_thread_id BIGINT,                              -- Thread có activity gần nhất (FK sẽ thêm sau)
    last_comment_id BIGINT,                             -- Comment gần nhất (FK sẽ thêm sau)
    last_activity_by_user_id BIGINT REFERENCES users(internal_id) ON DELETE SET NULL, -- User tạo activity gần nhất (hiển thị tên)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    UNIQUE(category_id, slug)                           -- Slug unique trong category
);

CREATE INDEX idx_sub_forums_category ON sub_forums(category_id);
CREATE INDEX idx_sub_forums_slug ON sub_forums(slug);
CREATE INDEX idx_sub_forums_last_activity ON sub_forums(last_activity_at DESC); -- Sort theo activity
CREATE INDEX idx_sub_forums_category_activity ON sub_forums(category_id, last_activity_at DESC); -- Sort sub-forums trong category theo activity
CREATE INDEX idx_sub_forums_last_thread ON sub_forums(last_thread_id) WHERE last_thread_id IS NOT NULL;
CREATE INDEX idx_sub_forums_last_comment ON sub_forums(last_comment_id) WHERE last_comment_id IS NOT NULL;

COMMENT ON TABLE sub_forums IS 'Sub-forum với thông tin activity gần nhất. last_thread_id/last_comment_id để jump đến activity khi click vào sub-forum';

CREATE TABLE forum_threads (
    id BIGINT PRIMARY KEY,                              -- TSID (internal_id)
    sub_forum_id BIGINT NOT NULL REFERENCES sub_forums(id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES users(internal_id) ON DELETE CASCADE,
    title VARCHAR(500) NOT NULL,                        -- Bắt buộc có tiêu đề
    content TEXT NOT NULL,                              -- Bắt buộc có nội dung
    public_id VARCHAR(12) NOT NULL UNIQUE,              -- Short ID cho URL (slug.public_id)
    slug VARCHAR(350),                                 -- Slug từ title (để SEO) - không phải BIGINT
    view_count INTEGER DEFAULT 0,
    last_activity_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_forum_threads_sub_forum ON forum_threads(sub_forum_id);
CREATE INDEX idx_forum_threads_author ON forum_threads(author_id);
CREATE INDEX idx_forum_threads_public_id ON forum_threads(public_id);
CREATE INDEX idx_forum_threads_last_activity ON forum_threads(last_activity_at DESC);
CREATE INDEX idx_forum_threads_created ON forum_threads(created_at DESC);

COMMENT ON TABLE forum_threads IS 'Threads trong Forum. Bắt buộc có title và content';


-- =====================================================
-- 3. SOCIAL SYSTEM (Communities & Posts)
-- =====================================================

CREATE TABLE communities (
    id BIGINT PRIMARY KEY,                              -- TSID (internal_id)
    name VARCHAR(255) NOT NULL,                         -- Display name (display_name)
    slug VARCHAR(255) NOT NULL,                         -- Không unique (có thể trùng)
    public_id VARCHAR(10) NOT NULL UNIQUE,              -- Short ID cho URL (slug.public_id) - không phải BIGINT
    description TEXT,
    owner_id BIGINT NOT NULL REFERENCES users(internal_id) ON DELETE RESTRICT,
    avatar_url VARCHAR(255),                             -- Avatar URL
    cover_url VARCHAR(255),                             -- Cover URL
    is_nsfw BOOLEAN DEFAULT FALSE,                      -- NSFW flag
    is_private BOOLEAN DEFAULT FALSE,                   -- Private community
    is_searchable BOOLEAN DEFAULT TRUE,                 -- Có thể search
    member_count INTEGER DEFAULT 0,
    post_count INTEGER DEFAULT 0,
    updated_user_id BIGINT REFERENCES users(internal_id) ON DELETE SET NULL, -- User cập nhật gần nhất
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_communities_public_id ON communities(public_id);
CREATE INDEX idx_communities_owner ON communities(owner_id);
CREATE INDEX idx_communities_slug ON communities(slug);
CREATE INDEX idx_communities_is_searchable ON communities(is_searchable) WHERE is_searchable = TRUE;

COMMENT ON TABLE communities IS 'Nhóm cộng đồng. community_id NULL trong posts = Personal Post';

-- =====================================================
-- TOPICS SYSTEM (Phải tạo trước posts vì posts có FK đến topics)
-- =====================================================

CREATE TABLE topics (
    id BIGINT PRIMARY KEY,                              -- TSID (internal_id)
    name VARCHAR(255) NOT NULL UNIQUE,                  -- Tên topic (e.g., "Technology", "Hà Nội")
    slug VARCHAR(255) NOT NULL UNIQUE,                 -- URL slug (e.g., "technology", "hanoi")
    description TEXT,                                   -- Mô tả topic
    image_url TEXT,                                      -- Ảnh đại diện topic (optional)
    post_count INTEGER DEFAULT 0,                       -- Số posts có topic này (denormalized)
    follower_count INTEGER DEFAULT 0,                   -- Số users follow topic này (denormalized)
    is_featured BOOLEAN DEFAULT FALSE,                 -- Topic nổi bật (admin feature)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_topics_slug ON topics(slug);
CREATE INDEX idx_topics_name ON topics(name);
CREATE INDEX idx_topics_featured ON topics(is_featured) WHERE is_featured = TRUE;
CREATE INDEX idx_topics_follower_count ON topics(follower_count DESC); -- Sort topics theo popularity

COMMENT ON TABLE topics IS 'Topics system giống Threads (Meta). Users có thể follow topics để xem posts về topic đó. Mỗi post chỉ có 1 topic (one-to-many: topic → posts). Post có thể không có topic (topic_id IS NULL trong posts)';

CREATE TABLE posts (
    id BIGINT PRIMARY KEY,                              -- TSID
    author_id BIGINT NOT NULL REFERENCES users(internal_id) ON DELETE CASCADE,
    community_id BIGINT REFERENCES communities(id) ON DELETE SET NULL, -- NULL = Personal Post
    topic_id BIGINT REFERENCES topics(id) ON DELETE SET NULL,          -- CHỈ DÙNG CHO PERSONAL POSTS. NULL = post không có topic
    title VARCHAR(255),                                 -- Title (optional, có thể NULL cho social posts)
    content TEXT NOT NULL,
    public_id VARCHAR(12) NOT NULL UNIQUE,             -- Short ID cho URL - không phải BIGINT
    slug VARCHAR(350),                                 -- Slug (optional, cho SEO)
    is_nsfw BOOLEAN DEFAULT FALSE,                      -- NSFW flag (kế thừa từ community nếu có)
    
    -- Stats cho Ranking Algorithm (Gravity Score)
    likes_count INTEGER DEFAULT 0,
    comments_count INTEGER DEFAULT 0,
    shares_count INTEGER DEFAULT 0,
    saves_count INTEGER DEFAULT 0,
    tags_count INTEGER DEFAULT 0,                       -- Số lượt tag bạn bè trong comment
    caption_expands_count INTEGER DEFAULT 0,            -- Số lượt bấm "Xem thêm"
    media_clicks_count INTEGER DEFAULT 0,              -- Số lượt click vào ảnh/video (media_clicks)
    dwell_7s_count INTEGER DEFAULT 0,                  -- Số lượt ở lại > 7 giây
    viral_score DECIMAL(20, 10) DEFAULT 0,            -- Điểm tính từ Gravity Algorithm
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE
    -- Note: Business logic (topic chỉ cho Personal Posts) được xử lý ở Backend
);

CREATE INDEX idx_posts_author ON posts(author_id);
CREATE INDEX idx_posts_community ON posts(community_id);
CREATE INDEX idx_posts_topic ON posts(topic_id) WHERE topic_id IS NOT NULL; -- Index cho posts có topic
CREATE INDEX idx_posts_public_id ON posts(public_id);
CREATE INDEX idx_posts_created ON posts(created_at DESC);
CREATE INDEX idx_posts_viral_score ON posts(viral_score DESC);
CREATE INDEX idx_posts_community_created ON posts(community_id, created_at DESC) WHERE community_id IS NOT NULL;
CREATE INDEX idx_posts_topic_created ON posts(topic_id, created_at DESC) WHERE topic_id IS NOT NULL; -- Lấy posts của topic (cho feed)

COMMENT ON TABLE posts IS 'Bài viết Social. community_id NULL = Personal Post, NOT NULL = Community Post. topic_id chỉ dùng cho Personal Posts (có thể NULL nếu post không có topic)';


-- =====================================================
-- 4. INTERACTION SYSTEM (Likes, Comments, Saves, Shares, Follows)
-- =====================================================

-- Unified Comments Table (Phải tạo trước comment_likes vì comment_likes có FK đến comments)
CREATE TABLE comments (
    id BIGINT PRIMARY KEY,                              -- TSID
    post_id BIGINT REFERENCES posts(id) ON DELETE CASCADE,      -- NULL nếu là comment của thread
    thread_id BIGINT REFERENCES forum_threads(id) ON DELETE CASCADE, -- NULL nếu là comment của post
    author_id BIGINT NOT NULL REFERENCES users(internal_id) ON DELETE CASCADE,
    parent_comment_id BIGINT REFERENCES comments(id) ON DELETE CASCADE, -- Nested comments (reply)
    content TEXT NOT NULL,
    likes_count INTEGER DEFAULT 0,                      -- Denormalized count (từ comment_likes)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE
    -- Note: Business logic (comment phải có post hoặc thread) được xử lý ở Backend
);

CREATE INDEX idx_comments_post ON comments(post_id) WHERE post_id IS NOT NULL;
CREATE INDEX idx_comments_thread ON comments(thread_id) WHERE thread_id IS NOT NULL;
CREATE INDEX idx_comments_author ON comments(author_id);
CREATE INDEX idx_comments_parent ON comments(parent_comment_id) WHERE parent_comment_id IS NOT NULL;
CREATE INDEX idx_comments_created ON comments(created_at DESC);

COMMENT ON TABLE comments IS 'Unified Comments cho Posts (Personal + Community) và Forum Threads. Dùng chung vì cấu trúc giống nhau';

-- Separate Likes Tables (Best Practice: Tách riêng để có FK constraint và optimize tốt hơn)
CREATE TABLE post_likes (
    user_id BIGINT NOT NULL REFERENCES users(internal_id) ON DELETE CASCADE,
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, post_id)  -- Composite PK: Mỗi user chỉ like 1 post 1 lần
);

CREATE INDEX idx_post_likes_post ON post_likes(post_id);  -- Query posts được like bởi ai
CREATE INDEX idx_post_likes_created ON post_likes(created_at DESC);  -- Sort theo thời gian

COMMENT ON TABLE post_likes IS 'Likes cho Posts (Personal + Community). Composite PK (user_id, post_id) - Junction table pattern';

CREATE TABLE comment_likes (
    user_id BIGINT NOT NULL REFERENCES users(internal_id) ON DELETE CASCADE,
    comment_id BIGINT NOT NULL REFERENCES comments(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, comment_id)  -- Composite PK: Mỗi user chỉ like 1 comment 1 lần
);

CREATE INDEX idx_comment_likes_comment ON comment_likes(comment_id);  -- Query comments được like bởi ai
CREATE INDEX idx_comment_likes_created ON comment_likes(created_at DESC);  -- Sort theo thời gian

COMMENT ON TABLE comment_likes IS 'Likes cho Comments. Composite PK (user_id, comment_id) - Junction table pattern';

CREATE TABLE thread_likes (
    user_id BIGINT NOT NULL REFERENCES users(internal_id) ON DELETE CASCADE,
    thread_id BIGINT NOT NULL REFERENCES forum_threads(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, thread_id)  -- Composite PK: Mỗi user chỉ like 1 thread 1 lần
);

CREATE INDEX idx_thread_likes_thread ON thread_likes(thread_id);  -- Query threads được like bởi ai
CREATE INDEX idx_thread_likes_created ON thread_likes(created_at DESC);  -- Sort theo thời gian

COMMENT ON TABLE thread_likes IS 'Likes cho Forum Threads. Composite PK (user_id, thread_id) - Junction table pattern';

-- Add foreign key constraints to sub_forums after forum_threads and comments are created
ALTER TABLE sub_forums 
    ADD CONSTRAINT fk_sub_forums_last_thread 
    FOREIGN KEY (last_thread_id) REFERENCES forum_threads(id) ON DELETE SET NULL;

ALTER TABLE sub_forums 
    ADD CONSTRAINT fk_sub_forums_last_comment 
    FOREIGN KEY (last_comment_id) REFERENCES comments(id) ON DELETE SET NULL;

CREATE TABLE saved_posts (
    user_id BIGINT NOT NULL REFERENCES users(internal_id) ON DELETE CASCADE,
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    saved_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, post_id)                     -- Composite PK: Mỗi user chỉ lưu 1 bài 1 lần
);

CREATE INDEX idx_saved_posts_user ON saved_posts(user_id);
CREATE INDEX idx_saved_posts_post ON saved_posts(post_id);

COMMENT ON TABLE saved_posts IS 'Bookmark posts. Trọng số cao (8 điểm) trong ranking algorithm';

CREATE TABLE shares (
    id BIGINT PRIMARY KEY,                              -- TSID
    user_id BIGINT NOT NULL REFERENCES users(internal_id) ON DELETE CASCADE,
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    shared_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_shares_user ON shares(user_id);
CREATE INDEX idx_shares_post ON shares(post_id);
CREATE INDEX idx_shares_created ON shares(shared_at DESC);

COMMENT ON TABLE shares IS 'Share posts. Trọng số cao nhất (10 điểm) trong ranking algorithm';

-- Follow Requests/Relationships: Hỗ trợ cả Public (follow ngay) và Private (cần approval)
CREATE TYPE follow_status AS ENUM ('PENDING', 'ACCEPTED', 'REJECTED');

CREATE TABLE user_follows (
    follower_id BIGINT NOT NULL REFERENCES users(internal_id) ON DELETE CASCADE,
    target_id BIGINT NOT NULL REFERENCES users(internal_id) ON DELETE CASCADE,
    status follow_status DEFAULT 'ACCEPTED',            -- PENDING nếu target là private account
    requested_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP, -- Thời gian request
    accepted_at TIMESTAMP WITH TIME ZONE,               -- Thời gian accept (nếu status = ACCEPTED)
    rejected_at TIMESTAMP WITH TIME ZONE,               -- Thời gian reject (nếu status = REJECTED)
    PRIMARY KEY (follower_id, target_id)              -- Composite PK
    -- Note: Business logic (không follow chính mình) được xử lý ở Backend
);

CREATE INDEX idx_follows_follower ON user_follows(follower_id);  -- Lấy danh sách đang follow (Build Feed)
CREATE INDEX idx_follows_target ON user_follows(target_id);      -- Lấy danh sách người theo dõi (Count/Notify)
CREATE INDEX idx_follows_status ON user_follows(status);         -- Filter theo status
CREATE INDEX idx_follows_target_pending ON user_follows(target_id, status) WHERE status = 'PENDING'; -- Lấy follow requests PENDING của user

COMMENT ON TABLE user_follows IS 'Follow relationships với status. Public accounts: status=ACCEPTED ngay. Private accounts: status=PENDING, cần approval';

-- Join Requests: User xin vào Forum hoặc Community
CREATE TYPE join_request_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED');

CREATE TABLE join_requests (
    id BIGINT PRIMARY KEY,                              -- TSID
    user_id BIGINT NOT NULL REFERENCES users(internal_id) ON DELETE CASCADE,
    forum_id BIGINT REFERENCES forums(id) ON DELETE CASCADE,
    community_id BIGINT REFERENCES communities(id) ON DELETE CASCADE,
    status join_request_status DEFAULT 'PENDING',
    message TEXT,                                        -- Lời nhắn khi xin join (optional)
    reviewed_by BIGINT REFERENCES users(internal_id) ON DELETE SET NULL, -- Người duyệt (admin/moderator)
    reviewed_at TIMESTAMP WITH TIME ZONE,               -- Thời gian duyệt
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
    -- Note: Business logic (join request phải có forum hoặc community) được xử lý ở Backend
    -- Note: Unique constraints được tạo bằng partial unique index bên dưới
);

CREATE INDEX idx_join_requests_user ON join_requests(user_id);
CREATE INDEX idx_join_requests_forum ON join_requests(forum_id) WHERE forum_id IS NOT NULL;
CREATE INDEX idx_join_requests_community ON join_requests(community_id) WHERE community_id IS NOT NULL;
CREATE INDEX idx_join_requests_status ON join_requests(status);
CREATE INDEX idx_join_requests_forum_status ON join_requests(forum_id, status) WHERE forum_id IS NOT NULL;
CREATE INDEX idx_join_requests_community_status ON join_requests(community_id, status) WHERE community_id IS NOT NULL;
CREATE INDEX idx_join_requests_created ON join_requests(created_at DESC);

-- Partial unique indexes (thay thế UNIQUE constraint với WHERE clause)
CREATE UNIQUE INDEX uq_join_request_user_forum ON join_requests(user_id, forum_id) WHERE forum_id IS NOT NULL;
CREATE UNIQUE INDEX uq_join_request_user_community ON join_requests(user_id, community_id) WHERE community_id IS NOT NULL;

COMMENT ON TABLE join_requests IS 'Yêu cầu tham gia Forum hoặc Community. Status: PENDING, APPROVED, REJECTED';

-- User Topic Follows: Users follow topics để xem posts về topic đó trong feed
CREATE TABLE user_topic_follows (
    user_id BIGINT NOT NULL REFERENCES users(internal_id) ON DELETE CASCADE,
    topic_id BIGINT NOT NULL REFERENCES topics(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, topic_id)
);

CREATE INDEX idx_user_topic_follows_user ON user_topic_follows(user_id);
CREATE INDEX idx_user_topic_follows_topic ON user_topic_follows(topic_id);

COMMENT ON TABLE user_topic_follows IS 'Users follow topics. Dùng để build feed: lấy posts từ topics user đã follow';


-- =====================================================
-- 6. COMMUNITY MEMBERSHIP
-- =====================================================

CREATE TABLE community_members (
    community_id BIGINT NOT NULL REFERENCES communities(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(internal_id) ON DELETE CASCADE,
    role VARCHAR(50) DEFAULT 'MEMBER',                 -- MEMBER, MODERATOR, ADMIN (role_id -> role)
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(255) DEFAULT 'ACTIVE',               -- ACTIVE, BANNED, LEFT
    PRIMARY KEY (community_id, user_id)
);

CREATE INDEX idx_community_members_community ON community_members(community_id);
CREATE INDEX idx_community_members_user ON community_members(user_id);
CREATE INDEX idx_community_members_role ON community_members(role);
CREATE INDEX idx_community_members_status ON community_members(status);

COMMENT ON TABLE community_members IS 'Membership trong Communities. Role: MEMBER, MODERATOR, ADMIN';


-- =====================================================
-- 7. MEDIA/ATTACHMENTS
-- =====================================================

CREATE TABLE media (
    id BIGINT PRIMARY KEY,                              -- TSID
    post_id BIGINT REFERENCES posts(id) ON DELETE CASCADE,
    thread_id BIGINT REFERENCES forum_threads(id) ON DELETE CASCADE,
    comment_id BIGINT REFERENCES comments(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(internal_id) ON DELETE CASCADE,
    media_type VARCHAR(50) NOT NULL,                   -- IMAGE, VIDEO, GIF (type)
    media_url TEXT NOT NULL,                            -- Media URL (media_url)
    thumbnail_url TEXT,                                 -- Thumbnail URL
    file_size BIGINT,                                   -- File size (bytes)
    width INTEGER,                                      -- Width
    height INTEGER,                                     -- Height
    duration INTERVAL,                                  -- Duration (for video) - không phải NOT NULL
    position INTEGER DEFAULT 0,                         -- Display order (position)
    display_order INTEGER DEFAULT 0,                    -- Display order
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
    -- Note: Business logic (media phải có post, thread, hoặc comment) được xử lý ở Backend
);

CREATE INDEX idx_media_post ON media(post_id) WHERE post_id IS NOT NULL;
CREATE INDEX idx_media_thread ON media(thread_id) WHERE thread_id IS NOT NULL;
CREATE INDEX idx_media_comment ON media(comment_id) WHERE comment_id IS NOT NULL;
CREATE INDEX idx_media_user ON media(user_id);

COMMENT ON TABLE media IS 'Media attachments cho Posts, Threads, và Comments';


-- =====================================================
-- 8. NOTIFICATIONS
-- =====================================================

CREATE TYPE notification_type AS ENUM (
    'LIKE', 'COMMENT', 'REPLY', 'FOLLOW', 
    'MENTION', 'COMMUNITY_INVITE', 'SYSTEM'
);

CREATE TABLE notifications (
    id BIGINT PRIMARY KEY,                              -- TSID
    user_id BIGINT NOT NULL REFERENCES users(internal_id) ON DELETE CASCADE,
    type notification_type NOT NULL,
    actor_id BIGINT REFERENCES users(internal_id) ON DELETE SET NULL,
    post_id BIGINT REFERENCES posts(id) ON DELETE CASCADE,
    thread_id BIGINT REFERENCES forum_threads(id) ON DELETE CASCADE,
    comment_id BIGINT REFERENCES comments(id) ON DELETE CASCADE,
    community_id BIGINT REFERENCES communities(id) ON DELETE CASCADE,
    content TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read, created_at DESC) WHERE is_read = FALSE;
CREATE INDEX idx_notifications_created ON notifications(created_at DESC);

COMMENT ON TABLE notifications IS 'Thông báo cho users';


-- =====================================================
-- 9. INITIAL DATA (Featured Topics)
-- =====================================================

-- Insert một số Topics mặc định (giống Threads của Meta)
INSERT INTO topics (id, name, slug, description, is_featured) VALUES
    (1000000000000000001, 'Technology', 'technology', 'Công nghệ và phần mềm', TRUE),
    (1000000000000000002, 'News', 'news', 'Tin tức và thời sự', TRUE),
    (1000000000000000003, 'Entertainment', 'entertainment', 'Giải trí và văn hóa', TRUE),
    (1000000000000000004, 'Sports', 'sports', 'Thể thao', TRUE),
    (1000000000000000005, 'Lifestyle', 'lifestyle', 'Lối sống', TRUE),
    (1000000000000000006, 'Education', 'education', 'Giáo dục và học tập', TRUE)
ON CONFLICT DO NOTHING;
