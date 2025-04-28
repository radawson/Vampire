# Vampire Plugin

A Minecraft plugin that adds vampire mechanics to your server, allowing players to become vampires, infect others, and use various vampire abilities.

## Credits

This plugin is a re-work of the MassiveCraft Vampire plugin. The original plugin was created by the MassiveCraft team. This version has been modernized, improved, and adapted for newer Minecraft versions with enhanced features and better code organization. It also no longer depends on the MassiveCore or other Massiver Plugins.

## Features

- **Vampire Transformation**: Players can become vampires through infection or commands.
- **Blood System**: Vampires need blood to survive and can offer blood to other players.
- **Infection Mechanics**: Vampires can infect other players, gradually turning them into vampires.
- **Vampire Abilities**:
  - Bloodlust Mode: Increased damage and speed.
  - Night Vision: See in the dark.
  - Intent Mode: Toggle infection chance on attack.
  - Shriek: Infect nearby players.
- **Sunlight Effects**: Vampires take damage in direct sunlight, configurable protection via blocks and armor.
- **Altars**: Special multi-block structures for infection (Dark Altar) and curing (Light Altar).
- **Holy Water / Blood Vials**: Craftable/obtainable items with effects on vampires/undead.
- **Dark Gift**: Vampires can offer to turn human players directly.
- **Database Support**: SQLite and MySQL database options with Hibernate ORM.
- **Localization**: Full language support with customizable messages.

## Gameplay

- **Becoming a Vampire**: Get infected by another vampire's attack (Intent mode increases chance) or via a Dark Altar ritual. Infection progresses over time, eventually transforming the player.
- **Survival**: Maintain your blood level by feeding on certain mobs or players (configurable). Avoid direct sunlight unless protected by blocks or armor.
- **Abilities**: Use commands like `/vampire mode` to toggle Night Vision and Intent. Use `/vampire shriek` to infect groups.
- **Altars**: Build Dark and Light Altars according to the structure requirements defined in `CONFIG.md` (materials within a radius around a specific core block).
  - To activate an altar: Ensure you have the required resource items (also defined in `CONFIG.md`) in your inventory.
  - Right-click the central core block of the completed altar structure.
  - If the structure, your permissions, and your resources are valid, a ritual will begin.
  - You **must remain relatively still** near the altar for a short channeling period (default 3 seconds, configurable).
  - If you move too far, the ritual cancels. If you stay still, the resources are consumed, and the altar's effect (infection for Dark, curing for Light) occurs.
- **Trading**: Use `/vampire offer` and `/vampire accept` to trade blood with other players.
- **Dark Gift**: Vampires can use `/vampire offergift` to offer turning a nearby human player, who can `/vampire acceptgift` or `/vampire rejectgift`.

## Commands

The main command is `/vampire`. You can also use the shorter alias `/v`. 

### Player Commands

*   `/vampire help [topic]` - Shows the command list or help on a specific lore topic.
*   `/vampire info` - Show general plugin information (version, authors, etc.).
*   `/vampire show [player]` - Show detailed status if the target player is a vampire.
*   `/vampire list [page]` - Show a list of online vampires and infected players.
*   `/vampire shriek` - Use the shriek ability.
*   `/vampire offer <player> <amount>` - Offer blood to another player.
*   `/vampire accept` - Accept a pending blood offer.
*   `/vampire reject` - Reject a pending blood offer.
*   `/vampire stats [player]` - Show general vampire statistics (own or others with perm).
*   `/vampire offergift <player>` - Offer the Dark Gift to a human player.
*   `/vampire acceptgift` - Accept a pending Dark Gift offer.
*   `/vampire rejectgift` - Reject a pending Dark Gift offer.
*   `/vampire flask [blood|holy]` - Create a Blood Vial (requires glass bottle) or Holy Water (admin command).

#### Mode Toggles (Shortcuts)

These commands toggle different vampire abilities or states:

*   `/vampire intent` - Toggle infection intent mode (requires `vampire.mode.intent`).
*   `/vampire bloodlust` - Toggle bloodlust mode (requires `vampire.mode.bloodlust`).
*   `/vampire nightvision` - Toggle night vision mode (requires `vampire.mode.nightvision`).
    *   *Note: You can also use the longer `/vampire mode <intent|bloodlust|nightvision>`.* 

### Admin Commands

*   `/vampire set <type> <value> [player]` - Set various player properties (requires `vampire.set` and sub-permissions). Types: `vampire`, `infection`, `blood`, `level`.
*   `/vampire reload` - Reloads configuration files (requires `vampire.config`).
*   `/vampire reset <player>` - Resets a player's vampire data (cures them) (requires `vampire.reset`).
*   `/vampire debug [on|off]` - Toggles runtime debug logging (requires `vampire.debug.toggle`).

## Permissions

*   `vampire.base` - Base permission for general user commands (Default: true)
*   `vampire.info` - Allows viewing plugin info with `/vampire info` (Default: true)
*   `vampire.show` - Allows viewing own detailed vampire status with `/vampire show` (Default: true)
*   `vampire.show.other` - Allows viewing others' detailed vampire status with `/vampire show [player]` (Default: op)
*   `vampire.list` - Allows viewing the online vampire/infected list (Default: op)
*   `vampire.shriek` - Allows using the shriek ability (Default: true)
*   `vampire.mode.bloodlust` - Allows toggling bloodlust mode (Default: true)
*   `vampire.mode.nightvision` - Allows toggling nightvision mode (Default: true)
*   `vampire.mode.intent` - Allows toggling infection intent mode (Default: true)
*   `vampire.trade.offer` - Allows offering blood (Default: true)
*   `vampire.trade.accept` - Allows accepting blood offers (Default: true)
*   `vampire.set` - Base permission for admin set commands (Default: op)
*   `vampire.set.vampire.true` - Allows making players vampires (Default: op)
*   `vampire.set.vampire.false` - Allows curing vampires (Default: op)
*   `vampire.set.infection` - Allows setting infection level (Default: op)
*   `vampire.set.food` - Allows setting food level (Default: op)
*   `vampire.set.health` - Allows setting health (Default: op)
*   `vampire.config` - Allows reloading configuration with `/vampire reload` (Default: op)
*   `vampire.lang` - Allows reloading language files with `/vampire reload` (Default: op)
*   `vampire.altar.dark` - Allows using Dark Altars (Default: true)
*   `vampire.altar.light` - Allows using Light Altars (Default: true)
*   `vampire.help.command` - Allows viewing the command list via `/vampire help`. (Default: true)
*   `vampire.help.lore` - Allows viewing detailed explanations via `/vampire help <topic>`. (Default: op)
*   `vampire.gift.offer` - Allows offering the Dark Gift (Default: op)
*   `vampire.gift.accept` - Allows accepting/rejecting the Dark Gift (Default: true)
*   `vampire.stats` - Allows viewing own general stats (Default: true)
*   `vampire.stats.other` - Allows viewing others' general stats (Default: op)
*   `vampire.reset` - Allows resetting player vampire data (Default: op)
*   `vampire.flask` - Allows creating/using blood vials (Default: true)
*   `vampire.flask.holywater` - Allows obtaining Holy Water via `/vampire flask holy` command (Default: op)
*   `vampire.is.vampire` - Indicator permission (managed by plugin)
*   `vampire.is.human` - Indicator permission (managed by plugin)
*   `vampire.bypass` (Default: `false`) - Grants immunity to most vampire effects and updates (sun damage, blood drain, etc.). Intended for administrators or specific testing scenarios. Must be granted explicitly.

## Configuration

The plugin uses `config.yml` for settings and YAML files in the `languages/` directory for messages.

**For detailed explanations of all configuration settings in `config.yml`, please see [CONFIG.md](CONFIG.md).**

### Language Files

Language files allow customization of all plugin messages. You can copy `en.yml` to create new language files (e.g., `es.yml`). Use `&` for color codes.

## Development

### Project Structure

- `org.clockworx.vampire` - Main package
  - `VampirePlugin.java` - Main plugin class
  - `altar` - Altar structure and management classes
  - `cmd` - Command handlers
  - `config` - Configuration loading classes
  - `database` - Database interface, Hibernate implementation
  - `entity` - Data entities (VampirePlayer, BloodOffer, Hibernate Entities)
  - `event` - Custom Bukkit events
  - `listener` - Bukkit event listeners
  - `manager` - Core logic managers (VampireManager, AltarManager, etc.)
  - `task` - Repeating background tasks
  - `util` - Utility classes (Messages, Effects, Resources, etc.)

### Key Components

- **`VampireManager`**: Handles caching, loading, saving, and modification of `VampirePlayer` data.
- **`AltarManager`**: Manages altar registration, structure validation, and interaction flow.
- **`AltarAbstract` subclasses (`AltarDark`, `AltarLight`)**: Define specific altar behavior (preconditions, resources, effects).
- **Database System (`DatabaseManager`, `HibernateDatabaseManager`)**: Persists player data using Hibernate ORM.
- **Event System (`AbstractVampireEvent` subclasses)**: Allows interaction with plugin logic (e.g., `EventAltarUse`, `EventVampirePlayerInfectionChange`).
- **Utilities (`VampireMessages`, `FxUtil`, `ResourceUtil`, `SunUtil`)**: Provide centralized functions for common tasks.

## Building

To build the plugin, use the following command:

```bash
./gradlew build
```

The compiled plugin JAR will be available in `build/libs/`.

## License

This plugin is licensed under the MIT License. See the LICENSE file for details.
