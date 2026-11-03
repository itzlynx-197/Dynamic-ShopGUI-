

package com.siliconstudios.dynamicshopguiplus.util;

import java.util.Map;
import org.bukkit.inventory.ItemStack;

public class SmartStack {
    private final ItemStack item;

    public SmartStack(ItemStack item) {
        this.item = item;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public static class Shulker
    extends SmartStack {
        private final Map<Integer, ItemStack> items;

        public Shulker(ItemStack item, Map<Integer, ItemStack> items) {
            super(item);
            this.items = items;
        }

        public Map<Integer, ItemStack> getItems() {
            return this.items;
        }
    }
}

