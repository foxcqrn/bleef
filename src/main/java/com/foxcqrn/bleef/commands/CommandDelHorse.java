package com.foxcqrn.bleef.commands;

import com.foxcqrn.bleef.Bleef;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class CommandDelHorse implements CommandExecutor {
    private final Bleef plugin;

    public CommandDelHorse(Bleef plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String string, String[] args) {
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        FileConfiguration config = plugin.getConfig();
        if (config.get(String.format("players.%s.horse", playerUUID)) == null) {
            player.sendMessage(ChatColor.RED + "You don't have a vehicle registered!");
            return true;
        }
        config.set(String.format("players.%s.horse", playerUUID), null);
        plugin.saveConfig();
        player.sendMessage(ChatColor.GREEN + String.format("Removed vehicle for %s.", player.getName()));
        return true;
    }
}
