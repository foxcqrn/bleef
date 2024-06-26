package com.foxcqrn.bleef.commands;

import com.foxcqrn.bleef.PluginUtil;

import org.bukkit.*;
import org.bukkit.Particle.DustTransition;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;

import dev.jorel.commandapi.executors.CommandArguments;

public class CommandMapLine {
    HashSet<UUID> mappingPlayers = new HashSet<>();
    HashMap<UUID, List<Location>> mappingData = new HashMap<>();

    public CommandMapLine() {}

    public void onCommand(CommandSender sender, @SuppressWarnings("unused") CommandArguments args) {
        if (PluginUtil.isCreative) {
            sender.sendMessage(PluginUtil.ErrWrongServer);
            return;
        }
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        if (mappingPlayers.contains(uuid)) {
            mappingPlayers.remove(player.getUniqueId());
            player.sendMessage(ChatColor.AQUA + "Tracing finished. Adding corners...");
            List<Location> data = mappingData.get(uuid);
            data.forEach((Location loc) -> Bukkit.dispatchCommand(sender,
                    "dmarker addcorner " + loc.getX() + " " + loc.getY() + " " + loc.getZ()
                            + " " + Objects.requireNonNull(loc.getWorld()).getName()));
            player.sendMessage(ChatColor.AQUA + "Adding complete.");
            mappingData.remove(uuid);
            return;
        } else {
            mappingPlayers.add(uuid);
            mappingData.put(uuid, new ArrayList<>());
            player.sendMessage(ChatColor.AQUA + "Tracing started.");
        }

        DustTransition dustTransition =
                new DustTransition(Color.fromRGB(255, 0, 255), Color.fromRGB(0, 255, 255), 1.0f);
        Thread thread = getThread(player, uuid, dustTransition);
        thread.start();
    }

    private Thread getThread(Player player, UUID uuid, DustTransition dustTransition) {
        Runnable tracerRunnable = () -> {
            List<Location> data = mappingData.get(uuid);
            Thread.currentThread().setName(
                    String.format("Mapline Thread for %s", player.getUniqueId()));
            while (mappingPlayers.contains(uuid)) {
                data.add(player.getLocation());
                data.subList(Math.max(data.size() - 101, 0), Math.max(data.size() - 1, 0))
                        .forEach((Location loc) -> Objects.requireNonNull(loc.getWorld())
                                .spawnParticle(Particle.DUST_COLOR_TRANSITION, loc, 50,
                                        dustTransition));
                try {
                    TimeUnit.MILLISECONDS.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        return new Thread(tracerRunnable);
    }
}
