package com.foxcqrn.bleef.listener;

import com.foxcqrn.bleef.Bleef;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.foxcqrn.bleef.Bleef.plugin;

public class CompassListener implements Listener {
    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent e) {
        int slot = e.getNewSlot();
        Player player = e.getPlayer();
        ItemStack item = player.getInventory().getItem(slot);
        if (item == null) {
            return;
        }
        // can't use isSimilar here due to the lodestone metadata
        if (item.getType() == Material.COMPASS &&
                Objects.equals(Objects.requireNonNull(item.getItemMeta())
                        .getPersistentDataContainer()
                        .get(
                                new NamespacedKey(plugin, "BleefSpecialType"),
                                PersistentDataType.STRING
                        ), "HORSE_COMPASS"
                )
        ) {
            CompassMeta meta = getCompassMeta(item, player);
            if (meta == null) {
                return;
            }
            item.setItemMeta(meta);
        }
    }

    private static CompassMeta getCompassMeta(ItemStack item, Player player) {
        CompassMeta meta = (CompassMeta) item.getItemMeta();
        assert meta != null;
        meta.setLodestoneTracked(false);
        FileConfiguration config = Bleef.plugin.getConfig();
        String playerVehicle = config.getString(String.format("players.%s.horse", player.getUniqueId()));
        if (playerVehicle == null) {
            return null;
        }
        UUID vehicle = UUID.fromString(playerVehicle);
        List<Entity> entities = player.getWorld().getEntities();
        Entity target = null;
        for (Entity ent : entities) {
            if (ent.getUniqueId().compareTo(vehicle) == 0) {
                target = ent;
                break;
            }
        }
        if (target != null) {
            meta.setLodestone(target.getLocation());
        } else {
            meta.setLodestone(null);
        }
        return meta;
    }
}
