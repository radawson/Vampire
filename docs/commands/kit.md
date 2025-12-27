# Kit Command

**Command:** `/vampire kit <altar_type>`  
**Alias:** `/v kit <altar_type>`  
**Permission:** `vampire.kit.rank3`  
**Default:** Operators only

## Description

Admin command that creates a chest containing all required materials for constructing altars. This command is useful for administrators who want to provide players with altar construction kits for testing, events, or gameplay purposes.

## Usage

```
/vampire kit <altar_type>
/v kit <altar_type>
```

### Arguments

- **`altar_type`** (required) - The type of altar kit to create: `dark`, `light`, or `all`

## Altar Types

### Dark Altar Kit
```
/vampire kit dark
```

Creates a chest containing all materials needed to construct a Dark Altar:
- Core block (1x)
- All structure materials with their required counts
- All resource items needed for activation

### Light Altar Kit
```
/vampire kit light
```

Creates a chest containing all materials needed to construct a Light Altar:
- Core block (1x)
- All structure materials with their required counts
- All resource items needed for activation

### All Altars Kit
```
/vampire kit all
```

Creates a chest containing materials for all registered altars. Useful for providing complete altar construction kits.

## Examples

```
/vampire kit dark
```
Creates a chest with Dark Altar materials at your location.

```
/v kit light
```
Creates a chest with Light Altar materials (using alias).

```
/vampire kit all
```
Creates a chest containing materials for all altars.

## Chest Placement

The chest is placed at the following locations (in order of preference):
1. Your current location (if the block is air)
2. Adjacent blocks (east, west, south, north, or above)
3. If no suitable location is found, an error message is displayed

The chest is automatically filled with all required materials and can be opened immediately.

## Permissions

- **`vampire.kit.rank3`** - Required to use the kit command (default: op)
  - This permission also grants access to the level command and other rank3 admin features

## Tab Completion

Tab completion provides suggestions for:
- **Altar type argument:** `dark`, `light`, `all`

## Error Messages

- **"Invalid altar type: <type>. Use 'dark', 'light', or 'all'"** - Invalid altar type specified
- **"No altars are currently registered"** - No altars are configured or enabled
- **"Could not find a suitable location to place the chest. Please ensure there's space nearby"** - No valid location for chest placement
- **"This command can only be used by players!"** - Command was run from console (requires player location)

## Success Messages

- **"Altar construction kit created for <altar_name>!"** - Single altar kit created successfully
- **"Altar construction kits created for <count> altars!"** - Multiple altar kits created (when using `all`)

## Kit Contents

Each altar kit includes:

### Structure Materials
- **Core Block:** The block players interact with (e.g., OBSIDIAN for Dark Altar, DIAMOND_BLOCK for Light Altar)
- **Structure Materials:** All blocks required for the altar structure, with their exact required counts

### Resource Items
- **Activation Resources:** All items consumed when using the altar (e.g., REDSTONE, WITHER_ROSE for Dark Altar)

The exact contents depend on the altar configuration in `config.yml` under the `altar.dark` and `altar.light` sections.

## Notes

- The command requires you to be a player (cannot be run from console)
- The chest is placed in your world at your location or nearby
- All materials are placed in a single chest (may require multiple chests if materials exceed 27 slots)
- Materials are provided in exact quantities as configured
- The command respects altar configuration - if altars are disabled, the command will report no altars available
- Chest placement respects world boundaries and block placement rules

## Use Cases

- Providing altar construction materials for events
- Testing altar configurations
- Giving players access to altar materials for gameplay
- Creating altar construction tutorials
- Admin assistance for players building altars

## Configuration

Altar materials are configured in `config.yml`:

```yaml
altar:
  dark:
    core-material: "OBSIDIAN"
    materials:
      OBSIDIAN: 4
      WITHER_ROSE: 1
      DIAMOND_BLOCK: 2
    resources:
      - "REDSTONE:10"
      - "WITHER_ROSE:1"
  light:
    core-material: "DIAMOND_BLOCK"
    materials:
      GOLD_BLOCK: 4
      GLOWSTONE: 1
      EMERALD_BLOCK: 2
    resources:
      - "LAPIS_LAZULI:10"
      - "DIAMOND:1"
```

See [Configuration Guide](../CONFIG.md) for more details on altar configuration.

## See Also

- [Level Command](level.md) - Manage vampire levels (same permission)
- [Configuration Guide](../CONFIG.md) - Plugin configuration options
- [Altar System](../CONFIG.md#altar-configuration) - Altar configuration details

