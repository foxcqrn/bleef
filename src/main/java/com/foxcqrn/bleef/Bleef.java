package com.foxcqrn.bleef;

import net.md_5.bungee.api.*;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.event.EventHandler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;

public final class Bleef extends Plugin implements Listener {

    public static Bleef plugin;

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        PluginManager pluginManager = getProxy().getPluginManager();

        pluginManager.registerListener(this, this);

        pluginManager.registerCommand(this, new CreativeCommand());
        pluginManager.registerCommand(this, new SurvivalCommand());

        plugin.getLogger().log(Level.INFO, "boofed up");
    }

    @Override
    public void onDisable() {
        plugin.getLogger().log(Level.INFO, "boofed down");
    }

    @EventHandler(priority = Byte.MAX_VALUE - 1)
    public void onProxyPing(ProxyPingEvent event) throws ExecutionException, InterruptedException {
        ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo("main");
        final FutureTask<Object> ft = new FutureTask<>(() -> {}, new Object());
        serverInfo.ping((p, e) -> {
            event.setResponse(p);
            ft.run();
        });
        ft.get();
    }
}