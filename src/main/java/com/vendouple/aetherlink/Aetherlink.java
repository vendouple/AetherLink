package com.vendouple.aetherlink;

import com.hypixel.hytale.server.core.plugin.JavaPlugin; 
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.ecs.DiscoverZoneEvent;
import com.hypixel.hytale.server.core.modules.entity.damage.event.KillFeedEvent;
import com.hypixel.hytale.server.core.HytaleServerConfig;
import java.util.logging.Level;
import java.io.File;
import javax.annotation.Nonnull; 
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;

@SuppressWarnings("null")
public class Aetherlink extends JavaPlugin {
    private JDA jda;
    private ConfigManager configManager;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private volatile long startTimeMillis;
    private volatile long lastReloadMillis;
    private volatile long retryDelaySeconds = 5;
    private final Set<String> warnedMissingManageChannelTopic = ConcurrentHashMap.newKeySet();
    private final Set<String> warnedMissingManageChannelSlowmode = ConcurrentHashMap.newKeySet();
    private volatile boolean warnedMissingPresenceTemplate;
    private volatile boolean warnedMissingTopicTemplate;
    private volatile boolean warnedMissingMaxPlayers;

    public Aetherlink(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void start() { 
        getLogger().at(Level.INFO).log("AetherLink is loading configurations...");
        startTimeMillis = System.currentTimeMillis();
        
        File dataFolder = new File("./config/AetherLink"); 
        configManager = new ConfigManager(dataFolder);
        configManager.load();

        String token = configManager.getConfig().botToken;

        if (token.equals("YOUR_BOT_TOKEN_HERE") || token.isEmpty()) {
            getLogger().at(Level.SEVERE).log("-------------------------------------------");
            getLogger().at(Level.SEVERE).log("[AetherLink] PLEASE SET YOUR BOT TOKEN!");
            getLogger().at(Level.SEVERE).log("Edit the file at: " + dataFolder.getPath() + "/config.json");
            getLogger().at(Level.SEVERE).log("Then restart the server.");
            getLogger().at(Level.SEVERE).log("-------------------------------------------");
            return; 
        }

        getLogger().at(Level.INFO).log("AetherLink is bridging the gap...");

        connectDiscord(token);

        registerHytaleEvents();
        registerCommands();
        schedulePresenceAndTopicUpdates();
        scheduleAutoRetry();
    }

    @Override
    protected void shutdown() {
        if (jda != null) jda.shutdown();
        scheduler.shutdownNow();
        getLogger().at(Level.INFO).log("AetherLink has disconnected.");
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }

    public AetherConfig getConfig() {
        return configManager == null ? null : configManager.getConfig();
    }

    public AetherMessages getMessages() {
        return configManager == null ? null : configManager.getMessages();
    }

    public void validateDiscordChannels(JDA discord) {
        if (configManager == null || configManager.getConfig() == null) {
            getLogger().at(Level.SEVERE).log("[AetherLink] Config not loaded; cannot validate channels.");
            return;
        }

        var channels = configManager.getConfig().getChannelConfigs();
        if (channels == null || channels.isEmpty()) {
            getLogger().at(Level.WARNING).log("[AetherLink] No Discord channels configured in config.json.");
            return;
        }

        if (configManager.getConfig().syncEnabled && channels.size() < 2) {
            getLogger().at(Level.WARNING).log("[AetherLink] Channel sync enabled but fewer than 2 channels are configured. Syncing will be ignored.");
        }

        for (AetherConfig.ChannelConfig channelCfg : channels) {
            if (channelCfg == null || channelCfg.channelId == null || channelCfg.channelId.isBlank()) {
                getLogger().at(Level.WARNING).log("[AetherLink] Empty channel ID in config.json.");
                continue;
            }

            TextChannel channel = discord.getTextChannelById(channelCfg.channelId);
            if (channel == null) {
                getLogger().at(Level.SEVERE).log("[AetherLink] Channel not found or bot lacks access: " + channelCfg.channelId);
                continue;
            }

            var selfMember = channel.getGuild().getSelfMember();
            if (!selfMember.hasPermission(channel, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND)) {
                getLogger().at(Level.SEVERE).log("[AetherLink] Missing permissions in #" + channel.getName()
                    + " (Guild: " + channel.getGuild().getName() + ")"
                    + " - Required: VIEW_CHANNEL, MESSAGE_SEND");
            }
        }
    }

    public void applyDiscordChannelSettings(JDA discord) {
        AetherConfig config = getConfig();
        if (config == null) return;

        int configCooldown = config.spamControl.discordCooldownSeconds;
        // -1 or 0 means disabled in config, so we do nothing
        if (configCooldown <= 0) return; 

        for (AetherConfig.ChannelConfig channelCfg : config.getChannelConfigs()) {
            if (!channelCfg.enabled) continue;

            TextChannel channel = discord.getTextChannelById(channelCfg.channelId);
            if (channel == null) continue;

            int currentSlowmode = channel.getSlowmode();

            // LOGIC: Only act if Config requires MORE restriction than currently exists
            if (configCooldown > currentSlowmode) {
                 var self = channel.getGuild().getSelfMember();

                 // Check permission first
                 if (self.hasPermission(channel, Permission.MANAGE_CHANNEL)) {
                     channel.getManager().setSlowmode(configCooldown).queue();
                     getLogger().at(Level.INFO).log("[AetherLink] Enforced " + configCooldown + "s slowmode in #" + channel.getName());
                 } else {
                     // WARN because we WANTED to set it but COULDN'T
                     getLogger().at(Level.WARNING).log("--------------------------------------------------");
                     getLogger().at(Level.WARNING).log("[AetherLink] SECURITY WARNING");
                     getLogger().at(Level.WARNING).log("Cannot enforce slowmode in channel: #" + channel.getName());
                     getLogger().at(Level.WARNING).log("Bot needs 'Manage Channel' permission.");
                     getLogger().at(Level.WARNING).log("Current: " + currentSlowmode + "s | Required: " + configCooldown + "s");
                     getLogger().at(Level.WARNING).log("--------------------------------------------------");
                     warnMissingManageChannelOnce(
                         channel,
                         null,
                         "AetherLink: Missing 'Manage Channel' permission; cannot enforce slowmode.",
                         warnedMissingManageChannelSlowmode
                     );
                 }
            }
        }
    }

    private void registerHytaleEvents() {
        HytaleListener listener = new HytaleListener(this);

        getEventRegistry().registerGlobal(PlayerConnectEvent.class, listener::onJoin);
        getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, listener::onQuit);

        getEventRegistry().registerGlobal(PlayerChatEvent.class, listener::onChat);

        registerEcsEvent(DiscoverZoneEvent.Display.class, listener::onZoneDiscover);
        registerEcsEvent(KillFeedEvent.KillerMessage.class, listener::onDeath);

        getLogger().at(Level.INFO).log("AetherLink events registered!");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <T> void registerEcsEvent(Class<T> eventClass, java.util.function.Consumer<T> handler) {
        getEventRegistry().registerGlobal((Class) eventClass, (java.util.function.Consumer) handler);
    }

    private void registerCommands() {
        getCommandRegistry().registerCommand(new AetherlinkCommand(this));
    }

    private void connectDiscord(String token) {
        try {
            if (jda != null) {
                jda.shutdownNow();
                jda = null;
            }
            jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new DiscordListener(this))
                .build();
            retryDelaySeconds = 5;
        } catch (Exception e) {
            getLogger().at(Level.SEVERE).withCause(e).log("Failed to start AetherLink Discord bot!");
        }
    }

    private void scheduleAutoRetry() {
        scheduler.scheduleAtFixedRate(() -> {
            if (jda == null || jda.getStatus() != JDA.Status.CONNECTED) {
                long delay = retryDelaySeconds;
                retryDelaySeconds = Math.min(retryDelaySeconds * 2, 300);
                scheduler.schedule(this::retryDiscordNow, delay, TimeUnit.SECONDS);
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    public synchronized void retryDiscordNow() {
        AetherConfig config = getConfig();
        if (config == null || config.botToken == null || config.botToken.isBlank()) return;
        if (jda != null && jda.getStatus() == JDA.Status.CONNECTED) return;
        connectDiscord(config.botToken);
    }

    public boolean getDiscordStatusConnected() {
        return jda != null && jda.getStatus() == JDA.Status.CONNECTED;
    }

    public synchronized boolean reloadConfigs() {
        if (configManager == null) return false;
        long now = System.currentTimeMillis();
        if (now - lastReloadMillis < 5000) return false;
        lastReloadMillis = now;
        configManager.load();
        if (jda != null) {
            validateDiscordChannels(jda);
            applyDiscordChannelSettings(jda);
            updatePresenceAndTopics();
        }
        return true;
    }

    private void schedulePresenceAndTopicUpdates() {
        scheduler.scheduleAtFixedRate(this::updatePresenceAndTopics, 10, 60, TimeUnit.SECONDS);
    }

    public void updatePresenceAndTopics() {
        if (jda == null) return;
        AetherMessages messages = getMessages();
        if (messages == null || messages.info == null) return;

        int playerCount = getHytalePlayerCount();
        String maxPlayers = getHytaleMaxPlayers();
        String uptime = formatUptime();

        if (messages.info.presence == null || messages.info.presence.isBlank()) {
            if (!warnedMissingPresenceTemplate) {
                warnedMissingPresenceTemplate = true;
                getLogger().at(Level.WARNING).log("[AetherLink] Presence template is missing. Set messages.info.presence in messages.json.");
            }
        } else {
            String presence = messages.info.presence
                .replace("{PlayerCount}", String.valueOf(playerCount));
            jda.getPresence().setActivity(Activity.playing(presence));
        }

        AetherConfig config = getConfig();
        if (config == null || config.getChannelConfigs() == null) return;

        if (messages.info.topicUpdater == null || messages.info.topicUpdater.isBlank()) {
            if (!warnedMissingTopicTemplate) {
                warnedMissingTopicTemplate = true;
                getLogger().at(Level.WARNING).log("[AetherLink] Topic template is missing. Set messages.info.topicUpdater in messages.json.");
            }
            return;
        }

        for (AetherConfig.ChannelConfig channelCfg : config.getChannelConfigs()) {
            if (channelCfg == null || !channelCfg.enabled) continue;
            if (channelCfg.channelId == null || channelCfg.channelId.isBlank()) continue;

            TextChannel channel = jda.getTextChannelById(channelCfg.channelId);
            if (channel == null) continue;

            var selfMember = channel.getGuild().getSelfMember();
            if (!selfMember.hasPermission(channel, Permission.MANAGE_CHANNEL)) {
                warnMissingManageChannelOnce(
                    channel,
                    "[AetherLink] Missing Manage Channel permission in #" + channel.getName()
                        + " (Guild: " + channel.getGuild().getName() + "). Topic cannot be updated.",
                    "AetherLink: Missing 'Manage Channel' permission; cannot update topic.",
                    warnedMissingManageChannelTopic
                );
                continue;
            }

            String topic = messages.info.topicUpdater
                .replace("{PlayerCount}", String.valueOf(playerCount))
                .replace("{MaxPlayers}", maxPlayers)
                .replace("{Uptime}", uptime);

            channel.getManager().setTopic(topic).queue();
        }
    }

    private int getHytalePlayerCount() {
        Universe universe = Universe.get();
        return universe != null ? universe.getPlayerCount() : 0;
    }

    private String getHytaleMaxPlayers() {
        try {
            HytaleServerConfig serverConfig = HytaleServerConfig.load();
            if (serverConfig == null) return "?";
            int maxPlayers = serverConfig.getMaxPlayers();
            if (maxPlayers > 0) return String.valueOf(maxPlayers);
        } catch (Exception e) {
            if (!warnedMissingMaxPlayers) {
                warnedMissingMaxPlayers = true;
                getLogger().at(Level.WARNING).withCause(e)
                    .log("[AetherLink] Unable to read max players from HytaleServerConfig.");
            }
        }
        return "?";
    }

    private String formatUptime() {
        long elapsed = System.currentTimeMillis() - startTimeMillis;
        Duration d = Duration.ofMillis(Math.max(0, elapsed));
        long hours = d.toHours();
        long minutes = d.toMinutesPart();
        long seconds = d.toSecondsPart();
        return String.format("%02dh %02dm %02ds", hours, minutes, seconds);
    }

    public void sendToHytaleChat(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) return;
        try {
            Universe universe = Universe.get();
            if (universe == null) {
                getLogger().at(Level.SEVERE).log("[AetherLink] Universe not ready. Cannot send chat message.");
                return;
            }
            universe.sendMessage(Message.raw(rawMessage));
        } catch (Exception e) {
            getLogger().at(Level.SEVERE).withCause(e).log("[AetherLink] Failed to send message to Hytale chat.");
        }
    }

    public void sendToDiscordChannels(String message) {
        if (message == null || message.isBlank() || jda == null) return;
        AetherConfig config = getConfig();
        if (config == null || config.getChannelConfigs() == null) return;

        for (AetherConfig.ChannelConfig cfg : config.getChannelConfigs()) {
            if (cfg == null || !cfg.enabled) continue;
            if (cfg.channelId == null || cfg.channelId.isBlank()) continue;

            TextChannel channel = jda.getTextChannelById(cfg.channelId);
            if (channel == null) {
                getLogger().at(Level.WARNING).log("[AetherLink] Channel not found or bot lacks access: " + cfg.channelId);
                continue;
            }

            var selfMember = channel.getGuild().getSelfMember();
            if (!selfMember.hasPermission(channel, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND)) {
                getLogger().at(Level.SEVERE).log("[AetherLink] Missing permissions in #" + channel.getName()
                    + " (Guild: " + channel.getGuild().getName() + ")"
                    + " - Required: VIEW_CHANNEL, MESSAGE_SEND");
                continue;
            }

            channel.sendMessage(message).queue();
        }
    }
        public void sendToDiscordChannelsCallback(String message, java.util.function.Consumer<net.dv8tion.jda.api.entities.Message> callback) {
        if (jda == null || message == null) return;
        for (AetherConfig.ChannelConfig cfg : getConfig().getChannelConfigs()) {
            if (!cfg.enabled || cfg.readOnly) continue;
            net.dv8tion.jda.api.entities.channel.concrete.TextChannel ch = jda.getTextChannelById(cfg.channelId);
            if (ch != null) {
                // Queue with success callback
                ch.sendMessage(message).queue(callback); 
            }
        }
    }

    public void editDiscordMessage(String messageId, String newContent) {
        if (jda == null || messageId == null) return;
        for (AetherConfig.ChannelConfig cfg : getConfig().getChannelConfigs()) {
            if (!cfg.enabled || cfg.readOnly) continue;
            net.dv8tion.jda.api.entities.channel.concrete.TextChannel ch = jda.getTextChannelById(cfg.channelId);
            if (ch != null) {
                // Retrieve message by ID and edit it
                ch.retrieveMessageById(messageId).queue(msg -> {
                    msg.editMessage(newContent).queue();
                }, failure -> { /* Message might have been deleted, ignore */ });
            }
        }
    }

    private void warnMissingManageChannelOnce(TextChannel channel, String logMessage, String channelMessage, Set<String> warnedSet) {
        if (channel == null || warnedSet == null) return;
        if (!warnedSet.add(channel.getId())) return;
        if (logMessage != null && !logMessage.isBlank()) {
            getLogger().at(Level.WARNING).log(logMessage);
        }
        if (channelMessage == null || channelMessage.isBlank()) return;
        var selfMember = channel.getGuild().getSelfMember();
        if (!selfMember.hasPermission(channel, Permission.MESSAGE_SEND)) return;
        channel.sendMessage(channelMessage).queue();
    }
}