package com.foxcqrn.bleef;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import net.kyori.adventure.key.Key;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class HorseWaypoints implements Listener {
    private static final Key STYLE = Key.key("minecraft", "bowtie");
    private static final double TRANSMIT_RANGE = 2000.0;
    private final JavaPlugin plugin;
    private final NamespacedKey modifierKey;
    private final Map<UUID, UUID> mountToOwner = new ConcurrentHashMap<>();
    private final Set<UUID> retired = ConcurrentHashMap.newKeySet();
    private final boolean filterActive;

    public HorseWaypoints(JavaPlugin plugin) {
        this.plugin = plugin;
        this.modifierKey = new NamespacedKey(plugin, "horse_waypoint");
        loadRegistered();
        this.filterActive = registerPacketFilter();

        if (!filterActive) {
            plugin.getLogger().warning(
                    "Horse waypoints are disabled: the ProtocolLib waypoint filter could not be installed.");
            return;
        }

        mountToOwner.forEach((mountId, ownerId) -> {
            if (Bukkit.getEntity(mountId) instanceof LivingEntity mount) {
                apply(mount, ownerId);
            }
        });
    }

    public void register(UUID ownerId, LivingEntity mount) {
        unregister(ownerId);
        retired.remove(mount.getUniqueId());
        mountToOwner.put(mount.getUniqueId(), ownerId);
        apply(mount, ownerId);
    }

    public void unregister(UUID ownerId) {
        mountToOwner.entrySet().removeIf(entry -> {
            if (!entry.getValue().equals(ownerId)) {
                return false;
            }
            retired.add(entry.getKey());
            if (Bukkit.getEntity(entry.getKey()) instanceof LivingEntity mount) {
                clear(mount);
            }
            return true;
        });
    }

    public void refresh(UUID ownerId) {
        mountToOwner.forEach((mountId, owner) -> {
            if (owner.equals(ownerId) && Bukkit.getEntity(mountId) instanceof LivingEntity mount) {
                apply(mount, ownerId);
            }
        });
    }

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        for (var entity : event.getEntities()) {
            UUID ownerId = mountToOwner.get(entity.getUniqueId());
            if (ownerId != null && entity instanceof LivingEntity mount) {
                apply(mount, ownerId);
            }
        }
    }

    private void apply(LivingEntity mount, UUID ownerId) {
        if (!filterActive) {
            return;
        }
        AttributeInstance range = mount.getAttribute(Attribute.WAYPOINT_TRANSMIT_RANGE);
        if (range == null) {
            return;
        }
        mount.setWaypointStyle(STYLE);
        mount.setWaypointColor(colorFor(ownerId));

        removeRangeModifier(range);
        range.addTransientModifier(new AttributeModifier(
                modifierKey, TRANSMIT_RANGE, AttributeModifier.Operation.ADD_NUMBER));
    }

    private void clear(LivingEntity mount) {
        AttributeInstance range = mount.getAttribute(Attribute.WAYPOINT_TRANSMIT_RANGE);
        if (range != null) {
            removeRangeModifier(range);
            if (range.getValue() > 0) {
                plugin.getLogger().warning("Horse waypoint range is still " + range.getValue()
                        + " after clearing it (base " + range.getBaseValue() + ", modifiers "
                        + range.getModifiers() + ")");
            }
        }
        mount.setWaypointStyle(null);
        mount.setWaypointColor(null);
    }

    private void removeRangeModifier(AttributeInstance range) {
        for (AttributeModifier modifier : new ArrayList<>(range.getModifiers())) {
            if (modifierKey.equals(modifier.getKey())) {
                range.removeModifier(modifier);
            }
        }
    }

    private Color colorFor(UUID ownerId) {
        java.awt.Color awt = PluginUtil.getColor(ownerId).getColor();
        return Color.fromRGB(awt.getRGB() & 0xFFFFFF);
    }

    private void loadRegistered() {
        ConfigurationSection players = plugin.getConfig().getConfigurationSection("players");
        if (players == null) {
            return;
        }
        for (String key : players.getKeys(false)) {
            String horse = players.getString(key + ".horse");
            if (horse == null) {
                continue;
            }
            try {
                mountToOwner.put(UUID.fromString(horse), UUID.fromString(key));
            } catch (IllegalArgumentException ignored) {
                // "default" and other non-player keys
            }
        }
    }

    private boolean registerPacketFilter() {
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") == null) {
            return false;
        }
        try {
            PacketType type = PacketType.findCurrent(
                    PacketType.Protocol.PLAY, PacketType.Sender.SERVER, "TrackedWaypoint");
            if (type == null || !type.isSupported()) {
                return false;
            }
            ProtocolLibrary.getProtocolManager().addPacketListener(
                    new WaypointFilter(plugin, type, mountToOwner, retired));
            return true;
        } catch (Exception | LinkageError e) {
            plugin.getLogger().warning("Could not install the waypoint packet filter: " + e);
            return false;
        }
    }

    /**
     * Cancels waypoint packets about a registered mount for every player except its owner, and
     * about a retired mount for everyone. Waypoints about players and everything else pass through.
     *
     * The packet holds an operation (track / update / untrack) and a TrackedWaypoint whose
     * identifier is Either<UUID, String>. An entity's waypoint uses the entity's UUID. Server
     * classes aren't wrapped by ProtocolLib, so they're read by reflection, matching on field
     * TYPES rather than names so this survives renames.
     */
    private static final class WaypointFilter extends PacketAdapter {
        private static final String EITHER = "com.mojang.datafixers.util.Either";

        private final JavaPlugin owner;
        private final Map<UUID, UUID> mountToOwner;
        private final Set<UUID> retired;
        private final AtomicBoolean warned = new AtomicBoolean();

        private volatile Field waypointField;
        private volatile Field identifierField;
        private volatile Method leftMethod;

        WaypointFilter(JavaPlugin plugin, PacketType type, Map<UUID, UUID> mountToOwner, Set<UUID> retired) {
            super(plugin, ListenerPriority.HIGHEST, type);
            this.owner = plugin;
            this.mountToOwner = mountToOwner;
            this.retired = retired;
        }

        @Override
        public void onPacketSending(PacketEvent event) {
            if (event.isPlayerTemporary()) {
                return;
            }
            UUID subject = readSubject(event.getPacket().getHandle());
            if (subject == null) {
                return;
            }
            if (retired.contains(subject)) {
                event.setCancelled(true);
                return;
            }
            UUID ownerId = mountToOwner.get(subject);
            if (ownerId != null && !ownerId.equals(event.getPlayer().getUniqueId())) {
                event.setCancelled(true);
            }
        }

        private UUID readSubject(Object packet) {
            try {
                Field wf = waypointField;
                if (wf == null) {
                    wf = locate(packet.getClass());
                    if (wf == null) {
                        warnOnce("no waypoint field found", packet.getClass());
                        return null;
                    }
                    waypointField = wf;
                }
                Object waypoint = wf.get(packet);
                if (waypoint == null) {
                    return null;
                }
                Object either = identifierField.get(waypoint);
                if (either == null) {
                    return null;
                }
                Optional<?> left = (Optional<?>) leftMethod.invoke(either);
                return left.isPresent() && left.get() instanceof UUID id ? id : null;
            } catch (ReflectiveOperationException | RuntimeException e) {
                warnOnce(e.toString(), packet.getClass());
                return null;
            }
        }

        private Field locate(Class<?> packetClass) throws ReflectiveOperationException {
            for (Field field : packetClass.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                for (Class<?> c = field.getType(); c != null && c != Object.class; c = c.getSuperclass()) {
                    for (Field inner : c.getDeclaredFields()) {
                        if (Modifier.isStatic(inner.getModifiers())
                                || !inner.getType().getName().equals(EITHER)) {
                            continue;
                        }
                        field.setAccessible(true);
                        inner.setAccessible(true);
                        identifierField = inner;
                        leftMethod = inner.getType().getMethod("left");
                        return field;
                    }
                }
            }
            return null;
        }

        private void warnOnce(String why, Class<?> packetClass) {
            if (!warned.compareAndSet(false, true)) {
                return;
            }
            StringBuilder fields = new StringBuilder();
            for (Field f : packetClass.getDeclaredFields()) {
                fields.append(f.getType().getName()).append(' ').append(f.getName()).append("; ");
            }
            owner.getLogger().warning("Horse waypoint filter could not read a waypoint packet (" + why
                    + "). Horse waypoints may be visible to other players. Packet "
                    + packetClass.getName() + " fields: " + fields);
        }
    }
}