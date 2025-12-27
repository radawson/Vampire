# Accept Gift Command

**Command:** `/vampire acceptgift`  
**Alias:** `/v acceptgift`  
**Permission:** `vampire.gift.accept`  
**Default:** All players  
**Player Only:** Yes

## Description

Allows a human player to accept a pending Dark Gift offer from a vampire, transforming them into a vampire immediately.

## Usage

```
/vampire acceptgift
/v acceptgift
```

## Requirements

- You must be a human (not already a vampire)
- You must have a pending Dark Gift offer
- The offer must not have expired
- The sender must still be valid (online and a vampire)
- The sender must still have enough blood

## Examples

```
/vampire acceptgift
```
Accepts a pending Dark Gift offer.

## Process

1. Check if player has a pending offer
2. Check if offer has expired
3. Check if sender is still valid
4. Check if sender has enough blood
5. Transform the player into a vampire
6. Consume blood from the sender
7. Notify both players

## Permissions

- **`vampire.gift.accept`** - Required to accept Dark Gift offers (default: true)

## Tab Completion

This command has no arguments, so no tab completion is provided.

## Error Messages

- **"You have no pending Dark Gift offer to accept or reject"** - No active offer
- **"The Dark Gift offer has expired"** - Offer timed out
- **"The vampire who offered the gift is no longer valid (offline or cured)"** - Sender is invalid
- **"Sender no longer has enough blood to complete the transformation"** - Sender lacks blood
- **"Failed to accept the Dark Gift due to an unknown error"** - Internal error

## Success Messages

- **"You have accepted the Dark Gift! You feel a dark power awaken within..."** - Sent to acceptor
- **"Player has accepted your Dark Gift! They are now one of us."** - Sent to sender

## Transformation Effects

When a player accepts the Dark Gift:
- They are immediately transformed into a vampire
- Their infection level is cleared (if any)
- They receive initial vampire blood (configurable)
- They are set to vampire level 1 (or configured starting level)
- All vampire abilities become available

## Notes

- The transformation is immediate and permanent
- The sender's blood is consumed when the offer is accepted
- Only one offer can be active at a time per player
- Offers expire after a configurable timeout period
- The command can only be used by players (not console)

## See Also

- [Offer Gift Command](offergift.md) - Offer the Dark Gift to a player
- [Reject Gift Command](rejectgift.md) - Reject a Dark Gift offer
- [Configuration Guide](../CONFIG.md) - Dark Gift configuration options

