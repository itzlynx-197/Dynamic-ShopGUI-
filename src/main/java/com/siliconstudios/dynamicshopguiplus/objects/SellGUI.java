

package com.siliconstudios.dynamicshopguiplus.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.api.events.PostTransactionEvent;
import com.siliconstudios.dynamicshopguiplus.api.events.PreTransactionEvent;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.objects.ShopInventory;
import com.siliconstudios.dynamicshopguiplus.objects.ShopItem;
import com.siliconstudios.dynamicshopguiplus.util.EcoType;
import com.siliconstudios.dynamicshopguiplus.util.LocalizedItemStack;
import com.siliconstudios.dynamicshopguiplus.util.SmartStack;
import com.siliconstudios.dynamicshopguiplus.util.Sold;
import com.siliconstudios.dynamicshopguiplus.util.Transaction;
import com.siliconstudios.dynamicshopguiplus.util.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class SellGUI
extends ShopInventory {
    private Inventory inv;
    private static final Map<Integer, LocalizedItemStack> staticItems = new HashMap<Integer, LocalizedItemStack>();
    private static final int size = Math.max(1, ConfigManager.getConfig().getInt("sellgui-screen.gui-rows", 6)) * 9;

    public void open(Player p) {
        this.inv = DynamicShopGUIPlus.getInstance().getMetaUtils().createInventory(this, size, Lang.INVENTORY_SELLGUI_TITLE.get(p));
        staticItems.forEach((s, i) -> this.inv.setItem(s.intValue(), i.getItem(p)));
        DynamicShopGUIPlus.getInstance().navBar.addSellGUINavBar(this.inv, p);
        p.openInventory(this.inv);
    }

    public void sellItems(Player player) {
        Transaction.Result result = Transaction.Result.SUCCESS;
        HashMap<Integer, SmartStack> toRemove = new HashMap<Integer, SmartStack>();
        HashMap<ShopItem, Sold> CACHE = new HashMap<ShopItem, Sold>();
        int amount = 0;
        for (int i = 0; i <= size - (DynamicShopGUIPlus.getInstance().navBar.isEnableSellGUINav() ? 10 : 1); ++i) {
            Integer sold;
            ItemStack item;
            if (staticItems.containsKey(i) || (item = this.inv.getItem(i)) == null || item.getType() == Material.AIR) continue;
            ShopItem shopItem = DynamicShopGUIPlus.getInstance().createItem.matchShopItem(item, player, "sellgui");
            if (shopItem != null && shopItem.getSellPrice() >= 0.0) {
                int qty = item.getAmount();
                if (shopItem.isMaxSell(CACHE.getOrDefault(shopItem, new Sold()).getAmount() + qty)) {
                    if (CACHE.getOrDefault(shopItem, new Sold()).getAmount() >= shopItem.getMaxSell()) continue;
                    qty = shopItem.getMaxSell() - CACHE.getOrDefault(shopItem, new Sold()).getAmount();
                }
                Sold sold2 = CACHE.computeIfAbsent(shopItem, k -> new Sold());
                sold2.addValues(qty, shopItem.getSellPrice(player, item, qty));
                amount += qty;
                ItemStack itemStack = new ItemStack(item);
                itemStack.setAmount(item.getAmount() - qty);
                toRemove.put(i, new SmartStack(itemStack));
                sold2.addIndex(i);
                continue;
            }
            if (!DynamicShopGUIPlus.getInstance().sellShulkers || !XMaterial.isShulker(item.getType()) || (sold = this.sellShulker(CACHE, toRemove, player, item, "sellgui", i)) == null) continue;
            amount += sold.intValue();
        }
        for (Map.Entry<?, ?> sold : new HashSet<>(CACHE.entrySet())) {
            if (!((ShopItem)sold.getKey()).isMinSell(((Sold)sold.getValue()).getAmount())) continue;
            CACHE.remove(sold.getKey());
            amount -= ((Sold)sold.getValue()).getAmount();
            ((Sold)sold.getValue()).getIndexes().forEach(toRemove::remove);
        }
        Map<EcoType, Double> prices = new HashMap<EcoType, Double>();
        HashMap<ShopItem, Integer> items = new HashMap<ShopItem, Integer>();
        if (amount > 0) {
            prices = this.callPreTransactionEvent(this.getItems(items, CACHE), this.getPrices(prices, CACHE), player, amount);
            if (prices != null) {
                this.removeItems(this.inv, toRemove);
                prices.forEach((type, price) -> DynamicShopGUIPlus.getInstance().getEcoHandler().getEcon((EcoType)type).depositBalance((OfflinePlayer)player, (double)price));
                SendMessage.sendTransactionMessage(player, amount, prices, items, Transaction.Type.SELL_GUI_SCREEN);
            } else {
                result = Transaction.Result.TRANSACTION_CANCELLED;
            }
        } else {
            result = Transaction.Result.NO_ITEMS_FOUND;
            SendMessage.chatToPlayer(player, Lang.NO_ITEM_FOUND.get());
        }
        this.addUnsoldItems(player);
        Bukkit.getPluginManager().callEvent((Event)new PostTransactionEvent(items, prices, player, amount, Transaction.Type.SELL_ALL_COMMAND, result));
    }

    private void addUnsoldItems(Player p) {
        for (int i = 0; i <= size - (DynamicShopGUIPlus.getInstance().navBar.isEnableSellGUINav() ? 10 : 1); ++i) {
            ItemStack item;
            if (staticItems.containsKey(i) || (item = this.inv.getItem(i)) == null || item.getType() == Material.AIR) continue;
            for (ItemStack stack : p.getInventory().addItem(new ItemStack[]{item}).values()) {
                p.getWorld().dropItem(p.getLocation(), stack);
            }
        }
    }

    private Integer sellShulker(Map<ShopItem, Sold> CACHE, Map<Integer, SmartStack> toRemove, Player p, ItemStack box, String perm, int slot) {
        BlockStateMeta bm;
        HashMap<ShopItem, Sold> bCACHE = new HashMap<ShopItem, Sold>();
        HashMap<Integer, ItemStack> items = new HashMap<Integer, ItemStack>();
        int removed = 0;
        if (box.getItemMeta() instanceof BlockStateMeta && (bm = (BlockStateMeta)box.getItemMeta()).getBlockState() instanceof ShulkerBox) {
            Inventory inv = ((ShulkerBox)bm.getBlockState()).getInventory();
            if (inv.isEmpty()) {
                return null;
            }
            for (int i = 0; i < 27; ++i) {
                ShopItem shopItem;
                ItemStack is = inv.getItem(i);
                if (is == null || is.getType().equals((Object)Material.AIR) || (shopItem = DynamicShopGUIPlus.getInstance().createItem.matchShopItem(is, p, perm)) == null || !(shopItem.getSellPrice() >= 0.0)) continue;
                int qty = is.getAmount();
                if (shopItem.isMaxSell(bCACHE.getOrDefault(shopItem, new Sold()).getAmount() + CACHE.getOrDefault(shopItem, new Sold()).getAmount() + qty)) {
                    if (bCACHE.getOrDefault(shopItem, new Sold()).getAmount() + CACHE.getOrDefault(shopItem, new Sold()).getAmount() >= shopItem.getMaxSell()) continue;
                    qty = shopItem.getMaxSell() - (bCACHE.getOrDefault(shopItem, new Sold()).getAmount() + CACHE.getOrDefault(shopItem, new Sold()).getAmount());
                }
                Sold sold2 = bCACHE.computeIfAbsent(shopItem, k -> new Sold());
                sold2.addValues(qty, shopItem.getSellPrice(p, is, qty));
                removed += qty;
                ItemStack stack = new ItemStack(is);
                stack.setAmount(is.getAmount() - qty);
                items.put(i, stack);
                sold2.addIndex(i);
            }
            if (!bCACHE.isEmpty()) {
                toRemove.put(slot, new SmartStack.Shulker(box, items));
                bCACHE.forEach((item, sold) -> CACHE.merge((ShopItem)item, (Sold)sold, Sold::addValue));
                return removed;
            }
        }
        return null;
    }

    private void removeItems(Inventory inv, Map<Integer, SmartStack> items) {
        items.forEach((i, stack) -> {
            if (stack instanceof SmartStack.Shulker) {
                this.emptyShulker((SmartStack.Shulker)stack);
            } else if (stack.getItem().getAmount() != 0) {
                inv.setItem(i.intValue(), stack.getItem());
            } else {
                inv.clear(i.intValue());
            }
        });
    }

    private void emptyShulker(SmartStack.Shulker shulker) {
        ItemStack sBox = shulker.getItem();
        BlockStateMeta sbm = (BlockStateMeta)sBox.getItemMeta();
        ShulkerBox s = (ShulkerBox)sbm.getBlockState();
        shulker.getItems().forEach((i, item) -> {
            if (item.getAmount() != 0) {
                s.getInventory().setItem(i.intValue(), item);
            } else {
                s.getInventory().clear(i.intValue());
            }
        });
        sbm.setBlockState((BlockState)s);
        sBox.setItemMeta((ItemMeta)sbm);
    }

    private Map<EcoType, Double> getPrices(Map<EcoType, Double> prices, Map<ShopItem, Sold> CACHE) {
        for (Map.Entry<ShopItem, Sold> entry : CACHE.entrySet()) {
            prices.merge(entry.getKey().getEcoType(), entry.getValue().getPrice(), Double::sum);
        }
        return prices;
    }

    private Map<ShopItem, Integer> getItems(Map<ShopItem, Integer> items, Map<ShopItem, Sold> CACHE) {
        for (Map.Entry<ShopItem, Sold> entry : CACHE.entrySet()) {
            items.merge(entry.getKey(), entry.getValue().getAmount(), Integer::sum);
        }
        return items;
    }

    private Map<EcoType, Double> callPreTransactionEvent(Map<ShopItem, Integer> items, Map<EcoType, Double> prices, Player player, int amount) {
        PreTransactionEvent preTransactionEvent = new PreTransactionEvent(items, prices, player, amount, Transaction.Type.SELL_GUI_SCREEN);
        Bukkit.getPluginManager().callEvent((Event)preTransactionEvent);
        return preTransactionEvent.isCancelled() ? null : preTransactionEvent.getPrices();
    }

    @Override
    public Inventory getInventory() {
        return this.inv;
    }

    private static void loadItems() {
        for (String item : ConfigManager.getConfig().getConfigurationSection("sellgui-screen.items").getKeys(false)) {
            ArrayList<Integer> slots = DynamicShopGUIPlus.getInstance().calculateAmount.getSlots(ConfigManager.getConfig().getString("sellgui-screen.items." + item + ".slot", null));
            if (slots == null) {
                SendMessage.warnMessage("Invalid slot for item, skipping...");
                SendMessage.errorItemConfig("sellgui-screen.items." + item);
                continue;
            }
            LocalizedItemStack stack = DynamicShopGUIPlus.getInstance().createItem.createFillItem(null, ConfigManager.getConfig().getConfigurationSection("sellgui-screen.items." + item));
            for (int i = slots.size() - 1; i > -1; --i) {
                int slot = (Integer)slots.get(i);
                if (slot >= (DynamicShopGUIPlus.getInstance().navBar.isEnableSellGUINav() ? size - 9 : size) || slot < 0) {
                    SendMessage.warnMessage("Item slot " + slot + " is out of bounds for menu size of " + size + ", skipping ...");
                    SendMessage.errorItemConfig("sellgui-screen.items." + item);
                    continue;
                }
                staticItems.put(slot, stack);
            }
        }
    }

    public static boolean isFillItem(int slot) {
        return staticItems.containsKey(slot);
    }

    static {
        if (ConfigManager.getConfig().contains("sellgui-screen.items")) {
            SellGUI.loadItems();
        }
    }
}

