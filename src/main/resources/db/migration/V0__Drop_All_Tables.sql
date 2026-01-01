-- =====================================================
-- DROP ALL TABLES (Cleanup script)
-- Run this before V1__Initial_Schema.sql if tables already exist
-- =====================================================

-- Drop tables in reverse dependency order
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS media CASCADE;
DROP TABLE IF EXISTS user_topic_follows CASCADE;
DROP TABLE IF EXISTS topics CASCADE;
DROP TABLE IF EXISTS community_members CASCADE;
DROP TABLE IF EXISTS join_requests CASCADE;
DROP TABLE IF EXISTS user_follows CASCADE;
DROP TABLE IF EXISTS shares CASCADE;
DROP TABLE IF EXISTS saved_posts CASCADE;
DROP TABLE IF EXISTS comment_likes CASCADE;
DROP TABLE IF EXISTS thread_likes CASCADE;
DROP TABLE IF EXISTS post_likes CASCADE;
DROP TABLE IF EXISTS comments CASCADE;
DROP TABLE IF EXISTS posts CASCADE;
DROP TABLE IF EXISTS communities CASCADE;
DROP TABLE IF EXISTS forum_threads CASCADE;
DROP TABLE IF EXISTS sub_forums CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS forums CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Drop custom types
DROP TYPE IF EXISTS follow_status CASCADE;
DROP TYPE IF EXISTS join_request_status CASCADE;
DROP TYPE IF EXISTS notification_type CASCADE;

