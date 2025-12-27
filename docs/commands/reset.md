# Reset Command

**Command:** `/vampire reset <player>`  
**Alias:** `/v reset <player>`  
**Permission:** `vampire.reset`  
**Default:** Operators only

## Description

Completely resets a player's vampire data, curing them of vampirism, removing infection, and clearing blood levels. This is a comprehensive reset command for administrative use.

## Usage

```
/vampire reset <player>
/v reset <player>
```

### Arguments

- **`player`** (required) - The name of the player to reset

## Examples

```
/vampire reset Notch
```
Resets Notch's vampire data completely.

```
/v reset Steve
```
Resets Steve's vampire data (using alias).

## What Gets Reset

When a player is reset, the following data is cleared:
- Vampire status (set to false)
- Infection level (set to 0.0)
- Infection reason (cleared)
- Blood level (set to 0.0)
- All active modes (bloodlust, intent, night vision)
- Mode states (all disabled)

## Permissions

- **`vampire.reset`** - Required to reset player data (default: op)

## Tab Completion

Tab completion suggests online player names when typing the player argument.

## Error Messages

- **"Missing argument: <player>"** - No player name provided
- **"Player is not online: <name>"** - Player is not currently online
- **"Player data not found"** - Player's vampire data could not be loaded
- **"Player <name> is already human (not vampire or infected)"** - Player has nothing to reset

## Success Messages

- **"Successfully reset vampire data for <player>"** - Sent to the command sender
- **"Your vampire status has been reset by an administrator"** - Sent to the target player (if online)

## Notes

- The target player must be online for this command to work
- If a player is already human (not a vampire and not infected), the command will inform you that no reset is needed
- The reset is immediate and permanent - all vampire-related data is cleared
- The command logs the reset action with the administrator's name
- Reset data is saved to the database immediately

## Use Cases

- Removing a player from the vampire system completely
- Fixing corrupted player data
- Resetting players for testing purposes
- Curing players who want to start fresh

## See Also

- [Set Command](set.md) - Modify individual vampire properties
- [Configuration Guide](../CONFIG.md) - Plugin configuration options

