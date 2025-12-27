# Help Command

**Command:** `/vampire help [topic]`  
**Alias:** `/v help [topic]`  
**Permission:** `vampire.help.command` (base), `vampire.help.lore` (for topics)  
**Default:** All players can view command help, operators can view lore

## Description

The help command displays information about available commands and detailed lore about vampire mechanics. It serves as both a command reference and an in-game guide.

## Usage

### Basic Command List
```
/vampire help
/v help
```

Displays a list of all available commands that the player has permission to use, along with their descriptions and usage patterns.

### Lore Topics
```
/vampire help <topic>
/v help <topic>
```

Displays detailed information about specific vampire mechanics. Available topics include:
- `blood_regeneration` - How blood regeneration works
- `infection` - How the infection system works
- `sunlight` - Sunlight effects and protection

## Examples

```
/vampire help
```
Shows the command list.

```
/vampire help infection
```
Shows detailed information about the infection mechanic.

```
/vampire help sunlight
```
Shows information about sunlight effects and how to protect yourself.

## Permissions

- **`vampire.help.command`** - Required to view the command list (default: true)
- **`vampire.help.lore`** - Required to view detailed lore topics (default: op)

## Tab Completion

When typing a topic name, tab completion suggests available lore topics:
- `blood_regeneration`
- `infection`
- `sunlight`

## Output Format

### Command List
The command list shows:
- Command name
- Usage pattern (if any)
- Brief description

Example output:
```
--- Vampire Help (Alias: /v) ---
/vampire show [player] - Show detailed vampire status
/vampire stats [player] - Display vampire statistics
...
```

### Lore Topics
Lore topics display formatted information with:
- Section headers
- Detailed explanations
- Configuration references
- Gameplay tips

## Notes

- The help command respects permission checks - players only see commands they can use
- Lore topics are stored in the language files and can be customized
- Mode shortcuts are also listed in the help output if the player has permission

