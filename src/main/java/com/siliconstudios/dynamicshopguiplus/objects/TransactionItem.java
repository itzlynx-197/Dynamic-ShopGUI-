

package com.siliconstudios.dynamicshopguiplus.objects;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.methodes.CreateItem;
import com.siliconstudios.dynamicshopguiplus.objects.ShopItem;
import com.siliconstudios.dynamicshopguiplus.util.ItemBuilder;
import com.siliconstudios.dynamicshopguiplus.util.Transaction;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TransactionItem {
    private final CreateItem.TransactionItemAction action;
    private final CreateItem.TransactionItemType type;
    private final Set<Integer> placeholders;
    private final boolean pricingPlaceholders;
    private final ItemStack item;
    private final List<Integer> slots;
    private final boolean dynamicItem;
    private final String bedrockTexture;

    public TransactionItem(CreateItem.TransactionItemAction action, CreateItem.TransactionItemType type, ItemStack item, List<Integer> slots, boolean dynamicItem) {
        this(action, type, item, slots, dynamicItem, null);
    }

    public TransactionItem(CreateItem.TransactionItemAction action, CreateItem.TransactionItemType type, ItemStack item, List<Integer> slots, boolean dynamicItem, String bedrockTexture) {
        this.action = action;
        this.dynamicItem = dynamicItem;
        this.placeholders = this.getPlaceholders(item);
        this.pricingPlaceholders = !this.placeholders.isEmpty();
        this.type = type;
        this.item = item;
        this.slots = slots;
        this.bedrockTexture = bedrockTexture;
    }

    public String getBedrockTexture() {
        return this.bedrockTexture;
    }

    public CreateItem.TransactionItemAction getAction() {
        return this.action;
    }

    public CreateItem.TransactionItemType getType() {
        return this.type;
    }

    public ItemStack getItem() {
        return new ItemStack(this.item);
    }

    public ItemStack getItem(ShopItem shopItem, Player p, Transaction.Type type, int amount) {
        ItemStack item;
        ItemStack itemStack = item = this.dynamicItem ? new ItemBuilder(shopItem.getShopItem()).withSimpleMeta(this.getItem()).build() : this.getItem();
        if (this.pricingPlaceholders && p != null) {
            return this.parsePlaceholders(shopItem, p, item, type, amount);
        }
        return item;
    }

    public ItemStack getItem(ItemStack selectedItem, ShopItem shopItem, Player p, Transaction.Type type, int amount) {
        selectedItem.setAmount(amount);
        ItemMeta dMeta = selectedItem.getItemMeta();
        ItemMeta sMeta = this.item.getItemMeta();
        if (sMeta.hasDisplayName()) {
            dMeta.setDisplayName(sMeta.getDisplayName());
        }
        if (sMeta.hasLore()) {
            dMeta.setLore(sMeta.getLore());
        }
        this.parsePricePlaceholders(shopItem, p, dMeta, type, type == Transaction.Type.BUY_STACKS_SCREEN ? amount * selectedItem.getMaxStackSize() : amount);
        selectedItem.setItemMeta(dMeta);
        return selectedItem;
    }

    public List<Integer> getSlots() {
        return this.slots;
    }

    private ItemStack parsePlaceholders(ShopItem shopItem, Player p, ItemStack item, Transaction.Type type, int amount) {
        ItemMeta meta = item.getItemMeta();
        if (this.action.type == CreateItem.TransactionItemActionType.INSTA_BUY || this.action.type == CreateItem.TransactionItemActionType.INSTA_SELL) {
            amount = item.getAmount();
        }
        if (this.placeholders.contains(-1)) {
            meta.setDisplayName(meta.getDisplayName().replace("%amount%", amount + "").replace("%price%", this.getCalculatedPriceFormatted(shopItem, p, item, type, amount)));
        }
        if (meta.hasLore()) {
            List lore = meta.getLore();
            for (Integer i : this.placeholders) {
                if (i == -1) continue;
                lore.set(i, ((String)lore.get(i)).replace("%amount%", amount + "").replace("%price%", this.getCalculatedPriceFormatted(shopItem, p, item, type, amount)));
            }
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    private ItemMeta parsePricePlaceholders(ShopItem shopItem, Player p, ItemMeta meta, Transaction.Type type, int amount) {
        if (this.placeholders.contains(-1)) {
            DynamicShopGUIPlus.getInstance().getMetaUtils().setRawDisplayName(meta, DynamicShopGUIPlus.getInstance().getMetaUtils().getRawDisplayName(meta).replace("%amount%", amount + "").replace("%price%", this.getCalculatedPriceFormatted(shopItem, p, this.item, type, amount)));
        }
        if (meta.hasLore()) {
            List<String> lore = DynamicShopGUIPlus.getInstance().getMetaUtils().getRawLore(meta);
            for (Integer i : this.placeholders) {
                if (i == -1) continue;
                lore.set(i, lore.get(i).replace("%amount%", amount + "").replace("%price%", this.getCalculatedPriceFormatted(shopItem, p, this.item, type, amount)));
            }
            DynamicShopGUIPlus.getInstance().getMetaUtils().setRawLore(meta, lore);
        }
        return meta;
    }

    private double getCalculatedPrice(ShopItem shopItem, Player p, ItemStack item, Transaction.Mode mode, int amount) {
        if (!this.pricingPlaceholders) {
            return 0.0;
        }
        if (this.action.type == CreateItem.TransactionItemActionType.INSTA_BUY || this.action.type == CreateItem.TransactionItemActionType.INSTA_SELL) {
            amount = item.getAmount();
        }
        if (mode == Transaction.Mode.BUY) {
            return shopItem.getBuyPrice(p) * (double)amount;
        }
        if (mode == Transaction.Mode.SELL) {
            return shopItem.getSellPrice(p, item, amount);
        }
        return 0.0;
    }

    private String getCalculatedPriceFormatted(ShopItem shopItem, Player p, ItemStack item, Transaction.Type type, int amount) {
        Transaction.Mode mode;
        if (!this.pricingPlaceholders) {
            return "0.0";
        }
        if (this.action.type == CreateItem.TransactionItemActionType.INSTA_BUY || this.action.type == CreateItem.TransactionItemActionType.INSTA_SELL) {
            int n = amount = type == Transaction.Type.BUY_STACKS_SCREEN ? item.getAmount() * item.getMaxStackSize() : item.getAmount();
        }
        if ((mode = type.getTransactionMode()) == Transaction.Mode.BUY) {
            return DynamicShopGUIPlus.getInstance().formatPrice(shopItem.getEcoType(), shopItem.getBuyPrice(p) * (double)amount);
        }
        if (mode == Transaction.Mode.SELL) {
            return DynamicShopGUIPlus.getInstance().formatPrice(shopItem.getEcoType(), shopItem.getSellPrice(p, item, amount));
        }
        return "0.0";
    }

    private Set<Integer> getPlaceholders(ItemStack item) {
        HashSet<Integer> placeholders = new HashSet<Integer>();
        if (!item.hasItemMeta()) {
            return placeholders;
        }
        String name = item.getItemMeta().getDisplayName();
        if (name != null && (name.contains("%price%") || name.contains("%amount%"))) {
            placeholders.add(-1);
        }
        if (!item.getItemMeta().hasLore()) {
            return placeholders;
        }
        int i = 0;
        for (String s : item.getItemMeta().getLore()) {
            if (s.contains("%price%") || s.contains("%amount%")) {
                placeholders.add(i);
            }
            ++i;
        }
        return placeholders;
    }
}

