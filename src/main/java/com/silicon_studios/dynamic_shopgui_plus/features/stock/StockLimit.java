package com.silicon_studios.dynamic_shopgui_plus.features.stock;

public class StockLimit {
    public enum Mode { PLAYER, GLOBAL }

    private final Mode mode;
    private final int maxAmount;
    private final long windowTicks;
    private final boolean buyLimit;

    public StockLimit(Mode mode, int maxAmount, long windowTicks, boolean buyLimit) {
        this.mode = mode;
        this.maxAmount = maxAmount;
        this.windowTicks = windowTicks;
        this.buyLimit = buyLimit;
    }

    public Mode getMode() { return mode; }
    public int getMaxAmount() { return maxAmount; }
    public long getWindowTicks() { return windowTicks; }
    public boolean isBuyLimit() { return buyLimit; }
}