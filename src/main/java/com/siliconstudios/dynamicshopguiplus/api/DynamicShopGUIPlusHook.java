

package com.siliconstudios.dynamicshopguiplus.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.api.events.PostTransactionEvent;
import com.siliconstudios.dynamicshopguiplus.api.events.PreTransactionEvent;
import com.siliconstudios.dynamicshopguiplus.api.objects.BuyPrice;
import com.siliconstudios.dynamicshopguiplus.api.objects.SellPrice;
import com.siliconstudios.dynamicshopguiplus.api.objects.SellPrices;
import com.siliconstudios.dynamicshopguiplus.api.objects.TransactionResult;
import com.siliconstudios.dynamicshopguiplus.api.prices.AdvancedBuyPrice;
import com.siliconstudios.dynamicshopguiplus.api.prices.AdvancedSellPrice;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.objects.ShopItem;
import com.siliconstudios.dynamicshopguiplus.objects.TransactionMenu;
import com.siliconstudios.dynamicshopguiplus.objects.shops.ShopSection;
import com.siliconstudios.dynamicshopguiplus.providers.EconomyProvider;
import com.siliconstudios.dynamicshopguiplus.util.EcoType;
import com.siliconstudios.dynamicshopguiplus.util.PermissionsCache;
import com.siliconstudios.dynamicshopguiplus.util.PlayerItems;
import com.siliconstudios.dynamicshopguiplus.util.Sold;
import com.siliconstudios.dynamicshopguiplus.util.Transaction;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DynamicShopGUIPlusHook {
    private static DynamicShopGUIPlus plugin;

    public DynamicShopGUIPlusHook(DynamicShopGUIPlus plugin) {
        DynamicShopGUIPlusHook.plugin = plugin;
    }

    public static boolean isSellAble(ShopItem shopItem) {
        return shopItem.getSellPrice() >= 0.0;
    }

    @Nullable
    @Deprecated
    public static Double getItemSellPrice(ItemStack item) {
        ShopItem shopItem = DynamicShopGUIPlusHook.plugin.createItem.matchShopItem(item);
        if (shopItem == null) {
            return null;
        }
        return shopItem.getSellPrice(item);
    }

    @Nullable
    @Deprecated
    public static Double getItemSellPrice(Player player, ItemStack item) {
        ShopItem shopItem = DynamicShopGUIPlusHook.plugin.createItem.matchShopItem(player, item, Transaction.Mode.SELL);
        if (shopItem == null) {
            return null;
        }
        return shopItem.getSellPrice(player, item);
    }

    @Nullable
    @Deprecated
    public static Double getItemBuyPrice(ItemStack item) {
        ShopItem shopItem = DynamicShopGUIPlusHook.plugin.createItem.matchShopItem(item);
        if (shopItem == null) {
            return null;
        }
        return shopItem.getBuyPrice() * (double)item.getAmount();
    }

    @Nullable
    @Deprecated
    public static Double getItemBuyPrice(Player player, ItemStack item) {
        ShopItem shopItem = DynamicShopGUIPlusHook.plugin.createItem.matchShopItem(player, item, Transaction.Mode.BUY);
        if (shopItem == null) {
            return null;
        }
        return shopItem.getBuyPrice(player) * (double)item.getAmount();
    }

    public static Double getItemSellPrice(ShopItem shopItem, ItemStack item) {
        return shopItem.getSellPrice(item);
    }

    public static Double getItemSellPrice(ShopItem shopItem, ItemStack item, int amount, int sold) {
        return shopItem.getSellPrice(item);
    }

    public static Double getItemSellPrice(ShopItem shopItem, ItemStack item, Player player) {
        return shopItem.getSellPrice(player, item);
    }

    public static Double getItemSellPrice(ShopItem shopItem, ItemStack item, Player player, int amount, int sold) {
        return shopItem.getSellPrice(player, item, amount);
    }

    public static Optional<SellPrice> getSellPrice(OfflinePlayer player, ItemStack item) {
        ShopItem shopItem;
        Objects.requireNonNull(item, "Item cannot be null");
        Objects.requireNonNull(player, "Player cannot be null");
        boolean online = player.isOnline();
        int amount = item.getAmount();
        ShopItem shopItem2 = shopItem = !online ? DynamicShopGUIPlusHook.getShopItem(item) : DynamicShopGUIPlusHook.plugin.createItem.matchShopItem((Player)player, item, Transaction.Mode.SELL);
        if (shopItem == null || !DynamicShopGUIPlusHook.isSellAble(shopItem)) {
            return Optional.empty();
        }
        if (shopItem.isMaxSell(amount)) {
            return Optional.empty();
        }
        if (shopItem.isMinSell(amount)) {
            return Optional.empty();
        }
        double sellPrice = !online ? DynamicShopGUIPlusHook.getItemSellPrice(shopItem, item, item.getAmount(), 0) : DynamicShopGUIPlusHook.getItemSellPrice(shopItem, item, (Player)player, item.getAmount(), 0);
        SellPrice price = new SellPrice(player, amount, shopItem, shopItem.getEcoType(), sellPrice);
        return Optional.of(price);
    }

    public static SellPrices getSellPrices(OfflinePlayer player, ItemStack ... items) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(items, "Items cannot be null");
        boolean online = player.isOnline();
        HashMap<EcoType, Double> prices = new HashMap<EcoType, Double>();
        HashMap<ShopItem, Sold> CACHE = new HashMap<ShopItem, Sold>();
        for (ItemStack item : items) {
            ShopItem shopItem;
            if (item == null || item.getType() == Material.AIR) continue;
            ShopItem shopItem2 = shopItem = !online ? DynamicShopGUIPlusHook.getShopItem(item) : DynamicShopGUIPlusHook.plugin.createItem.matchShopItem((Player)player, item, Transaction.Mode.SELL);
            if (shopItem == null || !DynamicShopGUIPlusHook.isSellAble(shopItem)) continue;
            Sold cached = CACHE.computeIfAbsent(shopItem, k -> new Sold());
            int alreadySold = cached.getAmount();
            if (shopItem.isMaxSell(item.getAmount() + alreadySold)) continue;
            double sellPrice = !online ? DynamicShopGUIPlusHook.getItemSellPrice(shopItem, item, item.getAmount(), alreadySold) : DynamicShopGUIPlusHook.getItemSellPrice(shopItem, item, (Player)player, item.getAmount(), alreadySold);
            prices.merge(shopItem.getEcoType(), sellPrice, Double::sum);
            cached.addValues(item.getAmount(), sellPrice);
        }
        for (Map.Entry<ShopItem, Sold> entry2 : new HashSet<>(CACHE.entrySet())) {
            if (!((ShopItem)entry2.getKey()).isMinSell(((Sold)entry2.getValue()).getAmount())) continue;
            CACHE.remove(entry2.getKey());
            prices.compute(((ShopItem)entry2.getKey()).getEcoType(), (key, value) -> {
                double newValue = value - ((Sold)entry2.getValue()).getPrice();
                return newValue <= 0.0 ? null : Double.valueOf(newValue);
            });
        }
        return new SellPrices(player, CACHE.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> ((Sold)entry.getValue()).getAmount(), Integer::sum)), prices);
    }

    public static SellPrices getCutSellPrices(OfflinePlayer player, ItemStack[] items, boolean allowModify) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(items, "Items cannot be null");
        boolean online = player.isOnline();
        HashMap<ShopItem, Sold> CACHE = new HashMap<ShopItem, Sold>();
        HashMap<EcoType, Double> prices = new HashMap<EcoType, Double>();
        for (int i = 0; i < items.length; ++i) {
            ShopItem shopItem;
            ItemStack item = items[i];
            if (item == null || item.getType() == Material.AIR) continue;
            ShopItem shopItem2 = shopItem = !online ? DynamicShopGUIPlusHook.getShopItem(item) : DynamicShopGUIPlusHook.plugin.createItem.matchShopItem((Player)player, item, Transaction.Mode.SELL);
            if (shopItem == null || !DynamicShopGUIPlusHook.isSellAble(shopItem)) continue;
            Sold cached = CACHE.computeIfAbsent(shopItem, k -> new Sold());
            int alreadySold = cached.getAmount();
            int limit = DynamicShopGUIPlusHook.getMaxSell(shopItem, item.getAmount(), alreadySold);
            if (limit == -1) continue;
            double sellPrice = !online ? DynamicShopGUIPlusHook.getItemSellPrice(shopItem, item, limit, alreadySold) : DynamicShopGUIPlusHook.getItemSellPrice(shopItem, item, (Player)player, limit, alreadySold);
            prices.merge(shopItem.getEcoType(), sellPrice, Double::sum);
            cached.addValues(limit, sellPrice);
            cached.addIndex(i);
        }
        block1: for (Map.Entry<?, ?> sold : new HashSet<>(CACHE.entrySet())) {
            if (((ShopItem)sold.getKey()).isMinSell(((Sold)sold.getValue()).getAmount())) {
                CACHE.remove(sold.getKey());
                prices.compute(((ShopItem)sold.getKey()).getEcoType(), (key, value) -> {
                    double newValue = value - ((Sold)sold.getValue()).getPrice();
                    return newValue <= 0.0 ? null : Double.valueOf(newValue);
                });
                continue;
            }
            if (!allowModify) continue;
            int amount = ((Sold)sold.getValue()).getAmount();
            for (Integer i : ((Sold)sold.getValue()).getIndexes()) {
                if (amount < items[i].getAmount()) {
                    ItemStack stack = new ItemStack(items[i]);
                    stack.setAmount(stack.getAmount() - amount);
                    items[i.intValue()] = stack;
                    continue block1;
                }
                amount -= items[i].getAmount();
                items[i.intValue()] = null;
            }
        }
        return new SellPrices(player, CACHE.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> ((Sold)entry.getValue()).getAmount(), Integer::sum)), prices);
    }

    @Deprecated
    public static TransactionResult sellItem(Player player, ShopItem shopItem, int amount) {
        return DynamicShopGUIPlusHook.sellItem(player, shopItem, amount, false, false, true, false);
    }

    public static TransactionResult sellItem(Player player, ShopItem shopItem, int amount, @NotNull String transactionType) {
        return DynamicShopGUIPlusHook.sellItem(player, shopItem, amount, false, false, true, false, transactionType);
    }

    @Deprecated
    public static TransactionResult sellItem(Player player, ShopItem shopItem, int amount, boolean checkPermissions, boolean checkRequirements) {
        return DynamicShopGUIPlusHook.sellItem(player, shopItem, amount, checkPermissions, checkRequirements, true, false);
    }

    public static TransactionResult sellItem(Player player, ShopItem shopItem, int amount, boolean checkPermissions, boolean checkRequirements, @NotNull String transactionType) {
        return DynamicShopGUIPlusHook.sellItem(player, shopItem, amount, checkPermissions, checkRequirements, true, false, transactionType);
    }

    @Deprecated
    public static TransactionResult sellItem(Player player, ShopItem shopItem, int amount, boolean checkPermissions, boolean checkRequirements, boolean allowQuantityChange, boolean playerTriggered) {
        return DynamicShopGUIPlusHook.sellItem(player, shopItem, amount, checkPermissions, checkRequirements, allowQuantityChange, playerTriggered, "Unknown");
    }

    public static TransactionResult sellItem(Player player, ShopItem shopItem, int amount, boolean checkPermissions, boolean checkRequirements, boolean allowQuantityChange, boolean playerTriggered, @NotNull String transactionType) {
        int n;
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(shopItem, "ShopItem cannot be null");
        if (amount < 1) {
            throw new NullPointerException("Amount cannot be less then 1");
        }
        if (!player.isOnline()) {
            throw new NullPointerException("Target player should be online");
        }
        if (shopItem.hasItemError()) {
            return new TransactionResult(Transaction.Result.ITEM_ERROR);
        }
        if (shopItem.isDisplayItem()) {
            return new TransactionResult(Transaction.Result.DISPLAY_ITEM);
        }
        if (shopItem.getSellPrice() < 0.0) {
            return new TransactionResult(Transaction.Result.NEGATIVE_ITEM_PRICE);
        }
        if (checkPermissions && !PermissionsCache.hasPermission(player, "DynamicShopGUIPlus.shop." + shopItem.section)) {
            return new TransactionResult(Transaction.Result.NO_PERMISSIONS);
        }
        if (checkRequirements && !shopItem.meetsRequirements(player, !playerTriggered)) {
            return new TransactionResult(Transaction.Result.REQUIREMENTS_FAILED);
        }
        double price = 0.0;
        ArrayList<ItemStack> sellAble = null;
        if (allowQuantityChange ? !PlayerItems.canSell(shopItem, player) : (sellAble = PlayerItems.getSellAble(shopItem, player, amount, false)).stream().mapToInt(ItemStack::getAmount).sum() < amount) {
            if (playerTriggered) {
                SendMessage.chatToPlayer(player, Lang.NO_ITEM_FOUND.get(player));
            }
            DynamicShopGUIPlusHook.callPostS(shopItem, player, amount, price, Transaction.Result.NO_ITEMS_FOUND);
            return new TransactionResult(Transaction.Result.NO_ITEMS_FOUND);
        }
        if (allowQuantityChange) {
            sellAble = PlayerItems.getSellAble(shopItem, player, amount, true);
            n = sellAble.stream().mapToInt(ItemStack::getAmount).sum();
        } else {
            n = amount = amount;
        }
        if (!allowQuantityChange && shopItem.isMaxSell(amount)) {
            DynamicShopGUIPlusHook.callPostS(shopItem, player, amount, price, Transaction.Result.TO_MANY_ITEMS);
            return new TransactionResult(Transaction.Result.TO_MANY_ITEMS);
        }
        if (shopItem.isMinSell(amount)) {
            if (playerTriggered) {
                SendMessage.chatToPlayer(player, Lang.NOT_ENOUGH_ITEMS_TO_SELL.get(player).replace("%min-sell%", String.valueOf(shopItem.getMinSell())).replace("%quantity%", String.valueOf(amount)));
            }
            DynamicShopGUIPlusHook.callPostS(shopItem, player, amount, price, Transaction.Result.NOT_ENOUGH_ITEMS);
            return new TransactionResult(Transaction.Result.NOT_ENOUGH_ITEMS);
        }
        for (ItemStack item2 : sellAble) {
            price += shopItem.getSellPrice(player, item2, item2.getAmount());
        }
        PreTransactionEvent preTransactionEvent = new PreTransactionEvent(shopItem, player, amount, price, Transaction.Type.API_SELL);
        Bukkit.getPluginManager().callEvent((Event)preTransactionEvent);
        price = preTransactionEvent.getPrice();
        if (preTransactionEvent.isCancelled()) {
            DynamicShopGUIPlusHook.callPostS(shopItem, player, amount, price, Transaction.Result.TRANSACTION_CANCELLED);
            return new TransactionResult(Transaction.Result.TRANSACTION_CANCELLED);
        }
        DynamicShopGUIPlusHook.getEcon(shopItem.getEcoType()).depositBalance((OfflinePlayer)player, price);
        sellAble.forEach(item -> player.getInventory().removeItem(new ItemStack[]{item}));
        if (playerTriggered) {
            SendMessage.sendTransactionMessage(player, amount, price, shopItem, Transaction.Mode.SELL, Transaction.Type.API_SELL.withCustomType(transactionType));
        }
        DynamicShopGUIPlusHook.callPostS(shopItem, player, amount, price, Transaction.Result.SUCCESS);
        return new TransactionResult(Transaction.Result.SUCCESS, shopItem, price, amount);
    }

    public static boolean isBuyAble(ShopItem shopItem) {
        return shopItem.getBuyPrice() >= 0.0;
    }

    @Deprecated
    public static Double getItemBuyPrice(ShopItem shopItem, ItemStack item) {
        return shopItem.getBuyPrice() * (double)item.getAmount();
    }

    @Deprecated
    public static Double getItemBuyPrice(ShopItem shopItem, ItemStack item, Player player) {
        return shopItem.getBuyPrice(player) * (double)item.getAmount();
    }

    public static double getItemBuyPrice(ShopItem shopItem, int amount) {
        return shopItem.getBuyPrice() * (double)amount;
    }

    public static double getItemBuyPrice(ShopItem shopItem, Player player, int amount) {
        return shopItem.getBuyPrice(player) * (double)amount;
    }

    public static Optional<BuyPrice> getBuyPrice(OfflinePlayer player, ItemStack item) {
        int stock;
        ShopItem shopItem;
        Objects.requireNonNull(item, "Item cannot be null");
        Objects.requireNonNull(player, "Player cannot be null");
        boolean online = player.isOnline();
        int amount = item.getAmount();
        ShopItem shopItem2 = shopItem = !online ? DynamicShopGUIPlusHook.getShopItem(item) : DynamicShopGUIPlusHook.plugin.createItem.matchShopItem((Player)player, item, Transaction.Mode.BUY);
        if (shopItem == null || !DynamicShopGUIPlusHook.isBuyAble(shopItem)) {
            return Optional.empty();
        }
        if (shopItem.isMaxBuy(item.getAmount())) {
            return Optional.empty();
        }
        if (shopItem.isMinBuy(item.getAmount())) {
            return Optional.empty();
        }
        if (shopItem.getLimitedStockMode() != 0 && amount > (stock = DynamicShopGUIPlusHook.getItemStock(shopItem, player.getUniqueId()))) {
            return Optional.empty();
        }
        double buyPrice = !online ? shopItem.getBuyPrice() * (double)amount : shopItem.getBuyPrice((Player)player) * (double)amount;
        BuyPrice price = new BuyPrice(player, amount, shopItem, shopItem.getEcoType(), buyPrice);
        return Optional.of(price);
    }

    public static boolean hasMultipleBuyPrices(ShopItem shopItem) {
        return false;
    }

    public static AdvancedBuyPrice getMultipleBuyPrices(ShopItem shopItem) {
        return new AdvancedBuyPrice(shopItem);
    }

    public static boolean hasMultipleSellPrices(ShopItem shopItem) {
        return false;
    }

    public static AdvancedSellPrice getMultipleSellPrices(ShopItem shopItem) {
        return new AdvancedSellPrice(shopItem);
    }

    @Deprecated
    public static TransactionResult purchaseItem(Player player, ShopItem shopItem, int amount) {
        return DynamicShopGUIPlusHook.purchaseItem(player, shopItem, amount, false, false, true, false);
    }

    public static TransactionResult purchaseItem(Player player, ShopItem shopItem, int amount, @NotNull String transactionType) {
        return DynamicShopGUIPlusHook.purchaseItem(player, shopItem, amount, false, false, true, false, transactionType);
    }

    @Deprecated
    public static TransactionResult purchaseItem(Player player, ShopItem shopItem, int amount, boolean checkPermissions, boolean checkRequirements) {
        return DynamicShopGUIPlusHook.purchaseItem(player, shopItem, amount, checkPermissions, checkRequirements, true, false);
    }

    public static TransactionResult purchaseItem(Player player, ShopItem shopItem, int amount, boolean checkPermissions, boolean checkRequirements, @NotNull String transactionType) {
        return DynamicShopGUIPlusHook.purchaseItem(player, shopItem, amount, checkPermissions, checkRequirements, true, false, transactionType);
    }

    @Deprecated
    public static TransactionResult purchaseItem(Player player, ShopItem shopItem, int amount, boolean checkPermissions, boolean checkRequirements, boolean allowQuantityChange, boolean playerTriggered) {
        return DynamicShopGUIPlusHook.purchaseItem(player, shopItem, amount, checkPermissions, checkRequirements, allowQuantityChange, playerTriggered, "Unknown");
    }

    public static TransactionResult purchaseItem(Player player, ShopItem shopItem, int amount, boolean checkPermissions, boolean checkRequirements, boolean allowQuantityChange, boolean playerTriggered, @NotNull String transactionType) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(shopItem, "ShopItem cannot be null");
        if (amount < 1) {
            throw new NullPointerException("Amount cannot be less then 1");
        }
        if (!player.isOnline()) {
            throw new NullPointerException("Target player should be online");
        }
        if (shopItem.hasItemError()) {
            return new TransactionResult(Transaction.Result.ITEM_ERROR);
        }
        if (shopItem.isDisplayItem()) {
            return new TransactionResult(Transaction.Result.DISPLAY_ITEM);
        }
        if (shopItem.getBuyPrice() < 0.0) {
            return new TransactionResult(Transaction.Result.NEGATIVE_ITEM_PRICE);
        }
        if (checkPermissions && !PermissionsCache.hasPermission(player, "DynamicShopGUIPlus.shop." + shopItem.section)) {
            return new TransactionResult(Transaction.Result.NO_PERMISSIONS);
        }
        if (checkRequirements && !shopItem.meetsRequirements(player, !playerTriggered)) {
            return new TransactionResult(Transaction.Result.REQUIREMENTS_FAILED);
        }
        double price = 0.0;
        if (allowQuantityChange ? !PlayerItems.canBuy(player, shopItem) : PlayerItems.getFinalAmount(shopItem, player, amount, false) < amount) {
            if (playerTriggered) {
                SendMessage.chatToPlayer(player, Lang.MORE_SPACE_NEEDED.get());
            }
            DynamicShopGUIPlusHook.callPostB(shopItem, player, amount, price, Transaction.Result.NO_INVENTORY_SPACE);
            return new TransactionResult(Transaction.Result.NO_INVENTORY_SPACE);
        }
        int n = amount = allowQuantityChange ? PlayerItems.getFinalAmount(shopItem, player, amount, true) : amount;
        if (!allowQuantityChange && shopItem.isMaxBuy(amount)) {
            DynamicShopGUIPlusHook.callPostB(shopItem, player, amount, price, Transaction.Result.TO_MANY_ITEMS);
            return new TransactionResult(Transaction.Result.TO_MANY_ITEMS);
        }
        if (shopItem.isMinBuy(amount)) {
            if (playerTriggered) {
                SendMessage.chatToPlayer(player, Lang.NOT_ENOUGH_ITEMS_TO_BUY.get().replace("%min-buy%", String.valueOf(shopItem.getMinBuy())));
            }
            DynamicShopGUIPlusHook.callPostB(shopItem, player, amount, price, Transaction.Result.NOT_ENOUGH_ITEMS);
            return new TransactionResult(Transaction.Result.NOT_ENOUGH_ITEMS);
        }
        if (shopItem.getLimitedStockMode() != 0) {
            int stock = DynamicShopGUIPlusHook.getItemStock(shopItem, player.getUniqueId());
            if (allowQuantityChange) {
                amount = Math.min(amount, stock);
            }
            if (allowQuantityChange ? amount <= 0 : stock < amount) {
                DynamicShopGUIPlusHook.callPostB(shopItem, player, amount, price, Transaction.Result.NO_ITEM_STOCK_LEFT);
                return new TransactionResult(Transaction.Result.NO_ITEM_STOCK_LEFT);
            }
        }
        PreTransactionEvent preTransactionEvent = new PreTransactionEvent(shopItem, player, amount, price, Transaction.Type.API_BUY);
        Bukkit.getPluginManager().callEvent((Event)preTransactionEvent);
        price = preTransactionEvent.getPrice();
        if (preTransactionEvent.isCancelled()) {
            DynamicShopGUIPlusHook.callPostB(shopItem, player, amount, price, Transaction.Result.TRANSACTION_CANCELLED);
            return new TransactionResult(Transaction.Result.TRANSACTION_CANCELLED);
        }
        if (DynamicShopGUIPlusHook.getEcon(shopItem.getEcoType()).getBalance((OfflinePlayer)player) < price) {
            DynamicShopGUIPlusHook.callPostB(shopItem, player, amount, price, Transaction.Result.INSUFFICIENT_FUNDS);
            return new TransactionResult(Transaction.Result.INSUFFICIENT_FUNDS);
        }
        DynamicShopGUIPlusHook.getEcon(shopItem.getEcoType()).withdrawBalance((OfflinePlayer)player, price);
        PlayerItems.giveItems(player, shopItem, amount);
        if (playerTriggered) {
            SendMessage.sendTransactionMessage(player, amount, price, shopItem, Transaction.Mode.BUY, Transaction.Type.API_BUY.withCustomType(transactionType));
        }
        DynamicShopGUIPlusHook.callPostB(shopItem, player, amount, price, Transaction.Result.SUCCESS);
        return new TransactionResult(Transaction.Result.SUCCESS, shopItem, price, amount);
    }

    public static EconomyProvider getEcon(@Nullable EcoType ecoType) {
        return plugin.getEcoHandler().getEcon(ecoType);
    }

    public static void logTransaction(UUID uuid, ShopItem item, Map<EcoType, Double> prices, int amount, Transaction.Mode action, @NotNull String transactionType) {
        plugin.getTransactionLog().log(uuid, item, prices, amount, action, (action == Transaction.Mode.BUY ? Transaction.Type.API_BUY : Transaction.Type.API_SELL).withCustomType(transactionType));
    }

    public static void logTransaction(UUID uuid, Map<ShopItem, Integer> items, Map<EcoType, Double> prices, int amount, Transaction.Mode action, @NotNull String transactionType) {
        plugin.getTransactionLog().log(uuid, items, prices, amount, action, (action == Transaction.Mode.BUY ? Transaction.Type.API_BUY : Transaction.Type.API_SELL).withCustomType(transactionType));
    }

    @Deprecated
    public static void buyItem(ItemStack item, int amount) {
    }

    @Deprecated
    public static void sellItem(ItemStack item, int amount) {
    }

    public static void buyItem(ShopItem shopItem, int amount) {
    }

    public static void sellItem(ShopItem shopItem, int amount) {
    }

    public static void openTransactionScreen(Player p, String section, String itemIndex, Transaction.Mode mode) {
        ShopSection shopSection = plugin.getSection(section);
        if (shopSection == null) {
            throw new NullPointerException("A section with name '" + section + "' does not exist");
        }
        ShopItem shopItem = shopSection.getShopItem(section + "." + itemIndex);
        if (shopItem == null) {
            throw new NullPointerException("The shop item at section " + section + " with index " + itemIndex + " does not exist");
        }
        DynamicShopGUIPlusHook.openTransactionScreen(p, shopItem, mode);
    }

    public static void openTransactionScreen(Player p, ShopItem shopItem, Transaction.Mode mode) {
        new TransactionMenu(p, shopItem, null, true, mode, mode == Transaction.Mode.BUY ? Transaction.Type.BUY_SCREEN : Transaction.Type.SELL_SCREEN, 1).open();
    }

    @Nullable
    public static ShopItem getShopItem(ItemStack item) {
        return DynamicShopGUIPlusHook.plugin.createItem.matchShopItem(item);
    }

    @Nullable
    public static ShopItem getShopItem(Player player, ItemStack item) {
        return DynamicShopGUIPlusHook.plugin.createItem.matchShopItem(player, item);
    }

    @Nullable
    public static ShopItem getShopItem(String itemPath) {
        try {
            ShopSection section = plugin.getSection(itemPath.split("\\.")[0]);
            return section.getShopItem(itemPath);
        }
        catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static ShopSection getShopSection(String section) {
        return plugin.getSection(section);
    }

    public static List<String> getShopSections() {
        return plugin.getShopSections();
    }

    public static Map<String, ShopSection> getSections() {
        return plugin.getSections();
    }

    public static int getItemStock(ShopItem shopItem, @Nullable UUID uuid) {
        return Integer.MAX_VALUE;
    }

    public static Long getItemStockRestockTime(ShopItem shopItem, @Nullable UUID uuid) {
        return null;
    }

    public static int buyItemStock(ShopItem shopItem, @Nullable UUID uuid, int amount) {
        return 0;
    }

    public static void sellItemStock(ShopItem shopItem, @Nullable UUID uuid, int amount) {
    }

    public static int getSellLimit(ShopItem shopItem, @Nullable UUID uuid) {
        return Integer.MAX_VALUE;
    }

    public static Long getSellLimitRestockTime(ShopItem shopItem, @Nullable UUID uuid) {
        return null;
    }

    public static int sellItemLimit(ShopItem shopItem, @Nullable UUID uuid, int amount) {
        return 0;
    }

    public static boolean hasPermissions(ShopItem shopItem, Player player) {
        return true;
    }

    public static boolean hasPermissions(ShopItem shopItem, Player player, String root) {
        return true;
    }

    private static int getMaxSell(ShopItem shopItem, int qty, int alreadySold) {
        if (shopItem.getMaxSell() > 0) {
            if (shopItem.isMaxSell(alreadySold)) {
                return -1;
            }
            qty = Math.min(qty, shopItem.getMaxSell() - alreadySold);
        }
        return qty;
    }

    private static void callPostB(ShopItem shopItem, Player p, int amount, double price, Transaction.Result result) {
        DynamicShopGUIPlusHook.callPost(shopItem, p, amount, price, Transaction.Type.API_BUY, result);
    }

    private static void callPostS(ShopItem shopItem, Player p, int amount, double price, Transaction.Result result) {
        DynamicShopGUIPlusHook.callPost(shopItem, p, amount, price, Transaction.Type.API_SELL, result);
    }

    private static void callPost(ShopItem shopItem, Player p, int amount, double price, Transaction.Type type, Transaction.Result result) {
        Bukkit.getPluginManager().callEvent((Event)new PostTransactionEvent(shopItem, p, amount, price, type, result));
    }
}

