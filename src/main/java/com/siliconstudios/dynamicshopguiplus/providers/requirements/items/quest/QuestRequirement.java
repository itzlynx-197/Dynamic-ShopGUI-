

package com.siliconstudios.dynamicshopguiplus.providers.requirements.items.quest;

import java.util.List;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.providers.RequirementManager;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.ItemRequirement;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.items.quest.QuestProvider;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.ItemRequirementLoadException;
import org.bukkit.entity.Player;

public class QuestRequirement
implements ItemRequirement {
    private final String questID;
    private final QuestProvider questProvider;
    private final RequirementManager.RequirementLore lore;

    public QuestRequirement(QuestProvider questProvider, String questID, List<String> lore) throws ItemRequirementLoadException {
        this.questID = questID;
        this.questProvider = questProvider;
        this.lore = RequirementManager.getLore(lore, Lang.QUEST_REQUIREMENT_LORE.get().toString(), new String[0]);
        if (this.questProvider.isReady()) {
            this.onLoad();
            if (!this.questProvider.isLoaded(questID)) {
                throw new ItemRequirementLoadException("Quest using ID '" + questID + "' isn't found");
            }
        }
    }

    public String getQuestID() {
        return this.questID;
    }

    public void onLoad() {
        this.lore.replace("%questName%", this.questProvider.getQuestName(this.questID));
    }

    @Override
    public boolean isMet(Player p) {
        return this.questProvider.hasCompleted(p, this.questID);
    }

    @Override
    public String[] getLore(Player p, boolean fast, boolean pr) {
        if (this.lore.isReplaced()) {
            return this.lore.get(p, fast, pr);
        }
        return this.lore.get(p, fast, pr, "%questName%", this.questProvider.getQuestName(this.questID));
    }

    @Override
    public void sendNotMetMessage(Player p) {
        SendMessage.chatToPlayer(p, Lang.QUEST_REQUIREMENT_NOT_MET.get(p).replace("%questName%", this.questProvider.getQuestName(this.questID)));
    }
}

