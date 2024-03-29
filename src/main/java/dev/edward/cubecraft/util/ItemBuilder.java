package dev.edward.cubecraft.util;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;

import java.util.*;

public class ItemBuilder {

    private final ItemStack item;

    public ItemBuilder(Material material) {
        this(material, 1);
    }

    public ItemBuilder(ItemStack item) {
        this.item = item;
    }

    public ItemBuilder(Material material, int amount) {
        item = new ItemStack(material, amount);
    }

    public ItemBuilder setDurability(short dur) {
        item.setDurability(dur);
        return this;
    }

    public ItemBuilder setName(String name) {
        if (name == null) {
            return this;
        }
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(name);
        item.setItemMeta(im);
        return this;
    }

    public ItemBuilder addUnsafeEnchantment(Enchantment ench, int level) {
        item.addUnsafeEnchantment(ench, level);
        return this;
    }

    public ItemBuilder removeEnchantment(Enchantment ench) {
        item.removeEnchantment(ench);
        return this;
    }

    public ItemBuilder setSkullOwner(String owner) {
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof SkullMeta)) {
            return this;
        }
        SkullMeta im = (SkullMeta) meta;
        item.setDurability((short) 3);
        im.setOwner(owner);
        item.setItemMeta(im);
        return this;
    }

    public ItemBuilder addEnchant(Enchantment enchantment, int level) {
        ItemMeta im = item.getItemMeta();
        im.addEnchant(enchantment, level, true);
        item.setItemMeta(im);
        return this;
    }

    public ItemBuilder addEnchants(HashMap<Enchantment, Integer> enchantments) {
        for (Map.Entry<Enchantment, Integer> enchant : enchantments.entrySet()) {
            addEnchant(enchant.getKey(), enchant.getValue());
        }
        return this;
    }

    public ItemBuilder addBookEnchantment(Enchantment enchantment, int level) {
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) meta;
            bookMeta.addStoredEnchant(enchantment, level, true);
            item.setItemMeta(bookMeta);
        }
        return this;
    }

    public ItemBuilder setLore(String... lore) {
        if (lore == null) {
            return this;
        }
        ItemMeta im = item.getItemMeta();
        im.setLore(Arrays.asList(lore));
        item.setItemMeta(im);
        return this;
    }

    public ItemBuilder addLore(String... lore) {
        ItemMeta meta = item.getItemMeta();
        List<String> current = meta.getLore();
        if (current == null) {
            current = new ArrayList<>();
        }
        current.addAll(Arrays.asList(lore));
        meta.setLore(current);
        item.setItemMeta(meta);
        return this;
    }

    @SuppressWarnings("deprecation")
    public ItemBuilder setWoolColour(DyeColor colour) {
        if (!item.getType().equals(Material.WOOL)) {
            return this;
        }
        this.item.setDurability(colour.getData());
        return this;
    }

    public ItemBuilder setLeatherArmorColor(Color colour) {
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof LeatherArmorMeta)) {
            return this;
        }
        LeatherArmorMeta im = (LeatherArmorMeta) meta;
        im.setColor(colour);
        item.setItemMeta(im);
        return this;
    }

    public ItemBuilder setAmount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemBuilder setUnbreakable(boolean unbreakable) {
        ItemMeta meta = item.getItemMeta();
        meta.spigot().setUnbreakable(unbreakable);
        item.setItemMeta(meta);
        return this;
    }

    public ItemStack toItemStack() {
        return item;
    }

    @Override
    public ItemBuilder clone() {
        return new ItemBuilder(item);
    }

    public ItemBuilder addCustomEffect(PotionEffect potionEffect, boolean b) {
        PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
        potionMeta.addCustomEffect(potionEffect, b);
        item.setItemMeta(potionMeta);
        return this;
    }
}


