# List Command

**Command:** `/vampire list [page]`  
**Alias:** `/v list [page]`  
**Permission:** `vampire.list`  
**Default:** Operators only

## Description

Lists all online vampires and infected players, organized by type. Supports pagination for servers with many players.

## Usage

```
/vampire list [page]
/v list [page]
```

### Arguments

- **`page`** (optional) - The page number to display (default: 1)

## Examples

```
/vampire list
```
Shows the first page of online vampires and infected players.

```
/vampire list 2
```
Shows the second page of the list.

## Output Format

The list is organized into two sections:
1. **Vampires** - Players who are currently vampires
2. **Infected** - Players who are infected but not yet vampires (shows infection percentage)

### Example Output

```
=== Vampire List (Page 1/2) ===
Vampires (Online):
  - Notch
  - Steve
Infected (Online):
  - Alex (45%)
  - Bob (12%)

/vampire list 2 (Next)
```

## Pagination

- Each page displays up to 10 entries
- Page numbers are shown at the top (e.g., "Page 1/3")
- Navigation hints are shown at the bottom if multiple pages exist
- Invalid page numbers default to the last available page

## Permissions

- **`vampire.list`** - Required to view the list (default: op)

## Tab Completion

Tab completion suggests page numbers (1, 2, 3, etc.) when typing the page argument.

## Error Messages

- **"Invalid page number"** - The provided page number is not a valid integer
- **"No vampires found"** - There are no online vampires or infected players

## Notes

- Only shows players who are currently online
- Infected players show their infection percentage in parentheses
- The list is sorted alphabetically by player name
- Empty sections (no vampires or no infected) are omitted from the output
- The list is generated from cached player data, so it reflects the current server state

