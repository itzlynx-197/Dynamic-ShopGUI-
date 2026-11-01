package com.silicon_studios.dynamic_shopgui_plus.commands;

import com.silicon_studios.dynamic_shopgui_plus.DynamicShopGUIPlus;
import com.silicon_studios.dynamic_shopgui_plus.shop.ShopManager;
import com.silicon_studios.dynamic_shopgui_plus.shop.ShopSection;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        ShopManager sm = DynamicShopGUIPlus.getInstance().getShopManager();
        if (args.length == 0) {
            openMainMenu(player, sm);
            return true;
        }
        ShopSection sec = sm.getSection(args[0]);
        if (sec == null) {
            player.sendMessage("Section not found: " + args[0]);
            return true;
        }
        openSection(player, sec);
        return true;
    }

    private void openMainMenu(Player player, ShopManager sm) {
        org.bukkit.inventory.Inventory inv = org.bukkit.Bukkit.createInventory(null, 54, "Shop");
        int slot = 0;
        for (ShopSection sec : sm.getAllSections().values()) {
            if (sec.isHidden()) continue;
            if (sec.isSubSection()) continue;
            if (slot >= 54) break;
            org.bukkit.inventory.ItemStack icon = new org.bukkit.inventory.ItemStack(sec.getIconMaterial());
            org.bukkit.inventory.meta.ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(sec.getTitle() != null ? sec.getTitle() : sec.getName());
                if (sec.getIconLore() != null && !sec.getIconLore().isEmpty()) {
                    meta.setLore(sec.getIconLore());
                }
                icon.setItemMeta(meta);
            }
            inv.setItem(slot, icon);
            slot++;
        }
        player.openInventory(inv);
    }

    private void openSection(Player player, ShopSection sec) {
        org.bukkit.inventory.Inventory inv = org.bukkit.Bukkit.createInventory(null, 54, sec.getTitle() != null ? sec.getTitle() : sec.getName());
        int slot = 0;
        for (com.silicon_studios.dynamic_shopgui_plus.shop.ShopItem item : sec.getItems()) {
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