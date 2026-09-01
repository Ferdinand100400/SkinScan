CREATE TABLE IF NOT EXISTS users
(
    id         UUID PRIMARY KEY,
    login      VARCHAR NOT NULL,
    password   VARCHAR NOT NULL,
    email      VARCHAR,
    phone      VARCHAR,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at  TIMESTAMP WITH TIME ZONE
);