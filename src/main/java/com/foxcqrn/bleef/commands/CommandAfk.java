package com.foxcqrn.bleef.commands;

import com.foxcqrn.bleef.PluginUtil;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.executors.CommandArguments;

public class CommandAfk {
    public static void onCommand(CommandSender sender, CommandArguments args) {
        Player player = (Player) sender;
        PluginUtil.setAFK(player, !PluginUtil.isAFK(player));
    }
}
