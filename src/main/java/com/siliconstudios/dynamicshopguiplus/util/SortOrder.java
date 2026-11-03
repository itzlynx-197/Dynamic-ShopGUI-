

package com.siliconstudios.dynamicshopguiplus.util;

import java.util.Locale;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;

public enum SortOrder {
    SHOP_LOAD_ORDER,
    ASCENDING_SELL_PRICE,
    DESCENDING_SELL_PRICE;


    public static SortOrder fromConfig() {
        String order = ConfigManager.getConfig().getString("sort-items-by", "DESCENDING_PRICE");
        try {
            return SortOrder.valueOf(order.toUpperCase(Locale.ROOT));
        }
        catch (IllegalArgumentException e) {
            return DESCENDING_SELL_PRICE;
        }
    }
}

