

package com.siliconstudios.dynamicshopguiplus.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.CalculateAmount;
import com.siliconstudios.dynamicshopguiplus.methodes.CreateItem;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.objects.ShopInventory;
import com.siliconstudios.dynamicshopguiplus.objects.ShopItem;
import com.siliconstudios.dynamicshopguiplus.objects.TransactionItem;
import com.siliconstudios.dynamicshopguiplus.objects.TransactionMenus;
import com.siliconstudios.dynamicshopguiplus.providers.UserManager;
import com.siliconstudios.dynamicshopguiplus.util.ItemBuilder;
import com.siliconstudios.dynamicshopguiplus.util.Transaction;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TransactionMenu
extends ShopInventory {
    private static final VisualLimitAction visualLimitAction = VisualLimitAction.get();
    private static final boolean checkInitialAmount = ConfigManager.getConfig().getBoolean("visual-transaction-limits.initial-amount");
    private final List<ItemStack> CACHE = new ArrayList<ItemStack>();
    private final ShopItem shopItem;
    private final boolean disabledBackButton;
    private final String rootSection;
    private final ItemStack selectedItem;
    private final Transaction.Mode transactionMode;
    private final Transaction.Type transactionType;
    private final Inventory inv;
    private final Player player;
    private int amount;

    public TransactionMenu(Player p, ShopItem shopItem, String rootSection, boolean disabledBackButton, Transaction.Mode transactionMode, Transaction.Type transactionType, int amount) {
        this.shopItem = shopItem;
        this.player = p;
        this.selectedItem = new ItemStack(this.shopItem.getShopItem());
        this.rootSection = rootSection;
        this.disabledBackButton = disabledBackButton;
        this.transactionMode = transactionMode;
        this.transactionType = transactionType;
        this.amount = this.getInitialAmount(this.shopItem.getStackSize());
        this.inv = DynamicShopGUIPlus.getInstance().getMetaUtils().createInventory(this, CalculateAmount.getInvSlotsDef(UserManager.getUser(p), TransactionMenus.getSize(transactionType)), this.getInvTitle().get(p).replace("%item%", this.shopItem.getDisplayname()).replace("%material%", DynamicShopGUIPlus.getInstance().getMaterialName(this.selectedItem.getType().name())));
        this.init();
    }

    private int getInitialAmount(int stackSize) {
        int amount;
        if (this.transactionMode == Transaction.Mode.BUY) {
            amount = this.shopItem.isMinBuy(stackSize) ? Math.max(this.shopItem.getMinBuy(), stackSize) : stackSize;
        } else if (this.transactionMode == Transaction.Mode.SELL) {
            amount = this.shopItem.isMinSell(stackSize) ? Math.max(this.shopItem.getMinSell(), stackSize) : stackSize;
        } else {
            return 1;
        }
        return this.shopItem.isFixedStackSize() ? this.shopItem.getClosestStackSize(amount) : amount;
    }

    public Player getPlayer() {
        return this.player;
    }

    public boolean isDisabledBackButton() {
        return this.disabledBackButton;
    }

    public String getRootSection() {
        return this.rootSection;
    }

    public Transaction.Mode getTransactionMode() {
        return this.transactionMode;
    }

    public Transaction.Type getTransactionType() {
        return this.transactionType;
    }

    public String getItemPath() {
        return this.shopItem.getItemPath();
    }

    public ItemStack getSelectedItem() {
        return this.selectedItem;
    }

    public ShopItem getShopItem() {
        return this.shopItem;
    }

    public int getAmount() {
        return this.amount;
    }

    public void emptyCache() {
        for (ItemStack i : this.CACHE) {
            this.player.getInventory().removeItem(new ItemStack[]{i});
        }
        this.CACHE.clear();
    }

    public int getTotalAmount() {
        return this.transactionType == Transaction.Type.BUY_STACKS_SCREEN ? this.amount * this.getMaxStackSize() : this.amount;
    }

    public int getMaxStackSize() {
        return this.shopItem.isFixedStackSize() ? this.shopItem.getFixedStackSize() : this.selectedItem.getMaxStackSize();
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void addItemStacks(int amount) {
        int maxStackSize = this.shopItem.getItemToGive().getMaxStackSize();
        double div = (double)amount / (double)maxStackSize;
        int fullStacks = (int)Math.floor(div);
        int stacks = div == Math.floor(div) ? fullStacks : (int)Math.ceil(div);
        int amountLeft = amount;
        ItemStack[] items = new ItemStack[stacks];
        if (fullStacks >= 1) {
            for (int i = 0; i < fullStacks; ++i) {
                ItemStack item = new ItemStack(this.shopItem.getItemToGive());
                item.setAmount(maxStackSize);
                items[i] = item;
                amountLeft -= maxStackSize;
            }
        }
        if (amountLeft >= 1) {
            ItemStack item = new ItemStack(this.shopItem.getItemToGive());
            item.setAmount(amountLeft);
            items[stacks - 1] = item;
        }
        this.addItems(items);
    }

    private void addItems(ItemStack[] items) {
        HashMap<Integer, ItemStack> leftItems = this.player.getInventory().addItem(items);
        if (!leftItems.isEmpty()) {
            if (DynamicShopGUIPlus.getInstance().dropItemsOnGround) {
                Location loc = this.player.getLocation();
                World world = this.player.getWorld();
                for (ItemStack item : leftItems.values()) {
                    world.dropItem(loc, item);
                }
                SendMessage.chatToPlayer(this.player, Lang.NOT_ENOUGH_SPACE_INSIDE_INVENTORY.get(this.player));
            } else {
                int itemsLeft = 0;
                for (ItemStack item : leftItems.values()) {
                    itemsLeft += item.getAmount();
                }
                SendMessage.chatToPlayer(this.player, Lang.COULD_NOT_STORE_ALL_ITEMS.get(this.player).replace("%amount%", Integer.toString(itemsLeft)));
            }
        }
        this.player.updateInventory();
    }

    public Double sellAllItems(int limit) {
        double sellPrice = 0.0;
        int amount = 0;
        if (this.shopItem.isMaxSell(limit)) {
            limit = this.shopItem.getMaxSell();
        }
        for (int i = 0; i < 36; ++i) {
            ItemStack itemInInv = this.player.getInventory().getItem(i);
            if (itemInInv == null || itemInInv.getType().equals((Object)Material.AIR) || !this.shopItem.match(itemInInv)) continue;
            if (limit < itemInInv.getAmount()) {
                ItemStack item = new ItemStack(itemInInv);
                item.setAmount(limit);
                this.CACHE.add(item);
                amount += limit;
                sellPrice += this.shopItem.getSellPrice(this.player, itemInInv, limit);
                break;
            }
            this.CACHE.add(itemInInv);
            amount += itemInInv.getAmount();
            sellPrice += this.shopItem.getSellPrice(this.player, itemInInv, itemInInv.getAmount());
            limit -= itemInInv.getAmount();
        }
        if (this.shopItem.isMinSell(amount)) {
            return null;
        }
        this.amount = amount;
        return amount == 0 ? null : Double.valueOf(sellPrice);
    }

    public int getFreeSpace() {
        int freespace = 0;
        int maxStackSize = this.shopItem.getItemToGive().getMaxStackSize();
        ArrayList<ItemStack> items = new ArrayList<ItemStack>(Arrays.asList(this.player.getInventory().getContents()));
        for (int i = 0; i < 36; ++i) {
            ItemStack item = (ItemStack)items.get(i);
            if (item == null || item.getType().equals((Object)Material.AIR)) {
                freespace += maxStackSize;
                continue;
            }
            if (!item.isSimilar(this.shopItem.getItemToGive()) || item.getAmount() >= maxStackSize) continue;
            freespace += maxStackSize - item.getAmount();
        }
        return freespace;
    }

    public int getAmountToSell(int freeSpace) {
        int totalAmount = this.getTotalAmount();
        if (totalAmount > freeSpace && !DynamicShopGUIPlus.getInstance().dropItemsOnGround) {
            return freeSpace;
        }
        if (this.shopItem.isMaxBuy(totalAmount)) {
            totalAmount = this.shopItem.getMaxBuy();
        }
        if (this.shopItem.isMinBuy(totalAmount)) {
            totalAmount = this.shopItem.getMinBuy();
        }
        return totalAmount;
    }

    public void sendTransactionCompleteMessage(double price) {
        SendMessage.sendTransactionMessage(this.player, this.getTotalAmount(), price, this.shopItem, this.transactionMode, this.transactionType);
    }

    private String getTransactionScreenType() {
        return this.transactionType.getName();
    }

    public Lang getInvTitle() {
        Lang key = null;
        switch (this.transactionType) {
            case SELL_SCREEN: {
                key = Lang.INVENTORY_HOWMUCHSELL_TITLE;
                break;
            }
            case BUY_SCREEN: {
                key = Lang.INVENTORY_HOWMUCHBUY_TITLE;
                break;
            }
            case BUY_STACKS_SCREEN: {
                key = Lang.INVENTORY_BUYSTACKS_TITLE;
                break;
            }
            case SHOPSTAND_BUY_SCREEN: {
                key = Lang.SHOPSTAND_BUY_SCREEN_TITLE;
                break;
            }
            case SHOPSTAND_SELL_SCREEN: {
                key = Lang.SHOPSTAND_SELL_SCREEN_TITLE;
            }
        }
        return key;
    }

    public double getBasePrice() {
        return this.transactionMode == Transaction.Mode.BUY ? this.shopItem.getBuyPrice() : this.shopItem.getSellPrice();
    }

    public double getPrice(int itemsToSell) {
        return this.shopItem.getBuyPrice(this.player) * (double)itemsToSell;
    }

    private void updateButtons(int amount) {
        int maxStack;
        TransactionItem fillItem = TransactionMenus.getItemByType(this.transactionType, CreateItem.TransactionItemType.FILLITEM);
        int n = maxStack = DynamicShopGUIPlus.getInstance().allowIllegalStacks ? 64 : this.shopItem.getItemToGive().getMaxStackSize();
        if (this.shopItem.isFixedStackSize()) {
            maxStack = this.shopItem.getSmallestStackSize(maxStack);
        }
        if (amount > this.amount || !this.isMaxAmount(amount)) {
            if (visualLimitAction != VisualLimitAction.NONE) {
                for (TransactionItem item : TransactionMenus.getItemsByType(this.transactionType, CreateItem.TransactionItemType.INCREASE_AMOUNT)) {
                    if (amount + item.getAction().amount > maxStack || this.isMaxAmount(amount + item.getAction().amount)) {
                        if (visualLimitAction == VisualLimitAction.RENAME) {
                            this.setItem(item.getSlots(), new ItemBuilder(item.getItem(this.shopItem, this.player, this.transactionType, this.getTotalAmount())).withDisplayName(Lang.MAX_AMOUNT_REACHED.get(this.player)).build());
                            continue;
                        }
                        this.setItem(item.getSlots(), fillItem == null ? new ItemStack(Material.AIR) : fillItem.getItem());
                        continue;
                    }
                    this.setItem(item.getSlots(), item.getItem(this.shopItem, this.player, this.transactionType, this.getTotalAmount()));
                }
                for (TransactionItem item : TransactionMenus.getItemsByType(this.transactionType, CreateItem.TransactionItemType.SELECT_AMOUNT)) {
                    if (amount == item.getAction().amount || this.isMaxAmount(item.getAction().amount)) {
                        if (visualLimitAction == VisualLimitAction.RENAME) {
                            this.setItem(item.getSlots(), new ItemBuilder(item.getItem(this.shopItem, this.player, this.transactionType, this.getTotalAmount())).withDisplayName(Lang.MAX_AMOUNT_REACHED.get(this.player)).build());
                            continue;
                        }
                        this.setItem(item.getSlots(), fillItem == null ? new ItemStack(Material.AIR) : fillItem.getItem());
                        continue;
                    }
                    this.setItem(item.getSlots(), item.getItem(this.shopItem, this.player, this.transactionType, this.getTotalAmount()));
                }
            }
            int n2 = amount > maxStack || this.isMaxAmount(amount) ? (this.getMaxAmount() != 0 ? Math.min(maxStack, this.getMaxAmount()) : maxStack) : (amount = amount);
        }
        if (amount < this.amount || !this.isMinAmount(amount)) {
            if (visualLimitAction != VisualLimitAction.NONE) {
                for (TransactionItem item : TransactionMenus.getItemsByType(this.transactionType, CreateItem.TransactionItemType.DECREASE_AMOUNT)) {
                    if (amount - item.getAction().amount < 1 || this.isMinAmount(amount - item.getAction().amount)) {
                        if (visualLimitAction == VisualLimitAction.RENAME) {
                            this.setItem(item.getSlots(), new ItemBuilder(item.getItem(this.shopItem, this.player, this.transactionType, this.getTotalAmount())).withDisplayName(Lang.MIN_AMOUNT_REACHED.get(this.player)).build());
                            continue;
                        }
                        this.setItem(item.getSlots(), fillItem == null ? new ItemStack(Material.AIR) : fillItem.getItem());
                        continue;
                    }
                    this.setItem(item.getSlots(), item.getItem(this.shopItem, this.player, this.transactionType, this.getTotalAmount()));
                }
                for (TransactionItem item : TransactionMenus.getItemsByType(this.transactionType, CreateItem.TransactionItemType.SELECT_AMOUNT)) {
                    if (amount == item.getAction().amount || this.isMaxAmount(amount + item.getAction().amount)) {
                        if (visualLimitAction == VisualLimitAction.RENAME) {
                            this.setItem(item.getSlots(), new ItemBuilder(item.getItem(this.shopItem, this.player, this.transactionType, this.getTotalAmount())).withDisplayName(Lang.MAX_AMOUNT_REACHED.get(this.player)).build());
                            continue;
                        }
                        this.setItem(item.getSlots(), fillItem == null ? new ItemStack(Material.AIR) : fillItem.getItem());
                        continue;
                    }
                    this.setItem(item.getSlots(), item.getItem(this.shopItem, this.player, this.transactionType, this.getTotalAmount()));
                }
            }
            amount = amount < 1 || this.isMinAmount(amount) ? (this.getMinAmount() != 0 ? Math.max(1, this.getMinAmount()) : 1) : amount;
        }
        this.amount = amount;
    }

    public void setConfirmTransactionItem() {
        TransactionItem item = TransactionMenus.getItemByType(this.transactionType, CreateItem.TransactionItemType.CONFIRM);
        if (item == null) {
            return;
        }
        String formatted = DynamicShopGUIPlus.getInstance().formatPrice(this.shopItem.getEcoType(), (this.transactionMode == Transaction.Mode.BUY ? this.shopItem.getBuyPrice(this.player) : this.shopItem.getSellPrice(this.player)) * (double)this.getTotalAmount());
        ItemStack stack = new ItemStack(item.getItem(this.shopItem, this.player, this.transactionType, this.getTotalAmount()));
        if (stack.hasItemMeta()) {
            ItemMeta meta = stack.getItemMeta();
            ItemBuilder builder = new ItemBuilder(stack);
            builder.setRawDisplayName(builder.getRawDisplayName().replace("%price%", formatted));
            if (meta.hasLore()) {
                builder.setRawLore(builder.getRawLore().stream().map(s -> s.replace("%price%", formatted)).collect(Collectors.toList()));
            }
            stack = builder.build();
        }
        this.setItem(item.getSlots(), stack);
    }

    public void setSelectedItem() {
        TransactionItem item = TransactionMenus.getItemByType(this.transactionType, CreateItem.TransactionItemType.SELECTED_ITEM);
        if (item != null) {
            this.setItem(item.getSlots(), item.getItem(this.selectedItem, this.shopItem, this.player, this.transactionType, this.amount));
        }
    }

    private void init() {
        TransactionItem fillItem = TransactionMenus.getItemByType(this.transactionType, CreateItem.TransactionItemType.FILLITEM);
        int maxStack = DynamicShopGUIPlus.getInstance().allowIllegalStacks ? 64 : this.shopItem.getItemToGive().getMaxStackSize();
        TransactionMenus.getItems(this.transactionType).forEach(item -> {
            switch (item.getType()) {
                case DECREASE_AMOUNT: {
                    if (visualLimitAction != VisualLimitAction.NONE && checkInitialAmount && (this.amount - item.getAction().amount < 1 || this.isMinAmount(this.amount - item.getAction().amount))) {
                        if (visualLimitAction == VisualLimitAction.RENAME) {
                            this.setItem(item.getSlots(), new ItemBuilder(item.getItem(this.shopItem, this.player, this.transactionType, this.getTotalAmount())).withDisplayName(Lang.MIN_AMOUNT_REACHED.get(this.player)).build());
                            break;
                        }
                        this.setItem(item.getSlots(), fillItem == null ? new ItemStack(Material.AIR) : fillItem.getItem());
                        break;
                    }
                    this.setItem(item.getSlots(), item.getItem(this.shopItem, this.player, this.transactionType, this.getTotalAmount()));
                    break;
                }
                case INCREASE_AMOUNT: {
                    if (visualLimitAction != VisualLimitAction.NONE && checkInitialAmount && (this.amount + item.getAction().amount > maxStack || this.isMaxAmount(this.amount + item.getAction().amount))) {
                        if (visualLimitAction == VisualLimitAction.RENAME) {
                            this.setItem(item.getSlots(), new ItemBuilder(item.getItem(this.shopItem, this.player, this.transactionType, this.getTotalAmount())).withDisplayName(Lang.MAX_AMOUNT_REACHED.get(this.player)).build());
                            break;
                        }
                        this.setItem(item.getSlots(), fillItem == null ? new ItemStack(Material.AIR) : fillItem.getItem());
                        break;
                    }
                    this.setItem(item.getSlots(), item.getItem(this.shopItem, this.player, this.transactionType, this.getTotalAmount()));
                    break;
                }
                case SELECT_AMOUNT: {
                    if (visualLimitAction != VisualLimitAction.NONE && checkInitialAmount && this.amount == item.getAction().amount || this.isMaxAmount(item.getAction().amount)) {
                        if (visualLimitAction == VisualLimitAction.RENAME) {
                            this.setItem(item.getSlots(), new ItemBuilder(item.getItem(this.shopItem, this.player, this.transactionType, this.getTotalAmount())).withDisplayName(Lang.MAX_AMOUNT_REACHED.get(this.player)).build());
                            break;
                        }
                        this.setItem(item.getSlots(), fillItem == null ? new ItemStack(Material.AIR) : fillItem.getItem());
                        break;
                    }
                    this.setItem(item.getSlots(), item.getItem(this.shopItem, this.player, this.transactionType, this.getTotalAmount()));
                    break;
                }
                case NORMAL: {
                    this.setItem(item.getSlots(), item.getItem(this.shopItem, this.player, this.transactionType, this.getTotalAmount()));
                }
            }
        });
        if (fillItem != null) {
            for (int i = 0; i < this.inv.getSize() - (DynamicShopGUIPlus.getInstance().navBar.isEnableTransactionNav() ? 9 : 0); ++i) {
                if (this.inv.getItem(i) != null) continue;
                this.inv.setItem(i, fillItem.getItem());
            }
        }
    }

    private boolean isMaxAmount(int amount) {
        return this.transactionMode == Transaction.Mode.BUY ? (this.transactionType == Transaction.Type.BUY_STACKS_SCREEN ? this.shopItem.isMaxBuy(amount * this.getMaxStackSize()) : this.shopItem.isMaxBuy(amount)) : this.shopItem.isMaxSell(amount);
    }

    private int getMaxAmount() {
        return this.transactionMode == Transaction.Mode.BUY ? (this.transactionType == Transaction.Type.BUY_STACKS_SCREEN ? this.shopItem.getMaxBuy() / this.getMaxStackSize() : this.shopItem.getMaxBuy()) : this.shopItem.getMaxSell();
    }

    private boolean isMinAmount(int amount) {
        return this.transactionMode == Transaction.Mode.BUY ? (this.transactionType == Transaction.Type.BUY_STACKS_SCREEN ? this.shopItem.isMinBuy(amount * this.getMaxStackSize()) : this.shopItem.isMinBuy(amount)) : this.shopItem.isMinSell(amount);
    }

    private int getMinAmount() {
        return this.transactionMode == Transaction.Mode.BUY ? (this.transactionType == Transaction.Type.BUY_STACKS_SCREEN ? this.shopItem.getMinBuy() / this.getMaxStackSize() : this.shopItem.getMinBuy()) : this.shopItem.getMinSell();
    }

    public void updateAmount(CreateItem.TransactionItemAction action) {
        int current = this.amount;
        switch (action.type) {
            case ADD: {
                current += action.amount;
                break;
            }
            case REMOVE: {
                current -= action.amount;
                break;
            }
            case SET: {
                current = action.amount;
            }
        }
        if (this.shopItem.isFixedStackSize()) {
            current = current > this.amount ? this.shopItem.getLargestStackSize(current) : this.shopItem.getSmallestStackSize(current);
        }
        this.updateButtons(current);
        this.setSelectedItem();
        this.setConfirmTransactionItem();
    }

    private void setItem(List<Integer> slots, ItemStack stack) {
        slots.forEach(i -> this.inv.setItem(i.intValue(), stack));
    }

    public void open() {
        if (DynamicShopGUIPlus.getInstance().bedrock && DynamicShopGUIPlus.getInstance().bedrockMenus && UserManager.getUser(this.player).isBedrock() && DynamicShopGUIPlus.getInstance().getBedrockInventorys().isEnabledFor(this.player)) {
            DynamicShopGUIPlus.getInstance().getBedrockInventorys().openTransactionMenu(this);
            return;
        }
        this.setSelectedItem();
        this.setConfirmTransactionItem();
        if (this.transactionType == Transaction.Type.SHOPSTAND_BUY_SCREEN || this.transactionType == Transaction.Type.SHOPSTAND_SELL_SCREEN) {
            DynamicShopGUIPlus.getInstance().navBar.addShopStandsNavBar(this.inv, this.player, this.shopItem.getEcoType());
        } else {
            DynamicShopGUIPlus.getInstance().navBar.addTransactionNavBar(this.inv, this.player, this.shopItem.getEcoType());
        }
        UserManager.getUser(this.player).setOpenNewGUI(true);
        this.player.openInventory(this.inv);
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    private static enum VisualLimitAction {
        NONE,
        RENAME,
        REMOVE;


        public static VisualLimitAction get() {
            try {
                return VisualLimitAction.valueOf(ConfigManager.getConfig().getString("visual-transaction-limits.action").toUpperCase(Locale.ROOT));
            }
            catch (IllegalArgumentException e) {
                SendMessage.logDebugMessage("Failed to load transaction visual limit for " + ConfigManager.getConfig().getString("visual-transaction-limits.action"));
                return REMOVE;
            }
        }
    }
}

