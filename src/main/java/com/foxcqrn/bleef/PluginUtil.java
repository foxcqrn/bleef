package com.foxcqrn.bleef;

import net.md_5.bungee.api.ChatColor;

import java.net.MalformedURLException;
import java.net.URL;

public class PluginUtil {
    public static String DiscordInvite = "https://discord.gg/8zZapcP7fV";
    public static URL WebhookURL;
    public static String WebhookStopPayload = "{\"embeds\": [{\"description\": \"Proxy has stopped\"}]}";
    public static String WebhookStartPayload = "{\"embeds\": [{\"description\": \"Proxy has started\"}]}";

    static {
        try {
            WebhookURL = new URL("https://discord.com/api/webhooks/1208596102081683497/DobBC01BuuhtPiEtcaLUbVtuTPsYopzsedCtZoBpf_YJAhfUxixV7blmRitrNdXAr-F1");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);  // should never happen
        }
    }

    public static String[] MOTDArray = new String[]{
            "best minecraft server in the world",
            ChatColor.AQUA + "B" + ChatColor.GOLD + "O" + ChatColor.LIGHT_PURPLE + "N" + ChatColor.YELLOW + "F " + ChatColor.GREEN + "<3",
            "honse",
            "i’m a little lerpp lol a",
            "jeeked",
            "clonked",
            "reaped",
            "corqued",
            "beaked",
            "boiled",
            "crongled",
            "parched",
            "kunked",
            "deefed",
            "glunched",
            "zunked",
            "gocked",
            "peeped",
            "morbed",
            "jorted",
            "puhnjeej",
            "bonked",
            "schlozzed",
            "schmabbed"
    };
}