package com.foxcqrn.bleef.commands;

import static com.foxcqrn.bleef.Bleef.plugin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.*;

import dev.jorel.commandapi.executors.CommandArguments;

public class CommandAddHorse {
    public static void onCommand(CommandSender sender, CommandArguments args) {
        Player player = (Player) sender;
        Entity vehicle = player.getVehicle();
        if (vehicle == null) {
            player.sendMessage(ChatColor.RED + "You must be riding an animal to run this command!");
            return;
        }
        EntityType type = vehicle.getType();
        if (type != EntityType.HORSE && type != EntityType.MULE && type != EntityType.CAMEL
                && type != EntityType.PIG) {
            player.sendMessage(ChatColor.RED + "You can't register that entity as a vehicle!");
            return;
        }
        UUID playerUUID = player.getUniqueId();
        UUID vehicleUUID = vehicle.getUniqueId();
        FileConfiguration config = plugin.getConfig();
        if (config.get(String.format("players.%s.horse", playerUUID)) != null) {
            player.sendMessage(ChatColor.RED + "You already have a vehicle registered!");
            return;
        }
        config.set(String.format("players.%s.horse", playerUUID), vehicleUUID.toString());
        plugin.saveConfig();
        player.sendMessage(ChatColor.GREEN
                + String.format("Registered %s to %s.", vehicle.getName(), player.getName()));
    }
}
