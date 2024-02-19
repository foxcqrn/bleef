package com.foxcqrn.bleef.commands;

import com.foxcqrn.bleef.Bleef;
import com.foxcqrn.bleef.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class CommandAddBorder implements CommandExecutor {
    public CommandAddBorder(Bleef plugin) {
        Bleef.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
        if (PluginUtil.isCreative) {
            sender.sendMessage(PluginUtil.ErrWrongServer);
            return true;
        }
        if (!sender.hasPermission("bleef.mapdraw")) {
            sender.sendMessage(PluginUtil.ErrNoPerm);
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /addborder <name>");
            return true;
        }

        String name = String.join(" ", args);
        String id = String.join("_", args).toLowerCase();
        Bukkit.dispatchCommand(sender, "dmarker addarea set:countries weight:2 opacity:0.5 fillopacity:0 label:\"" + name + "\" id:" + id + " color:000000");
        sender.sendMessage(ChatColor.GREEN + "Created border.");
        return true;
    }
}
