package com.foxcqrn.bleef;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static com.foxcqrn.bleef.Bleef.plugin;

public class PluginUtil {
    static FileConfiguration config = plugin.getConfig();
    public static boolean isCreative = config.getBoolean("creative");
    public static ArrayList<String> AFKList = new ArrayList<>();
    public static ArrayList<String> ToggleCoords = new ArrayList<>();
    public static Location SpawnLocation = new Location(Bukkit.getWorld("world"), 222.50, 73.00, -185.50, 180, 5);
    public static int WorldBorder = 5000;
    public static String ErrNoPerm = ChatColor.RED + "You do not have permission to run this command.";
    public static String ErrNoConsole = ChatColor.RED + "Only players may run this command.";
    public static String ErrWrongServer = ChatColor.RED + "You can't use that command on this server.";
    private static final String AFKPrefix = ChatColor.GRAY + "" + ChatColor.ITALIC + "[AFK] ";
    public static String DiscordInvite = "https://discord.gg/8zZapcP7fV";
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

    public static boolean isAFK(Player player) {
        return AFKList.contains(player.getName());
    }

    public static void setAFK(Player player, boolean afk) {
        if (afk) {
            AFKList.add(player.getName());
            Bukkit.broadcastMessage(player.getDisplayName() + ChatColor.LIGHT_PURPLE + " is now AFK");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "discord broadcast > " + getNickname(player) + " is now AFK");
            player.setPlayerListName(AFKPrefix + getNickname(player));
        } else {
            AFKList.remove(player.getName());
            updateName(player);
            Bukkit.broadcastMessage(player.getDisplayName() + ChatColor.LIGHT_PURPLE + " is no longer AFK");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "discord broadcast > " + getNickname(player) + " is no longer AFK");
        }
    }

    public static String getNickname(Player player) {
        String nickname = config.getString("players." + player.getUniqueId() + ".nickname");
        if (nickname == null) nickname = player.getName();
        return nickname;
    }

    public static net.md_5.bungee.api.ChatColor getColor(Player player) {
        String hex = config.getString("players." + player.getUniqueId() + ".color");
        if (hex == null) hex = config.getString("players.default.color");
        return net.md_5.bungee.api.ChatColor.of(hex);
    }

    public static void updateName(Player player) {
        String name = getNickname(player);
        net.md_5.bungee.api.ChatColor color = getColor(player);
        player.setDisplayName(color + name);
        player.setPlayerListName(color + name);
    }

    public static void updateTabList(boolean playerQuit) {
        int count = Bukkit.getOnlinePlayers().size();
        Bukkit.getOnlinePlayers().forEach((online) -> online.setPlayerListHeaderFooter(
                "\n" + ChatColor.LIGHT_PURPLE + (PluginUtil.isCreative ? "creative" : "survival") +" building server" + "\n",
                "\nPlayers online: " + ChatColor.AQUA + (playerQuit ? count-1 : count) + "\n")
        );
    }

    public static void registerGlow() {
        try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Glow glow = new Glow(new NamespacedKey(plugin, "glow"));
            Enchantment.registerEnchantment(glow);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

}