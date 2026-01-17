package com.vendouple.aetherlink;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import javax.annotation.Nonnull; 
import java.util.List;


public class DiscordListener extends ListenerAdapter {
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
            plugin.sendToHytaleChat(hytaleFormatted);
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
        plugin.applyDiscordChannelSettings(event.getJDA());
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
}