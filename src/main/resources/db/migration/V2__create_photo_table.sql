CREATE TABLE IF NOT EXISTS photo
(
    id                UUID PRIMARY KEY,
    user_id           UUID,
    file_name         VARCHAR NOT NULL,
    storage_file_path VARCHAR NOT NULL,
    file_size_bytes   INT     NOT NULL,
    mime_type         VARCHAR NOT NULL,
    status            VARCHAR NOT NULL,
    created_at        TIMESTAMP WITH TIME ZONE,
    updated_at        TIMESTAMP WITH TIME ZONE
);