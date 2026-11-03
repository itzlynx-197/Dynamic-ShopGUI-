

package com.siliconstudios.dynamicshopguiplus.events;

import java.util.List;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.util.ItemBuilder;
import com.siliconstudios.dynamicshopguiplus.util.XMaterial;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class SpawnerBreakEvent
implements Listener {
    private final boolean self;
    private final DynamicShopGUIPlus plugin;
    private final List<ItemStack> allowedTools;
    private final boolean dropEXP;
    private final boolean interact;

    public SpawnerBreakEvent(DynamicShopGUIPlus plugin) {
        this.plugin = plugin;
        this.allowedTools = plugin.createItem.getAllowedTools();
        this.dropEXP = ConfigManager.getConfig().getBoolean("player-placed-spawners-drop-exp");
        this.self = ConfigManager.getConfig().getBoolean("only-mine-plugin-spawners");
        this.interact = ConfigManager.getConfig().getBoolean("allow-spawner-type-change");
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (e.getBlock().getType() != XMaterial.SPAWNER.parseMaterial()) {
            return;
        }
        if (!this.plugin.getSpawnerManager().getProvider().getProviderName().equalsIgnoreCase("DEFAULT")) {
            return;
        }
        Block b = e.getBlock();
        Player player = e.getPlayer();
        CreatureSpawner spawnertype = (CreatureSpawner)b.getState();
        if (this.self && !this.isSelf(spawnertype)) {
            return;
        }
        if (this.isAllowedTool(this.plugin.versionHandler.getItemInHand(player))) {
            if (!this.dropEXP) {
                e.setExpToDrop(0);
            }
            this.giveSpawner(player, spawnertype.getSpawnedType());
        } else {
            SendMessage.logDebugMessage("Player " + player.getName() + " tried to mine a spawner with a not allowed spawner-break-tool: " + this.plugin.versionHandler.getItemInHand(player).toString());
        }
    }

    private boolean isAllowedTool(ItemStack itemInHand) {
        for (ItemStack allowedTool : this.allowedTools) {
            if (!itemInHand.getType().equals((Object)allowedTool.getType()) || !this.hasEnchants(allowedTool, itemInHand)) continue;
            return true;
        }
        return false;
    }

    private boolean isSelf(CreatureSpawner spawner) {
        return this.plugin.version >= 114 && spawner.getPersistentDataContainer().has(new NamespacedKey((Plugin)this.plugin, "SpawnerType"), PersistentDataType.STRING);
    }

    private boolean hasEnchants(ItemStack allowedTool, ItemStack itemInHand) {
        for (Enchantment ench : allowedTool.getEnchantments().keySet()) {
            if (itemInHand.getEnchantmentLevel(ench) >= allowedTool.getEnchantmentLevel(ench)) continue;
            return false;
        }
        return true;
    }

    private void giveSpawner(Player player, EntityType spawnerentity) {
        if (spawnerentity == null) {
            return;
        }
        ItemStack spawner = this.plugin.versionHandler.getSpawnerToGive(spawnerentity);
        if (!this.interact) {
            spawner = new ItemBuilder(spawner).hideInteractLore().build();
        }
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(new ItemStack[]{spawner});
            SendMessage.chatToPlayer(player, Lang.SPAWNER_BROKE_AND_APPLIED_TO_INVENTORY.get(player).replace("%spawnertype%", spawner.getItemMeta().getDisplayName()).replace("%spawner-type%", spawner.getItemMeta().getDisplayName()));
        } else {
            Location loc = player.getLocation();
            World world = player.getWorld();
            world.dropItem(loc, spawner);
        }
    }
}

