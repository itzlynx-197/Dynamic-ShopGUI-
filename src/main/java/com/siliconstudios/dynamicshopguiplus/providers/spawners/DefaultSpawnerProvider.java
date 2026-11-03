

package com.siliconstudios.dynamicshopguiplus.providers.spawners;

import javax.annotation.Nullable;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.events.SpawnerBreakEvent;
import com.siliconstudios.dynamicshopguiplus.events.SpawnerInteractEvent;
import com.siliconstudios.dynamicshopguiplus.events.SpawnerPlaceEvent;
import com.siliconstudios.dynamicshopguiplus.objects.ShopItem;
import com.siliconstudios.dynamicshopguiplus.providers.SpawnerManager;
import com.siliconstudios.dynamicshopguiplus.providers.SpawnerProvider;
import com.siliconstudios.dynamicshopguiplus.util.NBTMaps;
import com.siliconstudios.dynamicshopguiplus.util.ServerInfo;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class DefaultSpawnerProvider
implements SpawnerProvider {
    private final DynamicShopGUIPlus plugin;
    private final boolean interact;

    public DefaultSpawnerProvider(DynamicShopGUIPlus plugin, @Nullable SpawnerInteractEvent interactEvent, @Nullable SpawnerPlaceEvent placeEvent, @Nullable SpawnerBreakEvent breakEvent) {
        this.plugin = plugin;
        if (placeEvent != null) {
            this.plugin.getServer().getPluginManager().registerEvents((Listener)placeEvent, (Plugin)this.plugin);
        }
        if (breakEvent != null) {
            this.plugin.getServer().getPluginManager().registerEvents((Listener)breakEvent, (Plugin)this.plugin);
        }
        if (interactEvent != null) {
            this.plugin.getServer().getPluginManager().registerEvents((Listener)interactEvent, (Plugin)this.plugin);
        }
        this.interact = interactEvent == null;
    }

    @Override
    public String getProviderName() {
        return "DEFAULT";
    }

    @Override
    public ItemStack setSpawnedType(@Nullable ShopItem shopItem, ItemStack item, EntityType type) {
        if (shopItem == null) {
            item = this.plugin.versionHandler.setSpawnerType(item, type.name());
        }
        ItemMeta meta = item.getItemMeta();
        if (!this.interact) {
            this.hideInteractLore(meta);
        }
        if (!meta.hasDisplayName()) {
            DynamicShopGUIPlus.getInstance().getMetaUtils().setDisplayName(meta, SpawnerManager.getDefaultName(type));
        }
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public boolean isShopSpawner(ItemStack itemToMatch, ItemStack shopItem) {
        return this.plugin.isSimilar(itemToMatch, shopItem, NBTMaps.getTags(false, "display::Name"));
    }

    @Override
    public Double getSpawnerSellPrice(ItemStack itemToSearch, double baseSellPrice) {
        return baseSellPrice;
    }

    @Override
    public String getSpawnedType(ItemStack item) {
        return this.plugin.versionHandler.getSpawnerType(item);
    }

    private void hideInteractLore(ItemMeta meta) {
        if (ServerInfo.getVersion().newerOrEqualAs(ServerInfo.Version.v1_21_R4) && !ServerInfo.supportsPaper()) {
            ItemFlag flag = ItemFlag.valueOf((String)"HIDE_BLOCK_ENTITY_DATA");
            meta.addItemFlags(new ItemFlag[]{flag});
        } else {
            meta.addItemFlags(ItemFlag.values());
        }
    }
}

