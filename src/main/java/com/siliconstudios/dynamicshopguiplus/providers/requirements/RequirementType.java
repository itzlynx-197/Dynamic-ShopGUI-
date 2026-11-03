

package com.siliconstudios.dynamicshopguiplus.providers.requirements;

import com.siliconstudios.dynamicshopguiplus.providers.requirements.ItemRequirement;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.items.TimeRequirement;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.items.quest.QuestRequirement;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.items.region.RegionRequirement;

public enum RequirementType {
    TIME(TimeRequirement.class, "time-requirement-not-met"),
    QUEST(QuestRequirement.class, "quest-requirement-not-met"),
    REGION(RegionRequirement.class, "region-requirement-not-met");

    private final String lorePath;
    private final Class<? extends ItemRequirement> clazz;

    private RequirementType(Class<? extends ItemRequirement> clazz, String lorePath) {
        this.clazz = clazz;
        this.lorePath = "display-lore-layout.item-requirements." + lorePath;
    }

    public Class<? extends ItemRequirement> getClazz() {
        return this.clazz;
    }

    public String getLorePath() {
        return this.lorePath;
    }
}

