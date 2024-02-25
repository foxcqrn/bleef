package com.foxcqrn.bleef;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.foxcqrn.bleef.commands.*;
import com.foxcqrn.bleef.listener.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public final class Bleef extends JavaPlugin {

    public static Bleef plugin;
    FileConfiguration config = getConfig();

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        final PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(new EventListener(), plugin);
        pm.registerEvents(new AfkListener(), plugin);
        pm.registerEvents(new WeedListener(), plugin);
        pm.registerEvents(new LeashListener(), plugin);
        pm.registerEvents(new CompassListener(), plugin);
        pm.registerEvents(new WrenchListener(), plugin);
        if (PluginUtil.isCreative) pm.registerEvents(new CreativeListener(), plugin);

        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(new PacketAdapter(this, ListenerPriority.HIGHEST,
                PacketType.Play.Server.NAMED_SOUND_EFFECT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Server.NAMED_SOUND_EFFECT) {
                    PacketContainer packet = event.getPacket();
                    List<Sound> sounds = packet.getSoundEffects().getValues();
                    if (sounds.contains(Sound.BLOCK_PISTON_CONTRACT) || sounds.contains(Sound.BLOCK_PISTON_EXTEND)) {
                        event.setCancelled(true);
                    }
                }
            }
        });
        manager.addPacketListener(new PacketAdapter(this, ListenerPriority.HIGHEST, PacketType.Play.Server.WORLD_EVENT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                int effectID = packet.getIntegers().read(0);

                // Cancel dispenser fire sound & particle effect if dispenser is using a wrench
                if (effectID == 1000 || effectID == 2000) {
                    BlockPosition position = packet.getBlockPositionModifier().read(0);
                    Block block = event.getPlayer().getWorld().getBlockAt(position.getX(), position.getY(), position.getZ());
                    if (block.getType() == Material.DISPENSER &&
                    ((Dispenser) block.getState()).getInventory().containsAtLeast(Items.getWrenchItem(), 1)) {
                        event.setCancelled(true);
                    }
                }
            }
        });

        this.getCommand("afk").setExecutor(new CommandAfk(plugin));
        this.getCommand("togglecoords").setExecutor(new CommandToggleCoords(plugin));
        this.getCommand("playerstats").setExecutor(new CommandPlayerStats(plugin));
        this.getCommand("housemarker").setExecutor(new CommandHouseMarker(plugin));
        this.getCommand("addroad").setExecutor(new CommandAddRoad(plugin));
        this.getCommand("addborder").setExecutor(new CommandAddBorder(plugin));
        this.getCommand("mapline").setExecutor(new CommandMapLine(plugin));
        this.getCommand("color").setExecutor(new CommandColor(plugin));
        this.getCommand("nickname").setExecutor(new CommandNickname(plugin));
        this.getCommand("sudo").setExecutor(new CommandSudo(plugin));
        this.getCommand("creative").setExecutor(new CommandCreative(plugin));
        this.getCommand("survival").setExecutor(new CommandSurvival(plugin));
        this.getCommand("list").setExecutor(new CommandList(plugin));
        this.getCommand("speed").setExecutor(new CommandSpeed(plugin));
        this.getCommand("teleport").setExecutor(new CommandTeleport(plugin));
        this.getCommand("sequence").setExecutor(new CommandSequence(plugin));
        this.getCommand("addhorse").setExecutor(new CommandAddHorse(plugin));
        this.getCommand("delhorse").setExecutor(new CommandDelHorse(plugin));
        this.getCommand("map").setExecutor(new CommandMap(plugin));
        this.getCommand("wrench").setExecutor(new CommandWrench(plugin));

        config.addDefault("creative", false);
        config.addDefault("players.default.color", "#FFFFFF");
        config.addDefault("players.horses", new ArrayList<String>());
        saveDefaultConfig();
        saveConfig();

        Items.add();
        if (NamespacedKey.fromString("bleef:glow") != null) PluginUtil.registerGlow();

        if (PluginUtil.isCreative) plugin.getLogger().log(Level.WARNING, "Plugin running in creative mode! Set creative: false in config.yml if this is an error.");
        plugin.getLogger().log(Level.INFO, "boofed up");
    }

    @Override
    public void onDisable() {
        saveDefaultConfig();
        saveConfig();
        Items.remove();
        plugin.getLogger().log(Level.INFO, "boofed down");
    }
}