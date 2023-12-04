package com.foxcqrn.bleef.listener;

import com.foxcqrn.bleef.Bleef;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityUnleashEvent;

import java.util.Optional;

// Credit to Leasher
// https://github.com/hazae41/mc-leasher-bukkit/blob/master/src/main/java/hazae41/leasher/Plugin.java
public class LeashListener implements Listener {
    @EventHandler(priority = EventPriority.NORMAL)
    public void onUnleash(EntityUnleashEvent e){
        if(e.getReason() != EntityUnleashEvent.UnleashReason.DISTANCE) return;
        if(!(e.getEntity() instanceof LivingEntity)) return;
        LivingEntity living = (LivingEntity) e.getEntity();
        Entity holder = living.getLeashHolder();

        Bukkit.getServer().getScheduler().runTask(Bleef.plugin, task -> {
            Optional<Item> lead = living.getNearbyEntities(15.0, 15.0, 15.0).stream()
                    .filter(entity -> entity instanceof Item)
                    .map(entity -> (Item) entity)
                    .filter(item -> item.getItemStack().getType() == Material.LEAD)
                    .findFirst();

            if(!lead.isPresent()) return;
            lead.get().remove();

            living.teleport(holder);
            living.setLeashHolder(holder);
        });
    }
}
