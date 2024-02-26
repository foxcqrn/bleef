package com.foxcqrn.bleef.commands;

import com.foxcqrn.bleef.Bleef;
import com.foxcqrn.bleef.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class CommandTeleport implements CommandExecutor {
    public CommandTeleport(Bleef plugin) {
        Bleef.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
        if (sender.isOp()) {
            String command = String.join(" ", args);
            Bukkit.dispatchCommand(sender, "minecraft:tp " + command);
            return true;
        }
        if (!PluginUtil.isCreative) {
            sender.sendMessage(PluginUtil.ErrWrongServer);
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /teleport <username>");
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(PluginUtil.ErrNoPlayer);
            return true;
        }

        Player player = (Player) sender;
        sender.sendMessage(ChatColor.GREEN + "Teleporting to " + target.getDisplayName());
        target.sendMessage(player.getDisplayName() + ChatColor.GOLD + " is teleporting to you!");
        if (target.isFlying()) player.setFlying(true);
        player.teleport(target.getLocation());

        return true;
    }
}
