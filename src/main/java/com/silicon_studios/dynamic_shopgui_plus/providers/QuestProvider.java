package com.silicon_studios.dynamic_shopgui_plus.providers;

import com.silicon_studios.dynamic_shopgui_plus.DynamicShopGUIPlus;

public class QuestProvider {
    private final DynamicShopGUIPlus plugin;

    public QuestProvider(DynamicShopGUIPlus plugin) {
        this.plugin = plugin;
    }

    public boolean isAvailable() {
        return org.bukkit.Bukkit.getPluginManager().getPlugin("Quests") != null
            || org.bukkit.Bukkit.getPluginManager().getPlugin("BeautyQuests") != null
            || org.bukkit.Bukkit.getPluginManager().getPlugin("BetonQuest") != null;
    }

    public boolean hasCompleted(org.bukkit.entity.Player player, String questId) {
        return true;
    }
}