

package com.siliconstudios.dynamicshopguiplus.util;

import java.util.List;
import java.util.Map;
import com.siliconstudios.dynamicshopguiplus.files.Translatable;
import com.siliconstudios.dynamicshopguiplus.objects.ShopItem;
import com.siliconstudios.dynamicshopguiplus.util.EcoType;
import com.siliconstudios.dynamicshopguiplus.util.Transaction;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.ItemMeta;

public interface MetaUtils {
    public void update();

    public void send(Player var1, Translatable var2);

    public void send(Object var1, Translatable var2);

    public void send(Translatable var1);

    public void sendLegacy(Player var1, String var2);

    public void sendLegacy(Object var1, String var2);

    public void sendLegacy(String var1);

    public void sendTransactionMessage(Player var1, int var2, double var3, ShopItem var5, Transaction.Mode var6, Transaction.Type var7);

    public void sendTransactionMessage(Player var1, int var2, Map<EcoType, Double> var3, Map<ShopItem, Integer> var4, Transaction.Type var5);

    public Inventory createInventory(InventoryHolder var1, int var2, Translatable var3);

    public void setRawDisplayName(ItemMeta var1, String var2);

    public void setDisplayName(ItemMeta var1, Translatable var2);

    public String getRawDisplayName(ItemMeta var1);

    public Translatable getDisplayName(ItemMeta var1);

    public void setRawLore(ItemMeta var1, List<String> var2);

    public List<String> getRawLore(ItemMeta var1);

    public void setLore(ItemMeta var1, List<Translatable> var2);

    public void addLore(ItemMeta var1, Translatable var2);

    public List<Translatable> getLore(ItemMeta var1);

    public void broadcast(Translatable var1);

    public void logDevDebugMessage(Translatable var1);

    public void logPlayerTransaction(Translatable var1);

    public void logPlayerTransaction(String var1);

    public void infoMessage(Translatable var1);

    public void logDebugMessage(Translatable var1);

    public void warnMessage(Translatable var1);

    public void errorMessage(Translatable var1);

    public void infoLegacyMessage(String var1);

    public void logLegacyDebugMessage(String var1);

    public void warnLegacyMessage(String var1);

    public void errorLegacyMessage(String var1);

    public void infoMessage(Object var1, Translatable var2);

    public void warnMessage(Object var1, Translatable var2);

    public void errorMessage(Object var1, Translatable var2);

    public void infoLegacyMessage(Object var1, String var2);

    public void warnLegacyMessage(Object var1, String var2);

    public void errorLegacyMessage(Object var1, String var2);
}

