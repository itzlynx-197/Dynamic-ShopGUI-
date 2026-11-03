

package com.siliconstudios.dynamicshopguiplus.api.objects;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.siliconstudios.dynamicshopguiplus.objects.ShopItem;
import com.siliconstudios.dynamicshopguiplus.util.EcoType;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class SellPrice {
    private final OfflinePlayer player;
    private final int amount;
    private final ShopItem shopItem;
    private final Map<EcoType, Double> prices;

    public SellPrice(OfflinePlayer player, int amount, ShopItem shopItem, Map<EcoType, Double> prices) {
        this.player = player;
        this.amount = amount;
        this.shopItem = shopItem;
        this.prices = prices;
    }

    public SellPrice(OfflinePlayer player, int amount, ShopItem shopItem, EcoType ecoType, double price) {
        this.player = player;
        this.amount = amount;
        this.shopItem = shopItem;
        this.prices = new HashMap<EcoType, Double>(Collections.singletonMap(ecoType, price));
    }

    public OfflinePlayer getPlayer() {
        return this.player;
    }

    public int getAmount() {
        return this.amount;
    }

    public ShopItem getShopItem() {
        return this.shopItem;
    }

    public Map<EcoType, Double> getPrices() {
        return this.prices;
    }

    public double getPrice(@NotNull EcoType ecoType) {
        return this.prices.getOrDefault(ecoType, -1.0);
    }

    public SellPrice updateLimits() {
        return this;
    }
}

