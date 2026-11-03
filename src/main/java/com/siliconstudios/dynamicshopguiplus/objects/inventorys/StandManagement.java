

package com.siliconstudios.dynamicshopguiplus.objects.inventorys;

import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.objects.ShopInventory;
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
import org.bukkit.plugin.Plugin;

public class StandManagement
extends ShopInventory {
    private final Inventory inv;
    private final Listen listener = new Listen();
    private final DynamicShopGUIPlus plugin;
    private final Stand stand;

    public StandManagement(DynamicShopGUIPlus plugin, Player p, Stand stand) {
        this.inv = plugin.getMetaUtils().createInventory(this, 9, Lang.SHOP_STANDS_MANAGEMENT_INVENTORY_TITLE.get());
        this.plugin = plugin;
        this.stand = stand;
        this.buildPane();
        Bukkit.getPluginManager().registerEvents((Listener)this.listener, (Plugin)this.plugin);
        p.openInventory(this.inv);
    }

    private void buildPane() {
        this.inv.setItem(0, new ItemBuilder(XMaterial.NETHER_STAR.parseItem()).withDisplayName(Lang.SHOP_STAND_INFO_ITEM_NAME.get()).withLore(Lang.SHOP_STAND_INFO_ITEM_LORE.get().replace("%id%", String.valueOf(this.stand.getId())).replace("%location%", this.stand.getLoc().toString()).replace("%item%", this.stand.getItem()).replace("%type%", this.stand.getType().name()).replace("%loaded%", String.valueOf(this.stand.isLoaded()))).build());
        this.inv.setItem(7, new ItemBuilder(Material.REDSTONE).withDisplayName(Lang.FORCE_RELOAD_STAND_ITEM_NAME.get()).withLore(Lang.FORCE_RELOAD_STAND_ITEM_LORE.get()).build());
        this.inv.setItem(8, new ItemBuilder(XMaterial.BARRIER.parseItem()).withDisplayName(Lang.DESTROY_STAND_ITEM_NAME.get()).withLore(Lang.DESTROY_STAND_ITEM_LORE.get()).build());
        this.fillEmpty();
    }

    private void fillEmpty() {
        if (this.plugin.createItem.fillItem == null) {
            return;
        }
        for (int i = 0; i < this.inv.getSize(); ++i) {
            if (this.inv.getItem(i) != null) continue;
            this.inv.setItem(i, this.plugin.createItem.fillItem);
        }
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
            if (e.getClickedInventory() == null || !(e.getClickedInventory().getHolder() instanceof StandManagement) || e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
                return;
            }
            e.setCancelled(true);
            Player p = (Player)e.getWhoClicked();
            if (e.getRawSlot() == 7) {
                if (StandManagement.this.plugin.getStandProvider().reloadStand(StandManagement.this.stand)) {
                    SendMessage.chatToPlayer(p, Lang.RELOADED_SHOP_STAND.get().replace("%id%", String.valueOf(StandManagement.this.stand.getId())));
                }
            } else if (e.getRawSlot() == 8) {
                if (!PermissionsCache.hasPermission(p, "DynamicShopGUIPlus.eshop.shopstands.destroy")) {
                    SendMessage.chatToPlayer(p, Lang.NO_PERMISSIONS.get());
                    return;
                }
                StandManagement.this.plugin.getStandProvider().destroy(StandManagement.this.stand);
                SendMessage.chatToPlayer(p, Lang.REMOVED_SHOP_STAND.get().replace("%location%", StandManagement.this.stand.getLoc().toString()));
            } else {
                return;
            }
            StandManagement.this.quit();
            e.getWhoClicked().closeInventory();
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent e) {
            if (e.getInventory().getHolder() instanceof StandManagement) {
                StandManagement.this.quit();
            }
        }
    }
}

