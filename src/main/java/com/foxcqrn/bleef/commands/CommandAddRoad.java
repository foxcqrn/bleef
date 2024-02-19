package com.foxcqrn.bleef.commands;

import com.foxcqrn.bleef.Bleef;
import com.foxcqrn.bleef.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class CommandAddRoad implements CommandExecutor {
    public CommandAddRoad(Bleef plugin) {
        Bleef.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
        if (PluginUtil.isCreative) {
            sender.sendMessage(PluginUtil.ErrWrongServer);
            return true;
        }
        if (!sender.isOp()) {
            sender.sendMessage(PluginUtil.ErrNoPerm);
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /addroad <normal|highway> <name>");
            return true;
        }

        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        String id = String.join("_", Arrays.copyOfRange(args, 1, args.length)).toLowerCase();
        String color = args[0].equals("highway") ? PluginUtil.HighwayColor : PluginUtil.RoadColor;
        Bukkit.dispatchCommand(sender, "dmarker addline set:roads weight:3 opacity:0.6 label:\"" + name + "\" id:" + id + " color:" + color);
        sender.sendMessage(ChatColor.GREEN + "Created road.");
        return true;
    }
}
