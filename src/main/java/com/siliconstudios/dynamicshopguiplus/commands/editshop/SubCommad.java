

package com.siliconstudios.dynamicshopguiplus.commands.editshop;

import java.util.List;
import org.bukkit.command.CommandSender;

public abstract class SubCommad {
    public abstract String getName();

    public abstract String getDescription();

    public abstract String getSyntax();

    public abstract boolean hasPermission(CommandSender var1);

    public abstract void perform(Object var1, String[] var2);

    public abstract List<String> getTabCompletion(String[] var1);
}

