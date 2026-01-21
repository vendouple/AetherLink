package com.vendouple.aetherlink;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import java.util.concurrent.CompletableFuture;

public class AetherlinkCommand extends AbstractCommand {
    private final Aetherlink plugin;

    public AetherlinkCommand(Aetherlink plugin) {
        super("aetherlink", "AetherLink commands");
        this.plugin = plugin;
        requirePermission("aetherlink.admin");
        setAllowsExtraArguments(true);
    }

    @Override
    protected CompletableFuture<Void> execute(@SuppressWarnings("null") CommandContext context) {
        String input = context.getInputString();
        String[] parts = input == null ? new String[0] : input.trim().split("\\s+");

        if (parts.length <= 1) {
            sendHelp(context);
            return CompletableFuture.completedFuture(null);
        }

        String sub = parts[1].toLowerCase();
        switch (sub) {
            case "help" -> sendHelp(context);
            case "reload" -> {
                boolean ok = plugin.reloadConfigs();
                if (!ok) {
                    context.sendMessage(Message.raw("Reload is on cooldown. Try again in a few seconds."));
                } else {
                    context.sendMessage(Message.raw("AetherLink config reloaded."));
                }
            }
            case "retrynow" -> {
                if (plugin.getDiscordStatusConnected()) {
                    context.sendMessage(Message.raw("Discord is already connected."));
                } else {
                    plugin.retryDiscordNow();
                    context.sendMessage(Message.raw("Retrying Discord connection..."));
                }
            }
            default -> {
                context.sendMessage(Message.raw("Unknown subcommand. Use /aetherlink help."));
                sendHelp(context);
            }
        }

        return CompletableFuture.completedFuture(null);
    }

    private void sendHelp(CommandContext context) {
        context.sendMessage(Message.raw("AetherLink commands:\n" +
            "/aetherlink help - Show this help menu\n" +
            "/aetherlink reload - Reload config and messages\n" +
            "/aetherlink retrynow - Retry Discord connection"));
    }
}
