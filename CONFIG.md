# Vampire Plugin Configuration Details

This file provides detailed explanations for the settings found in `config.yml`. For general features, commands, and permissions, please see the main [README.md](README.md).

## Database Configuration

The plugin supports SQLite and MySQL databases using Hibernate ORM. Configure your database settings under the `database:` key in `config.yml`:

```yaml
database:
  # Database type (mysql or sqlite)
  type: "sqlite"
  # Database URL (for MySQL: jdbc:mysql://localhost:3306/vampire)
  # For SQLite: jdbc:sqlite:plugins/Vampire/database.db
  url: "jdbc:sqlite:plugins/Vampire/database.db"
  # Database username (for MySQL)
  user: ""
  # Database password (for MySQL)
  password: ""
```

-   **`type`**: Specify `"sqlite"` (recommended for most servers) or `"mysql"`.
-   **`url`**: The JDBC connection string. Use the example format for your chosen type. SQLite path is relative to the server root.
-   **`user`**: MySQL username (leave blank for SQLite).
-   **`password`**: MySQL password (leave blank for SQLite).

---

## General Settings

Basic plugin operation settings found under the `general:` key:

```yaml
general:
  # Enable detailed console logs for debugging issues.
  debug: false
  # Default language file to load from the 'languages' folder (e.g., "en", "es").
  language: "en"
  # Save player data automatically when they quit the server.
  save-on-quit: true
  # Enable periodic auto-saving of all online vampire data.
  auto-save: true
  # Interval in minutes for auto-saving data (if enabled).
  auto-save-interval: 5
```

---

## Vampire Settings

Core mechanics for vampires, found under the `vampire:` key:

```yaml
vampire:
  # Blood settings
  max-blood: 20.0          # Maximum blood level (like default health/food).
  blood-regen-rate: 0.1    # Base blood points regenerated per second under ideal conditions.
  blood-decrease-rate: 0.05 # Base blood points lost per second passively.
  low-blood-threshold: 2.0 # Blood level below which Weakness may be applied.

  # Combat settings
  combat:
    damageFactor:
      withBloodlust: 2.0   # Damage multiplier when bloodlust is active.
      withoutBloodlust: 1.5 # Damage multiplier when bloodlust is not active.
    infectRisk:
      withIntent: 0.3      # Chance (0.0-1.0) to infect on hit when Intent mode is active.
      withoutIntent: 0.1    # Chance (0.0-1.0) to infect on hit when Intent mode is off.

  # Infection settings
  can-infect: true         # Global toggle for whether vampires can infect others.
  infection-chance: 0.25   # Base chance (0.0-1.0) for infection attempt on hit (modified by Intent mode). Deprecated/superseded by combat.infectRisk? Review needed.
  infection-duration: 60   # Duration in seconds? Ticks? Needs clarification. Affects infection progression? Review needed.
  infection-rate: 0.1      # How much infection progresses per second? Review needed.

  # Blood trade settings
  can-offer-blood: true    # Allow `/vampire offer` command.
  max-blood-offer: 5.0     # Maximum blood points transferable in one offer.
  trade:
    offerTolerance: 30000  # How long (milliseconds) a blood offer remains valid (30 seconds).
    maxDistance: 5.0       # Maximum distance (blocks) between players for offering/accepting blood.

  # Shriek settings
  shriek:
    cooldown: 30000        # Cooldown in milliseconds for the `/vampire shriek` ability (30 seconds).
    sound: "ENTITY_ENDER_DRAGON_GROWL" # Sound played when shriek is used.

  # Truce settings (Functionality needs review/implementation details)
  truce:
    breakTime: 300000      # Duration in milliseconds for a truce (5 minutes).

  # Effects settings (Applied persistently based on state)
  # Note: These define the *base* effects. Duration is often very long as they are reapplied.
  effects:
    vampire: # Applied when player is a vampire
      type: "INCREASE_DAMAGE"
      duration: 100000
      amplifier: 1
    bloodlust: # Applied when bloodlust is active
      type: "SPEED"
      duration: 100000
      amplifier: 2
    nightvision: # Applied when night vision mode is active (and conditions met)
      type: "NIGHT_VISION"
      duration: 100000
      amplifier: 0
    infected: # Applied when player is infected (but not yet a vampire)
      type: "CONFUSION" # Consider changing to WEAKNESS or SLOWNESS?
      duration: 100000
      amplifier: 0
    human: # Applied when player is human (potentially for balance)
      type: "DAMAGE_RESISTANCE" # Or remove if no effect desired
      duration: 100000
      amplifier: 0

  # Night vision settings (Manual toggle via /vampire mode nightvision)
  night-vision:
    enabled: true          # Allow `/vampire mode nightvision`.
    level: 1               # Potion effect level (1 = Amplifier 0).

  # Bloodlust settings (Automatic activation at low blood)
  bloodlust:
    # Note: These seem to control a *temporary* bloodlust effect, potentially conflicting
    # with the persistent one in effects.vampire. Needs review/clarification.
    duration: 300          # Duration in seconds? Ticks?
    damage-boost: 1.5      # Multiplier?
    speed-boost: 1.2       # Multiplier?
    cooldown: 600          # Cooldown in seconds? Ticks?
    threshold: 15.0        # Blood level below which this might activate.
    blood-decrease: 0.1    # Extra blood drain per second/tick?

  # Sun damage settings (Simplified, primary logic now in 'sunlight' section)
  # These might be legacy settings, verify if still used or superseded by 'sunlight.base_damage'
  sun-damage: true
  sun-damage-amount: 1.0
  sunlight-damage: 1.0
```

*Note: Some settings under `vampire:` (like `infection-chance`, `infection-duration`, `infection-rate`, `bloodlust`, `sun-damage`) might need review as they potentially overlap or conflict with newer, more specific sections (`combat`, `effects`, `sunlight`).*

---

## Sunlight Configuration

Settings related to how sunlight affects vampires.

```yaml
sunlight:
  # Damage amount per damage check interval when fully exposed (0 total opacity).
  base_damage: 1.0

  # Block Opacity Settings
  # How much sunlight specific block materials block (0.0 = transparent, 1.0 = fully opaque).
  # Affects the block directly above the player's head when checking sky light access.
  # Blocks *not* listed here default to 1.0 (fully opaque).
  block_opacity:
    AIR: 0.0
    GLASS: 0.05
    GLASS_PANE: 0.05
    WATER: 0.3
    ICE: 0.1
    PACKED_ICE: 0.2
    BLUE_ICE: 0.3
    LAVA: 0.0
    HONEY_BLOCK: 0.3
    SLIME_BLOCK: 0.3
    SNOW: 0.1 # Single layer
    SNOW_BLOCK: 0.4
    # Example Leaves
    OAK_LEAVES: 0.2
    SPRUCE_LEAVES: 0.25
    # Example Other Transparent
    VINE: 0.1
    LADDER: 0.1
    IRON_BARS: 0.15
    COBWEB: 0.1
    # Add other materials as needed.

  # Armor Opacity Settings
  # Base sun protection provided by the armor *material*.
  # Represents opacity if the piece provided 100% coverage (weight 1.0).
  armor_base_material_opacities:
    LEATHER: 0.15
    CHAINMAIL: 0.25
    IRON: 0.40
    GOLD: 0.30
    DIAMOND: 0.55
    NETHERITE: 0.70
    TURTLE: 0.35 # Turtle Shell
    ELYTRA: 0.05

  # Weight modifier based on armor *type/slot*.
  # Represents approximate body coverage. Sum for a full set is ~1.0.
  # Final contribution = base_material_opacity * type_weight
  armor_type_weights:
    HELMET: 0.20
    CHESTPLATE: 0.45
    LEGGINGS: 0.25
    BOOTS: 0.10
    ELYTRA: 0.45 # Uses the Chestplate weight
```

**Calculation:** Total protection is determined by combining terrain opacity (block above head) and total armor opacity. Armor opacity is calculated per piece (`material_opacity * type_weight`) and summed up. The final irradiation level (0.0 to 1.0) multiplies the `base_damage`.

---

## Altar Configuration

Settings for the Dark and Light Altars.

```yaml
altar:
  # Globally enable/disable altar functionality.
  enabled: true
  # Radius (cube) around the core block to scan for structure materials.
  # PERFORMANCE NOTE: Larger values (e.g., >5) will scan more blocks and impact performance.
  # Recommended: 3-5 blocks for optimal balance between flexibility and performance.
  # Validation only occurs on player interaction (right-click), not constantly.
  search-radius: 3
  # Minimum ratio (0.0-1.0) of required materials that must be present within the radius
  # for the structure to be considered valid (in addition to specific counts).
  # This is a secondary check - per-material minimum counts are validated first.
  # Allows creative builds with extra decorative blocks.
  min-ratio: 0.5

  # Dark Altar settings (Infection)
  dark:
    # PERFORMANCE NOTE: Choose UNCOMMON materials for core blocks (e.g., OBSIDIAN, DIAMOND_BLOCK).
    # Common materials like STONE or DIRT will trigger validation checks on every interaction
    # with that material type, potentially impacting server performance.
    core-material: "OBSIDIAN" # Block players interact with.
    # Map of materials required for the structure (Material: Minimum Count).
    materials:
      OBSIDIAN: 4
      WITHER_ROSE: 1
      DIAMOND_BLOCK: 2
    # List of items consumed on use (Format: "MATERIAL:AMOUNT").
    resources:
      - "REDSTONE:10"
      - "WITHER_ROSE:1"
    # Delay in ticks before the ritual takes effect (60 ticks = 3 seconds).
    channeling_delay_ticks: 60
    # Maximum distance (blocks) player can move from starting point before cancelling.
    max_movement_distance: 1.5

  # Light Altar settings (Curing)
  light:
    # PERFORMANCE NOTE: Choose UNCOMMON materials for core blocks (e.g., DIAMOND_BLOCK, EMERALD_BLOCK).
    # Common materials like STONE or DIRT will trigger validation checks on every interaction
    # with that material type, potentially impacting server performance.
    core-material: "DIAMOND_BLOCK" # Changed default from LAPIS_BLOCK for consistency
    materials:
      GOLD_BLOCK: 4
      GLOWSTONE: 1
      EMERALD_BLOCK: 2
    resources:
      - "LAPIS_LAZULI:10"
      - "DIAMOND:1"
    channeling_delay_ticks: 60
    max_movement_distance: 1.5
```

### Altar Performance Considerations

**Sysadmin Responsibility:**

1. **Core Material Selection**: 
   - Choose **uncommon materials** for core blocks (e.g., `OBSIDIAN`, `DIAMOND_BLOCK`, `EMERALD_BLOCK`)
   - **Avoid common materials** like `STONE`, `DIRT`, `GRASS_BLOCK`, `COBBLESTONE`
   - Common materials will trigger validation checks on every player interaction with that block type
   - This can significantly impact server performance on busy servers

2. **Search Radius**:
   - Recommended: **3-5 blocks** for optimal performance
   - Larger values (e.g., >5) scan exponentially more blocks: `(2*radius+1)^3` blocks
   - Example: radius 3 = 343 blocks, radius 5 = 1331 blocks, radius 7 = 3375 blocks
   - Validation only occurs on player right-click interaction, not constantly

3. **Structure Flexibility**:
   - The validation system supports creative builds with extra decorative blocks
   - Minimum material counts are checked strictly (per-material validation)
   - Ratio check is secondary and more lenient, allowing extra blocks beyond minimums
   - Altars with only a core block requirement validate instantly (no scanning)

4. **Performance Optimizations**:
   - Early exits for simple cases (only core material required)
   - Per-material validation before expensive ratio checks
   - Core block handling optimized to avoid double-counting
   - Block scanning only occurs when core material matches

---

## Gift Offer Settings

Configuration for the Dark Gift feature (`/vampire offergift`, etc.).

```yaml
gift:
  # Enable the Dark Gift commands.
  enabled: true
  # Max distance (blocks) for offering/accepting the gift.
  max-distance: 5.0
  # Blood cost subtracted from the offering vampire upon success.
  blood-cost: 5.0
  # How long (seconds) a gift offer remains valid before expiring.
  offer-tolerance-seconds: 60
```

---

## Blood Sources Configuration

Define which mobs give blood when hit and how much.

```yaml
blood_sources:
  # Amount of blood gained per successful hit on these entity types.
  # Set amount to 0 to disable gaining blood. Uses Bukkit EntityType names.
  gain_per_hit:
    PLAYER: 0.5
    VILLAGER: 1.0
    COW: 0.8
    PIG: 0.7
    SHEEP: 0.7
    # ... (add other desired living entities)
    # Example: Disable blood from chickens
    CHICKEN: 0.0
    # Undead/non-living mobs typically default to 0 implicitly, but can be listed.
    ZOMBIE: 0.0
    SKELETON: 0.0

  # Damage dealt to a *player* when a vampire hits them to gain blood.
  # Set to 0.0 if feeding shouldn't harm the source player.
  # Note: This is separate from damage to mobs.
  player_health_cost_on_hit: 0.0 # Renamed for clarity from legacy 'damage_per_hit'

# Legacy setting, possibly superseded by player_health_cost_on_hit? Review needed.
# damage_per_hit: 0.5

# Should hitting a creature make it aggressive towards the vampire?
anger_on_hit: true
```

---

## Item Settings

Configure the effects of custom plugin items like Blood Vials and Holy Water.

```yaml
items:
  blood_vial:
    # Amount of infection (0.0 to 1.0) applied when splashed on a non-vampire player.
    infection_amount: 0.15 # e.g., 15%
  holy_water:
    # Damage dealt to vampires when splashed.
    vampire_damage: 4.0 # e.g., 2 hearts
    # Damage dealt to standard undead (zombies, skeletons) when splashed.
    undead_damage: 6.0 # e.g., 3 hearts
    # Amount of infection (0.0 to 1.0) cured when splashed on an infected player.
    infection_cure_amount: 0.20 # e.g., 20%
```

---

## Vampire Level System (`levels.yml`)

This file defines the progression path for vampires. Each level can grant different stats, abilities, or modify existing mechanics.

**Structure:**
- The file contains a `levels:` map.
- Each key under `levels:` is the level number (as a string, e.g., '0', '1').
- Each level has nested keys defining its properties.

**Current Properties (WIP):**
- `description`: A short text description for the level (used in `/vampire show`).
- `max_blood`: The maximum blood capacity at this level.
- `blood_regen_rate`: A multiplier applied to the base blood regeneration rate from `config.yml`.
- `sun_modifier`: A multiplier applied to incoming sun damage (1.0 = normal, <1.0 = less damage, >1.0 = more damage).
- `speed_boost`: A multiplier intended to affect movement speed (Implementation likely uses potion effects).
- `can_use_shriek`: Boolean (`true`/`false`) determining if the shriek ability is usable.
- `shriek_cooldown_modifier`: A multiplier applied to the base shriek cooldown from `config.yml` (e.g., 0.8 = 20% shorter cooldown).

*More stats and ability flags may be added to this system in the future.*

---

*This covers the main configuration sections. Refer to the `config.yml` file itself for all available options and default values.* 