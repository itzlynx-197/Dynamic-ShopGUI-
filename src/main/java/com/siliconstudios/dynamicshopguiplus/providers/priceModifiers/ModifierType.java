

package com.siliconstudios.dynamicshopguiplus.providers.priceModifiers;

import com.siliconstudios.dynamicshopguiplus.providers.priceModifiers.PriceModifier;
import com.siliconstudios.dynamicshopguiplus.providers.priceModifiers.seasons.SeasonModifier;

public enum ModifierType {
    SEASON(SeasonModifier.class);

    private final Class<? extends PriceModifier> clazz;

    private ModifierType(Class<? extends PriceModifier> clazz) {
        this.clazz = clazz;
    }

    public Class<? extends PriceModifier> getClazz() {
        return this.clazz;
    }
}

