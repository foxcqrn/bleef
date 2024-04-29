package com.foxcqrn.bleef.commands;

import com.foxcqrn.bleef.Bleef;
import com.foxcqrn.bleef.PluginUtil;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.executors.CommandArguments;

public class CommandSpeed {
    public static void onCommand(CommandSender sender, CommandArguments args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(PluginUtil.ErrNoConsole);
            return;
        }

        if (!sender.isOp() && !PluginUtil.isCreative) {
            sender.sendMessage(PluginUtil.ErrWrongServer);
            return;
        }
        Player player = (Player) sender;
        float speed = player.isFlying() ? 1f : 2f;
        String action = (String) args.get("action");
        Float value = (Float) args.get("value");
        assert action != null;
        assert value != null;
        if (!action.equals("reset")) {
            speed = value;
        }
        if (sender.isOp() && speed > 10f || !sender.isOp() && speed > 5f || speed < 0f) {
            sender.sendMessage(ChatColor.RED + "Speed must be between 0 and 5.");
            return;
        }

        if (player.isFlying()) {
            player.setFlySpeed(speed / 10f);
            sender.sendMessage(ChatColor.GREEN + "Set flying speed to " + ChatColor.YELLOW + speed);
        } else {
            player.setWalkSpeed(speed / 10f);
            sender.sendMessage(
                    ChatColor.GREEN + "Set walking speed to " + ChatColor.YELLOW + speed);
        }
    }
}