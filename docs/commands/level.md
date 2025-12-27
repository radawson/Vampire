# Level Command

**Command:** `/vampire level <operation> [amount] [player]`  
**Alias:** `/v level <operation> [amount] [player]`  
**Permission:** `vampire.kit.rank3`  
**Default:** Operators only

## Description

Admin command for managing vampire levels. Allows rank3 administrators to increase, decrease, or set a player's vampire level. This command provides more flexible level management than the basic `/vampire set level` command.

## Usage

```
/vampire level <operation> [amount] [player]
/v level <operation> [amount] [player]
```

### Arguments

- **`operation`** (required) - The operation to perform: `increase`, `decrease`, or `set`
- **`amount`** (optional) - The amount to change (defaults to 1 for increase/decrease, required for set)
- **`player`** (optional) - Target player name (defaults to yourself if you're a player)

## Operations

### Increase Level
```
/vampire level increase [amount] [player]
```

Increases a player's vampire level by the specified amount.

**Examples:**
- `/vampire level increase` - Increase your own level by 1
- `/vampire level increase 2` - Increase your own level by 2
- `/vampire level increase 1 PlayerName` - Increase PlayerName's level by 1

**Default amount:** 1 (if not specified)

### Decrease Level
```
/vampire level decrease [amount] [player]
```

Decreases a player's vampire level by the specified amount.

**Examples:**
- `/vampire level decrease` - Decrease your own level by 1
- `/vampire level decrease 2` - Decrease your own level by 2
- `/vampire level decrease 1 PlayerName` - Decrease PlayerName's level by 1

**Default amount:** 1 (if not specified)

### Set Level
```
/vampire level set <amount> [player]
```

Sets a player's vampire level to a specific value.

**Examples:**
- `/vampire level set 3` - Set your own level to 3
- `/vampire level set 5 PlayerName` - Set PlayerName's level to 5

**Note:** Amount is required for the `set` operation.

## Level Bounds

Levels are validated against the configuration in `levels.yml`:
- **Minimum level:** 0 (always valid)
- **Maximum level:** Highest level defined in `levels.yml` (typically 5, but configurable)

The command will reject operations that would result in levels outside these bounds.

## Examples

```
/vampire level increase
```
Increases your own vampire level by 1.

```
/vampire level increase 2 Steve
```
Increases Steve's vampire level by 2.

```
/vampire level decrease 1
```
Decreases your own vampire level by 1.

```
/vampire level set 3 Alex
```
Sets Alex's vampire level to 3.

```
/vampire level set 0
```
Sets your own vampire level to 0 (lowest level).

## Permissions

- **`vampire.kit.rank3`** - Required to use the level command (default: op)
  - This permission also grants access to the kit command and other rank3 admin features

## Tab Completion

Tab completion provides suggestions for:
1. **Operation argument:** `increase`, `decrease`, `set`
2. **Amount argument (for set):** Level numbers from 0 to maximum configured level
3. **Amount argument (for increase/decrease):** Any positive number, or player names
4. **Player argument:** Online player names

## Error Messages

- **"Invalid operation: <operation>. Use 'increase', 'decrease', or 'set'"** - Invalid operation specified
- **"Invalid amount: <amount>. Amount must be a positive number"** - Invalid or negative amount
- **"Cannot set <player>'s level below minimum (<min>)"** - Level would be below 0
- **"Cannot set <player>'s level above maximum (<max>)"** - Level would exceed maximum configured level
- **"<player> is not a vampire"** - Target player is not a vampire (levels only apply to vampires)
- **"Could not find vampire data for player: <player>"** - Player data not found
- **"You must be a player or specify a player name"** - Console must specify a player
- **"Player not found: <name>"** - Player has never joined the server
- **"Failed to change <player>'s level"** - General failure message

## Success Messages

- **"Increased <player>'s level by <amount> (from <old> to <new>)"** - Level increased successfully
- **"Decreased <player>'s level by <amount> (from <old> to <new>)"** - Level decreased successfully
- **"Set <player>'s level to <level>"** - Level set successfully

## Notes

- The target player must be a vampire for level changes to work
- Level changes are saved to the database immediately
- Level affects maximum blood capacity and other level-based features (see `levels.yml`)
- The command works on both online and offline players (if they've joined before)
- Level bounds are enforced based on the configuration in `levels.yml`
- Using `increase` or `decrease` with an amount that would exceed bounds will fail with an appropriate error message

## Level Effects

Vampire levels affect various gameplay aspects:
- **Maximum Blood:** Higher levels typically have higher max blood capacity
- **Blood Regeneration:** Some levels have regeneration rate multipliers
- **Sun Damage:** Some levels have sun damage modifiers
- **Speed Boost:** Some levels provide speed multipliers
- **Abilities:** Some levels unlock abilities like shriek
- **Cooldowns:** Some levels reduce ability cooldowns

See `levels.yml` for the complete configuration of level effects.

## Use Cases

- Testing different level configurations
- Adjusting player levels for roleplay scenarios
- Correcting level assignments
- Managing level progression in custom scenarios
- Providing level adjustments as rewards or punishments

## See Also

- [Set Command](set.md) - Alternative way to set levels directly
- [Configuration Guide](../CONFIG.md) - Plugin configuration options
- [Levels Configuration](../levels.yml) - Level definitions and effects

