package com.foxcqrn.bleef;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ForcedChunkManager {
    ConcurrentHashMap<UUID, List<Map.Entry<Integer, Integer>>> entityLoadedTrackedChunks;

    ForcedChunkManager() {
        entityLoadedTrackedChunks = new ConcurrentHashMap<>();
    }

    public List<Map.Entry<Integer, Integer>> getEntityLoadedTrackedChunks(UUID entityId) {
        return entityLoadedTrackedChunks.getOrDefault(entityId, new ArrayList<>());
    }

    public void entityLoadChunk(UUID entityId, Integer x, Integer z) {
        if (!entityLoadedTrackedChunks.containsKey(entityId)) {
            entityLoadedTrackedChunks.put(entityId, new ArrayList<>());
        }
        List<Map.Entry<Integer, Integer>> chunks = entityLoadedTrackedChunks.get(entityId);
        chunks.add(new AbstractMap.SimpleEntry<>(x, z));
    }

    public void entityUnloadChunk(UUID entityId, Integer x, Integer z) {
        if (!entityLoadedTrackedChunks.containsKey(entityId)) return;

        List<Map.Entry<Integer, Integer>> chunks = entityLoadedTrackedChunks.get(entityId);
        chunks.remove(new AbstractMap.SimpleEntry<>(x, z));
    }

    public boolean isChunkLoadedByEntity(UUID entityId, Integer x, Integer z) {
        if (!entityLoadedTrackedChunks.containsKey(entityId)) return false;
        return entityLoadedTrackedChunks.get(entityId).contains(new AbstractMap.SimpleEntry<>(x, z));
    }

    public void clearChunksForEntity(UUID entityId) {
        this.entityLoadedTrackedChunks.remove(entityId);
    }
}
