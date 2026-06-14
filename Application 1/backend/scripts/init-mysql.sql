-- Script MySQL pour production / développement local avec DB_MODE=mysql
CREATE DATABASE IF NOT EXISTS getevents_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE getevents_db;

-- Les tables sont créées automatiquement par Exposed (SchemaUtils.createMissingTablesAndColumns)
