package com.foxcqrn.bleef.listener;

import com.foxcqrn.bleef.Bleef;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityUnleashEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

// Credit to Leasher
// https://github.com/hazae41/mc-leasher-bukkit/blob/master/src/main/java/hazae41/leasher/Plugin.java
public class LeashListener implements Listener {
    private static final int SEARCH_RADIUS = 12;

    private static final Set<Material> HAZARDS = EnumSet.of(
            Material.FIRE, Material.SOUL_FIRE, Material.CAMPFIRE, Material.SOUL_CAMPFIRE,
            Material.MAGMA_BLOCK, Material.CACTUS, Material.SWEET_BERRY_BUSH,
            Material.WITHER_ROSE, Material.COBWEB, Material.POWDER_SNOW);

    private static final List<int[]> OFFSETS = new ArrayList<>();

    static {
        for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++) {
            for (int dy = -SEARCH_RADIUS; dy <= SEARCH_RADIUS; dy++) {
                for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++) {
                    if (dx * dx + dy * dy + dz * dz <= SEARCH_RADIUS * SEARCH_RADIUS) {
                        OFFSETS.add(new int[] {dx, dy, dz});
                    }
                }
            }
        }
        OFFSETS.sort(Comparator.comparingInt(o -> o[0] * o[0] + o[1] * o[1] + o[2] * o[2]));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    @SuppressWarnings("unused")
    public void onUnleash(EntityUnleashEvent e) {
        if (e.getReason() != EntityUnleashEvent.UnleashReason.DISTANCE) return;
        if (!(e.getEntity() instanceof LivingEntity living)) return;
        Entity holder = living.getLeashHolder();

        Bukkit.getServer().getScheduler().runTask(Bleef.plugin, task -> {
            if (!holder.isValid() || !living.isValid()) return;

            Optional<Item> lead =
                    living.getNearbyEntities(15.0, 15.0, 15.0)
                            .stream()
                            .filter(entity -> entity instanceof Item)
                            .map(entity -> (Item) entity)
                            .filter(item -> item.getItemStack().getType() == Material.LEAD)
                            .findFirst();

            if (lead.isEmpty()) return;

            Location spot = findSafeSpot(living, holder);
            if (spot == null) return;

            living.eject();
            if (!living.teleport(spot)) return;

            lead.get().remove();
            living.setLeashHolder(holder);
        });
    }

    private static Location findSafeSpot(LivingEntity living, Entity holder) {
        World world = holder.getWorld();
        if (living.getWorld() != world) return null;

        double width = living.getWidth();
        double top = living.getHeight();

        Location origin = holder.getLocation();
        for (int[] offset : OFFSETS) {
            int x = origin.getBlockX() + offset[0];
            int y = origin.getBlockY() + offset[1];
            int z = origin.getBlockZ() + offset[2];
            if (isSafe(world, x, y, z, width, top)) {
                Location current = living.getLocation();
                return new Location(world, x + 0.5, y, z + 0.5, current.getYaw(), current.getPitch());
            }
        }
        return null;
    }

    private static boolean isSafe(World world, int x, int y, int z, double width, double height) {
        if (y - 1 < world.getMinHeight() || y + height >= world.getMaxHeight()) return false;

        if (!world.isChunkLoaded(x >> 4, z >> 4)) return false;
        Block floor = world.getBlockAt(x, y - 1, z);
        if (!floor.isSolid() || HAZARDS.contains(floor.getType())) return false;

        int minX = (int) Math.floor(x + 0.5 - width / 2 + 0.01);
        int maxX = (int) Math.floor(x + 0.5 + width / 2 - 0.01);
        int minZ = (int) Math.floor(z + 0.5 - width / 2 + 0.01);
        int maxZ = (int) Math.floor(z + 0.5 + width / 2 - 0.01);
        int maxY = (int) Math.floor(y + height - 0.01);

        for (int bx = minX; bx <= maxX; bx++) {
            for (int bz = minZ; bz <= maxZ; bz++) {
                if (!world.isChunkLoaded(bx >> 4, bz >> 4)) return false;
                for (int by = y; by <= maxY; by++) {
                    Block block = world.getBlockAt(bx, by, bz);
                    if (!block.isPassable() || block.isLiquid() || HAZARDS.contains(block.getType())) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}