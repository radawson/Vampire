# Flask Command

**Command:** `/vampire flask [type]`  
**Alias:** `/v flask [type]`  
**Permission:** `vampire.flask` (blood), `vampire.flask.holywater` (holy)  
**Default:** All players (blood), operators (holy)  
**Player Only:** Yes

## Description

Allows vampires to create Blood Vials or allows operators to create Holy Water. Blood Vials can be used to store and transfer blood, while Holy Water can cure infections.

## Usage

### Create Blood Vial
```
/vampire flask
/vampire flask blood
/v flask blood
```

### Create Holy Water
```
/vampire flask holy
/v flask holy
```

## Requirements

### Blood Vial
- You must be a vampire
- You must be holding an empty glass bottle
- You must have at least 1.0 blood
- You must have the `vampire.flask` permission

### Holy Water
- You must have the `vampire.flask.holywater` permission (default: op)
- No additional requirements (no blood cost, no bottle needed)

## Examples

```
/vampire flask
```
Creates a blood vial (default type).

```
/vampire flask blood
```
Explicitly creates a blood vial.

```
/vampire flask holy
```
Creates holy water (requires admin permission).

## Blood Vial Creation Process

1. Check if player is a vampire
2. Check if player is holding an empty glass bottle
3. Check if player has enough blood (1.0 by default)
4. Consume 1 glass bottle from inventory
5. Consume 1.0 blood
6. Add Blood Vial to inventory (or drop if inventory is full)

## Holy Water Creation Process

1. Check if player has permission
2. Create Holy Water item
3. Add to inventory (or drop if inventory is full)

## Item Details

### Blood Vial
- **Name:** Blood Vial
- **Lore:** "A vial containing potent vampire blood. Use with caution..."
- **Use:** Can be consumed to restore blood
- **Cost:** 1.0 blood + 1 glass bottle

### Holy Water
- **Name:** Holy Water
- **Lore:** "Blessed water, harmful to the undead. May cure infection."
- **Use:** Can cure infections when used
- **Cost:** None (admin command)

## Permissions

- **`vampire.flask`** - Create blood vials (default: true)
- **`vampire.flask.holywater`** - Create holy water (default: op)

## Tab Completion

Tab completion suggests:
- `blood` - If you have `vampire.flask` permission
- `holy` - If you have `vampire.flask.holywater` permission

## Error Messages

- **"This command can only be used by players"** - Console cannot use this command
- **"Only vampires can create blood vials"** - You must be a vampire to create blood vials
- **"You must be holding an empty glass bottle"** - Need a glass bottle in hand
- **"You need at least X blood to create a vial"** - Insufficient blood
- **"Failed to draw blood for the vial"** - Internal error during blood consumption
- **"Inventory full! Blood vial dropped nearby"** - Item dropped on ground
- **"Invalid vial type. Use 'blood' or 'holy'"** - Invalid type specified

## Notes

- Blood vial creation costs 1.0 blood by default (configurable)
- If inventory is full, the item is dropped at the player's location
- Blood vials can be used to transfer blood between players
- Holy water is an admin tool for curing infections
- The blood cost for vials may be configurable in future versions

## Configuration

The blood cost for creating vials may be configurable. See [CONFIG.md](../CONFIG.md) for details.

