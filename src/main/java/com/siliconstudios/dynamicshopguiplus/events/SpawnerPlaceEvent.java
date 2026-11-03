

package com.siliconstudios.dynamicshopguiplus.events;

import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.util.XMaterial;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class SpawnerPlaceEvent
implements Listener {
    private final boolean self;
    private final DynamicShopGUIPlus plugin;

    public SpawnerPlaceEvent(DynamicShopGUIPlus plugin) {
        this.plugin = plugin;
        this.self = ConfigManager.getConfig().getBoolean("only-mine-plugin-spawners");
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (e.getBlock().getType() != XMaterial.SPAWNER.parseMaterial()) {
            return;
        }
        if (!DynamicShopGUIPlus.getInstance().getSpawnerManager().getProvider().getProviderName().equalsIgnoreCase("DEFAULT")) {
            return;
        }
        Block b = e.getBlock();
        String spawnertype = DynamicShopGUIPlus.getInstance().versionHandler.getSpawnerType(e.getItemInHand());
        if (spawnertype != null) {
            EntityType type = EntityType.valueOf((String)spawnertype.toUpperCase());
            CreatureSpawner spawner = (CreatureSpawner)b.getState();
            if (this.self && this.plugin.version >= 114) {
                spawner.getPersistentDataContainer().set(new NamespacedKey((Plugin)this.plugin, "SpawnerType"), PersistentDataType.STRING, type.name());
            }
            spawner.setSpawnedType(type);
            spawner.update();
        }
    }
}

