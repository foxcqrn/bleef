package com.foxcqrn.bleef.commands;

import com.foxcqrn.bleef.Bleef;
import com.foxcqrn.bleef.PluginUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class CommandColor implements CommandExecutor {
    private final Bleef plugin;

    public CommandColor(Bleef plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(PluginUtil.ErrNoConsole);
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 0 || !isValidHex(args[0])) {
            sender.sendMessage(ChatColor.RED + "Usage: /color <#hex>");
            return true;
        }

        FileConfiguration config = plugin.getConfig();
        config.set("players." + player.getUniqueId() + ".color", args[0]);
        plugin.saveConfig();
        sender.sendMessage(net.md_5.bungee.api.ChatColor.of(args[0]) + "Set name color to " + args[0]);
        PluginUtil.updateName(player);
        return true;
    }

    private boolean isValidHex(String hex) {
        return hex.matches("^#[0-9A-Fa-f]{6}$");
    }
}