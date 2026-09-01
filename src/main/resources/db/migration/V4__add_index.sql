CREATE INDEX idx_photo_user_id ON photo(user_id);

CREATE INDEX idx_photo_processing ON photo (updated_at)
    WHERE status = 'PROCESSING';

CREATE INDEX idx_analysis_photo_id ON analysis(photo_id);
;