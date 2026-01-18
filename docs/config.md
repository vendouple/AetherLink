# config.json Reference

The main configuration file is located at `config/AetherLink/config.json`.

---

## Full Example

```json
{
  "botToken": "YOUR_BOT_TOKEN_HERE",
  "channelConfigs": [
    {
      "channelId": "123456789012345678",
      "readOnly": false,
      "syncWithOtherChannels": true,
      "enabled": true
    },
    {
      "channelId": "987654321098765432",
      "readOnly": true,
      "syncWithOtherChannels": false,
      "enabled": true
    }
  ],
  "syncEnabled": false,
  "linking": {
    "enabled": true,
    "codeExpirationSeconds": 300
  },
  "spamControl": {
    "discordCooldownSeconds": 2,
    "hytaleAggregateSeconds": 2
  }
}
```

---

## Options

### `botToken`
**Type:** `string`  
**Default:** `"YOUR_BOT_TOKEN_HERE"`

Your Discord bot token. Get one from the [Discord Developer Portal](https://discord.com/developers/applications).

!!! warning
    Never share your bot token publicly. If exposed, regenerate it immediately.

---

### `channelConfigs`
**Type:** `array`

A list of Discord channels the bot will use. Each channel has its own settings.

#### Channel Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `channelId` | string | `""` | The Discord channel ID |
| `readOnly` | boolean | `false` | If `true`, messages from this channel are **not** sent to Hytale. Hytale messages still appear in this channel. |
| `syncWithOtherChannels` | boolean | `true` | If `true` and `syncEnabled` is `true`, messages from this channel are forwarded to other synced channels. |
| `enabled` | boolean | `true` | If `false`, the channel is completely ignored. |

**How to get a Channel ID:**

1. Enable Developer Mode in Discord (Settings → Advanced → Developer Mode)
2. Right-click the channel → Copy Channel ID

---

### `syncEnabled`
**Type:** `boolean`  
**Default:** `false`

When `true`, messages sent in one Discord channel are forwarded to all other channels that have `syncWithOtherChannels: true`.

!!! note
    You need at least 2 channels configured for sync to work.

---

### `linking`
**Type:** `object`

Settings for Discord-to-Hytale account linking.

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `enabled` | boolean | `true` | Enable/disable the linking feature |
| `codeExpirationSeconds` | integer | `300` | How long a linking code is valid (in seconds) |

!!! info
    The linking feature is **not yet implemented**. These settings are reserved for future use.

---

### `spamControl`
**Type:** `object`

Settings to prevent spam and rate-limit messages.

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `discordCooldownSeconds` | integer | `2` | Sets Discord channel slowmode (seconds between messages per user) |
| `hytaleAggregateSeconds` | integer | `2` | Groups duplicate Hytale events (e.g., rapid join/leave) within this window |

#### How Spam Control Works

**Discord Slowmode:**  
The bot automatically sets the configured slowmode on all enabled channels. Requires `Manage Channel` permission.

**Hytale Aggregation:**  
If the same event (e.g., player join) occurs multiple times within the window, it's sent to Discord as a single message like:  
`→ PlayerName joined the server. (x3)`

---

## Default Config

When you first run AetherLink, it creates this default config:

```json
{
  "botToken": "YOUR_BOT_TOKEN_HERE",
  "channelConfigs": [],
  "syncEnabled": false,
  "linking": {
    "enabled": true,
    "codeExpirationSeconds": 300
  },
  "spamControl": {
    "discordCooldownSeconds": 2,
    "hytaleAggregateSeconds": 2
  }
}
```

You **must** set `botToken` and add at least one channel to `channelConfigs` for the plugin to work.
