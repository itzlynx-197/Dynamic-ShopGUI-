

package com.siliconstudios.dynamicshopguiplus.providers.requirements.items.region;

import java.util.List;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.providers.RequirementManager;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.ItemRequirement;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.items.region.RegionProvider;
import org.bukkit.entity.Player;

public class RegionRequirement
implements ItemRequirement {
    private final String regionID;
    private final RegionProvider regionProvider;
    private final RequirementManager.RequirementLore lore;

    public RegionRequirement(RegionProvider regionProvider, String regionID, List<String> lore) {
        this.regionID = regionID;
        this.regionProvider = regionProvider;
        this.lore = RequirementManager.getLore(lore, Lang.REGION_REQUIREMENT_LORE.get().toString(), new String[0]);
        if (this.regionProvider.isReady()) {
            this.onLoad();
        }
    }

    public String getRegionID() {
        return this.regionID;
    }

    public void onLoad() {
        if (!this.regionProvider.isLoaded(this.regionID)) {
            SendMessage.logDebugMessage("Region using ID '" + this.regionID + "' wasn't found yet after provider enabled, is this supposed to be a region which isn't created yet? Else the name of the region is incorrect for the item requirement");
            return;
        }
        String regionName = this.regionProvider.getRegionName(this.regionID);
        this.lore.replace("%regionName%", regionName);
    }

    @Override
    public boolean isMet(Player p) {
        return this.regionProvider.isInsideRegion(p, this.regionID);
    }

    @Override
    public String[] getLore(Player p, boolean fast, boolean pr) {
        if (this.lore.isReplaced()) {
            return this.lore.get(p, fast, pr);
        }
        return this.lore.get(p, fast, pr, "%regionName%", this.regionProvider.getRegionName(this.regionID));
    }

    @Override
    public void sendNotMetMessage(Player p) {
        SendMessage.chatToPlayer(p, Lang.REGION_REQUIREMENT_NOT_MET.get(p).replace("%regionName%", this.regionProvider.getRegionName(this.regionID)));
    }
}

