# Vampire Plugin

A Minecraft plugin that adds vampire mechanics to your server, allowing players to become vampires, infect others, and use various vampire abilities.

## Credits

This plugin is a re-work of the MassiveCraft Vampire plugin. The original plugin was created by the MassiveCraft team. This version has been modernized, improved, and adapted for newer Minecraft versions with enhanced features and better code organization. It also no longer depends on the MassiveCore or other Massiver Plugins.

## Features

- **Vampire Transformation**: Players can become vampires through infection or commands
- **Blood System**: Vampires need blood to survive and can offer blood to other players
- **Infection Mechanics**: Vampires can infect other players, gradually turning them into vampires
- **Vampire Abilities**: 
  - Bloodlust Mode: Increased damage and speed
  - Night Vision: See in the dark
  - Intent Mode: Show vampire status to other players
  - Shriek: Infect nearby players
- **Holy Water**: Players can use holy water to damage vampires
- **Altars**: Dark and light altars with special effects
- **Database Support**: SQLite and MySQL database options with Hibernate ORM
- **Localization**: Full language support with customizable messages

## Commands

- `/vampire help` - Show help information
- `/vampire version` - Show plugin version
- `/vampire show [player]` - Show vampire status
- `/vampire list [page]` - Show a list of vampires
- `/vampire set <type> <value> [player]` - Set vampire properties
- `/vampire reset [player]` - Reset vampire status
- `/vampire reload` - Reload plugin configuration
- `/vampire offer <player> <amount>` - Offer blood to another player
- `/vampire accept` - Accept a blood offer
- `/vampire reject` - Reject a blood offer
- `/vampire shriek` - Shriek to infect nearby players
- `/vampire mode <bloodlust|nightvision|intend>` - Set vampire mode
- `/vampire stats` - Show vampire statistics

## Permissions

- `vampire.use` - Allows use of basic vampire commands
- `vampire.admin` - Allows use of admin commands
- `vampire.trade.offer` - Allows offering blood to other players
- `vampire.trade.accept` - Allows accepting blood offers
- `vampire.mode.bloodlust` - Allows toggling bloodlust mode
- `vampire.mode.nightvision` - Allows toggling nightvision mode
- `vampire.mode.intend` - Allows toggling infection intent mode
- `vampire.list` - Allows viewing the vampire list
- `vampire.show` - Allows viewing vampire status
- `vampire.show.other` - Allows viewing other players' vampire status
- `vampire.shriek` - Allows using the vampire shriek ability
- `vampire.set` - Allows setting player vampire status
- `vampire.is.vampire` - Allows becoming a vampire
- `vampire.is.human` - Allows remaining human
- `vampire.config` - Allows modifying plugin configuration
- `vampire.lang` - Allows modifying plugin language
- `vampire.bypass` - Allows bypassing vampire restrictions
- `vampire.flask` - Allows using vampire flask
- `vampire.reset` - Allows resetting vampire status

## Configuration

The plugin uses YAML format for configuration files and language files. Database storage is handled by either MySQL or SQLite backends.

### Language Files

Language files are stored in YAML format in the `languages` directory. To add a new language, simply create a new YAML file in the `languages` directory (e.g., `fr.yml` for French) and translate all the messages.

### Database Configuration

The plugin supports SQLite and MySQL databases using Hibernate ORM. Configure your database settings in `config.yml`:

```yaml
database:
  type: sqlite  # or mysql
  url: jdbc:sqlite:plugins/Vampire/database.db  # for SQLite
  # For MySQL:
  # url: jdbc:mysql://localhost:3306/vampire
  # user: root
  # password: password
```

### Language Configuration

The plugin uses a comprehensive language system that allows for easy customization of all messages. Language files are stored in the `languages` directory and follow a hierarchical structure:

```yaml
general:
  prefix: "&8[&cVampire&8]"
  reload: "&aPlugin reloaded successfully!"

command:
  help:
    header: "&7=== &cVampire Commands &7==="
    info: "&7/vampire info [player] &8- &fShow vampire info for a player"
```

You can use color codes with the `&` symbol and placeholders with the `%variable%` format. The plugin will automatically replace these placeholders with the appropriate values.

To add a new language, simply create a new YAML file in the `languages` directory (e.g., `fr.yml` for French) and translate all the messages.

## Development

### Project Structure

- `org.clockworx.vampire` - Main package
  - `VampirePlugin.java` - Main plugin class
  - `entity` - Entity classes (VampirePlayer, BloodOffer)
  - `config` - Configuration classes
  - `database` - Database managers and Hibernate configuration
  - `cmd` - Command classes
  - `listener` - Event listeners
  - `task` - Scheduled tasks
  - `util` - Utility classes

### Key Components

#### DatabaseManager

The `DatabaseManager` interface defines methods for:

- Player data management
- Blood offer handling
- Infection tracking
- Configuration storage

The plugin uses Hibernate ORM for database operations, supporting both SQLite and MySQL backends.

#### LanguageConfig

The `LanguageConfig` class manages:

- Loading language files
- Retrieving localized messages
- Message formatting with placeholders

## Building

To build the plugin, use the following command:

```bash
./gradlew build
```

The compiled plugin will be available in `build/libs/`.

## Roadmap

- Add better language support
- Add vampire levels

## License

This plugin is licensed under the MIT License. See the LICENSE file for details.
