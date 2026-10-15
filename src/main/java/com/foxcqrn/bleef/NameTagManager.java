package com.foxcqrn.bleef;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class NameTagManager implements Listener {
    private static final String TEAM_NAME = "bleef_hidenames";

    private static final float HEIGHT_OFFSET = 0.35f;
    private static final float PET_HEIGHT_OFFSET = 0.3f;

    private static final byte SNEAK_OPACITY = 64;
    private static final byte FULL_OPACITY = -1;

    private static final double LOOK_RANGE = 5.0;

    private static final String DISPLAY_TAG = "bleef_nametag";

    private final JavaPlugin plugin;
    private final Map<UUID, TextDisplay> tags = new HashMap<>();
    private final Map<UUID, TextDisplay> petTags = new HashMap<>();
    private final Set<UUID> lookAtPets = new HashSet<>();
    private final Map<UUID, Set<UUID>> petViewers = new HashMap<>();
    private final BukkitTask lookTask;
    private int lookTicks;
    private final Team hiddenTeam;

    public NameTagManager(JavaPlugin plugin) {
        this.plugin = plugin;

        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = board.getTeam(TEAM_NAME);
        if (team == null) {
            team = board.registerNewTeam(TEAM_NAME);
        }
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        team.setCanSeeFriendlyInvisibles(false);
        this.hiddenTeam = team;

        for (Player player : Bukkit.getOnlinePlayers()) {
            update(player);
        }
        for (World world : Bukkit.getWorlds()) {
            for (TextDisplay stray : world.getEntitiesByClass(TextDisplay.class)) {
                removeIfStray(stray);
            }
            for (Tameable pet : world.getEntitiesByClass(Tameable.class)) {
                if (isTeamInheritingPet(pet) && pet.customName() != null) {
                    updatePet(pet, PluginUtil.getColor(pet.getOwner().getUniqueId()));
                }
            }
        }

        this.lookTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickLookAt, 2L, 2L);
    }

    public void update(Player player) {
        if (!player.isOnline() || player.isDead()) {
            remove(player);
            return;
        }

        refreshPets(player.getUniqueId());

        hiddenTeam.addEntry(player.getName());

        if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            remove(player);
            return;
        }

        TextDisplay tag = tags.get(player.getUniqueId());
        if (tag == null || !tag.isValid() || !player.getPassengers().contains(tag)) {
            remove(player);
            tag = spawn(player);
            tags.put(player.getUniqueId(), tag);
        }

        tag.text(buildName(player));
        applySneaking(tag, player.isSneaking());
    }

    public void remove(Player player) {
        TextDisplay tag = tags.remove(player.getUniqueId());
        if (tag != null) {
            tag.remove();
        }
    }

    public void shutdown() {
        lookTask.cancel();
        lookAtPets.clear();
        petViewers.clear();
        tags.values().forEach(Entity::remove);
        tags.clear();
        petTags.values().forEach(Entity::remove);
        petTags.clear();
        try {
            hiddenTeam.unregister();
        } catch (IllegalStateException ignored) {
        }
    }

    private TextDisplay spawn(Player player) {
        TextDisplay tag = spawnDisplay(player, HEIGHT_OFFSET);
        player.hideEntity(plugin, tag);
        return tag;
    }

    private TextDisplay spawnDisplay(Entity carrier, float heightOffset) {
        TextDisplay tag = carrier.getWorld().spawn(carrier.getLocation(), TextDisplay.class, d -> {
            d.setPersistent(false);
            d.addScoreboardTag(DISPLAY_TAG);
            d.setBillboard(Display.Billboard.CENTER);
            d.setShadowed(false);
            d.setSeeThrough(true);
            d.setDefaultBackground(false);
            d.setBackgroundColor(Color.fromARGB(64, 0, 0, 0));
            d.setTransformation(new Transformation(
                    new Vector3f(0f, heightOffset, 0f),
                    new AxisAngle4f(),
                    new Vector3f(1f, 1f, 1f),
                    new AxisAngle4f()));
        });

        carrier.addPassenger(tag);
        return tag;
    }

    private Component buildName(Player player) {
        java.awt.Color awt = PluginUtil.getColor(player).getColor();
        TextColor color = TextColor.color(awt.getRGB() & 0xFFFFFF);
        String nickname = PluginUtil.getNickname(player);

        if (PluginUtil.isAFK(player)) {
            return Component.text("[AFK] " + nickname, NamedTextColor.GRAY, TextDecoration.ITALIC);
        }
        return Component.text(nickname, color);
    }

    private void applySneaking(TextDisplay tag, boolean sneaking) {
        tag.setTextOpacity(sneaking ? SNEAK_OPACITY : FULL_OPACITY);
        tag.setSeeThrough(!sneaking);
    }

    private void updateNextTick(Player player) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                update(player);
            }
        }, 1L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        for (UUID petId : lookAtPets) {
            TextDisplay tag = petTags.get(petId);
            if (tag != null) {
                event.getPlayer().hideEntity(plugin, tag);
            }
        }
        updateNextTick(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID quitter = event.getPlayer().getUniqueId();
        petViewers.values().forEach(viewers -> viewers.remove(quitter));
        remove(event.getPlayer());
        hiddenTeam.removeEntry(event.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        updateNextTick(event.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        remove(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        updateNextTick(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        PotionEffectType type = event.getModifiedType();
        if (type == null || PotionEffectType.INVISIBILITY.equals(type)) {
            updateNextTick(player);
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        TextDisplay tag = tags.get(event.getPlayer().getUniqueId());
        if (tag != null && tag.isValid()) {
            applySneaking(tag, event.isSneaking());
        }
    }

    private static boolean isTeamInheritingPet(Entity entity) {
        return entity instanceof Tameable pet && !(entity instanceof AbstractHorse) && pet.isTamed();
    }

    public void updatePet(Entity entity, net.md_5.bungee.api.ChatColor color) {
        UUID id = entity.getUniqueId();
        if (!(entity instanceof LivingEntity pet) || !pet.isValid() || !isTeamInheritingPet(pet)) {
            removePet(id);
            return;
        }
        Component name = pet.customName();
        if (name == null || pet.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            removePet(id);
            return;
        }

        if (color != null) {
            java.awt.Color awt = color.getColor();
            if (awt != null) {
                name = name.color(TextColor.color(awt.getRGB() & 0xFFFFFF));
            }
        }

        boolean lookAt = !pet.isCustomNameVisible();

        hiddenTeam.addEntry(id.toString());

        if (petTags.containsKey(id) && lookAtPets.contains(id) != lookAt) {
            removePetDisplay(id);
        }

        TextDisplay tag = petTags.get(id);
        if (tag == null || !tag.isValid() || !pet.getPassengers().contains(tag)) {
            removePetDisplay(id);
            tag = spawnDisplay(pet, PET_HEIGHT_OFFSET);
            petTags.put(id, tag);
            if (lookAt) {
                lookAtPets.add(id);
                petViewers.put(id, new HashSet<>());
                for (Player online : Bukkit.getOnlinePlayers()) {
                    online.hideEntity(plugin, tag);
                }
            }
        }
        tag.text(name);
    }

    private void refreshPets(UUID ownerId) {
        for (UUID petId : new ArrayList<>(petTags.keySet())) {
            if (Bukkit.getEntity(petId) instanceof Tameable pet && pet.getOwner().getUniqueId().equals(ownerId)) {
                updatePet(pet, PluginUtil.getColor(ownerId));
            }
        }
    }

    private void tickLookAt() {
        if (lookAtPets.isEmpty()) {
            return;
        }
        boolean reassert = ++lookTicks % 5 == 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID looked = null;
            RayTraceResult hit = player.getWorld().rayTrace(
                    player.getEyeLocation(), player.getEyeLocation().getDirection(), LOOK_RANGE,
                    FluidCollisionMode.NEVER, true, 0.0, e -> lookAtPets.contains(e.getUniqueId()));
            if (hit != null && hit.getHitEntity() != null) {
                looked = hit.getHitEntity().getUniqueId();
            }

            for (UUID petId : lookAtPets) {
                TextDisplay tag = petTags.get(petId);
                Set<UUID> viewers = petViewers.get(petId);
                if (tag == null || viewers == null) {
                    continue;
                }
                boolean shouldSee = petId.equals(looked);
                boolean sees = viewers.contains(player.getUniqueId());
                if (shouldSee && !sees) {
                    player.showEntity(plugin, tag);
                    viewers.add(player.getUniqueId());
                } else if (!shouldSee && (sees || reassert)) {
                    player.hideEntity(plugin, tag);
                    viewers.remove(player.getUniqueId());
                }
            }
        }
    }

    private void removePetDisplay(UUID id) {
        lookAtPets.remove(id);
        petViewers.remove(id);
        TextDisplay tag = petTags.remove(id);
        if (tag != null) {
            tag.remove();
        }
    }

    private void removePet(UUID id) {
        removePetDisplay(id);
        hiddenTeam.removeEntry(id.toString());
    }

    private void updatePetNextTick(UUID id) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Tameable pet = (Tameable) Bukkit.getEntity(id);
            if (pet != null) {
                updatePet(pet, PluginUtil.getColor(pet.getOwner().getUniqueId()));
            } else {
                removePetDisplay(id);
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTame(EntityTameEvent event) {
        updatePetNextTick(event.getEntity().getUniqueId());
    }

    private void removeIfStray(Entity entity) {
        if (entity instanceof TextDisplay display
                && display.getScoreboardTags().contains(DISPLAY_TAG)
                && !tags.containsValue(display)
                && !petTags.containsValue(display)) {
            display.remove();
        }
    }

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        for (Entity entity : event.getEntities()) {
            removeIfStray(entity);
            if (isTeamInheritingPet(entity) && entity.customName() != null) {
                updatePetNextTick(entity.getUniqueId());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNameTag(PlayerInteractEntityEvent event) {
        var held = event.getPlayer().getInventory().getItem(event.getHand());
        if (held.getType() == Material.NAME_TAG && event.getRightClicked() instanceof Tameable) {
            updatePetNextTick(event.getRightClicked().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPetTeleport(EntityTeleportEvent event) {
        UUID id = event.getEntity().getUniqueId();
        if (petTags.containsKey(id)) {
            updatePetNextTick(id);
        }
    }

    @EventHandler
    public void onPetDeath(EntityDeathEvent event) {
        UUID id = event.getEntity().getUniqueId();
        if (petTags.containsKey(id)) {
            removePet(id);
        }
    }

    @EventHandler
    public void onPetRemove(EntityRemoveEvent event) {
        UUID id = event.getEntity().getUniqueId();
        if (!petTags.containsKey(id)) {
            return;
        }
        removePetDisplay(id);
        if (event.getCause() != EntityRemoveEvent.Cause.UNLOAD) {
            hiddenTeam.removeEntry(id.toString());
        }
    }
}