

package com.siliconstudios.dynamicshopguiplus.objects;

import java.util.List;
import com.siliconstudios.dynamicshopguiplus.objects.TransactionItem;

public class TransactionScreen {
    private final List<TransactionItem> items;
    private final int size;

    public TransactionScreen(List<TransactionItem> items, int size) {
        this.items = items;
        this.size = size;
    }

    public List<TransactionItem> getItems() {
        return this.items;
    }

    public int getSize() {
        return this.size;
    }
}

