package com.vendouple.aetherlink;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import javax.annotation.Nonnull; 
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.List;


public class DiscordListener extends ListenerAdapter {
    private static final ScheduledExecutorService AGGREGATOR = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "Aetherlink-DiscordAggregator");
            t.setDaemon(true);
            return t;
        }
    });

    private final ConcurrentHashMap<String, AggregationState> aggregates = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> lastMessageTimes = new ConcurrentHashMap<>();

    @SuppressWarnings("unused") 
    private final Aetherlink plugin;

    public DiscordListener(Aetherlink plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        if (!event.isFromGuild()) return;

        AetherConfig config = plugin.getConfig();
        AetherMessages messages = plugin.getMessages();
        if (config == null || messages == null) {
            return;
        }

        String channelId = event.getChannel().getId();
        AetherConfig.ChannelConfig channelConfig = config.findChannelConfig(channelId);
        if (channelConfig == null || !channelConfig.enabled) {
            return; 
        }

        GuildMessageChannel channel = event.getChannel().asGuildMessageChannel();
        var selfMember = event.getGuild().getSelfMember();
        if (!selfMember.hasPermission(channel, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND)) {
            plugin.getLogger().at(java.util.logging.Level.SEVERE)
                .log("[AetherLink] Missing permissions in #" + channel.getName()
                    + " (Guild: " + event.getGuild().getName() + ")"
                    + " - Required: VIEW_CHANNEL, MESSAGE_SEND");
            return;
        }

        String content = event.getMessage().getContentDisplay();
        if (content == null || content.isBlank()) return;

        Member member = event.getMember();
        String displayName = member != null ? member.getEffectiveName() : event.getAuthor().getName();
        String roleInitial = getTopRoleInitial(member);

        String hytaleFormatted = messages.chat.discordToHytale
            .replace("{DiscordName}", displayName)
            .replace("{Message}", content)
            .replace("{TopDiscordRoleInitials1}", roleInitial);

        if (!channelConfig.readOnly) {
            int cooldownSeconds = config.spamControl != null ? config.spamControl.discordCooldownSeconds : 0;
            int aggregateSeconds = config.spamControl != null ? config.spamControl.hytaleAggregateSeconds : 0;

            if (cooldownSeconds > 0) {
                String key = channelId + ":" + event.getAuthor().getId();
                long now = System.currentTimeMillis();
                Long last = lastMessageTimes.put(key, now);

                if (last != null && (now - last) < (cooldownSeconds * 1000L)) {
                    if (aggregateSeconds > 0) {
                        enqueueAggregate(key, displayName, roleInitial, content, messages, aggregateSeconds);
                    }
                } else {
                    plugin.sendToHytaleChat(hytaleFormatted);
                }
            } else {
                plugin.sendToHytaleChat(hytaleFormatted);
            }
        }

        // Cross-channel sync
        if (config.syncEnabled) {
            var configuredChannels = config.getChannelConfigs();
            if (configuredChannels != null && configuredChannels.size() >= 2) {
                for (AetherConfig.ChannelConfig targetCfg : configuredChannels) {
                    if (targetCfg == null || !targetCfg.enabled || !targetCfg.syncWithOtherChannels) continue;
                    if (targetCfg.channelId == null || targetCfg.channelId.isBlank()) continue;
                    if (targetCfg.channelId.equals(channelId)) continue;

                    GuildMessageChannel targetChannel = event.getJDA().getTextChannelById(targetCfg.channelId);
                    if (targetChannel == null) {
                        plugin.getLogger().at(java.util.logging.Level.WARNING)
                            .log("[AetherLink] Sync target channel not found or inaccessible: " + targetCfg.channelId);
                        continue;
                    }

                    var selfMemberTarget = targetChannel.getGuild().getSelfMember();
                    if (!selfMemberTarget.hasPermission(targetChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND)) {
                        plugin.getLogger().at(java.util.logging.Level.SEVERE)
                            .log("[AetherLink] Missing permissions in #" + targetChannel.getName()
                                + " (Guild: " + targetChannel.getGuild().getName() + ")"
                                + " - Required: VIEW_CHANNEL, MESSAGE_SEND");
                        continue;
                    }

                    String discordToDiscord = messages.chat.discordToDiscord
                        .replace("{SourceChannel}", channel.getName())
                        .replace("{DiscordName}", displayName)
                        .replace("{Message}", content)
                        .replace("{TopDiscordRoleInitials1}", roleInitial);

                    targetChannel.sendMessage(discordToDiscord).queue();
                }
            }
        }
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        plugin.validateDiscordChannels(event.getJDA());
    }

    private String getTopRoleInitial(Member member) {
        if (member == null) return "";
        List<Role> roles = member.getRoles();
        for (Role role : roles) {
            if (role.isPublicRole()) continue;
            String name = role.getName();
            if (name == null || name.isBlank()) continue;
            return String.valueOf(name.trim().charAt(0));
        }
        return "";
    }

    private void enqueueAggregate(
        String key,
        String displayName,
        String roleInitial,
        String content,
        AetherMessages messages,
        int aggregateSeconds
    ) {
        AggregationState state = aggregates.computeIfAbsent(key, k -> new AggregationState());
        synchronized (state) {
            if (state.buffer.length() > 0) {
                state.buffer.append(" | ");
            }
            state.buffer.append(content);
            state.lastDisplayName = displayName;
            state.lastRoleInitial = roleInitial;
            state.lastMessages = messages;

            if (state.future == null || state.future.isDone()) {
                state.future = AGGREGATOR.schedule(() -> flushAggregate(key), aggregateSeconds, TimeUnit.SECONDS);
            }
        }
    }

    private void flushAggregate(String key) {
        AggregationState state = aggregates.remove(key);
        if (state == null) return;

        String combined;
        String displayName;
        String roleInitial;
        AetherMessages messages;

        synchronized (state) {
            combined = state.buffer.toString();
            displayName = state.lastDisplayName;
            roleInitial = state.lastRoleInitial;
            messages = state.lastMessages;
            state.buffer.setLength(0);
        }

        if (combined == null || combined.isBlank() || messages == null) return;

        String formatted = messages.chat.discordToHytale
            .replace("{DiscordName}", displayName != null ? displayName : "")
            .replace("{Message}", combined)
            .replace("{TopDiscordRoleInitials1}", roleInitial != null ? roleInitial : "");

        plugin.sendToHytaleChat(formatted);
    }

    private static class AggregationState {
        private final StringBuilder buffer = new StringBuilder();
        private String lastDisplayName;
        private String lastRoleInitial;
        private AetherMessages lastMessages;
        private ScheduledFuture<?> future;
    }
}