# messages.json Reference

Customize all messages sent by AetherLink. Located at `config/AetherLink/messages.json`.

---

## Full Example

```json
{
  "chat": {
    "hytaleToDiscord": "**{HytalePlayer}**: {Message}",
    "discordToHytale": "[Discord] [{TopDiscordRoleInitials1}] {DiscordName}: {Message}",
    "discordToDiscord": "**[{SourceChannel}]** {DiscordName}: {Message}",
    "serverStart": ":white_check_mark: **Server has started!**",
    "serverStop": ":octagonal_sign: **Server has stopped.**",
    "join": ":arrow_right: **{HytalePlayer}** joined the server.",
    "leave": ":arrow_left: **{HytalePlayer}** left the server.",
    "death": ":skull: **{HytalePlayer}** died. Reason: {DeathReason}",
    "advancement": ":trophy: **{HytalePlayer}** just unlocked **{ZoneUnlock}**!"
  },
  "linking": {
    "codeGenerated": "Your linking code is **{Code}**. Run `/link {Code}` in Hytale. Expires in 5 minutes.",
    "linkSuccess": "Successfully linked with **{HytalePlayer}**!",
    "alreadyLinked": "You are already linked to **{HytalePlayer}**.",
    "invalidCode": "Invalid or expired code."
  },
  "info": {
    "topicUpdater": "Online: {PlayerCount}/{MaxPlayers} | Uptime: {Uptime}",
    "presence": "Playing Hytale with {PlayerCount} players"
  }
}
```

---

## Chat Messages

Messages for in-game and Discord chat events.

### `hytaleToDiscord`

**Default:** `"**{HytalePlayer}**: {Message}"`

Sent to Discord when a player chats in Hytale.

| Placeholder | Description |
|-------------|-------------|
| `{HytalePlayer}` | The player's Hytale username |
| `{Message}` | The chat message content |

---

### `discordToHytale`

**Default:** `"[Discord] [{TopDiscordRoleInitials1}] {DiscordName}: {Message}"`

Sent to Hytale when someone chats in Discord.

| Placeholder | Description |
|-------------|-------------|
| `{DiscordName}` | The Discord user's display name |
| `{TopDiscordRoleInitials1}` | First letter of the user's highest role (e.g., "A" for "Admin") |
| `{Message}` | The chat message content |

---

### `discordToDiscord`

**Default:** `"**[{SourceChannel}]** {DiscordName}: {Message}"`

Used for cross-channel sync (when `syncEnabled: true`).

| Placeholder | Description |
|-------------|-------------|
| `{SourceChannel}` | Name of the channel where the message originated |
| `{DiscordName}` | The Discord user's display name |
| `{Message}` | The chat message content |

---

### `serverStart`

**Default:** `":white_check_mark: **Server has started!**"`

Sent to Discord when the Hytale server starts.

*No placeholders available.*

---

### `serverStop`

**Default:** `":octagonal_sign: **Server has stopped.**"`

Sent to Discord when the Hytale server stops.

*No placeholders available.*

---

### `join`

**Default:** `":arrow_right: **{HytalePlayer}** joined the server."`

Sent to Discord when a player joins.

| Placeholder | Description |
|-------------|-------------|
| `{HytalePlayer}` | The player's Hytale username |

---

### `leave`

**Default:** `":arrow_left: **{HytalePlayer}** left the server."`

Sent to Discord when a player leaves.

| Placeholder | Description |
|-------------|-------------|
| `{HytalePlayer}` | The player's Hytale username |

---

### `death`

**Default:** `":skull: **{HytalePlayer}** died. Reason: {DeathReason}"`

Sent to Discord when a player dies.

| Placeholder | Description |
|-------------|-------------|
| `{HytalePlayer}` | The player's Hytale username |
| `{DeathReason}` | The cause of death |

!!! info
    Death messages are **not yet implemented** (waiting for Hytale API).

---

### `advancement`

**Default:** `":trophy: **{HytalePlayer}** just unlocked **{ZoneUnlock}**!"`

Sent to Discord when a player unlocks something.

| Placeholder | Description |
|-------------|-------------|
| `{HytalePlayer}` | The player's Hytale username |
| `{ZoneUnlock}` | The zone or advancement unlocked |

!!! info
    Advancement messages are **not yet implemented** (waiting for Hytale API).

---

## Linking Messages

Messages for the Discord-Hytale account linking system.

!!! info
    The linking feature is **not yet implemented**. These settings are reserved for future use.

### `codeGenerated`

**Default:** `"Your linking code is **{Code}**. Run \`/link {Code}\` in Hytale. Expires in 5 minutes."`

| Placeholder | Description |
|-------------|-------------|
| `{Code}` | The generated linking code |

### `linkSuccess`

**Default:** `"Successfully linked with **{HytalePlayer}**!"`

| Placeholder | Description |
|-------------|-------------|
| `{HytalePlayer}` | The linked Hytale username |

### `alreadyLinked`

**Default:** `"You are already linked to **{HytalePlayer}**."`

| Placeholder | Description |
|-------------|-------------|
| `{HytalePlayer}` | The already-linked Hytale username |

### `invalidCode`

**Default:** `"Invalid or expired code."`

*No placeholders available.*

---

## Info Messages

Messages for bot presence and channel topics.

### `topicUpdater`

**Default:** `"Online: {PlayerCount}/{MaxPlayers} | Uptime: {Uptime}"`

Sets the Discord channel topic (updated every 60 seconds).

| Placeholder | Description |
|-------------|-------------|
| `{PlayerCount}` | Current number of online players |
| `{MaxPlayers}` | Maximum server capacity (currently shows "?") |
| `{Uptime}` | Server uptime (e.g., "02h 15m 30s") |

---

### `presence`

**Default:** `"Playing Hytale with {PlayerCount} players"`

The bot's Discord presence/status.

| Placeholder | Description |
|-------------|-------------|
| `{PlayerCount}` | Current number of online players |

---

## Using Discord Emoji

You can use Discord emoji codes in your messages, below are a few examples:

| Code | Emoji |
|------|-------|
| `:white_check_mark:` | ✅ |
| `:octagonal_sign:` | 🛑 |
| `:arrow_right:` | ➡️ |
| `:arrow_left:` | ⬅️ |
| `:skull:` | 💀 |
| `:trophy:` | 🏆 |

For custom server emoji, use the format: `<:emoji_name:emoji_id>`
