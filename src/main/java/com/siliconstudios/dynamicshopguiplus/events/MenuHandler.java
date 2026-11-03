

package com.siliconstudios.dynamicshopguiplus.events;

import com.google.common.cache.CacheBuilder;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.api.events.PostTransactionEvent;
import com.siliconstudios.dynamicshopguiplus.api.events.PreTransactionEvent;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.objects.ActionItem;
import com.siliconstudios.dynamicshopguiplus.objects.DisplayItem;
import com.siliconstudios.dynamicshopguiplus.objects.MainMenu;
import com.siliconstudios.dynamicshopguiplus.objects.SellGUI;
import com.siliconstudios.dynamicshopguiplus.objects.ShopInventory;
import com.siliconstudios.dynamicshopguiplus.objects.ShopItem;
import com.siliconstudios.dynamicshopguiplus.objects.ShopPage;
import com.siliconstudios.dynamicshopguiplus.objects.TransactionItem;
import com.siliconstudios.dynamicshopguiplus.objects.TransactionMenu;
import com.siliconstudios.dynamicshopguiplus.objects.TransactionMenus;
import com.siliconstudios.dynamicshopguiplus.objects.mappings.ClickAction;
import com.siliconstudios.dynamicshopguiplus.objects.shops.ShopSection;
import com.siliconstudios.dynamicshopguiplus.providers.UserManager;
import com.siliconstudios.dynamicshopguiplus.util.BackButton;
import com.siliconstudios.dynamicshopguiplus.util.InventoryUtil;
import com.siliconstudios.dynamicshopguiplus.util.PermissionsCache;
import com.siliconstudios.dynamicshopguiplus.util.Transaction;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class MenuHandler
implements Listener {
    private final DynamicShopGUIPlus plugin;
    private final ConcurrentMap<UUID, Boolean> cooldown;
    private final int cooldownMillis;
    public static final boolean backOnClose = ConfigManager.getConfig().getBoolean("escape-back");

    public MenuHandler(DynamicShopGUIPlus plugin) {
        this.plugin = plugin;
        this.cooldownMillis = Math.max(0, ConfigManager.getConfig().getInt("click-cooldown", 125));
        this.cooldown = new java.util.concurrent.ConcurrentHashMap<>();
    }

    
    @EventHandler(priority=EventPriority.HIGH)
    public void onMenuClick(InventoryClickEvent e) {
        int slot = e.getRawSlot();
        Inventory inventory = e.getInventory();
        InventoryHolder holder = InventoryUtil.getHolder(inventory);
        if (!(holder instanceof ShopInventory)) return;
        if (slot >= inventory.getSize()) {
            if (e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT) {
                if (holder instanceof SellGUI) {
                    this.handleShiftSellGUI(e, inventory, holder, slot);
                    return;
                } else {
                    e.setCancelled(true);
                }
                return;
            } else {
                if (!e.getAction().equals((Object)InventoryAction.COLLECT_TO_CURSOR)) return;
                e.setCancelled(true);
            }
            return;
        }
        e.setCancelled(true);
        if (e.getClick() == ClickType.DOUBLE_CLICK) {
            return;
        }
        Player player = (Player)e.getWhoClicked();
        if (this.cooldown.putIfAbsent(player.getUniqueId(), Boolean.TRUE) != null) {
            return;
        }
        if (this.cooldownMillis > 0) {
            this.plugin.runTaskLater(() -> this.cooldown.remove(player.getUniqueId()), Math.max(1, this.cooldownMillis / 50));
        } else {
            this.cooldown.remove(player.getUniqueId());
        }
        if (!(e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR || holder instanceof SellGUI)) {
            return;
        }
        if (holder instanceof ShopPage) {
            ShopPage page = (ShopPage)holder;
            if (this.plugin.navBar.isEnabled(page.getSection()) && slot >= inventory.getSize() - 9 && slot <= inventory.getSize() - 1) {
                this.plugin.navBar.execute(player, page, slot, page.getPage(), page.isDisabledBackButton());
                return;
            } else {
                DisplayItem displayItem = this.plugin.getDisplayItem(page.getSection(), page.getItem(slot));
                if (displayItem == null) {
                    return;
                }
                ClickAction action = this.plugin.getSection(page.getSection()).getClickAction(e.getClick());
                if (action == null) {
                    return;
                }
                if (!displayItem.hasItemError()) {
                    if (!this.isAllowedGamemode(player)) {
                        return;
                    }
                    if (!displayItem.meetsRequirements(player, false)) return;
                    if (displayItem instanceof ActionItem) {
                        this.plugin.navBar.execute(player, page, ((ActionItem)displayItem).getAction(), page.getPage(), page.isDisabledBackButton());
                        return;
                    }
                    ShopItem shopItem = (ShopItem)displayItem;
                    if (shopItem.isLinked()) {
                        if (PermissionsCache.hasPermission(player, "DynamicShopGUIPlus.shop." + shopItem.getSubSection().toLowerCase())) {
                            this.plugin.getSection(shopItem.getSubSection()).openShopSection(player, page.isDisabledBackButton(), shopItem.section);
                            return;
                        }
                        SendMessage.chatToPlayer(player, Lang.NO_PERMISSIONS_TO_OPEN_SHOP.get());
                    }
                    if (shopItem.isDisplayItem()) {
                        return;
                    }
                    if (action == ClickAction.BUY) {
                        if (!(shopItem.getBuyPrice() >= 0.0)) return;
                        if (shopItem.getMaxBuy() == -1) {
                            this.purchaseItem(player, shopItem);
                            inventory.setItem(e.getSlot(), this.plugin.getSection(shopItem.section).updateItem(player, shopItem));
                            return;
                        } else {
                            new TransactionMenu(player, shopItem, page.getRootSection(), page.isDisabledBackButton(), Transaction.Mode.BUY, Transaction.Type.BUY_SCREEN, 1).open();
                        }
                        return;
                    } else if (action == ClickAction.SELL_ALL) {
                        if (shopItem.getSellPrice() < 0.0 || !this.plugin.MMB) {
                            return;
                        }
                        this.sellItem(player, shopItem, Integer.MAX_VALUE);
                        return;
                    } else {
                        if (action != ClickAction.SELL || !(shopItem.getSellPrice() >= 0.0)) return;
                        if (shopItem.getMaxSell() == -1) {
                            this.sellItem(player, shopItem, shopItem.getStackSize());
                            inventory.setItem(e.getSlot(), this.plugin.getSection(shopItem.section).updateItem(player, shopItem));
                            return;
                        } else {
                            new TransactionMenu(player, shopItem, page.getRootSection(), page.isDisabledBackButton(), Transaction.Mode.SELL, Transaction.Type.SELL_SCREEN, 1).open();
                        }
                    }
                    return;
                } else {
                    SendMessage.chatToPlayer(player, Lang.ITEM_ERROR.get());
                }
            }
            return;
        } else if (holder instanceof TransactionMenu) {
            TransactionItem item;
            TransactionMenu menu = (TransactionMenu)holder;
            if (slot >= inventory.getSize() - 9 && slot <= inventory.getSize() - 1) {
                switch (menu.getTransactionType()) {
                    case SHOPSTAND_BUY_SCREEN: 
                    case SHOPSTAND_SELL_SCREEN: {
                        if (!this.plugin.navBar.isEnableShopStandsNav()) break;
                        this.plugin.navBar.execute(player, menu.getShopItem(), slot - (inventory.getSize() - 9), menu.getTransactionMode(), menu.getAmount());
                        break;
                    }
                    default: {
                        if (!this.plugin.navBar.isEnableTransactionNav()) break;
                        this.plugin.navBar.execute(player, menu.getShopItem(), slot - (inventory.getSize() - 9), menu.getRootSection(), menu.isDisabledBackButton(), menu.getTransactionMode(), menu.getTransactionType(), menu.getAmount());
                    }
                }
            }
            if ((item = TransactionMenus.getItemFromSlot(menu.getTransactionType(), slot)) == null) return;
            switch (item.getAction().type) {
                case NONE: {
                    return;
                }
                case BACK: {
                    if (menu.getTransactionType() != Transaction.Type.BUY_STACKS_SCREEN) {
                        ShopSection section = this.plugin.getSection(menu.getShopItem().section);
                        section.openShopSection(player, section.getPageForShopItem(menu.getShopItem().itemLoc), menu.isDisabledBackButton(), menu.getRootSection());
                        return;
                    }
                    new TransactionMenu(player, menu.getShopItem(), menu.getRootSection(), menu.isDisabledBackButton(), menu.getTransactionMode(), Transaction.Type.BUY_SCREEN, menu.getAmount()).open();
                    return;
                }
                case INSTA_BUY: 
                case INSTA_SELL: {
                    menu.setAmount(item.getItem().getAmount());
                }
                case CONFIRM_TRANSACTION: {
                    this.completeTransaction(menu, player);
                    return;
                }
                case OPEN_BUY_STACKS: {
                    if (menu.getShopItem().isMaxBuy(menu.getSelectedItem().getMaxStackSize())) return;
                    new TransactionMenu(player, menu.getShopItem(), menu.getRootSection(), menu.isDisabledBackButton(), Transaction.Mode.BUY, Transaction.Type.BUY_STACKS_SCREEN, menu.getAmount()).open();
                    return;
                }
                case SELL_ALL: {
                    this.completeSellAll(menu, player);
                    return;
                }
                default: {
                    menu.updateAmount(item.getAction());
                }
            }
            return;
        } else if (holder instanceof MainMenu) {
            if (this.plugin.navBar.isEnableMainNav() && slot >= inventory.getSize() - 9 && slot <= inventory.getSize() - 1) {
                this.plugin.navBar.execute(holder, player, slot - (inventory.getSize() - 9));
                return;
            } else {
                String section = this.plugin.getMainMenuSectionForSlot(slot);
                if (section == null || !this.plugin.getSections().containsKey(section)) return;
                if (PermissionsCache.hasPermission(player, "DynamicShopGUIPlus.shop." + section.toLowerCase())) {
                    this.plugin.getSection(section).openShopSection(player, false);
                    return;
                } else {
                    if (this.plugin.getSectionItem(section).isHideIfNoPerms()) return;
                    SendMessage.chatToPlayer(player, Lang.NO_PERMISSIONS_TO_OPEN_SHOP.get());
                }
            }
            return;
        } else {
            if (!(holder instanceof SellGUI)) return;
            if (this.plugin.navBar.isEnableSellGUINav() && slot >= inventory.getSize() - 9 && slot <= inventory.getSize() - 1) {
                this.plugin.navBar.execute(holder, player, slot - (inventory.getSize() - 9));
                return;
            } else {
                if (SellGUI.isFillItem(slot)) return;
                e.setCancelled(false);
            }
        }
    }

    @EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
    public void onMenuClose(InventoryCloseEvent e) {
        if (InventoryUtil.getHolder(e.getInventory()) instanceof ShopInventory) {
            Player player = (Player)e.getPlayer();
            InventoryHolder holder = InventoryUtil.getHolder(e.getInventory());
            boolean goBack = backOnClose && !UserManager.getUser(player).isOpenNewGUI();
            this.plugin.runTaskLater(() -> this.closeInventory(player, holder, goBack), 1L);
        }
    }

    private void closeInventory(Player player, InventoryHolder holder, boolean goBack) {
        if (goBack && holder instanceof ShopPage) {
            ShopPage page = (ShopPage)holder;
            if (!page.isDisabledBackButton() || page.isSubSection() && !BackButton.SUB_SECTIONS) {
                if (page.isSubSection()) {
                    this.plugin.getSection(page.getRootSection()).openShopSection(player, page.isDisabledBackButton());
                } else {
                    MainMenu.open(player);
                }
            }
        } else if (goBack && holder instanceof TransactionMenu) {
            TransactionMenu menu = (TransactionMenu)holder;
            if (menu.getTransactionType() != Transaction.Type.BUY_STACKS_SCREEN) {
                ShopSection section = this.plugin.getSection(menu.getShopItem().section);
                section.openShopSection(player, section.getPageForShopItem(menu.getShopItem().itemLoc), menu.isDisabledBackButton());
            } else {
                new TransactionMenu(player, menu.getShopItem(), menu.getRootSection(), menu.isDisabledBackButton(), Transaction.Mode.getFromType(menu.getTransactionType()), Transaction.Type.BUY_SCREEN, menu.getAmount()).open();
            }
        } else if (holder instanceof SellGUI) {
            ((SellGUI)holder).sellItems(player);
        }
        player.updateInventory();
    }

    private void close(Player player) {
        UserManager.getUser(player).setOpenNewGUI(true);
        player.closeInventory();
    }

    private void handleShiftSellGUI(InventoryClickEvent e, Inventory inv, InventoryHolder holder, int slot) {
        int i;
        ItemStack source = e.getCurrentItem();
        if (source == null) {
            return;
        }
        ItemStack toSimulate = source.clone();
        ArrayList<Integer> affectedSlots = new ArrayList<Integer>();
        ItemStack[] destContents = holder.getInventory().getContents();
        for (i = 0; i < destContents.length; ++i) {
            int max;
            int space;
            ItemStack itemStack = destContents[i];
            if (itemStack == null || !itemStack.isSimilar(toSimulate) || (space = (max = Math.min(itemStack.getMaxStackSize(), toSimulate.getMaxStackSize())) - itemStack.getAmount()) <= 0) continue;
            int moved = Math.min(space, toSimulate.getAmount());
            toSimulate.setAmount(toSimulate.getAmount() - moved);
            affectedSlots.add(i);
            if (toSimulate.getAmount() <= 0) break;
        }
        if (toSimulate.getAmount() > 0) {
            for (i = 0; i < destContents.length; ++i) {
                if (destContents[i] != null) continue;
                int moved = Math.min(toSimulate.getAmount(), toSimulate.getMaxStackSize());
                toSimulate.setAmount(toSimulate.getAmount() - moved);
                affectedSlots.add(i);
                if (toSimulate.getAmount() <= 0) break;
            }
        }
        Iterator iterator = affectedSlots.iterator();
        while (iterator.hasNext()) {
            int slotIndex = (Integer)iterator.next();
            if (this.plugin.navBar.isEnableSellGUINav() && slotIndex >= inv.getSize() - 9 && slotIndex <= inv.getSize() - 1) {
                e.setCancelled(true);
                return;
            }
            if (!SellGUI.isFillItem(slotIndex)) continue;
            e.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
    public void onDrag(InventoryDragEvent e) {
        InventoryHolder holder = InventoryUtil.getHolder(e.getInventory());
        if (!(holder instanceof ShopInventory)) {
            return;
        }
        int topSize = holder.getInventory().getSize();
        Iterator iterator = e.getRawSlots().iterator();
        while (iterator.hasNext()) {
            int rawSlot = (Integer)iterator.next();
            if (rawSlot >= topSize || holder instanceof SellGUI && !SellGUI.isFillItem(rawSlot) && (!this.plugin.navBar.isEnableSellGUINav() || rawSlot < topSize - 9 || rawSlot > topSize - 1)) continue;
            e.setCancelled(true);
            e.setResult(Event.Result.DENY);
            return;
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onCreative(InventoryCreativeEvent e) {
        InventoryHolder holder = InventoryUtil.getHolder(e.getInventory());
        if (!(holder instanceof ShopInventory)) {
            return;
        }
        e.setCancelled(true);
        e.setResult(Event.Result.DENY);
    }

    public boolean isAllowedGamemode(Player player) {
        if (this.plugin.bannedGamemodes.contains(player.getGameMode()) && !PermissionsCache.hasPermission(player, "DynamicShopGUIPlus.bypassgamemode")) {
            SendMessage.chatToPlayer(player, Lang.CANNOT_ENTER_SHOP_BANNED_GAMEMODE.get().replace("%gamemode%", player.getGameMode().name().toLowerCase()));
            return false;
        }
        return true;
    }

    public void completeTransaction(TransactionMenu menu, Player player) {
        Transaction.Result result = Transaction.Result.SUCCESS;
        double price = 0.0;
        int amount = 0;
        if (menu.getTransactionMode() != Transaction.Mode.SELL) {
            if (menu.getBasePrice() >= 0.0) {
                int freeSpace = menu.getFreeSpace();
                if (freeSpace != 0) {
                    amount = menu.getAmountToSell(freeSpace);
                    PreTransactionEvent preTransactionEvent = new PreTransactionEvent(menu.getShopItem(), player, amount, menu.getPrice(amount), menu.getTransactionType());
                    Bukkit.getPluginManager().callEvent((Event)preTransactionEvent);
                    price = preTransactionEvent.getPrice();
                    if (!preTransactionEvent.isCancelled()) {
                        if (this.plugin.getEcoHandler().getEcon(menu.getShopItem().getEcoType()).getBalance((OfflinePlayer)player) >= price) {
                            this.plugin.getEcoHandler().getEcon(menu.getShopItem().getEcoType()).withdrawBalance((OfflinePlayer)player, price);
                            menu.sendTransactionCompleteMessage(price);
                            menu.addItemStacks(amount);
                            if (menu.getShopItem().isCloseMenu()) {
                                this.close(player);
                            } else {
                                menu.open();
                            }
                        } else {
                            SendMessage.chatToPlayer(player, Lang.INSUFFICIENT_MONEY.get().replace("%currency%", this.plugin.getEcoHandler().getEcon(menu.getShopItem().getEcoType()).getFriendly()));
                            result = Transaction.Result.INSUFFICIENT_FUNDS;
                        }
                    } else {
                        result = Transaction.Result.TRANSACTION_CANCELLED;
                    }
                } else {
                    SendMessage.chatToPlayer(player, Lang.MORE_SPACE_NEEDED.get());
                    result = Transaction.Result.NO_INVENTORY_SPACE;
                }
            } else {
                SendMessage.chatToPlayer(player, Lang.CANNOT_PURCHASE_ITEM.get());
                result = Transaction.Result.NEGATIVE_ITEM_PRICE;
            }
        } else {
            Double sellPrice = menu.sellAllItems(menu.getAmount());
            if (sellPrice != null) {
                amount = menu.getTotalAmount();
                PreTransactionEvent preTransactionEvent = new PreTransactionEvent(menu.getShopItem(), player, amount, sellPrice, menu.getTransactionType());
                Bukkit.getPluginManager().callEvent((Event)preTransactionEvent);
                price = preTransactionEvent.getPrice();
                if (!preTransactionEvent.isCancelled()) {
                    if (price >= 0.0) {
                        this.plugin.getEcoHandler().getEcon(menu.getShopItem().getEcoType()).depositBalance((OfflinePlayer)player, price);
                        menu.sendTransactionCompleteMessage(price);
                        menu.emptyCache();
                        if (menu.getShopItem().isCloseMenu()) {
                            this.close(player);
                        } else {
                            menu.open();
                        }
                    } else {
                        SendMessage.chatToPlayer(player, Lang.ITEM_CANNOT_BE_SOLD.get());
                        result = Transaction.Result.NEGATIVE_ITEM_PRICE;
                    }
                } else {
                    result = Transaction.Result.TRANSACTION_CANCELLED;
                }
            } else {
                SendMessage.chatToPlayer(player, Lang.NO_ITEM_FOUND.get());
                result = Transaction.Result.NO_ITEMS_FOUND;
            }
        }
        Bukkit.getPluginManager().callEvent((Event)new PostTransactionEvent(menu.getShopItem(), player, amount, price, menu.getTransactionType(), result));
    }

    public void completeSellAll(TransactionMenu menu, Player player) {
        Transaction.Result result = Transaction.Result.SUCCESS;
        Double sellPrice = menu.sellAllItems(Integer.MAX_VALUE);
        if (sellPrice != null) {
            PreTransactionEvent preTransactionEvent = new PreTransactionEvent(menu.getShopItem(), player, menu.getAmount(), sellPrice, menu.getTransactionType());
            Bukkit.getPluginManager().callEvent((Event)preTransactionEvent);
            sellPrice = preTransactionEvent.getPrice();
            if (!preTransactionEvent.isCancelled()) {
                if (sellPrice >= 0.0) {
                    this.plugin.getEcoHandler().getEcon(menu.getShopItem().getEcoType()).depositBalance((OfflinePlayer)player, sellPrice);
                    menu.sendTransactionCompleteMessage(sellPrice);
                    menu.emptyCache();
                    if (menu.getShopItem().isCloseMenu()) {
                        this.close(player);
                    } else {
                        menu.open();
                    }
                } else {
                    result = Transaction.Result.NEGATIVE_ITEM_PRICE;
                }
            } else {
                result = Transaction.Result.TRANSACTION_CANCELLED;
            }
        } else {
            SendMessage.chatToPlayer(player, Lang.NO_ITEM_TO_BE_SOLD.get());
            result = Transaction.Result.NO_ITEMS_FOUND;
            sellPrice = 0.0;
        }
        Bukkit.getPluginManager().callEvent((Event)new PostTransactionEvent(menu.getShopItem(), player, menu.getAmount(), sellPrice, menu.getTransactionType(), result));
    }

    public void purchaseItem(Player p, ShopItem shopItem) {
        int amount = shopItem.getStackSize();
        double price = 0.0;
        if (!this.canBuy(p, shopItem)) {
            SendMessage.chatToPlayer(p, Lang.MORE_SPACE_NEEDED.get());
            this.callPost(shopItem, p, amount, price, Transaction.Type.QUICK_BUY, Transaction.Result.NO_INVENTORY_SPACE);
            return;
        }
        amount = this.getFinalAmount(shopItem, p);
        if (shopItem.isMinBuy(amount)) {
            SendMessage.chatToPlayer(p, Lang.NOT_ENOUGH_ITEMS_TO_BUY.get().replace("%min-buy%", String.valueOf(shopItem.getMinBuy())));
            this.callPost(shopItem, p, amount, price, Transaction.Type.QUICK_BUY, Transaction.Result.NOT_ENOUGH_ITEMS);
            return;
        }
        PreTransactionEvent preTransactionEvent = new PreTransactionEvent(Collections.singletonMap(shopItem, amount), Collections.singletonMap(shopItem.getEcoType(), shopItem.getBuyPrice(p) * (double)amount), p, amount, Transaction.Type.QUICK_BUY);
        Bukkit.getPluginManager().callEvent((Event)preTransactionEvent);
        price = preTransactionEvent.getPrice();
        if (preTransactionEvent.isCancelled()) {
            this.callPost(shopItem, p, amount, price, Transaction.Type.QUICK_BUY, Transaction.Result.TRANSACTION_CANCELLED);
            return;
        }
        if (this.plugin.getEcoHandler().getEcon(shopItem.getEcoType()).getBalance((OfflinePlayer)p) < price) {
            SendMessage.chatToPlayer(p, Lang.INSUFFICIENT_MONEY.get().replace("%currency%", this.plugin.getEcoHandler().getEcon(shopItem.getEcoType()).getFriendly()));
            this.callPost(shopItem, p, amount, price, Transaction.Type.QUICK_BUY, Transaction.Result.INSUFFICIENT_FUNDS);
            return;
        }
        this.plugin.getEcoHandler().getEcon(shopItem.getEcoType()).withdrawBalance((OfflinePlayer)p, price);
        ItemStack stack = new ItemStack(shopItem.getItemToGive());
        stack.setAmount(amount);
        p.getInventory().addItem(new ItemStack[]{stack});
        SendMessage.sendTransactionMessage(p, amount, price, shopItem, Transaction.Mode.BUY, Transaction.Type.QUICK_BUY);
        if (shopItem.isCloseMenu()) {
            p.closeInventory();
            return;
        }
        this.callPost(shopItem, p, amount, price, Transaction.Type.QUICK_BUY, Transaction.Result.SUCCESS);
    }

    public void sellItem(Player p, ShopItem shopItem, int amount) {
        double price = 0.0;
        if (!this.canSell(p, shopItem)) {
            SendMessage.chatToPlayer(p, Lang.NO_ITEM_FOUND.get());
            this.callPost(shopItem, p, amount, price, Transaction.Type.QUICK_SELL, Transaction.Result.NO_ITEMS_FOUND);
            return;
        }
        ArrayList<ItemStack> items = this.getSellAble(p, shopItem, amount);
        if (shopItem.isMinSell(amount = items.stream().mapToInt(ItemStack::getAmount).sum())) {
            SendMessage.chatToPlayer(p, Lang.NOT_ENOUGH_ITEMS_TO_SELL.get().replace("%min-sell%", String.valueOf(shopItem.getMinSell())).replace("%quantity%", String.valueOf(amount)));
            this.callPost(shopItem, p, amount, price, Transaction.Type.QUICK_SELL, Transaction.Result.NOT_ENOUGH_ITEMS);
            return;
        }
        price = items.stream().mapToDouble(i -> shopItem.getSellPrice(p, (ItemStack)i)).sum();
        PreTransactionEvent preTransactionEvent = new PreTransactionEvent(shopItem, p, amount, price, Transaction.Type.QUICK_SELL);
        Bukkit.getPluginManager().callEvent((Event)preTransactionEvent);
        price = preTransactionEvent.getPrice();
        if (preTransactionEvent.isCancelled()) {
            this.callPost(shopItem, p, amount, price, Transaction.Type.QUICK_SELL, Transaction.Result.TRANSACTION_CANCELLED);
            return;
        }
        this.plugin.getEcoHandler().getEcon(shopItem.getEcoType()).depositBalance((OfflinePlayer)p, price);
        items.forEach(item -> p.getInventory().removeItem(new ItemStack[]{item}));
        SendMessage.sendTransactionMessage(p, amount, price, shopItem, Transaction.Mode.SELL, Transaction.Type.QUICK_SELL);
        if (shopItem.isCloseMenu()) {
            p.closeInventory();
            return;
        }
        this.callPost(shopItem, p, amount, price, Transaction.Type.QUICK_SELL, Transaction.Result.SUCCESS);
    }

    private void callPost(ShopItem shopItem, Player p, int amount, double price, Transaction.Type type, Transaction.Result result) {
        Bukkit.getPluginManager().callEvent((Event)new PostTransactionEvent(shopItem, p, amount, price, type, result));
    }

    private ArrayList<ItemStack> getSellAble(Player p, ShopItem shopItem, int limit) {
        if (shopItem.isMaxSell(limit)) {
            limit = shopItem.getMaxSell();
        }
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        for (int i = 0; i < 36; ++i) {
            ItemStack itemInInv = p.getInventory().getItem(i);
            if (itemInInv == null || itemInInv.getType().equals((Object)Material.AIR) || !shopItem.match(itemInInv)) continue;
            if (limit < itemInInv.getAmount()) {
                ItemStack item = new ItemStack(itemInInv);
                item.setAmount(limit);
                items.add(item);
                break;
            }
            items.add(itemInInv);
            limit -= itemInInv.getAmount();
        }
        return items;
    }

    private boolean canSell(Player p, ShopItem shopItem) {
        for (int i = 0; i < 36; ++i) {
            ItemStack itemInInv = p.getInventory().getItem(i);
            if (itemInInv == null || itemInInv.getType().equals((Object)Material.AIR) || !shopItem.match(itemInInv)) continue;
            return true;
        }
        return false;
    }

    private boolean canBuy(Player p, ShopItem shopItem) {
        return p.getInventory().firstEmpty() != -1 || Arrays.stream(p.getInventory().getContents()).anyMatch(stack -> stack != null && stack.isSimilar(shopItem.getItemToGive()) && stack.getAmount() < stack.getMaxStackSize());
    }

    private int getFinalAmount(ShopItem shopItem, Player p) {
        int freeSpace = 0;
        int maxStackSize = shopItem.getItemToGive().getMaxStackSize();
        List<ItemStack> items = Arrays.asList(p.getInventory().getContents());
        for (int i = 0; i < 36; ++i) {
            ItemStack item = items.get(i);
            if (item == null || item.getType().equals((Object)Material.AIR)) {
                freeSpace += maxStackSize;
                continue;
            }
            if (!item.isSimilar(shopItem.getItemToGive()) || item.getAmount() >= maxStackSize) continue;
            freeSpace += maxStackSize - item.getAmount();
        }
        if (shopItem.isMaxBuy(freeSpace)) {
            freeSpace = shopItem.getMaxBuy();
        }
        return shopItem.getStackSize() > freeSpace && !this.plugin.dropItemsOnGround ? freeSpace : shopItem.getStackSize();
    }
}

