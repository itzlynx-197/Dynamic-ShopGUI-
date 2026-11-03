

package com.siliconstudios.dynamicshopguiplus.api.events;

import java.util.Collections;
import java.util.Map;
import javax.annotation.Nullable;
import com.siliconstudios.dynamicshopguiplus.api.events.CustomEvent;
import com.siliconstudios.dynamicshopguiplus.objects.ShopItem;
import com.siliconstudios.dynamicshopguiplus.util.EcoType;
import com.siliconstudios.dynamicshopguiplus.util.Transaction;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;

public class PreTransactionEvent
extends CustomEvent
implements Cancellable {
    private final int amount;
    private boolean cancelled;
    private double price;
    private final double originalPrice;
    private final ShopItem shopItem;
    private final Map<ShopItem, Integer> items;
    private final Map<EcoType, Double> prices;
    private final Map<EcoType, Double> originalPrices;
    private final Player player;
    private final Transaction.Type transactionType;

    public PreTransactionEvent(ShopItem shopItem, Player player, int amount, double price, Transaction.Type transactionType) {
        this.amount = amount;
        this.shopItem = shopItem;
        this.player = player;
        this.originalPrice = this.price = price;
        this.items = Collections.EMPTY_MAP;
        this.prices = Collections.EMPTY_MAP;
        this.originalPrices = this.prices;
        this.transactionType = transactionType;
    }

    public PreTransactionEvent(Map<ShopItem, Integer> items, Map<EcoType, Double> prices, Player player, int amount, Transaction.Type transactionType) {
        this.amount = amount;
        this.shopItem = (ShopItem)items.keySet().toArray()[0];
        this.player = player;
        this.originalPrice = this.price = prices.get(this.shopItem.getEcoType()).doubleValue();
        this.prices = prices;
        this.originalPrices = Collections.unmodifiableMap(this.prices);
        this.items = items;
        this.transactionType = transactionType;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public int getAmount() {
        return this.amount;
    }

    public double getPrice() {
        return this.price;
    }

    public final double getOriginalPrice() {
        return this.originalPrice;
    }

    public Map<EcoType, Double> getPrices() {
        return this.prices;
    }

    public final Map<EcoType, Double> getOriginalPrices() {
        return this.originalPrices;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Deprecated
    public ItemStack getItemStack() {
        return this.shopItem.getItemToGive();
    }

    @Nullable
    public ShopItem getShopItem() {
        return this.shopItem;
    }

    public Map<ShopItem, Integer> getItems() {
        return this.items;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Transaction.Type getTransactionType() {
        return this.transactionType;
    }
}

