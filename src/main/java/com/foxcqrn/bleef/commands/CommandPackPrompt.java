package com.foxcqrn.bleef.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import dev.jorel.commandapi.executors.CommandArguments;

import static com.foxcqrn.bleef.Bleef.plugin;

public class CommandPackPrompt {
    public static void onCommand(CommandSender sender, CommandArguments args) {
        Player player = (Player) sender;
        String mode = (String) args.get("mode");
        assert mode != null;
        boolean isYtDl = mode.equals("external");
        @SuppressWarnings("unchecked")
        Collection<Entity> target = (Collection<Entity>) args.get("target");
        AtomicReference<String> urlTarget = new AtomicReference<>((String) args.get("url"));

        Runnable run = () -> {
            UUID id = UUID.randomUUID();

            URL url;
            byte[] hash = new byte[20];
            try {
                assert urlTarget.get() != null;
                url = new URL(isYtDl ? ("https://packs.dumbserg.al/create?url=" +
                        URLEncoder.encode(urlTarget.get(), "UTF-8")) : urlTarget.get());
                Logger.getLogger("Bleef").info("Requesting: " + url);
            } catch (MalformedURLException e) {
                sender.sendMessage(ChatColor.RED + "Invalid URL!");
                return;
            } catch (UnsupportedEncodingException e) {
                sender.sendMessage(ChatColor.RED + "Can't encode query params!");
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
                conn.setRequestMethod(isYtDl ? "POST" : "GET");
            } catch (ProtocolException e) {
                // should never get here
                sender.sendMessage(ChatColor.RED + "Cannot set method!");
                return;
            }
            try (InputStream stream = conn.getInputStream()) {
                if (isYtDl) {
                    sender.sendMessage(ChatColor.GREEN + "Preparing resource pack, please wait...");
                    HttpURLConnection conn2;
                    try {
                        String srcUrl = "https://packs.dumbserg.al/packs/" + new String(stream.readAllBytes(), StandardCharsets.UTF_8) + ".zip";
                        URL url2 = new URL(srcUrl);
                        Logger.getLogger("Bleef").info("Retrieving pack from " + url2);
                        urlTarget.set(srcUrl);
                        conn2 = (HttpURLConnection) url2.openConnection();
                    } catch (IOException e) {
                        sender.sendMessage(ChatColor.RED + "Unable to open URL!");
                        return;
                    }
                    try {
                        conn2.setRequestMethod("GET");
                    } catch (ProtocolException e) {
                        // should never get here
                        sender.sendMessage(ChatColor.RED + "Cannot set method to GET!");
                    }
                    try (InputStream stream2 = conn2.getInputStream()) {
                        MessageDigest md = MessageDigest.getInstance("SHA-1");
                        hash = md.digest(stream2.readAllBytes());
                    } catch (IOException e) {
                        sender.sendMessage(ChatColor.RED + "Unable to open URL!");
                        return;
                    }
                } else {
                    MessageDigest md = MessageDigest.getInstance("SHA-1");
                    hash = md.digest(stream.readAllBytes());
                }

            } catch (IOException e) {
                sender.sendMessage(ChatColor.RED + "Failed to read from connection!");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            byte[] finalHash = hash;
            assert target != null;
            target.forEach(p
                    -> Bukkit.getScheduler().runTask(plugin, () -> ((Player) p)
                    .addResourcePack(id, urlTarget.get(), finalHash,
                            "An administrator has requested a resource pack to be added. Would you like to add it?",
                            false)));
            player.sendMessage("Requested resource pack with ID", id.toString());
        };
        new Thread(run).start();
    }
}