

package com.siliconstudios.dynamicshopguiplus.methodes;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.DefaultVersionHandler;
import com.siliconstudios.dynamicshopguiplus.VersionHandler;
import com.siliconstudios.dynamicshopguiplus.api.events.ShopItemsLoadEvent;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.files.Translatable;
import com.siliconstudios.dynamicshopguiplus.methodes.CalculateAmount;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.objects.LoreFormatter;
import com.siliconstudios.dynamicshopguiplus.objects.SectionItem;
import com.siliconstudios.dynamicshopguiplus.objects.ShopItem;
import com.siliconstudios.dynamicshopguiplus.objects.ShopSection;
import com.siliconstudios.dynamicshopguiplus.objects.TransactionMenus;
import com.siliconstudios.dynamicshopguiplus.providers.StandProvider;
import com.siliconstudios.dynamicshopguiplus.util.BackButton;
import com.siliconstudios.dynamicshopguiplus.util.LocalizedItemStack;
import com.siliconstudios.dynamicshopguiplus.util.NBTMaps;
import com.siliconstudios.dynamicshopguiplus.util.ServerInfo;
import com.siliconstudios.dynamicshopguiplus.util.SortOrder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;

public class StartupReload {
    private DynamicShopGUIPlus plugin;

    public StartupReload(DynamicShopGUIPlus plugin) {
        this.plugin = plugin;
    }

    public void loadInventoryTitles() {
        this.plugin.getSectionTitles().clear();
        this.plugin.getBedrockSectionTitles().clear();
        HashMap<String, Translatable> newSectionTitles = new HashMap<String, Translatable>();
        HashMap<String, Translatable> newBedrockSectionTitles = new HashMap<String, Translatable>();
        for (String section : this.plugin.getShopSections()) {
            String bedrockTitle;
            String title = ConfigManager.getSection(section).getString("title");
            if (title == null || title.isEmpty()) {
                title = ConfigManager.getSection(section).getString("item.name");
            }
            if (title != null && !title.isEmpty()) {
                newSectionTitles.put(section, Lang.fromConfig(this.plugin.getInvTitle(title)));
            } else {
                newSectionTitles.put(section, Lang.DISPLAYNAME_NULL.get());
            }
            if ((bedrockTitle = ConfigManager.getSection(section).getString("bedrock-title")) == null || bedrockTitle.isEmpty()) continue;
            newBedrockSectionTitles.put(section, Lang.fromConfig(this.plugin.getInvTitle(bedrockTitle)));
        }
        this.plugin.setSectionTitles(newSectionTitles);
        this.plugin.setBedrockSectionTitles(newBedrockSectionTitles);
    }

    public void checkIfLanguageFilesExist() {
        String[] supported;
        for (String fileName : supported = new String[]{"lang-cs.yml", "lang-de.yml", "lang-en.yml", "lang-es.yml", "lang-fr.yml", "lang-hu.yml", "lang-it.yml", "lang-ja.yml", "lang-ko.yml", "lang-nl.yml", "lang-pl.yml", "lang-pt.yml", "lang-ru.yml", "lang-sk.yml", "lang-sv.yml", "lang-tr.yml", "lang-uk.yml", "lang-vi.yml", "lang-zh_cn.yml", "lang-zh_hk.yml", "lang-zh_tw.yml"}) {
            if (new File(this.plugin.getDataFolder() + File.separator + "LanguageFiles", fileName).exists()) continue;
            this.plugin.saveResource("LanguageFiles" + File.separator + fileName, false);
        }
    }

    public void loadItems() {
        this.plugin.allowEnchanted = ConfigManager.getConfig().getBoolean("enchanted-variants", true);
        if (this.usesEnchantmentVariations()) {
            this.plugin.getEnchantCostManager().reload();
        }
        SendMessage.infoMessage(Lang.LOADING_ITEMS.get());
        if (ConfigManager.getConfig().getBoolean("shop-stands.enable")) {
            if (this.plugin.getStandProvider() == null) {
                this.plugin.setStandProvider(new StandProvider(this.plugin));
                this.plugin.shopStands = true;
            } else {
                this.plugin.getStandProvider().reload();
            }
        }
        this.plugin.createItem.reloadPrices();
        this.plugin.boughtItemsLore = ConfigManager.getConfig().getBoolean("bought-items-lore");
        this.plugin.allowUnsafeEnchants = ConfigManager.getConfig().getBoolean("allow-unsafe-enchants");
        this.plugin.discountsActive = ConfigManager.getConfig().getBoolean("enable-discounts");
        this.plugin.multipliers = ConfigManager.getConfig().getBoolean("enable-sell-multipliers");
        this.plugin.ignoredNBTData = ConfigManager.getConfig().getStringList("sold-items-ignored-NBTtags").stream().map(s -> NBTMaps.getTag(s, null, "sold-items-ignored-NBTtags")).collect(Collectors.toList());
        if (ConfigManager.getConfig().getBoolean("allow-renamed-items")) {
            this.plugin.ignoredNBTData.add(ServerInfo.supportsComponents() ? "custom_name" : "display::Name");
        }
        if (ConfigManager.getConfig().getBoolean("allow-lore-items")) {
            this.plugin.ignoredNBTData.add(ServerInfo.supportsComponents() ? "lore" : "display::Lore");
        }
        this.plugin.dropItemsOnGround = ConfigManager.getConfig().getBoolean("drop-remaining-items-on-ground");
        this.plugin.prioritizeItemLore = ConfigManager.getConfig().getBoolean("prioritize-item-lore");
        this.plugin.MMB = ConfigManager.getConfig().getBoolean("shift-right-click-sell-all", true);
        this.plugin.maxShopSize = ConfigManager.getConfig().getInt("max-shop-pages", 50);
        this.plugin.allowIllegalStacks = ConfigManager.getConfig().getBoolean("allow-illegal-stacks");
        this.plugin.useItemName = ConfigManager.getConfig().getBoolean("use-item-name", true);
        this.plugin.matchMeta = ConfigManager.getConfig().getBoolean("match-item-meta", true);
        this.plugin.navBar.loadNavBars();
        this.plugin.navBar.createCustomPlayerProfile();
        this.plugin.mainMenuSize = CalculateAmount.getInvSlots(ConfigManager.getConfig().getInt("main-menu.gui-rows", 6));
        this.plugin.setMainMenuItemSlots(this.plugin.calculateAmount.getMainMenuItemSlots());
        this.plugin.seasonalLore = ConfigManager.getConfig().getBoolean("seasonal-lore.enabled");
        this.plugin.getTransactionLog().saveUnsaved();
        SendMessage.init();
        BackButton.init();
        this.plugin.bedrock = this.plugin.getServer().getPluginManager().getPlugin("floodgate") != null;
        this.plugin.bedrockMenus = ConfigManager.getConfig().getBoolean("bedrock-forms.enable", false);
        this.plugin.perPlayerLanguages = ConfigManager.getConfig().getBoolean("per-player-languages", true);
        this.plugin.resizeGUI = ConfigManager.getConfig().getBoolean("resize-gui-for-bedrock", true);
        this.plugin.bannedGamemodes = ConfigManager.getConfig().getStringList("banned-gamemodes").stream().filter(s -> {
            try {
                GameMode.valueOf((String)s);
                return true;
            }
            catch (IllegalArgumentException ignored) {
                SendMessage.warnMessage("Failed to load banned gamemode like '" + s + "'");
                return false;
            }
        }).map(GameMode::valueOf).collect(Collectors.toList());
        if (this.plugin.getServer().getPluginManager().isPluginEnabled("Vault")) {
            this.plugin.loadChat();
        }
        if (!ConfigManager.getConfig().getBoolean("use-vault-prefix-suffix", true) || this.plugin.getServer().getPluginManager().isPluginEnabled("Essentials") && this.plugin.getServer().getPluginManager().getPlugin("Essentials").getConfig().getBoolean("change-displayname") && this.plugin.getServer().getPluginManager().getPlugin("Essentials").getConfig().getBoolean("add-prefix-suffix", this.plugin.getServer().getPluginManager().isPluginEnabled("EssentialsChat"))) {
            this.plugin.prefixSuffix = false;
        }
        this.plugin.sellShulkers = ConfigManager.getConfig().getBoolean("sell-shulker-boxes", true);
        this.plugin.setLoreFormatter(new LoreFormatter(this.plugin));
        HashMap<String, SectionItem> sectionItems = new HashMap<String, SectionItem>();
        Map<Integer, String> slots = this.plugin.getMainMenuItemSlots();
        for (Map.Entry<Integer, String> e : slots.entrySet()) {
            SectionItem item;
            if (!sectionItems.containsKey(e.getValue())) {
                item = new SectionItem(this.plugin, (String)e.getValue());
                sectionItems.put(e.getValue(), item);
            } else {
                item = (SectionItem)sectionItems.get(e.getValue());
            }
            item.addSlot((Integer)e.getKey());
        }
        this.plugin.setSectionItems(sectionItems);
        HashSet<String> invalid = new HashSet<String>();
        for (String section : this.plugin.getShopSections()) {
            try {
                this.plugin.addSection(section, new ShopSection(this.plugin, section));
            }
            catch (Exception e) {
                invalid.add(section);
                SendMessage.errorMessage("Failed to create section " + section + ": ");
                e.printStackTrace();
            }
        }
        this.plugin.getShopSections().removeAll(invalid);
        TransactionMenus.load(this.plugin);
        if (ConfigManager.getConfig().getBoolean("main-menu.fill-item.enable", true)) {
            LocalizedItemStack fillitem = this.plugin.createItem.createFillItem(null, ConfigManager.getConfig().getConfigurationSection("main-menu.fill-item"));
            if (fillitem != null) {
                this.plugin.createItem.fillItem = fillitem.getItem();
            } else {
                SendMessage.warnMessage("Failed to find fill item inside config.yml");
                SendMessage.logDebugMessage(Lang.ITEMS_PATH_IN_CONFIG.get().replace("%location%", "main-menu.fill-item"));
            }
        } else {
            this.plugin.createItem.fillItem = null;
        }
        if (this.plugin.discountsActive) {
            this.setupDiscounts();
        }
        if (this.plugin.multipliers) {
            this.loadSellMultipliers();
        }
        this.plugin.shopItemsByMaterialName = this.getShopItemsByMaterialName();
        this.putShopItemsByOrder();
        Bukkit.getPluginManager().callEvent((Event)new ShopItemsLoadEvent());
    }

    private void setupDiscounts() {
        HashMap<String, HashMap<String, Double>> sectionDiscounts = new HashMap<String, HashMap<String, Double>>();
        for (String section : ConfigManager.getConfig().getConfigurationSection("discounts").getKeys(false)) {
            if (section.equals("*") || section.equals("all")) {
                for (String s : this.plugin.getShopSections()) {
                    HashMap<String, Double> discounts = sectionDiscounts.getOrDefault(s, new HashMap());
                    for (String group : ConfigManager.getConfig().getConfigurationSection("discounts." + section).getKeys(false)) {
                        double discount = ConfigManager.getConfig().getDouble("discounts." + section + "." + group);
                        if (discount == 0.0) continue;
                        discounts.put(group, discount);
                    }
                    if (discounts.isEmpty()) continue;
                    sectionDiscounts.put(s, discounts);
                }
                continue;
            }
            if (this.plugin.getShopSections().contains(section)) {
                HashMap<String, Double> discounts = sectionDiscounts.getOrDefault(section, new HashMap());
                for (String group : ConfigManager.getConfig().getConfigurationSection("discounts." + section).getKeys(false)) {
                    double discount = ConfigManager.getConfig().getDouble("discounts." + section + "." + group);
                    if (discount == 0.0) continue;
                    discounts.put(group, discount);
                }
                if (discounts.isEmpty()) continue;
                sectionDiscounts.put(section, discounts);
                continue;
            }
            SendMessage.warnMessage(Lang.CANNOT_FIND_DISCOUNTED_SECTION.get().replace("%section%", section));
        }
        if (!sectionDiscounts.isEmpty()) {
            this.plugin.setDiscounts(sectionDiscounts);
        }
    }

    private void loadSellMultipliers() {
        HashMap<String, HashMap<String, Double>> sectionMultipliers = new HashMap<String, HashMap<String, Double>>();
        for (String section : ConfigManager.getConfig().getConfigurationSection("sell-multipliers").getKeys(false)) {
            if (section.equals("*") || section.equals("all")) {
                for (String s : this.plugin.getShopSections()) {
                    HashMap<String, Double> multipliers = sectionMultipliers.getOrDefault(s, new HashMap());
                    for (String group : ConfigManager.getConfig().getConfigurationSection("sell-multipliers." + section).getKeys(false)) {
                        double multiplier = ConfigManager.getConfig().getDouble("sell-multipliers." + section + "." + group);
                        if (multiplier == 0.0) continue;
                        multipliers.put(group, multiplier);
                    }
                    if (multipliers.isEmpty()) continue;
                    sectionMultipliers.put(s, multipliers);
                }
                continue;
            }
            if (this.plugin.getShopSections().contains(section)) {
                HashMap<String, Double> multipliers = sectionMultipliers.getOrDefault(section, new HashMap());
                for (String group : ConfigManager.getConfig().getConfigurationSection("sell-multipliers." + section).getKeys(false)) {
                    double multiplier = ConfigManager.getConfig().getDouble("sell-multipliers." + section + "." + group);
                    if (multiplier == 0.0) continue;
                    multipliers.put(group, multiplier);
                }
                if (multipliers.isEmpty()) continue;
                sectionMultipliers.put(section, multipliers);
                continue;
            }
            SendMessage.warnMessage("Cannot add sell multipliers for section '%section%', either the shop section is disabled or not found.".replace("%section%", section));
        }
        if (!sectionMultipliers.isEmpty()) {
            this.plugin.setSellMultipliers(sectionMultipliers);
        }
    }

    private boolean usesEnchantmentVariations() {
        if (this.plugin.allowEnchanted) {
            return true;
        }
        for (String section : this.plugin.getShopSections()) {
            if (ConfigManager.getSection(section).getBoolean("enchanted-variants")) {
                return true;
            }
            for (String itemLoc : this.plugin.getConfigManager().getItemsRaw(section)) {
                if (!ConfigManager.getShop(section).getBoolean("pages." + itemLoc + ".enchanted-variants")) continue;
                return true;
            }
        }
        return false;
    }

    private Map<String, List<ShopItem>> getShopItemsByMaterialName() {
        HashMap<String, List<ShopItem>> mats = new HashMap<String, List<ShopItem>>();
        for (String s : this.plugin.getShopSections()) {
            com.siliconstudios.dynamicshopguiplus.objects.shops.ShopSection section = this.plugin.getSection(s);
            for (ShopItem shopItem : section.getShopItems()) {
                if (shopItem.hasItemError() || shopItem.isDisplayItem()) continue;
                String mat = shopItem.getShopItem().getType().name();
                List<ShopItem> items = mats.containsKey(mat) ? (List)mats.get(mat) : new ArrayList();
                items.add(shopItem);
                mats.put(mat, items);
            }
        }
        return mats;
    }

    public void putShopItemsByOrder() {
        SortOrder order = SortOrder.fromConfig();
        if (order == SortOrder.SHOP_LOAD_ORDER) {
            return;
        }
        for (String mat : this.plugin.shopItemsByMaterialName.keySet()) {
            List<ShopItem> items = this.plugin.shopItemsByMaterialName.get(mat);
            if (items.size() < 2) continue;
            switch (order) {
                case DESCENDING_SELL_PRICE: {
                    items.sort((i1, i2) -> Double.compare(i2.getSellPrice(), i1.getSellPrice()));
                    break;
                }
                case ASCENDING_SELL_PRICE: {
                    items.sort((i1, i2) -> Double.compare(i1.getSellPrice(), i2.getSellPrice()));
                }
            }
        }
    }

    public void updateAvailable() {
        if (this.plugin.updateChecker != null) {
            this.plugin.updateChecker.checkForUpdates();
        }
    }

    public void setupPluginVersion() {
        ServerInfo.Version version = ServerInfo.getVersion();
        if (version == ServerInfo.Version.UNKOWN_VERSION) {
            SendMessage.errorMessage("===================================================");
            SendMessage.errorMessage(" ");
            SendMessage.errorMessage(Lang.COULD_NOT_FIND_VALID_VERSION.get());
            SendMessage.errorMessage("Version: " + Bukkit.getBukkitVersion());
            SendMessage.errorMessage(" ");
            SendMessage.errorMessage("===================================================");
            Bukkit.getPluginManager().disablePlugin((Plugin)DynamicShopGUIPlus.getInstance());
            return;
        }
        if (version.olderOrEqualAs(ServerInfo.Version.v1_16_R2)) {
            SendMessage.errorMessage("===================================================");
            SendMessage.errorMessage(" ");
            SendMessage.errorMessage("This server version is not supported anymore on this plugin version, please downgrade to v6.16.4 or lower to keep using the plugin on this server version");
            SendMessage.errorMessage("Version: " + Bukkit.getBukkitVersion());
            SendMessage.errorMessage(" ");
            SendMessage.errorMessage("===================================================");
            Bukkit.getPluginManager().disablePlugin((Plugin)DynamicShopGUIPlus.getInstance());
            return;
        }
        try {
            this.plugin.versionHandler = new DefaultVersionHandler();
            SendMessage.infoMessage(Lang.MINECRAFT_VERSION_USING.get().replace("%version%", Bukkit.getBukkitVersion().split("-")[0].split("\\.build")[0]));
        } catch (Exception e) {
            SendMessage.infoMessage(Lang.MINECRAFT_VERSION_USING.get().replace("%version%", Bukkit.getBukkitVersion().split("-")[0].split("\\.build")[0]));
        }
    }

    public void checkDebugMode() {
        if (ConfigManager.getConfig().getBoolean("debug", true)) {
            SendMessage.infoMessage(Lang.DEBUG_MODE_ENABLED.get());
        }
    }
}

