# Reload Command

**Command:** `/vampire reload`  
**Alias:** `/v reload`  
**Permission:** `vampire.config`  
**Default:** Operators only

## Description

Reloads the plugin's configuration and language files without requiring a server restart. Useful for applying configuration changes immediately.

## Usage

```
/vampire reload
/v reload
```

## What Gets Reloaded

When executed, the command reloads:
- Main configuration file (`config.yml`)
- Language files (based on configured language)
- All cached configuration values
- Message cache

## Examples

```
/vampire reload
```
Reloads all configuration files.

## Permissions

- **`vampire.config`** - Required to reload configuration (default: op)

## Tab Completion

This command has no arguments, so no tab completion is provided.

## Error Messages

- **"Failed to reload configuration. Check console for errors"** - An error occurred during reload

## Success Messages

- **"Configuration and language files successfully reloaded!"** - Reload completed successfully

## Notes

- The reload process may take a moment depending on file size
- Any errors during reload are logged to the server console
- Configuration changes take effect immediately after a successful reload
- Language file changes are applied to all new messages
- Some settings may require a server restart to fully take effect (check configuration documentation)

## Console Logging

When the reload command is executed, the server console logs:
- "Configuration and language files successfully reloaded!" (on success)
- Detailed error messages (on failure)

The console also logs who executed the reload command:
```
Configuration and language files reloaded by <player name>
```

## Best Practices

- Always check the console for errors after reloading
- Test configuration changes in a safe environment first
- Some configuration changes (like database settings) may not take effect until restart
- Backup your configuration files before making major changes

## See Also

- [Configuration Guide](../CONFIG.md) - Detailed configuration options
- [README](../README.md) - General plugin information

