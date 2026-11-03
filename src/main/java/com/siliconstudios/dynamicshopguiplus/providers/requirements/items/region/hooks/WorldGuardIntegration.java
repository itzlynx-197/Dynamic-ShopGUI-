

package com.siliconstudios.dynamicshopguiplus.providers.requirements.items.region.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.items.region.RegionManager;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.items.region.RegionProvider;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WorldGuardIntegration
implements RegionProvider {
    private final DynamicShopGUIPlus plugin;

    public WorldGuardIntegration(DynamicShopGUIPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init(DynamicShopGUIPlus plugin, RegionManager manager) {
        if (!this.isReady()) {
            SendMessage.infoMessage(this.getName() + " found, waiting...");
            plugin.runTaskLater(() -> {
                if (plugin.getServer().getPluginManager().isPluginEnabled(this.getName())) {
                    SendMessage.infoMessage(Lang.ENABLED_PLUGIN_HOOK.get().replace("%plugin%", this.getName()));
                } else {
                    SendMessage.warnMessage(Lang.FAILED_PLUGIN_INTEGRATION.get().replace("%plugin%", this.getName()));
                }
                manager.onLoad();
            }, 5L);
        } else {
            SendMessage.infoMessage(Lang.ENABLED_PLUGIN_HOOK.get().replace("%plugin%", this.getName()));
        }
    }

    @Override
    public boolean isReady() {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        for (World world : this.plugin.getServer().getWorlds()) {
            com.sk89q.worldguard.protection.managers.RegionManager regions = container.get(BukkitAdapter.adapt((World)world));
            if (regions == null) continue;
            return true;
        }
        return false;
    }

    @Override
    public String getName() {
        return "WorldGuard";
    }

    @Override
    public boolean isLoaded(String regionID) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        for (World world : this.plugin.getServer().getWorlds()) {
            com.sk89q.worldguard.protection.managers.RegionManager regions = container.get(BukkitAdapter.adapt((World)world));
            if (regions == null || !regions.hasRegion(regionID)) continue;
            return true;
        }
        return false;
    }

    @Override
    public String getRegionName(String regionID) {
        return regionID;
    }

    @Override
    public boolean isInsideRegion(Player p, String regionID) {
        com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt((Location)p.getLocation());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        com.sk89q.worldguard.protection.managers.RegionManager regions = container.get(BukkitAdapter.adapt((World)p.getWorld()));
        if (regions == null) {
            return false;
        }
        ApplicableRegionSet set = regions.getApplicableRegions(loc.toVector().toBlockPoint());
        for (ProtectedRegion region : set.getRegions()) {
            if (!region.getId().equalsIgnoreCase(regionID)) continue;
            return true;
        }
        return false;
    }
}

