package com.foxcqrn.bleef;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.Random;
import java.util.logging.Level;

public final class Bleef extends Plugin implements Listener {

    public static Bleef plugin;

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        plugin.getLogger().log(Level.INFO, "boofed up");
    }

    @Override
    public void onDisable() {
        plugin.getLogger().log(Level.INFO, "boofed down");
    }

    @EventHandler
    public void onServerPing(ProxyPingEvent event) {
        Random r = new Random();
        int randomNumber = r.nextInt(PluginUtil.MOTDArray.length);
        ServerPing resp = new ServerPing();
        BaseComponent component = new ComponentBuilder()
                .color(ChatColor.LIGHT_PURPLE)
                .append(PluginUtil.MOTDArray[randomNumber])
                .append("\n")
                .color(ChatColor.AQUA)
                .append(PluginUtil.DiscordInvite)
                .getComponent(0);
        resp.setDescriptionComponent(component);
        event.setResponse(resp);
    }
}