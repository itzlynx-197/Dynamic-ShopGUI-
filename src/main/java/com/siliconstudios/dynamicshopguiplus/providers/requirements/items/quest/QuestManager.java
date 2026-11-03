

package com.siliconstudios.dynamicshopguiplus.providers.requirements.items.quest;

import java.util.HashMap;
import java.util.Map;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.items.quest.QuestProvider;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.items.quest.QuestRequirement;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.ItemRequirementLoadException;
import org.bukkit.plugin.PluginManager;

public class QuestManager {
    private final QuestProvider provider;
    private final Map<QuestRequirement, String> requirements = new HashMap<QuestRequirement, String>();

    public QuestManager(DynamicShopGUIPlus plugin) {
        PluginManager pm = plugin.getServer().getPluginManager();
        this.provider = this.initProvider(plugin, pm);
    }

    private QuestProvider initProvider(DynamicShopGUIPlus plugin, PluginManager pm) {
        return null;
    }

    public QuestRequirement addRequirement(QuestRequirement requirement, String path) {
        this.requirements.put(requirement, path);
        return requirement;
    }

    public QuestProvider getProvider() throws ItemRequirementLoadException {
        if (this.provider != null) {
            return this.provider;
        }
        throw new ItemRequirementLoadException("Failed to enable integration with supported Quest plugin");
    }

    public void onLoad() {
        if (!this.provider.isReady()) {
            SendMessage.errorMessage("Failed to enable quest integration with '" + this.provider.getName() + "'");
        }
        for (QuestRequirement requirement : this.requirements.keySet()) {
            String path = this.requirements.get(requirement);
            if (!this.provider.isReady()) {
                SendMessage.warnMessage("Failed to load quest item requirement: Quest integration failed to load");
                SendMessage.errorShops(path.split("\\.")[0], path.split("\\.", 2)[1]);
                continue;
            }
            if (!this.provider.isLoaded(requirement.getQuestID())) {
                SendMessage.warnMessage("Failed to load quest item requirement: Quest using ID '" + requirement.getQuestID() + "' does not exist in " + this.provider.getName());
                SendMessage.errorShops(path.split("\\.")[0], path.split("\\.", 2)[1]);
                continue;
            }
            requirement.onLoad();
        }
    }
}

