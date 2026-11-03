

package com.siliconstudios.dynamicshopguiplus.methodes;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.files.Translatable;
import com.siliconstudios.dynamicshopguiplus.objects.ShopItem;
import com.siliconstudios.dynamicshopguiplus.providers.DiscordSRVProvider;
import com.siliconstudios.dynamicshopguiplus.util.ChatUtil;
import com.siliconstudios.dynamicshopguiplus.util.EcoType;
import com.siliconstudios.dynamicshopguiplus.util.Transaction;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SendMessage {
    private static DynamicShopGUIPlus plugin;
    private static DiscordSRVProvider discordSRV;
    private static boolean advancedTransactionStore;
    private static boolean logPlayerTransactions;
    private static boolean logTransactions;
    private static boolean onScreenTitle;

    public SendMessage(DynamicShopGUIPlus plugin) {
        SendMessage.plugin = plugin;
    }

    public static void init() {
        if (ConfigManager.getConfig().getBoolean("log-player-transactions")) {
            logPlayerTransactions = true;
        }
        if (ConfigManager.getConfig().getBoolean("transaction-log")) {
            logTransactions = true;
        }
        if (ConfigManager.getConfig().getBoolean("advanced-transaction-log", true)) {
            advancedTransactionStore = true;
        }
        if (ConfigManager.getConfig().getBoolean("on-screen-title")) {
            onScreenTitle = true;
        }
        if (false) {
        }
        plugin.getTransactionLog().setup();
    }

    public static void sendTransactionMessage(Player player, int amount, double price, ShopItem shopItem, Transaction.Mode mode, Transaction.Type type) {
        plugin.getMetaUtils().sendTransactionMessage(player, amount, price, shopItem, mode, type);
        Translatable message = Lang.PLAYER_MADE_TRANSACTION.get();
        String resetColor = ChatUtil.getLastColors(message.toString().split("%material%")[0]);
        if (resetColor.isEmpty()) {
            resetColor = "\u00a7f";
        }
        String finalResetColor = resetColor;
        SendMessage.logPlayerTransaction(message.replace("%playername%", player.getName()).replace("%player_displayname%", plugin.getDisplayName(player)).replace("%bought/sold%", mode.getName()).replace("%amount%", String.valueOf(amount)).replace("%material%", shopItem instanceof ShopItem.Shulker ? shopItem.getDisplayname() + resetColor + "(" + shopItem.getItemPath() + ")" + ((ShopItem.Shulker)shopItem).getContents().entrySet().stream().map(entry -> entry.getValue() + "x " + ((ShopItem)entry.getKey()).getDisplayname() + finalResetColor + "(" + ((ShopItem)entry.getKey()).getItemPath() + ")").collect(Collectors.toList()) : shopItem.getDisplayname() + resetColor + "(" + shopItem.getItemPath() + ")").replace("%amountofmoney%", plugin.formatPrice(shopItem.getEcoType(), price) + resetColor).replace("%buy/sell-method%", type.getName()));
        if (discordSRV != null && SendMessage.discordSRV.enabled) {
            plugin.runTaskAsync(() -> discordSRV.logTransaction(player, amount, mode.getName(), shopItem.getDisplayname(), plugin.formatPrice(shopItem.getEcoType(), price)));
        }
        if (onScreenTitle && (type == Transaction.Type.SELL_ALL_COMMAND || type == Transaction.Type.SELL_GUI_SCREEN || shopItem.isCloseMenu())) {
            SendMessage.sendTitle(player, mode, Collections.singletonMap(shopItem.getEcoType(), price));
        }
        if (advancedTransactionStore) {
            plugin.getTransactionLog().log(player.getUniqueId(), shopItem, Collections.singletonMap(shopItem.getEcoType(), price), amount, mode, type);
        }
    }

    public static void sendTransactionMessage(Player player, int amount, Map<EcoType, Double> prices, Map<ShopItem, Integer> items, Transaction.Type type) {
        plugin.getMetaUtils().sendTransactionMessage(player, amount, prices, items, type);
        Translatable message = Lang.PLAYER_MADE_TRANSACTION_MULTIPLE_ITEMS.get();
        String resetColor = ChatUtil.getLastColors(message.toString().split("%items%")[0]);
        if (resetColor.isEmpty()) {
            resetColor = "\u00a7f";
        }
        String finalResetColor = resetColor;
        SendMessage.logPlayerTransaction(message.replace("%playername%", player.getName()).replace("%player_displayname%", plugin.getDisplayName(player)).replace("%bought/sold%", Lang.SOLD.get().toString()).replace("%items%", SendMessage.getItemDisplay(resetColor, items, true)).replace("%amountofmoney%", SendMessage.getCurrencyDisplay(resetColor, prices, true)).replace("%buy/sell-method%", type.getName()));
        if (discordSRV != null && SendMessage.discordSRV.enabled) {
            plugin.runTaskAsync(() -> discordSRV.logTransaction(player, amount, Lang.SOLD.get().getLegacy(), SendMessage.getItemDisplay(finalResetColor, items, false), SendMessage.getCurrencyDisplay(finalResetColor, prices, false)));
        }
        if (onScreenTitle) {
            SendMessage.sendTitle(player, prices);
        }
        if (advancedTransactionStore) {
            plugin.getTransactionLog().log(player.getUniqueId(), items, prices, amount, Transaction.Mode.SELL, type);
        }
    }

    private static String getItemDisplay(String resetColor, Map<ShopItem, Integer> items, boolean logs) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Map.Entry<ShopItem, Integer> entry : items.entrySet()) {
            if (entry.getKey() instanceof ShopItem.Shulker) {
                Map<ShopItem, Integer> contents = ((ShopItem.Shulker)entry.getKey()).getContents();
                sb.append(entry.getValue()).append("x ").append(entry.getKey().getDisplayname());
                sb.append(contents.entrySet().stream().map(e -> e.getValue() + "x " + ((ShopItem)e.getKey()).getDisplayname()).collect(Collectors.toList()));
            } else {
                sb.append(entry.getValue()).append("x ").append(entry.getKey().getDisplayname());
            }
            if (logs) {
                if (resetColor != null) {
                    sb.append(resetColor);
                }
                sb.append("(").append(entry.getKey().getItemPath()).append(")");
            }
            if (i != items.size() - 1) {
                sb.append(", ");
            }
            ++i;
        }
        return sb.toString();
    }

    private static String getCurrencyDisplay(String resetColor, Map<EcoType, Double> prices, boolean logs) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Map.Entry<EcoType, Double> entry : prices.entrySet()) {
            sb.append(plugin.formatPrice(entry.getKey(), entry.getValue()));
            if (logs) {
                sb.append(resetColor);
            }
            if (i != prices.size() - 1) {
                sb.append(", ");
            }
            ++i;
        }
        return sb.toString();
    }

    private static void sendTitle(Player player, Map<EcoType, Double> prices) {
        SendMessage.sendTitle(player, null, prices);
    }

    private static void sendTitle(Player player, Transaction.Mode mode, Map<EcoType, Double> prices) {
        String message = mode == null || mode == Transaction.Mode.SELL ? Lang.ON_SCREEN_TITLE_SOLD.get().getLegacy() : Lang.ON_SCREEN_TITLE_BOUGHT.get().getLegacy();
        String resetColor = ChatColor.getLastColors((String)message.split("%price%")[0]);
        if (resetColor.isEmpty()) {
            resetColor = "\u00a7f";
        }
        player.sendTitle(message.replace("%price%", SendMessage.getCurrencyDisplay(resetColor, prices, true)), "");
    }

    public static void sendMessage(Object logger, Translatable message) {
        plugin.getMetaUtils().send(logger, message);
    }

    public static void sendMessage(Object logger, String msg) {
        plugin.getMetaUtils().sendLegacy(logger, msg);
    }

    public static void chatToPlayer(Player p, Translatable message) {
        plugin.getMetaUtils().send(p, message);
    }

    public static void chatToPlayer(Player p, String message) {
        plugin.getMetaUtils().sendLegacy(p, message);
    }

    public static void chatToPlayer(Player p, TextComponent message) {
        TextComponent text = new TextComponent(TextComponent.fromLegacyText((String)Lang.SHOP_PREFIX.get().getLegacy()));
        text.addExtra((BaseComponent)message);
        p.spigot().sendMessage((BaseComponent)text);
    }

    public static void broadcastMessage(Translatable msg) {
        plugin.getMetaUtils().broadcast(msg);
    }

    public static void logDebugMessage(String msg) {
        if (ConfigManager.getConfig().getBoolean("debug")) {
            plugin.getMetaUtils().logLegacyDebugMessage(msg);
        }
    }

    public static void logDebugMessage(Translatable message) {
        if (ConfigManager.getConfig().getBoolean("debug")) {
            plugin.getMetaUtils().logDebugMessage(message);
        }
    }

    public static void logPlayerTransaction(Translatable message) {
        if (logPlayerTransactions) {
            plugin.getMetaUtils().logPlayerTransaction(message);
        }
        if (logTransactions) {
            plugin.getTransactionLog().log(ChatUtil.stripColor(message.toString()));
        }
    }

    public static void infoMessage(String msg) {
        plugin.getMetaUtils().infoLegacyMessage(msg);
    }

    public static void infoMessage(Translatable message) {
        plugin.getMetaUtils().infoMessage(message);
    }

    public static void infoMessage(Object logger, String msg) {
        plugin.getMetaUtils().infoLegacyMessage(logger, msg);
    }

    public static void infoMessage(Object logger, Translatable message) {
        plugin.getMetaUtils().infoMessage(logger, message);
    }

    public static void warnMessage(String msg) {
        plugin.getMetaUtils().warnLegacyMessage(msg);
    }

    public static void warnMessage(Translatable message) {
        plugin.getMetaUtils().warnMessage(message);
    }

    public static void warnMessage(Object logger, String msg) {
        plugin.getMetaUtils().warnLegacyMessage(logger, msg);
    }

    public static void warnMessage(Object logger, Translatable message) {
        plugin.getMetaUtils().warnMessage(logger, message);
    }

    public static void errorMessage(String message) {
        plugin.getMetaUtils().errorLegacyMessage(message);
    }

    public static void errorMessage(Translatable message) {
        plugin.getMetaUtils().errorMessage(message);
    }

    public static void errorMessage(Object logger, String msg) {
        plugin.getMetaUtils().errorLegacyMessage(logger, msg);
    }

    public static void errorMessage(Object logger, Translatable message) {
        plugin.getMetaUtils().errorMessage(logger, message);
    }

    public static void errorItemShops(String itemPath) {
        SendMessage.warnMessage(Lang.ITEMS_PATH_IN_SHOPS_CONFIG.get().replace("%location%", itemPath));
    }

    public static void errorShops(String section, String path) {
        SendMessage.warnMessage(Lang.ITEMS_LOCATION_IN_SHOPS.get().replace("%section%", section).replace("%location%", path));
    }

    public static void errorItemConfig(String path) {
        SendMessage.warnMessage(Lang.ITEMS_PATH_IN_CONFIG.get().replace("%location%", path));
    }

    public static void errorItemSections(String itemPath) {
        SendMessage.warnMessage(Lang.ITEMS_PATH_IN_SECTIONS_CONFIG.get().replace("%location%", itemPath));
    }

    public static void errorSections(String section, String path) {
        SendMessage.warnMessage(Lang.ITEMS_LOCATION_IN_SECTIONS.get().replace("%section%", section).replace("%location%", path));
    }
}

