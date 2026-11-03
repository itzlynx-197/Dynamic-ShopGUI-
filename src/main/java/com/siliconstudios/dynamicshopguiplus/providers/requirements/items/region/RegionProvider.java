

package com.siliconstudios.dynamicshopguiplus.providers.requirements.items.region;

import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.items.region.RegionManager;
import org.bukkit.entity.Player;

public interface RegionProvider {
    public void init(DynamicShopGUIPlus var1, RegionManager var2);

    public boolean isReady();

    public String getName();

    public boolean isLoaded(String var1);

    public String getRegionName(String var1);

    public boolean isInsideRegion(Player var1, String var2);
}

