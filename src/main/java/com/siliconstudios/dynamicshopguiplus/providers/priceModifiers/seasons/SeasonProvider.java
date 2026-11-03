package com.siliconstudios.dynamicshopguiplus.providers.priceModifiers.seasons;

import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.ModifierLoadException;

public class SeasonProvider {
    public SeasonProvider(DynamicShopGUIPlus plugin) throws ModifierLoadException {
        throw new ModifierLoadException("disabled");
    }

    public void reloadModifiers() {
    }

    public static Season getSeason(String world) {
        return new Season();
    }

    public static class Season {
        public String getIcon() {
            return "";
        }

        public String getName() {
            return "";
        }
    }
}
