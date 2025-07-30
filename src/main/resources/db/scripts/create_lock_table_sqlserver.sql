-- Create lock table for SQL Server
-- Used by OutboxSchemaInitializer for preventing concurrent initialization

IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='schema_initialization_lock' AND xtype='U')
CREATE TABLE schema_initialization_lock (
    instance_id NVARCHAR(255) PRIMARY KEY,
    acquired_at DATETIME2 NOT NULL DEFAULT GETDATE()
); 