package com.vendouple.aetherlink;

import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;

public class HytaleListener {
    private final Aetherlink plugin;

    public HytaleListener(Aetherlink plugin) {
        this.plugin = plugin;
    }

    public void onJoin(PlayerConnectEvent event) {
        if (event == null || plugin == null) return;
        AetherMessages messages = plugin.getMessages();
        if (messages == null) return;

        String playerName = event.getPlayerRef() != null ? event.getPlayerRef().getUsername() : null;
        if (playerName == null || playerName.isBlank()) return;

        String formatted = messages.chat.join
            .replace("{HytalePlayer}", playerName);

        plugin.sendToDiscordChannels(formatted);
    }

    public void onQuit(PlayerDisconnectEvent event) {
        if (event == null || plugin == null) return;
        AetherMessages messages = plugin.getMessages();
        if (messages == null) return;

        String playerName = event.getPlayerRef() != null ? event.getPlayerRef().getUsername() : null;
        if (playerName == null || playerName.isBlank()) return;

        String formatted = messages.chat.leave
            .replace("{HytalePlayer}", playerName);

        plugin.sendToDiscordChannels(formatted);
    }

    public void onChat(PlayerChatEvent event) {
        if (event == null || plugin == null) return;
        AetherMessages messages = plugin.getMessages();
        if (messages == null) return;

        String playerName = event.getSender() != null ? event.getSender().getUsername() : null;
        String content = event.getContent();
        if (playerName == null || playerName.isBlank()) return;
        if (content == null || content.isBlank()) return;

        String formatted = messages.chat.hytaleToDiscord
            .replace("{HytalePlayer}", playerName)
            .replace("{Message}", content);

        plugin.sendToDiscordChannels(formatted);
    }
}
