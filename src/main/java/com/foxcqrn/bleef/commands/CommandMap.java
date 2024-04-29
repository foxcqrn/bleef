package com.foxcqrn.bleef.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.executors.CommandArguments;

public class CommandMap {
    public static void onCommand(CommandSender sender, CommandArguments args) {
        sender.sendMessage(ChatColor.GREEN
                + "The world map can be viewed here: https://map.backslash.dumbserg.al/");
    }
}
