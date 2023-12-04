package com.foxcqrn.bleef.commands;

import com.foxcqrn.bleef.Bleef;
import com.foxcqrn.bleef.PluginUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class CommandNickname implements CommandExecutor {
    private final Bleef plugin;

    public CommandNickname(Bleef plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(PluginUtil.ErrNoConsole);
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /nickname <name>");
            return true;
        }

        FileConfiguration config = plugin.getConfig();
        String nickname = String.join(" ", args);
        config.set("players." + player.getUniqueId() + ".nickname", nickname);
        plugin.saveConfig();
        sender.sendMessage(ChatColor.GREEN + "Set nickname to " + nickname);
        PluginUtil.updateName(player);
        return true;
    }
}