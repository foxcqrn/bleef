package com.foxcqrn.bleef.listener;

import static com.foxcqrn.bleef.Bleef.plugin;

import com.foxcqrn.bleef.Items;
import com.foxcqrn.bleef.PluginUtil;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;
import java.util.Random;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class EventListener implements Listener {
    @EventHandler
    @SuppressWarnings("unused")
    public void onServerPing(ServerListPingEvent event) {
        Random r = new Random();
        int randomNumber = r.nextInt(PluginUtil.MOTDArray.length);
        event.setMotd(ChatColor.LIGHT_PURPLE + PluginUtil.MOTDArray[randomNumber] + "\n"
                + ChatColor.AQUA + PluginUtil.DiscordInvite);
        event.setMaxPlayers(420);
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PluginUtil.updateTabList(false);
        if (!player.hasPlayedBefore()) {
            Bukkit.broadcastMessage(
                    ChatColor.AQUA + "Welcome to the server, " + player.getName() + "!");
            player.teleport(PluginUtil.SpawnLocation);
        }
        PluginUtil.updateName(player);
        if (PluginUtil.isCreative) {
            player.setGameMode(GameMode.CREATIVE);
        } else {
            player.setGameMode(GameMode.SURVIVAL);
        }
        event.setJoinMessage(player.getDisplayName() + ChatColor.YELLOW + " has joined the game");
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PluginUtil.updateTabList(true);
        event.setQuitMessage(player.getDisplayName() + ChatColor.YELLOW + " has left the game");
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        int x1 = event.getFrom().getBlockX();
        int z1 = event.getFrom().getBlockZ();
        int x2 = Objects.requireNonNull(event.getTo()).getBlockX();
        int z2 = event.getTo().getBlockZ();
        int y = event.getTo().getBlockY();
        int wb = PluginUtil.WorldBorder;
        boolean fromInsideBorder = x1 < wb && x1 > -wb && z1 < wb && z1 > -wb;
        boolean toOutsideBorder = x2 >= wb || x2 <= -wb || z2 >= wb || z2 <= -wb;
        // survival world border
        if (fromInsideBorder && toOutsideBorder && !PluginUtil.isCreative) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.AQUA
                    + "You have reached the world border. You cannot go past this point.");
        }
        double speed = Math.floor(event.getFrom().distance(event.getTo()) * 200) / 10;
        if (!PluginUtil.ToggleCoords.contains(player.getName())) {
            ChatColor color = ChatColor.LIGHT_PURPLE; // doesn't support hex colors :(
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new ComponentBuilder(ChatColor.WHITE + "X: " + color + x2 + ChatColor.WHITE
                            + " Y: " + color + y + ChatColor.WHITE + " Z: " + color + z2
                            + ChatColor.WHITE + " MPS: " + color + speed)
                            .create());
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        event.setDeathMessage(ChatColor.RED + event.getDeathMessage());
        player.sendMessage(ChatColor.GOLD + "You died at " + ChatColor.RED
                + player.getLocation().getBlockX() + " " + player.getLocation().getBlockY() + " "
                + player.getLocation().getBlockZ() + ChatColor.GOLD + " in world " + ChatColor.RED
                + Objects.requireNonNull(player.getLocation().getWorld()).getName() + ChatColor.GOLD + ".");
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        net.md_5.bungee.api.ChatColor color = PluginUtil.getColor(player);
        event.setFormat("<" + color + "%1$s" + ChatColor.RESET + "> %2$s");
    }

    private boolean isHoe(ItemStack tool) {
        Material type = tool.getType();
        return type == Material.WOODEN_HOE || type == Material.STONE_HOE
                || type == Material.IRON_HOE || type == Material.GOLDEN_HOE
                || type == Material.DIAMOND_HOE || type == Material.NETHERITE_HOE;
    }

    private boolean isAxe(ItemStack tool) {
        Material type = tool.getType();
        return type == Material.WOODEN_AXE || type == Material.STONE_AXE
                || type == Material.IRON_AXE || type == Material.GOLDEN_AXE
                || type == Material.DIAMOND_AXE || type == Material.NETHERITE_AXE;
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        ItemStack tool = player.getInventory().getItemInMainHand();

        if (!isHoe(tool)) return;
        if (block.getType() != Material.WHEAT) return;
        event.setCancelled(true);
        Ageable ageable = (Ageable) block.getBlockData();
        if (ageable.getAge() != 7) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        Damageable meta = (Damageable) item.getItemMeta();
        Location location = block.getLocation();
        World world = location.getWorld();
        assert meta != null;
        meta.setDamage(meta.getDamage() + 1);
        item.setItemMeta(meta);
        location.getBlock().setType(Material.WHEAT);
        assert world != null;
        world.dropItemNaturally(location, new ItemStack(Material.WHEAT));
        world.playSound(location, Sound.BLOCK_GROWING_PLANT_CROP, 1, 0.7f);
        if (meta.getDamage() > tool.getType().getMaxDurability() - 1) {
            tool.setAmount(tool.getAmount() - 1);
            world.playSound(location, Sound.ENTITY_ITEM_BREAK, 1, 1f);
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Block target = event.getClickedBlock();
        Action action = event.getAction();

        if (item == null) return;

        // disable fireworks for elytra flight in survival
        if (item.getType() == Material.FIREWORK_ROCKET && player.isGliding()
                && !PluginUtil.isCreative) {
            event.setCancelled(true);
            player.sendMessage(
                    ChatColor.RED + "Rocket powered elytra flight is disabled on this server.");
        }

        // sign dewax
        if (isAxe(item) && action == Action.RIGHT_CLICK_BLOCK && target.getState() instanceof Sign) {
            Sign sign = (Sign) target.getState();
            if (!sign.isWaxed()) return;
            World world = player.getWorld();
            world.playEffect(target.getLocation(), Effect.COPPER_WAX_OFF, null);
            world.playSound(target.getLocation(), Sound.ITEM_AXE_STRIP, 1, 1);
            sign.setWaxed(false);
            sign.update(true);
            event.setCancelled(true);
        }

        // rotten flesh block action
        if (action == Action.RIGHT_CLICK_AIR
                || action == Action.RIGHT_CLICK_BLOCK) {
            if (item.isSimilar(Items.getRottenFleshBlockItem())) {
                event.setCancelled(true);
                player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 400, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 200, 60));
                Objects.requireNonNull(player.getLocation().getWorld()).playSound(
                        player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);
                item.setAmount(item.getAmount() - 1);
            }
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onArmor(InventoryClickEvent event) {
        if (event.getSlot() != 39 || event.getSlotType() != InventoryType.SlotType.ARMOR) return;

        ItemStack cursor = event.getCursor();
        ItemStack item = event.getCurrentItem();

        if (cursor == null || item == null) return;

        if (item.getType() == Material.AIR && cursor.getType() != Material.AIR) {
            Player p = (Player) event.getWhoClicked();
            p.setItemOnCursor(null);
            Bukkit.getScheduler().runTask(plugin, () -> p.getInventory().setHelmet(cursor));
        }
    }

    /*@EventHandler
    @SuppressWarnings("unused")
    public void onVehicleCreate(VehicleCreateEvent event) {
        Vehicle vehicle = event.getVehicle();
        if (vehicle instanceof RideableMinecart) {
            RideableMinecart cart = (RideableMinecart) vehicle;
            cart.setMaxSpeed(0.5);
        }
    }*/

    @EventHandler
    @SuppressWarnings("unused")
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String msg = event.getMessage().toLowerCase();
        if (!msg.startsWith("/dynmap hide") || player.isOp()) return;
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dynmap show " + player.getName()), 60000);
    }
}
