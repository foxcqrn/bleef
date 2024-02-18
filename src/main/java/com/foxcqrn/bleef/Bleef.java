package com.foxcqrn.bleef;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
        getProxy().getPluginManager().registerListener(this, this);

        plugin.getLogger().log(Level.INFO, "boofed up");
    }

    @Override
    public void onDisable() {
        plugin.getLogger().log(Level.INFO, "boofed down");
    }

    @EventHandler(priority = Byte.MAX_VALUE - 1)
    public void onProxyPing(ProxyPingEvent event) throws IOException {
        Random r = new Random();
        int randomNumber = r.nextInt(PluginUtil.MOTDArray.length);
        ServerPing resp = new ServerPing();
        BaseComponent component = new ComponentBuilder().append(
                ChatColor.LIGHT_PURPLE +
                PluginUtil.MOTDArray[randomNumber] + "\n" +
                ChatColor.AQUA +
                PluginUtil.DiscordInvite
        ).getComponent(0);
        resp.setDescriptionComponent(component);
        resp.setVersion(event.getResponse().getVersion());
        InputStream stream = this.getResourceAsStream("favicon.png");
        resp.setFavicon(Favicon.create(ImageIO.read(stream)));
        event.setResponse(resp);
    }
}