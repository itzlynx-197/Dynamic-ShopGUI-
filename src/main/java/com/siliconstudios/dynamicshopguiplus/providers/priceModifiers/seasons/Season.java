

package com.siliconstudios.dynamicshopguiplus.providers.priceModifiers.seasons;

import com.siliconstudios.dynamicshopguiplus.providers.priceModifiers.seasons.SeasonType;

public class Season {
    private final String icon;
    private final String name;
    private final SeasonType type;

    public Season(String icon, String name, SeasonType type) {
        this.icon = icon;
        this.name = name;
        this.type = type;
    }

    public String getIcon() {
        return this.icon;
    }

    public String getName() {
        return this.name;
    }

    public SeasonType getType() {
        return this.type;
    }
}

