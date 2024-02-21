package com.foxcqrn.bleef.commands;

import com.foxcqrn.bleef.Bleef;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandMap implements CommandExecutor {
    private final Bleef plugin;

    public CommandMap(Bleef plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {

        sender.sendMessage(ChatColor.GREEN + "The world map can be viewed here: https://map.backslash.dumbserg.al/");

        return true;
    }

}