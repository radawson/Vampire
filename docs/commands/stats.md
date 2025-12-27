# Stats Command

**Command:** `/vampire stats [player]`  
**Alias:** `/v stats [player]`  
**Permission:** `vampire.stats` (self), `vampire.stats.other` (others)  
**Default:** All players can view their own stats, operators can view others

## Description

Displays vampire statistics for yourself or another player. Shows different information depending on whether the target is a vampire or infected human.

## Usage

### View Own Statistics
```
/vampire stats
/v stats
```

Shows your own vampire statistics.

### View Other Player's Statistics
```
/vampire stats <player>
/v stats <player>
```

Shows another player's vampire statistics. Requires the `vampire.stats.other` permission.

## Examples

```
/vampire stats
```
Shows your own statistics.

```
/vampire stats Steve
```
Shows Steve's statistics (if you have permission).

## Output Format

The output varies based on whether the target is a vampire or infected human:

### For Vampires
- Vampire status (Yes/No)
- Current blood level and maximum
- Vampire level
- Mode statuses (Bloodlust, Intent, Night Vision)

### For Infected Humans
- Vampire status (No)
- Infection level (as percentage)
- Infection reason (if available)

### Example Output (Vampire)

```
=== Vampire Status [Steve] ===
Vampire: Yes
Blood: 18.50/20.00
Level: 2
 - Bloodlust: Active
 - Intent: Inactive
 - Night Vision: Active
```

### Example Output (Infected Human)

```
=== Vampire Status [Alex] ===
Vampire: No
Infection: 45.0%
(Reason: Bitten by Steve)
```

## Permissions

- **`vampire.stats`** - Required to view your own statistics (default: true)
- **`vampire.stats.other`** - Required to view other players' statistics (default: op)

## Tab Completion

When viewing another player's statistics, tab completion suggests online player names.

## Error Messages

- **"Player is not online"** - The specified player is not currently online
- **"You must be a player or specify a player name"** - Console cannot use this command without specifying a player
- **"Player data not found"** - The player's vampire data could not be loaded

## Notes

- Works for both vampires and infected humans
- Infection level is displayed as a percentage (0-100%)
- Infection reason is only shown if available
- Mode statuses are only displayed for vampires
- Maximum blood is calculated based on the player's vampire level

