-- Outbox table for the outbox pattern
-- Version: 1.0
-- Safe for concurrent execution
-- PostgreSQL compatible

-- Create schema version tracking table if not exists
CREATE TABLE IF NOT EXISTS schema_version (
    id SERIAL PRIMARY KEY,
    version VARCHAR(50) NOT NULL,
    applied_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    description TEXT
);

-- Create lock table for preventing concurrent initialization
CREATE TABLE IF NOT EXISTS schema_initialization_lock (
    instance_id VARCHAR(255) PRIMARY KEY,
    acquired_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Create outbox table (IF NOT EXISTS handles concurrent execution)
CREATE TABLE IF NOT EXISTS outbox_message (
    id UUID PRIMARY KEY,
    payload TEXT NOT NULL,
    type VARCHAR(255) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    processed_at TIMESTAMPTZ
);

-- Create indexes with IF NOT EXISTS
CREATE INDEX IF NOT EXISTS idx_outbox_unprocessed_recipient_type
    ON outbox_message (recipient, type)
    WHERE processed_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_outbox_processed_at
    ON outbox_message (processed_at);

-- Record schema version (ignore conflicts for concurrent execution)
INSERT INTO schema_version (version, description) 
VALUES ('1.0', 'Initial outbox_message table and indexes')
ON CONFLICT DO NOTHING; 