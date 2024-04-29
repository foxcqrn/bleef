package com.foxcqrn.bleef.commands;

import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Formatter;
import java.util.UUID;

public class CommandPackPrompt {
    public static void onCommand(CommandSender sender, CommandArguments args) {
        Player player = (Player) sender;
        @SuppressWarnings("unchecked")
        Collection<Entity> target = (Collection<Entity>) args.get("target");
        String urlTarget = (String) args.get("url");

        UUID id = UUID.randomUUID();

        URL url;
        byte[] hash = new byte[20];
        try {
            assert urlTarget != null;
            url = new URL(urlTarget);
        } catch (MalformedURLException e) {
            sender.sendMessage(ChatColor.RED + "Invalid URL!");
            return;
        }
        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Unable to open URL!");
            return;
        }
        try {
            conn.setRequestMethod("GET");
        } catch (ProtocolException e) {
            // should never get here
            sender.sendMessage(ChatColor.RED + "Cannot set method to GET!");
        }
        try (InputStream stream = conn.getInputStream()) {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            hash = md.digest(stream.readAllBytes());
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Failed to read from connection!");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        byte[] finalHash = hash;
        assert target != null;
        target.forEach(p -> ((Player) p).addResourcePack(id, urlTarget, finalHash,
                "An administrator has requested a resource pack to be added. Would you like to add it?", false));
        player.sendMessage("Requested resource pack with ID", id.toString());
    }
}