# AetherLink — Getting Started

AetherLink bridges Hytale server events to Discord and relays Discord chat to Hytale. This guide covers setup, config, and known limitations.

## Requirements

- Java 21
- Hytale server with plugin support
- A Discord bot token

## Get the JAR

You can either download a prebuilt release or build it yourself.

### Option A — Download from Releases

Grab the latest release (or nightly) JAR from GitHub Releases and place it in your server’s plugins folder.

### Option B — Build locally

Use the Shadow JAR task to produce the plugin artifact:

- `./gradlew shadowJar`

The output JAR is created in `build/libs`.

## Install

1. Place the built JAR in your Hytale server plugins folder.
2. Start the server once to generate config files in `./config/AetherLink`.
3. Edit `config.json` and `messages.json`.
4. Restart the server or run `/aetherlink reload`.

## Configuration

`config.json` is generated on first run. Key fields:

### Bot Token

- `botToken`: Discord bot token.

### Channels

`channelConfigs` is the primary list for all channels.

```json
{
  "channelConfigs": [
    {
      "channelId": "123456789012345678",
      "readOnly": false,
      "syncWithOtherChannels": true,
      "enabled": true
    }
  ],
  "syncEnabled": false,
  "spamControl": {
    "discordCooldownSeconds": 5,
    "hytaleAggregateSeconds": 10
  }
}
```

- `enabled`: If false, the channel is ignored entirely.
- `readOnly`: If true, Discord → Hytale is blocked for that channel. Hytale → Discord still posts.
- `syncWithOtherChannels`: If true, channel participates in cross-channel sync.
- `syncEnabled`: Global switch for Discord ↔ Discord sync (requires 2+ channels).

### Spam Control

- `discordCooldownSeconds`: Uses Discord’s built-in slowmode to throttle Discord → Hytale.
- `hytaleAggregateSeconds`: Aggregates duplicate **Hytale → Discord** messages within the window. Example:
  - `Test, Test, Test` in 5 seconds → `Test (3)`
  - `Test, asd, Test, Test` → `Test`, `asd`, `Test (2)`

## Messages

`messages.json` controls the output formatting. Example placeholders:

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

## Work in Progress / Known Limitations

- Zone/advancement events are not wired.
- Max player count is not wired.
- Discord reconnect uses backoff, but does not persist retry state across server restarts.
- Hytale → Discord aggregation is limited to duplicate detection only.

## Troubleshooting

### Bot doesn’t post to a channel

- Ensure the channel ID is correct.
- The bot needs `VIEW_CHANNEL`, `MESSAGE_SEND`, and for slowmode updates `MANAGE_CHANNEL`.

### Discord → Hytale not working

- Check server logs for `[AetherLink] Failed to send message to Hytale chat.`
- Ensure the plugin is built against the same `HytaleServer.jar` as the server runtime.
