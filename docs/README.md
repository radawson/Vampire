# Vampire Plugin Documentation

Welcome to the Vampire plugin documentation. This directory contains comprehensive guides and references for using and configuring the plugin.

## Documentation Structure

### Commands
- **[COMMANDS.md](COMMANDS.md)** - Complete command reference and overview
- **[commands/](commands/)** - Detailed documentation for each command
  - [help.md](commands/help.md) - Help and lore command
  - [info.md](commands/info.md) - Plugin information command
  - [show.md](commands/show.md) - Show vampire status command
  - [stats.md](commands/stats.md) - View statistics command
  - [list.md](commands/list.md) - List vampires command
  - [shriek.md](commands/shriek.md) - Shriek ability command
  - [mode.md](commands/mode.md) - Mode toggle commands
  - [flask.md](commands/flask.md) - Flask creation command
  - [set.md](commands/set.md) - Admin set command
  - [reset.md](commands/reset.md) - Reset player data command
  - [reload.md](commands/reload.md) - Reload configuration command
  - [debug.md](commands/debug.md) - Debug mode command
  - [offergift.md](commands/offergift.md) - Offer Dark Gift command
  - [acceptgift.md](commands/acceptgift.md) - Accept Dark Gift command
  - [rejectgift.md](commands/rejectgift.md) - Reject Dark Gift command

## Quick Start

1. **New to the plugin?** Start with [COMMANDS.md](COMMANDS.md) for an overview
2. **Looking for a specific command?** Check the [commands/](commands/) directory
3. **Need configuration help?** See [../CONFIG.md](../CONFIG.md) in the root directory
4. **Database questions?** See [../schema.md](../schema.md) for database structure

## Command Categories

### User Commands
Commands available to all players (with appropriate permissions):
- Help, Info, Show, Stats, List
- Shriek, Mode toggles, Flask creation

### Admin Commands
Commands for server administrators:
- Set, Reset, Reload, Debug

### Gift Commands
Commands for the Dark Gift feature:
- Offer Gift, Accept Gift, Reject Gift

## Documentation Standards

Each command documentation includes:
- Command syntax and aliases
- Required permissions
- Usage examples
- Output format
- Error messages
- Tab completion information
- Configuration notes
- Related commands

## Contributing

When adding new commands or features:
1. Update [COMMANDS.md](COMMANDS.md) with the new command
2. Create a detailed documentation file in [commands/](commands/)
3. Follow the existing documentation format
4. Include examples and error cases

## See Also

- [README.md](../README.md) - General plugin information
- [CONFIG.md](../CONFIG.md) - Configuration guide
- [schema.md](../schema.md) - Database schema documentation

