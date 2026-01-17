package com.vendouple.aetherlink;

import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class HytaleListener {
    private final Aetherlink plugin;

    private static final ScheduledExecutorService AGGREGATOR = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "Aetherlink-HytaleAggregator");
            t.setDaemon(true);
            return t;
        }
    });

    private final ConcurrentHashMap<String, DuplicateState> duplicates = new ConcurrentHashMap<>();

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

        AetherConfig config = plugin.getConfig();
        int aggregateSeconds = config != null && config.spamControl != null
            ? config.spamControl.hytaleAggregateSeconds
            : 0;

        if (aggregateSeconds > 0) {
            enqueueDuplicate(playerName, content, messages, aggregateSeconds);
        } else {
            String formatted = messages.chat.hytaleToDiscord
                .replace("{HytalePlayer}", playerName)
                .replace("{Message}", content);
            plugin.sendToDiscordChannels(formatted);
        }
    }

    private void enqueueDuplicate(String playerName, String content, AetherMessages messages, int aggregateSeconds) {
        String key = playerName + ":" + content;
        DuplicateState state = duplicates.computeIfAbsent(key, k -> new DuplicateState(playerName, content));
        synchronized (state) {
            state.count++;
            state.messages = messages;
            if (state.future == null || state.future.isDone()) {
                state.future = AGGREGATOR.schedule(() -> flushDuplicate(key), aggregateSeconds, TimeUnit.SECONDS);
            }
        }
    }

    private void flushDuplicate(String key) {
        DuplicateState state = duplicates.remove(key);
        if (state == null) return;

        int count;
        String playerName;
        String content;
        AetherMessages messages;

        synchronized (state) {
            count = state.count;
            playerName = state.playerName;
            content = state.content;
            messages = state.messages;
        }

        if (messages == null || playerName == null || playerName.isBlank() || content == null || content.isBlank()) {
            return;
        }

        String messageText = count > 1 ? content + " (" + count + ")" : content;
        String formatted = messages.chat.hytaleToDiscord
            .replace("{HytalePlayer}", playerName)
            .replace("{Message}", messageText);

        plugin.sendToDiscordChannels(formatted);
    }

    private static class DuplicateState {
        private final String playerName;
        private final String content;
        private int count = 0;
        private AetherMessages messages;
        private ScheduledFuture<?> future;

        private DuplicateState(String playerName, String content) {
            this.playerName = playerName;
            this.content = content;
        }
    }
}
