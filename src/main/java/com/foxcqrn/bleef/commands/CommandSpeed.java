package com.foxcqrn.bleef.commands;

import com.foxcqrn.bleef.Bleef;
import com.foxcqrn.bleef.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSpeed implements CommandExecutor {
    private final Bleef plugin;

    public CommandSpeed(Bleef plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(PluginUtil.ErrNoConsole);
            return true;
        }
        if (!sender.isOp() || !PluginUtil.isCreative) {
            sender.sendMessage(PluginUtil.ErrWrongServer);
            return true;
        }
        if (args.length == 0 || args[0] == "") {
            sender.sendMessage(ChatColor.RED + "Usage: /speed <value>");
            return true;
        }
        float speed = Float.parseFloat(args[0]);
        if (speed > 5 || speed < 1) {
            sender.sendMessage(ChatColor.RED + "Speed must be between 1 and 10.");
            return true;
        }

        Player player = (Player) sender;
        if (player.isFlying()) {
            player.setFlySpeed(speed);
            sender.sendMessage(ChatColor.GREEN + "Set flying speed to " + ChatColor.YELLOW + speed);
        } else {
            player.setWalkSpeed(speed);
            sender.sendMessage(ChatColor.GREEN + "Set walking speed to " + ChatColor.YELLOW + speed);
        }

        return true;
    }

}