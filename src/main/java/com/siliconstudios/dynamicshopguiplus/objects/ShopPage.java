

package com.siliconstudios.dynamicshopguiplus.objects;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.Translatable;
import com.siliconstudios.dynamicshopguiplus.objects.ShopInventory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ShopPage
extends ShopInventory {
    private final int size;
    private final int allPages;
    private final boolean disabledBackButton;
    private final String rootSection;
    private final Set<Integer> fillItems = new HashSet<Integer>();
    private int page;
    private final String section;
    private final Inventory inventory;

    public int getAllPages() {
        return this.allPages;
    }

    public boolean isDisabledBackButton() {
        return this.disabledBackButton;
    }

    public String getSection() {
        return this.section;
    }

    public String getItem(int slot) {
        return DynamicShopGUIPlus.getInstance().getSection(this.section).getShopPageItems(this.page).getItem(slot);
    }

    public ShopPage(Map<Integer, ItemStack> items, ItemStack fillItem, Player player, Translatable title, String section, int page, int allPages, boolean disabledBackButton, boolean navBar, int size, @Nullable String rootSection) {
        this.page = page;
        this.allPages = allPages;
        this.section = section;
        this.disabledBackButton = disabledBackButton;
        this.size = size;
        this.rootSection = rootSection;
        this.inventory = DynamicShopGUIPlus.getInstance().getMetaUtils().createInventory(this, size, title);
        this.init(player, items, fillItem, navBar);
    }

    private void init(Player p, Map<Integer, ItemStack> items, ItemStack fillItem, boolean navBar) {
        for (int i = 0; i < (navBar ? this.size - 9 : this.size); ++i) {
            if (items.containsKey(i)) {
                this.inventory.setItem(i, items.get(i));
                continue;
            }
            if (fillItem != null) {
                this.inventory.setItem(i, fillItem);
                this.fillItems.add(i);
                continue;
            }
            this.inventory.setItem(i, new ItemStack(Material.AIR));
        }
        DynamicShopGUIPlus.getInstance().navBar.addShopsNavBar(this.inventory, p, this.section, this.page, this.allPages);
    }

    public void updateContents(Player player, Map<Integer, ItemStack> items, ItemStack fillItem, int page, boolean navBar) {
        this.page = page;
        for (int i = 0; i < (navBar ? this.size - 9 : this.size); ++i) {
            if (items.containsKey(i)) {
                this.inventory.setItem(i, items.get(i));
                continue;
            }
            if (fillItem != null) {
                this.inventory.setItem(i, fillItem);
                continue;
            }
            this.inventory.setItem(i, new ItemStack(Material.AIR));
        }
        items.forEach((arg_0, arg_1) -> ((Inventory)this.inventory).setItem(arg_0, arg_1));
        DynamicShopGUIPlus.getInstance().navBar.updateShopsNavBar(this.inventory, player, this.section, page, this.allPages);
    }

    public int getPage() {
        return this.page;
    }

    public int getSize() {
        return this.size;
    }

    public boolean isSubSection() {
        return this.rootSection != null;
    }

    public String getRootSection() {
        return this.rootSection;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}

