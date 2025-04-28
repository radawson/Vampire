-- Vampire Player Data
CREATE TABLE ${tablePrefix}players (
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
    total_blood_consumed DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
    vampire_level INT DEFAULT 0 NOT NULL,
    maker_id VARCHAR(36)
);

-- Index for potential lookups by vampire status
CREATE INDEX idx_${tablePrefix}players_is_vampire ON ${tablePrefix}players(is_vampire);
CREATE INDEX idx_${tablePrefix}players_blood_consumed ON ${tablePrefix}players(total_blood_consumed);


-- Blood Offer Data
-- Note: Using standard INTEGER PRIMARY KEY for SQLite auto-increment behavior
--       and AUTO_INCREMENT for MySQL (Flyway handles dialect differences here if needed)
CREATE TABLE ${tablePrefix}blood_offers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, -- Reverted to AUTO_INCREMENT for MySQL compatibility
    sender_uuid VARCHAR(36) NOT NULL,
    target_uuid VARCHAR(36) NOT NULL,
    amount DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
    timestamp BIGINT NOT NULL,
    accepted BOOLEAN NOT NULL DEFAULT FALSE, -- Use standard BOOLEAN for portability
    rejected BOOLEAN NOT NULL DEFAULT FALSE  -- Use standard BOOLEAN for portability
);

-- Indexes for efficient offer lookup and cleanup
CREATE INDEX idx_${tablePrefix}blood_offers_target ON ${tablePrefix}blood_offers(target_uuid, accepted, rejected);
CREATE INDEX idx_${tablePrefix}blood_offers_timestamp ON ${tablePrefix}blood_offers(timestamp);
