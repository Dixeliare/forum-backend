-- =====================================================
-- Migration: Change is_public to is_searchable in forums table
-- =====================================================

-- Drop old index
DROP INDEX IF EXISTS idx_forums_is_public;

-- Add new column is_searchable (default TRUE, same as old is_public default)
ALTER TABLE forums 
    ADD COLUMN IF NOT EXISTS is_searchable BOOLEAN DEFAULT TRUE;

-- Migrate data: is_searchable = NOT is_private (if is_private = false, then searchable = true)
-- This maintains the same logic: public forums (is_private = false) are searchable
UPDATE forums 
SET is_searchable = NOT is_private 
WHERE is_searchable IS NULL;

-- Set NOT NULL constraint
ALTER TABLE forums 
    ALTER COLUMN is_searchable SET NOT NULL;

-- Drop old column is_public
ALTER TABLE forums 
    DROP COLUMN IF EXISTS is_public;

-- Create new index for is_searchable
CREATE INDEX idx_forums_is_searchable ON forums(is_searchable) WHERE is_searchable = TRUE;

COMMENT ON COLUMN forums.is_searchable IS 'Có thể search (discoverable). Replaces old is_public column';

