package com.foxcqrn.bleef.commands;

import com.foxcqrn.bleef.Bleef;
import com.foxcqrn.bleef.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHouseMarker implements CommandExecutor {
    public CommandHouseMarker(Bleef plugin) {
        Bleef.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
        Player player = (Player) sender;
        if (PluginUtil.isCreative(player)) {
            sender.sendMessage(PluginUtil.ErrWrongWorld);
            return true;
        }
        String name = sender.getName();
        if (sender.isOp() && args.length == 2) name = args[1];
        Location location = player.getLocation();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /housemarker <create|delete>");
        } else if (args[0].equalsIgnoreCase("create")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dmarker add set:houses id:" + name.toLowerCase() + " label:\"" + name + "'s House\" x:" + x + " y:" + y + " z:" + z + " world:" + location.getWorld().getName());
            sender.sendMessage(ChatColor.GREEN + "Marker created. Run /housemarker delete to delete the marker.");
        } else if (args[0].equalsIgnoreCase("delete")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dmarker delete set:houses id:" + name.toLowerCase());
            sender.sendMessage(ChatColor.RED + "Marker deleted. Run /housemarker create to make a new marker.");
        }
        return true;
    }
}
