

package com.siliconstudios.dynamicshopguiplus.providers;

import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.providers.priceModifiers.seasons.SeasonProvider;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.ModifierLoadException;

public class ModifierManager {
    private final DynamicShopGUIPlus plugin;
    private static SeasonProvider seasonProvider;

    public ModifierManager(DynamicShopGUIPlus plugin) {
        this.plugin = plugin;
        this.loadModifiers();
    }

    private void loadModifiers() {
        this.loadSeasonModifier();
    }

    private void loadSeasonModifier() {
        if (ConfigManager.getConfig().getBoolean("enable-seasonal-pricing", false)) {
            try {
                SendMessage.infoMessage("Loading season modifiers...");
                seasonProvider = new SeasonProvider(this.plugin);
            }
            catch (ModifierLoadException e) {
                SendMessage.errorMessage("Failed to load SeasonModifiers from config for reason: " + e.getMessage());
            }
        }
    }

    public boolean hasSeasonalPrices() {
        return seasonProvider != null;
    }

    public void reloadModifiers() {
        if (this.hasSeasonalPrices()) {
            seasonProvider.reloadModifiers();
        } else {
            this.loadSeasonModifier();
        }
    }
}

