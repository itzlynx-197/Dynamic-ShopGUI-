

package com.siliconstudios.dynamicshopguiplus.api.objects;

import java.util.Collections;
import java.util.Map;
import com.siliconstudios.dynamicshopguiplus.objects.ShopItem;
import com.siliconstudios.dynamicshopguiplus.util.EcoType;
import com.siliconstudios.dynamicshopguiplus.util.Transaction;

public class TransactionResult {
    private final Transaction.Result result;
    private final Map<EcoType, Double> prices;
    private final int amount;

    public TransactionResult(Transaction.Result result) {
        this.result = result;
        this.prices = null;
        this.amount = 0;
    }

    public TransactionResult(Transaction.Result result, ShopItem item, double price, int amount) {
        this.result = result;
        this.prices = Collections.singletonMap(item.getEcoType(), price);
        this.amount = amount;
    }

    public Transaction.Result getResult() {
        return this.result;
    }

    public Map<EcoType, Double> getPrices() {
        return this.prices;
    }

    public int getAmount() {
        return this.amount;
    }
}

