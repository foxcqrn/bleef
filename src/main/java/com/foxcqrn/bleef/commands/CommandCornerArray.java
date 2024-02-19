package com.foxcqrn.bleef.commands;

import com.foxcqrn.bleef.Bleef;
import com.foxcqrn.bleef.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandCornerArray implements CommandExecutor {
    public CommandCornerArray(Bleef plugin) {
        Bleef.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
        if (PluginUtil.isCreative) {
            sender.sendMessage(PluginUtil.ErrWrongServer);
            return true;
        }
        Player player = (Player) sender;
        Location location = player.getLocation();
        int y = location.getBlockY();
        String world = location.getWorld().getName();
        if (!sender.isOp()) {
            sender.sendMessage(PluginUtil.ErrNoPerm);
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /cornerarray <x,z ...>");
            return true;
        }
        for (String arg : args) {
            String x = arg.split(",")[0];
            String z = arg.split(",")[1];
            Bukkit.dispatchCommand(sender, "dmarker addcorner "+x+" "+y+" "+z+" "+world);
        }
        sender.sendMessage(ChatColor.GREEN + "Added " + args.length + " corners.");
        return true;
    }
}
