

package com.siliconstudios.dynamicshopguiplus.objects.inventorys;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.CalculateAmount;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.objects.ShopInventory;
import com.siliconstudios.dynamicshopguiplus.objects.inventorys.StandManagement;
import com.siliconstudios.dynamicshopguiplus.objects.stands.Stand;
import com.siliconstudios.dynamicshopguiplus.util.ItemBuilder;
import com.siliconstudios.dynamicshopguiplus.util.PermissionsCache;
import com.siliconstudios.dynamicshopguiplus.util.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class StandBrowser
extends ShopInventory {
    private final Inventory inv;
    private final Listen listener = new Listen();
    private final DynamicShopGUIPlus plugin;
    private final ArrayList<Stand> stands;
    private int page = 1;
    private final int allPages;

    public StandBrowser(DynamicShopGUIPlus plugin, Player p) {
        this.stands = plugin.getStandProvider().getStands().stream().sorted(Comparator.comparingInt(Stand::getId)).collect(Collectors.toCollection(ArrayList::new));
        int total = this.stands.size();
        this.allPages = (int)Math.ceil((double)total / 45.0);
        this.inv = plugin.getMetaUtils().createInventory(this, CalculateAmount.getSlots(total), Lang.SHOP_STAND_BROWSE_INVENTORY_TITLE.get());
        this.plugin = plugin;
        this.buildPane();
        this.openPage(1);
        Bukkit.getPluginManager().registerEvents((Listener)this.listener, (Plugin)this.plugin);
        p.openInventory(this.inv);
    }

    private void buildPane() {
        this.inv.setItem(this.inv.getSize() - 5, new ItemBuilder(XMaterial.COMPASS.parseItem()).withDisplayName(Lang.CURRENT_PAGE.get() + " " + this.page + "/" + this.allPages).build());
        this.inv.setItem(this.inv.getSize() - 6, new ItemBuilder(XMaterial.RED_STAINED_GLASS_PANE.parseItem()).withDisplayName(Lang.PREVIOUS_PAGE.get()).build());
        this.inv.setItem(this.inv.getSize() - 4, new ItemBuilder(XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE.parseItem()).withDisplayName(Lang.NEXT_PAGE.get()).build());
    }

    private void openPage(int page) {
        int index;
        for (int i = index = page * 45 - 45; i < index + Math.min(this.stands.size() - index, 45); ++i) {
            Stand stand = this.stands.get(i);
            this.inv.addItem(new ItemStack[]{new ItemBuilder(XMaterial.NETHER_STAR.parseItem()).withDisplayName(Lang.SHOP_STAND_INFO_ITEM_NAME.get()).withLore(Lang.SHOP_STAND_INFO_ITEM_LORE.get().replace("%id%", String.valueOf(stand.getId())).replace("%location%", stand.getLoc().toString()).replace("%item%", stand.getItem()).replace("%type%", stand.getType().name()).replace("%loaded%", String.valueOf(stand.isLoaded()))).build()});
        }
        this.page = page;
    }

    private void quit() {
        InventoryClickEvent.getHandlerList().unregister((Listener)this.listener);
    }

    private class Listen
    implements Listener {
        private Listen() {
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent e) {
            if (e.getClickedInventory() == null || !(e.getClickedInventory().getHolder() instanceof StandBrowser) || e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
                return;
            }
            e.setCancelled(true);
            Player p = (Player)e.getWhoClicked();
            if (e.getRawSlot() == StandBrowser.this.inv.getSize() - 6) {
                if (StandBrowser.this.page > 1) {
                    StandBrowser.this.inv.clear();
                    StandBrowser.this.openPage(StandBrowser.this.page - 1);
                    StandBrowser.this.buildPane();
                }
            } else if (e.getRawSlot() == StandBrowser.this.inv.getSize() - 4) {
                if (StandBrowser.this.page < StandBrowser.this.allPages) {
                    StandBrowser.this.inv.clear();
                    StandBrowser.this.openPage(StandBrowser.this.page + 1);
                    StandBrowser.this.buildPane();
                }
            } else {
                Stand stand = (Stand)StandBrowser.this.stands.get(StandBrowser.this.page * 45 - 45 + e.getRawSlot());
                if (stand != null) {
                    if (!PermissionsCache.hasPermission(p, "DynamicShopGUIPlus.eshop.shopstands.edit")) {
                        SendMessage.chatToPlayer(p, Lang.NO_PERMISSIONS.get());
                        return;
                    }
                    new StandManagement(StandBrowser.this.plugin, p, (Stand)StandBrowser.this.stands.get(StandBrowser.this.page * 45 - 45 + e.getRawSlot()));
                    StandBrowser.this.quit();
                }
            }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent e) {
            if (e.getInventory().getHolder() instanceof StandBrowser) {
                StandBrowser.this.quit();
            }
        }
    }
}

