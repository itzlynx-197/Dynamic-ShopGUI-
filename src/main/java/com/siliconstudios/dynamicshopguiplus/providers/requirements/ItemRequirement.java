

package com.siliconstudios.dynamicshopguiplus.providers.requirements;

import org.bukkit.entity.Player;

public interface ItemRequirement {
    public boolean isMet(Player var1);

    public String[] getLore(Player var1, boolean var2, boolean var3);

    public void sendNotMetMessage(Player var1);
}

