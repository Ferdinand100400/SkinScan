CREATE TABLE IF NOT EXISTS analysis
(
    id           UUID PRIMARY KEY,
    photo_id     UUID    NOT NULL,
    status       VARCHAR NOT NULL,
    result       JSONB,
    requested_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE
);