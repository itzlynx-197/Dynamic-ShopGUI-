

package com.siliconstudios.dynamicshopguiplus.providers.requirements.items.region;

import java.util.HashMap;
import java.util.Map;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.items.region.RegionProvider;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.items.region.RegionRequirement;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.items.region.hooks.WorldGuardIntegration;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.ItemRequirementLoadException;
import org.bukkit.plugin.PluginManager;

public class RegionManager {
    private final RegionProvider provider;
    private final Map<RegionRequirement, String> requirements = new HashMap<RegionRequirement, String>();

    public RegionManager(DynamicShopGUIPlus plugin) {
        PluginManager pm = plugin.getServer().getPluginManager();
        this.provider = this.getProvider(plugin, pm);
        if (this.provider != null) {
            this.provider.init(plugin, this);
        }
    }

    private RegionProvider getProvider(DynamicShopGUIPlus plugin, PluginManager pm) {
        if (pm.getPlugin("WorldGuard") != null) {
            return new WorldGuardIntegration(plugin);
        }
        return null;
    }

    public RegionRequirement addRequirement(RegionRequirement requirement, String path) {
        this.requirements.put(requirement, path);
        return requirement;
    }

    public RegionProvider getProvider() throws ItemRequirementLoadException {
        if (this.provider != null) {
            return this.provider;
        }
        throw new ItemRequirementLoadException("Failed to enable integration with supported region plugin");
    }

    public void onLoad() {
        if (!this.provider.isReady()) {
            SendMessage.errorMessage("Failed to enable quest integration with '" + this.provider.getName() + "'");
        }
        for (RegionRequirement requirement : this.requirements.keySet()) {
            String path = this.requirements.get(requirement);
            if (!this.provider.isReady()) {
                SendMessage.warnMessage("Failed to load region item requirement: " + this.provider.getName() + " integration failed to load");
                SendMessage.errorShops(path.split("\\.")[0], path.split("\\.", 2)[1]);
                continue;
            }
            if (!this.provider.isLoaded(requirement.getRegionID())) {
                SendMessage.warnMessage("Issue while loading region item requirement: Region using ID '" + requirement.getRegionID() + "' was not found in " + this.provider.getName() + ", is this supposed to be a region which hasn't been created yet? Else the name of the region is incorrect, continuing anyway...");
                SendMessage.errorShops(path.split("\\.")[0], path.split("\\.", 2)[1]);
            }
            requirement.onLoad();
        }
    }
}

