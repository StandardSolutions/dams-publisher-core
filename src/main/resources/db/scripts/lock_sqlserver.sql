-- Lock operations for SQL Server
-- Used by OutboxSchemaInitializer for preventing concurrent initialization

-- Insert lock (used in acquireLock)
INSERT INTO schema_initialization_lock (instance_id, acquired_at) VALUES (?, GETDATE());

-- Delete lock (used in releaseLock)
DELETE FROM schema_initialization_lock WHERE instance_id = ?;

-- Check for stale locks (used in isLockStale)
SELECT 1 FROM schema_initialization_lock WHERE acquired_at < DATEADD(MINUTE, -5, GETDATE());

-- Clear stale locks (used in clearStaleLock)
DELETE FROM schema_initialization_lock WHERE acquired_at < DATEADD(MINUTE, -5, GETDATE()); 