package com.foxcqrn.bleef;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Replaces the vanilla nametag above players with a TextDisplay that can use
 * any hex color. The vanilla nametag is hidden through a scoreboard team, and
 * the display is mounted on the player as a passenger so it follows them.
 */
public final class NameTagManager implements Listener {
    private static final String TEAM_NAME = "bleef_hidenames";

    private static final float HEIGHT_OFFSET = 0.35f;

    private static final byte SNEAK_OPACITY = 64;
    private static final byte FULL_OPACITY = -1;

    private final JavaPlugin plugin;
    private final Map<UUID, TextDisplay> tags = new HashMap<>();
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
    }

    public void update(Player player) {
        if (!player.isOnline() || player.isDead()) {
            remove(player);
            return;
        }

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
        tags.values().forEach(Entity::remove);
        tags.clear();
        try {
            hiddenTeam.unregister();
        } catch (IllegalStateException ignored) {
            // already unregistered
        }
    }

    private TextDisplay spawn(Player player) {
        TextDisplay tag = player.getWorld().spawn(player.getLocation(), TextDisplay.class, d -> {
            d.setPersistent(false);
            d.setBillboard(Display.Billboard.CENTER);
            d.setShadowed(false);
            d.setSeeThrough(true);
            d.setDefaultBackground(false);
            d.setBackgroundColor(Color.fromARGB(64, 0, 0, 0));
            d.setTransformation(new Transformation(
                    new Vector3f(0f, HEIGHT_OFFSET, 0f),
                    new AxisAngle4f(),
                    new Vector3f(1f, 1f, 1f),
                    new AxisAngle4f()));
        });

        player.addPassenger(tag);
        player.hideEntity(plugin, tag);
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
        updateNextTick(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
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
}