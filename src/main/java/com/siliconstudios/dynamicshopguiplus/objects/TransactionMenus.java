

package com.siliconstudios.dynamicshopguiplus.objects;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.methodes.CreateItem;
import com.siliconstudios.dynamicshopguiplus.objects.TransactionItem;
import com.siliconstudios.dynamicshopguiplus.objects.TransactionScreen;
import com.siliconstudios.dynamicshopguiplus.util.Transaction;

public class TransactionMenus {
    private static TransactionScreen buyScreen;
    private static TransactionScreen sellScreen;
    private static TransactionScreen buyStacksScreen;
    private static TransactionScreen shopStandBuy;
    private static TransactionScreen shopStandSell;

    public static void load(DynamicShopGUIPlus plugin) {
        buyScreen = plugin.createItem.getTransactionScreen(Transaction.Type.BUY_SCREEN);
        sellScreen = plugin.createItem.getTransactionScreen(Transaction.Type.SELL_SCREEN);
        buyStacksScreen = plugin.createItem.getTransactionScreen(Transaction.Type.BUY_STACKS_SCREEN);
        if (plugin.shopStands) {
            shopStandBuy = plugin.createItem.getTransactionScreen(Transaction.Type.SHOPSTAND_BUY_SCREEN);
            shopStandSell = plugin.createItem.getTransactionScreen(Transaction.Type.SHOPSTAND_SELL_SCREEN);
        }
    }

    public static List<TransactionItem> getItems(Transaction.Type type) {
        switch (type) {
            case BUY_STACKS_SCREEN: {
                return buyStacksScreen.getItems();
            }
            case BUY_SCREEN: {
                return buyScreen.getItems();
            }
            case SELL_SCREEN: {
                return sellScreen.getItems();
            }
            case SHOPSTAND_BUY_SCREEN: {
                return shopStandBuy.getItems();
            }
            case SHOPSTAND_SELL_SCREEN: {
                return shopStandSell.getItems();
            }
        }
        return null;
    }

    public static CreateItem.TransactionItemAction getActionFromSlot(Transaction.Type type, int slot) {
        try {
            TransactionItem item = TransactionMenus.getItems(type).stream().filter(i -> i.getSlots().contains(slot) && i.getAction() != CreateItem.TransactionItemAction.NONE).findFirst().orElse(null);
            if (item != null) {
                return item.getAction();
            }
        }
        catch (NullPointerException nullPointerException) {
            
        }
        return CreateItem.TransactionItemAction.NONE;
    }

    public static TransactionItem getItemFromSlot(Transaction.Type type, int slot) {
        return TransactionMenus.getItems(type).stream().filter(item -> item.getSlots().contains(slot)).findFirst().orElse(null);
    }

    public static TransactionItem getItemByType(Transaction.Type type, CreateItem.TransactionItemType itemType) {
        return TransactionMenus.getItems(type).stream().filter(i -> i.getType() == itemType).findFirst().orElse(null);
    }

    public static List<TransactionItem> getItemsByType(Transaction.Type type, CreateItem.TransactionItemType itemType) {
        return TransactionMenus.getItems(type).stream().filter(i -> i.getType() == itemType).collect(Collectors.toList());
    }

    public static List<TransactionItem> getItemsByAction(Transaction.Type type, CreateItem.TransactionItemAction ... actions) {
        return TransactionMenus.getItems(type).stream().filter(i -> Arrays.stream(actions).anyMatch(a -> i.getAction() == a)).collect(Collectors.toList());
    }

    public static int getSize(Transaction.Type type) {
        switch (type) {
            case BUY_STACKS_SCREEN: {
                return buyStacksScreen.getSize();
            }
            case BUY_SCREEN: {
                return buyScreen.getSize();
            }
            case SELL_SCREEN: {
                return sellScreen.getSize();
            }
            case SHOPSTAND_BUY_SCREEN: {
                return shopStandBuy.getSize();
            }
            case SHOPSTAND_SELL_SCREEN: {
                return shopStandSell.getSize();
            }
        }
        return 0;
    }
}

