package com.foxcqrn.bleef.commands;

import com.foxcqrn.bleef.PluginUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Objects;

import dev.jorel.commandapi.executors.CommandArguments;

public class CommandAddRoad {
    public static void onCommand(CommandSender sender, CommandArguments args) {
        if (PluginUtil.isCreative) {
            sender.sendMessage(PluginUtil.ErrWrongServer);
            return;
        }

        String name = (String) args.get("name");
        assert name != null;
        String id = name.replace(" ", "_").replace("'", "_").replace(",", "_").toLowerCase();
        String color = Objects.equals(args.get("type"), "highway") ? PluginUtil.HighwayColor
                                                                   : PluginUtil.RoadColor;
        Bukkit.dispatchCommand(sender,
                "dmarker addline set:roads weight:3 opacity:0.6 label:\"" + name + "\" id:" + id
                        + " color:" + color);
        sender.sendMessage(ChatColor.GREEN + "Created road.");
    }
}
