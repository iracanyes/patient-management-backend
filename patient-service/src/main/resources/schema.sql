-- This file defines the schema for the patient service database. --

--DROP DATABASE IF EXISTS patient_db;

--CREATE USER pm_admin WITH ENCRYPTED PASSWORD 'pm_password';
--CREATE DATABASE patient_db WITH OWNER pm_admin;

DROP SCHEMA IF EXISTS patient_service CASCADE;
-- Recreate the schema for the patient service.
CREATE SCHEMA IF NOT EXISTS patient_service AUTHORIZATION pm_admin;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA patient_service TO pm_admin;


CREATE SCHEMA IF NOT EXISTS patient_service;

-- Ensure the table "patients" exists with the required fields.
CREATE TABLE IF NOT EXISTS patient_service.patients(
    patient_id UUID PRIMARY KEY,
    firstname VARCHAR(50) NOT NULL,
    lastname VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(15),
    birthdate DATE NOT NULL
);

