# Getting Started

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
