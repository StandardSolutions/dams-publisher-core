-- Outbox table for the outbox pattern
-- Version: 1.0
-- Safe for concurrent execution
-- MySQL compatible

-- Create schema version tracking table if not exists
CREATE TABLE IF NOT EXISTS schema_version (
    id INT AUTO_INCREMENT PRIMARY KEY,
    version VARCHAR(50) NOT NULL,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    description TEXT
);

-- Create lock table for preventing concurrent initialization
CREATE TABLE IF NOT EXISTS schema_initialization_lock (
    instance_id VARCHAR(255) PRIMARY KEY,
    acquired_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create outbox table (IF NOT EXISTS handles concurrent execution)
CREATE TABLE IF NOT EXISTS outbox_message (
    id CHAR(36) PRIMARY KEY,
    payload LONGTEXT NOT NULL,
    type VARCHAR(255) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL
);

-- Create indexes with IF NOT EXISTS
CREATE INDEX IF NOT EXISTS idx_outbox_unprocessed_recipient_type
    ON outbox_message (recipient, type)
    WHERE processed_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_outbox_processed_at
    ON outbox_message (processed_at);

-- Record schema version (MySQL uses INSERT IGNORE for conflict handling)
INSERT IGNORE INTO schema_version (version, description) 
VALUES ('1.0', 'Initial outbox_message table and indexes'); 