

package com.siliconstudios.dynamicshopguiplus.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.CalculateAmount;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.objects.ActionItem;
import com.siliconstudios.dynamicshopguiplus.objects.ShopItem;
import com.siliconstudios.dynamicshopguiplus.providers.UserManager;
import com.siliconstudios.dynamicshopguiplus.providers.priceModifiers.seasons.SeasonProvider;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.ItemRequirement;
import com.siliconstudios.dynamicshopguiplus.util.ChatUtil;
import com.siliconstudios.dynamicshopguiplus.util.EcoType;
import com.siliconstudios.dynamicshopguiplus.util.Replacement;
import com.siliconstudios.dynamicshopguiplus.util.meta.PaperMeta;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class LoreFormatter {
    private final DynamicShopGUIPlus plugin;
    private final boolean components;
    private final List<DisplayLore> displayLore;
    private final ActionAdminLore actionAdminLore;
    private int item;
    private final List<StaticLore> itemLore;

    public LoreFormatter(DynamicShopGUIPlus plugin) {
        ConfigurationSection config;
        this.plugin = plugin;
        this.components = plugin.paperMeta;
        List<String> arrangement = ConfigManager.getConfig().getStringList("lore-arrangement");
        int index = arrangement.indexOf("item-lore");
        if (index == -1) {
            SendMessage.warnMessage("Failed to load lore arrangement, cannot find 'item-lore' which is required!");
            arrangement = ConfigManager.getConfig().getDef().getStringList("lore-arrangement");
            index = arrangement.indexOf("item-lore");
        }
        String path = "display-lore-layout.";
        ArrayList<DisplayLore> displayLore = new ArrayList<DisplayLore>();
        for (String section : arrangement) {
            int i = arrangement.indexOf(section);
            config = null;
            if (!section.equals("item-lore") && (config = ConfigManager.getConfig().getConfigurationSection(path + section)) == null) {
                SendMessage.warnMessage("Cannot find lore format for %part% inside config.yml, using default...".replace("%part%", section));
                config = ConfigManager.getConfig().getDef().getConfigurationSection(path + section);
            }
            switch (section) {
                case "special-lore": {
                    displayLore.add(new SpecialLore(config, i > index));
                    break;
                }
                case "item-requirements": {
                    displayLore.add(new RequirementLore());
                    break;
                }
                case "buy-prices": {
                    displayLore.add(new BuyLore(config, i > index));
                    break;
                }
                case "sell-prices": {
                    displayLore.add(new SellLore(config, i > index));
                    break;
                }
                case "decoration-lore": {
                    displayLore.add(new DecorationLore(config));
                    break;
                }
                case "item-lore": {
                    this.item = i;
                    displayLore.add(new ItemLore());
                    break;
                }
                case "admin-lore": {
                    displayLore.add(new AdminLore(config, i > index));
                }
            }
        }
        this.displayLore = displayLore;
        ArrayList<StaticLore> itemLore = new ArrayList<StaticLore>();
        block43: for (String section : arrangement) {
            config = ConfigManager.getConfig().getConfigurationSection(path + section);
            if (config == null && !section.equals("item-lore")) {
                SendMessage.warnMessage("Cannot find lore format for %part% inside config.yml, using default...".replace("%part%", section));
                config = ConfigManager.getConfig().getDef().getConfigurationSection(path + section);
            }
            switch (section) {
                case "buy-prices": {
                    itemLore.add(new StaticLore(LoreType.BUY_LORE, config.getStringList("normal")));
                    break;
                }
                case "sell-prices": {
                    itemLore.add(new StaticLore(LoreType.SELL_LORE, config.getStringList("normal")));
                    if (!this.plugin.MMB || Lang.SHIFT_RIGHT_CLICK_SELL_ALL.get().getLegacy().isEmpty()) break;
                    itemLore.add(new StaticLore(LoreType.SELL_ALL_LORE, config.getStringList("sell-all")));
                    break;
                }
                case "decoration-lore": {
                    for (String s : ConfigManager.getConfig().getConfigurationSection(path + section).getKeys(false)) {
                        List lore = ConfigManager.getConfig().getStringList(path + section + "." + s);
                        if (lore.isEmpty()) continue;
                        switch (s) {
                            case "extra-lore": {
                                itemLore.add(new StaticLore(LoreType.DECORATION_LORE, lore));
                                break;
                            }
                            case "extra-buy-lore": {
                                itemLore.add(new StaticLore(LoreType.DECORATION_LORE_BUY, lore));
                                break;
                            }
                            case "extra-sell-all-lore": {
                                if (!this.plugin.MMB || Lang.SHIFT_RIGHT_CLICK_SELL_ALL.get().getLegacy().isEmpty()) break;
                                itemLore.add(new StaticLore(LoreType.DECORATION_LORE_SELL_ALL, lore));
                                break;
                            }
                            case "extra-sell-lore": {
                                itemLore.add(new StaticLore(LoreType.DECORATION_LORE_SELL, lore));
                            }
                        }
                    }
                    continue block43;
                }
                case "item-lore": {
                    itemLore.add(new StaticLore(LoreType.ITEM_LORE, (List<String>)null));
                }
            }
        }
        this.itemLore = itemLore;
        this.actionAdminLore = new ActionAdminLore(ConfigManager.getConfig().getConfigurationSection(path + ".admin-lore"));
    }

    public ItemStack format(ActionItem actionItem, ItemStack item, Player p, EcoType type, String section, boolean fast, boolean pr, boolean itemIndex, int page, int pages) {
        Lore lore = this.components ? (fast ? new ComponentLore(item) : new RawComponentLore(item)) : (fast && !pr ? new FastLore(item) : new ShopLore(item));
        if (actionItem.getRequirements() != null) {
            for (ItemRequirement requirement : actionItem.getRequirements()) {
                if (requirement.isMet(p)) continue;
                lore.addAll(requirement.getLore(p, fast, pr));
            }
        }
        lore.addAll(this.parseLocal(actionItem, p, type, fast, pr, page, pages));
        this.actionAdminLore.apply(actionItem, p, lore, section, fast, pr, itemIndex);
        return lore.form();
    }

    public ItemStack format(ShopItem shopItem, ItemStack item, Player p, String section, boolean fast, boolean pr, boolean discounts, boolean multipliers, boolean itemIndex, boolean seasonal, int page, int pages) {
        Lore lore = this.components ? (fast ? new ComponentLore(item) : new RawComponentLore(item)) : (fast && !pr ? new FastLore(item) : new ShopLore(item));
        for (DisplayLore e : shopItem.isPriorLore() ? this.priorLore() : this.displayLore) {
            if (e instanceof ItemLore) {
                lore.addAll(this.parseLocal(shopItem, p, discounts, multipliers, seasonal, fast, pr, page, pages));
                continue;
            }
            if (e instanceof BuyLore || e instanceof SellLore) {
                if (shopItem.isHidePricingLore() || shopItem.hasItemError() || shopItem.isDisplayItem()) continue;
                e.apply(shopItem, p, lore, section, fast, pr, discounts, multipliers, seasonal);
                continue;
            }
            if (e instanceof AdminLore) {
                e.apply(shopItem, p, lore, section, fast, pr, itemIndex);
                continue;
            }
            if (e instanceof SpecialLore) {
                e.apply(shopItem, p, lore, section, fast, pr, seasonal);
                continue;
            }
            if (e instanceof DecorationLore) {
                e.apply(shopItem, p, lore, section, fast, pr, new boolean[0]);
                continue;
            }
            if (!(e instanceof RequirementLore) || shopItem.getRequirements() == null) continue;
            for (ItemRequirement requirement : shopItem.getRequirements()) {
                if (requirement.isMet(p)) continue;
                lore.addAll(requirement.getLore(p, fast, pr));
            }
        }
        return lore.form();
    }

    private List<DisplayLore> priorLore() {
        ArrayList<DisplayLore> l = new ArrayList<DisplayLore>(this.displayLore);
        l.add(0, (DisplayLore)l.remove(this.item));
        return l;
    }

    public List<StaticLore> getItemLore() {
        return this.itemLore;
    }

    private DynamicLoreList getDynamicLoreList(List<String> list, String def, String ... ph) {
        return this.components ? new DynamicCompLoreList(list, def, ph) : new DynamicBukkitLoreList(list, def, ph);
    }

    private static LocalizedLore getLocalized(Map<Locale, LocalizedLore> lore, Player p) {
        LocalizedLore localized = lore.get(UserManager.getUser(p).getLocale());
        return localized != null ? localized : lore.get(Lang.getDefaultLocale());
    }

    private List<String> getStringList(ConfigurationSection config, String key) {
        return config.get(key) instanceof List ? config.getStringList(key) : new ArrayList<String>(Collections.singletonList(config.getString(key)));
    }

    private boolean isEmpty(List<String> list) {
        return list.stream().allMatch(String::isEmpty);
    }

    private ItemStack parseDisplayname(ItemStack item, Player player, ShopItem shopItem, boolean discounts, boolean multipliers, boolean seasonal, boolean fast, boolean pr, int page, int pages) {
        if (!this.components && fast && !pr) {
            return item;
        }
        ItemMeta meta = item.getItemMeta();
        String translated = this.plugin.getMetaUtils().getRawDisplayName(meta);
        this.plugin.getMetaUtils().setRawDisplayName(meta, translated);
        item.setItemMeta(meta);
        return item;
    }

    private Replacement[] pricePlaceholders(ShopItem shopItem, Player p, boolean discounts, boolean multipliers, boolean seasonal, int page, int pages) {
        Replacement[] replacementArray = new Replacement[10];
        replacementArray[0] = new Replacement("%buyPrice%", () -> this.plugin.formatPrice(shopItem.getEcoType(), (discounts || seasonal ? shopItem.getBuyPrice(p) : shopItem.getBuyPrice()) * (double)shopItem.getStackSize()));
        replacementArray[1] = new Replacement("%sellPrice%", () -> this.plugin.formatPrice(shopItem.getEcoType(), (multipliers || seasonal ? shopItem.getSellPrice(p) : shopItem.getSellPrice()) * (double)shopItem.getStackSize()));
        replacementArray[2] = new Replacement("%buyPriceRaw%", () -> String.valueOf((discounts || seasonal ? shopItem.getBuyPrice(p) : shopItem.getBuyPrice()) * (double)shopItem.getStackSize()));
        replacementArray[3] = new Replacement("%sellPriceRaw%", () -> String.valueOf((multipliers || seasonal ? shopItem.getSellPrice(p) : shopItem.getSellPrice()) * (double)shopItem.getStackSize()));
        replacementArray[4] = new Replacement("%player_name%", () -> ((Player)p).getName());
        replacementArray[5] = new Replacement("%player_level%", () -> String.valueOf(p.getLevel()));
        replacementArray[6] = new Replacement("%player_displayname%", () -> this.plugin.getDisplayName(p));
        replacementArray[7] = new Replacement("%player_balance%", () -> this.plugin.formatPrice(shopItem.getEcoType(), this.plugin.getEcoHandler().getEcon(shopItem.getEcoType()).getBalance((OfflinePlayer)p)));
        replacementArray[8] = new Replacement("%page%", () -> String.valueOf(page));
        replacementArray[9] = new Replacement("%pages%", () -> String.valueOf(pages));
        return replacementArray;
    }

    private String[] parseLocal(ShopItem shopItem, Player player, boolean discounts, boolean multipliers, boolean seasonal, boolean fast, boolean pr, int page, int pages) {
        String[] lore = shopItem.getLore().get(fast, pr);
        for (Integer i : shopItem.getPlaceholders()) {
            if (i == -1) continue;
            lore[i.intValue()] = this.replaceLocal(lore[i], this.pricePlaceholders(shopItem, player, discounts, multipliers, seasonal, page, pages));
        }
        return lore;
    }

    private Replacement[] actionPlaceholders(ActionItem actionItem, Player p, EcoType type, int page, int pages) {
        Replacement[] replacementArray = new Replacement[6];
        replacementArray[0] = new Replacement("%player_name%", () -> ((Player)p).getName());
        replacementArray[1] = new Replacement("%player_level%", () -> String.valueOf(p.getLevel()));
        replacementArray[2] = new Replacement("%player_displayname%", () -> this.plugin.getDisplayName(p));
        replacementArray[3] = new Replacement("%player_balance%", () -> this.plugin.formatPrice(type, this.plugin.getEcoHandler().getEcon(type).getBalance((OfflinePlayer)p)));
        replacementArray[4] = new Replacement("%page%", () -> String.valueOf(page));
        replacementArray[5] = new Replacement("%pages%", () -> String.valueOf(pages));
        return replacementArray;
    }

    private String[] parseLocal(ActionItem actionItem, Player player, EcoType type, boolean fast, boolean pr, int page, int pages) {
        String[] lore = actionItem.getLore().get(fast, pr);
        for (Integer i : actionItem.getPlaceholders()) {
            if (i == -1) continue;
            lore[i.intValue()] = this.replaceLocal(lore[i], this.actionPlaceholders(actionItem, player, type, page, pages));
        }
        return lore;
    }

    private String replaceLocal(String s, Replacement[] replacements) {
        for (Replacement replacement : replacements) {
            if (!s.contains(replacement.key)) continue;
            s = s.replace(replacement.key, replacement.value.get());
        }
        return s;
    }

    private String translate(String s) {
        return this.translate(s, Lang.getDefaultLocale());
    }

    private String translate(String s, Locale locale) {
        if (s == null) {
            return "";
        }
        if (s.contains("%translations-")) {
            try {
                Lang placeholder = Lang.valueOf(s.split("%translations-")[1].split("%")[0].toUpperCase(Locale.ENGLISH).replace("-", "_"));
                s = s.replace("%translations-" + placeholder.getKey() + "%", placeholder.getRaw(locale));
            }
            catch (IllegalArgumentException illegalArgumentException) {
                
            }
        }
        return this.components ? ChatUtil.getAdventureUtils().formatLegacyToMini(s) : ChatUtil.formatColors(s);
    }

    private List<String> translate(List<String> list) {
        return this.translate(list, Lang.getDefaultLocale());
    }

    private List<String> translate(List<String> list, Locale locale) {
        if (list.isEmpty()) {
            return list;
        }
        ArrayList<String> lore = new ArrayList<String>();
        for (String s : list) {
            if (s.contains("%translations-")) {
                try {
                    Lang placeholder = Lang.valueOf(s.split("%translations-")[1].split("%")[0].toUpperCase(Locale.ENGLISH).replace("-", "_"));
                    s = s.replace("%translations-" + placeholder.getKey() + "%", placeholder.getRaw(locale).replace("\\n", "\n"));
                }
                catch (IllegalArgumentException illegalArgumentException) {
                    
                }
            }
            for (String e : s.split("\n")) {
                lore.add(this.components ? ChatUtil.getAdventureUtils().formatLegacyToMini(e) : ChatUtil.formatColors(e));
            }
        }
        return lore;
    }

    private static final class LocalizedLore {
        private final Map<Integer, String> placeholders = new HashMap<Integer, String>();
        private final String[] lore;
        private final String[] fastLore;

        private LocalizedLore(String[] lore, String[] fastLore, String ... ph) {
            this.lore = lore;
            this.fastLore = fastLore;
            int i = 0;
            for (String s : lore) {
                if (Stream.of(ph).anyMatch(s::contains)) {
                    this.placeholders.put(i, s);
                }
                ++i;
            }
        }
    }

    private static interface DynamicLoreList {
        public String[] get(boolean var1, boolean var2, Player var3, String ... var4);
    }

    private final class SpecialLore
    implements DisplayLore {
        private final DynamicLoreList seasonalPrice;
        private final boolean after;

        public SpecialLore(ConfigurationSection section, boolean after) {
            this.after = after;
            this.seasonalPrice = LoreFormatter.this.getDynamicLoreList(LoreFormatter.this.getStringList(section, "seasonal-pricing"), Lang.ITEM_SEASONAL_PRICE.get().toString(), new String[]{"%season-icon%", "%season-name%"});
        }

        @Override
        public void apply(ShopItem shopItem, Player p, Lore lore, String section, boolean fast, boolean pr, boolean ... data) {
            if (data[0]) {
                lore.addAll(this.seasonalPrice.get(fast, pr, p, "%season-icon%", SeasonProvider.getSeason(p.getWorld().getName()).getIcon(), "%season-name%", SeasonProvider.getSeason(p.getWorld().getName()).getName()));
            }
        }
    }

    private final class RequirementLore
    implements DisplayLore {
        @Override
        public void apply(ShopItem shopItem, Player p, Lore lore, String section, boolean fast, boolean pr, boolean ... data) {
        }
    }

    private final class BuyLore
    implements DisplayLore {
        private final DynamicLoreList disabled;
        private final DynamicLoreList undefined;
        private final DynamicLoreList buyPrices;
        private final DynamicLoreList seasonalBuyPrices;
        private final DynamicLoreList discountedBuyPrices;
        private final boolean after;

        public BuyLore(ConfigurationSection section, boolean after) {
            this.after = after;
            this.disabled = LoreFormatter.this.getDynamicLoreList(section.getStringList("disabled"), Lang.ITEM_CANNOT_BE_BOUGHT.get().toString(), new String[0]);
            this.undefined = LoreFormatter.this.getDynamicLoreList(section.getStringList("undefined"), "", new String[0]);
            this.buyPrices = LoreFormatter.this.getDynamicLoreList(section.getStringList("normal"), Lang.LEFT_CLICK_BUY.get().toString(), new String[]{"%buyPrice%", "%buyPriceRaw%"});
            this.seasonalBuyPrices = LoreFormatter.this.getDynamicLoreList(section.getStringList("seasonal"), Lang.SEASONAL_BUY_PRICE.get().toString(), new String[]{"%buyPrice%", "%buyPriceRaw%", "%seasonalBuyPrice%"});
            this.discountedBuyPrices = LoreFormatter.this.getDynamicLoreList(section.getStringList("discounted"), Lang.DISCOUNTED_BUY_PRICE.get().toString(), new String[]{"%buyPrice%", "%buyPriceRaw%", "%discountedPrice%"});
        }

        @Override
        public void apply(ShopItem shopItem, Player p, Lore lore, String section, boolean fast, boolean pr, boolean ... data) {
            if (shopItem.isBuyLore()) {
                if (shopItem.getBuyPrice() >= 0.0) {
                    if (data[2]) {
                        lore.addAll(this.seasonalBuyPrices.get(fast, pr, p, "%buyPrice%", ChatColor.stripColor((String)LoreFormatter.this.plugin.formatPrice(shopItem.getEcoType(), shopItem.getBuyPrice() * (double)shopItem.getStackSize())), "%buyPriceRaw%", String.valueOf(shopItem.getBuyPrice() * (double)shopItem.getStackSize()), "%seasonalBuyPrice%", LoreFormatter.this.plugin.formatPrice(shopItem.getEcoType(), shopItem.getBuyPrice(p) * (double)shopItem.getStackSize())));
                    } else if (data[0] && CalculateAmount.calculateDiscount(p, section, shopItem.getBuyPrice()) != shopItem.getBuyPrice()) {
                        lore.addAll(this.discountedBuyPrices.get(fast, pr, p, "%buyPrice%", ChatColor.stripColor((String)LoreFormatter.this.plugin.formatPrice(shopItem.getEcoType(), shopItem.getBuyPrice() * (double)shopItem.getStackSize())), "%buyPriceRaw%", String.valueOf(shopItem.getBuyPrice() * (double)shopItem.getStackSize()), "%discountedPrice%", LoreFormatter.this.plugin.formatPrice(shopItem.getEcoType(), shopItem.getBuyPrice(p) * (double)shopItem.getStackSize())));
                    } else {
                        lore.addAll(this.buyPrices.get(fast, pr, p, "%buyPrice%", LoreFormatter.this.plugin.formatPrice(shopItem.getEcoType(), shopItem.getBuyPrice() * (double)shopItem.getStackSize()), "%buyPriceRaw%", String.valueOf(shopItem.getBuyPrice() * (double)shopItem.getStackSize())));
                    }
                } else {
                    lore.addAll(this.disabled.get(fast, pr, p, new String[0]));
                }
            } else {
                lore.addAll(this.undefined.get(fast, pr, p, new String[0]));
            }
        }
    }

    private final class SellLore
    implements DisplayLore {
        private final DynamicLoreList disabled;
        private final DynamicLoreList undefined;
        private final DynamicLoreList sellPrices;
        private final DynamicLoreList seasonalSellPrices;
        private final DynamicLoreList multipliedSellPrices;
        private final DynamicLoreList sellAllLore;
        private final boolean after;

        public SellLore(ConfigurationSection section, boolean after) {
            this.after = after;
            this.disabled = LoreFormatter.this.getDynamicLoreList(section.getStringList("disabled"), Lang.ITEM_CANNOT_BE_SOLD.get().toString(), new String[0]);
            this.undefined = LoreFormatter.this.getDynamicLoreList(section.getStringList("undefined"), "", new String[0]);
            this.sellPrices = LoreFormatter.this.getDynamicLoreList(section.getStringList("normal"), Lang.RIGHT_CLICK_SELL.get().toString(), new String[]{"%sellPrice%", "%sellPriceRaw%"});
            this.seasonalSellPrices = LoreFormatter.this.getDynamicLoreList(section.getStringList("seasonal"), Lang.SEASONAL_SELL_PRICE.get().toString(), new String[]{"%sellPrice%", "%sellPriceRaw%", "%seasonalSellPrice%"});
            this.multipliedSellPrices = LoreFormatter.this.getDynamicLoreList(section.getStringList("multiplied"), Lang.MULTIPLIED_SELL_PRICE.get().toString(), new String[]{"%sellPrice%", "%sellPriceRaw%", "%multipliedPrice%"});
            this.sellAllLore = LoreFormatter.this.getDynamicLoreList(section.getStringList("sell-all"), Lang.SHIFT_RIGHT_CLICK_SELL_ALL.get().toString(), new String[0]);
        }

        @Override
        public void apply(ShopItem shopItem, Player p, Lore lore, String section, boolean fast, boolean pr, boolean ... data) {
            if (shopItem.isSellLore()) {
                if (shopItem.getSellPrice() >= 0.0) {
                    if (data[2]) {
                        lore.addAll(this.seasonalSellPrices.get(fast, pr, p, "%sellPrice%", ChatColor.stripColor((String)LoreFormatter.this.plugin.formatPrice(shopItem.getEcoType(), shopItem.getSellPrice() * (double)shopItem.getStackSize())), "%sellPriceRaw%", String.valueOf(shopItem.getSellPrice(p) * (double)shopItem.getStackSize()), "%seasonalSellPrice%", LoreFormatter.this.plugin.formatPrice(shopItem.getEcoType(), shopItem.getSellPrice(p) * (double)shopItem.getStackSize())));
                    } else if (data[1] && CalculateAmount.calculateMultiplier(p, section, shopItem.getSellPrice()) != shopItem.getSellPrice()) {
                        lore.addAll(this.multipliedSellPrices.get(fast, pr, p, "%sellPrice%", ChatColor.stripColor((String)LoreFormatter.this.plugin.formatPrice(shopItem.getEcoType(), shopItem.getSellPrice() * (double)shopItem.getStackSize())), "%sellPriceRaw%", String.valueOf(shopItem.getSellPrice(p) * (double)shopItem.getStackSize()), "%multipliedPrice%", LoreFormatter.this.plugin.formatPrice(shopItem.getEcoType(), shopItem.getSellPrice(p) * (double)shopItem.getStackSize())));
                    } else {
                        lore.addAll(this.sellPrices.get(fast, pr, p, "%sellPrice%", LoreFormatter.this.plugin.formatPrice(shopItem.getEcoType(), shopItem.getSellPrice() * (double)shopItem.getStackSize()), "%sellPriceRaw%", String.valueOf(shopItem.getSellPrice() * (double)shopItem.getStackSize())));
                    }
                    if (((LoreFormatter)LoreFormatter.this).plugin.MMB) {
                        lore.addAll(this.sellAllLore.get(fast, pr, p, new String[0]));
                    }
                } else {
                    lore.addAll(this.disabled.get(fast, pr, p, new String[0]));
                }
            } else {
                lore.addAll(this.undefined.get(fast, pr, p, new String[0]));
            }
        }
    }

    private final class DecorationLore
    implements DisplayLore {
        private final DynamicLoreList lore;
        private final DynamicLoreList buyLore;
        private final DynamicLoreList sellLore;
        private final DynamicLoreList sellAllLore;

        public DecorationLore(ConfigurationSection section) {
            this.lore = LoreFormatter.this.getDynamicLoreList(section.getStringList("extra-lore"), "", new String[0]);
            this.buyLore = LoreFormatter.this.getDynamicLoreList(section.getStringList("extra-buy-lore"), "", new String[0]);
            this.sellLore = LoreFormatter.this.getDynamicLoreList(section.getStringList("extra-sell-lore"), "", new String[0]);
            this.sellAllLore = LoreFormatter.this.getDynamicLoreList(section.getStringList("extra-sell-all-lore"), "", new String[0]);
        }

        @Override
        public void apply(ShopItem shopItem, Player p, Lore lore, String section, boolean fast, boolean pr, boolean ... data) {
            if (shopItem.isDecorationLore()) {
                lore.addAll(this.lore.get(fast, pr, p, new String[0]));
                if (shopItem.getBuyPrice() >= 0.0 && shopItem.isBuyLore()) {
                    lore.addAll(this.buyLore.get(fast, pr, p, new String[0]));
                }
                if (shopItem.getSellPrice() >= 0.0 && shopItem.isSellLore()) {
                    if (((LoreFormatter)LoreFormatter.this).plugin.MMB) {
                        lore.addAll(this.sellAllLore.get(fast, pr, p, new String[0]));
                    }
                    lore.addAll(this.sellLore.get(fast, pr, p, new String[0]));
                }
            }
        }
    }

    private final class ItemLore
    implements DisplayLore {
        @Override
        public void apply(ShopItem shopItem, Player p, Lore lore, String section, boolean fast, boolean pr, boolean ... data) {
        }
    }

    private final class AdminLore
    implements DisplayLore {
        private final DynamicLoreList itemIndex;
        private final boolean after;

        public AdminLore(ConfigurationSection section, boolean after) {
            this.after = after;
            this.itemIndex = LoreFormatter.this.getDynamicLoreList(LoreFormatter.this.getStringList(section, "item-index"), Lang.ITEM_LOCATION_IN_SHOP.get().toString(), new String[]{"%shopsection%", "%itemLoc%"});
        }

        @Override
        public void apply(ShopItem shopItem, Player p, Lore lore, String section, boolean fast, boolean pr, boolean ... data) {
            if (data[0]) {
                lore.addAll(this.itemIndex.get(fast, pr, p, "%shopsection%", section, "%itemLoc%", shopItem.itemLoc));
            }
        }
    }

    public final class StaticLore {
        public final LoreType type;
        public final List<String> lore;

        public StaticLore(LoreType type, String[] lore) {
            this.type = type;
            this.lore = lore == null && type == LoreType.ITEM_LORE ? null : LoreFormatter.this.translate(type == LoreType.BUY_LORE || type == LoreType.SELL_LORE ? Arrays.stream(lore).map(s -> s.replace("%dpp%", "")).collect(Collectors.toList()) : Arrays.asList(lore));
        }

        public StaticLore(LoreType type, List<String> lore) {
            this.type = type;
            this.lore = lore == null && type == LoreType.ITEM_LORE ? null : (type == LoreType.BUY_LORE || type == LoreType.SELL_LORE ? LoreFormatter.this.translate(lore).stream().map(s -> s.replace("%dpp%", "")).collect(Collectors.toList()) : LoreFormatter.this.translate(lore));
        }
    }

    public static enum LoreType {
        BUY_LORE,
        SELL_LORE,
        SELL_ALL_LORE,
        DECORATION_LORE,
        DECORATION_LORE_BUY,
        DECORATION_LORE_SELL,
        DECORATION_LORE_SELL_ALL,
        ITEM_LORE;

    }

    private final class ActionAdminLore {
        private final DynamicLoreList itemIndex;

        public ActionAdminLore(ConfigurationSection section) {
            this.itemIndex = LoreFormatter.this.getDynamicLoreList(LoreFormatter.this.getStringList(section, "item-index"), Lang.ITEM_LOCATION_IN_SHOP.get().toString(), new String[]{"%shopsection%", "%itemLoc%"});
        }

        public void apply(ActionItem shopItem, Player p, Lore lore, String section, boolean fast, boolean pr, boolean ... data) {
            if (data[0]) {
                lore.addAll(this.itemIndex.get(fast, pr, p, "%shopsection%", section, "%itemLoc%", shopItem.itemLoc));
            }
        }
    }

    private final class ComponentLore
    implements Lore {
        private final ArrayList<String> lore = new ArrayList();
        private final ItemStack item;

        public ComponentLore(ItemStack item) {
            this.item = item;
        }

        @Override
        public void addAll(String[] list) {
            this.lore.addAll(Arrays.asList(list));
        }

        @Override
        public void add(String o) {
            this.lore.add(o);
        }

        @Override
        public ItemStack form() {
            try {
                return PaperMeta.setLore(this.item, this.lore);
            }
            catch (Exception e) {
                SendMessage.warnMessage("Failed to display component lore on shop item");
                e.printStackTrace();
                return null;
            }
        }
    }

    private final class RawComponentLore
    implements Lore {
        private final ArrayList<String> lore = new ArrayList();
        private final ItemStack item;

        public RawComponentLore(ItemStack item) {
            this.item = item;
        }

        @Override
        public void addAll(String[] list) {
            this.lore.addAll(Arrays.asList(list));
        }

        @Override
        public void add(String o) {
            this.lore.add(o);
        }

        @Override
        public ItemStack form() {
            try {
                return PaperMeta.setRawLore(this.item, this.lore);
            }
            catch (Exception e) {
                SendMessage.warnMessage("Failed to display component lore on shop item");
                e.printStackTrace();
                return null;
            }
        }
    }

    private final class FastLore
    implements Lore {
        private final Object lore;
        private final ItemStack stack;

        public FastLore(ItemStack item) {
            this.lore = ((LoreFormatter)LoreFormatter.this).plugin.versionHandler.emptyList();
            this.stack = item;
        }

        @Override
        public void add(String s) {
            ((LoreFormatter)LoreFormatter.this).plugin.versionHandler.addItemLore(this.lore, s);
        }

        public void add(String s, int i) {
            ((LoreFormatter)LoreFormatter.this).plugin.versionHandler.addItemLore(this.lore, s, i);
        }

        @Override
        public void addAll(String[] list) {
            for (String s : list) {
                this.add(s);
            }
        }

        @Override
        public ItemStack form() {
            return ((LoreFormatter)LoreFormatter.this).plugin.versionHandler.applyNMSLore(this.stack, this.lore);
        }
    }

    private final class ShopLore
    implements Lore {
        private final List<String> lore = new ArrayList<String>();
        private final ItemStack stack;

        public ShopLore(ItemStack item) {
            this.stack = item;
        }

        @Override
        public void add(String s) {
            this.lore.add(s);
        }

        public void add(String s, int i) {
            this.lore.add(i, s);
        }

        @Override
        public void addAll(String[] list) {
            for (String s : list) {
                this.add(s);
            }
        }

        @Override
        public ItemStack form() {
            ItemMeta meta = this.stack.getItemMeta();
            meta.setLore(this.lore);
            this.stack.setItemMeta(meta);
            return this.stack;
        }
    }

    private static interface Lore {
        public void addAll(String[] var1);

        public void add(String var1);

        public ItemStack form();
    }

    private static interface DisplayLore {
        public void apply(ShopItem var1, Player var2, Lore var3, String var4, boolean var5, boolean var6, boolean ... var7);
    }

    private final class DynamicCompLoreList
    implements DynamicLoreList {
        private final LocalizedLore lore;
        private final Map<Locale, LocalizedLore> localizedLore;

        public DynamicCompLoreList(List<String> list, String def, String ... ph) {
            String[] lore = list != null ? (!LoreFormatter.this.isEmpty(list) ? LoreFormatter.this.translate(list).toArray(new String[list.size()]) : new String[]{}) : LoreFormatter.this.translate(def).split("\n");
            String[] compLore = (String[])Arrays.stream(lore).map(ChatUtil::getGsonComponent).toArray(String[]::new);
            this.lore = !((LoreFormatter)LoreFormatter.this).plugin.perPlayerLanguages ? new LocalizedLore(lore, compLore, ph) : null;
            this.localizedLore = ((LoreFormatter)LoreFormatter.this).plugin.perPlayerLanguages ? this.getLocalizedLore(list, def, ph) : null;
        }

        private Map<Locale, LocalizedLore> getLocalizedLore(List<String> list, String def, String ... ph) {
            HashMap<Locale, LocalizedLore> localizedLore = new HashMap<Locale, LocalizedLore>();
            for (Locale locale : Lang.getLocales()) {
                String[] lore = list != null ? (!LoreFormatter.this.isEmpty(list) ? LoreFormatter.this.translate(list, locale).toArray(new String[0]) : new String[]{}) : LoreFormatter.this.translate(def, locale).split("\n");
                String[] compLore = (String[])Arrays.stream(lore).map(ChatUtil::getGsonComponent).toArray(String[]::new);
                localizedLore.put(locale, new LocalizedLore(lore, compLore, ph));
            }
            return localizedLore;
        }

        @Override
        public String[] get(boolean fast, boolean pr, Player p, String ... replacements) {
            LocalizedLore localized = this.localizedLore != null ? LoreFormatter.getLocalized(this.localizedLore, p) : this.lore;
            String[] clone = (String[])(fast ? localized.fastLore : localized.lore).clone();
            for (Map.Entry ph : localized.placeholders.entrySet()) {
                for (int i = 0; i < replacements.length; i += 2) {
                    if (!((String)ph.getValue()).contains(replacements[i])) continue;
                    clone[((Integer)ph.getKey()).intValue()] = clone[(Integer)ph.getKey()].replace(replacements[i], replacements[i + 1]);
                }
            }
            return clone;
        }
    }

    private final class DynamicBukkitLoreList
    implements DynamicLoreList {
        private final LocalizedLore lore;
        private final Map<Locale, LocalizedLore> localizedLore;

        public DynamicBukkitLoreList(List<String> list, String def, String ... ph) {
            String[] lore = list != null ? (!LoreFormatter.this.isEmpty(list) ? LoreFormatter.this.translate(list).toArray(new String[list.size()]) : new String[]{}) : LoreFormatter.this.translate(def).split("\n");
            String[] fastLore = (String[])Arrays.stream(lore).map(s -> LoreFormatter.this.plugin.versionHandler.toNBT((String)s)).toArray(String[]::new);
            this.lore = !((LoreFormatter)LoreFormatter.this).plugin.perPlayerLanguages ? new LocalizedLore(lore, fastLore, ph) : null;
            this.localizedLore = ((LoreFormatter)LoreFormatter.this).plugin.perPlayerLanguages ? this.getLocalizedLore(list, def, ph) : null;
        }

        private Map<Locale, LocalizedLore> getLocalizedLore(List<String> list, String def, String ... ph) {
            HashMap<Locale, LocalizedLore> localizedLore = new HashMap<Locale, LocalizedLore>();
            for (Locale locale : Lang.getLocales()) {
                String[] lore = list != null ? (!LoreFormatter.this.isEmpty(list) ? LoreFormatter.this.translate(list, locale).toArray(new String[0]) : new String[]{}) : LoreFormatter.this.translate(def, locale).split("\n");
                String[] fastLore = (String[])Arrays.stream(lore).map(s -> ((LoreFormatter)LoreFormatter.this).plugin.versionHandler.toNBT((String)s)).toArray(String[]::new);
                localizedLore.put(locale, new LocalizedLore(lore, fastLore, ph));
            }
            return localizedLore;
        }

        @Override
        public String[] get(boolean fast, boolean pr, Player p, String ... replacements) {
            LocalizedLore localized = this.localizedLore != null ? LoreFormatter.getLocalized(this.localizedLore, p) : this.lore;
            String[] clone = (String[])(fast && !pr ? localized.fastLore : localized.lore).clone();
            for (Map.Entry ph : localized.placeholders.entrySet()) {
                for (int i = 0; i < replacements.length; i += 2) {
                    if (!clone[(Integer)ph.getKey()].contains(replacements[i])) continue;
                    clone[((Integer)ph.getKey()).intValue()] = clone[(Integer)ph.getKey()].replace(replacements[i], replacements[i + 1]);
                }
            }
            return clone;
        }
    }
}

