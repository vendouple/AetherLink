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
        plugin.sendToDiscordViaWebhook(name, message);
    }

    public void onChat(PlayerChatEvent event) {
        String name = event.getSender().getUsername();
        String content = event.getContent();
        
        plugin.sendToDiscordViaWebhook(name, content);
    }

    private String formatMessage(String player, String msg) {
        return plugin.getMessages().chat.hytaleToDiscord
                .replace("{HytalePlayer}", player)
                .replace("{Message}", msg);
    }

    public void onDeath(KillFeedEvent.DecedentMessage event) {
        AetherMessages messages = plugin.getMessages();
        if (messages == null || messages.chat == null) return;

        Message message = event.getMessage();
        String deathMessage = message != null ? message.getRawText() : null;
        
        String playerName = "A player";
        if (deathMessage != null && !deathMessage.isBlank()) {
            int spaceIndex = deathMessage.indexOf(' ');
            if (spaceIndex > 0) {
                playerName = deathMessage.substring(0, spaceIndex);
            }
        }
        
        if (deathMessage == null || deathMessage.isBlank()) {
            deathMessage = "Unknown cause";
        }

        String formatted = messages.chat.death
            .replace("{HytalePlayer}", playerName)
            .replace("{DeathReason}", deathMessage);

        plugin.sendToDiscordViaWebhook(playerName, formatted);
    }


    public void onKill(KillFeedEvent.KillerMessage event) {
        // NOt needed, can be added tho
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

        plugin.sendToDiscordViaWebhook(playerName, formatted);
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