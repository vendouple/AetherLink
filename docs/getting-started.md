# Getting Started

This guide will help you download and install AetherLink on your Hytale server.

---

## Requirements

- A Hytale dedicated server
- A Discord bot token ([Create one here](https://discord.com/developers/applications))
- Java 21 or newer

---

## Download

### Option 1: Download from curseforge (Recommended)

Download the latest stable build from [CurseForge](https://legacy.curseforge.com/hytale/mods/aetherlink/files)

### Option 2: Nightly Builds

Download the latest nightly build from GitHub Releases:

**[Download Latest Nightly](https://github.com/vendouple/AetherLink/releases)**

Look for releases tagged `nightly-YYYYMMDD`.

### Option 3: Build from Source

See [Build It Yourself](build.md) for instructions on compiling the plugin yourself.

---

## Installation

1. **Download** `AetherLink-1.0.0-beta.1.jar` (or the latest version)

2. **Copy** the JAR file to your Hytale server's `plugins/` folder

3. **Start** your server once to generate the config files

4. **Stop** the server

5. **Edit** `config/AetherLink/config.json`:
    - Set your Discord bot token
    - Add your Discord channel ID(s)

6. **Start** the server again

---

## Discord Bot Setup

1. Go to the [Discord Developer Portal](https://discord.com/developers/applications)

2. Click **New Application** and give it a name

3. Go to **Bot** -> **Add Bot**

4. Under **Privileged Gateway Intents**, enable:
    - **Message Content Intent** (required for reading messages)

5. Copy the **Token** and paste it into your `config.json`

6. Go to **OAuth2** -> **URL Generator**:
    - Select scopes: `bot`
    - Select permissions: `Send Messages`, `Read Message History`, `View Channels`, `Manage Channels` (for slowmode/topic)

7. Use the generated URL to invite the bot to your server

---

## Verify It Works

1. Start your Hytale server
2. Check the console for: `AetherLink events registered!`
3. Send a message in your configured Discord channel
4. The message should appear in Hytale chat

---

## Next Steps

- [Configure your channels](config.md)
- [Customize messages](messages.md)
- [Troubleshooting](troubleshooting.md) if something isn't working
