# Vampire Plugin

A Minecraft plugin that adds vampire mechanics to your server, allowing players to become vampires, infect others, and use various vampire abilities.

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
- **Database Support**: MySQL and YAML database options for persistent data
- **Localization**: Full language support with customizable messages

## Commands

- `/vampire help` - Show help information
- `/vampire info [player]` - Show vampire info for a player
- `/vampire list [page]` - Show a list of vampires
- `/vampire offer <player> <amount>` - Offer blood to another player
- `/vampire accept` - Accept a blood offer
- `/vampire reject` - Reject a blood offer
- `/vampire shriek` - Shriek to infect nearby players
- `/vampire mode <bloodlust|nightvision|intent>` - Set vampire mode
- `/vampire reload` - Reload the plugin configuration

## Permissions

- `vampire.admin` - Access to admin commands
- `vampire.vampire` - Access to vampire commands
- `vampire.bypass` - Bypass vampire restrictions

## Configuration

The plugin uses two main configuration files:

1. `config.yml` - General plugin settings
2. `languages/en.yml` - Language messages

### Database Configuration

The plugin supports SQLlite, MySQL, and YAML databases for storing player data. Configure your database settings in `config.yml`:

```yaml
database:
  type: mysql  # or yaml
  host: localhost
  port: 3306
  name: vampire
  username: root
  password: ''
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
  - `database` - Database managers
  - `cmd` - Command classes
  - `listener` - Event listeners
  - `task` - Scheduled tasks
  - `util` - Utility classes

### Key Components

#### ResourceUtil

The `ResourceUtil` class provides utility methods for:
- Player inventory management
- Message formatting and sending
- Time and number formatting
- Text colorization
- Language message retrieval

This class is the central point for accessing language messages through the `getMessage(String key)` and `getMessage(String key, String... args)` methods.

#### DatabaseManager

The `DatabaseManager` interface defines methods for:
- Player data management
- Blood offer handling
- Infection tracking

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

## License

This plugin is licensed under the MIT License. See the LICENSE file for details.
