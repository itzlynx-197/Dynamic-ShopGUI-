

package com.siliconstudios.dynamicshopguiplus.providers.priceModifiers;

import java.util.ArrayList;
import com.siliconstudios.dynamicshopguiplus.providers.priceModifiers.ModifierType;
import com.siliconstudios.dynamicshopguiplus.providers.priceModifiers.PriceModifier;
import com.siliconstudios.dynamicshopguiplus.providers.priceModifiers.seasons.SeasonModifier;

public class Modifiers
extends ArrayList<PriceModifier> {
    public PriceModifier getByType(ModifierType type) {
        for (PriceModifier modifier : this) {
            if (modifier.getClass() != type.getClazz()) continue;
            return modifier;
        }
        return null;
    }

    public boolean hasType(ModifierType type) {
        for (PriceModifier modifier : this) {
            if (modifier.getClass() != type.getClazz()) continue;
            return true;
        }
        return false;
    }

    public boolean removeType(ModifierType type) {
        for (PriceModifier modifier : this) {
            if (modifier.getClass() != type.getClazz()) continue;
            return super.remove(modifier);
        }
        return false;
    }

    public boolean hasSeasonModifier(String world) {
        for (PriceModifier modifier : this) {
            if (!(modifier instanceof SeasonModifier) || !((SeasonModifier)modifier).getWorld().equals(world)) continue;
            return true;
        }
        return false;
    }
}

