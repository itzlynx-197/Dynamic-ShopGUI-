

package com.siliconstudios.dynamicshopguiplus.commands.base;

import java.util.List;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.commands.base.PluginCommand;

public abstract class PluginCommands
extends PluginCommand {
    public PluginCommands(DynamicShopGUIPlus plugin, String commandName, String description, String usageMessage, String permission, List<String> aliases, List<String> disabledWorlds) {
        super(plugin, commandName, description, usageMessage, permission, disabledWorlds);
        this.setAliases(aliases);
    }
}

