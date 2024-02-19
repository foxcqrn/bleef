package com.foxcqrn.bleef;

import com.foxcqrn.bleef.commands.*;
import com.foxcqrn.bleef.listener.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

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

        this.getCommand("afk").setExecutor(new CommandAfk(plugin));
        this.getCommand("togglecoords").setExecutor(new CommandToggleCoords(plugin));
        this.getCommand("playerstats").setExecutor(new CommandPlayerStats(plugin));
        this.getCommand("housemarker").setExecutor(new CommandHouseMarker(plugin));
        this.getCommand("cornerarray").setExecutor(new CommandCornerArray(plugin));
        this.getCommand("color").setExecutor(new CommandColor(plugin));
        this.getCommand("nickname").setExecutor(new CommandNickname(plugin));
        this.getCommand("sudo").setExecutor(new CommandSudo(plugin));
        this.getCommand("creative").setExecutor(new CommandCreative(plugin));
        this.getCommand("survival").setExecutor(new CommandSurvival(plugin));
        this.getCommand("list").setExecutor(new CommandList(plugin));
        this.getCommand("speed").setExecutor(new CommandSpeed(plugin));

        Bukkit.addRecipe(Items.fleshBlockRecipe());
        Bukkit.addRecipe(Items.rottenFleshRecipe());
        Bukkit.addRecipe(Items.pipeWithWeedRecipe());

        config.addDefault("creative", false);
        config.addDefault("players.default.color", "#FFFFFF");
        saveDefaultConfig();
        saveConfig();

        PluginUtil.registerGlow();
        plugin.getLogger().log(Level.INFO, "boofed up");
    }

    @Override
    public void onDisable() {
        plugin.getLogger().log(Level.INFO, "boofed down");
    }
}