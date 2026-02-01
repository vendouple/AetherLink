package com.vendouple.aetherlink;

import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.ecs.DiscoverZoneEvent;
import com.hypixel.hytale.server.core.modules.entity.damage.event.KillFeedEvent;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import java.util.concurrent.ConcurrentHashMap;

public class HytaleListener {
    private final Aetherlink plugin;
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

        // Is this a duplicate sent within the time window?
        if (lastInfo != null && (now - lastInfo.timestamp) < (aggregateWindow * 1000L)) {
            lastInfo.count++;
            lastInfo.timestamp = now; 
            
            String editedMsg = formatMessage(name, content + " (" + lastInfo.count + "x)");
            
            plugin.editDiscordMessage(lastInfo.discordMsgId, editedMsg);
        } else {
            String formatted = formatMessage(name, content);
            
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

    public void onDeath(KillFeedEvent.KillerMessage event) {
        AetherMessages messages = plugin.getMessages();
        if (messages == null || messages.chat == null) return;

        String playerName = resolvePlayerName(event);
        String reason = resolveDeathReason(event);

        if (playerName == null || playerName.isBlank()) playerName = "A player";
        if (reason == null || reason.isBlank()) reason = "Unknown";

        String formatted = messages.chat.death
            .replace("{HytalePlayer}", playerName)
            .replace("{DeathReason}", reason);

        plugin.sendToDiscordChannels(formatted);
    }

    public void onZoneDiscover(DiscoverZoneEvent.Display event) {
        AetherMessages messages = plugin.getMessages();
        if (messages == null || messages.chat == null) return;

        var info = event.getDiscoveryInfo();
        if (info == null) return;
        if (!info.display()) return;

        String zoneName = info.zoneName();
        if (zoneName == null || zoneName.isBlank()) {
            zoneName = info.regionName();
        }
        if (zoneName == null || zoneName.isBlank()) {
            zoneName = "Unknown Zone";
        }

        String playerName = resolveSingleOnlinePlayerName();

        String formatted = messages.chat.advancement
            .replace("{HytalePlayer}", playerName)
            .replace("{ZoneUnlock}", zoneName);

        plugin.sendToDiscordChannels(formatted);
    }

    private String resolvePlayerName(KillFeedEvent.KillerMessage event) {
        if (event == null) return null;
        var targetRef = event.getTargetRef();
        if (targetRef == null || targetRef.getStore() == null) return null;
        var store = targetRef.getStore();
        PlayerRef playerRef = store.getComponent(targetRef, PlayerRef.getComponentType());
        return playerRef != null ? playerRef.getUsername() : null;
    }

    private String resolveDeathReason(KillFeedEvent.KillerMessage event) {
        if (event == null) return null;
        Message message = event.getMessage();
        if (message == null && event.getDamage() != null && event.getTargetRef() != null && event.getTargetRef().getStore() != null) {
            try {
                message = event.getDamage().getDeathMessage(event.getTargetRef(), event.getTargetRef().getStore());
            } catch (Exception ignored) {
                // ignore and fall back
            }
        }
        return message != null ? message.getRawText() : null;
    }

    private String resolveSingleOnlinePlayerName() {
        Universe universe = Universe.get();
        if (universe == null) return "A player";
        var players = universe.getPlayers();
        if (players == null || players.isEmpty()) return "A player";
        if (players.size() == 1) return players.get(0).getUsername();
        return "A player";
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