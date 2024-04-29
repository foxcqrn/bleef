package com.foxcqrn.bleef.commands;

import com.foxcqrn.bleef.PluginUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.executors.CommandArguments;

public class CommandAddBorder {
    public static void onCommand(CommandSender sender, CommandArguments args) {
        if (PluginUtil.isCreative) {
            sender.sendMessage(PluginUtil.ErrWrongServer);
            return;
        }

        String name = (String) args.get("name");
        assert name != null;
        String id = name.replace(" ", "_").toLowerCase();
        Bukkit.dispatchCommand(sender,
                "dmarker addarea set:countries weight:2 opacity:0.5 fillopacity:0 label:\"" + name
                        + "\" id:" + id + " color:000000");
        sender.sendMessage(ChatColor.GREEN + "Created border.");
    }
}
