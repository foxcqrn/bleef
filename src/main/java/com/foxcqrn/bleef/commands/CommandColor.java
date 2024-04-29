package com.foxcqrn.bleef.commands;

import static com.foxcqrn.bleef.Bleef.plugin;

import com.foxcqrn.bleef.PluginUtil;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.executors.CommandArguments;

public class CommandColor {
    public static void onCommand(CommandSender sender, CommandArguments args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(PluginUtil.ErrNoConsole);
            return;
        }

        Player player = (Player) sender;
        String color = (String) args.get("color");
        assert color != null;
        if (!isValidHex(color)) {
            sender.sendMessage(ChatColor.RED + "Invalid color hex!");
            return;
        }

        FileConfiguration config = plugin.getConfig();
        config.set("players." + player.getUniqueId() + ".color", color);
        plugin.saveConfig();
        sender.sendMessage(net.md_5.bungee.api.ChatColor.of(color) + "Set name color to " + color);
        PluginUtil.updateName(player);
    }

    private static boolean isValidHex(String hex) {
        return hex.matches("^#[0-9A-Fa-f]{6}$");
    }
}