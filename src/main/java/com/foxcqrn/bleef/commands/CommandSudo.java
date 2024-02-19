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

public class CommandSudo implements CommandExecutor {
    public CommandSudo(Bleef plugin) {
        Bleef.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(PluginUtil.ErrNoPerm);
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /sudo <username> </command|message>");
            return true;
        }
        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "That player doesn't exist");
            return true;
        }
        String command = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        if (command.startsWith("/")) {
            Bukkit.dispatchCommand(player, command.substring(1));
        } else {
            player.chat(command);
        }

        return true;
    }
}
