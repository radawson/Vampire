# Shriek Command

**Command:** `/vampire shriek`  
**Alias:** `/v shriek`  
**Permission:** `vampire.shriek`  
**Default:** All players  
**Player Only:** Yes

## Description

Allows vampires to perform a terrifying shriek that can be heard by other players. The shriek has a cooldown period to prevent spam.

## Usage

```
/vampire shriek
/v shriek
```

## Requirements

- You must be a vampire
- You must not be on cooldown (cooldown is configurable)

## Examples

```
/vampire shriek
```
Performs a shriek if you're a vampire and not on cooldown.

## Effects

When executed successfully:
- A shriek sound/effect is played
- Other players in the area can hear the shriek
- A success message is sent to the shrieking player

## Cooldown

The shriek command has a cooldown period to prevent abuse. If you try to shriek while on cooldown:
- The command fails
- You receive a message indicating how many seconds remain until you can shriek again

## Permissions

- **`vampire.shriek`** - Required to use the shriek command (default: true)

## Tab Completion

This command has no arguments, so no tab completion is provided.

## Error Messages

- **"This command can only be used by players"** - Console cannot use this command
- **"You are not a vampire"** - You must be a vampire to use this command
- **"You must wait X seconds before shrieking again"** - You're on cooldown

## Notes

- The shriek cooldown is configurable in the plugin configuration
- The shriek effect and sound are handled by the plugin's effect system
- Only vampires can use this command
- The cooldown is tracked per-player

## Configuration

The shriek cooldown can be configured in the main configuration file. See [CONFIG.md](../CONFIG.md) for details.

