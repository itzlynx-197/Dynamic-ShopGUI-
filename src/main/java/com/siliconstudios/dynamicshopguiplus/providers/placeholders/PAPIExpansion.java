

package com.siliconstudios.dynamicshopguiplus.providers.placeholders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.TransactionLog;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.objects.ShopItem;
import com.siliconstudios.dynamicshopguiplus.objects.shops.ShopSection;
import com.siliconstudios.dynamicshopguiplus.util.PermissionsCache;
import com.siliconstudios.dynamicshopguiplus.util.SimplePair;
import com.siliconstudios.dynamicshopguiplus.util.Transaction;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PAPIExpansion
extends PlaceholderExpansion {
    private final DynamicShopGUIPlus plugin;
    private PlaceholderCache cache;
    private PlaceholderItemCache itemCache;
    private PlaceholderPlayerItemCache playerItemCache;
    private boolean placeholderCache = false;

    public PAPIExpansion(DynamicShopGUIPlus plugin) {
        this.plugin = plugin;
        this.register();
    }

    public String getAuthor() {
        return "Silicon Studios";
    }

    public String getName() {
        return "DynamicShopGUIPlus";
    }

    public String getIdentifier() {
        return "dynamicshopguiplus";
    }

    public String getVersion() {
        return "1.1.0";
    }

    public boolean persist() {
        return true;
    }

    public String onRequest(OfflinePlayer player, String params) {
        if (params.startsWith("buyprice_")) {
            String path = params.replace("buyprice_", "").replace(">", ".");
            if (!path.contains(".")) {
                return "\u00a7cInvalid item path for '" + path + "'";
            }
            return this.getItemBuyPrice(path.split("\\.", 2));
        }
        if (params.startsWith("sellprice_")) {
            String path = params.replace("sellprice_", "").replace(">", ".");
            if (!path.contains(".")) {
                return "\u00a7cInvalid item path for '" + path + "'";
            }
            return this.getItemSellPrice(path.split("\\.", 2));
        }
        if (params.startsWith("discount_")) {
            if (!DynamicShopGUIPlus.getInstance().discountsActive) {
                return "\u00a7cDiscounts not enabled";
            }
            String section = params.replace("discount_", "");
            if (player != null && player.isOnline()) {
                return this.getDiscount(player.getPlayer(), section);
            }
            return "\u00a7cInvalid player";
        }
        if (params.startsWith("sell-multiplier_")) {
            if (!DynamicShopGUIPlus.getInstance().multipliers) {
                return "\u00a7cSell multipliers not enabled";
            }
            String section = params.replace("sell-multiplier_", "");
            if (player != null && player.isOnline()) {
                return this.getMultiplier(player.getPlayer(), section);
            }
            return "\u00a7cInvalid player";
        }
        if (params.startsWith("item_purchases_")) {
            SimplePair<TransactionLog.TimeFrame, String> data = this.getData(params.replace("item_purchases_", ""));
            if (data == null) {
                return "\u00a7cInvalid/missing timeframe, use day/week/month";
            }
            if (data.value == null) {
                return "\u00a7cInvalid/missing item path";
            }
            return this.getTotalTransactions(Transaction.Mode.BUY, (TransactionLog.TimeFrame)((Object)data.key), (String)data.value);
        }
        if (params.startsWith("item_sales_")) {
            SimplePair<TransactionLog.TimeFrame, String> data = this.getData(params.replace("item_sales_", ""));
            if (data == null) {
                return "\u00a7cInvalid/missing timeframe, use day/week/month";
            }
            if (data.value == null) {
                return "\u00a7cInvalid/missing item path";
            }
            return this.getTotalTransactions(Transaction.Mode.SELL, (TransactionLog.TimeFrame)((Object)data.key), (String)data.value);
        }
        if (params.startsWith("player_purchases_")) {
            PlayerData data = this.getPlayerData(params.substring("player_purchases_".length()));
            if (data == null) {
                return "\u00a7cInvalid/missing player, timeframe (day/week/month) or item path";
            }
            return this.getTotalTransactions(Transaction.Mode.BUY, data.timeFrame, data.path == null ? "" : data.path, data.uuid);
        }
        if (params.startsWith("player_sales_")) {
            PlayerData data = this.getPlayerData(params.substring("player_sales_".length()));
            if (data == null) {
                return "\u00a7cInvalid/missing player, timeframe (day/week/month) or item path";
            }
            return this.getTotalTransactions(Transaction.Mode.SELL, data.timeFrame, data.path == null ? "" : data.path, data.uuid);
        }
        PlaceholderCache cache = this.getPlaceholderCache();
        if (cache == null) {
            return "\u00a7cLoading cache...";
        }
        try {
            SimplePair<PlaceholderType, SimpleIndex> placeholder = this.getPlaceholder(params.toUpperCase().split("_", 2));
            if (placeholder == null) {
                return "Unknown placeholder";
            }
            switch (((PlaceholderType)((Object)placeholder.key)).ordinal()) {
                case 0: {
                    return cache.getTopPlayerBoughtItemsCurrentMonth().get((int)((SimpleIndex)((Object)placeholder.value)).index).player.name;
                }
                case 1: {
                    return String.valueOf(cache.getTopPlayerBoughtItemsCurrentMonth().get((int)((SimpleIndex)((Object)placeholder.value)).index).amount);
                }
                case 2: {
                    return cache.getTopPlayerBoughtItemsCurrentWeek().get((int)((SimpleIndex)((Object)placeholder.value)).index).player.name;
                }
                case 3: {
                    return String.valueOf(cache.getTopPlayerBoughtItemsCurrentWeek().get((int)((SimpleIndex)((Object)placeholder.value)).index).amount);
                }
                case 4: {
                    return cache.getTopPlayerBoughtItemsCurrentDay().get((int)((SimpleIndex)((Object)placeholder.value)).index).player.name;
                }
                case 5: {
                    return String.valueOf(cache.getTopPlayerBoughtItemsCurrentDay().get((int)((SimpleIndex)((Object)placeholder.value)).index).amount);
                }
                case 6: {
                    return cache.getTopPlayerSoldItemsCurrentMonth().get((int)((SimpleIndex)((Object)placeholder.value)).index).player.name;
                }
                case 7: {
                    return String.valueOf(cache.getTopPlayerSoldItemsCurrentMonth().get((int)((SimpleIndex)((Object)placeholder.value)).index).amount);
                }
                case 8: {
                    return cache.getTopPlayerSoldItemsCurrentWeek().get((int)((SimpleIndex)((Object)placeholder.value)).index).player.name;
                }
                case 9: {
                    return String.valueOf(cache.getTopPlayerSoldItemsCurrentWeek().get((int)((SimpleIndex)((Object)placeholder.value)).index).amount);
                }
                case 10: {
                    return cache.getTopPlayerSoldItemsCurrentDay().get((int)((SimpleIndex)((Object)placeholder.value)).index).player.name;
                }
                case 11: {
                    return String.valueOf(cache.getTopPlayerSoldItemsCurrentDay().get((int)((SimpleIndex)((Object)placeholder.value)).index).amount);
                }
                case 12: {
                    return cache.getTopBoughtItemsCurrentMonth().get((int)((SimpleIndex)((Object)placeholder.value)).index).item.name;
                }
                case 13: {
                    return String.valueOf(cache.getTopBoughtItemsCurrentMonth().get((int)((SimpleIndex)((Object)placeholder.value)).index).amount);
                }
                case 14: {
                    return cache.getTopBoughtItemsCurrentWeek().get((int)((SimpleIndex)((Object)placeholder.value)).index).item.name;
                }
                case 15: {
                    return String.valueOf(cache.getTopBoughtItemsCurrentWeek().get((int)((SimpleIndex)((Object)placeholder.value)).index).amount);
                }
                case 16: {
                    return cache.getTopBoughtItemsCurrentDay().get((int)((SimpleIndex)((Object)placeholder.value)).index).item.name;
                }
                case 17: {
                    return String.valueOf(cache.getTopBoughtItemsCurrentDay().get((int)((SimpleIndex)((Object)placeholder.value)).index).amount);
                }
                case 18: {
                    return cache.getTopSoldItemsCurrentMonth().get((int)((SimpleIndex)((Object)placeholder.value)).index).item.name;
                }
                case 19: {
                    return String.valueOf(cache.getTopSoldItemsCurrentMonth().get((int)((SimpleIndex)((Object)placeholder.value)).index).amount);
                }
                case 20: {
                    return cache.getTopSoldItemsCurrentWeek().get((int)((SimpleIndex)((Object)placeholder.value)).index).item.name;
                }
                case 21: {
                    return String.valueOf(cache.getTopSoldItemsCurrentWeek().get((int)((SimpleIndex)((Object)placeholder.value)).index).amount);
                }
                case 22: {
                    return cache.getTopSoldItemsCurrentDay().get((int)((SimpleIndex)((Object)placeholder.value)).index).item.name;
                }
                case 23: {
                    return String.valueOf(cache.getTopSoldItemsCurrentDay().get((int)((SimpleIndex)((Object)placeholder.value)).index).amount);
                }
            }
            return "Unknown placeholder";
        }
        catch (IndexOutOfBoundsException e) {
            return "null";
        }
    }

    public String getItemBuyPrice(String[] parts) {
        ShopSection shop = this.plugin.getSection(parts[0]);
        if (shop == null) {
            return "\u00a7cNo shop found for '" + parts[0] + "'";
        }
        ShopItem item = shop.getShopItem(parts[0] + "." + parts[1]);
        if (item == null) {
            return "\u00a7cNo item found in shop '" + parts[0] + "' at index '" + parts[1] + "'";
        }
        if (item.hasItemError()) {
            return "\u00a7cCannot get price of invalid item";
        }
        return DynamicShopGUIPlus.getInstance().formatPrice(item.getEcoType(), item.getBuyPrice());
    }

    public String getItemSellPrice(String[] parts) {
        ShopSection shop = this.plugin.getSection(parts[0]);
        if (shop == null) {
            return "\u00a7cNo shop found for '" + parts[0] + "'";
        }
        ShopItem item = shop.getShopItem(parts[0] + "." + parts[1]);
        if (item == null) {
            return "\u00a7cNo item found in shop '" + parts[0] + "' at index '" + parts[1] + "'";
        }
        if (item.hasItemError()) {
            return "\u00a7cCannot get price of invalid item";
        }
        return DynamicShopGUIPlus.getInstance().formatPrice(item.getEcoType(), item.getSellPrice());
    }

    public String getDiscount(Player player, String section) {
        if (this.plugin.getSection(section) == null) {
            return "\u00a7cNo shop found for '" + section + "'";
        }
        if (!this.plugin.hasDiscount(section)) {
            return "0";
        }
        double discount = 0.0;
        for (String group : this.plugin.getDiscounts(section).keySet()) {
            if (!PermissionsCache.hasPermission(player, "DynamicShopGUIPlus.discounts." + group)) continue;
            double i = this.plugin.getDiscounts(section).get(group);
            if (discount != 0.0 && !(discount < i)) continue;
            discount = i;
        }
        return String.valueOf(discount);
    }

    public String getMultiplier(Player player, String section) {
        if (this.plugin.getSection(section) == null) {
            return "\u00a7cNo shop found for '" + section + "'";
        }
        if (!this.plugin.hasMultiplier(section)) {
            return "0";
        }
        double multiplier = 0.0;
        for (String group : this.plugin.getMultipliers(section).keySet()) {
            if (!PermissionsCache.hasPermission(player, "DynamicShopGUIPlus.sell-multipliers." + group)) continue;
            double i = this.plugin.getMultipliers(section).get(group);
            if (multiplier != 0.0 && !(multiplier < i)) continue;
            multiplier = i;
        }
        return String.valueOf(multiplier);
    }

    public String getTotalTransactions(Transaction.Mode mode, TransactionLog.TimeFrame timeFrame, String path) {
        PlaceholderItemCache cache;
        String section = null;
        String index = null;
        if (!path.isEmpty()) {
            if (!path.contains(".")) {
                if (this.plugin.getSection(path) == null) {
                    return "\u00a7cNo shop found for '" + path + "'";
                }
                section = path;
            } else {
                String[] parts = path.split("\\.", 2);
                ShopSection shop = this.plugin.getSection(parts[0]);
                if (shop == null) {
                    return "\u00a7cNo shop found for '" + parts[0] + "'";
                }
                ShopItem item = shop.getShopItem(parts[0] + "." + parts[1]);
                if (item == null) {
                    return "\u00a7cNo item found in shop '" + parts[0] + "' at index '" + parts[1] + "'";
                }
                section = parts[0];
                index = parts[1];
            }
        }
        if ((cache = this.getPlaceholderItemCache()) == null) {
            return "\u00a7cLoading cache...";
        }
        return String.valueOf(cache.getTotalTransactions(mode, timeFrame, section, index));
    }

    public String getTotalTransactions(Transaction.Mode mode, TransactionLog.TimeFrame timeFrame, String path, UUID playerUUID) {
        PlaceholderPlayerItemCache cache;
        String section = null;
        String index = null;
        if (!path.isEmpty()) {
            if (!path.contains(".")) {
                if (this.plugin.getSection(path) == null) {
                    return "\u00a7cNo shop found for '" + path + "'";
                }
                section = path;
            } else {
                String[] parts = path.split("\\.", 2);
                ShopSection shop = this.plugin.getSection(parts[0]);
                if (shop == null) {
                    return "\u00a7cNo shop found for '" + parts[0] + "'";
                }
                ShopItem item = shop.getShopItem(parts[0] + "." + parts[1]);
                if (item == null) {
                    return "\u00a7cNo item found in shop '" + parts[0] + "' at index '" + parts[1] + "'";
                }
                section = parts[0];
                index = parts[1];
            }
        }
        if ((cache = this.getPlaceholderPlayerItemCache()) == null) {
            return "\u00a7cLoading cache...";
        }
        return String.valueOf(cache.getTotalTransactions(mode, timeFrame, playerUUID, section, index));
    }

    private void enablePlaceholderStore() {
        int delay = ConfigManager.getConfig().getInt("placeholders.transaction-cache") * 20;
        if (delay < 200) {
            delay = 200;
        }
        this.plugin.runTaskAsyncTimer(() -> {
            if (this.cache == null || this.itemCache == null || this.playerItemCache == null) {
                this.plugin.getTransactionLog().loadCache();
            }
            if (this.cache == null) {
                this.cache = new PlaceholderCache();
            } else {
                this.getPlaceholderCache().reloadActivePlaceholders();
            }
            if (this.itemCache == null) {
                this.itemCache = new PlaceholderItemCache();
            } else {
                this.getPlaceholderItemCache().reloadActive();
            }
            if (this.playerItemCache == null) {
                this.playerItemCache = new PlaceholderPlayerItemCache();
            } else {
                this.getPlaceholderPlayerItemCache().reloadActive();
            }
        }, 0L, delay);
        this.placeholderCache = true;
    }

    public PlaceholderCache getPlaceholderCache() {
        if (!this.placeholderCache) {
            SendMessage.infoMessage("Received placeholder request, loading transactions...");
            this.enablePlaceholderStore();
            return null;
        }
        return this.cache;
    }

    public PlaceholderItemCache getPlaceholderItemCache() {
        if (!this.placeholderCache) {
            SendMessage.infoMessage("Received placeholder request, loading transactions...");
            this.enablePlaceholderStore();
            return null;
        }
        return this.itemCache;
    }

    public PlaceholderPlayerItemCache getPlaceholderPlayerItemCache() {
        if (!this.placeholderCache) {
            SendMessage.infoMessage("Received placeholder request, loading transactions...");
            this.enablePlaceholderStore();
            return null;
        }
        return this.playerItemCache;
    }

    private SimplePair<TransactionLog.TimeFrame, String> getData(String params) {
        String[] parts = params.split("_", 2);
        try {
            TransactionLog.TimeFrame timeFrame = TransactionLog.TimeFrame.valueOf(parts[0].toUpperCase(Locale.ENGLISH));
            return new SimplePair<TransactionLog.TimeFrame, String>(timeFrame, (parts.length > 1 ? parts[1].replace(">", ".") : null));
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    private PlayerData getPlayerData(String params) {
        String[] parts = params.split("_");
        if (parts.length < 2) {
            return null;
        }
        for (int i = 1; i < parts.length; ++i) {
            TransactionLog.TimeFrame timeFrame;
            try {
                timeFrame = TransactionLog.TimeFrame.valueOf(parts[i].toUpperCase(Locale.ENGLISH));
            }
            catch (IllegalArgumentException e) {
                continue;
            }
            UUID uuid = this.getPlayerUUID(String.join((CharSequence)"_", Arrays.copyOfRange(parts, 0, i)));
            if (uuid == null) continue;
            String path = i + 1 < parts.length ? String.join((CharSequence)"_", Arrays.copyOfRange(parts, i + 1, parts.length)).replace(">", ".") : null;
            return new PlayerData(uuid, timeFrame, path);
        }
        return null;
    }

    private UUID getPlayerUUID(String s) {
        try {
            return UUID.fromString(s);
        }
        catch (IllegalArgumentException e) {
            return Bukkit.getOfflinePlayer((String)s).getUniqueId();
        }
    }

    private SimplePair<PlaceholderType, SimpleIndex> getPlaceholder(String ... params) {
        try {
            SimpleIndex index = SimpleIndex.get(params[0]);
            PlaceholderType type = PlaceholderType.valueOf(index == null ? String.join((CharSequence)"_", params) : params[1]);
            return new SimplePair<PlaceholderType, SimpleIndex>(type, index == null ? SimpleIndex.FIRST : index);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static final class PlayerData {
        final UUID uuid;
        final TransactionLog.TimeFrame timeFrame;
        final String path;

        PlayerData(UUID uuid, TransactionLog.TimeFrame timeFrame, String path) {
            this.uuid = uuid;
            this.timeFrame = timeFrame;
            this.path = path;
        }
    }

    public final class PlaceholderCache {
        private final ActivePlaceholders activePlaceholders;
        private ArrayList<TransactionLog.TopPlayer> top_player_bought_items_current_month;
        private ArrayList<TransactionLog.TopPlayer> top_player_bought_items_current_week;
        private ArrayList<TransactionLog.TopPlayer> top_player_bought_items_current_day;
        private ArrayList<TransactionLog.TopPlayer> top_player_sold_items_current_month;
        private ArrayList<TransactionLog.TopPlayer> top_player_sold_items_current_week;
        private ArrayList<TransactionLog.TopPlayer> top_player_sold_items_current_day;
        private ArrayList<TransactionLog.TopItem> top_bought_items_current_month;
        private ArrayList<TransactionLog.TopItem> top_bought_items_current_week;
        private ArrayList<TransactionLog.TopItem> top_bought_items_current_day;
        private ArrayList<TransactionLog.TopItem> top_sold_items_current_month;
        private ArrayList<TransactionLog.TopItem> top_sold_items_current_week;
        private ArrayList<TransactionLog.TopItem> top_sold_items_current_day;

        public PlaceholderCache() {
            this.activePlaceholders = new ActivePlaceholders();
            this.reloadActivePlaceholders();
        }

        public void reloadActivePlaceholders() {
            if (this.activePlaceholders.top_player_bought_items_current_month) {
                this.loadTopPlayerBoughtItemsCurrentMonth();
            }
            if (this.activePlaceholders.top_player_bought_items_current_week) {
                this.loadTopPlayerBoughtItemsCurrentWeek();
            }
            if (this.activePlaceholders.top_player_bought_items_current_day) {
                this.loadTopPlayerBoughtItemsCurrentDay();
            }
            if (this.activePlaceholders.top_player_sold_items_current_month) {
                this.loadTopPlayerSoldItemsCurrentMonth();
            }
            if (this.activePlaceholders.top_player_sold_items_current_week) {
                this.loadTopPlayerSoldItemsCurrentWeek();
            }
            if (this.activePlaceholders.top_player_sold_items_current_day) {
                this.loadTopPlayerSoldItemsCurrentDay();
            }
            if (this.activePlaceholders.top_bought_items_current_month) {
                this.loadTopBoughtItemsCurrentMonth();
            }
            if (this.activePlaceholders.top_bought_items_current_week) {
                this.loadTopBoughtItemsCurrentWeek();
            }
            if (this.activePlaceholders.top_bought_items_current_day) {
                this.loadTopBoughtItemsCurrentDay();
            }
            if (this.activePlaceholders.top_sold_items_current_month) {
                this.loadTopSoldItemsCurrentMonth();
            }
            if (this.activePlaceholders.top_sold_items_current_week) {
                this.loadTopSoldItemsCurrentWeek();
            }
            if (this.activePlaceholders.top_sold_items_current_day) {
                this.loadTopSoldItemsCurrentDay();
            }
        }

        public ArrayList<TransactionLog.TopPlayer> getTopPlayerBoughtItemsCurrentMonth() {
            if (!this.activePlaceholders.top_player_bought_items_current_month) {
                this.loadTopPlayerBoughtItemsCurrentMonth();
                this.activePlaceholders.top_player_bought_items_current_month = true;
            }
            return this.top_player_bought_items_current_month;
        }

        private void loadTopPlayerBoughtItemsCurrentMonth() {
            this.top_player_bought_items_current_month = PAPIExpansion.this.plugin.getTransactionLog().getTopThreePlayers(TransactionLog.TimeFrame.MONTH, Transaction.Mode.BUY);
        }

        public ArrayList<TransactionLog.TopPlayer> getTopPlayerBoughtItemsCurrentWeek() {
            if (!this.activePlaceholders.top_player_bought_items_current_week) {
                this.loadTopPlayerBoughtItemsCurrentWeek();
                this.activePlaceholders.top_player_bought_items_current_week = true;
            }
            return this.top_player_bought_items_current_week;
        }

        private void loadTopPlayerBoughtItemsCurrentWeek() {
            this.top_player_bought_items_current_week = PAPIExpansion.this.plugin.getTransactionLog().getTopThreePlayers(TransactionLog.TimeFrame.WEEK, Transaction.Mode.BUY);
        }

        public ArrayList<TransactionLog.TopPlayer> getTopPlayerBoughtItemsCurrentDay() {
            if (!this.activePlaceholders.top_player_bought_items_current_day) {
                this.loadTopPlayerBoughtItemsCurrentDay();
                this.activePlaceholders.top_player_bought_items_current_day = true;
            }
            return this.top_player_bought_items_current_day;
        }

        private void loadTopPlayerBoughtItemsCurrentDay() {
            this.top_player_bought_items_current_day = PAPIExpansion.this.plugin.getTransactionLog().getTopThreePlayers(TransactionLog.TimeFrame.DAY, Transaction.Mode.BUY);
        }

        public ArrayList<TransactionLog.TopPlayer> getTopPlayerSoldItemsCurrentMonth() {
            if (!this.activePlaceholders.top_player_sold_items_current_month) {
                this.loadTopPlayerSoldItemsCurrentMonth();
                this.activePlaceholders.top_player_sold_items_current_month = true;
            }
            return this.top_player_sold_items_current_month;
        }

        private void loadTopPlayerSoldItemsCurrentMonth() {
            this.top_player_sold_items_current_month = PAPIExpansion.this.plugin.getTransactionLog().getTopThreePlayers(TransactionLog.TimeFrame.MONTH, Transaction.Mode.SELL);
        }

        public ArrayList<TransactionLog.TopPlayer> getTopPlayerSoldItemsCurrentWeek() {
            if (!this.activePlaceholders.top_player_sold_items_current_week) {
                this.loadTopPlayerSoldItemsCurrentWeek();
                this.activePlaceholders.top_player_sold_items_current_week = true;
            }
            return this.top_player_sold_items_current_week;
        }

        private void loadTopPlayerSoldItemsCurrentWeek() {
            this.top_player_sold_items_current_week = PAPIExpansion.this.plugin.getTransactionLog().getTopThreePlayers(TransactionLog.TimeFrame.WEEK, Transaction.Mode.SELL);
        }

        public ArrayList<TransactionLog.TopPlayer> getTopPlayerSoldItemsCurrentDay() {
            if (!this.activePlaceholders.top_player_sold_items_current_day) {
                this.loadTopPlayerSoldItemsCurrentDay();
                this.activePlaceholders.top_player_sold_items_current_day = true;
            }
            return this.top_player_sold_items_current_day;
        }

        private void loadTopPlayerSoldItemsCurrentDay() {
            this.top_player_sold_items_current_day = PAPIExpansion.this.plugin.getTransactionLog().getTopThreePlayers(TransactionLog.TimeFrame.DAY, Transaction.Mode.SELL);
        }

        public ArrayList<TransactionLog.TopItem> getTopBoughtItemsCurrentMonth() {
            if (!this.activePlaceholders.top_bought_items_current_month) {
                this.loadTopBoughtItemsCurrentMonth();
                this.activePlaceholders.top_bought_items_current_month = true;
            }
            return this.top_bought_items_current_month;
        }

        private void loadTopBoughtItemsCurrentMonth() {
            this.top_bought_items_current_month = PAPIExpansion.this.plugin.getTransactionLog().getTopThreeItems(TransactionLog.TimeFrame.MONTH, Transaction.Mode.BUY);
        }

        public ArrayList<TransactionLog.TopItem> getTopBoughtItemsCurrentWeek() {
            if (!this.activePlaceholders.top_bought_items_current_week) {
                this.loadTopBoughtItemsCurrentWeek();
                this.activePlaceholders.top_bought_items_current_week = true;
            }
            return this.top_bought_items_current_week;
        }

        private void loadTopBoughtItemsCurrentWeek() {
            this.top_bought_items_current_week = PAPIExpansion.this.plugin.getTransactionLog().getTopThreeItems(TransactionLog.TimeFrame.WEEK, Transaction.Mode.BUY);
        }

        public ArrayList<TransactionLog.TopItem> getTopBoughtItemsCurrentDay() {
            if (!this.activePlaceholders.top_bought_items_current_day) {
                this.loadTopBoughtItemsCurrentDay();
                this.activePlaceholders.top_bought_items_current_day = true;
            }
            return this.top_bought_items_current_day;
        }

        private void loadTopBoughtItemsCurrentDay() {
            this.top_bought_items_current_day = PAPIExpansion.this.plugin.getTransactionLog().getTopThreeItems(TransactionLog.TimeFrame.DAY, Transaction.Mode.BUY);
        }

        public ArrayList<TransactionLog.TopItem> getTopSoldItemsCurrentMonth() {
            if (!this.activePlaceholders.top_sold_items_current_month) {
                this.loadTopSoldItemsCurrentMonth();
                this.activePlaceholders.top_sold_items_current_month = true;
            }
            return this.top_sold_items_current_month;
        }

        private void loadTopSoldItemsCurrentMonth() {
            this.top_sold_items_current_month = PAPIExpansion.this.plugin.getTransactionLog().getTopThreeItems(TransactionLog.TimeFrame.MONTH, Transaction.Mode.SELL);
        }

        public ArrayList<TransactionLog.TopItem> getTopSoldItemsCurrentWeek() {
            if (!this.activePlaceholders.top_sold_items_current_week) {
                this.loadTopSoldItemsCurrentWeek();
                this.activePlaceholders.top_sold_items_current_week = true;
            }
            return this.top_sold_items_current_week;
        }

        private void loadTopSoldItemsCurrentWeek() {
            this.top_sold_items_current_week = PAPIExpansion.this.plugin.getTransactionLog().getTopThreeItems(TransactionLog.TimeFrame.WEEK, Transaction.Mode.SELL);
        }

        public ArrayList<TransactionLog.TopItem> getTopSoldItemsCurrentDay() {
            if (!this.activePlaceholders.top_sold_items_current_day) {
                this.loadTopSoldItemsCurrentDay();
                this.activePlaceholders.top_sold_items_current_day = true;
            }
            return this.top_sold_items_current_day;
        }

        private void loadTopSoldItemsCurrentDay() {
            this.top_sold_items_current_day = PAPIExpansion.this.plugin.getTransactionLog().getTopThreeItems(TransactionLog.TimeFrame.DAY, Transaction.Mode.SELL);
        }
    }

    private static enum PlaceholderType {
        PLAYER_MOST_BOUGHT_ITEMS_CURRENT_MONTH,
        PLAYER_MOST_BOUGHT_ITEMS_CURRENT_MONTH_AMOUNT,
        PLAYER_MOST_BOUGHT_ITEMS_CURRENT_WEEK,
        PLAYER_MOST_BOUGHT_ITEMS_CURRENT_WEEK_AMOUNT,
        PLAYER_MOST_BOUGHT_ITEMS_CURRENT_DAY,
        PLAYER_MOST_BOUGHT_ITEMS_CURRENT_DAY_AMOUNT,
        PLAYER_MOST_SOLD_ITEMS_CURRENT_MONTH,
        PLAYER_MOST_SOLD_ITEMS_CURRENT_MONTH_AMOUNT,
        PLAYER_MOST_SOLD_ITEMS_CURRENT_WEEK,
        PLAYER_MOST_SOLD_ITEMS_CURRENT_WEEK_AMOUNT,
        PLAYER_MOST_SOLD_ITEMS_CURRENT_DAY,
        PLAYER_MOST_SOLD_ITEMS_CURRENT_DAY_AMOUNT,
        MOST_BOUGHT_ITEM_CURRENT_MONTH,
        MOST_BOUGHT_ITEM_CURRENT_MONTH_AMOUNT,
        MOST_BOUGHT_ITEM_CURRENT_WEEK,
        MOST_BOUGHT_ITEM_CURRENT_WEEK_AMOUNT,
        MOST_BOUGHT_ITEM_CURRENT_DAY,
        MOST_BOUGHT_ITEM_CURRENT_DAY_AMOUNT,
        MOST_SOLD_ITEM_CURRENT_MONTH,
        MOST_SOLD_ITEM_CURRENT_MONTH_AMOUNT,
        MOST_SOLD_ITEM_CURRENT_WEEK,
        MOST_SOLD_ITEM_CURRENT_WEEK_AMOUNT,
        MOST_SOLD_ITEM_CURRENT_DAY,
        MOST_SOLD_ITEM_CURRENT_DAY_AMOUNT;

    }

    private static enum SimpleIndex {
        FIRST(0),
        SECOND(1),
        THIRD(2);

        public final int index;

        private SimpleIndex(int index) {
            this.index = index;
        }

        public static SimpleIndex get(String param) {
            try {
                return SimpleIndex.valueOf(param);
            }
            catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    public final class PlaceholderItemCache {
        private final Map<Item, Integer> CACHE = new ConcurrentHashMap<Item, Integer>();

        private void reloadActive() {
            this.CACHE.replaceAll((a, v) -> PAPIExpansion.this.plugin.getTransactionLog().getItemTransactions(a.mode, a.timeFrame, a.section, a.index, null));
        }

        public int getTotalTransactions(Transaction.Mode mode, TransactionLog.TimeFrame timeFrame, String section, String index) {
            return this.CACHE.computeIfAbsent(new Item(mode, timeFrame, section, index), k -> PAPIExpansion.this.plugin.getTransactionLog().getItemTransactions(k.mode, k.timeFrame, k.section, k.index, null));
        }
    }

    public final class PlaceholderPlayerItemCache {
        private final Map<PlayerItem, Integer> CACHE = new ConcurrentHashMap<PlayerItem, Integer>();

        private void reloadActive() {
            this.CACHE.replaceAll((a, v) -> PAPIExpansion.this.plugin.getTransactionLog().getItemTransactions(a.mode, a.timeFrame, a.section, a.index, a.playerUUID));
        }

        public int getTotalTransactions(Transaction.Mode mode, TransactionLog.TimeFrame timeFrame, UUID playerUUID, String section, String index) {
            return this.CACHE.computeIfAbsent(new PlayerItem(mode, timeFrame, playerUUID, section, index), k -> PAPIExpansion.this.plugin.getTransactionLog().getItemTransactions(k.mode, k.timeFrame, k.section, k.index, k.playerUUID));
        }
    }

    private static final class PlayerItem {
        final Transaction.Mode mode;
        final TransactionLog.TimeFrame timeFrame;
        final UUID playerUUID;
        final String section;
        final String index;

        public PlayerItem(Transaction.Mode mode, TransactionLog.TimeFrame timeFrame, UUID playerUUID, String section, String index) {
            this.mode = mode;
            this.timeFrame = timeFrame;
            this.playerUUID = playerUUID;
            this.section = section;
            this.index = index;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof PlayerItem)) {
                return false;
            }
            PlayerItem item = (PlayerItem)o;
            return this.mode == item.mode && this.timeFrame == item.timeFrame && this.playerUUID == item.playerUUID && this.section == null ? item.section == null : this.section.equals(item.section) && Objects.equals(this.index, item.index);
        }

        public int hashCode() {
            return Objects.hash(new Object[]{this.mode, this.timeFrame, this.playerUUID, this.section, this.index});
        }
    }

    private static final class Item {
        final Transaction.Mode mode;
        final TransactionLog.TimeFrame timeFrame;
        final String section;
        final String index;

        public Item(Transaction.Mode mode, TransactionLog.TimeFrame timeFrame, String section, String index) {
            this.mode = mode;
            this.timeFrame = timeFrame;
            this.section = section;
            this.index = index;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Item)) {
                return false;
            }
            Item item = (Item)o;
            return this.mode == item.mode && this.timeFrame == item.timeFrame && this.section == null ? item.section == null : this.section.equals(item.section) && Objects.equals(this.index, item.index);
        }

        public int hashCode() {
            return Objects.hash(new Object[]{this.mode, this.timeFrame, this.section, this.index});
        }
    }

    private final class ActivePlaceholders {
        boolean top_player_bought_items_current_month = false;
        boolean top_player_bought_items_current_week = false;
        boolean top_player_bought_items_current_day = false;
        boolean top_player_sold_items_current_month = false;
        boolean top_player_sold_items_current_week = false;
        boolean top_player_sold_items_current_day = false;
        boolean top_bought_items_current_month = false;
        boolean top_bought_items_current_week = false;
        boolean top_bought_items_current_day = false;
        boolean top_sold_items_current_month = false;
        boolean top_sold_items_current_week = false;
        boolean top_sold_items_current_day = false;

        private ActivePlaceholders() {
        }
    }
}

