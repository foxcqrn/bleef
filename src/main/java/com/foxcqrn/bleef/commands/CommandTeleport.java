package com.foxcqrn.bleef.commands;

import com.foxcqrn.bleef.PluginUtil;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.executors.CommandArguments;

public class CommandTeleport {
    public static void onCommand(CommandSender sender, CommandArguments args) {
        Player target = (Player) args.get("player");
        Player player = (Player) sender;
        if (target == null) {
            sender.sendMessage(PluginUtil.ErrNoPlayer);
            return;
        }
        if (sender.isOp()) {
            player.teleport(target);
            return;
        }
        if (!PluginUtil.isCreative) {
            sender.sendMessage(PluginUtil.ErrWrongServer);
            return;
        }

        sender.sendMessage(ChatColor.GREEN + "Teleporting to " + target.getDisplayName());
        target.sendMessage(player.getDisplayName() + ChatColor.GOLD + " is teleporting to you!");
        if (target.isFlying()) player.setFlying(true);
        player.teleport(target.getLocation());
    }
}
