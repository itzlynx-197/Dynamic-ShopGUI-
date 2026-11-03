package com.siliconstudios.dynamicshopguiplus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.siliconstudios.dynamicshopguiplus.files.Translatable;
import com.siliconstudios.dynamicshopguiplus.util.PotionTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;
public class DefaultVersionHandler
implements VersionHandler {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    @Override
    public boolean hasData(ItemStack item, String key) {
        return false;
    }

    @Override
    public Object emptyList() {
        return new ArrayList<String>();
    }

    @Override
    public String getLoreAsNBT(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        boolean first = true;
        for (Component c : meta.lore()) {
            if (!first) {
                out.append('\n');
            }
            first = false;
            out.append(LEGACY.serialize(c));
        }
        return out.toString();
    }

    @Override
    public String toNBT(String s) {
        return s;
    }

    @Override
    public Object getNMSLore(ItemStack item) {
        return null;
    }

    @Override
    public ItemStack applyNMSLore(ItemStack stack, Object lore) {
        try {
            ItemMeta meta = stack.getItemMeta();
            if (meta == null) {
                return stack;
            }
            List<Component> components = new ArrayList<Component>();
            for (Object o : (List<?>) lore) {
                components.add(Component.text(String.valueOf(o)));
            }
            meta.lore(components);
            stack.setItemMeta(meta);
        } catch (Exception ignored) {}
        return stack;
    }

    @Override
    public void setItemLore(Object lore, String s, int i) {
        try {
            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) lore;
            if (i < 0 || i >= list.size()) {
                list.add(s);
            } else {
                list.set(i, s);
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void addItemLore(Object lore, String s) {
        try {
            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) lore;
            list.add(s);
        } catch (Exception ignored) {}
    }

    @Override
    public void addItemLore(Object lore, String s, int i) {
        try {
            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) lore;
            if (i < 0 || i >= list.size()) {
                list.add(s);
            } else {
                list.add(i, s);
            }
        } catch (Exception ignored) {}
    }

    @Override
    public ItemStack setNBTString(ItemStack item, String key, String value) {
        return item;
    }

    @Override
    public String getNBTString(ItemStack item, String key) {
        return null;
    }

    @Override
    public ItemStack setNBTInt(ItemStack item, String key, Integer value) {
        return item;
    }

    @Override
    public Integer getNBTInt(ItemStack item, String key) {
        return null;
    }

    @Override
    public boolean isSimilar(ItemStack itemOne, ItemStack itemTwo, List<String> ignoredNBTData) {
        if (itemOne == null || itemTwo == null) {
            return false;
        }
        return itemOne.isSimilar(itemTwo);
    }

    @Override
    public ItemStack setPages(ItemStack item, String pages) {
        return item;
    }

    @Override
    public String getPages(ItemStack item) {
        return null;
    }

    @Override
    public ItemStack setDisabledBackButton(ItemStack item) {
        return item;
    }

    @Override
    public boolean isDisabledBackButton(ItemStack item) {
        return false;
    }

    @Override
    public double getBuyPrice(ItemStack item) {
        return 0.0;
    }

    @Override
    public double getSellPrice(ItemStack item) {
        return 0.0;
    }

    @Override
    public ItemStack addNBTPrices(ItemStack item, String path) {
        return item;
    }

    @Override
    public ItemStack setSpawnerType(ItemStack item, String type) {
        return item;
    }

    @Override
    public ItemStack setPathToItem(ItemStack item, String path) {
        return item;
    }

    @Override
    public ItemStack setItemErrorToItem(ItemStack item) {
        return item;
    }

    @Override
    public ItemStack setPotionType(ItemStack item, PotionTypes type) {
        return item;
    }

    @Override
    public Optional<PotionTypes> getBasePotion(ItemStack item) {
        return Optional.empty();
    }

    @Override
    public ItemStack getSpawnerToGive(EntityType type) {
        return new ItemStack(Material.SPAWNER);
    }

    @Override
    public String getPathToItem(ItemStack item) {
        return null;
    }

    @Override
    public Boolean hasItemError(ItemStack item) {
        return false;
    }

    @Override
    public String getSpawnerType(ItemStack item) {
        return null;
    }

    @Override
    public ItemStack getItemInHand(Player player) {
        return player.getInventory().getItemInMainHand();
    }

    @Override
    public String getTitle(Inventory inventory) {
        return null;
    }

    public ItemStack setOminousStrength(ItemStack item, Integer level) {
        try {
            org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                item.setItemMeta(meta);
            }
        }
        catch (Exception ignored) {}
        return item;
    }

    public ItemStack setComponent(ItemStack item, String component, Object value) {
        try {
            if ("max_stack_size".equals(component) && value instanceof Number) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    item.setItemMeta(meta);
                }
            }
        }
        catch (Exception ignored) {}
        return item;
    }

    @Override
    public void updateInventoryTitle(Player player, Translatable title) {
        try {
            ItemFlag flag = ItemFlag.valueOf("HIDE_ATTRIBUTES");
            player.getInventory().getItemInMainHand().getItemMeta().addItemFlags(new ItemFlag[]{flag});
        } catch (Exception ignored) {}
    }
}
