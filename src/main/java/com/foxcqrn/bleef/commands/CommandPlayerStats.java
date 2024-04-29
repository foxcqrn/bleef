package com.foxcqrn.bleef.commands;

import com.foxcqrn.bleef.PluginUtil;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

import dev.jorel.commandapi.executors.CommandArguments;

public class CommandPlayerStats {
    public static void onCommand(CommandSender sender, CommandArguments args) {
        Player player = (Player) args.get("player");
        if (player == null) {
            sender.sendMessage(PluginUtil.ErrNoPlayer);
            return;
        }
        sender.sendMessage(
                ChatColor.GOLD + "Showing information for player \"" + player.getName() + "\":");
        sender.sendMessage(ChatColor.GOLD + "\nNickname: " + PluginUtil.getNickname(player)
                + ChatColor.GOLD + "\nHealth: " + ChatColor.WHITE + player.getHealth()
                + ChatColor.GOLD + "\nFood: " + ChatColor.WHITE + player.getFoodLevel()
                + ChatColor.GOLD + "\nIP Address: " + ChatColor.WHITE
                + Objects.requireNonNull(player.getAddress()).toString().trim() + ChatColor.GOLD
                + "\nUUID: " + ChatColor.WHITE + player.getUniqueId() + ChatColor.GOLD
                + "\nLocation: " + ChatColor.WHITE + player.getLocation() + ChatColor.GOLD
                + "\nGamemode: " + ChatColor.WHITE + player.getGameMode());
    }
}
