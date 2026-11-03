

package com.siliconstudios.dynamicshopguiplus.events;

import com.siliconstudios.dynamicshopguiplus.util.XMaterial;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SpawnerInteractEvent
implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            return;
        }
        if (e.getClickedBlock() == null || e.getClickedBlock().getType() != XMaterial.SPAWNER.parseMaterial()) {
            return;
        }
        if (!this.isSpawnEgg(e.getPlayer().getItemInHand())) {
            return;
        }
        e.setCancelled(true);
    }

    private boolean isSpawnEgg(ItemStack item) {
        return XMaterial.isNewVersion() ? item.getType().name().endsWith("SPAWN_EGG") : item.getType().name().contains("MONSTER_EGG");
    }
}

