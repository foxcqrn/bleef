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
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import dev.jorel.commandapi.*;
import dev.jorel.commandapi.arguments.*;

public final class Bleef extends JavaPlugin {
    public static Bleef plugin;
    FileConfiguration config = getConfig();

    @Override
    public void onLoad() {
        plugin = this;
        CommandAPI.onLoad(new CommandAPIBukkitConfig(plugin));
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
        manager.addPacketListener(new PacketAdapter(
                this, ListenerPriority.HIGHEST, PacketType.Play.Server.NAMED_SOUND_EFFECT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Server.NAMED_SOUND_EFFECT) {
                    PacketContainer packet = event.getPacket();
                    List<Sound> sounds = packet.getSoundEffects().getValues();
                    if (sounds.contains(Sound.BLOCK_PISTON_CONTRACT)
                            || sounds.contains(Sound.BLOCK_PISTON_EXTEND)) {
                        event.setCancelled(true);
                    }
                }
            }
        });
        manager.addPacketListener(new PacketAdapter(
                this, ListenerPriority.HIGHEST, PacketType.Play.Server.WORLD_EVENT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                int effectID = packet.getIntegers().read(0);

                // Cancel dispenser fire sound & particle effect if
                // dispenser is using a wrench
                if (effectID == 1000 || effectID == 2000) {
                    BlockPosition position = packet.getBlockPositionModifier().read(0);
                    Block block = event.getPlayer().getWorld().getBlockAt(
                            position.getX(), position.getY(), position.getZ());
                    if (block.getType() == Material.DISPENSER
                            && ((Dispenser) block.getState())
                                       .getInventory()
                                       .containsAtLeast(Items.getWrenchItem(), 1)) {
                        event.setCancelled(true);
                    }
                }
            }
        });

        CommandAPI.onEnable();

        new CommandAPICommand("afk")
                .withFullDescription("Indicates that you are AFK (away-from-keyboard).")
                .executes(CommandAfk::onCommand)
                .register();

        new CommandAPICommand("togglecoords")
                .withFullDescription("Toggles the HUD above the taskbar.")
                .withAliases("tc", "coords")
                .executes(CommandToggleCoords::onCommand)
                .register();

        new CommandAPICommand("playerstats")
                .withFullDescription("Shows information and statistics about a specific player.")
                .withArguments(new EntitySelectorArgument.OnePlayer("player"))
                .withPermission(CommandPermission.OP)
                .withAliases("pstats", "pinfo", "ps", "pi")
                .executes(CommandPlayerStats::onCommand)
                .register();

        new CommandAPICommand("housemarker")
                .withFullDescription(
                        "Creates a marker for your house on the Dynmap at your current location.")
                .withArguments(new MultiLiteralArgument("action", "create", "delete"),
                        new TextArgument("name").setOptional(true))
                .withAliases("hm")
                .executes(CommandHouseMarker::onCommand)
                .register();

        new CommandAPICommand("addroad")
                .withFullDescription("Creates a road on the Dynmap from the current set of points.")
                .withArguments(new MultiLiteralArgument("type", "normal", "highway"),
                        new GreedyStringArgument("name"))
                .withPermission("bleef.mapdraw")
                .withAliases("road")
                .executes(CommandAddRoad::onCommand)
                .register();

        new CommandAPICommand("addborder")
                .withFullDescription(
                        "Creates a region border on the Dynmap from the current set of points.")
                .withArguments(new GreedyStringArgument("name"))
                .withPermission("bleef.mapdraw")
                .withAliases("border")
                .executes(CommandAddBorder::onCommand)
                .register();

        CommandMapLine lineMapper = new CommandMapLine();

        new CommandAPICommand("mapline")
                .withFullDescription(
                        "Actively maps a line as the player moves, and adds the points to the Dynmap point set.")
                .withPermission("bleef.mapdraw")
                .withAliases("ml")
                .executes(lineMapper::onCommand)
                .register();

        new CommandAPICommand("color")
                .withFullDescription("Changes the color of the player's display name.")
                .withAliases("setcolor")
                .withArguments(new GreedyStringArgument("color"))
                .executes(CommandColor::onCommand)
                .register();

        new CommandAPICommand("nickname")
                .withFullDescription("Sets the player's display name.")
                .withAliases("nick", "name", "setnick", "setname", "nn")
                .withArguments(new GreedyStringArgument("name"))
                .executes(CommandNickname::onCommand)
                .register();

        new CommandAPICommand("sudo")
                .withFullDescription(
                        "Executes a command (or sends a message) as if from another player.")
                .withArguments(new EntitySelectorArgument.OnePlayer("player"),
                        new GreedyStringArgument("command"))
                .withPermission(CommandPermission.OP)
                .executes(CommandSudo::onCommand)
                .register();

        new CommandAPICommand("speed")
                .withFullDescription(
                        "Sets the speed of the player to a specified value, or resets it to the default value.")
                .withArguments(new MultiLiteralArgument("action", "set", "reset"),
                        new FloatArgument("value"))
                .executes(CommandSpeed::onCommand)
                .register();

        new CommandAPICommand("tpa")
                .withFullDescription(
                        "Teleports the player to another specified player, notifying the other player.")
                .withArguments(new EntitySelectorArgument.OnePlayer("player"))
                .executes(CommandTeleport::onCommand)
                .register();

        new CommandAPICommand("addhorse")
                .withFullDescription("Registers a horse to the player.")
                .executes(CommandAddHorse::onCommand)
                .register();

        new CommandAPICommand("delhorse")
                .withFullDescription("Unregisters a horse from the player.")
                .executes(CommandDelHorse::onCommand)
                .register();

        new CommandAPICommand("map")
                .withFullDescription("Provides the URL to the server Dynmap.")
                .executes(CommandMap::onCommand)
                .register();

        new CommandAPICommand("wrench")
                .withFullDescription("Give a wrench to the current player.")
                .withAliases("givewrench")
                .withPermission("bleef.wrench.give")
                .executes(CommandWrench::onCommand)
                .register();

        new CommandAPICommand("packprompt")
                .withArguments(
                        new MultiLiteralArgument("mode", "direct", "external"),
                        new TextArgument("url"),
                        new EntitySelectorArgument.ManyPlayers("target"))
                .withPermission(CommandPermission.OP)
                .executes(CommandPackPrompt::onCommand)
                .register();

        config.addDefault("creative", false);
        config.addDefault("players.default.color", "#FFFFFF");
        config.addDefault("players.horses", new ArrayList<String>());
        saveDefaultConfig();
        saveConfig();

        Items.add();

        if (PluginUtil.isCreative)
            plugin.getLogger().log(Level.WARNING,
                    "Plugin running in creative mode! Set creative: false in config.yml if this is an error.");
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