# Info Command

**Command:** `/vampire info`  
**Alias:** `/v info`  
**Permission:** `vampire.info`  
**Default:** All players

## Description

Displays general information about the Vampire plugin, including version, authors, website, and runtime status.

## Usage

```
/vampire info
/v info
```

## Output

The command displays:
- Plugin version
- Authors
- Website URL
- Debug mode status (On/Off)
- Registered altars (if altars are enabled)

### Example Output

```
=== Vampire Info ===
Version: 3.1.20
Authors: MassiveCraft, Clockworx
Website: https://github.com/clockworx/vampire
Runtime Debug Mode: Off

=== Registered Altars ===
- Dark Altar: Requires DIAMOND: 1, OBSIDIAN: 4
- Light Altar: Requires GOLD_INGOT: 2, LAPIS_LAZULI: 1
```

## Permissions

- **`vampire.info`** - Required to view plugin information (default: true)

## Tab Completion

This command has no arguments, so no tab completion is provided.

## Notes

- The debug status shows whether debug logging is currently enabled
- Altar information is only shown if altars are enabled in the configuration
- All information is read from the plugin's metadata and configuration files

