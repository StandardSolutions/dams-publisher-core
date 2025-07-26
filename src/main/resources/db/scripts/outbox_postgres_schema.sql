-- Outbox table for the outbox pattern
CREATE TABLE IF NOT EXISTS outbox_message (
    id UUID PRIMARY KEY,
    payload TEXT NOT NULL,
    type VARCHAR(255) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    processed_at TIMESTAMPTZ
);

-- Index for fetching unprocessed messages by recipient and type
CREATE INDEX IF NOT EXISTS idx_outbox_unprocessed_recipient_type
    ON outbox_message (recipient, type)
    WHERE processed_at IS NULL;

-- Index for processed_at for cleanup/archival
CREATE INDEX IF NOT EXISTS idx_outbox_processed_at
    ON outbox_message (processed_at); 