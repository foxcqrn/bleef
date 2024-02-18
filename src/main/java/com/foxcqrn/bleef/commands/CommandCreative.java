package com.foxcqrn.bleef.commands;

import com.foxcqrn.bleef.Bleef;
import com.foxcqrn.bleef.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandCreative implements CommandExecutor {
    private final Bleef plugin;

    public CommandCreative(Bleef plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(PluginUtil.ErrNoConsole);
            return true;
        }

        sender.sendMessage(ChatColor.GREEN + "Teleporting to creative world...");
        Bukkit.dispatchCommand(sender, "server creative");

        return true;
    }

}