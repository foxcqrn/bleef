package com.foxcqrn.bleef.commands;

import com.foxcqrn.bleef.Bleef;
import com.foxcqrn.bleef.Items;
import com.foxcqrn.bleef.PluginUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandWrench implements CommandExecutor {
    private final Bleef plugin;

    public CommandWrench(Bleef plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {

        if (!sender.hasPermission("bleef.wrench.give")) {
            sender.sendMessage(PluginUtil.ErrNoPerm);
            return true;
        }
        Player player = (Player) sender;
        if (player.getInventory().firstEmpty() == -1) {
            sender.sendMessage(ChatColor.RED + "Your inventory is full!");
            return true;
        }

        player.getInventory().addItem(Items.getWrenchItem());
        sender.sendMessage(ChatColor.YELLOW + "You got a wrench! Right click on a block to rotate it.");

        return true;
    }

}