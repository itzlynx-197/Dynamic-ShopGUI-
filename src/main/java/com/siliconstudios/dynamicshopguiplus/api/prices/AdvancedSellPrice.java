

package com.siliconstudios.dynamicshopguiplus.api.prices;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import com.siliconstudios.dynamicshopguiplus.objects.ShopItem;
import com.siliconstudios.dynamicshopguiplus.util.EcoType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AdvancedSellPrice {
    public AdvancedSellPrice(ShopItem shopItem) {
    }

    public List<EcoType> getSellTypes() {
        return null;
    }

    public boolean isSellAble() {
        return false;
    }

    public boolean giveAll() {
        return false;
    }

    public Map<EcoType, Double> getSellPrices(@Nullable EcoType ecoType, ItemStack item) {
        return null;
    }

    public Map<EcoType, Double> getSellPrices(@Nullable EcoType ecoType, ItemStack item, int amount, int sold) {
        return null;
    }

    public Map<EcoType, Double> getSellPrices(@Nullable EcoType ecoType, Player player, ItemStack item) {
        return null;
    }

    public Map<EcoType, Double> getSellPrices(@Nullable EcoType ecoType, Player player, ItemStack item, int amount, int sold) {
        return null;
    }
}

