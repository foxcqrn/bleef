package com.foxcqrn.bleef.listener;

import com.foxcqrn.bleef.Items;
import com.foxcqrn.bleef.PluginUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

import static com.foxcqrn.bleef.Bleef.plugin;

public class EventListener implements Listener {

    FileConfiguration config = plugin.getConfig();

    @EventHandler
    public void onServerPing(ServerListPingEvent event) {
        Random r = new Random();
        int randomNumber = r.nextInt(PluginUtil.MOTDArray.length);
        event.setMotd(ChatColor.LIGHT_PURPLE + PluginUtil.MOTDArray[randomNumber] + "\n" + ChatColor.AQUA + PluginUtil.DiscordInvite);
        event.setMaxPlayers(420);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getOnlinePlayers().forEach((online) -> online.setPlayerListHeaderFooter("\n" + ChatColor.LIGHT_PURPLE + "survival building server" + "\n", "\nPlayers online: " + ChatColor.YELLOW + Bukkit.getOnlinePlayers().size() + "\n"));
        if (!player.hasPlayedBefore()) {
            Bukkit.broadcastMessage(ChatColor.AQUA + "Welcome to the server, " + player.getName() + "!");
            player.teleport(PluginUtil.SpawnLocation);
        }
        PluginUtil.updateName(player);
        event.setJoinMessage(player.getDisplayName() + ChatColor.YELLOW + " has joined the game");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Bukkit.getOnlinePlayers().forEach((online) -> online.setPlayerListHeaderFooter("\n" + ChatColor.LIGHT_PURPLE + "survival building server" + "\n", "\nPlayers online: " + ChatColor.YELLOW + (Bukkit.getOnlinePlayers().size() - 1) + "\n"));
        event.setQuitMessage(player.getDisplayName() + ChatColor.YELLOW + " has left the game");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        int x1 = event.getFrom().getBlockX();
        int z1 = event.getFrom().getBlockZ();
        int x2 = event.getTo().getBlockX();
        int z2 = event.getTo().getBlockZ();
        int y = event.getTo().getBlockY();
        int wb = PluginUtil.WorldBorder;
        boolean fromInsideBorder = x1 < wb && x1 > -wb && z1 < wb && z1 > -wb;
        boolean toOutsideBorder = x2 >= wb || x2 <= -wb || z2 >= wb || z2 <= -wb;
        if (fromInsideBorder && toOutsideBorder) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.AQUA + "You have reached the world border. You cannot go past this point.");
        }
        double speed = Math.floor(event.getFrom().distance(event.getTo()) * 200) / 10;
        if (!PluginUtil.ToggleCoords.contains(player.getName())) {
            ChatColor color = ChatColor.LIGHT_PURPLE; // doesn't support hex colors :(
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder(ChatColor.WHITE + "X: " + color + x2 + ChatColor.WHITE + " Y: " + color + y + ChatColor.WHITE + " Z: " + color + z2 + ChatColor.WHITE + " MPS: " + color + speed).create());
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        event.setDeathMessage(ChatColor.RED + event.getDeathMessage());
        player.sendMessage(ChatColor.GOLD + "You died at " + ChatColor.RED + player.getLocation().getBlockX() + " " + player.getLocation().getBlockY() + " " + player.getLocation().getBlockZ() + ChatColor.GOLD + " in world " + ChatColor.RED + player.getLocation().getWorld().getName() + ChatColor.GOLD + ".");
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        net.md_5.bungee.api.ChatColor color = PluginUtil.getColor(player);
        event.setFormat("<" + color + "%1$s" + ChatColor.RESET + "> %2$s");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location location = block.getLocation();
        ItemStack tool = player.getInventory().getItemInMainHand();

        if (tool.getType() == Material.WOODEN_HOE ||
                tool.getType() == Material.STONE_HOE ||
                tool.getType() == Material.IRON_HOE ||
                tool.getType() == Material.GOLDEN_HOE ||
                tool.getType() == Material.DIAMOND_HOE ||
                tool.getType() == Material.NETHERITE_HOE) {
            if (block.getType() == Material.WHEAT) {
                event.setCancelled(true);
                Ageable ageable = (Ageable) block.getBlockData();
                if (ageable.getAge() == 7) {
                    ItemStack item = player.getInventory().getItemInMainHand();
                    Damageable meta = (Damageable) item.getItemMeta();
                    meta.setDamage(meta.getDamage() + 1);
                    item.setItemMeta(meta);
                    location.getBlock().setType(Material.WHEAT);
                    player.getInventory().addItem(new ItemStack(Material.WHEAT));
                    location.getWorld().playSound(location, Sound.BLOCK_GROWING_PLANT_CROP, 1, 0.7f);
                    if (meta.getDamage() > tool.getType().getMaxDurability() - 1) {
                        tool.setAmount(tool.getAmount() - 1);
                        player.playSound(location, Sound.ENTITY_ITEM_BREAK, 1, 1f);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null) return;

        // disable fireworks for elytra flight
        if (item.getType() == Material.FIREWORK_ROCKET) {
            if (player.isGliding()) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Rocket powered elytra flight is disabled on this server.");
            }
        }

        // rotten flesh block action
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (item.isSimilar(Items.getRottenFleshBlockItem())) {
                event.setCancelled(true);
                player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 400, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 200, 60));
                player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);
                item.setAmount(item.getAmount() - 1);
            }
        }
    }

    @EventHandler
    public void onVehicleCreate(VehicleCreateEvent event) {
        Vehicle vehicle = event.getVehicle();
        if (vehicle instanceof RideableMinecart) {
            RideableMinecart cart = (RideableMinecart) vehicle;
            cart.setMaxSpeed(0.5);
        }
    }

    @EventHandler
    public void onArmor(InventoryClickEvent event) {
        if (event.getSlot() != 39 || event.getSlotType() != InventoryType.SlotType.ARMOR) {
            return;
        }

        ItemStack cursor = event.getCursor();
        ItemStack item = event.getCurrentItem();
        Player p = (Player) event.getWhoClicked();

        if (cursor == null || item == null) {
            return;
        }

        if (item.getType() == Material.AIR && cursor.getType() != Material.AIR) {
            p.setItemOnCursor(null);
            Bukkit.getScheduler().runTask(plugin, () -> p.getInventory().setHelmet(cursor));
        }
    }
}
