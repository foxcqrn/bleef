package com.foxcqrn.bleef.listener;

import com.foxcqrn.bleef.Bleef;
import com.foxcqrn.bleef.PluginUtil;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class AfkListener implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (PluginUtil.isAFK(player)) {
            PluginUtil.AFKList.remove(player.getName());
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (PluginUtil.isAFK(player)) {
            Location preloc = event.getFrom();
            Location postloc = event.getTo();
            if (preloc.getBlockX() != postloc.getBlockX()
                    || preloc.getBlockY() != postloc.getBlockY()
                    || preloc.getBlockZ() != postloc.getBlockZ()) {
                PluginUtil.setAFK(player, false);
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (PluginUtil.isAFK(player)) {
            Bukkit.getScheduler().runTask(Bleef.plugin, () -> PluginUtil.setAFK(player, false));
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (PluginUtil.isAFK(player)) {
            if (!event.getMessage().equals("/afk")) PluginUtil.setAFK(player, false);
        }
    }
}
