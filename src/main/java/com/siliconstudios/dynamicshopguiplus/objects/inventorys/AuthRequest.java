

package com.siliconstudios.dynamicshopguiplus.objects.inventorys;

import java.util.function.Consumer;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.methodes.CalculateAmount;
import com.siliconstudios.dynamicshopguiplus.objects.ShopInventory;
import com.siliconstudios.dynamicshopguiplus.util.ItemBuilder;
import com.siliconstudios.dynamicshopguiplus.util.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;

public class AuthRequest
extends ShopInventory {
    private final Inventory inv;
    private final Listen listener = new Listen();
    private final DynamicShopGUIPlus plugin = DynamicShopGUIPlus.getInstance();
    private final Consumer<AuthMethod> onComplete;

    public AuthRequest(Player p, Consumer<AuthMethod> onComplete) {
        this.inv = Bukkit.createInventory((InventoryHolder)this, (int)36, (String)"\u00a78\u00a7lAuthorization required");
        this.onComplete = onComplete;
        this.buildPane();
        Bukkit.getPluginManager().registerEvents((Listener)this.listener, (Plugin)this.plugin);
        p.openInventory(this.inv);
    }

    private void buildPane() {
        this.inv.setItem(12, new ItemBuilder(XMaterial.PAPER.parseItem()).withDisplayName("\u00a7a\u00a7lTemporary authorization").withLore(CalculateAmount.splitLongString("\u00a7cWhen using the temporary authorization method, the shop layout will be uploaded \u00a7c\u00a7lanomalously \u00a7r\u00a7cbut will only be available for 5 days only and removed after.")).build());
        this.inv.setItem(14, new ItemBuilder(XMaterial.PAPER.parseItem()).withDisplayName("\u00a79\u00a7lAuthorization via discord").withLore(CalculateAmount.splitLongString("\u00a7cWhen authorizing using discord, the shop layout will be linked to your discord account. \n \u00a7cThis allows you to update your existing layout and will be available until you remove the layout yourself.")).build());
        this.inv.setItem(22, new ItemBuilder(XMaterial.NETHER_STAR.parseItem()).withDisplayName("\u00a7c\u00a7lCancel upload process").withEnchantGlint().build());
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
            if (e.getClickedInventory() == null || e.getClickedInventory() != AuthRequest.this.inv || e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
                return;
            }
            e.setCancelled(true);
            if (e.getCurrentItem().getType() == ((AuthRequest)AuthRequest.this).plugin.createItem.fillItem.getType()) {
                return;
            }
            AuthMethod method = null;
            if (e.getRawSlot() == 12) {
                method = AuthMethod.TEMP;
            } else if (e.getRawSlot() == 14) {
                method = AuthMethod.DISCORD;
            }
            AuthRequest.this.quit();
            e.getWhoClicked().closeInventory();
            AuthRequest.this.onComplete.accept(method);
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent e) {
            if (e.getInventory() == AuthRequest.this.inv) {
                AuthRequest.this.quit();
            }
        }
    }

    public static enum AuthMethod {
        DISCORD,
        TEMP;

    }
}

