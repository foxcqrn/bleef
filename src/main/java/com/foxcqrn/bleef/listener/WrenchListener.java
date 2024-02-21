package com.foxcqrn.bleef.listener;

import com.foxcqrn.bleef.PluginUtil;
import org.bukkit.Axis;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.type.*;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WrenchListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Block target = event.getClickedBlock();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (item == null || target == null) return;
        if (!"WRENCH".equals(PluginUtil.getDataType(item.getItemMeta()))) return;

        event.setCancelled(true);

        if (!player.hasPermission("bleef.wrench.bypass")) {
            BlockData data = target.getBlockData();
            BlockState state = target.getState();
            if (data instanceof WallSign ||
                data instanceof WallHangingSign ||
                data instanceof AmethystCluster ||
                data instanceof Switch ||
                data instanceof RedstoneWallTorch ||
                data instanceof Bed ||
                data instanceof CoralWallFan ||
                state instanceof Skull ||
                state instanceof Banner) {
                player.sendMessage(ChatColor.RED + "You can't rotate that block.");
                return;
            }
            for (Material m : PluginUtil.WrenchBlacklist) {
                if (m == target.getType()) {
                    player.sendMessage(ChatColor.RED + "You can't rotate that block.");
                    return;
                }
            }
        }

        BlockData data = target.getBlockData();
        if (data instanceof Rotatable) {
            Rotatable block = (Rotatable) data;
            List<BlockFace> rotations = new ArrayList<>(Arrays.asList(BlockFace.values()));
            rotations.remove(BlockFace.SELF); // illegal rotations
            rotations.remove(BlockFace.UP);
            rotations.remove(BlockFace.DOWN);
            int startIndex = rotations.indexOf(block.getRotation());

            for (int i = startIndex; i < rotations.size() + startIndex; i++) {
                int index = i % rotations.size(); // wrap to beginning
                block.setRotation(rotations.get(index));
                target.setBlockData(data);
            }
        } else if (data instanceof Directional) {
            Directional block = (Directional) data;
            List<BlockFace> faces = new ArrayList<>(block.getFaces());
            int startIndex = faces.indexOf(block.getFacing());

            for (int i = startIndex; i < faces.size() + startIndex; i++) {
                int index = i % faces.size();
                block.setFacing(faces.get(index));
                target.setBlockData(data);
            }
        } else if (data instanceof Orientable) {
            Orientable block = (Orientable) data;
            List<Axis> axes = new ArrayList<>(block.getAxes());
            int startIndex = axes.indexOf(block.getAxis());

            for (int i = startIndex; i < axes.size() + startIndex; i++) {
                int index = i % axes.size();
                block.setAxis(axes.get(index));
                target.setBlockData(data);
            }
        } else {
            player.sendMessage(ChatColor.RED + "That block can't be rotated.");
        }
    }
}
