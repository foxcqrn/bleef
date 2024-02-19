package com.foxcqrn.bleef.commands;

import com.foxcqrn.bleef.Bleef;
import com.foxcqrn.bleef.PluginUtil;
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
        if (!sender.isOp() && !PluginUtil.isCreative) {
            sender.sendMessage(PluginUtil.ErrWrongServer);
            return true;
        }
        if (args.length == 0 || args[0].equals("")) {
            sender.sendMessage(ChatColor.RED + "Usage: /speed <value|reset>");
            return true;
        }
        Player player = (Player) sender;
        float speed = player.isFlying() ? 1f : 2f;
        if (!args[0].equals("reset")) {
            try {
                speed = Float.parseFloat(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Speed must be a float.");
                return true;
            }
        }
        if (sender.isOp() && speed > 10f || !sender.isOp() && speed > 5f || speed < 0f) {
            sender.sendMessage(ChatColor.RED + "Speed must be between 0 and 5.");
            return true;
        }

        if (player.isFlying()) {
            player.setFlySpeed(speed/10f);
            sender.sendMessage(ChatColor.GREEN + "Set flying speed to " + ChatColor.YELLOW + speed);
        } else {
            player.setWalkSpeed(speed/10f);
            sender.sendMessage(ChatColor.GREEN + "Set walking speed to " + ChatColor.YELLOW + speed);
        }

        return true;
    }

}