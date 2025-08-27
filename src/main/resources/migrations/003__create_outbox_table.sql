CREATE TABLE IF NOT EXISTS ${OUTBOX_TABLE} (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_id VARCHAR(255) NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    processed_at TIMESTAMP,
    
    FOREIGN KEY (recipient_id) REFERENCES ${RECIPIENT_TABLE}(id)
);