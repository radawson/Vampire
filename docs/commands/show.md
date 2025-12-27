# Show Command

**Command:** `/vampire show [player]`  
**Alias:** `/v show [player]`  
**Permission:** `vampire.show` (self), `vampire.show.other` (others)  
**Default:** All players can view their own status, operators can view others

## Description

Displays detailed status information about a vampire player, including blood levels, vampire level, active modes, and sun exposure.

## Usage

### View Own Status
```
/vampire show
/v show
```

Shows your own vampire status if you are a vampire.

### View Other Player's Status
```
/vampire show <player>
/v show <player>
```

Shows another player's vampire status. Requires the `vampire.show.other` permission.

## Examples

```
/vampire show
```
Shows your own vampire status.

```
/vampire show Notch
```
Shows Notch's vampire status (if you have permission).

## Output Format

The command displays:
- Player name
- Current blood level and maximum blood
- Vampire level and description
- Active modes (Bloodlust, Intent, Night Vision)
- Current sun exposure percentage

### Example Output

```
=== Vampire Details [Notch] ===
Blood: 15.5/20.0
Level: 3 (Experienced Hunter)
Modes:
  Bloodlust Mode: ACTIVE
  Infection Intent: INACTIVE
  Night Vision: ACTIVE
Sun Exposure: 0.0%
```

## Permissions

- **`vampire.show`** - Required to view your own vampire status (default: true)
- **`vampire.show.other`** - Required to view other players' vampire status (default: op)

## Tab Completion

When viewing another player's status, tab completion suggests online player names.

## Error Messages

- **"Player is not online"** - The specified player is not currently online
- **"You do not have permission to view other players' vampire status"** - Missing `vampire.show.other` permission
- **"Player data not found"** - The player's vampire data could not be loaded
- **"Player is not currently a vampire"** - The target player is not a vampire

## Notes

- Only works for players who are currently vampires
- Sun exposure is calculated in real-time based on the player's current location
- Blood maximum is determined by the player's vampire level
- Mode status reflects the current active state, not the configured toggle state

