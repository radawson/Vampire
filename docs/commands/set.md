# Set Command

**Command:** `/vampire set <type> <value> [player]`  
**Alias:** `/v set <type> <value> [player]`  
**Permission:** `vampire.set.*` (varies by type)  
**Default:** Operators only

## Description

Admin command to modify vampire-related properties for players. Can set vampire status, infection level, blood level, and vampire level.

## Usage

```
/vampire set <type> <value> [player]
/v set <type> <value> [player]
```

### Arguments

- **`type`** - The property to set: `vampire`, `infection`, `blood`, or `level`
- **`value`** - The value to set (format depends on type)
- **`player`** (optional) - Target player name (defaults to yourself if you're a player)

## Set Types

### Vampire Status
```
/vampire set vampire <true|false|on|off|1|0> [player]
```

Sets whether a player is a vampire.

**Values:**
- `true`, `on`, `1` - Makes the player a vampire
- `false`, `off`, `0` - Cures the player (removes vampirism)

**Permission:** `vampire.set` (or `vampire.set.vampire.true`/`vampire.set.vampire.false`)

### Infection Level
```
/vampire set infection <0.0-1.0> [player]
```

Sets a player's infection level.

**Values:**
- `0.0` to `1.0` - Infection level (0.0 = no infection, 1.0 = fully infected)

**Permission:** `vampire.set.infection`

### Blood Level
```
/vampire set blood <amount> [player]
```

Sets a player's current blood level.

**Values:**
- Any non-negative number - Blood amount (0.0 or higher)

**Permission:** `vampire.set.food` (note: uses food permission node)

### Vampire Level
```
/vampire set level <0-5> [player]
```

Sets a player's vampire level.

**Values:**
- `0` to `5` (or higher if configured) - Vampire level

**Permission:** `vampire.set.level`

## Examples

```
/vampire set vampire true Notch
```
Makes Notch a vampire.

```
/vampire set infection 0.5 Steve
```
Sets Steve's infection level to 50%.

```
/vampire set blood 20.0
```
Sets your own blood level to 20.0 (if you're a player).

```
/vampire set level 3 Alex
```
Sets Alex's vampire level to 3.

## Permissions

- **`vampire.set`** - Base permission for all set commands (default: op)
- **`vampire.set.vampire.true`** - Set player as vampire (default: op)
- **`vampire.set.vampire.false`** - Cure player (default: op)
- **`vampire.set.infection`** - Set infection level (default: op)
- **`vampire.set.food`** - Set blood level (default: op)
- **`vampire.set.level`** - Set vampire level (default: op)

## Tab Completion

Tab completion provides suggestions for:
1. **Type argument:** `vampire`, `infection`, `blood`, `level`
2. **Value argument:** 
   - For `vampire`: `true`, `false`
   - For `level`: `0`, `1`, `2`, `3`, `4`, `5`
   - For `infection`: `0.0`, `0.5`, `1.0`
3. **Player argument:** Online player names

## Error Messages

- **"You must be a player or specify a player name"** - Console must specify a player
- **"Player not found: <name>"** - Player has never joined the server
- **"You don't have permission to use this command"** - Missing required permission
- **"Invalid boolean value: '<value>'. Use 'true' or 'false'"** - Invalid value for vampire type
- **"Invalid value: '<value>'. Must be between 0.0 and 1.0"** - Invalid infection value
- **"Invalid value: '<value>'. Must be a non-negative number"** - Invalid blood value
- **"Invalid value: '<value>'. Must be a non-negative whole number"** - Invalid level value
- **"Unknown set type: '<type>'. Valid types: vampire, infection, blood, level"** - Invalid type

## Success Messages

- **"Successfully set <player>'s vampire status to <value>"**
- **"Successfully set <player>'s infection level to <value>"**
- **"Successfully set <player>'s blood level to <value>"**
- **"Successfully set <player>'s vampire level to <value>"**

## Notes

- Setting a player as a vampire (`vampire true`) automatically clears their infection
- Setting infection level to 0.0 removes the infection
- Blood level can exceed the maximum for the player's level (but will be capped by gameplay)
- Vampire level affects maximum blood capacity and other level-based features
- All changes are saved to the database immediately
- The command works on both online and offline players (if they've joined before)

## See Also

- [Reset Command](reset.md) - Reset all vampire data for a player
- [Configuration Guide](../CONFIG.md) - Plugin configuration options

