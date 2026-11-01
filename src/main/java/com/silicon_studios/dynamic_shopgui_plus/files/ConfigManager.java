package com.silicon_studios.dynamic_shopgui_plus.files;

import com.silicon_studios.dynamic_shopgui_plus.DynamicShopGUIPlus;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    private static FileConfiguration config;
    private static File configFile;

    public static void loadConfig() {
        DynamicShopGUIPlus plugin = DynamicShopGUIPlus.getInstance();
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public static FileConfiguration getConfig() {
        if (config == null) loadConfig();
        return config;
    }

    public static void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            DynamicShopGUIPlus.getInstance().getLogger().severe(e.getMessage());
        }
    }

    public static void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }
}