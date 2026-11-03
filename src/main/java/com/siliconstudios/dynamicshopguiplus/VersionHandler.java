

package com.siliconstudios.dynamicshopguiplus;

import java.util.List;
import java.util.Optional;
import com.siliconstudios.dynamicshopguiplus.files.Translatable;
import com.siliconstudios.dynamicshopguiplus.util.PotionTypes;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface VersionHandler {
    public boolean hasData(ItemStack var1, String var2);

    public Object emptyList();

    public String getLoreAsNBT(ItemStack var1);

    public String toNBT(String var1);

    public Object getNMSLore(ItemStack var1);

    public ItemStack applyNMSLore(ItemStack var1, Object var2);

    public void setItemLore(Object var1, String var2, int var3);

    public void addItemLore(Object var1, String var2, int var3);

    public void addItemLore(Object var1, String var2);

    public ItemStack setNBTString(ItemStack var1, String var2, String var3);

    public String getNBTString(ItemStack var1, String var2);

    public ItemStack setNBTInt(ItemStack var1, String var2, Integer var3);

    public Integer getNBTInt(ItemStack var1, String var2);

    public boolean isSimilar(ItemStack var1, ItemStack var2, List<String> var3);

    public ItemStack setPages(ItemStack var1, String var2);

    public String getPages(ItemStack var1);

    public ItemStack setDisabledBackButton(ItemStack var1);

    public boolean isDisabledBackButton(ItemStack var1);

    public double getBuyPrice(ItemStack var1);

    public double getSellPrice(ItemStack var1);

    public ItemStack addNBTPrices(ItemStack var1, String var2);

    public ItemStack setSpawnerType(ItemStack var1, String var2);

    public ItemStack setPathToItem(ItemStack var1, String var2);

    public ItemStack setItemErrorToItem(ItemStack var1);

    public ItemStack setPotionType(ItemStack var1, PotionTypes var2);

    public Optional<PotionTypes> getBasePotion(ItemStack var1);

    public ItemStack getSpawnerToGive(EntityType var1);

    public String getPathToItem(ItemStack var1);

    public Boolean hasItemError(ItemStack var1);

    public String getSpawnerType(ItemStack var1);

    public ItemStack getItemInHand(Player var1);

    public String getTitle(Inventory var1);

    public void updateInventoryTitle(Player var1, Translatable var2);

    public ItemStack setOminousStrength(ItemStack item, Integer level);

    public ItemStack setComponent(ItemStack item, String component, Object value);
}

