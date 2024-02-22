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

import java.util.*;
import java.util.Comparator;

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
                state instanceof Banner ||
                (data instanceof Piston && ((Piston) data).isExtended())) {
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
        if (!(data instanceof Rotatable) && !(data instanceof Directional) && !(data instanceof Orientable)) {
            player.sendMessage(ChatColor.RED + "That block can't be rotated.");
            return;
        }

        Enum<?> facing;
        List<? extends Enum<?>> rotations;
        List<BlockFace> clockwiseRotations = Arrays.asList(PluginUtil.clockwiseFaces);
        Comparator<BlockFace> clockwiseComparator = (face1, face2) -> {
            int index1 = clockwiseRotations.indexOf(face1);
            int index2 = clockwiseRotations.indexOf(face2);
            return index1 - index2;
        };

        if (data instanceof Rotatable) {
            rotations = new ArrayList<>(Arrays.asList(BlockFace.values()));
            rotations.remove(BlockFace.SELF);
            rotations.remove(BlockFace.UP);
            rotations.remove(BlockFace.DOWN);
            facing = ((Rotatable) data).getRotation();
        } else if (data instanceof Directional) {
            rotations = new ArrayList<>(((Directional) data).getFaces());
            facing = ((Directional) data).getFacing();
        } else {
            rotations = new ArrayList<>(((Orientable) data).getAxes());
            facing = ((Orientable) data).getAxis();
        }
        if (!(data instanceof Orientable)) {
            ((List<BlockFace>) rotations).sort(clockwiseComparator);
        }

        // Reverse the rotation if player is holding shift
        int index = (rotations.indexOf(facing) + (player.isSneaking() ? -1 : 1) + rotations.size()) % rotations.size();
        if (data instanceof Rotatable) {
            ((Rotatable) data).setRotation((BlockFace) rotations.get(index));
        } else if (data instanceof Directional) {
            ((Directional) data).setFacing((BlockFace) rotations.get(index));
        } else {
            ((Orientable) data).setAxis((Axis) rotations.get(index));
        }
        target.setBlockData(data);
    }
}
