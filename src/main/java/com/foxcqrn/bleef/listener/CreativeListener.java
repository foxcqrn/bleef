package com.foxcqrn.bleef.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPortalEvent;

public class CreativeListener implements Listener {
    // Will only run on creative: true

    @EventHandler
    @SuppressWarnings("unused")
    public void onItemDrop(PlayerDropItemEvent event) {
        event.getItemDrop().remove();
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onPortal(PlayerPortalEvent event) {
        event.setCancelled(true);
    }
}
