package com.silicon_studios.dynamic_shopgui_plus.features.guieditor;

import com.silicon_studios.dynamic_shopgui_plus.DynamicShopGUIPlus;
import com.silicon_studios.dynamic_shopgui_plus.shop.ShopManager;
import com.silicon_studios.dynamic_shopgui_plus.shop.ShopSection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EditorMenu {
    private static final String TITLE_SECTIONS = ChatColor.DARK_GREEN + "Shop Editor";
    private static final Map<UUID, String> sessions = new HashMap<>();

    public static void openMain(DynamicShopGUIPlus plugin, Player p, String section) {
        if (section != null) {
            openSection(plugin, p, section);
            return;
        }
        Inventory inv = Bukkit.createInventory(null, 54, TITLE_SECTIONS);
        int slot = 0;
        for (String sec : DynamicShopGUIPlus.getInstance().getShopManager().getSections()) {
            if (slot >= 45) break;
            ItemStack it = new ItemStack(Material.BOOK);
            ItemMeta m = it.getItemMeta();
            m.setDisplayName(ChatColor.YELLOW + sec);
            m.setLore(Arrays.asList(ChatColor.GRAY + "Click to edit"));
            it.setItemMeta(m);
            inv.setItem(slot++, it);
        }
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta cm = close.getItemMeta();
        cm.setDisplayName(ChatColor.RED + "Close");
        close.setItemMeta(cm);
        inv.setItem(53, close);
        p.openInventory(inv);
    }

    public static void openSection(DynamicShopGUIPlus plugin, Player p, String section) {
        sessions.put(p.getUniqueId(), section);
        ShopSection sec = DynamicShopGUIPlus.getInstance().getShopManager().getSection(section);
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "Editor - " + section);
        int slot = 0;
        if (sec != null) {
            for (com.silicon_studios.dynamic_shopgui_plus.shop.ShopItem item : sec.getItems()) {
                if (slot >= 45) break;
                Material m = item.getMaterial() != null ? item.getMaterial() : Material.STONE;
                ItemStack stack = new ItemStack(m);
                ItemMeta meta = stack.getItemMeta();
                meta.setDisplayName(ChatColor.WHITE + item.getId());
                java.util.List<String> lore = new java.util.ArrayList<>();
                if (item.hasBuy()) lore.add(ChatColor.GREEN + "Buy: " + item.getBuy());
                if (item.hasSell()) lore.add(ChatColor.RED + "Sell: " + item.getSell());
                lore.add(ChatColor.DARK_GRAY + "Click to edit");
                meta.setLore(lore);
                stack.setItemMeta(meta);
                inv.setItem(slot++, stack);
            }
        }
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta bm = back.getItemMeta();
        bm.setDisplayName(ChatColor.YELLOW + "Back");
        back.setItemMeta(bm);
        inv.setItem(45, back);
        p.openInventory(inv);
    }

    public static void handleClick(DynamicShopGUIPlus plugin, InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        e.setCancelled(true);
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        String title = e.getView().getTitle();
        if (title.equals(ChatColor.DARK_GREEN + "Shop Editor")) {
            if (clicked.getType() == Material.BARRIER) { p.closeInventory(); return; }
            String sec = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            openSection(plugin, p, sec);
        } else if (title.startsWith(ChatColor.DARK_GREEN + "Editor - ")) {
            if (clicked.getType() == Material.ARROW) { openMain(plugin, p, null); return; }
        }
    }

    public static void handleClose(InventoryCloseEvent e) {}
}