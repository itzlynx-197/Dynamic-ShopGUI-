package com.silicon_studios.dynamic_shopgui_plus.providers;

import com.silicon_studios.dynamic_shopgui_plus.DynamicShopGUIPlus;

public class DiscordSRVProvider {
    private final DynamicShopGUIPlus plugin;

    public DiscordSRVProvider(DynamicShopGUIPlus plugin) {
        this.plugin = plugin;
    }

    public boolean isAvailable() {
        return org.bukkit.Bukkit.getPluginManager().getPlugin("DiscordSRV") != null;
    }

    public void logTransaction(String player, String action, int amount, String item, double price) {
        if (!isAvailable()) return;
    }
}