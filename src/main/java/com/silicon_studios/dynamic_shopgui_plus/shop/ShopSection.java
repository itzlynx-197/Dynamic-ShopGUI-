package com.silicon_studios.dynamic_shopgui_plus.shop;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class ShopSection {
    private final String name;
    private int slot;
    private String title;
    private String bedrockTitle;
    private boolean hidden = false;
    private boolean subSection = false;
    private boolean displayItem = false;
    private boolean hideIfNoPerms = true;
    private String economy = "VAULT";
    private Material iconMaterial = Material.CHEST;
    private String iconName;
    private List<String> iconLore = new ArrayList<>();
    private List<ShopItem> items = new ArrayList<>();

    public ShopSection(String name) {
        this.name = name;
        this.title = name;
    }

    public String getName() { return name; }
    public int getSlot() { return slot; }
    public void setSlot(int slot) { this.slot = slot; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBedrockTitle() { return bedrockTitle; }
    public void setBedrockTitle(String bedrockTitle) { this.bedrockTitle = bedrockTitle; }
    public boolean isHidden() { return hidden; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }
    public boolean isSubSection() { return subSection; }
    public void setSubSection(boolean subSection) { this.subSection = subSection; }
    public boolean isDisplayItem() { return displayItem; }
    public void setDisplayItem(boolean displayItem) { this.displayItem = displayItem; }
    public boolean isHideIfNoPerms() { return hideIfNoPerms; }
    public void setHideIfNoPerms(boolean hideIfNoPerms) { this.hideIfNoPerms = hideIfNoPerms; }
    public String getEconomy() { return economy; }
    public void setEconomy(String economy) { this.economy = economy; }
    public Material getIconMaterial() { return iconMaterial; }
    public void setIconMaterial(Material iconMaterial) { this.iconMaterial = iconMaterial; }
    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }
    public List<String> getIconLore() { return iconLore; }
    public void setIconLore(List<String> iconLore) { this.iconLore = iconLore; }
    public List<ShopItem> getItems() { return items; }
    public void setItems(List<ShopItem> items) { this.items = items; }
    public void addItem(ShopItem item) { items.add(item); }
}