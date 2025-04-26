-- Add total_blood_consumed column to track player progression
ALTER TABLE vampire_player
ADD COLUMN total_blood_consumed DOUBLE PRECISION DEFAULT 0.0 NOT NULL;

-- Optional: Add an index if you plan to query based on this frequently
CREATE INDEX idx_vampire_player_blood_consumed ON vampire_player(total_blood_consumed); 