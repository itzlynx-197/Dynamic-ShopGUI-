

package com.siliconstudios.dynamicshopguiplus.providers.requirements.items.quest;

import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.items.quest.QuestManager;
import org.bukkit.entity.Player;

public interface QuestProvider {
    public void init(DynamicShopGUIPlus var1, QuestManager var2);

    public boolean isReady();

    public String getName();

    public boolean isLoaded(String var1);

    public String getQuestName(String var1);

    public boolean hasCompleted(Player var1, String var2);
}

