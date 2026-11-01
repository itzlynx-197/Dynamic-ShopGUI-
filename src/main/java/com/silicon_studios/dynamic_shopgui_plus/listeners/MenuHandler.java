package com.silicon_studios.dynamic_shopgui_plus.listeners;

import com.silicon_studios.dynamic_shopgui_plus.DynamicShopGUIPlus;
import com.silicon_studios.dynamic_shopgui_plus.shop.ShopItem;
import com.silicon_studios.dynamic_shopgui_plus.shop.ShopManager;
import com.silicon_studios.dynamic_shopgui_plus.shop.ShopSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MenuHandler implements Listener {
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (!(event.getWhoClicked() instanceof org.bukkit.entity.Player)) return;
        org.bukkit.entity.Player player = (org.bukkit.entity.Player) event.getWhoClicked();
        event.setCancelled(true);
        org.bukkit.inventory.ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == org.bukkit.Material.AIR) return;
        String title = event.getView().getTitle();
        ShopManager sm = DynamicShopGUIPlus.getInstance().getShopManager();
        ShopSection matched = null;
        for (ShopSection sec : sm.getAllSections().values()) {
            String secTitle = sec.getTitle() != null ? sec.getTitle() : sec.getName();
            if (title.equals(secTitle)) {
                matched = sec;
                break;
            }
        }
        if (matched != null) {
            int idx = 0;
            for (ShopItem item : matched.getItems()) {
                if (item.isHidden()) continue;
                if (idx == event.getSlot()) {
                    if (item.getSubSection() != null) {
                        ShopSection sub = sm.getSection(item.getSubSection());
                        if (sub != null) {
                            openSection(player, sub);
                            return;
                        }
                    }
                    if (item.hasBuy()) {
                        player.sendMessage("Buy: " + item.getBuy() + " - " + item.getDisplayName());
                    }
                    if (item.hasSell()) {
                        player.sendMessage("Sell: " + item.getSell() + " - " + item.getDisplayName());
                    }
                    if (item.isCloseMenu()) {
                        player.closeInventory();
                    }
                    return;
                }
                idx++;
            }
            return;
        }
        if (title.equals("Shop")) {
            int idx = 0;
            for (ShopSection sec : sm.getAllSections().values()) {
                if (sec.isHidden() || sec.isSubSection()) continue;
                if (idx == event.getSlot()) {
                    openSection(player, sec);
                    return;
                }
                idx++;
            }
        }
    }

    private void openSection(org.bukkit.entity.Player player, ShopSection sec) {
        org.bukkit.inventory.Inventory inv = org.bukkit.Bukkit.createInventory(null, 54, sec.getTitle() != null ? sec.getTitle() : sec.getName());
        int slot = 0;
        for (ShopItem item : sec.getItems()) {
            if (item.isHidden()) continue;
            if (item.getItemStack() != null && slot < 54) {
                org.bukkit.inventory.ItemStack is = item.getItemStack().clone();
                org.bukkit.inventory.meta.ItemMeta meta = is.getItemMeta();
                if (meta != null) {
                    if (item.getDisplayName() != null) meta.setDisplayName(item.getDisplayName());
                    java.util.List<String> lore = new java.util.ArrayList<>();
                    if (item.getLore() != null) lore.addAll(item.getLore());
                    if (item.hasBuy()) lore.add("Buy: " + item.getBuy());
                    if (item.hasSell()) lore.add("Sell: " + item.getSell());
                    meta.setLore(lore);
                    is.setItemMeta(meta);
                }
                inv.setItem(slot++, is);
            }
        }
        player.openInventory(inv);
    }
}