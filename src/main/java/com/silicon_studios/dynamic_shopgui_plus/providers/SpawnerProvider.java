package com.silicon_studios.dynamic_shopgui_plus.providers;

import com.silicon_studios.dynamic_shopgui_plus.DynamicShopGUIPlus;
import org.bukkit.inventory.ItemStack;

public class SpawnerProvider {
    private final DynamicShopGUIPlus plugin;

    public SpawnerProvider(DynamicShopGUIPlus plugin) {
        this.plugin = plugin;
    }

    public boolean isAvailable() {
        return org.bukkit.Bukkit.getPluginManager().getPlugin("SilkSpawners") != null
            || org.bukkit.Bukkit.getPluginManager().getPlugin("SilkSpawners_v2") != null
            || org.bukkit.Bukkit.getPluginManager().getPlugin("WildStacker") != null
            || org.bukkit.Bukkit.getPluginManager().getPlugin("RoseStacker") != null;
    }

    public ItemStack createSpawner(String entityType) {
        return new ItemStack(org.bukkit.Material.SPAWNER);
    }
}