# Offer Gift Command

**Command:** `/vampire offergift <player>`  
**Alias:** `/v offergift <player>`  
**Permission:** `vampire.gift.offer`  
**Default:** Operators only  
**Player Only:** Yes

## Description

Allows a vampire to offer the Dark Gift to a human player, which can transform them into a vampire. This is an admin feature for roleplay and controlled transformations.

## Usage

```
/vampire offergift <player>
/v offergift <player>
```

### Arguments

- **`player`** (required) - The name of the human player to offer the gift to

## Requirements

- You must be a vampire
- The target must be a human (not already a vampire or infected)
- The target must be online
- The target must be within range (configurable)
- You must have enough blood (configurable amount)
- The Dark Gift feature must be enabled in configuration
- The target must not already have a pending offer

## Examples

```
/vampire offergift Steve
```
Offers the Dark Gift to Steve.

```
/v offergift Alex
```
Offers the Dark Gift to Alex (using alias).

## Process

1. Check if sender is a vampire
2. Check if target is online and human
3. Check if target is within range
4. Check if sender has enough blood
5. Check if target already has a pending offer
6. Create the gift offer
7. Notify both players

## Permissions

- **`vampire.gift.offer`** - Required to offer the Dark Gift (default: op)

## Tab Completion

Tab completion suggests online player names when typing the player argument.

## Error Messages

- **"The Dark Gift feature is currently disabled"** - Feature is disabled in config
- **"You cannot offer the Dark Gift to yourself"** - Cannot target yourself
- **"Only vampires can offer the Dark Gift"** - You must be a vampire
- **"Player is not a suitable human target (already vampire or infected)"** - Target is not human
- **"Player already has a pending Dark Gift offer"** - Target has existing offer
- **"You are too far away from player"** - Target is out of range
- **"You don't have enough blood (X required)"** - Insufficient blood

## Success Messages

- **"You have offered the Dark Gift to <player>"** - Sent to sender
- **"<player> has offered you the Dark Gift! Type /vampire acceptgift or /vampire rejectgift"** - Sent to target

## Notes

- The offer expires after a configurable timeout
- Only one active offer can exist per target player
- The target must accept or reject the offer before a new one can be made
- Blood is consumed when the offer is accepted, not when it's made
- The command requires the sender to be a vampire
- Range checking ensures players are close enough for the offer

## See Also

- [Accept Gift Command](acceptgift.md) - Accept a Dark Gift offer
- [Reject Gift Command](rejectgift.md) - Reject a Dark Gift offer
- [Configuration Guide](../CONFIG.md) - Dark Gift configuration options

