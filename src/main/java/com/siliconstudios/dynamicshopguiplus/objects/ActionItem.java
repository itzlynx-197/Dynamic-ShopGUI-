

package com.siliconstudios.dynamicshopguiplus.objects;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.objects.DisplayItem;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.ItemRequirement;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.RequirementType;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.Requirements;
import com.siliconstudios.dynamicshopguiplus.util.ChatUtil;
import com.siliconstudios.dynamicshopguiplus.util.SkullUtil;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.ItemLoadException;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class ActionItem
implements DisplayItem {
    private static final String[] actionPlaceholders = new String[]{"%player_name%", "%player_displayname%", "%player_level%", "%player_balance%", "%page%", "%pages%"};
    private ItemStack actionItem;
    private Action action;
    private boolean error = false;
    private boolean clickCommand;
    private Requirements requirements;
    private DisplayItem.DynamicLore lore;
    private final List<Integer> placeholders = new ArrayList<Integer>();
    private String displayname;
    private final String itemPath;
    public final String section;
    public final String itemLoc;

    public ActionItem(String section, String itemLoc) {
        this.section = section;
        this.itemLoc = itemLoc;
        this.itemPath = this.section + "." + this.itemLoc;
        ConfigurationSection conf = ConfigManager.getShop(this.section).getConfigurationSection("pages." + this.itemLoc);
        try {
            this.requirements = DynamicShopGUIPlus.getInstance().getRequirementManager().load(this.section, "pages." + this.itemLoc);
            try {
                this.action = Action.getFromString(conf.getString("action", "NONE"));
            }
            catch (IllegalArgumentException e) {
                throw new ItemLoadException(Lang.INVALID_ITEM_ACTION.get());
            }
            this.actionItem = DynamicShopGUIPlus.getInstance().createItem.loadActionItem(this);
            if (this.actionItem == null) {
                this.actionItem = DynamicShopGUIPlus.getInstance().createItem.getInvalidShopItem(this.section, this.itemLoc);
                this.error = true;
            } else if (this.actionItem.getType() != Material.AIR) {
                this.displayname = DynamicShopGUIPlus.getInstance().useItemName ? (this.actionItem.getItemMeta().hasDisplayName() ? DynamicShopGUIPlus.getInstance().getMetaUtils().getRawDisplayName(this.actionItem.getItemMeta()) : DynamicShopGUIPlus.getInstance().getMaterialName(this.actionItem.getType().name())) : DynamicShopGUIPlus.getInstance().getMaterialName(this.actionItem.getType().name());
                List<String> list = this.actionItem.hasItemMeta() ? DynamicShopGUIPlus.getInstance().getMetaUtils().getRawLore(this.actionItem.getItemMeta()) : null;
                this.lore = this.getDynamicLore(DynamicShopGUIPlus.getInstance(), list);
            }
        }
        catch (ItemLoadException e) {
            this.actionItem = DynamicShopGUIPlus.getInstance().createItem.getInvalidShopItem(this.section, this.itemLoc);
            this.error = true;
            SendMessage.warnMessage(e.getMessage());
            SendMessage.errorShops(this.section, this.itemLoc);
        }
        catch (Exception e) {
            this.actionItem = DynamicShopGUIPlus.getInstance().createItem.getInvalidShopItem(this.section, this.itemLoc);
            this.error = true;
            SendMessage.warnMessage("A unknown error occurred while loading item: " + this.itemPath);
            e.printStackTrace();
        }
    }

    public ActionItem(ActionItem actionItem) {
        this.itemPath = actionItem.itemPath;
        this.section = actionItem.section;
        this.itemLoc = actionItem.itemLoc;
        this.actionItem = actionItem.actionItem;
        this.action = actionItem.action;
        this.displayname = actionItem.displayname;
        this.clickCommand = actionItem.clickCommand;
        this.error = actionItem.error;
        this.lore = actionItem.lore;
        this.requirements = actionItem.requirements;
    }

    @Override
    public DisplayItem.DynamicLore getLore() {
        return this.lore;
    }

    @Override
    public String section() {
        return this.section;
    }

    @Override
    public String itemLoc() {
        return this.itemLoc;
    }

    @Override
    public String getItemPath() {
        return this.itemPath;
    }

    @Override
    public ItemStack getShopItem() {
        return this.actionItem;
    }

    public Action getAction() {
        return this.action;
    }

    public boolean hasPlaceholders() {
        return !this.placeholders.isEmpty();
    }

    public List<Integer> getPlaceholders() {
        return this.placeholders;
    }

    @Override
    public boolean meetsRequirements(Player p, boolean silent) {
        if (this.requirements == null) {
            return true;
        }
        for (ItemRequirement requirement : this.requirements) {
            if (requirement.isMet(p)) continue;
            if (!silent) {
                requirement.sendNotMetMessage(p);
            }
            return false;
        }
        return true;
    }

    @Override
    public void addItemRequirement(ItemRequirement requirement) {
        this.requirements.add(requirement);
    }

    @Override
    public void addItemRequirements(Collection<? extends ItemRequirement> requirements) {
        this.requirements.addAll(requirements);
    }

    @Override
    public Requirements getRequirements() {
        return this.requirements;
    }

    @Override
    public boolean hasItemRequirement(RequirementType type) {
        return this.requirements.hasType(type);
    }

    @Override
    public void removeItemRequirement(ItemRequirement requirement) {
        this.requirements.remove(requirement);
    }

    @Override
    public void removeItemRequirements(Collection<? extends ItemRequirement> requirements) {
        this.requirements.removeAll(requirements);
    }

    @Override
    public String getDisplayname() {
        return this.displayname;
    }

    @Override
    public boolean hasItemError() {
        return this.error;
    }

    @Override
    public void updateSkullTexture(GameProfile profile, boolean itg) {
        SkullMeta meta = (SkullMeta)this.actionItem.getItemMeta();
        SkullUtil.applySkullProfile(profile, meta);
        this.actionItem.setItemMeta((ItemMeta)meta);
    }

    private DisplayItem.DynamicLore getDynamicLore(DynamicShopGUIPlus plugin, List<String> lore) {
        return plugin.paperMeta ? new DynamicCompLore(lore) : new DynamicBukkitLore(plugin, lore);
    }

    public static enum Action {
        BACK,
        CLOSE,
        PAGE_BACK,
        PAGE_NEXT;


        public static Action getFromString(String s) throws IllegalArgumentException {
            if (s == null) {
                throw new IllegalArgumentException();
            }
            return Action.valueOf(s.toUpperCase(Locale.ENGLISH).replace(" ", "_"));
        }
    }

    public final class DynamicCompLore
    implements DisplayItem.DynamicLore {
        private final String[] lore;
        private final String[] compLore;

        public DynamicCompLore(List<String> list) {
            this.lore = list == null || list.isEmpty() ? new String[]{} : list.toArray(new String[0]);
            this.compLore = (String[])Arrays.stream(this.lore).map(ChatUtil::getGsonComponent).toArray(String[]::new);
            int i = 0;
            for (String s : this.compLore) {
                if (Arrays.stream(actionPlaceholders).anyMatch(s::contains)) {
                    ActionItem.this.placeholders.add(i);
                }
                ++i;
            }
        }

        @Override
        public String[] get(boolean fast, boolean pr) {
            return (String[])(fast ? this.compLore : this.lore).clone();
        }
    }

    private final class DynamicBukkitLore
    implements DisplayItem.DynamicLore {
        private final String[] lore;
        private final String[] fastLore;

        public DynamicBukkitLore(DynamicShopGUIPlus plugin, List<String> list) {
            this.lore = list == null || list.isEmpty() ? new String[]{} : (String[])list.stream().map(ChatUtil::formatColors).toArray(String[]::new);
            this.fastLore = (String[])Arrays.stream(this.lore).map(s -> plugin.versionHandler.toNBT((String)s)).toArray(String[]::new);
            int i = 0;
            for (String s2 : this.lore) {
                if (Arrays.stream(actionPlaceholders).anyMatch(s2::contains)) {
                    ActionItem.this.placeholders.add(i);
                }
                ++i;
            }
        }

        @Override
        public String[] get(boolean fast, boolean pr) {
            return (String[])(fast && !pr ? this.fastLore : this.lore).clone();
        }
    }
}

