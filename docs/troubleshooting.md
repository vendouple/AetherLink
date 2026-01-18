# Troubleshooting

Common issues and how to fix them.

---

## Bot Not Connecting

### "PLEASE SET YOUR BOT TOKEN!"

**Cause:** The bot token is missing or still set to the default.

**Fix:**

1. Open `config/AetherLink/config.json`
2. Replace `"YOUR_BOT_TOKEN_HERE"` with your actual bot token
3. Restart the server

---

### Bot is online in Discord but not responding

**Possible causes:**

1. **Message Content Intent not enabled**
   - Go to [Discord Developer Portal](https://discord.com/developers/applications)
   - Select your bot → Bot → Privileged Gateway Intents
   - Enable **Message Content Intent**

2. **Wrong channel ID**
   - Verify the channel ID in `config.json` is correct
   - Make sure you copied the **channel** ID, not the server ID

3. **Bot not in the server**
   - Re-invite the bot using the OAuth2 URL

---

### "Channel not found or bot lacks access"

**Cause:** The bot cannot see the configured channel.

**Fix:**

1. Check the channel ID is correct
2. Ensure the bot has **View Channel** permission
3. If the channel is in a category, check category permissions too

---

## Messages Not Sending

### Discord → Hytale not working

**Check:**

1. The channel's `readOnly` is set to `false`
2. The channel's `enabled` is set to `true`
3. Console shows no permission errors

---

### Hytale → Discord not working

**Check:**

1. At least one channel is configured and `enabled: true`
2. Bot has **Send Messages** permission in the channel
3. Check console for error messages

---

### "Missing permissions in #channel"

**Cause:** The bot lacks required Discord permissions.

**Required permissions:**

- `View Channel` – See the channel
- `Send Messages` – Post messages
- `Read Message History` – Read past messages
- `Manage Channel` – Set slowmode and channel topic (optional)

**Fix:**

1. Go to Server Settings → Roles
2. Find the bot's role
3. Enable the required permissions
4. Or set permissions per-channel in Channel Settings → Permissions

---

## Commands Not Working

### "/aetherlink" command not found

**Possible causes:**

1. **Server didn't fully start**
   - Check console for "AetherLink events registered!"
   - If missing, there was a startup error

2. **Commands not registered**
   - This is a bug – please report it on GitHub

---

### "/aetherlink reload" does nothing

**Cause:** Reload is rate-limited to once every 5 seconds.

**Fix:** Wait 5 seconds and try again.

---

## Spam Control Issues

### Slowmode not being set

**Cause:** Bot lacks `Manage Channel` permission.

**Fix:** Grant the bot `Manage Channel` permission.

---

### Messages being grouped incorrectly

The aggregation groups duplicate events within `hytaleAggregateSeconds`.

**To disable aggregation:**

```json
"spamControl": {
  "hytaleAggregateSeconds": 0
}
```

---

## Performance Issues

### High CPU usage

**Possible causes:**

1. **Too many channels configured**
   - The bot updates topics for all channels every 60 seconds
   - Consider disabling some channels

2. **Rapid reconnection attempts**
   - If Discord is unreachable, the bot retries with exponential backoff
   - Check your network connection

---

### Memory leaks

If memory usage grows over time:

1. Check for errors in the console
2. Restart the server periodically
3. Report the issue on GitHub with logs

---

## Getting Help

If your issue isn't listed here:

1. **Check the console** for error messages
2. **Enable debug logging** (if available)
3. **Open an issue** on [GitHub](https://github.com/vendouple/AetherLink/issues) with:
   - Your `config.json` (remove the bot token!)
   - Relevant console logs
   - Steps to reproduce the issue

---

## Other information

If you are comfortable with downloading and building, if one day aetherlink doesn't work because of an hytale update you can modify and build the .jar file yourself in [Build It Yourself](build.md).

## Known Limitations

| Limitation | Reason |
|------------|--------|
| Max players shows "?" | Hytale API doesn't expose max players yet |
| Death messages don't work | Waiting for Hytale death event API |
| Advancement messages don't work | Waiting for Hytale advancement API |
| Linking not implemented | Planned for future release |
