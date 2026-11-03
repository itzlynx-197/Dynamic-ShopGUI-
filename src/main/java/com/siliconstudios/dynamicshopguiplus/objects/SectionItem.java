

package com.siliconstudios.dynamicshopguiplus.objects;

import java.util.HashSet;
import java.util.Set;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.ItemLoadException;
import org.bukkit.inventory.ItemStack;

public class SectionItem {
    private final boolean hideIfNoPerms;
    private final Set<Integer> slots = new HashSet<Integer>();
    private ItemStack item;
    private String bedrockTexture;
    public String section;

    public SectionItem(DynamicShopGUIPlus plugin, String section) {
        this.section = section;
        try {
            this.item = plugin.createItem.loadSectionItem(this.section, "item");
        }
        catch (ItemLoadException e) {
            SendMessage.warnMessage(e.getMessage());
            SendMessage.errorSections(section, "item");
            this.item = plugin.createItem.getInvalidItem(e);
        }
        this.bedrockTexture = ConfigManager.getSection(section).getString("item.bedrock-texture");
        this.hideIfNoPerms = ConfigManager.getSection(section).getBoolean("hide-if-no-perms");
        if (ConfigManager.getSection(this.section).getBoolean("display-item")) {
            plugin.removeShopSection(section);
        }
    }

    public void addSlot(int slot) {
        this.slots.add(slot);
    }

    public Set<Integer> getSlots() {
        return this.slots;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public String getBedrockTexture() {
        return this.bedrockTexture;
    }

    public boolean isHideIfNoPerms() {
        return this.hideIfNoPerms;
    }
}

