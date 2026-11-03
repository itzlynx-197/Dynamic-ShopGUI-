package com.siliconstudios.dynamicshopguiplus.providers;

import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import org.bukkit.configuration.ConfigurationSection;

public class DiscordSRVProvider {
    public boolean enabled;

    public DiscordSRVProvider(DynamicShopGUIPlus plugin) {
    }

    public DiscordSRVProvider(DynamicShopGUIPlus plugin, ConfigurationSection section) {
    }

    public boolean init(ConfigurationSection section) {
        return false;
    }

    public void logTransaction(org.bukkit.entity.Player player, int amount, String transactionMode, String items, String price) {
    }
}
