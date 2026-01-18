# Usage

## Spam Control

- `discordCooldownSeconds`: Uses Discord’s built-in slowmode to throttle Discord → Hytale.
- `hytaleAggregateSeconds`: Aggregates duplicate **Hytale → Discord** messages within the window. Example:
  - `Test, Test, Test` in 5 seconds → `Test (3)`
  - `Test, asd, Test, Test` → `Test`, `asd`, `Test (2)`

## Messages

`messages.json` controls output formatting. Placeholders:

- `{HytalePlayer}`
- `{DiscordName}`
- `{Message}`
- `{TopDiscordRoleInitials1}`
- `{SourceChannel}`
- `{Uptime}`
- `{PlayerCount}`
- `{MaxPlayers}`

## Commands

- `/aetherlink reload` — Reloads config and messages (5s cooldown).
- `/aetherlink retrynow` — Forces a Discord reconnect if offline.

## Presence + Channel Topic

- Presence uses: `messages.info.presence`
- Channel topic uses: `messages.info.topicUpdater`

> Max players is currently unknown and uses `?` until a server API is found.

## Sync Behavior

- If `syncEnabled` is **true** and 2+ channels are configured, messages sent in Discord Channel A are mirrored to Channel B (and vice versa).
- If Channel B is `readOnly`, messages sent in Channel B are **only** mirrored to other Discord channels and are **not** sent to Hytale.
- Hytale messages are posted to **all enabled channels**.

## Work in Progress / Known Limitations

- Zone/advancement events are not wired.
- Max player count is not wired.
- Discord reconnect uses backoff but does not persist retry state across server restarts.
- Hytale → Discord aggregation is limited to duplicate detection only.

## Troubleshooting

### Bot doesn’t post to a channel

- Ensure the channel ID is correct.
- The bot needs `VIEW_CHANNEL`, `MESSAGE_SEND`, and for slowmode updates `MANAGE_CHANNEL`.

### Discord → Hytale not working

- Check server logs for `[AetherLink] Failed to send message to Hytale chat.`
- Ensure the plugin is built against the same `HytaleServer.jar` as the server runtime.
