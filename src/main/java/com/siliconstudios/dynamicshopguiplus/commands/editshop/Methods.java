

package com.siliconstudios.dynamicshopguiplus.commands.editshop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.util.XMaterial;
import org.bukkit.ChatColor;

public class Methods {
    private final DynamicShopGUIPlus plugin;

    public Methods(DynamicShopGUIPlus plugin) {
        this.plugin = plugin;
    }

    public String getSection(Object logger, String section) {
        if (this.plugin.getShopSections().contains(section)) {
            return section;
        }
        SendMessage.sendMessage(logger, Lang.NO_SHOP_FOUND.get());
        return null;
    }

    public String getMaterial(Object logger, String material) {
        Optional<XMaterial> mat = XMaterial.matchXMaterial(material);
        if (mat.isPresent()) {
            if (mat.get().parseItem() != null) {
                return material;
            }
            SendMessage.warnMessage(logger, Lang.MATERIAL_NOT_SUPPORTED.get());
        } else {
            SendMessage.warnMessage(logger, Lang.ITEM_MATERIAL_NULL.get().replace("%material%", ChatColor.DARK_RED + material));
        }
        return null;
    }

    public Double getPrice(Object logger, Object price) {
        try {
            return Double.parseDouble(price.toString());
        }
        catch (NumberFormatException e) {
            SendMessage.sendMessage(logger, Lang.NO_VALID_AMOUNT.get());
            return null;
        }
    }

    public String getShopItemLoc(Object logger, String section, String itemLoc) {
        if (this.plugin.getSection(section).getItemLocs().contains(itemLoc)) {
            if (this.plugin.getSection(section).isActionItem(section, itemLoc)) {
                SendMessage.sendMessage(logger, Lang.CANNOT_DO_THAT_ACTION_ITEM.get());
                return null;
            }
            return itemLoc;
        }
        SendMessage.sendMessage(logger, Lang.SECTION_DOES_NOT_CONTAIN_ITEM.get().replace("%shopsection%", section).replace("%itemLoc%", itemLoc));
        return null;
    }

    public String getItemLoc(Object logger, String section, String itemLoc) {
        if (this.plugin.getSection(section).getItemLocs().contains(itemLoc)) {
            return itemLoc;
        }
        SendMessage.sendMessage(logger, Lang.SECTION_DOES_NOT_CONTAIN_ITEM.get().replace("%shopsection%", section).replace("%itemLoc%", itemLoc));
        return null;
    }

    public final List<String> getExampleRGBColors() {
        return Arrays.asList("0x0000FF", "0x008000", "0xFF0000", "0xFFFF00");
    }

    public final List<String> getExampleItemNames() {
        return Arrays.asList("&aGreen carpet", "&bDiamond", "&c&lRedstone dust", "&0Bedrock", "#979797Gunpowder", "&aRed flower");
    }

    public Integer getMainMenuSlot(Object logger, String value) {
        try {
            int slot = Integer.parseInt(value);
            if (this.plugin.getMainMenuItemSlots().containsKey(slot)) {
                SendMessage.sendMessage(logger, Lang.SLOT_ALREADY_IN_USE.get().replace("%available-slots%", Arrays.toString(this.getAvailableMainMenuSlots().toArray())));
                return null;
            }
            return slot;
        }
        catch (NumberFormatException e) {
            SendMessage.sendMessage(logger, Lang.SLOT_NEEDS_TO_BE_NUMBER.get());
            return null;
        }
    }

    public List<String> getAvailableMainMenuSlots() {
        ArrayList<String> slots = new ArrayList<String>();
        for (int i2 = 1; i2 < 46; ++i2) {
            slots.add(String.valueOf(i2));
        }
        this.plugin.getMainMenuItemSlots().keySet().forEach(i -> slots.remove(String.valueOf(i)));
        return slots;
    }
}

