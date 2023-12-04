package com.foxcqrn.bleef.commands;

import com.foxcqrn.bleef.Bleef;
import com.foxcqrn.bleef.PluginUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandToggleCoords implements CommandExecutor {
    public CommandToggleCoords(Bleef plugin) {
        Bleef.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
        if (PluginUtil.ToggleCoords.contains(sender.getName())) {
            PluginUtil.ToggleCoords.remove(sender.getName());
            sender.sendMessage(ChatColor.GREEN + "You have enabled actionbar coordinates.");
        } else {
            PluginUtil.ToggleCoords.add(sender.getName());
            sender.sendMessage(ChatColor.RED + "You have disabled actionbar coordinates.");
        }
        return true;
    }
}
