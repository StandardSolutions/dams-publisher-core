-- Outbox table for the outbox pattern
-- Version: 1.0
-- Safe for concurrent execution
-- H2 Database compatible

-- Create schema version tracking table if not exists
CREATE TABLE IF NOT EXISTS schema_version (
    id IDENTITY PRIMARY KEY,
    version VARCHAR(50) NOT NULL,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    description VARCHAR(1000)
);

-- Create lock table for preventing concurrent initialization
CREATE TABLE IF NOT EXISTS schema_initialization_lock (
    instance_id VARCHAR(255) PRIMARY KEY,
    acquired_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP()
);

-- Create outbox table (IF NOT EXISTS handles concurrent execution)
CREATE TABLE IF NOT EXISTS outbox_message (
    id UUID PRIMARY KEY,
    payload CLOB NOT NULL,
    type VARCHAR(255) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    processed_at TIMESTAMP
);

-- Create indexes with IF NOT EXISTS
CREATE INDEX IF NOT EXISTS idx_outbox_unprocessed_recipient_type
    ON outbox_message (recipient, type)
    WHERE processed_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_outbox_processed_at
    ON outbox_message (processed_at);

-- Record schema version (H2 doesn't have ON CONFLICT, so we use MERGE)
MERGE INTO schema_version (id, version, applied_at, description)
KEY(id)
VALUES (NEXT VALUE FOR schema_version_seq, '1.0', CURRENT_TIMESTAMP(), 'Initial outbox_message table and indexes'); 