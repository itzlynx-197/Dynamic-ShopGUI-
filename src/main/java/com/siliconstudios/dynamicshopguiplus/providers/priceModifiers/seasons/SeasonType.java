

package com.siliconstudios.dynamicshopguiplus.providers.priceModifiers.seasons;

import java.util.Locale;

public enum SeasonType {
    SPRING,
    FALL,
    SUMMER,
    WINTER,
    DISABLED;


    public static SeasonType getFromString(String name) {
        try {
            return SeasonType.valueOf(name.toUpperCase(Locale.ENGLISH));
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static SeasonType get(Object season) {
        try {
            return SeasonType.valueOf(season.toString());
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }
}

