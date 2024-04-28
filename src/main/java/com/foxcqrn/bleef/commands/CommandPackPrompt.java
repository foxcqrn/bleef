package com.foxcqrn.bleef.commands;

import com.foxcqrn.bleef.Bleef;
import com.foxcqrn.bleef.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class CommandPackPrompt implements CommandExecutor {
    private final Bleef plugin;

    public CommandPackPrompt(Bleef plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(PluginUtil.ErrNoPerm);
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(PluginUtil.ErrNoConsole);
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /packprompt <url> <hash>");
            return true;
        }

        Player player = (Player) sender;
        String url = args[0];
        String hash = args[1];
        UUID id = UUID.randomUUID();
        Bukkit.getOnlinePlayers().forEach(p -> p.addResourcePack(id, url, new BigInteger(hash, 16).toByteArray(),
                "An administrator has requested a resource pack to be added. Would you like to add it?", false));
        player.sendMessage("Requested resource pack with ID", id.toString());
        return true;
    }
}