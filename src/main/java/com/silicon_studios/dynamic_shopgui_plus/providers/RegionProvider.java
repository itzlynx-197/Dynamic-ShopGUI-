package com.silicon_studios.dynamic_shopgui_plus.providers;

import com.silicon_studios.dynamic_shopgui_plus.DynamicShopGUIPlus;

public class RegionProvider {
    private final DynamicShopGUIPlus plugin;

    public RegionProvider(DynamicShopGUIPlus plugin) {
        this.plugin = plugin;
    }

    public boolean isAvailable() {
        return org.bukkit.Bukkit.getPluginManager().getPlugin("WorldGuard") != null;
    }

    public boolean isInRegion(org.bukkit.entity.Player player, String regionName) {
        return true;
    }
}