

package com.siliconstudios.dynamicshopguiplus.api.events;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import com.siliconstudios.dynamicshopguiplus.api.events.CustomEvent;
import com.siliconstudios.dynamicshopguiplus.objects.ShopItem;
import com.siliconstudios.dynamicshopguiplus.util.EcoType;
import com.siliconstudios.dynamicshopguiplus.util.Transaction;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PostTransactionEvent
extends CustomEvent {
    private final int amount;
    private final double price;
    private final ShopItem shopItem;
    private final Map<ShopItem, Integer> items;
    private final Map<EcoType, Double> prices;
    private final Player player;
    private final Transaction.Type type;
    private final Transaction.Result result;

    public PostTransactionEvent(ShopItem shopItem, Player player, int amount, double price, Transaction.Type type, Transaction.Result result) {
        this.amount = amount;
        this.shopItem = shopItem;
        this.player = player;
        this.price = price;
        this.type = type;
        this.result = result;
        this.items = new HashMap<ShopItem, Integer>();
        this.prices = new HashMap<EcoType, Double>();
    }

    public PostTransactionEvent(Map<ShopItem, Integer> items, Map<EcoType, Double> prices, Player player, int amount, Transaction.Type type, Transaction.Result result) {
        this.amount = amount;
        this.shopItem = items.size() >= 1 ? (ShopItem)items.keySet().toArray()[0] : null;
        this.player = player;
        this.price = this.shopItem != null ? prices.get(this.shopItem.getEcoType()) : 0.0;
        this.type = type;
        this.result = result;
        this.items = items;
        this.prices = prices;
    }

    public int getAmount() {
        return this.amount;
    }

    public double getPrice() {
        return this.price;
    }

    @Nullable
    public Map<EcoType, Double> getPrices() {
        return this.prices;
    }

    public ItemStack getItemStack() {
        return this.shopItem.getItemToGive();
    }

    public ShopItem getShopItem() {
        return this.shopItem;
    }

    @Nullable
    public Map<ShopItem, Integer> getItems() {
        return this.items;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Transaction.Type getTransactionType() {
        return this.type;
    }

    public Transaction.Result getTransactionResult() {
        return this.result;
    }
}

