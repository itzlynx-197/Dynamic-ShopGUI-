

package com.siliconstudios.dynamicshopguiplus.util;

import java.util.HashSet;
import java.util.Set;

public class Sold {
    private int amount = 0;
    private double price = 0.0;
    private final Set<Integer> indexes = new HashSet<Integer>();

    public Set<Integer> getIndexes() {
        return this.indexes;
    }

    public int getAmount() {
        return this.amount;
    }

    public double getPrice() {
        return this.price;
    }

    public void addIndex(int index) {
        this.indexes.add(index);
    }

    public Sold addValues(int amount, double price) {
        this.amount += amount;
        this.price += price;
        return this;
    }

    public Sold addValue(Sold sold) {
        this.amount += sold.amount;
        this.price += sold.price;
        return this;
    }

    public Sold addAmount(int amount) {
        this.amount += amount;
        return this;
    }

    public Sold addPrice(double price) {
        this.price += price;
        return this;
    }
}

