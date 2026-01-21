package com.vendouple.aetherlink;

import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import java.util.concurrent.ConcurrentHashMap;

public class HytaleListener {
    private final Aetherlink plugin;
    // Stores the last message info for each player to handle editing
    private final ConcurrentHashMap<String, LastMessageInfo> lastMessages = new ConcurrentHashMap<>();

    public HytaleListener(Aetherlink plugin) {
        this.plugin = plugin;
    }

    public void onJoin(PlayerConnectEvent event) {
        if (plugin.getMessages() == null) return;
        String name = event.getPlayerRef().getUsername();
        plugin.sendToDiscordChannels(plugin.getMessages().chat.join.replace("{HytalePlayer}", name));
    }

    public void onQuit(PlayerDisconnectEvent event) {
        if (plugin.getMessages() == null) return;
        String name = event.getPlayerRef().getUsername();
        plugin.sendToDiscordChannels(plugin.getMessages().chat.leave.replace("{HytalePlayer}", name));
    }

    public void onChat(PlayerChatEvent event) {
        String name = event.getSender().getUsername();
        String content = event.getContent();
        
        AetherConfig config = plugin.getConfig();
        int aggregateWindow = config.spamControl.hytaleAggregateSeconds;

        // Unique key for this player + this specific message content
        String key = name + ":" + content;
        long now = System.currentTimeMillis();

        LastMessageInfo lastInfo = lastMessages.get(key);

        // CHECK: Is this a duplicate sent within the time window?
        if (lastInfo != null && (now - lastInfo.timestamp) < (aggregateWindow * 1000L)) {
            // YES: It's a duplicate. Update the count and EDIT the Discord message.
            lastInfo.count++;
            lastInfo.timestamp = now; // Reset timer so they can keep chaining
            
            String editedMsg = formatMessage(name, content + " (" + lastInfo.count + "x)");
            
            // We specifically edit the specific Discord message we saved earlier
            plugin.editDiscordMessage(lastInfo.discordMsgId, editedMsg);
        } else {
            // NO: It's a new message (or time expired). Send normally.
            String formatted = formatMessage(name, content);
            
            // Send and SAVE the Discord Message ID so we can edit it later
            plugin.sendToDiscordChannelsCallback(formatted, (sentMsg) -> {
                if (sentMsg != null) {
                    lastMessages.put(key, new LastMessageInfo(sentMsg.getId(), now));
                }
            });
        }
    }

    private String formatMessage(String player, String msg) {
        return plugin.getMessages().chat.hytaleToDiscord
                .replace("{HytalePlayer}", player)
                .replace("{Message}", msg);
    }

    // simple data holder
    private static class LastMessageInfo {
        String discordMsgId;
        long timestamp;
        int count;

        public LastMessageInfo(String msgId, long timestamp) {
            this.discordMsgId = msgId;
            this.timestamp = timestamp;
            this.count = 1;
        }
    }
}