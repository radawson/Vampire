# Reject Gift Command

**Command:** `/vampire rejectgift`  
**Alias:** `/v rejectgift`  
**Permission:** `vampire.gift.accept` (same as accept)  
**Default:** All players  
**Player Only:** Yes

## Description

Allows a human player to reject a pending Dark Gift offer from a vampire. This declines the transformation and clears the offer.

## Usage

```
/vampire rejectgift
/v rejectgift
```

## Requirements

- You must have a pending Dark Gift offer
- The offer must not have expired (though expired offers can still be rejected to clear them)

## Examples

```
/vampire rejectgift
```
Rejects a pending Dark Gift offer.

## Process

1. Check if player has a pending offer
2. Clear the offer
3. Notify both players

## Permissions

- **`vampire.gift.accept`** - Required to reject Dark Gift offers (default: true)
  - Note: Uses the same permission as accepting, as it's part of the gift interaction system

## Tab Completion

This command has no arguments, so no tab completion is provided.

## Error Messages

- **"You have no pending Dark Gift offer to accept or reject"** - No active offer

## Success Messages

- **"You have rejected the Dark Gift offer"** - Sent to rejector
- **"Player has rejected your Dark Gift offer"** - Sent to sender

## Notes

- Rejecting an offer immediately clears it
- The sender is notified of the rejection
- No blood is consumed when an offer is rejected
- Rejected offers cannot be re-accepted - a new offer must be made
- The command can only be used by players (not console)
- Expired offers can be rejected to clear them from the system

## Use Cases

- Player decides they don't want to become a vampire
- Player wants to decline a roleplay offer
- Clearing expired offers from the system

## See Also

- [Offer Gift Command](offergift.md) - Offer the Dark Gift to a player
- [Accept Gift Command](acceptgift.md) - Accept a Dark Gift offer
- [Configuration Guide](../CONFIG.md) - Dark Gift configuration options

