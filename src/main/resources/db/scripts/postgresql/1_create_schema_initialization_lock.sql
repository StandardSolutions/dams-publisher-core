-- Create lock table for preventing concurrent initialization
CREATE TABLE IF NOT EXISTS dams_publisher_changelog_lock (
    instance_id VARCHAR(255) PRIMARY KEY,
    acquired_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);