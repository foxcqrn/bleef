package com.foxcqrn.bleef.commands;

import com.foxcqrn.bleef.Bleef;
import com.foxcqrn.bleef.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandPlayerStats implements CommandExecutor {
    public CommandPlayerStats(Bleef plugin) {
        Bleef.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(PluginUtil.ErrNoPerm);
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /playerstats <username>");
        } else {
            Player player = Bukkit.getPlayer(args[0]);
            if (player != null) {
                sender.sendMessage(ChatColor.GOLD + "Showing information for player \"" + player.getName() + "\":");
                sender.sendMessage(
                             ChatColor.GOLD + "\nNickname: " + PluginUtil.getNickname(player) +
                                ChatColor.GOLD + "\nHealth: " + ChatColor.WHITE + player.getHealth() +
                                ChatColor.GOLD + "\nFood: " + ChatColor.WHITE + player.getFoodLevel() +
                                ChatColor.GOLD + "\nIP Address: " + ChatColor.WHITE + player.getAddress().toString().trim() +
                                ChatColor.GOLD + "\nUUID: " + ChatColor.WHITE + player.getUniqueId() +
                                ChatColor.GOLD + "\nLocation: " + ChatColor.WHITE + player.getLocation() +
                                ChatColor.GOLD + "\nGamemode: " + ChatColor.WHITE + player.getGameMode()
                );
            } else {
                sender.sendMessage(ChatColor.RED + "That is not a valid player.");
            }
        }
        return true;
    }
}
