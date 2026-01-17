package com.vendouple.aetherlink;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;

public class ConfigManager {
    private final File configFile;
    private final File messagesFile;
    private final Gson gson;
    
    private AetherConfig config;
    private AetherMessages messages;

    public ConfigManager(File dataFolder) {
        // Ensure the config folder exists
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        this.configFile = new File(dataFolder, "config.json");
        this.messagesFile = new File(dataFolder, "messages.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void load() {
        loadConfig();
        loadMessages();
    }

    private void loadConfig() {
        if (!configFile.exists()) {
            config = new AetherConfig();
            AetherConfig.ChannelConfig channel = new AetherConfig.ChannelConfig();
            channel.channelId = "000000000000000000";
            config.channelConfigs.add(channel);
            saveConfig();
            return;
        }
        try (Reader reader = new FileReader(configFile)) {
            config = gson.fromJson(reader, AetherConfig.class);
            if (config.channelConfigs == null) {
                config.channelConfigs = new java.util.ArrayList<>();
                saveConfig();
            }
        } catch (IOException e) {
            e.printStackTrace();
            config = new AetherConfig();
        }
    }

    private void loadMessages() {
        if (!messagesFile.exists()) {
            messages = new AetherMessages();
            saveMessages();
            return;
        }
        try (Reader reader = new FileReader(messagesFile)) {
            messages = gson.fromJson(reader, AetherMessages.class);
        } catch (IOException e) {
            e.printStackTrace();
            messages = new AetherMessages();
        }
    }

    public void saveConfig() {
        try (Writer writer = new FileWriter(configFile)) {
            gson.toJson(config, writer);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void saveMessages() {
        try (Writer writer = new FileWriter(messagesFile)) {
            gson.toJson(messages, writer);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public AetherConfig getConfig() { return config; }
    public AetherMessages getMessages() { return messages; }
}