# Vampire Plugin Database Schema

This document outlines the data structure and relationships used in the Vampire Plugin's database systems.

## Core Entities

### VampirePlayer

Represents a player's vampire-related data and state.

| Field | Type | Description |
|-------|------|-------------|
| uuid | UUID | Unique identifier for the player |
| name | string | The player's name |
| is_vampire | boolean | Whether the player is a vampire |
| is_infected | boolean | Whether the player is infected |
| infection_level | double | Current infection level (0.0 to 1.0) |
| infection_reason | string | Reason for the infection |
| infection_time | long | Timestamp when the player was infected |
| blood | double | Current blood level (0.0 to 1.0) |
| blood_level | double | Current blood level (0.0 to 1.0) |
| mode | string | Current vampire mode (disabled, bloodlust, nightvision, intent) |
| last_blood_regen | long | Timestamp of last blood regeneration |
| last_infection_update | long | Timestamp of last infection update |
| last_mode_change | long | Timestamp of last mode change |
| last_infection_reason | string | Reason for last infection |
| last_shriek_time | long | Timestamp of last shriek |
| last_blood_trade_time | long | Timestamp of last blood trade |
| last_blood_trade_partner | UUID | UUID of last blood trade partner |
| last_blood_trade_amount | double | Amount of last blood trade |
| last_blood_trade_type | string | Type of last blood trade |
| bloodlust_mode | boolean | Whether bloodlust mode is enabled |
| night_vision_mode | boolean | Whether night vision mode is enabled |
| intent_mode | boolean | Whether intent mode is enabled |
| last_bloodlust_time | long | Timestamp of last bloodlust |
| maker_id | UUID | UUID of the vampire who turned this player |
| intending | boolean | Whether the player is intending |
| bloodlusting | boolean | Whether the player is bloodlusting |
| using_night_vision | boolean | Whether the player is using night vision |
| temperature | double | Player's temperature |
| radiation | double | Player's radiation level |
| last_damage_time | long | Timestamp of last damage |
| last_shriek_wait_message_time | long | Timestamp of last shriek wait message |
| truce_break_time_left | long | Time left until truce break |
| trade_offered_from | VampirePlayer | Player who offered a trade |
| trade_offered_amount | double | Amount of blood offered in trade |
| trade_offered_at_time | long | Timestamp when trade was offered |

### BloodOffer

Represents a blood offer between vampires.

| Field | Type | Description |
|-------|------|-------------|
| id | int | Unique identifier for the offer (SQLite/MySQL only) |
| sender_uuid | UUID | UUID of the vampire offering blood |
| target_uuid | UUID | UUID of the vampire being offered blood |
| amount | double | Amount of blood being offered |
| timestamp | long | When the offer was created |
| accepted | boolean | Whether the offer was accepted |
| rejected | boolean | Whether the offer was rejected |

### Infection

Represents an infection event.

| Field | Type | Description |
|-------|------|-------------|
| id | int | Unique identifier for the infection (SQLite/MySQL only) |
| player_uuid | UUID | UUID of the infected player |
| amount | double | Amount of infection added |
| reason | string | Reason for the infection |
| timestamp | long | When the infection occurred |

## Database Tables

### vampire_players

Stores vampire player data.

```sql
CREATE TABLE vampire_players (
    uuid VARCHAR(36) PRIMARY KEY,
    name VARCHAR(16) NOT NULL,
    is_vampire BOOLEAN DEFAULT FALSE,
    is_infected BOOLEAN DEFAULT FALSE,
    infection_level DOUBLE DEFAULT 0.0,
    infection_reason VARCHAR(255),
    infection_time BIGINT,
    blood DOUBLE DEFAULT 0.0,
    blood_level DOUBLE DEFAULT 0.0,
    mode VARCHAR(16) DEFAULT 'disabled',
    last_blood_regen BIGINT,
    last_infection_update BIGINT,
    last_mode_change BIGINT,
    last_infection_reason VARCHAR(255),
    last_shriek_time BIGINT,
    last_blood_trade_time BIGINT,
    last_blood_trade_partner VARCHAR(36),
    last_blood_trade_amount DOUBLE,
    last_blood_trade_type VARCHAR(16),
    bloodlust_mode BOOLEAN DEFAULT FALSE,
    night_vision_mode BOOLEAN DEFAULT FALSE,
    intent_mode BOOLEAN DEFAULT FALSE,
    last_bloodlust_time BIGINT,
    maker_id VARCHAR(36),
    intending BOOLEAN DEFAULT FALSE,
    bloodlusting BOOLEAN DEFAULT FALSE,
    using_night_vision BOOLEAN DEFAULT FALSE,
    temperature DOUBLE DEFAULT 0.0,
    radiation DOUBLE DEFAULT 0.0,
    last_damage_time BIGINT,
    last_shriek_wait_message_time BIGINT,
    truce_break_time_left BIGINT,
    trade_offered_from VARCHAR(36),
    trade_offered_amount DOUBLE,
    trade_offered_at_time BIGINT
);
```

### blood_offers

Stores blood offer data.

```sql
CREATE TABLE blood_offers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    sender_uuid VARCHAR(36),
    target_uuid VARCHAR(36),
    amount DOUBLE,
    timestamp BIGINT,
    accepted BOOLEAN DEFAULT FALSE,
    rejected BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (sender_uuid) REFERENCES vampire_players(uuid),
    FOREIGN KEY (target_uuid) REFERENCES vampire_players(uuid)
);
```

### infections

Stores infection history.

```sql
CREATE TABLE infections (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    player_uuid VARCHAR(36),
    amount DOUBLE,
    reason VARCHAR(255),
    timestamp BIGINT,
    FOREIGN KEY (player_uuid) REFERENCES vampire_players(uuid)
);
```

## YAML Structure

### players/[uuid].yml

```yaml
uuid: "player-uuid"
name: "PlayerName"
is_vampire: false
is_infected: false
infection_level: 0.0
infection_reason: "reason"
infection_time: 1234567890
blood: 0.0
blood_level: 0.0
mode: "disabled"
last_blood_regen: 1234567890
last_infection_update: 1234567890
last_mode_change: 1234567890
last_infection_reason: "reason"
last_shriek_time: 1234567890
last_blood_trade_time: 1234567890
last_blood_trade_partner: "partner-uuid"
last_blood_trade_amount: 0.5
last_blood_trade_type: "offer"
bloodlust_mode: false
night_vision_mode: false
intent_mode: false
last_bloodlust_time: 1234567890
maker_id: "maker-uuid"
intending: false
bloodlusting: false
using_night_vision: false
temperature: 0.0
radiation: 0.0
last_damage_time: 1234567890
last_shriek_wait_message_time: 1234567890
truce_break_time_left: 0
trade_offered_from: "offerer-uuid"
trade_offered_amount: 0.5
trade_offered_at_time: 1234567890
```

### blood_offers.yml

```yaml
offers:
  target-uuid:
    sender: "sender-uuid"
    target: "target-uuid"
    amount: 1.0
    timestamp: 1234567890
    accepted: false
    rejected: false
```

### infections.yml

```yaml
infections:
  player-uuid:
    timestamp:
      amount: 0.1
      reason: "reason"
```

## Data Relationships

1. **VampirePlayer to BloodOffer**
   - One VampirePlayer can have multiple outgoing blood offers (as sender)
   - One VampirePlayer can have multiple incoming blood offers (as target)
   - Each BloodOffer must have exactly one sender and one target

2. **VampirePlayer to Infection**
   - One VampirePlayer can have multiple infection records
   - Each Infection must be associated with exactly one VampirePlayer

## Data Constraints

1. **Blood Level**
   - Range: 0.0 to 1.0
   - Cannot be negative
   - Cannot exceed maximum (1.0)

2. **Infection Level**
   - Range: 0.0 to 1.0
   - Cannot be negative
   - Cannot exceed maximum (1.0)

3. **Blood Offers**
   - Amount must be positive
   - Cannot have both accepted and rejected true
   - Cannot accept or reject an already accepted/rejected offer

4. **Infections**
   - Amount must be positive
   - Reason cannot be null
   - Timestamp must be valid

## Database Manager Interface

All database managers (MySQL, SQLite, YAML) must implement the following core functionality:

1. **Player Management**
   - Get player data
   - Save player data
   - Set vampire status
   - Set infection level
   - Set blood level

2. **Blood Offer Management**
   - Create blood offer
   - Get blood offer
   - Accept blood offer
   - Reject blood offer
   - Get all blood offers
   - Cleanup expired offers

3. **Infection Management**
   - Add infection
   - Get infection level
   - Set infection level

4. **Configuration Management**
   - Load config
   - Save config
   - Load language
   - Save language

Each operation must be:

- Asynchronous (using CompletableFuture)
- Thread-safe
- Properly error-handled
- Consistent across all implementations 