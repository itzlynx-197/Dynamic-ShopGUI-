package com.silicon_studios.dynamic_shopgui_plus.providers;

import com.silicon_studios.dynamic_shopgui_plus.DynamicShopGUIPlus;

public class SeasonProvider {
    private final DynamicShopGUIPlus plugin;

    public SeasonProvider(DynamicShopGUIPlus plugin) {
        this.plugin = plugin;
    }

    public boolean isAvailable() {
        return org.bukkit.Bukkit.getPluginManager().getPlugin("RealisticSeasons") != null;
    }

    public String getCurrentSeason() {
        return "Spring";
    }
}