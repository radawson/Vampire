-- Vampire Player Data
CREATE TABLE vampire_player (
    uuid VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(255),
    is_vampire BOOLEAN DEFAULT FALSE NOT NULL,
    blood_level DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
    infection_level DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
    infection_reason VARCHAR(255),
    infection_time BIGINT DEFAULT 0 NOT NULL,
    last_shriek_time BIGINT DEFAULT 0 NOT NULL,
    last_blood_trade_time BIGINT DEFAULT 0 NOT NULL,
    last_blood_trade_partner VARCHAR(36),
    last_blood_trade_amount DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
    last_blood_trade_type VARCHAR(50),
    vampire_level INT DEFAULT 0 NOT NULL,
    maker_id VARCHAR(36)
);

-- Index for potential lookups by vampire status
CREATE INDEX idx_vampire_player_is_vampire ON vampire_player(is_vampire);


-- Blood Offer Data
-- Note: Using standard INTEGER PRIMARY KEY for SQLite auto-increment behavior
--       and AUTO_INCREMENT for MySQL (Flyway handles dialect differences here if needed)
CREATE TABLE blood_offer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, -- For MySQL. SQLite uses INTEGER PRIMARY KEY for auto-increment. Flyway might adapt, or use specific syntax if needed.
    sender_uuid VARCHAR(36) NOT NULL,
    target_uuid VARCHAR(36) NOT NULL,
    amount DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
    timestamp BIGINT NOT NULL,
    accepted BOOLEAN DEFAULT FALSE NOT NULL,
    rejected BOOLEAN DEFAULT FALSE NOT NULL
);

-- Indexes for efficient offer lookup and cleanup
CREATE INDEX idx_blood_offer_target ON blood_offer(target_uuid, accepted, rejected);
CREATE INDEX idx_blood_offer_timestamp ON blood_offer(timestamp);
