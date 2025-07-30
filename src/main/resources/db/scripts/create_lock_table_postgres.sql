-- Create lock table for PostgreSQL
-- Used by OutboxSchemaInitializer for preventing concurrent initialization

CREATE TABLE IF NOT EXISTS schema_initialization_lock (
    instance_id VARCHAR(255) PRIMARY KEY,
    acquired_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
); 