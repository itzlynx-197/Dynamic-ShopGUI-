

package com.siliconstudios.dynamicshopguiplus.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.objects.ShopItem;
import com.siliconstudios.dynamicshopguiplus.util.ServerInfo;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerItems {
    private static final DynamicShopGUIPlus plugin = DynamicShopGUIPlus.getInstance();

    public static void giveItems(Player p, ShopItem shopItem, int amount) {
        int maxStackSize = shopItem.getItemToGive().getMaxStackSize();
        double div = (double)amount / (double)maxStackSize;
        int fullStacks = (int)Math.floor(div);
        int stacks = div == Math.floor(div) ? fullStacks : (int)Math.ceil(div);
        int amountLeft = amount;
        ItemStack[] items = new ItemStack[stacks];
        if (fullStacks >= 1) {
            for (int i = 0; i < fullStacks; ++i) {
                ItemStack item = new ItemStack(shopItem.getItemToGive());
                item.setAmount(maxStackSize);
                items[i] = item;
                amountLeft -= maxStackSize;
            }
        }
        if (amountLeft >= 1) {
            ItemStack item = new ItemStack(shopItem.getItemToGive());
            item.setAmount(amountLeft);
            items[stacks - 1] = item;
        }
        PlayerItems.addItems(p, items);
    }

    private static void addItems(Player p, ItemStack[] items) {
        HashMap<Integer, ItemStack> leftItems = p.getInventory().addItem(items);
        if (!leftItems.isEmpty()) {
            if (DynamicShopGUIPlus.getInstance().dropItemsOnGround) {
                Location loc = p.getLocation();
                World world = p.getWorld();
                for (ItemStack item : leftItems.values()) {
                    world.dropItem(loc, item);
                }
                SendMessage.chatToPlayer(p, Lang.NOT_ENOUGH_SPACE_INSIDE_INVENTORY.get());
            } else {
                int itemsLeft = 0;
                for (ItemStack item : leftItems.values()) {
                    itemsLeft += item.getAmount();
                }
                SendMessage.chatToPlayer(p, Lang.COULD_NOT_STORE_ALL_ITEMS.get().replace("%amount%", Integer.toString(itemsLeft)));
            }
        }
        p.updateInventory();
    }

    public static boolean canSell(ShopItem shopItem, Player p) {
        for (int i = 0; i < 36; ++i) {
            ItemStack itemInInv = p.getInventory().getItem(i);
            if (itemInInv == null || itemInInv.getType().equals((Object)Material.AIR) || !shopItem.match(itemInInv)) continue;
            return true;
        }
        return false;
    }

    public static ArrayList<ItemStack> getSellAble(ShopItem shopItem, Player p, int amount) {
        return PlayerItems.getSellAble(shopItem, p, amount, true);
    }

    public static ArrayList<ItemStack> getSellAble(ShopItem shopItem, Player p, int amount, boolean allowQuantityChange) {
        if (allowQuantityChange && shopItem.isMaxSell(amount)) {
            amount = shopItem.getMaxSell();
        }
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        for (int i = 0; i < 36; ++i) {
            ItemStack itemInInv = p.getInventory().getItem(i);
            if (itemInInv == null || itemInInv.getType().equals((Object)Material.AIR) || !shopItem.match(itemInInv)) continue;
            if (amount < itemInInv.getAmount()) {
                ItemStack item = new ItemStack(itemInInv);
                item.setAmount(amount);
                items.add(item);
                break;
            }
            items.add(itemInInv);
            amount -= itemInInv.getAmount();
        }
        return items;
    }

    public static boolean canBuy(Player p, ShopItem shopItem) {
        return p.getInventory().firstEmpty() != -1 || Arrays.stream(p.getInventory().getContents()).anyMatch(stack -> stack != null && stack.isSimilar(shopItem.getItemToGive()) && stack.getAmount() < stack.getMaxStackSize());
    }

    public static int getFinalAmount(ShopItem shopItem, Player p, int expected) {
        return PlayerItems.getFinalAmount(shopItem, p, expected, true);
    }

    public static int getFinalAmount(ShopItem shopItem, Player p, int expected, boolean allowQuantityChange) {
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
        if (allowQuantityChange && shopItem.isMaxBuy(freeSpace)) {
            freeSpace = shopItem.getMaxBuy();
        }
        return !PlayerItems.plugin.dropItemsOnGround ? Math.min(expected, freeSpace) : expected;
    }

    public static boolean hasCustomName(ItemMeta meta) {
        return meta.hasDisplayName() || ServerInfo.supportsComponents() && meta.hasItemName();
    }
}

