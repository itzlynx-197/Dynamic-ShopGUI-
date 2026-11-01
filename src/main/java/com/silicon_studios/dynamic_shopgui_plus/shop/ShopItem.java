package com.silicon_studios.dynamic_shopgui_plus.shop;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ShopItem {
    private final String section;
    private final String id;
    private Material material;
    private String displayName;
    private java.util.List<String> lore = new java.util.ArrayList<>();
    private double buy = -1;
    private double sell = -1;
    private int stackSize = 1;
    private int minBuy = 0;
    private int maxBuy = 0;
    private int minSell = 0;
    private int maxSell = 0;
    private boolean closeMenu = false;
    private boolean hidden = false;
    private String subSection;
    private ItemStack itemStack;
    private java.util.List<com.silicon_studios.dynamic_shopgui_plus.features.multiprice.PriceTier> priceTiers = new java.util.ArrayList<>();
    private java.util.List<com.silicon_studios.dynamic_shopgui_plus.features.stock.StockLimit> stockLimits = new java.util.ArrayList<>();
    private com.silicon_studios.dynamic_shopgui_plus.features.commanditems.CommandItem commandItem;

    public ShopItem(String section, String id) {
        this.section = section;
        this.id = id;
    }

    public String getSection() { return section; }
    public String getId() { return id; }
    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public java.util.List<String> getLore() { return lore; }
    public void setLore(java.util.List<String> lore) { this.lore = lore; }
    public double getBuy() { return buy; }
    public void setBuy(double buy) { this.buy = buy; }
    public double getSell() { return sell; }
    public void setSell(double sell) { this.sell = sell; }
    public int getStackSize() { return stackSize; }
    public void setStackSize(int stackSize) { this.stackSize = stackSize; }
    public int getMinBuy() { return minBuy; }
    public void setMinBuy(int minBuy) { this.minBuy = minBuy; }
    public int getMaxBuy() { return maxBuy; }
    public void setMaxBuy(int maxBuy) { this.maxBuy = maxBuy; }
    public int getMinSell() { return minSell; }
    public void setMinSell(int minSell) { this.minSell = minSell; }
    public int getMaxSell() { return maxSell; }
    public void setMaxSell(int maxSell) { this.maxSell = maxSell; }
    public boolean isCloseMenu() { return closeMenu; }
    public void setCloseMenu(boolean closeMenu) { this.closeMenu = closeMenu; }
    public boolean isHidden() { return hidden; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }
    public String getSubSection() { return subSection; }
    public void setSubSection(String subSection) { this.subSection = subSection; }
    public ItemStack getItemStack() { return itemStack; }
    public void setItemStack(ItemStack itemStack) { this.itemStack = itemStack; }
    public boolean hasBuy() { return buy >= 0; }
    public boolean hasSell() { return sell >= 0; }
    public boolean isDisplayItem() { return !hasBuy() && !hasSell(); }
    public java.util.List<com.silicon_studios.dynamic_shopgui_plus.features.multiprice.PriceTier> getPriceTiers() { return priceTiers; }
    public void setPriceTiers(java.util.List<com.silicon_studios.dynamic_shopgui_plus.features.multiprice.PriceTier> priceTiers) { this.priceTiers = priceTiers; }
    public java.util.List<com.silicon_studios.dynamic_shopgui_plus.features.stock.StockLimit> getStockLimits() { return stockLimits; }
    public void setStockLimits(java.util.List<com.silicon_studios.dynamic_shopgui_plus.features.stock.StockLimit> stockLimits) { this.stockLimits = stockLimits; }
    public com.silicon_studios.dynamic_shopgui_plus.features.commanditems.CommandItem getCommandItem() { return commandItem; }
    public void setCommandItem(com.silicon_studios.dynamic_shopgui_plus.features.commanditems.CommandItem commandItem) { this.commandItem = commandItem; }
    public boolean isCommandItem() { return commandItem != null && commandItem.hasCommands(); }
}