-- CRM Platform — PostgreSQL initialization
-- Runs once when the postgres container is first created.
-- Hibernate ddl-auto=update handles table creation per service,
-- so this script only creates the database and grants.

\c crm_db;

-- Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";   -- fuzzy text search

-- Grant full access to the app user
GRANT ALL PRIVILEGES ON DATABASE crm_db TO crm_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO crm_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO crm_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO crm_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO crm_user;
