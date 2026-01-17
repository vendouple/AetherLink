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
    protected CompletableFuture<Void> execute(CommandContext context) {
        String input = context.getInputString();
        String[] parts = input == null ? new String[0] : input.trim().split("\\s+");

        if (parts.length <= 1) {
            context.sendMessage(Message.raw("Usage: /aetherlink reload | /aetherlink retrynow"));
            return CompletableFuture.completedFuture(null);
        }

        String sub = parts[1].toLowerCase();
        switch (sub) {
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
            default -> context.sendMessage(Message.raw("Unknown subcommand. Use: /aetherlink reload | /aetherlink retrynow"));
        }

        return CompletableFuture.completedFuture(null);
    }
}
