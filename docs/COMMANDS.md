# Vampire Plugin Commands

This document provides comprehensive documentation for all commands available in the Vampire plugin.

## Command Overview

The Vampire plugin uses `/vampire` (or `/v`) as the base command, with various subcommands for different functionalities.

### Quick Reference

| Command | Permission | Description | Player Only |
|---------|-----------|-------------|-------------|
| `/vampire help [topic]` | `vampire.help.command` | Display command help or lore | No |
| `/vampire info` | `vampire.info` | Show plugin information | No |
| `/vampire show [player]` | `vampire.show` | Show detailed vampire status | No |
| `/vampire stats [player]` | `vampire.stats` | Display vampire statistics | No |
| `/vampire list [page]` | `vampire.list` | List online vampires and infected | No |
| `/vampire shriek` | `vampire.shriek` | Perform a vampire shriek | Yes |
| `/vampire mode <type>` | `vampire.mode.*` | Toggle vampire modes | Yes |
| `/vampire flask [type]` | `vampire.flask` | Create blood vials or holy water | Yes |
| `/vampire set <type> <value> [player]` | `vampire.set.*` | Set vampire properties (Admin) | No |
| `/vampire reset <player>` | `vampire.reset` | Reset player's vampire data (Admin) | No |
| `/vampire reload` | `vampire.config` | Reload plugin configuration (Admin) | No |
| `/vampire debug` | `vampire.base` | Toggle debug mode (Admin) | No |
| `/vampire offergift <player>` | `vampire.gift.offer` | Offer the Dark Gift (Admin) | Yes |
| `/vampire acceptgift` | `vampire.gift.accept` | Accept Dark Gift offer | Yes |
| `/vampire rejectgift` | `vampire.gift.accept` | Reject Dark Gift offer | Yes |

## Command Categories

### User Commands
Commands available to all players (with appropriate permissions):
- [Help Command](commands/help.md) - Get help and lore information
- [Info Command](commands/info.md) - View plugin information
- [Show Command](commands/show.md) - Display vampire status
- [Stats Command](commands/stats.md) - View vampire statistics
- [List Command](commands/list.md) - List online vampires
- [Shriek Command](commands/shriek.md) - Perform vampire shriek
- [Mode Commands](commands/mode.md) - Toggle vampire modes
- [Flask Command](commands/flask.md) - Create blood vials

### Admin Commands
Commands for server administrators:
- [Set Command](commands/set.md) - Modify vampire properties
- [Reset Command](commands/reset.md) - Reset player vampire data
- [Reload Command](commands/reload.md) - Reload configuration
- [Debug Command](commands/debug.md) - Toggle debug mode

### Gift Commands
Commands for the Dark Gift feature:
- [Offer Gift Command](commands/offergift.md) - Offer the Dark Gift
- [Accept Gift Command](commands/acceptgift.md) - Accept Dark Gift offer
- [Reject Gift Command](commands/rejectgift.md) - Reject Dark Gift offer

## Permission System

The plugin uses a hierarchical permission system. Most commands have base permissions that default to `true` for all players, while admin commands default to `op` only.

### Permission Structure

- `vampire.*` - Grants all vampire permissions (default: op)
- `vampire.base` - Base permission for user commands (default: true)
- `vampire.set.*` - All set command permissions (default: op)
- `vampire.mode.*` - All mode toggle permissions (default: true)

See [plugin.yml](../plugin.yml) for the complete permission structure.

## Command Aliases

The base command `/vampire` can also be accessed via `/v` (if configured in your server's command aliases).

## Mode Shortcuts

Some mode commands can be accessed directly without the `mode` subcommand:
- `/vampire bloodlust` - Toggle bloodlust mode
- `/vampire intent` - Toggle infection intent mode
- `/vampire nightvision` - Toggle night vision mode

These shortcuts require the same permissions as their full command equivalents.

## Tab Completion

All commands support tab completion where applicable:
- Player names are suggested for commands that accept player arguments
- Command names are suggested for the base command
- Mode types are suggested for the mode command
- Set types are suggested for the set command

## Error Messages

All error messages are localized and can be customized in the language files located at `plugins/Vampire/languages/`.

## See Also

- [Configuration Guide](../CONFIG.md) - Plugin configuration options
- [Schema Documentation](../schema.md) - Database schema information
- [README](../README.md) - General plugin information

