package com.vendouple.aetherlink;

import java.util.ArrayList;
import java.util.List;

public class AetherConfig {
    public String botToken = "YOUR_BOT_TOKEN_HERE";

    public List<ChannelConfig> channelConfigs = new ArrayList<>();

    public boolean syncEnabled = false;
    
    public Linking linking = new Linking();
    public SpamControl spamControl = new SpamControl();

    public static class Linking {
        public boolean enabled = true;
        public int codeExpirationSeconds = 300; 
    }

    public static class SpamControl {
        public int discordCooldownSeconds = 2; 
        public int hytaleAggregateSeconds = 2; 
    }

    public static class ChannelConfig {
        public String channelId = "";
        /**
         * If true, Discord messages in this channel are NOT sent to Hytale.
         * Hytale messages can still be posted to this channel.
         */
        public boolean readOnly = false;

        /**
         * If true, this channel participates in cross-channel sync when globally enabled.
         */
        public boolean syncWithOtherChannels = true;

        /**
         * If false, the channel is ignored entirely.
         */
        public boolean enabled = true;
    }

    public List<ChannelConfig> getChannelConfigs() {
        return channelConfigs;
    }

    public ChannelConfig findChannelConfig(String channelId) {
        if (channelId == null || channelId.isBlank()) return null;
        for (ChannelConfig cfg : getChannelConfigs()) {
            if (cfg != null && channelId.equals(cfg.channelId)) {
                return cfg;
            }
        }
        return null;
    }
}