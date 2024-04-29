package com.foxcqrn.bleef.commands;

import static com.foxcqrn.bleef.Bleef.plugin;

import com.foxcqrn.bleef.PluginUtil;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.executors.CommandArguments;

public class CommandNickname {
    public static void onCommand(CommandSender sender, CommandArguments args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(PluginUtil.ErrNoConsole);
            return;
        }

        Player player = (Player) sender;
        FileConfiguration config = plugin.getConfig();
        String nickname = (String) args.get("name");
        config.set("players." + player.getUniqueId() + ".nickname", nickname);
        plugin.saveConfig();
        sender.sendMessage(ChatColor.GREEN + "Set nickname to " + nickname);
        PluginUtil.updateName(player);
    }
}