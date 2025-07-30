-- Outbox table for the outbox pattern
-- Version: 1.0
-- Safe for concurrent execution
-- SQL Server compatible

-- Create schema version tracking table if not exists
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='schema_version' AND xtype='U')
CREATE TABLE schema_version (
    id INT IDENTITY(1,1) PRIMARY KEY,
    version NVARCHAR(50) NOT NULL,
    applied_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    description NVARCHAR(1000)
);

-- Create lock table for preventing concurrent initialization
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='schema_initialization_lock' AND xtype='U')
CREATE TABLE schema_initialization_lock (
    instance_id NVARCHAR(255) PRIMARY KEY,
    acquired_at DATETIME2 NOT NULL DEFAULT GETDATE()
);

-- Create outbox table (IF NOT EXISTS handles concurrent execution)
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='outbox_message' AND xtype='U')
CREATE TABLE outbox_message (
    id UNIQUEIDENTIFIER PRIMARY KEY,
    payload NVARCHAR(MAX) NOT NULL,
    type NVARCHAR(255) NOT NULL,
    recipient NVARCHAR(255) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    processed_at DATETIME2 NULL
);

-- Create indexes (SQL Server doesn't support IF NOT EXISTS for indexes)
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_outbox_unprocessed_recipient_type')
CREATE INDEX idx_outbox_unprocessed_recipient_type
    ON outbox_message (recipient, type)
    WHERE processed_at IS NULL;

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_outbox_processed_at')
CREATE INDEX idx_outbox_processed_at
    ON outbox_message (processed_at);

-- Record schema version (SQL Server uses IF NOT EXISTS for conflict handling)
IF NOT EXISTS (SELECT 1 FROM schema_version WHERE version = '1.0')
    INSERT INTO schema_version (version, description) 
    VALUES ('1.0', 'Initial outbox_message table and indexes'); 