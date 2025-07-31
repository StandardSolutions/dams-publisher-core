-- Lock operations for PostgreSQL
-- Used by OutboxSchemaInitializer for preventing concurrent initialization

-- Insert lock (used in acquireLock)
INSERT INTO schema_initialization_lock (instance_id, acquired_at) VALUES (?, NOW());

-- Delete lock (used in releaseLock)
DELETE FROM schema_initialization_lock WHERE instance_id = ?;

-- Check for stale locks (used in isLockStale)
SELECT 1 FROM schema_initialization_lock WHERE acquired_at < NOW() - INTERVAL '5 minutes';

-- Clear stale locks (used in clearStaleLock)
DELETE FROM schema_initialization_lock WHERE acquired_at < NOW() - INTERVAL '5 minutes'; 