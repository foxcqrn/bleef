package com.foxcqrn.bleef;

import net.md_5.bungee.api.*;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.event.EventHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
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
        pluginManager.registerCommand(this, new CreativeAliasCommand());
        pluginManager.registerCommand(this, new SurvivalCommand());
        pluginManager.registerCommand(this, new SurvivalAliasCommand());

        sendWebhookMessage(PluginUtil.WebhookStartPayload);

        plugin.getLogger().log(Level.INFO, "boofed up");
    }

    @Override
    public void onDisable() {
        sendWebhookMessage(PluginUtil.WebhookStopPayload);
        plugin.getLogger().log(Level.INFO, "boofed down");
    }

    static void sendWebhookMessage(String message) {
        try {
            URLConnection con = PluginUtil.WebhookURL.openConnection();
            HttpURLConnection http = (HttpURLConnection)con;
            http.setDoOutput(true);
            byte[] payload = message.getBytes(StandardCharsets.UTF_8);
            http.setFixedLengthStreamingMode(payload.length);
            http.setRequestProperty("Content-Type", "application/json");
            http.connect();
            try (OutputStream os = http.getOutputStream()) {
                os.write(payload);
            }
            byte[] data = http.getInputStream().readAllBytes();
            System.out.println(new String(data));
            http.disconnect();
        } catch (IOException e) {
            System.out.println("Failed to update webhook");
        }
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