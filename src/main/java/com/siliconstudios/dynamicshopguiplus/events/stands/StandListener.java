

package com.siliconstudios.dynamicshopguiplus.events.stands;

import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.objects.ShopItem;
import com.siliconstudios.dynamicshopguiplus.objects.TransactionMenu;
import com.siliconstudios.dynamicshopguiplus.objects.inventorys.StandManagement;
import com.siliconstudios.dynamicshopguiplus.objects.stands.ChunkLoc;
import com.siliconstudios.dynamicshopguiplus.objects.stands.Stand;
import com.siliconstudios.dynamicshopguiplus.objects.stands.StandLoc;
import com.siliconstudios.dynamicshopguiplus.objects.stands.StandType;
import com.siliconstudios.dynamicshopguiplus.providers.StandProvider;
import com.siliconstudios.dynamicshopguiplus.util.PermissionsCache;
import com.siliconstudios.dynamicshopguiplus.util.Transaction;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.StandLoadException;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class StandListener
implements Listener {
    private final DynamicShopGUIPlus plugin;
    private final StandProvider provider;

    public StandListener(DynamicShopGUIPlus plugin, StandProvider provider) {
        this.plugin = plugin;
        this.provider = provider;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        this.provider.loadStands(new ChunkLoc(e.getChunk()));
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {
        this.provider.checkAndUnload(new ChunkLoc(e.getChunk()));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!StandType.isValidType(event.getBlock())) {
            return;
        }
        Stand stand = this.provider.getStand(new StandLoc(event.getBlock()));
        if (stand == null) {
            return;
        }
        Player player = event.getPlayer();
        if (!PermissionsCache.hasPermission(player, "DynamicShopGUIPlus.eshop.shopstands.edit")) {
            SendMessage.chatToPlayer(player, Lang.NO_PERMISSIONS.get());
            return;
        }
        new StandManagement(this.plugin, player, stand);
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        StandType type = StandType.fromType(event.getBlock());
        if (type == null) {
            return;
        }
        String item = this.plugin.versionHandler.getNBTString(event.getItemInHand(), "shopstand_item");
        if (item == null) {
            return;
        }
        Player player = event.getPlayer();
        if (!PermissionsCache.hasPermission(player, "DynamicShopGUIPlus.eshop.shopstands.create")) {
            SendMessage.chatToPlayer(player, Lang.NO_PERMISSIONS.get());
            return;
        }
        try {
            this.provider.createStand(event.getBlock(), item, type);
            SendMessage.chatToPlayer(player, Lang.CREATED_NEW_SHOP_STAND.get().replace("%item%", item));
        }
        catch (StandLoadException e) {
            SendMessage.chatToPlayer(player, Lang.ERROR_CREATING_NEW_SHOP_STAND.get().replace("%reason%", e.getMessage()));
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof ArmorStand) || !(e.getDamager() instanceof Player)) {
            return;
        }
        ArmorStand clicked = (ArmorStand)e.getEntity();
        Stand stand = this.provider.getStand(clicked.getUniqueId());
        if (stand == null) {
            return;
        }
        ShopItem shopItem = this.plugin.getShopItem(stand.getItem());
        if (shopItem == null) {
            SendMessage.warnMessage(Lang.CANNOT_OPEN_SHOP_STAND.get().replace("%player%", e.getEntity().getName()).replace("%item%", stand.getItem()));
            return;
        }
        Player player = (Player)e.getDamager();
        if (player.isSneaking() && PermissionsCache.hasPermission(player, "DynamicShopGUIPlus.eshop.shopstands.edit")) {
            new StandManagement(this.plugin, player, stand);
        } else if (shopItem.getBuyPrice() >= 0.0) {
            if (!this.isAllowedGamemode(player)) {
                return;
            }
            if (!PermissionsCache.hasPermission(player, "DynamicShopGUIPlus.shop." + stand.getItem().split("\\.")[0])) {
                SendMessage.chatToPlayer(player, Lang.NO_PERMISSIONS.get());
            }
            if (!shopItem.meetsRequirements(player, false)) {
                return;
            }
            new TransactionMenu(player, shopItem, null, true, Transaction.Mode.BUY, Transaction.Type.SHOPSTAND_BUY_SCREEN, 1).open();
        } else {
            SendMessage.chatToPlayer(player, Lang.CANNOT_PURCHASE_ITEM.get());
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        if (!(e.getRightClicked() instanceof ArmorStand)) {
            return;
        }
        ArmorStand clicked = (ArmorStand)e.getRightClicked();
        Stand stand = this.provider.getStand(clicked.getUniqueId());
        if (stand == null) {
            return;
        }
        ShopItem shopItem = this.plugin.getShopItem(stand.getItem());
        if (shopItem == null) {
            SendMessage.warnMessage(Lang.CANNOT_OPEN_SHOP_STAND.get().replace("%player%", e.getPlayer().getName()).replace("%item%", stand.getItem()));
            return;
        }
        Player player = e.getPlayer();
        if (!this.isAllowedGamemode(player)) {
            return;
        }
        if (!PermissionsCache.hasPermission(player, "DynamicShopGUIPlus.shop." + stand.getItem().split("\\.")[0])) {
            SendMessage.chatToPlayer(player, Lang.NO_PERMISSIONS.get());
        }
        if (!shopItem.meetsRequirements(player, false)) {
            return;
        }
        if (!(shopItem.getSellPrice() >= 0.0)) {
            SendMessage.chatToPlayer(player, Lang.CANNOT_SELL_ITEM.get());
            return;
        }
        new TransactionMenu(player, shopItem, null, true, Transaction.Mode.SELL, Transaction.Type.SHOPSTAND_SELL_SCREEN, 1).open();
    }

    private boolean isAllowedGamemode(Player player) {
        if (this.plugin.bannedGamemodes.contains(player.getGameMode()) && !PermissionsCache.hasPermission(player, "DynamicShopGUIPlus.bypassgamemode")) {
            SendMessage.chatToPlayer(player, Lang.CANNOT_ENTER_SHOP_BANNED_GAMEMODE.get().replace("%gamemode%", player.getGameMode().name().toLowerCase()));
            return false;
        }
        return true;
    }
}

