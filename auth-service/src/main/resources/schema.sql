-- This file defines the schema for the patient service database. --


DROP SCHEMA IF EXISTS auth_service CASCADE;
-- Recreate the schema for the patient service.
CREATE SCHEMA IF NOT EXISTS auth_service AUTHORIZATION pm_admin;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA auth_service TO pm_admin;

-- Ensure the 'users' table exists
CREATE TABLE IF NOT EXISTS auth_service."users" (
   id UUID PRIMARY KEY,
   email VARCHAR(255) UNIQUE NOT NULL,
   password VARCHAR(255) NOT NULL,
   role VARCHAR(50) NOT NULL
);

