

package com.siliconstudios.dynamicshopguiplus.objects.shops;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import com.siliconstudios.dynamicshopguiplus.objects.DisplayItem;
import com.siliconstudios.dynamicshopguiplus.objects.ShopItem;
import com.siliconstudios.dynamicshopguiplus.objects.ShopPageItems;
import com.siliconstudios.dynamicshopguiplus.objects.mappings.ClickAction;
import com.siliconstudios.dynamicshopguiplus.objects.shops.ShopType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public interface ShopSection {
    public String getSection();

    public Collection<DisplayItem> getDisplayItems();

    public DisplayItem getDisplayItem(String var1);

    public boolean isActionItem(String var1, String var2);

    public Collection<ShopItem> getShopItems();

    public ShopItem getShopItem(String var1);

    public ShopPageItems getShopPageItems(int var1);

    public int getPages();

    public boolean isSubSection();

    public boolean isHidden();

    public boolean isCloseMenu();

    public ShopType getType();

    public List<String> getItemLocs();

    public List<String> getShopItemLocs();

    public ClickAction getClickAction(ClickType var1);

    public int getPageForShopItem(String var1);

    public void openShopSection(Player var1, boolean var2);

    public void openShopSection(Player var1, boolean var2, String var3);

    public void openShopSection(Player var1, int var2, boolean var3);

    public void openShopSection(Player var1, int var2, boolean var3, @Nullable String var4);

    public void reloadItem(String var1);

    public ItemStack updateItem(Player var1, ShopItem var2);
}

