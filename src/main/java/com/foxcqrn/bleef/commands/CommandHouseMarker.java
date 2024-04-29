package com.foxcqrn.bleef.commands;

import com.foxcqrn.bleef.PluginUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.executors.CommandArguments;

import java.util.Objects;

public class CommandHouseMarker {
    public static void onCommand(CommandSender sender, CommandArguments args) {
        if (PluginUtil.isCreative) {
            sender.sendMessage(PluginUtil.ErrWrongServer);
            return;
        }
        Player player = (Player) sender;
        String name = sender.getName();
        if (sender.isOp()) {
            name = (String) args.get("name");
        } else {
            if (args.get("name") != null) {
                sender.sendMessage(ChatColor.RED + "You must be an operator to set a custom name!");
            }
        }
        Location location = player.getLocation();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        String action = (String) args.get("action");
        assert action != null;
        if (action.equalsIgnoreCase("create")) {
            assert name != null;
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    "dmarker add set:houses id:" + name.toLowerCase() + " label:\"" + name
                            + "'s House\" x:" + x + " y:" + y + " z:" + z
                            + " world:" + Objects.requireNonNull(location.getWorld()).getName());
            sender.sendMessage(ChatColor.GREEN
                    + "Marker created. Run /housemarker delete to delete the marker.");
        } else if (action.equalsIgnoreCase("delete")) {
            assert name != null;
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    "dmarker delete set:houses id:" + name.toLowerCase());
            sender.sendMessage(ChatColor.RED
                    + "Marker deleted. Run /housemarker create to make a new marker.");
        }
    }
}
