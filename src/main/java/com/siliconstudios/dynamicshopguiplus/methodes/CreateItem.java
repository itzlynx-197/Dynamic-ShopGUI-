

package com.siliconstudios.dynamicshopguiplus.methodes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.files.Translatable;
import com.siliconstudios.dynamicshopguiplus.files.lang.TranslatableRaw;
import com.siliconstudios.dynamicshopguiplus.methodes.CalculateAmount;
import com.siliconstudios.dynamicshopguiplus.methodes.CreateItemMethodes;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.objects.ActionItem;
import com.siliconstudios.dynamicshopguiplus.objects.ShopItem;
import com.siliconstudios.dynamicshopguiplus.objects.TransactionItem;
import com.siliconstudios.dynamicshopguiplus.objects.TransactionScreen;
import com.siliconstudios.dynamicshopguiplus.providers.SpawnerManager;
import com.siliconstudios.dynamicshopguiplus.util.ChatUtil;
import com.siliconstudios.dynamicshopguiplus.util.FireworkUtil;
import com.siliconstudios.dynamicshopguiplus.util.ItemBuilder;
import com.siliconstudios.dynamicshopguiplus.util.LocalizedItemStack;
import com.siliconstudios.dynamicshopguiplus.util.PermissionsCache;
import com.siliconstudios.dynamicshopguiplus.util.PotionTypes;
import com.siliconstudios.dynamicshopguiplus.util.ServerInfo;
import com.siliconstudios.dynamicshopguiplus.util.Transaction;
import com.siliconstudios.dynamicshopguiplus.util.XMaterial;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.ItemLoadException;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.KnowledgeBookMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MusicInstrumentMeta;
import org.bukkit.inventory.meta.OminousBottleMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.persistence.PersistentDataType;

public class CreateItem {
    private final DynamicShopGUIPlus plugin;
    private final CreateItemMethodes itemMethodes;
    private Map<String, Double> buyPrices = new HashMap<String, Double>();
    private Map<String, Double> sellPrices = new HashMap<String, Double>();
    public ItemStack fillItem;

    public CreateItem(DynamicShopGUIPlus plugin) {
        this.plugin = plugin;
        this.itemMethodes = new CreateItemMethodes();
    }

    public Double getBaseBuyPrice(String itemPath) {
        return this.buyPrices.get(itemPath);
    }

    public Double getBaseSellPrice(String itemPath) {
        return this.sellPrices.get(itemPath);
    }

    public void addNewBuyPrice(String itemPath, Double buyPrice) {
        this.buyPrices.put(itemPath, buyPrice);
    }

    public void addNewSellPrice(String itemPath, Double sellPrice) {
        this.sellPrices.put(itemPath, sellPrice);
    }

    public void reloadPrices() {
        if (!this.buyPrices.isEmpty()) {
            this.buyPrices.clear();
        }
        if (!this.sellPrices.isEmpty()) {
            this.sellPrices.clear();
        }
    }

    public ItemStack loadShopSectionItem(ShopItem shopItem) throws ItemLoadException {
        ConfigurationSection config = ConfigManager.getShop(shopItem.section).contains("pages." + shopItem.itemLoc + ".display-item") ? ConfigManager.getShop(shopItem.section).getConfigurationSection("pages." + shopItem.itemLoc + ".display-item") : ConfigManager.getShop(shopItem.section).getConfigurationSection("pages." + shopItem.itemLoc);
        if (config == null || config.getKeys(false).isEmpty()) {
            throw new ItemLoadException(Lang.ITEM_NULL.get());
        }
        ItemStack item = CreateItemMethodes.createItemMaterialFromString(config.getString("material"));
        if (item.getType() == Material.AIR) {
            return item;
        }
        if (config.contains("buy")) {
            this.addNewBuyPrice(shopItem.getItemPath(), config.getDouble("buy"));
        }
        if (config.contains("sell")) {
            this.addNewSellPrice(shopItem.getItemPath(), config.getDouble("sell"));
        }
        Translatable displayname = this.itemMethodes.getDisplayname(config);
        List<Translatable> lore = this.getDisplayLore(config);
        if (displayname != null || !lore.isEmpty()) {
            ItemMeta meta = item.getItemMeta();
            if (displayname != null) {
                this.plugin.getMetaUtils().setDisplayName(meta, displayname);
            }
            if (!lore.isEmpty()) {
                this.plugin.getMetaUtils().setLore(meta, lore);
            }
            item.setItemMeta(meta);
        }
        item = this.itemMethodes.setOption(shopItem, item, config, shopItem.section, true);
        try {
            if (DynamicShopGUIPlus.getInstance().allowIllegalStacks && ServerInfo.supportsComponents()) {
                item = (ItemStack)ServerInfo.invokeModernVersionMethod("setComponent", new Class[]{ItemStack.class, String.class, Object.class}, new Object[]{item, "max_stack_size", 64});
            }
        }
        catch (Exception exception) {
            
        }
        return this.plugin.versionHandler.setPathToItem(item, shopItem.getItemPath());
    }

    public ItemStack loadActionItem(ActionItem actionItem) throws ItemLoadException {
        ConfigurationSection config = ConfigManager.getShop(actionItem.section).getConfigurationSection("pages." + actionItem.itemLoc);
        ItemStack item = CreateItemMethodes.createItemMaterialFromString(config.getString("material"));
        if (item.getType() == Material.AIR) {
            return null;
        }
        Translatable displayname = this.itemMethodes.getDisplayname(config);
        List<Translatable> lore = this.getDisplayLore(config);
        if (displayname != null || !lore.isEmpty()) {
            ItemMeta meta = item.getItemMeta();
            if (displayname != null) {
                this.plugin.getMetaUtils().setDisplayName(meta, displayname);
            }
            if (!lore.isEmpty()) {
                this.plugin.getMetaUtils().setLore(meta, lore);
            }
            item.setItemMeta(meta);
        }
        item = this.itemMethodes.setOption(actionItem, item, config, actionItem.section, true);
        try {
            if (DynamicShopGUIPlus.getInstance().allowIllegalStacks && ServerInfo.supportsComponents()) {
                item = (ItemStack)ServerInfo.invokeModernVersionMethod("setComponent", new Class[]{ItemStack.class, String.class, Object.class}, new Object[]{item, "max_stack_size", 64});
            }
        }
        catch (Exception exception) {
            
        }
        return this.plugin.versionHandler.setPathToItem(item, actionItem.getItemPath());
    }

    public ItemStack loadSectionItem(String section, String path) throws ItemLoadException {
        ConfigurationSection config = ConfigManager.getSection(section).getConfigurationSection(path);
        if (config == null || config.getKeys(false).isEmpty()) {
            throw new ItemLoadException(Lang.ITEM_NULL.get());
        }
        ItemStack item = CreateItemMethodes.createItemMaterialFromString(config.getString("material"));
        if (item.getType() == Material.AIR) {
            throw new ItemLoadException("Item material cannot be AIR");
        }
        Translatable displayname = this.itemMethodes.getDisplayname(config);
        List<Translatable> lore = this.getDisplayLore(config);
        if (displayname != null || !lore.isEmpty() || config.getBoolean("hideDefaultLore", true)) {
            ItemMeta meta = item.getItemMeta();
            if (displayname != null) {
                this.plugin.getMetaUtils().setDisplayName(meta, displayname);
            }
            if (!lore.isEmpty()) {
                this.plugin.getMetaUtils().setLore(meta, lore);
            }
            if (config.getBoolean("hideDefaultLore", true)) {
                meta.addItemFlags(ItemFlag.values());
            }
            item.setItemMeta(meta);
        }
        item = this.itemMethodes.setSectionItemOption(item, section, config);
        return item;
    }

    public ItemStack loadItemToGive(ShopItem shopItem) throws ItemLoadException {
        ConfigurationSection config = ConfigManager.getShop(shopItem.section).getConfigurationSection("pages." + shopItem.itemLoc);
        ItemStack item = CreateItemMethodes.createItemMaterialFromString(config.getString("material"));
        if (item.getType() == Material.AIR) {
            return item;
        }
        Translatable displayname = this.getName(config);
        List<Translatable> lore = this.getLore(config);
        if (displayname != null || !lore.isEmpty()) {
            ItemMeta meta = item.getItemMeta();
            if (displayname != null) {
                this.plugin.getMetaUtils().setDisplayName(meta, displayname);
            }
            if (!lore.isEmpty() && this.plugin.boughtItemsLore) {
                this.plugin.getMetaUtils().setLore(meta, lore);
            }
            item.setItemMeta(meta);
        }
        item = this.itemMethodes.setOption(shopItem, item, config, shopItem.section, false);
        return item;
    }

    public ItemStack getInvalidShopItem(String section, String locateItem) {
        ItemStack item = new ItemBuilder(XMaterial.BARRIER.parseMaterial()).withDisplayName(Lang.ITEM_ERROR.get()).build();
        item = this.plugin.versionHandler.setPathToItem(item, section + "." + locateItem);
        return item;
    }

    public ItemStack getInvalidShopItem(String section, String locateItem, ItemLoadException e) {
        List<String> lore = CalculateAmount.splitLongString(e.getMessage());
        lore.replaceAll(s -> ChatUtil.formatColors("&c" + s));
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(lore.remove(0));
        if (!lore.isEmpty()) {
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack getInvalidItem(ItemLoadException e) {
        List<String> lore = CalculateAmount.splitLongString(e.getMessage());
        lore.replaceAll(s -> ChatUtil.formatColors("&c" + s));
        ItemBuilder item = new ItemBuilder(Material.BARRIER);
        item.setDisplayName(lore.remove(0));
        if (!lore.isEmpty()) {
            item.setLore(lore);
        }
        return item.build();
    }

    private ItemStack getTransactionItem(String path, Translatable displayname) {
        try {
            ItemStack item = CreateItemMethodes.createItemMaterialFromString(ConfigManager.getConfig().getString(path + ".material"));
            return item.getType() != Material.AIR ? new ItemBuilder(item).setAmount(1).withDisplayName(displayname).build() : item;
        }
        catch (ItemLoadException e) {
            SendMessage.logDebugMessage(e.getMessage());
            SendMessage.errorItemConfig(path);
            ItemStack item = XMaterial.matchXMaterial(ConfigManager.getConfig().getDef().getString(path + ".material")).get().parseItem();
            return new ItemBuilder(item).setAmount(1).withDisplayName(displayname).build();
        }
    }

    public List<ItemStack> getAllowedTools() {
        ArrayList<ItemStack> allowedTools = new ArrayList<ItemStack>();
        ConfigurationSection section = ConfigManager.getConfig().getConfigurationSection("spawner-break-tools");
        if (section == null) {
            return new ArrayList<ItemStack>();
        }
        for (String key : section.getKeys(false)) {
            try {
                ItemStack item = CreateItemMethodes.createItemMaterialFromString(section.getString(key + ".material"));
                allowedTools.add(this.itemMethodes.setEnchantments(item, null, section.getConfigurationSection(key)));
            }
            catch (ItemLoadException e) {
                SendMessage.logDebugMessage(e.getMessage());
                SendMessage.errorItemConfig("spawner-break-tools." + key);
            }
        }
        return allowedTools;
    }

    public boolean isShopItem(ItemStack itemToMatch, ItemStack shopItem) {
        if (!itemToMatch.getType().equals((Object)shopItem.getType())) {
            return false;
        }
        if (itemToMatch.getType().equals((Object)XMaterial.SPAWNER.parseMaterial())) {
            return this.plugin.getSpawnerManager().getProvider().isShopSpawner(itemToMatch, shopItem);
        }
        return this.plugin.isSimilar(itemToMatch, shopItem);
    }

    public double getShopItemSellPrice(ItemStack itemToSell, String itemPath) {
        double price = this.getBaseSellPrice(itemPath);
        return itemToSell.getType().equals((Object)XMaterial.SPAWNER.parseMaterial()) ? this.plugin.getSpawnerManager().getProvider().getSpawnerSellPrice(itemToSell, price) : price * (double)itemToSell.getAmount();
    }

    @Deprecated
    public ShopItem matchShopItem(Player player, ItemStack itemToLookFor) {
        if (this.plugin.shopItemsByMaterialName.containsKey(itemToLookFor.getType().name())) {
            for (ShopItem shopItem : this.plugin.shopItemsByMaterialName.get(itemToLookFor.getType().name())) {
                if (!PermissionsCache.hasPermission(player, "DynamicShopGUIPlus.shop." + shopItem.section) || !shopItem.match(itemToLookFor) || !shopItem.meetsRequirements(player, true)) continue;
                return shopItem;
            }
        }
        return null;
    }

    public ShopItem matchShopItem(Player player, ItemStack itemToLookFor, Transaction.Mode method) {
        if (this.plugin.shopItemsByMaterialName.containsKey(itemToLookFor.getType().name())) {
            for (ShopItem shopItem : this.plugin.shopItemsByMaterialName.get(itemToLookFor.getType().name())) {
                if (!PermissionsCache.hasPermission(player, "DynamicShopGUIPlus.shop." + shopItem.section) || (method != Transaction.Mode.BUY ? shopItem.getSellPrice() < 0.0 : shopItem.getBuyPrice() < 0.0)) continue;
                if (shopItem.isDisplayItem() || !shopItem.match(itemToLookFor) || !shopItem.meetsRequirements(player, true)) continue;
                return shopItem;
            }
        }
        return null;
    }

    public ShopItem matchShopItem(ItemStack itemToLookFor) {
        if (this.plugin.shopItemsByMaterialName.containsKey(itemToLookFor.getType().name())) {
            for (ShopItem shopItem : this.plugin.shopItemsByMaterialName.get(itemToLookFor.getType().name())) {
                if (shopItem.isDisplayItem() || !shopItem.match(itemToLookFor)) continue;
                return shopItem;
            }
        }
        return null;
    }

    public ShopItem matchShopItem(ItemStack itemToLookFor, Player player, String root) {
        if (this.plugin.shopItemsByMaterialName.containsKey(itemToLookFor.getType().name())) {
            for (ShopItem shopItem : this.plugin.shopItemsByMaterialName.get(itemToLookFor.getType().name())) {
                if (!PermissionsCache.hasPermission(player, "DynamicShopGUIPlus." + root + "." + shopItem.section) || shopItem.getSellPrice() < 0.0 || shopItem.isDisplayItem() || !shopItem.match(itemToLookFor) || !shopItem.meetsRequirements(player, true)) continue;
                return shopItem;
            }
        }
        return null;
    }

    public ItemStack createSpawner(EntityType type) {
        ItemBuilder builder = new ItemBuilder(XMaterial.SPAWNER.parseItem());
        builder.setDisplayName(SpawnerManager.getDefaultName(type));
        return builder.build();
    }

    public ItemStack createItem(ItemStack item, String Displayname, String ... lore) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Displayname);
        if (lore != null) {
            ArrayList<String> customlore = new ArrayList<String>();
            customlore.addAll(Arrays.asList(lore));
            meta.setLore(customlore);
        }
        item.setItemMeta(meta);
        return item;
    }

    public LocalizedItemStack createFillItem(String section, ConfigurationSection config) {
        if (config == null) {
            return null;
        }
        if (!this.hasTranslations(config)) {
            ItemStack item = this.createFillItem(section, config, Lang.getDefaultLocale());
            return item == null ? null : new LocalizedItemStack(item);
        }
        HashMap<Locale, ItemStack> variants = new HashMap<Locale, ItemStack>();
        for (Locale locale : Lang.getLocales()) {
            ItemStack item = this.createFillItem(section, config, locale);
            if (item == null) {
                return null;
            }
            if (item.getType() == Material.AIR) {
                return new LocalizedItemStack(item);
            }
            variants.put(locale, item);
        }
        return new LocalizedItemStack(variants);
    }

    private boolean hasTranslations(ConfigurationSection config) {
        if (config.contains("name") && String.valueOf(config.getString("name")).contains("%translations-")) {
            return true;
        }
        return config.contains("lore") && config.getStringList("lore").stream().anyMatch(s -> s.contains("%translations-"));
    }

    private ItemStack createFillItem(String section, ConfigurationSection config, Locale locale) {
        try {
            ItemStack item = CreateItemMethodes.createItemMaterialFromString(config.getString("material"));
            if (item.getType() == Material.AIR) {
                return item;
            }
            ItemBuilder builder = new ItemBuilder(item);
            if (config.contains("name")) {
                builder.setDisplayName(this.getTransactionItemName(config.getString("name"), locale));
            }
            if (config.contains("lore")) {
                builder.setCLore(this.getTransactionItemLore(config.getStringList("lore"), locale));
            }
            if (config.contains("enchantment-glint")) {
                builder.withEnchantGlint();
            }
            if (config.contains("skullowner") && !config.getString("skullowner").equals("%player_name%")) {
                if (XMaterial.matchXMaterial(item).equals((Object)XMaterial.PLAYER_HEAD)) {
                    builder.withSkullOwner(config.getString("skullowner"));
                } else {
                    throw new ItemLoadException(Lang.MATERIAL_NEEDS_TO_BE_SKULL.get());
                }
            }
            item = builder.build();
            if (config.contains("potiontypes")) {
                this.itemMethodes.setPotionTypes(item, config.getStringList("potiontypes"), null, config.getCurrentPath(), XMaterial.matchXMaterial(item) == XMaterial.SPLASH_POTION);
            }
            if (config.contains("armor-trim")) {
                if (DynamicShopGUIPlus.getInstance().version >= 120) {
                    new CreateItemMethodes.ArmorTrim(config.getString("armor-trim.type"), config.getString("armor-trim.pattern")).build(item);
                } else {
                    throw new ItemLoadException("ArmorTrim's are only supported in version 1.20 and above");
                }
            }
            if (config.contains("armorcolor")) {
                if (item.getItemMeta() instanceof LeatherArmorMeta) {
                    LeatherArmorMeta meta = (LeatherArmorMeta)item.getItemMeta();
                    meta.setColor(this.itemMethodes.getRGBColor(config.getString("armorcolor"), config.getCurrentPath() + ".armorcolor"));
                    item.setItemMeta((ItemMeta)meta);
                } else {
                    throw new ItemLoadException(Lang.MATERIAL_NEEDS_TO_BE_LEATHER_ARMOR.get());
                }
            }
            if (config.contains("stack-size")) {
                int stackSize = config.getInt("stack-size");
                if (!DynamicShopGUIPlus.getInstance().allowIllegalStacks && stackSize > item.getMaxStackSize()) {
                    throw new ItemLoadException("Cannot set the stack size of item %material% to %stack-size% because it would exceed the vanilla max stack size.".replace("%material%", item.getType().toString()).replace("%stack-size%", String.valueOf(stackSize)));
                }
                item.setAmount(stackSize);
            }
            return item;
        }
        catch (ItemLoadException e) {
            SendMessage.warnMessage(e.getMessage());
            if (section == null) {
                SendMessage.errorItemConfig(config.getCurrentPath());
            } else {
                SendMessage.errorSections(section, config.getCurrentPath());
            }
            return null;
        }
    }

    public HashMap<String, Object> getShopItem(ItemStack item, double buyPrice, double sellPrice) {
        HashMap<String, Object> keys = new HashMap<String, Object>();
        if (this.plugin.version > 112) {
            keys.put("material", item.getType().name());
        } else if (XMaterial.matchXMaterial(item.getType().name() + ":" + item.getDurability()).isPresent()) {
            keys.put("material", item.getType().name() + ":" + item.getDurability());
        } else {
            SendMessage.warnMessage(Lang.ITEM_MATERIAL_NULL.get().replace("%material%", item.getType().name() + ":  " + item.getDurability()));
        }
        keys.put("buy", buyPrice);
        keys.put("sell", sellPrice);
        if (item.getAmount() > 1) {
            keys.put("stack-size", item.getAmount());
        }
        if (item.hasItemMeta()) {
            OminousBottleMeta bm;
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName()) {
                keys.put("name", meta.getDisplayName());
            }
            if (this.plugin.version >= 112 && meta instanceof KnowledgeBookMeta) {
                KnowledgeBookMeta bookMeta = (KnowledgeBookMeta)item.getItemMeta();
                ArrayList<String> recipes = new ArrayList<String>();
                for (int i = 0; i < bookMeta.getRecipes().size(); ++i) {
                    recipes.add(((NamespacedKey)bookMeta.getRecipes().get(i)).getKey());
                }
                keys.put("recipes", recipes);
            } else if (meta instanceof SkullMeta) {
                SkullMeta skullMeta = (SkullMeta)item.getItemMeta();
                keys.put("skullowner", skullMeta.getOwner());
            } else if (meta instanceof LeatherArmorMeta) {
                LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta)item.getItemMeta();
                org.bukkit.Color color = leatherArmorMeta.getColor();
                keys.put("armorcolor", "#" + Integer.toHexString(new Color(color.getRed(), color.getGreen(), color.getBlue()).getRGB()).substring(2).toUpperCase());
            } else if (meta instanceof FireworkMeta) {
                keys.putAll(FireworkUtil.serialize((FireworkMeta)meta));
            } else if (ServerInfo.getVersion().newerOrEqualAs(ServerInfo.Version.v1_19_R3) && meta instanceof MusicInstrumentMeta) {
                MusicInstrumentMeta mm = (MusicInstrumentMeta)meta;
                keys.put("instrument", mm.getInstrument().getKey().getKey().replace("_goat_horn", ""));
            } else if (ServerInfo.getVersion().newerOrEqualAs(ServerInfo.Version.v1_20_R1) && meta instanceof ArmorMeta) {
                ArmorMeta am = (ArmorMeta)meta;
                if (am.hasTrim()) {
                    ArmorTrim trim = am.getTrim();
                    keys.put("armor-trim.type", trim.getMaterial().getKey().getKey());
                    keys.put("armor-trim.pattern", trim.getPattern().getKey().getKey());
                }
            } else if (ServerInfo.getVersion().newerOrEqualAs(ServerInfo.Version.v1_20_R4) && meta instanceof OminousBottleMeta && (bm = (OminousBottleMeta)meta).hasAmplifier()) {
                keys.put("ominous-strength", bm.getAmplifier());
            }
            if (!meta.getEnchants().isEmpty() || item.getType() == Material.ENCHANTED_BOOK) {
                ArrayList enchants = new ArrayList();
                (item.getType() == Material.ENCHANTED_BOOK ? ((EnchantmentStorageMeta)item.getItemMeta()).getStoredEnchants() : item.getEnchantments()).forEach((enchant, strength) -> {
                    if (strength > enchant.getMaxLevel() && !this.plugin.allowUnsafeEnchants) {
                        strength = enchant.getMaxLevel();
                    }
                    enchants.add((ServerInfo.supportsComponents() ? enchant.getKey().getKey().toUpperCase(Locale.ROOT) : enchant.getName()) + ":" + strength);
                });
                keys.put("enchantments", enchants);
            }
            if (PotionTypes.canHaveEffects(item.getType())) {
                ArrayList<String> potionTypes = new ArrayList<String>();
                for (PotionTypes effect : PotionTypes.getPotionTypes(item)) {
                    potionTypes.add(effect.name());
                }
                keys.put("potiontypes", potionTypes);
            }
            if (meta.hasLore()) {
                ArrayList<String> lore = new ArrayList<String>();
                for (String line : meta.getLore()) {
                    lore.add(line.replaceAll(String.valueOf('\u00a7'), "&"));
                }
                keys.put("lore", lore);
            }
        }
        if (this.plugin.version >= 116 && item.hasItemMeta()) {
            try {
                if ((Integer)item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Bukkit.getPluginManager().getPlugin("AutoSellChests"), "autosell"), PersistentDataType.INTEGER) == 1) {
                    keys.put("autosell", true);
                }
            }
            catch (Exception exception) {
                
            }
        }
        if (item.getType().equals((Object)XMaterial.SPAWNER.parseMaterial())) {
            keys.put("spawnertype", this.plugin.getSpawnerManager().getProvider().getSpawnedType(item));
        }
        return keys;
    }

    public TransactionScreen getTransactionScreen(Transaction.Type menu) {
        ArrayList<TransactionItem> items = new ArrayList<TransactionItem>();
        String section = menu.name().toLowerCase().replace("_", "-");
        int size = CalculateAmount.getInvSlots(ConfigManager.getConfig().getInt(section + ".menu-size", 5));
        if (ConfigManager.getConfig().contains(section + ".fill-item")) {
            LocalizedItemStack item = this.createFillItem(null, ConfigManager.getConfig().getConfigurationSection(section + ".fill-item"));
            items.add(new TransactionItem(TransactionItemAction.NONE, TransactionItemType.FILLITEM, item.getItem(), new ArrayList<Integer>(), false, ConfigManager.getConfig().getString(section + ".fill-item.bedrock-texture")));
        }
        for (String s : ConfigManager.getConfig().getConfigurationSection(section + ".items").getKeys(false)) {
            TransactionItemAction action = new TransactionItemAction(ConfigManager.getConfig().getString(section + ".items." + s + ".action", "NONE"));
            TransactionItemType type = TransactionItemType.getFromString(action, ConfigManager.getConfig().getString(section + ".items." + s + ".type", "NORMAL"));
            if (type != TransactionItemType.NORMAL && type != TransactionItemType.DECREASE_AMOUNT && type != TransactionItemType.INCREASE_AMOUNT && type != TransactionItemType.SELECT_AMOUNT && items.stream().filter(i -> i.getType() == type).findFirst().orElse(null) != null) {
                SendMessage.warnMessage("The " + section + " cannot contain multiple items from type " + type.name() + ", instead use the ability to copy this item into multiple slots by using 'slot: 9,13,14,15', skipping...");
                SendMessage.errorItemConfig(section + ".items." + s);
                continue;
            }
            boolean dynamicItem = ConfigManager.getConfig().getString(section + ".items." + s + ".material", "").equalsIgnoreCase("%SELECTED_ITEM%") || type == TransactionItemType.SELECTED_ITEM;
            ItemStack item = this.getTransactionItem(type, section + ".items." + s, dynamicItem);
            List<Integer> slots = this.getSlots(ConfigManager.getConfig().getString(section + ".items." + s + ".slot", ConfigManager.getConfig().getDef().getString(section + ".items." + s + ".slot", null)));
            if (slots == null) {
                SendMessage.warnMessage("Invalid slot for item, skipping...");
                SendMessage.errorItemConfig(section + ".items." + s);
                continue;
            }
            for (int i2 = slots.size() - 1; i2 > 0; --i2) {
                int slot = slots.get(i2);
                if (slot < (this.plugin.navBar.isEnableTransactionNav() ? size - 9 : size) && slot >= 0) continue;
                SendMessage.warnMessage("Item slot " + slot + " is out of bounds for menu size of " + size + ", skipping ...");
                SendMessage.errorItemConfig(section + ".items." + s);
                slots.remove(i2);
            }
            items.add(new TransactionItem(action, type, item, slots, dynamicItem, ConfigManager.getConfig().getString(section + ".items." + s + ".bedrock-texture")));
        }
        if (!items.stream().anyMatch(i -> i.getAction().type == TransactionItemActionType.INSTA_BUY || i.getAction().type == TransactionItemActionType.INSTA_SELL) && !items.stream().anyMatch(i -> i.getType() == TransactionItemType.CONFIRM)) {
            SendMessage.warnMessage("The transaction screen inside the config.yml at path '" + section + "' needs to contain atleast one item with type CONFIRM, adding default...");
            items.add(13, new TransactionItem(new TransactionItemAction(TransactionItemActionType.CONFIRM_TRANSACTION), TransactionItemType.CONFIRM, this.getTransactionItem(TransactionItemType.CONFIRM, section + ".items.7", false), Collections.singletonList(13), false));
        }
        return new TransactionScreen(items, size);
    }

    private List<Integer> getSlots(String slots) {
        if (slots == null) {
            return null;
        }
        try {
            return new ArrayList<Integer>(Collections.singletonList(Integer.parseInt(slots)));
        }
        catch (NumberFormatException e) {
            ArrayList<Integer> pos = new ArrayList<Integer>();
            slots = StringUtils.deleteWhitespace((String)slots);
            for (String index : slots.split(",")) {
                String[] params = index.split("-");
                if (params.length == 2) {
                    for (int i = Integer.parseInt(params[0]); i < Integer.parseInt(params[1]); ++i) {
                        pos.add(i);
                    }
                    continue;
                }
                if (params.length != 1) continue;
                pos.add(Integer.parseInt(params[0]));
            }
            if (!pos.isEmpty()) {
                return pos;
            }
            return null;
        }
    }

    private ItemStack getTransactionItem(TransactionItemType type, String path, boolean dynamicItem) {
        try {
            ItemStack item = CreateItemMethodes.createItemMaterialFromString(dynamicItem ? "BARRIER" : ConfigManager.getConfig().getString(path + ".material"));
            if (item.getType() == Material.AIR) {
                return null;
            }
            int amount = ConfigManager.getConfig().getInt(path + ".stack-size", 1);
            ItemBuilder builder = new ItemBuilder(item);
            if (amount >= 1) {
                builder.setAmount(amount);
            }
            if (ConfigManager.getConfig().contains(path + ".name")) {
                builder.setDisplayName(this.getTransactionItemName(ConfigManager.getConfig().getString(path + ".name")));
            }
            if (ConfigManager.getConfig().contains(path + ".lore")) {
                builder.withCLore(this.getTransactionItemLore(ConfigManager.getConfig().getStringList(path + ".lore")));
            }
            if (type == TransactionItemType.SELECTED_ITEM && !ConfigManager.getConfig().contains(path + ".lore")) {
                builder.setCLore(Lang.TRANSACTION_SCREEN_TOTAL_AMOUNT.get().getLines());
            }
            if (ConfigManager.getConfig().getBoolean(path + ".enchantment-glint")) {
                builder.withEnchantGlint();
            }
            if (ConfigManager.getConfig().getBoolean(path + ".hideDefaultLore")) {
                builder.hideFlags();
            }
            if (ConfigManager.getConfig().contains(path + ".skullowner")) {
                if (XMaterial.matchXMaterial(item).equals((Object)XMaterial.PLAYER_HEAD)) {
                    builder.setOwner(ConfigManager.getConfig().getString(path + ".skullowner"));
                } else {
                    throw new ItemLoadException(Lang.MATERIAL_NEEDS_TO_BE_SKULL.get());
                }
            }
            item = builder.build();
            return item;
        }
        catch (ItemLoadException e) {
            SendMessage.warnMessage(e.getMessage());
            SendMessage.errorItemConfig(path);
            if (!ConfigManager.getConfig().getDef().contains(path)) {
                return null;
            }
            ItemStack item = XMaterial.matchXMaterial(ConfigManager.getConfig().getDef().getString(path + ".material")).get().parseItem();
            return new ItemBuilder(item).setAmount(ConfigManager.getConfig().getDef().getInt(path + ".stack-size", 1)).withDisplayName(this.getTransactionItemName(ConfigManager.getConfig().getDef().getString(path + ".name"))).withCLore(this.getTransactionItemLore(ConfigManager.getConfig().getDef().getStringList(path + ".lore"))).build();
        }
    }

    private Translatable getTransactionItemName(String s) {
        return this.getTransactionItemName(s, Lang.getDefaultLocale());
    }

    private Translatable getTransactionItemName(String s, Locale locale) {
        if (s == null) {
            return new TranslatableRaw("");
        }
        while (s.contains("%translations-")) {
            try {
                Lang placeholder = Lang.valueOf(s.split("%translations-")[1].split("%")[0].toUpperCase(Locale.ENGLISH).replace("-", "_"));
                s = s.replace("%translations-" + placeholder.getKey() + "%", placeholder.getRaw(locale));
                if (placeholder != Lang.CURRENT_PAGE || s.contains("%page%")) continue;
                s = s + " %page%/%pages%";
            }
            catch (IllegalArgumentException e) {
                break;
            }
        }
        return Lang.fromConfig(s);
    }

    private List<Translatable> getTransactionItemLore(List<String> lore) {
        return this.getTransactionItemLore(lore, Lang.getDefaultLocale());
    }

    private List<Translatable> getTransactionItemLore(List<String> lore, Locale locale) {
        if (lore.isEmpty()) {
            return new ArrayList<Translatable>();
        }
        return lore.stream().map(s -> {
            while (s.contains("%translations-")) {
                try {
                    Lang placeholder = Lang.valueOf(s.split("%translations-")[1].split("%")[0].toUpperCase(Locale.ENGLISH).replace("-", "_"));
                    s = s.replace("%translations-" + placeholder.getKey() + "%", placeholder.getRaw(locale));
                }
                catch (IllegalArgumentException e) {
                    break;
                }
            }
            return Lang.fromConfig(s);
        }).collect(Collectors.toList());
    }

    public List<Translatable> getDisplayLore(ConfigurationSection itemConfig) {
        List<String> l = new ArrayList<>();
        if (itemConfig.contains("displaylore")) {
            l = itemConfig.getStringList("displaylore");
        } else if (itemConfig.contains("lore")) {
            l = itemConfig.getStringList("lore");
        }
        return l.stream().map(Lang::fromConfig).collect(Collectors.toList());
    }

    public Translatable getName(ConfigurationSection itemConfig) throws ItemLoadException {
        if (itemConfig.contains("name")) {
            return Lang.fromConfig(itemConfig.getString("name"));
        }
        return null;
    }

    public List<Translatable> getLore(ConfigurationSection itemConfig) {
        List<String> l = new ArrayList<>();
        if (itemConfig.contains("lore")) {
            l = itemConfig.getStringList("lore");
        }
        return l.stream().map(Lang::fromConfig).collect(Collectors.toList());
    }

    public static final class TransactionItemAction {
        public static final TransactionItemAction NONE = new TransactionItemAction(TransactionItemActionType.NONE);
        public TransactionItemActionType type;
        public int amount = 1;

        public TransactionItemAction(TransactionItemActionType type) {
            this.type = type;
        }

        public TransactionItemAction(String s) {
            try {
                s = s.toUpperCase(Locale.ENGLISH).replace(" ", "_");
                if (s.startsWith("ADD")) {
                    this.getFromStringAmount(TransactionItemActionType.ADD, s);
                } else if (s.startsWith("REMOVE")) {
                    this.getFromStringAmount(TransactionItemActionType.REMOVE, s);
                } else if (s.startsWith("SET")) {
                    this.getFromStringAmount(TransactionItemActionType.SET, s);
                } else {
                    this.type = TransactionItemActionType.valueOf(s);
                }
            }
            catch (IllegalArgumentException e) {
                SendMessage.warnMessage("Failed to load action for transaction item like '" + s + "'");
                this.type = TransactionItemActionType.NONE;
            }
        }

        public void getFromStringAmount(TransactionItemActionType action, String s) {
            this.type = action;
            try {
                this.amount = Math.max(Integer.parseInt(s.replace(action.name(), "")), 1);
            }
            catch (NumberFormatException e) {
                SendMessage.warnMessage("Failed to get amount for transaction item action " + action.name() + " like '" + s + "'");
            }
        }
    }

    public static enum TransactionItemType {
        INCREASE_AMOUNT,
        DECREASE_AMOUNT,
        SELECT_AMOUNT,
        SELECTED_ITEM,
        CONFIRM,
        FILLITEM,
        NORMAL;


        public static TransactionItemType getFromString(TransactionItemAction action, String s) {
            try {
                switch (action.type.ordinal()) {
                    case 0: {
                        return DECREASE_AMOUNT;
                    }
                    case 1: {
                        return INCREASE_AMOUNT;
                    }
                    case 2: {
                        return SELECT_AMOUNT;
                    }
                }
                return TransactionItemType.valueOf(s.toUpperCase(Locale.ENGLISH).replace(" ", "_"));
            }
            catch (IllegalArgumentException | NullPointerException e) {
                SendMessage.warnMessage("Failed to load item type for transaction item like '" + s + "'");
                return NORMAL;
            }
        }
    }

    public static enum TransactionItemActionType {
        REMOVE,
        ADD,
        SET,
        CONFIRM_TRANSACTION,
        INSTA_BUY,
        INSTA_SELL,
        OPEN_BUY_STACKS,
        SELL_ALL,
        BACK,
        COMMAND,
        NONE;


        public static TransactionItemActionType get(String s) {
            return TransactionItemActionType.valueOf(s);
        }
    }
}

