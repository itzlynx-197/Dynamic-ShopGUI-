

package com.siliconstudios.dynamicshopguiplus.objects.navbar;

import com.siliconstudios.dynamicshopguiplus.util.EcoType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface NavItem {
    public boolean isDisabled();

    public boolean isOnlyGeyser();

    public boolean isStatic(int var1, int var2);

    public String getAction();

    public String getAction(int var1, int var2);

    public ItemStack getItem(Player var1, EcoType var2);

    public ItemStack getItem(Player var1, EcoType var2, int var3, int var4);

    public String getBedrockTexture();

    public String getBedrockTexture(int var1, int var2);
}

