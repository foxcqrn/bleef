package com.foxcqrn.bleef.commands;

import com.foxcqrn.bleef.Bleef;
import com.foxcqrn.bleef.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSurvival implements CommandExecutor {
    private final Bleef plugin;

    public CommandSurvival(Bleef plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(PluginUtil.ErrNoConsole);
            return true;
        }

        Player player = (Player) sender;
        if (!PluginUtil.isCreative(player)) {
            sender.sendMessage(PluginUtil.ErrWrongWorld);
            return true;
        }
        sender.sendMessage(ChatColor.GREEN + "Teleporting to survival world...");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvtp " + sender.getName().toLowerCase() + " world");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gamemode survival " + sender.getName().toLowerCase());

        return true;
    }

}