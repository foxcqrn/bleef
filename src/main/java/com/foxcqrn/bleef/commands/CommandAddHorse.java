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

import java.util.*;

public class CommandAddHorse implements CommandExecutor {
    private final Bleef plugin;

    public CommandAddHorse(Bleef plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String string, String[] args) {
        Player player = (Player) sender;
        Entity vehicle = player.getVehicle();
        if (vehicle == null) {
            player.sendMessage(ChatColor.RED + "You must be riding a horse, camel, mule, or pig to run this command!");
            return true;
        }
        EntityType type = vehicle.getType();
        if (type != EntityType.HORSE && type != EntityType.MULE && type != EntityType.CAMEL && type != EntityType.PIG) {
            player.sendMessage(ChatColor.RED + "You can't register that entity as a vehicle!");
            return true;
        }
        UUID playerUUID = player.getUniqueId();
        UUID vehicleUUID = vehicle.getUniqueId();
        FileConfiguration config = plugin.getConfig();
        if (config.get(String.format("players.%s.horse", playerUUID)) != null) {
            player.sendMessage(ChatColor.RED + "You already have a vehicle registered!");
            return true;
        }
        config.set(String.format("players.%s.horse", playerUUID), vehicleUUID.toString());
        plugin.saveConfig();
        player.sendMessage(ChatColor.GREEN + String.format("Registered %s to %s.", vehicle.getName(), player.getName()));
        return true;
    }
}
