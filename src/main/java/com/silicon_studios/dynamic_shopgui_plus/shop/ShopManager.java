package com.silicon_studios.dynamic_shopgui_plus.shop;

import com.silicon_studios.dynamic_shopgui_plus.DynamicShopGUIPlus;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ShopManager {
    private final Map<String, ShopSection> sections = new HashMap<>();
    private final Map<String, List<ShopItem>> items = new HashMap<>();

    public void load() {
        sections.clear();
        items.clear();
        File sectionsDir = new File(DynamicShopGUIPlus.getInstance().getDataFolder(), "sections");
        File shopsDir = new File(DynamicShopGUIPlus.getInstance().getDataFolder(), "shops");
        if (!sectionsDir.exists()) {
            sectionsDir.mkdirs();
        }
        if (!shopsDir.exists()) {
            shopsDir.mkdirs();
        }
        loadSections(sectionsDir);
        loadShops(shopsDir);
    }

    private void loadSections(File dir) {
        if (dir == null || !dir.isDirectory()) return;
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                loadSections(f);
            } else if (f.getName().endsWith(".yml")) {
                FileConfiguration cfg = YamlConfiguration.loadConfiguration(f);
                String name = f.getName().replace(".yml", "");
                ShopSection sec = new ShopSection(name);
                sec.setSlot(cfg.getInt("slot", 0));
                sec.setTitle(cfg.getString("title", name));
                sec.setHidden(cfg.getBoolean("hidden", false));
                sec.setSubSection(cfg.getBoolean("sub-section", false));
                sec.setDisplayItem(cfg.getBoolean("display-item", false));
                sec.setHideIfNoPerms(cfg.getBoolean("hide-if-no-perms", true));
                sec.setEconomy(cfg.getString("economy", "VAULT"));
                sections.put(name, sec);
            }
        }
    }

    private void loadShops(File dir) {
        if (dir == null || !dir.isDirectory()) return;
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                loadShops(f);
            } else if (f.getName().endsWith(".yml")) {
                FileConfiguration cfg = YamlConfiguration.loadConfiguration(f);
                String sectionName = f.getName().replace(".yml", "");
                ShopSection sec = sections.get(sectionName);
                if (sec == null) {
                    sec = new ShopSection(sectionName);
                    sections.put(sectionName, sec);
                }
                if (cfg.contains("pages")) {
                    ConfigurationSection pages = cfg.getConfigurationSection("pages");
                    for (String page : pages.getKeys(false)) {
                        ConfigurationSection pageSec = pages.getConfigurationSection(page);
                        if (pageSec == null) continue;
                        ConfigurationSection itemsSec = pageSec.getConfigurationSection("items");
                        if (itemsSec == null) continue;
                        for (String idx : itemsSec.getKeys(false)) {
                            ConfigurationSection itemSec = itemsSec.getConfigurationSection(idx);
                            if (itemSec == null) continue;
                            ShopItem item = new ShopItem(sectionName, idx);
                            String mat = itemSec.getString("material");
                            if (mat != null) {
                                try {
                                    item.setMaterial(org.bukkit.Material.valueOf(mat.toUpperCase()));
                                } catch (Exception ignored) {}
                            }
                            item.setDisplayName(itemSec.getString("name", itemSec.getString("displayname")));
                            if (itemSec.contains("lore")) item.setLore(itemSec.getStringList("lore"));
                            if (itemSec.contains("buy")) item.setBuy(itemSec.getDouble("buy"));
                            if (itemSec.contains("sell")) item.setSell(itemSec.getDouble("sell"));
                            item.setStackSize(itemSec.getInt("stack-size", 1));
                            item.setMinBuy(itemSec.getInt("min-buy", 0));
                            item.setMaxBuy(itemSec.getInt("max-buy", 0));
                            item.setMinSell(itemSec.getInt("min-sell", 0));
                            item.setMaxSell(itemSec.getInt("max-sell", 0));
                            item.setCloseMenu(itemSec.getBoolean("close-menu", false));
                            item.setHidden(itemSec.getBoolean("hidden", false));
                            item.setSubSection(itemSec.getString("section"));
                            if (item.getMaterial() != null) {
                                item.setItemStack(new org.bukkit.inventory.ItemStack(item.getMaterial(), item.getStackSize()));
                            }
                            sec.addItem(item);
                            items.computeIfAbsent(sectionName, k -> new ArrayList<>()).add(item);
                        }
                    }
                }
            }
        }
    }

    public ShopSection getSection(String name) {
        return sections.get(name);
    }

    public Set<String> getSections() {
        return sections.keySet();
    }

    public Map<String, ShopSection> getAllSections() {
        return sections;
    }

    public List<ShopItem> getItems(String section) {
        return items.getOrDefault(section, new ArrayList<>());
    }
}