package com.foxcqrn.bleef.commands;

import com.foxcqrn.bleef.PluginUtil;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.executors.CommandArguments;

public class CommandToggleCoords {
    public static void onCommand(CommandSender sender, CommandArguments args) {
        if (PluginUtil.ToggleCoords.contains(sender.getName())) {
            PluginUtil.ToggleCoords.remove(sender.getName());
            sender.sendMessage(ChatColor.GREEN + "You have enabled actionbar coordinates.");
        } else {
            PluginUtil.ToggleCoords.add(sender.getName());
            sender.sendMessage(ChatColor.RED + "You have disabled actionbar coordinates.");
        }
    }
}
