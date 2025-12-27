# Debug Command

**Command:** `/vampire debug`  
**Alias:** `/v debug`  
**Permission:** `vampire.base` (inherited)  
**Default:** All players (if they have base permission)

## Description

Toggles debug mode for the plugin. When enabled, the plugin logs additional debug information to the server console, which can be helpful for troubleshooting issues.

## Usage

```
/vampire debug
/v debug
```

## Behavior

The debug command toggles the debug state:
- If debug is currently **off**, running the command turns it **on**
- If debug is currently **on**, running the command turns it **off**

## Examples

```
/vampire debug
```
Toggles debug mode on or off.

## Output

When toggling debug mode, you receive a message indicating the new state:
- **"Debug mode set to: On"** - Debug mode enabled
- **"Debug mode is already On"** - Debug was already on
- **"Debug mode set to: Off"** - Debug mode disabled
- **"Debug mode is already Off"** - Debug was already off

## Permissions

- **`vampire.base`** - Required to use the debug command (default: true)

## Tab Completion

This command has no arguments, so no tab completion is provided.

## Debug Information

When debug mode is enabled, the plugin logs additional information including:
- Player state changes
- Blood level changes
- Infection progress
- Mode activations/deactivations
- Database operations
- Task executions
- Error details

All debug messages are prefixed with `[DEBUG]` in the console.

## Notes

- Debug mode affects console logging only, not in-game messages
- Debug logging can generate a lot of output - use with caution on production servers
- Debug state is stored in the configuration file
- The debug state persists across server restarts
- Debug information helps developers and administrators troubleshoot issues

## Configuration

Debug mode can also be toggled directly in the configuration file:
```yaml
debug: true  # or false
```

## See Also

- [Configuration Guide](../CONFIG.md) - Configuration options including debug settings
- [README](../README.md) - General plugin information

