package com.foxcqrn.bleef.listener;

import com.foxcqrn.bleef.Items;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;

public class WeedListener implements Listener {
    @EventHandler
    @SuppressWarnings("unused")
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Location smokeLocation = player.getLocation().add(player.getLocation().getDirection());
        if (event.getAction() == Action.RIGHT_CLICK_AIR
                || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (item != null && item.isSimilar(Items.getPipeWithWeedItem())) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 150, 1));
                player.addPotionEffect(
                        new PotionEffect(PotionEffectType.HASTE, (1200 * 5), 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 600, 1));
                player.addPotionEffect(
                        new PotionEffect(PotionEffectType.STRENGTH, (1200 * 5), 0));
                Objects.requireNonNull(player.getLocation().getWorld()).playSound(
                        player.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 2, 0.2F);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        "particle minecraft:campfire_cosy_smoke " + smokeLocation.getBlockX() + " "
                                + (player.getLocation().getY() + 1.5) + " "
                                + smokeLocation.getBlockZ());
                // item.setItemMeta(Items.getPipeItem().getItemMeta());
                item.setItemMeta(new ItemStack(Material.GOAT_HORN).getItemMeta());
                event.setCancelled(true);
            }
        }
    }
}
