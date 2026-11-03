

package com.siliconstudios.dynamicshopguiplus.objects;

import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.objects.ShopInventory;
import com.siliconstudios.dynamicshopguiplus.objects.User;
import com.siliconstudios.dynamicshopguiplus.providers.UserManager;
import com.siliconstudios.dynamicshopguiplus.util.PermissionsCache;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MainMenu
extends ShopInventory {
    private final Inventory inv;
    private final DynamicShopGUIPlus plugin = DynamicShopGUIPlus.getInstance();

    public static void open(Player player) {
        DynamicShopGUIPlus plugin = DynamicShopGUIPlus.getInstance();
        if (plugin.bedrock && plugin.bedrockMenus && UserManager.getUser(player).isBedrock() && plugin.getBedrockInventorys().isEnabledFor(player)) {
            plugin.getBedrockInventorys().openMainMenu(player);
        } else {
            new MainMenu(player);
        }
    }

    public MainMenu(Player p) {
        User user = UserManager.getUser(p);
        this.inv = this.plugin.getMetaUtils().createInventory(this, this.plugin.resizeGUI && user.isBedrock() ? 54 : this.plugin.mainMenuSize, Lang.INVENTORY_MAIN_SHOP_TITLE.get(p));
        this.init(p);
        this.plugin.navBar.addMainMenuNavBar(this.inv, p);
        this.open(p, user);
    }

    private void open(Player player, User user) {
        user.setOpenNewGUI(true);
        player.openInventory(this.inv);
    }

    private void init(Player p) {
        this.plugin.getSectionItems().forEach((section, item) -> {
            ItemStack stack = item.getItem();
            if (item.isHideIfNoPerms() && !PermissionsCache.hasPermission(p, "DynamicShopGUIPlus.shop." + section.toLowerCase())) {
                return;
            }
            for (Integer i : item.getSlots()) {
                this.inv.setItem(i.intValue(), stack);
            }
        });
        if (this.plugin.createItem.fillItem != null) {
            int menuSize = this.plugin.mainMenuSize;
            for (int i = 0; i < (this.plugin.navBar.isEnableMainNav() ? menuSize - 9 : menuSize); ++i) {
                if (this.inv.getItem(i) != null) continue;
                this.inv.setItem(i, this.plugin.createItem.fillItem);
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return this.inv;
    }
}

