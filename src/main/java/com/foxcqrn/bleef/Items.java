package com.foxcqrn.bleef;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import static com.foxcqrn.bleef.Bleef.plugin;

public class Items {
    private static final Recipe[] recipes = new Recipe[]{
            fleshBlockRecipe(),
            rottenFleshRecipe(),
            pipeWithWeedRecipe(),
            horseCompassRecipe(),
            wrenchRecipe(),
    };
    public static void add() {
        for (Recipe r : recipes) {
            Bukkit.addRecipe(r);
        }
    }
    public static void remove() {
        for (Recipe r : recipes) {
            Bukkit.removeRecipe(new NamespacedKey(plugin, PluginUtil.getDataType(r.getResult().getItemMeta())));
        }
    }

    public static ItemStack getPipeItem() {
        ItemStack i = new ItemStack(Material.GOAT_HORN);
        ItemMeta im = i.getItemMeta();
        assert im != null;
        im.setDisplayName(ChatColor.YELLOW + "Pipe");
        PluginUtil.setDataType(im, "PIPE");
        i.setItemMeta(im);
        return i;
    }

    public static ItemStack getPipeWithWeedItem() {
        ItemStack i = new ItemStack(Material.GOAT_HORN);
        ItemMeta im = i.getItemMeta();
        Glow glow = new Glow(new NamespacedKey(plugin, "glow"));
        assert im != null;
        im.setDisplayName(ChatColor.GREEN + "Pipe with Weed");
        im.addEnchant(glow, 1, true);
        PluginUtil.setDataType(im, "WEED_PIPE");
        i.setItemMeta(im);
        return i;
    }

    public static ItemStack getRottenFleshBlockItem() {
        ItemStack i = new ItemStack(Material.NETHERRACK);
        ItemMeta im = i.getItemMeta();
        assert im != null;
        im.setDisplayName(ChatColor.WHITE + "Block of Rotten Flesh");
        PluginUtil.setDataType(im, "FLESH_BLOCK");
        i.setItemMeta(im);
        return i;
    }

    public static ShapedRecipe fleshBlockRecipe() {
        NamespacedKey fleshblockkey = new NamespacedKey(plugin, "rotten_flesh_block");
        ShapedRecipe fleshblockrecipe = new ShapedRecipe(fleshblockkey, getRottenFleshBlockItem());
        fleshblockrecipe.shape("***", "***", "***");
        fleshblockrecipe.setIngredient('*', Material.ROTTEN_FLESH);
        return fleshblockrecipe;
    }

    public static ShapelessRecipe rottenFleshRecipe() {
        ItemStack flesh = new ItemStack(Material.ROTTEN_FLESH, 9);
        NamespacedKey fleshkey = new NamespacedKey(plugin, "rotten_flesh_from_rotten_flesh_block");
        ShapelessRecipe fleshrecipe = new ShapelessRecipe(fleshkey, flesh);
        fleshrecipe.addIngredient(new RecipeChoice.ExactChoice(getRottenFleshBlockItem()));
        return fleshrecipe;
    }

    public static ShapelessRecipe pipeWithWeedRecipe() {
        NamespacedKey pipeKey = new NamespacedKey(plugin, "pipe_with_weed");
        ShapelessRecipe pipeRecipe = new ShapelessRecipe(pipeKey, getPipeWithWeedItem());
        pipeRecipe.addIngredient(Material.GOAT_HORN);
        pipeRecipe.addIngredient(Material.DRIED_KELP_BLOCK);
        return pipeRecipe;
    }

    public static ItemStack getHorseCompassItem() {
        ItemStack i = new ItemStack(Material.COMPASS);
        ItemMeta im = i.getItemMeta();
        assert im != null;
        im.setDisplayName(ChatColor.WHITE + "honse finder");
        PluginUtil.setDataType(im, "HORSE_COMPASS");
        i.setItemMeta(im);
        return i;
    }

    public static ShapedRecipe horseCompassRecipe() {
        NamespacedKey horseCompassKey = new NamespacedKey(plugin, "horse_compass");
        ShapedRecipe horseCompassRecipe = new ShapedRecipe(horseCompassKey, getHorseCompassItem());
        horseCompassRecipe.shape(" i ", "iri", " l ");
        horseCompassRecipe.setIngredient('i', Material.IRON_INGOT);
        horseCompassRecipe.setIngredient('r', Material.REDSTONE);
        horseCompassRecipe.setIngredient('l', Material.LEATHER);
        return horseCompassRecipe;
    }

    public static ItemStack getWrenchItem() {
        ItemStack i = new ItemStack(Material.TRIPWIRE_HOOK);
        ItemMeta im = i.getItemMeta();
        assert im != null;
        im.setDisplayName(ChatColor.WHITE + "Wrench");
        PluginUtil.setDataType(im, "WRENCH");
        i.setItemMeta(im);
        return i;
    }

    public static ShapelessRecipe wrenchRecipe() {
        NamespacedKey wrenchKey = new NamespacedKey(plugin, "wrench");
        ShapelessRecipe wrenchRecipe = new ShapelessRecipe(wrenchKey, getWrenchItem());
        wrenchRecipe.addIngredient(Material.IRON_INGOT);
        wrenchRecipe.addIngredient(Material.IRON_NUGGET);
        return wrenchRecipe;
    }
}