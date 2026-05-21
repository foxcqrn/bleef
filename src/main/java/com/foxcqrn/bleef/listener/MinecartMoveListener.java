package com.foxcqrn.bleef.listener;

import com.foxcqrn.bleef.Bleef;
import com.foxcqrn.bleef.ForcedChunkManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import java.util.*;

public class MinecartMoveListener implements Listener {
    @EventHandler
    @SuppressWarnings("unused")
    public void onVehicleMove(VehicleMoveEvent event) {
        Vehicle v = event.getVehicle();
        if (v.getType() == EntityType.CHEST_MINECART) {
            ForcedChunkManager forcedChunkManager = Bleef.plugin.getForcedChunkManager();

            List<Map.Entry<Integer, Integer>> previouslyTracked = new ArrayList<>(forcedChunkManager
                    .getEntityLoadedTrackedChunks(v.getUniqueId()));

            Location loc = v.getLocation();

            Integer centerChunkX = loc.getChunk().getX();
            Integer centerChunkZ = loc.getChunk().getZ();

            if (!forcedChunkManager.isChunkLoadedByEntity(v.getUniqueId(), centerChunkX, centerChunkZ)) {
                Objects.requireNonNull(loc.getWorld()).addPluginChunkTicket(centerChunkX, centerChunkZ, Bleef.plugin);

                forcedChunkManager.entityLoadChunk(v.getUniqueId(), centerChunkX, centerChunkZ);
            }

            previouslyTracked.forEach(chunkPos -> {

                Integer chunkX = chunkPos.getKey();
                Integer chunkZ = chunkPos.getValue();

                if (!(Objects.equals(chunkX, centerChunkX) && Objects.equals(chunkZ, centerChunkZ))) {
                    Objects.requireNonNull(loc.getWorld()).removePluginChunkTicket(chunkX, chunkZ, Bleef.plugin);

                    forcedChunkManager.entityUnloadChunk(v.getUniqueId(), chunkX, chunkZ);
                }
            });
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        UUID entityId = event.getVehicle().getUniqueId();
        World world = event.getVehicle().getWorld();
        ForcedChunkManager forcedChunkManager = Bleef.plugin.getForcedChunkManager();
        forcedChunkManager.getEntityLoadedTrackedChunks(entityId).forEach(c ->
                world.removePluginChunkTicket(c.getKey(), c.getValue(), Bleef.plugin)
        );
        forcedChunkManager.clearChunksForEntity(entityId);
    }
}
