

package com.siliconstudios.dynamicshopguiplus.api.prices;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import com.siliconstudios.dynamicshopguiplus.objects.ShopItem;
import com.siliconstudios.dynamicshopguiplus.util.EcoType;
import org.bukkit.entity.Player;

public class AdvancedBuyPrice {
    public AdvancedBuyPrice(ShopItem shopItem) {
    }

    public List<EcoType> getBuyTypes() {
        return null;
    }

    public boolean isBuyAble() {
        return false;
    }

    public boolean requireAll() {
        return false;
    }

    public Map<EcoType, Double> getBuyPrices(@Nullable EcoType ecoType, int amount) {
        return null;
    }

    public Map<EcoType, Double> getBuyPrices(@Nullable EcoType ecoType, Player player, int amount) {
        return null;
    }
}

