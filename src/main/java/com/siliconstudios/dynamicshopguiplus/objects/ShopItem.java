

package com.siliconstudios.dynamicshopguiplus.objects;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.CalculateAmount;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.objects.DisplayItem;
import com.siliconstudios.dynamicshopguiplus.providers.EconomyProvider;
import com.siliconstudios.dynamicshopguiplus.providers.priceModifiers.ModifierType;
import com.siliconstudios.dynamicshopguiplus.providers.priceModifiers.Modifiers;
import com.siliconstudios.dynamicshopguiplus.providers.priceModifiers.PriceModifier;
import com.siliconstudios.dynamicshopguiplus.providers.priceModifiers.seasons.SeasonModifier;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.ItemRequirement;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.RequirementType;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.Requirements;
import com.siliconstudios.dynamicshopguiplus.util.ChatUtil;
import com.siliconstudios.dynamicshopguiplus.util.EcoType;
import com.siliconstudios.dynamicshopguiplus.util.NBTMaps;
import com.siliconstudios.dynamicshopguiplus.util.SkullUtil;
import com.siliconstudios.dynamicshopguiplus.util.XMaterial;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.ItemLoadException;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class ShopItem
implements DisplayItem {
    private static final String[] pricePlaceholders = new String[]{"%buyPrice%", "%buyPriceRaw%", "%sellPrice%", "%sellPriceRaw%", "%player_displayname%", "%player_balance%", "%page%", "%pages%"};
    private ItemStack shopItem;
    private ItemStack itemToGive;
    private boolean error = false;
    private final EconomyProvider economy;
    private String bedrockTexture;
    private boolean decorationLore;
    private boolean displayItem;
    private boolean priorLore;
    private boolean disablePricingLore;
    private boolean buyLore = true;
    private boolean sellLore = true;
    private final boolean hidden;
    private final boolean closeMenu;
    private final boolean matchMeta;
    public boolean rgb;
    private final Modifiers modifiers = new Modifiers();
    private final Requirements requirements;
    private boolean allowDamaged;
    private double damagedPriceDifference;
    private boolean enchantedVariants;
    private int stackSize;
    private final int fixedStackSize;
    private double buyPrice = -1.0;
    private double sellPrice = -1.0;
    private int loreLine = 0;
    private DisplayItem.DynamicLore lore;
    private final List<Integer> placeholders = new ArrayList<Integer>();
    private String displayname;
    private String name;
    private final int maxBuy;
    private final int maxSell;
    private final int minBuy;
    private final int minSell;
    private final String subSection;
    private final String itemPath;
    public final String section;
    public final String itemLoc;

    public ShopItem(String section, String itemLoc) {
        this.section = section;
        this.itemLoc = itemLoc;
        this.itemPath = this.section + "." + this.itemLoc;
        this.hidden = ConfigManager.getShop(this.section).getBoolean("pages." + this.itemLoc + ".hidden");
        this.subSection = ConfigManager.getShop(this.section).getString("pages." + this.itemLoc + ".section");
        if (this.subSection != null) {
            this.displayItem = true;
        }
        this.priorLore = ConfigManager.getShop(this.section).getBoolean("pages." + this.itemLoc + ".prior-lore", DynamicShopGUIPlus.getInstance().prioritizeItemLore);
        this.economy = DynamicShopGUIPlus.getInstance().getEcoHandler().getEcon(this.section);
        this.closeMenu = ConfigManager.getShop(this.section).getBoolean(this.itemPath + ".close-menu");
        this.requirements = DynamicShopGUIPlus.getInstance().getRequirementManager().load(this.section, "pages." + this.itemLoc);
        this.matchMeta = ConfigManager.getShop(section).getBoolean("pages." + this.itemLoc + ".matchMeta", true) && DynamicShopGUIPlus.getInstance().matchMeta;
        this.allowDamaged = ConfigManager.getShop(section).getBoolean("pages." + this.itemLoc + ".damaged-items.enabled", ConfigManager.getSection(this.section).getBoolean("damaged-items.enabled", ConfigManager.getConfig().getBoolean("damaged-items.enabled", true)));
        this.damagedPriceDifference = ConfigManager.getShop(section).getDouble("pages." + this.itemLoc + ".damaged-items.price-difference", ConfigManager.getSection(this.section).getDouble("damaged-items.price-difference", ConfigManager.getConfig().getDouble("damaged-items.price-difference", 0.85)));
        this.enchantedVariants = ConfigManager.getShop(section).getBoolean("pages." + this.itemLoc + ".enchanted-variants", ConfigManager.getSection(this.section).getBoolean("enchanted-variants", DynamicShopGUIPlus.getInstance().allowEnchanted));
        this.bedrockTexture = ConfigManager.getShop(section).getString("pages." + this.itemLoc + ".bedrock-texture");
        this.maxBuy = ConfigManager.getShop(this.section).getInt("pages." + this.itemLoc + ".max-buy");
        this.maxSell = ConfigManager.getShop(this.section).getInt("pages." + this.itemLoc + ".max-sell");
        this.minBuy = ConfigManager.getShop(this.section).getInt("pages." + this.itemLoc + ".min-buy");
        this.minSell = ConfigManager.getShop(this.section).getInt("pages." + this.itemLoc + ".min-sell");
        this.fixedStackSize = ConfigManager.getShop(this.section).getInt("pages." + this.itemLoc + ".fixed-stack-size", 0);
        boolean defaultStackSize = false;
        if (!ConfigManager.getShop(this.section).contains("pages." + this.itemLoc + ".stack-size")) {
            if (this.fixedStackSize > 0) {
                this.stackSize = this.fixedStackSize;
            } else if (this.minBuy > 0 && this.minBuy > this.stackSize) {
                this.stackSize = this.minBuy;
                defaultStackSize = true;
            } else if (this.minSell > 0 && this.minSell > this.stackSize) {
                this.stackSize = this.minSell;
                defaultStackSize = true;
            }
        }
        try {
            if (ChatUtil.hasRGB(this.economy.getPlural()) || ChatUtil.hasRGB(this.economy.getSingular())) {
                this.rgb = true;
            }
            this.shopItem = DynamicShopGUIPlus.getInstance().createItem.loadShopSectionItem(this);
            if (this.shopItem == null) {
                this.shopItem = DynamicShopGUIPlus.getInstance().createItem.getInvalidShopItem(this.section, this.itemLoc);
                this.loreLine = 0;
                this.error = true;
            } else if (this.shopItem.getType() != Material.AIR) {
                this.itemToGive = DynamicShopGUIPlus.getInstance().createItem.loadItemToGive(this);
                this.stackSize = this.itemToGive.getAmount();
                Double buyPrice = DynamicShopGUIPlus.getInstance().createItem.getBaseBuyPrice(this.itemPath);
                Double sellPrice = DynamicShopGUIPlus.getInstance().createItem.getBaseSellPrice(this.itemPath);
                if (buyPrice != null || sellPrice != null) {
                    this.buyPrice = buyPrice != null ? (defaultStackSize ? buyPrice : buyPrice / (double)this.stackSize) : -1.0;
                    this.sellPrice = sellPrice != null ? (defaultStackSize ? sellPrice : sellPrice / (double)this.stackSize) : -1.0;
                } else {
                    this.displayItem = true;
                }
                String string = DynamicShopGUIPlus.getInstance().useItemName ? (this.itemToGive.getItemMeta().hasDisplayName() ? DynamicShopGUIPlus.getInstance().getMetaUtils().getRawDisplayName(this.itemToGive.getItemMeta()) : DynamicShopGUIPlus.getInstance().getMaterialName(this.itemToGive.getType().name())) : (this.name = DynamicShopGUIPlus.getInstance().getMaterialName(this.itemToGive.getType().name()));
                this.displayname = DynamicShopGUIPlus.getInstance().useItemName ? (this.shopItem.getItemMeta().hasDisplayName() ? DynamicShopGUIPlus.getInstance().getMetaUtils().getRawDisplayName(this.shopItem.getItemMeta()) : DynamicShopGUIPlus.getInstance().getMaterialName(this.shopItem.getType().name())) : DynamicShopGUIPlus.getInstance().getMaterialName(this.shopItem.getType().name());
                List<String> list = this.shopItem.hasItemMeta() ? DynamicShopGUIPlus.getInstance().getMetaUtils().getRawLore(this.shopItem.getItemMeta()) : null;
                this.lore = this.getDynamicLore(DynamicShopGUIPlus.getInstance(), list);
                this.loreLine = 2 + (DynamicShopGUIPlus.getInstance().MMB && !Lang.MIDDLE_CLICK_SELL_ALL.get().getLegacy().isEmpty() && sellPrice != null && !(this.sellPrice < 0.0) ? 1 : 0);
                if (ConfigManager.getShop(this.section).getBoolean("pages." + this.itemLoc + ".hidePricingLore", false)) {
                    this.disablePricingLore = true;
                    this.loreLine -= 2;
                } else {
                    if (sellPrice == null || this.sellPrice >= 0.0 && Lang.RIGHT_CLICK_SELL.get().getLegacy().isEmpty() || this.sellPrice < 0.0 && Lang.ITEM_CANNOT_BE_SOLD.get().getLegacy().isEmpty()) {
                        this.sellLore = false;
                        --this.loreLine;
                    }
                    if (buyPrice == null || this.buyPrice >= 0.0 && Lang.LEFT_CLICK_BUY.get().getLegacy().isEmpty() || this.buyPrice < 0.0 && Lang.ITEM_CANNOT_BE_BOUGHT.get().getLegacy().isEmpty()) {
                        this.buyLore = false;
                        --this.loreLine;
                    }
                }
                if (!this.isLinked() && (this.buyPrice >= 0.0 || this.sellPrice >= 0.0)) {
                    this.decorationLore = true;
                }
            } else {
                this.displayItem = true;
            }
        }
        catch (ItemLoadException e) {
            this.shopItem = DynamicShopGUIPlus.getInstance().createItem.getInvalidShopItem(this.section, this.itemLoc, e);
            this.loreLine = CalculateAmount.splitLongString(e.getMessage()).size() - 1;
            this.error = true;
            SendMessage.warnMessage(e.getMessage());
            SendMessage.errorShops(section, itemLoc);
        }
        catch (Exception e) {
            this.shopItem = DynamicShopGUIPlus.getInstance().createItem.getInvalidShopItem(this.section, this.itemLoc);
            this.loreLine = 0;
            this.error = true;
            SendMessage.warnMessage("A error occurred while loading item '" + itemLoc + "' from section " + section);
            e.printStackTrace();
        }
    }

    public ShopItem(ShopItem shopItem) {
        this.itemPath = shopItem.itemPath;
        this.subSection = shopItem.subSection;
        this.section = shopItem.section;
        this.itemLoc = shopItem.itemLoc;
        this.hidden = shopItem.hidden;
        this.economy = shopItem.economy;
        this.closeMenu = shopItem.closeMenu;
        this.matchMeta = shopItem.matchMeta;
        this.maxBuy = shopItem.maxBuy;
        this.maxSell = shopItem.maxSell;
        this.minBuy = shopItem.minBuy;
        this.minSell = shopItem.minSell;
        this.shopItem = shopItem.shopItem;
        this.itemToGive = shopItem.itemToGive;
        this.buyPrice = shopItem.buyPrice;
        this.sellPrice = shopItem.sellPrice;
        this.requirements = shopItem.requirements;
        this.name = shopItem.name;
        this.displayname = shopItem.displayname;
        this.loreLine = shopItem.loreLine;
        this.buyLore = shopItem.buyLore;
        this.sellLore = shopItem.sellLore;
        this.priorLore = shopItem.priorLore;
        this.disablePricingLore = shopItem.disablePricingLore;
        this.enchantedVariants = shopItem.enchantedVariants;
        this.error = shopItem.error;
        this.stackSize = shopItem.stackSize;
        this.fixedStackSize = shopItem.fixedStackSize;
        this.displayItem = shopItem.displayItem;
    }

    @Override
    public DisplayItem.DynamicLore getLore() {
        return this.lore;
    }

    @Override
    public String section() {
        return this.section;
    }

    @Override
    public String itemLoc() {
        return this.itemLoc;
    }

    @Override
    public String getItemPath() {
        return this.itemPath;
    }

    public String getBedrockTexture() {
        return this.bedrockTexture;
    }

    @Override
    public ItemStack getShopItem() {
        return this.shopItem;
    }

    public ItemStack getItemToGive() {
        return this.itemToGive;
    }

    public boolean hasPlaceholders() {
        return !this.placeholders.isEmpty();
    }

    public List<Integer> getPlaceholders() {
        return this.placeholders;
    }

    @Override
    public boolean meetsRequirements(Player p, boolean silent) {
        if (this.requirements == null) {
            return true;
        }
        for (ItemRequirement requirement : this.requirements) {
            if (requirement.isMet(p)) continue;
            if (!silent) {
                requirement.sendNotMetMessage(p);
            }
            return false;
        }
        return true;
    }

    @Override
    public void addItemRequirement(ItemRequirement requirement) {
        this.requirements.add(requirement);
    }

    @Override
    public void addItemRequirements(Collection<? extends ItemRequirement> requirements) {
        this.requirements.addAll(requirements);
    }

    @Override
    public Requirements getRequirements() {
        return this.requirements;
    }

    @Override
    public boolean hasItemRequirement(RequirementType type) {
        return this.requirements.hasType(type);
    }

    @Override
    public void removeItemRequirement(ItemRequirement requirement) {
        this.requirements.remove(requirement);
    }

    @Override
    public void removeItemRequirements(Collection<? extends ItemRequirement> requirements) {
        this.requirements.removeAll(requirements);
    }

    public boolean isLinked() {
        return this.subSection != null;
    }

    public String getSubSection() {
        return this.subSection;
    }

    public int getStackSize() {
        return this.stackSize;
    }

    public boolean isFixedStackSize() {
        return this.fixedStackSize > 0;
    }

    public int getFixedStackSize() {
        return this.fixedStackSize;
    }

    public int getClosestStackSize(int amount) {
        return Math.max(this.fixedStackSize, Math.round((float)amount / (float)this.fixedStackSize) * this.fixedStackSize);
    }

    public int getLargestStackSize(int amount) {
        return Math.max(this.fixedStackSize, (int)Math.ceil((double)amount / (double)this.fixedStackSize) * this.fixedStackSize);
    }

    public int getSmallestStackSize(int amount) {
        return Math.max(this.fixedStackSize, (int)Math.floor((double)amount / (double)this.fixedStackSize) * this.fixedStackSize);
    }

    public double getBuyPrice() {
        return this.buyPrice;
    }

    public double getBuyPrice(Player player) {
        return this.getPlayerBuyPrice(player, this.buyPrice);
    }

    private double getPlayerBuyPrice(Player player, double price) {
        for (PriceModifier modifier : this.modifiers) {
            price = modifier.modify(player, price);
        }
        if (DynamicShopGUIPlus.getInstance().discountsActive && DynamicShopGUIPlus.getInstance().hasDiscount(this.section)) {
            price = CalculateAmount.calculateDiscount(player, this.section, this.buyPrice);
        }
        return this.economy.round(price);
    }

    public double getSellPrice() {
        return this.sellPrice;
    }

    public double getSellPrice(ItemStack itemToSell) {
        return itemToSell.getType().equals((Object)XMaterial.SPAWNER.parseMaterial()) ? DynamicShopGUIPlus.getInstance().getSpawnerManager().getProvider().getSpawnerSellPrice(itemToSell, this.sellPrice) * (double)itemToSell.getAmount() : this.getItemSellPrice(this.sellPrice * (double)itemToSell.getAmount(), itemToSell);
    }

    public double getSellPrice(Player player) {
        return this.getPlayerSellPrice(player, this.sellPrice);
    }

    public double getSellPrice(Player player, ItemStack itemToSell) {
        double base = itemToSell.getType().equals((Object)XMaterial.SPAWNER.parseMaterial()) ? DynamicShopGUIPlus.getInstance().getSpawnerManager().getProvider().getSpawnerSellPrice(itemToSell, this.sellPrice) * (double)itemToSell.getAmount() : this.sellPrice * (double)itemToSell.getAmount();
        return this.getPlayerSellPrice(player, this.getItemSellPrice(base, itemToSell));
    }

    public double getSellPrice(Player player, ItemStack itemToSell, int amount) {
        double base = itemToSell.getType().equals((Object)XMaterial.SPAWNER.parseMaterial()) ? DynamicShopGUIPlus.getInstance().getSpawnerManager().getProvider().getSpawnerSellPrice(itemToSell, this.sellPrice) * (double)amount : this.sellPrice * (double)amount;
        return this.getPlayerSellPrice(player, this.getItemSellPrice(base, itemToSell));
    }

    private double getPlayerSellPrice(Player player, double price) {
        for (PriceModifier modifier : this.modifiers) {
            price = modifier.modify(player, price);
        }
        if (DynamicShopGUIPlus.getInstance().multipliers && DynamicShopGUIPlus.getInstance().hasMultiplier(this.section)) {
            price = CalculateAmount.calculateMultiplier(player, this.section, price);
        }
        return this.economy.round(price);
    }

    private double getItemSellPrice(double base, ItemStack itemToSell) {
        Integer damage;
        if (this.allowDamaged && this.itemToGive.getType().getMaxDurability() > 0 && (damage = DynamicShopGUIPlus.getInstance().versionHandler.getNBTInt(itemToSell, NBTMaps.DAMAGE.getTag())) != null && damage > 0) {
            short max = itemToSell.getType().getMaxDurability();
            double durabilityP = (double)(max - damage) / (double)max;
            base = base * durabilityP * this.damagedPriceDifference;
        }
        if (this.enchantedVariants) {
            base += this.getEnchantAddition(base, itemToSell);
        }
        return base;
    }

    private double getEnchantAddition(double base, ItemStack itemToSell) {
        int amount = Math.max(1, itemToSell.getAmount());
        return DynamicShopGUIPlus.getInstance().getEnchantCostManager().getEnchantAddition(itemToSell, this.itemToGive, base / (double)amount) * (double)amount;
    }

    @Override
    public String getDisplayname() {
        return this.displayname;
    }

    public String getName() {
        return this.name;
    }

    public boolean isDisplayItem() {
        return this.displayItem;
    }

    public boolean isDynamicPricing() {
        return false;
    }

    public int getLevelRequired() {
        return 0;
    }

    public int getLimitedStockMode() {
        return 0;
    }

    public boolean isRefillStock() {
        return false;
    }

    public int getLimitedSellMode() {
        return 0;
    }

    @Override
    public boolean hasItemError() {
        return this.error;
    }

    public int getMaxBuy() {
        return this.maxBuy;
    }

    public boolean isMaxBuy(int amount) {
        return this.maxBuy > 0 && this.maxBuy < amount;
    }

    public int getMaxSell() {
        return this.maxSell;
    }

    public boolean isMaxSell(int amount) {
        return this.maxSell > 0 && this.maxSell < amount;
    }

    public int getMinBuy() {
        return this.minBuy;
    }

    public boolean isMinBuy(int amount) {
        return this.minBuy > 0 && this.minBuy > amount;
    }

    public int getMinSell() {
        return this.minSell;
    }

    public boolean isMinSell(int amount) {
        return this.minSell > 0 && this.minSell > amount;
    }

    public int getLoreLine() {
        return this.loreLine;
    }

    public boolean isBuyLore() {
        return this.buyLore;
    }

    public boolean isSellLore() {
        return this.sellLore;
    }

    public boolean isDecorationLore() {
        return this.decorationLore;
    }

    public boolean isPriorLore() {
        return this.priorLore;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public boolean isCloseMenu() {
        return this.closeMenu;
    }

    public boolean isHidePricingLore() {
        return this.disablePricingLore;
    }

    public EcoType getEcoType() {
        return this.economy.getType();
    }

    public void addPriceModifier(PriceModifier modifier) {
        this.modifiers.add(modifier);
    }

    public void addPriceModifiers(Collection<? extends PriceModifier> modifiers) {
        this.modifiers.addAll(modifiers);
    }

    public boolean hasPriceModifier(ModifierType type) {
        return this.modifiers.hasType(type);
    }

    public boolean hasSeasonModifier(String world) {
        return this.modifiers.hasSeasonModifier(world);
    }

    public void removePriceModifier(SeasonModifier modifier) {
        this.modifiers.remove(modifier);
    }

    public void removePriceModifiers(Collection<? extends PriceModifier> modifiers) {
        this.modifiers.removeAll(modifiers);
    }

    @Override
    public void updateSkullTexture(GameProfile profile, boolean itg) {
        SkullMeta meta = (SkullMeta)(itg ? this.itemToGive : this.shopItem).getItemMeta();
        SkullUtil.applySkullProfile(profile, meta);
        (itg ? this.itemToGive : this.shopItem).setItemMeta((ItemMeta)meta);
    }

    public boolean matchMeta(ItemStack item) {
        if (!this.matchMeta) {
            return true;
        }
        if (item.getType().equals((Object)XMaterial.SPAWNER.parseMaterial())) {
            return DynamicShopGUIPlus.getInstance().getSpawnerManager().getProvider().isShopSpawner(item, this.itemToGive);
        }
        ArrayList<String> ignored = new ArrayList<String>();
        if (this.allowDamaged) {
            ignored.add(NBTMaps.DAMAGE.getTag());
        }
        if (this.enchantedVariants) {
            ignored.add(NBTMaps.ENCHANTMENTS.getTag());
            ignored.add(NBTMaps.STORED_ENCHANTMENTS.getTag());
        }
        return DynamicShopGUIPlus.getInstance().isSimilar(item, this.itemToGive, ignored);
    }

    public boolean match(ItemStack item) {
        if (!item.getType().equals((Object)this.itemToGive.getType())) {
            return false;
        }
        return this.matchMeta(item);
    }

    private DisplayItem.DynamicLore getDynamicLore(DynamicShopGUIPlus plugin, List<String> lore) {
        return plugin.paperMeta ? new DynamicCompLore(lore) : new DynamicBukkitLore(plugin, lore);
    }

    public final class DynamicCompLore
    implements DisplayItem.DynamicLore {
        private final String[] lore;
        private final String[] compLore;

        public DynamicCompLore(List<String> list) {
            this.lore = list == null || list.isEmpty() ? new String[]{} : list.toArray(new String[0]);
            this.compLore = (String[])Arrays.stream(this.lore).map(ChatUtil::getGsonComponent).toArray(String[]::new);
            int i = 0;
            for (String s : this.compLore) {
                if (Arrays.stream(pricePlaceholders).anyMatch(s::contains)) {
                    ShopItem.this.placeholders.add(i);
                }
                ++i;
            }
        }

        @Override
        public String[] get(boolean fast, boolean pr) {
            return (String[])(fast ? this.compLore : this.lore).clone();
        }
    }

    public final class DynamicBukkitLore
    implements DisplayItem.DynamicLore {
        private final String[] lore;
        private final String[] fastLore;

        public DynamicBukkitLore(DynamicShopGUIPlus plugin, List<String> list) {
            this.lore = list == null || list.isEmpty() ? new String[]{} : (String[])list.stream().map(ChatUtil::formatColors).toArray(String[]::new);
            this.fastLore = (String[])Arrays.stream(this.lore).map(s -> plugin.versionHandler.toNBT((String)s)).toArray(String[]::new);
            int i = 0;
            for (String s2 : this.lore) {
                if (Arrays.stream(pricePlaceholders).anyMatch(s2::contains)) {
                    ShopItem.this.placeholders.add(i);
                }
                ++i;
            }
        }

        @Override
        public String[] get(boolean fast, boolean pr) {
            return (String[])(fast && !pr ? this.fastLore : this.lore).clone();
        }
    }

    public static class Shulker
    extends ShopItem {
        private final Map<ShopItem, Integer> contents;
        private final ItemStack box;

        public Shulker(ShopItem shopItem, ItemStack box, Map<ShopItem, Integer> contents) {
            super(shopItem);
            this.box = box;
            this.contents = contents;
        }

        public Map<ShopItem, Integer> getContents() {
            return this.contents;
        }

        public ItemStack getBox() {
            return this.box;
        }
    }
}

