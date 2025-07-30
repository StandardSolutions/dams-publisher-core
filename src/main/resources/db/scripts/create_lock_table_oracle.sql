-- Create lock table for Oracle
-- Used by OutboxSchemaInitializer for preventing concurrent initialization

BEGIN
    EXECUTE IMMEDIATE 'CREATE TABLE schema_initialization_lock (
        instance_id VARCHAR2(255) PRIMARY KEY,
        acquired_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
    )';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -955 THEN NULL; -- Table already exists
        ELSE RAISE;
        END IF;
END;
/ 