# Mode Commands

**Command:** `/vampire mode <type>` or `/vampire <mode>` (shortcut)  
**Alias:** `/v mode <type>` or `/v <mode>`  
**Permission:** `vampire.mode.*` (varies by mode)  
**Default:** All players  
**Player Only:** Yes

## Description

Vampires can toggle various modes that affect their gameplay. Modes can be accessed either through the main mode command or via shortcuts.

## Available Modes

### Bloodlust Mode
- **Command:** `/vampire mode bloodlust` or `/vampire bloodlust`
- **Permission:** `vampire.mode.bloodlust`
- **Description:** Increases combat effectiveness but drains blood faster

### Intent Mode
- **Command:** `/vampire mode intent` or `/vampire intent`
- **Permission:** `vampire.mode.intent`
- **Description:** Allows vampires to infect humans during combat

### Night Vision Mode
- **Command:** `/vampire mode nightvision` or `/vampire nightvision`
- **Permission:** `vampire.mode.nightvision`
- **Description:** Provides permanent night vision (if enabled globally)

## Usage

### Using the Mode Command
```
/vampire mode <type>
/v mode <type>
```

### Using Shortcuts
```
/vampire bloodlust
/vampire intent
/vampire nightvision
```

## Examples

```
/vampire mode bloodlust
```
Toggles bloodlust mode on or off.

```
/vampire intent
```
Toggles intent mode (shortcut form).

```
/vampire nightvision
```
Toggles night vision mode (shortcut form).

## Mode Details

### Bloodlust Mode

When enabled:
- Increases combat damage
- Applies Speed and Strength potion effects
- Drains blood at an increased rate
- Automatically activates when blood drops below a threshold
- Automatically deactivates when blood rises above the threshold

**Effects:**
- Speed effect (level 1)
- Strength effect (level 1)
- Increased blood drain rate

### Intent Mode

When enabled:
- Allows vampires to infect humans during combat
- Attacks on humans have a chance to increase infection level
- Costs blood to infect players
- Must be manually toggled

**Effects:**
- Enables infection on attack
- Each infection attempt costs blood

### Night Vision Mode

When enabled:
- Provides permanent night vision effect
- Only works during nighttime (in-game)
- Automatically activates at night if enabled
- Automatically deactivates during day if enabled
- Can be globally disabled by server configuration

**Effects:**
- Night Vision potion effect (level configurable)
- Only active during nighttime hours

## Permissions

- **`vampire.mode.bloodlust`** - Toggle bloodlust mode (default: true)
- **`vampire.mode.intent`** - Toggle intent mode (default: true)
- **`vampire.mode.nightvision`** - Toggle night vision mode (default: true)

## Tab Completion

When using `/vampire mode`, tab completion suggests available mode types:
- `bloodlust`
- `intent`
- `nightvision`

## Error Messages

- **"This command can only be used by players"** - Console cannot use this command
- **"You are not a vampire"** - You must be a vampire to use mode commands
- **"Unknown mode type: <type>"** - Invalid mode type specified
- **"Night vision is globally disabled by the server"** - Night vision is disabled in config

## Notes

- All mode commands require you to be a vampire
- Modes can be toggled on and off at any time
- Some modes have automatic activation/deactivation based on conditions
- Mode states persist across server restarts
- Night vision mode respects server configuration settings

## Configuration

Mode behavior can be configured in the main configuration file:
- Bloodlust threshold and drain rate
- Intent infection chance and blood cost
- Night vision level and global enable/disable

See [CONFIG.md](../CONFIG.md) for configuration details.

