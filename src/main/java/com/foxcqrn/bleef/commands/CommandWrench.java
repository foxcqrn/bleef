package com.foxcqrn.bleef.commands;

import com.foxcqrn.bleef.Items;
import com.foxcqrn.bleef.PluginUtil;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.executors.CommandArguments;

public class CommandWrench {
    public static void onCommand(CommandSender sender, CommandArguments args) {
        Player player = (Player) sender;
        if (player.getInventory().firstEmpty() == -1) {
            sender.sendMessage(ChatColor.RED + "Your inventory is full!");
            return;
        }

        player.getInventory().addItem(Items.getWrenchItem());
        sender.sendMessage(
                ChatColor.YELLOW + "You got a wrench! Right-click on a block to rotate it.");
    }
}