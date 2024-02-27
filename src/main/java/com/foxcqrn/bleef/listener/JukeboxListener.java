package com.foxcqrn.bleef.listener;

import com.foxcqrn.bleef.Bleef;
import com.foxcqrn.bleef.util.SequencePlayer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import static com.foxcqrn.bleef.Bleef.plugin;

public class JukeboxListener implements Listener {

    private void playFromJukebox(Block block, ItemStack record) {
        ItemMeta meta = record.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer persistent = meta.getPersistentDataContainer();
        if (!persistent.has(new NamespacedKey(plugin, "sequence"), PersistentDataType.INTEGER)) return;
        int sequenceId = persistent.get(new NamespacedKey(plugin, "sequence"), PersistentDataType.INTEGER);

        try {
            if (block.getMetadata("sequence_playing").get(0).asBoolean()) return;
        } catch (IndexOutOfBoundsException ie) {
            // not set
        }

        block.setMetadata("sequence_playing", new FixedMetadataValue(Bleef.plugin, true));

        SequencePlayer sequencePlayer = new SequencePlayer();


        Thread thread = sequencePlayer.getThread(block.getLocation(), sequenceId, () -> {
            if (!block.getMetadata("sequence_playing").get(0).asBoolean()) {
                sequencePlayer.stop();
            }
            return null;
        }, () -> {
            block.setMetadata("sequence_playing", new FixedMetadataValue(Bleef.plugin, false));
            return null;
        });
        thread.start();
        BossBar bar = Bukkit.createBossBar(ChatColor.GREEN + "Now Playing: " + meta.getLore().get(0), BarColor.GREEN, BarStyle.SOLID);
        for (Player p : Bukkit.getOnlinePlayers()) {
            Location loc = p.getLocation();
            if (block.getWorld() != loc.getWorld() || loc.distance(block.getLocation()) > 40) continue;
            bar.addPlayer(p);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                bar.removeAll();
            }
        }.runTaskLater(plugin, 200);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = e.getClickedBlock();
        if (block == null) return;
        if (block.getType() != Material.JUKEBOX) return;
        Player player = e.getPlayer();
        if (((Jukebox) block.getState()).getRecord().getType() != Material.AIR) {
            ItemStack itemInvolvedInEvent = getItemStack(e, player);

            if (player.isSneaking() && !itemInvolvedInEvent.getType().equals(Material.AIR)) return;

            block.setMetadata("sequence_playing", new FixedMetadataValue(Bleef.plugin, false));
            return;
        }
        ItemStack record = e.getItem();
        if (record == null) return;

        playFromJukebox(block, record);
    }
//
//    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
//    public void onRecordTransfer(InventoryMoveItemEvent e) {
//        if (e.getDestination().getType() != InventoryType.JUKEBOX) {
//            if (e.getSource().getType() == InventoryType.JUKEBOX && e.getItem().getType().isRecord()) {
//                Objects.requireNonNull(e.getSource().getLocation()).getBlock()
//                        .setMetadata("sequence_playing", new FixedMetadataValue(Bleef.plugin, false));
//            }
//            return;
//        }
//        if (!e.getItem().getType().isRecord()) return;
//        playFromJukebox(Objects.requireNonNull(e.getSource().getLocation()).getBlock(), e.getItem());
//    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onJukeboxBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.JUKEBOX) return;
        block.setMetadata("sequence_playing", new FixedMetadataValue(Bleef.plugin, false));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onJukeboxExplode(EntityExplodeEvent event) {
        for (Block explodedBlock : event.blockList()) {
            if (explodedBlock.getType() == Material.JUKEBOX) {
                explodedBlock.setMetadata("sequence_playing", new FixedMetadataValue(Bleef.plugin, false));
            }
        }

    }

    private static ItemStack getItemStack(PlayerInteractEvent e, Player player) {
        ItemStack itemInvolvedInEvent;
        if (e.getMaterial().equals(Material.AIR)) {

            if (!player.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
                itemInvolvedInEvent = player.getInventory().getItemInMainHand();
            } else if (!player.getInventory().getItemInOffHand().getType().equals(Material.AIR)) {
                itemInvolvedInEvent = player.getInventory().getItemInOffHand();
            } else {
                itemInvolvedInEvent = new ItemStack(Material.AIR);
            }

        } else {
            itemInvolvedInEvent = new ItemStack(e.getMaterial());
        }
        return itemInvolvedInEvent;
    }
}
