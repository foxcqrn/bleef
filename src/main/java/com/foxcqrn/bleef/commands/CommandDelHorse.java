package com.foxcqrn.bleef.commands;

import static com.foxcqrn.bleef.Bleef.plugin;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

import dev.jorel.commandapi.executors.CommandArguments;

public class CommandDelHorse {
    public static void onCommand(CommandSender sender, CommandArguments args) {
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        FileConfiguration config = plugin.getConfig();
        if (config.get(String.format("players.%s.horse", playerUUID)) == null) {
            player.sendMessage(ChatColor.RED + "You don't have a vehicle registered!");
            return;
        }
        config.set(String.format("players.%s.horse", playerUUID), null);
        plugin.saveConfig();
        player.sendMessage(
                ChatColor.GREEN + String.format("Removed vehicle for %s.", player.getName()));
    }
}
