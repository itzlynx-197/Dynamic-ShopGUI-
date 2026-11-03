

package com.siliconstudios.dynamicshopguiplus.providers;

import javax.annotation.Nullable;
import com.siliconstudios.dynamicshopguiplus.objects.ShopItem;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public interface SpawnerProvider {
    public String getProviderName();

    public ItemStack setSpawnedType(@Nullable ShopItem var1, ItemStack var2, EntityType var3);

    public boolean isShopSpawner(ItemStack var1, ItemStack var2);

    public Double getSpawnerSellPrice(ItemStack var1, double var2);

    public String getSpawnedType(ItemStack var1);
}

