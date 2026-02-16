package com.vendouple.aetherlink;

import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.ecs.DiscoverZoneEvent;
import com.hypixel.hytale.server.core.modules.entity.damage.event.KillFeedEvent;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

public class HytaleListener {
    private final Aetherlink plugin;

    public HytaleListener(Aetherlink plugin) {
        this.plugin = plugin;
    }

    public void onJoin(PlayerConnectEvent event) {
        if (plugin.getMessages() == null) return;
        String name = event.getPlayerRef().getUsername();
        String message = plugin.getMessages().chat.join.replace("{HytalePlayer}", name);
        // Use webhook with player name for join messages
        plugin.sendToDiscordViaWebhook(name, message);
    }

    public void onQuit(PlayerDisconnectEvent event) {
        if (plugin.getMessages() == null) return;
        String name = event.getPlayerRef().getUsername();
        String message = plugin.getMessages().chat.leave.replace("{HytalePlayer}", name);
        // Use webhook with player name for leave messages
        plugin.sendToDiscordViaWebhook(name, message);
    }

    public void onChat(PlayerChatEvent event) {
        String name = event.getSender().getUsername();
        String content = event.getContent();
        
        // Send chat message via webhook with player's name
        // The message content is sent directly, username comes from webhook
        plugin.sendToDiscordViaWebhook(name, content);
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

        // Use webhook with player name for death messages
        plugin.sendToDiscordViaWebhook(playerName, formatted);
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

        // Use webhook with player name for advancement messages
        plugin.sendToDiscordViaWebhook(playerName, formatted);
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
}