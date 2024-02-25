package com.foxcqrn.bleef.commands;

import com.foxcqrn.bleef.Bleef;
import com.foxcqrn.bleef.protos.SequenceProto;
import com.foxcqrn.bleef.util.SequencePlayer;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.jsoup.Jsoup;

import com.foxcqrn.bleef.protos.SequenceProto.*;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class CommandSequence implements CommandExecutor {
    HashSet<UUID> sequencePlayers = new HashSet<>();

    public CommandSequence(Bleef plugin) {
        Bleef.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
        Player player = (Player) sender;

        if (sequencePlayers.contains(player.getUniqueId())) {
            sequencePlayers.remove(player.getUniqueId());
            sender.sendMessage(ChatColor.AQUA + "Stopping sequence.");
            return true;
        }
        int sequenceId;
        try {
            sequenceId = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Sequence ID must be an integer.");
            return true;
        }

        sequencePlayers.add(player.getUniqueId());

        SequencePlayer sequencePlayer = new SequencePlayer();

        Thread thread = sequencePlayer.getThread(player, sequenceId, () -> {
            if (!sequencePlayers.contains(player.getUniqueId())) {
                sequencePlayer.stop();
            }
            return null;
        }, () -> {
            sequencePlayers.remove(player.getUniqueId());
            sender.sendMessage(ChatColor.AQUA + "Sequence ended.");
            return null;
        });
        thread.start();

        return true;
    }


}
