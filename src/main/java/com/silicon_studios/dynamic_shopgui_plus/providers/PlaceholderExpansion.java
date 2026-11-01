package com.silicon_studios.dynamic_shopgui_plus.providers;

import com.silicon_studios.dynamic_shopgui_plus.DynamicShopGUIPlus;

import org.bukkit.entity.Player;

public class PlaceholderExpansion extends me.clip.placeholderapi.expansion.PlaceholderExpansion {
    private final DynamicShopGUIPlus plugin;

    public PlaceholderExpansion(DynamicShopGUIPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "dsg";
    }

    @Override
    public String getAuthor() {
        return "Silicon Studios";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) return "";
        if (params.startsWith("balance")) {
            if (plugin.getEconomyHandler().hasEconomy()) {
                return String.valueOf(plugin.getEconomyHandler().getBalance(player));
            }
        }
        return null;
    }
}