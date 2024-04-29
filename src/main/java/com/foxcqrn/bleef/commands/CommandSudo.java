package com.foxcqrn.bleef.commands;

import com.foxcqrn.bleef.PluginUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

import dev.jorel.commandapi.executors.CommandArguments;

public class CommandSudo {
    public static void onCommand(CommandSender sender, CommandArguments args) {
        Player player = (Player) args.get("player");
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "That player doesn't exist");
            return;
        }
        String command = (String) args.get("command");
        assert command != null;
        if (command.startsWith("/")) {
            Bukkit.dispatchCommand(player, command.substring(1));
        } else {
            player.chat(command);
        }
    }
}
