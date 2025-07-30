-- Create lock table for MySQL
-- Used by OutboxSchemaInitializer for preventing concurrent initialization

CREATE TABLE IF NOT EXISTS schema_initialization_lock (
    instance_id VARCHAR(255) PRIMARY KEY,
    acquired_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
); 