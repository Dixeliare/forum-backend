-- =====================================================
-- Script to drop and recreate database
-- Run this in IntelliJ Database tool connected to 'postgres' database
-- =====================================================

-- Disconnect all connections to silre_backend
SELECT pg_terminate_backend(pg_stat_activity.pid)
FROM pg_stat_activity
WHERE pg_stat_activity.datname = 'silre_backend'
  AND pid <> pg_backend_pid();

-- Drop database if exists
DROP DATABASE IF EXISTS silre_backend;

-- Create fresh database
CREATE DATABASE silre_backend;

-- Note: After running this, reconnect to 'silre_backend' database
-- Then run V1__Initial_Schema.sql

