package com.silicon_studios.dynamic_shopgui_plus.storage;

import com.silicon_studios.dynamic_shopgui_plus.DynamicShopGUIPlus;
import com.silicon_studios.dynamic_shopgui_plus.shop.ShopItem;
import com.silicon_studios.dynamic_shopgui_plus.shop.ShopManager;
import com.silicon_studios.dynamic_shopgui_plus.shop.ShopSection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SharedShopDataManager {

    private final DynamicShopGUIPlus plugin;
    private final SharedStorageManager storage;

    public SharedShopDataManager(DynamicShopGUIPlus plugin, SharedStorageManager storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    public boolean isSharedMode() {
        return storage != null && storage.isProxyMode() && storage.isConnected();
    }

    public void loadSharedShopData() {
        if (!isSharedMode()) return;
        try (Connection conn = storage.getConnection()) {
            if (conn == null) return;
            ShopManager sm = plugin.getShopManager();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT name, slot, title, hidden, sub_section, display_item, economy, icon_material, version FROM dsg_shop_sections")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String name = rs.getString("name");
                        ShopSection sec = sm.getSection(name);
                        if (sec == null) {
                            sec = new ShopSection(name);
                            sm.getAllSections().put(name, sec);
                        }
                        sec.setSlot(rs.getInt("slot"));
                        sec.setTitle(rs.getString("title"));
                        sec.setHidden(rs.getBoolean("hidden"));
                        sec.setSubSection(rs.getBoolean("sub_section"));
                        sec.setDisplayItem(rs.getBoolean("display_item"));
                        sec.setEconomy(rs.getString("economy"));
                        String iconMat = rs.getString("icon_material");
                        if (iconMat != null) {
                            try { sec.setIconMaterial(org.bukkit.Material.valueOf(iconMat)); } catch (Exception ignored) {}
                        }
                        storage.getSectionVersion(name);
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, section, material, display_name, lore, buy_price, sell_price, stack_size, " +
                            "min_buy, max_buy, min_sell, max_sell, hidden, close_menu, sub_section, version FROM dsg_shop_items")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String id = rs.getString("id");
                        String section = rs.getString("section");
                        ShopSection sec = sm.getSection(section);
                        if (sec == null) continue;
                        ShopItem item = new ShopItem(section, id);
                        try { item.setMaterial(org.bukkit.Material.valueOf(rs.getString("material"))); } catch (Exception ignored) {}
                        item.setDisplayName(rs.getString("display_name"));
                        String loreStr = rs.getString("lore");
                        if (loreStr != null && !loreStr.isEmpty()) {
                            item.setLore(java.util.Arrays.asList(loreStr.split("\\|\\|")));
                        }
                        item.setBuy(rs.getDouble("buy_price"));
                        item.setSell(rs.getDouble("sell_price"));
                        item.setStackSize(rs.getInt("stack_size"));
                        item.setMinBuy(rs.getInt("min_buy"));
                        item.setMaxBuy(rs.getInt("max_buy"));
                        item.setMinSell(rs.getInt("min_sell"));
                        item.setMaxSell(rs.getInt("max_sell"));
                        item.setHidden(rs.getBoolean("hidden"));
                        item.setCloseMenu(rs.getBoolean("close_menu"));
                        item.setSubSection(rs.getString("sub_section"));
                        if (item.getMaterial() != null) {
                            item.setItemStack(new org.bukkit.inventory.ItemStack(item.getMaterial(), item.getStackSize()));
                        }
                        sec.getItems().add(item);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to load shared shop data: " + e.getMessage());
        }
    }

    public void updateItemInCache(String section, String itemId) {
        if (!isSharedMode()) return;
        try (Connection conn = storage.getConnection()) {
            if (conn == null) return;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, section, material, display_name, lore, buy_price, sell_price, stack_size, " +
                            "min_buy, max_buy, min_sell, max_sell, hidden, close_menu, sub_section, version FROM dsg_shop_items " +
                            "WHERE id = ? AND section = ?")) {
                ps.setString(1, itemId);
                ps.setString(2, section);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        ShopSection sec = plugin.getShopManager().getSection(section);
                        if (sec == null) return;
                        for (int i = 0; i < sec.getItems().size(); i++) {
                            ShopItem item = sec.getItems().get(i);
                            if (item.getId().equals(itemId)) {
                                try { item.setMaterial(org.bukkit.Material.valueOf(rs.getString("material"))); } catch (Exception ignored) {}
                                item.setDisplayName(rs.getString("display_name"));
                                String loreStr = rs.getString("lore");
                                if (loreStr != null && !loreStr.isEmpty()) {
                                    item.setLore(java.util.Arrays.asList(loreStr.split("\\|\\|")));
                                } else {
                                    item.setLore(new java.util.ArrayList<>());
                                }
                                item.setBuy(rs.getDouble("buy_price"));
                                item.setSell(rs.getDouble("sell_price"));
                                item.setStackSize(rs.getInt("stack_size"));
                                item.setMinBuy(rs.getInt("min_buy"));
                                item.setMaxBuy(rs.getInt("max_buy"));
                                item.setMinSell(rs.getInt("min_sell"));
                                item.setMaxSell(rs.getInt("max_sell"));
                                item.setHidden(rs.getBoolean("hidden"));
                                item.setCloseMenu(rs.getBoolean("close_menu"));
                                item.setSubSection(rs.getString("sub_section"));
                                if (item.getMaterial() != null) {
                                    item.setItemStack(new org.bukkit.inventory.ItemStack(item.getMaterial(), item.getStackSize()));
                                }
                                break;
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to update item in cache: " + e.getMessage());
        }
    }

    public void updateSectionInCache(String sectionName) {
        if (!isSharedMode()) return;
        try (Connection conn = storage.getConnection()) {
            if (conn == null) return;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT name, slot, title, hidden, sub_section, display_item, economy, icon_material, version FROM dsg_shop_sections " +
                            "WHERE name = ?")) {
                ps.setString(1, sectionName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        ShopSection sec = plugin.getShopManager().getSection(sectionName);
                        if (sec == null) {
                            sec = new ShopSection(sectionName);
                            plugin.getShopManager().getAllSections().put(sectionName, sec);
                        }
                        sec.setSlot(rs.getInt("slot"));
                        sec.setTitle(rs.getString("title"));
                        sec.setHidden(rs.getBoolean("hidden"));
                        sec.setSubSection(rs.getBoolean("sub_section"));
                        sec.setDisplayItem(rs.getBoolean("display_item"));
                        sec.setEconomy(rs.getString("economy"));
                        String iconMat = rs.getString("icon_material");
                        if (iconMat != null) {
                            try { sec.setIconMaterial(org.bukkit.Material.valueOf(iconMat)); } catch (Exception ignored) {}
                        }
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to update section in cache: " + e.getMessage());
        }
    }

    public void removeItemFromCache(String section, String itemId) {
        ShopSection sec = plugin.getShopManager().getSection(section);
        if (sec == null) return;
        sec.getItems().removeIf(item -> item.getId().equals(itemId));
    }
}