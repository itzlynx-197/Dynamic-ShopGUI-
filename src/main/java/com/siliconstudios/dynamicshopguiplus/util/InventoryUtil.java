

package com.siliconstudios.dynamicshopguiplus.util;

import java.lang.reflect.Method;
import java.util.function.Function;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class InventoryUtil {
    private static final Function<Inventory, InventoryHolder> holderFunction = InventoryUtil.getHolderFunction();
    private static final Function<Player, Inventory> topFunction = InventoryUtil.getTopFunction();

    public static InventoryHolder getHolder(Inventory inv) {
        return holderFunction.apply(inv);
    }

    public static Inventory getTopInventory(Player p) {
        return topFunction.apply(p);
    }

    public static InventoryHolder getTopHolder(Player p) {
        return holderFunction.apply(topFunction.apply(p));
    }

    private static Function<Inventory, InventoryHolder> getHolderFunction() {
        try {
            Method method = Inventory.class.getMethod("getHolder", Boolean.TYPE);
            return inv -> {
                try {
                    return (InventoryHolder)method.invoke(inv, false);
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        }
        catch (NoSuchMethodException ignored) {
            return Inventory::getHolder;
        }
    }

    private static Function<Player, Inventory> getTopFunction() {
        try {
            Method getOpenInv = Player.class.getMethod("getOpenInventory", new Class[0]);
            Method getTopInv = getOpenInv.getReturnType().getDeclaredMethod("getTopInventory", new Class[0]);
            getTopInv.setAccessible(true);
            return p -> {
                try {
                    Object invView = getOpenInv.invoke(p, new Object[0]);
                    return (Inventory)getTopInv.invoke(invView, new Object[0]);
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        }
        catch (NoSuchMethodException ignored) {
            return p -> p.getOpenInventory().getTopInventory();
        }
    }
}

