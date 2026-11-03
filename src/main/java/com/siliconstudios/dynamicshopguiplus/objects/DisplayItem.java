

package com.siliconstudios.dynamicshopguiplus.objects;

import com.mojang.authlib.GameProfile;
import java.util.Collection;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.ItemRequirement;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.RequirementType;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.Requirements;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface DisplayItem {
    public String section();

    public String itemLoc();

    public String getItemPath();

    public String getDisplayname();

    public ItemStack getShopItem();

    public Requirements getRequirements();

    public boolean meetsRequirements(Player var1, boolean var2);

    public boolean hasItemRequirement(RequirementType var1);

    public void addItemRequirement(ItemRequirement var1);

    public void removeItemRequirement(ItemRequirement var1);

    public void addItemRequirements(Collection<? extends ItemRequirement> var1);

    public void removeItemRequirements(Collection<? extends ItemRequirement> var1);

    public boolean hasItemError();

    public void updateSkullTexture(GameProfile var1, boolean var2);

    public DynamicLore getLore();

    public static interface DynamicLore {
        public String[] get(boolean var1, boolean var2);
    }
}

