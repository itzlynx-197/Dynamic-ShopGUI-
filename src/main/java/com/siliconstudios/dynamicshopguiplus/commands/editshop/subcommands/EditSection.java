

package com.siliconstudios.dynamicshopguiplus.commands.editshop.subcommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.Methods;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.SubCommad;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.CreateItemMethodes;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.util.EcoType;
import com.siliconstudios.dynamicshopguiplus.util.EconomyType;
import com.siliconstudios.dynamicshopguiplus.util.PermissionsCache;
import com.siliconstudios.dynamicshopguiplus.util.PotionTypes;
import com.siliconstudios.dynamicshopguiplus.util.XMaterial;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.ItemLoadException;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.util.StringUtil;

public class EditSection
extends SubCommad {
    private final Methods methods;
    private final DynamicShopGUIPlus plugin;
    private final List<String> allKeys = Arrays.asList("enable", "item.material", "item.stack-size", "item.hideDefaultLore", "item.armor-trim.type", "item.armor-trim.pattern", "item.name", "item.displayname", "slot", "item.enchantment-glint", "item.skullowner", "item.armorcolor", "hidden", "title", "sub-section", "item.potion-glow", "economy", "display-item");
    private final List<String> listKeys = Arrays.asList("item.lore", "item.potiontypes");
    private final List<String> actions = Arrays.asList("add", "set", "remove");
    private String section;
    private String action;
    private String key;
    private Object value;

    public EditSection(DynamicShopGUIPlus plugin, Methods methods) {
        this.plugin = plugin;
        this.methods = methods;
    }

    @Override
    public String getName() {
        return "editsection";
    }

    @Override
    public String getDescription() {
        return Lang.EDITSHOP_EDIT_SECTION_SUBCOMMAND_DESC.get().getLegacy();
    }

    @Override
    public String getSyntax() {
        return Lang.EDITSHOP_EDIT_SECTION_SUBCOMMAND_SYNTAX.get().getLegacy();
    }

    @Override
    public boolean hasPermission(CommandSender source) {
        return PermissionsCache.hasPermission(source, "DynamicShopGUIPlus.eshop." + this.getName());
    }

    @Override
    public void perform(Object logger, String[] args) {
        if (args.length > 1) {
            this.section = this.methods.getSection(logger, args[1]);
            if (this.section == null) {
                return;
            }
        } else {
            SendMessage.sendMessage(logger, this.getSyntax());
            return;
        }
        if (args.length > 2) {
            this.action = args[2].toLowerCase(Locale.ENGLISH);
            if (!this.actions.contains(this.action)) {
                SendMessage.sendMessage(logger, Lang.NO_VALID_ACTION.get().replace("%action%", args[2]).replace("%actions%", Arrays.toString(this.actions.toArray())));
                return;
            }
        } else {
            SendMessage.sendMessage(logger, this.getSyntax());
            return;
        }
        if (args.length > 3) {
            this.key = args[3];
            if (this.getAvailableKeys().stream().noneMatch(this.key::equalsIgnoreCase)) {
                SendMessage.sendMessage(logger, Lang.NO_VALID_KEY.get().replace("%key%", args[3]).replace("%keys%", Arrays.toString(this.getAvailableKeys().toArray())));
                return;
            }
            if (this.action.equalsIgnoreCase("remove")) {
                if (this.listKeys.stream().noneMatch(this.key::equalsIgnoreCase)) {
                    this.value = null;
                    if (!this.validate(logger)) {
                        return;
                    }
                    this.updateOption(logger);
                    return;
                }
            }
        } else {
            SendMessage.sendMessage(logger, this.getSyntax());
            return;
        }
        if (args.length > 4) {
            StringBuilder value2 = new StringBuilder();
            for (int i = 4; i < args.length; ++i) {
                value2.append(args[i]).append(" ");
            }
            this.value = value2.substring(0, value2.length() - 1);
            if (!this.validate(logger)) {
                return;
            }
        } else {
            SendMessage.sendMessage(logger, this.getSyntax());
            return;
        }
        this.updateOption(logger);
    }

    @Override
    public List<String> getTabCompletion(String[] args) {
        switch (args.length) {
            case 2: {
                if (!args[1].isEmpty()) {
                    ArrayList<String> completions = new ArrayList<String>();
                    StringUtil.copyPartialMatches((String)args[1], this.plugin.getShopSections(), completions);
                    Collections.sort(completions);
                    return completions;
                }
                return this.plugin.getShopSections();
            }
            case 3: {
                if (!args[2].isEmpty()) {
                    ArrayList<String> completions = new ArrayList<String>();
                    StringUtil.copyPartialMatches((String)args[2], this.actions, completions);
                    Collections.sort(completions);
                    return completions;
                }
                return this.actions;
            }
            case 4: {
                if (!this.isSection(args[1]) || !this.isAction(args[2])) {
                    return null;
                }
                if (!args[3].isEmpty()) {
                    ArrayList<String> completions = new ArrayList<String>();
                    StringUtil.copyPartialMatches((String)args[3], this.getAvailableKeys(), completions);
                    Collections.sort(completions);
                    return completions;
                }
                return this.getAvailableKeys();
            }
            case 5: {
                if (!args[4].isEmpty()) {
                    ArrayList<String> completions = new ArrayList<String>();
                    StringUtil.copyPartialMatches((String)args[4], this.getTabcompletionForValue(args[1], args[2], args[3]), completions);
                    Collections.sort(completions);
                    return completions;
                }
                return this.getTabcompletionForValue(args[1], args[2], args[3]);
            }
        }
        return null;
    }

    private boolean isSection(String section) {
        if (this.plugin.getShopSections().contains(section)) {
            this.section = section;
            return true;
        }
        return false;
    }

    private boolean isAction(String action) {
        if (this.actions.contains(action)) {
            this.action = action;
            return true;
        }
        return false;
    }

    private boolean isKey(String key) {
        if (this.getAvailableKeys().contains(key)) {
            this.key = key;
            return true;
        }
        return false;
    }

    private List<String> getExamplePlayerNames() {
        ArrayList<String> playerNames = new ArrayList<String>();
        this.plugin.getServer().getOnlinePlayers().forEach(p -> playerNames.add(p.getName()));
        return playerNames;
    }

    private List<String> getTabcompletionForValue(String section, String action, String key) {
        String[] parts;
        if (!(this.isSection(section) && this.isAction(action) && this.isKey(key))) {
            return new ArrayList<String>();
        }
        if (action.equals("remove") && this.listKeys.contains(key)) {
            if (this.listKeys.contains(key)) {
                return this.getValues(section, key);
            }
            return Collections.singletonList(ConfigManager.getSection(section).get(key, "").toString());
        }
        switch (key) {
            case "item.material": {
                return this.plugin.getSupportedMatNames();
            }
            case "item.name": 
            case "item.displayname": 
            case "item.lore": 
            case "title": {
                return Arrays.asList("#A1672D&lWood", "&1&lArmor", "#1B22E8&lF#1BAAE8&li#1BE8E1&lr#1BE896&le#1BE829&lw#E8DA1B&lo#E88F1B&lr#EB0909&lk#EB09DC&ls", "&7&lStones", "#09EBDC&lCustom#89EB09&lItems");
            }
            case "item.armorcolor": {
                return this.methods.getExampleRGBColors();
            }
            case "item.stack-size": {
                ArrayList<String> array = new ArrayList<String>();
                for (int i = 1; i < 64; ++i) {
                    array.add(String.valueOf(i));
                }
                return array;
            }
            case "item.potiontypes": {
                return PotionTypes.getNames();
            }
            case "item.skullowner": {
                return this.getExamplePlayerNames();
            }
            case "item.enchantment-glint": 
            case "item.potion-glow": 
            case "item.hideDefaultLore": 
            case "sub-section": 
            case "display-item": {
                return Arrays.asList("true", "false");
            }
            case "item.armor-trim.type": {
                return new CreateItemMethodes.ArmorTrim().getTypes();
            }
            case "item.armor-trim.pattern": {
                return new CreateItemMethodes.ArmorTrim().getPatterns();
            }
            case "economy": {
                return Arrays.asList("ULTRA_ECONOMY:Gems", "ULTRA_ECONOMY:Crystals", "VAULT", "GEMS_ECONOMY:Sticks", "PLAYER_POINTS");
            }
            case "slot": {
                return this.methods.getAvailableMainMenuSlots();
            }
        }
        if (key.startsWith("pages") && (parts = key.split("\\.")).length >= 2) {
            switch (parts[2]) {
                case "gui-rows": {
                    return Arrays.asList("1", "2", "3", "4", "5", "6");
                }
                case "title": {
                    return Arrays.asList("&8&lTools", "&d&lWools", "#34ebb4Utilities", "&f&lBuildingBlocks", "<blue><bold>TradingItems");
                }
            }
        }
        return new ArrayList<String>();
    }

    
    private boolean validate(Object logger) {
        if (this.action.equalsIgnoreCase("remove") && !this.listKeys.contains(this.key) && (this.key.equalsIgnoreCase("item.material") || this.key.equalsIgnoreCase("enable"))) {
            SendMessage.sendMessage(logger, Lang.ACTION_NOT_VALID_ON_KEY.get().replace("%action%", this.action).replace("%key%", this.key));
            return false;
        }
        if (this.value == null) return true;
        String materialName = ConfigManager.getSection(this.section).getString("item.material");
        if (materialName != null && !materialName.isEmpty()) {
            Optional<XMaterial> xMaterial = XMaterial.matchXMaterial(materialName);
            if (xMaterial.isPresent() && xMaterial.get().parseItem() != null) {
                Material mat = xMaterial.get().parseItem().getType();
                switch (this.key) {
                    case "item.material": {
                        if (this.methods.getMaterial(logger, this.value.toString()) == null) return false;
                        return true;
                    }
                    case "item.name": 
                    case "item.displayname": 
                    case "item.lore": 
                    case "title": {
                        return true;
                    }
                    case "slot": {
                        this.value = this.methods.getMainMenuSlot(logger, this.value.toString());
                        if (this.value == null) return false;
                        return true;
                    }
                    case "item.armor-trim.type": {
                        if (!(new ItemStack(mat).getItemMeta() instanceof ArmorMeta)) {
                            SendMessage.warnMessage(logger, "To add a armor trim to an item, the material needs to be a armor piece.");
                            return false;
                        }
                        try {
                            new CreateItemMethodes.ArmorTrim().getTrimType(this.value.toString());
                            return true;
                        }
                        catch (ItemLoadException e) {
                            SendMessage.warnMessage(logger, e.getMessage());
                            return false;
                        }
                    }
                    case "item.armor-trim.pattern": {
                        if (!(new ItemStack(mat).getItemMeta() instanceof ArmorMeta)) {
                            SendMessage.warnMessage(logger, "To add a armor trim to an item, the material needs to be a armor piece.");
                            return false;
                        }
                        try {
                            new CreateItemMethodes.ArmorTrim().getTrimPattern(this.value.toString());
                            return true;
                        }
                        catch (ItemLoadException e) {
                            SendMessage.warnMessage(logger, e.getMessage());
                            return false;
                        }
                    }
                    case "economy": {
                        EcoType type = EconomyType.getFromString(this.value.toString());
                        if (type != null) return true;
                        SendMessage.warnMessage(logger, "Could not find a economy type such as '" + this.value.toString() + "', valid values are: " + Arrays.stream(EconomyType.values()).filter(t -> t != EconomyType.ITEM && t != EconomyType.LEVELS).map(Enum::name).collect(Collectors.joining(",")));
                        return false;
                    }
                    case "item.enchantment-glint": 
                    case "item.potion-glow": 
                    case "item.hideDefaultLore": 
                    case "item.sub-section": 
                    case "display-item": 
                    case "hidden": 
                    case "enable": {
                        this.value = Boolean.valueOf(this.value.toString());
                        return true;
                    }
                    case "item.armorcolor": {
                        try {
                            Color.fromRGB((int)Integer.parseInt(this.value.toString()));
                            return true;
                        }
                        catch (IllegalArgumentException e) {
                            SendMessage.warnMessage(logger, Lang.RGB_COLOR_FORMATTED_WRONG.get().replace("%path%", "ShopSections." + this.section + "." + this.key));
                            return false;
                        }
                    }
                    case "item.stack-size": {
                        try {
                            this.value = Integer.parseInt(this.value.toString());
                        }
                        catch (NumberFormatException e) {
                            SendMessage.warnMessage(logger, Lang.NO_VALID_AMOUNT.get());
                            return false;
                        }
                        if (!this.key.equalsIgnoreCase("item.stack-size")) return true;
                        if (this.plugin.allowIllegalStacks) return true;
                        if ((Integer)this.value >= mat.getMaxStackSize()) return true;
                        SendMessage.warnMessage(logger, "Cannot set the stack size of item %material% to %stack-size% because it would exceed the vanilla max stack size.".replace("%material%", mat.toString()).replace("%stack-size%", this.value.toString()));
                        return false;
                    }
                    case "item.potiontypes": {
                        if (!PotionTypes.canHaveEffects(mat)) {
                            SendMessage.warnMessage(logger, Lang.MATERIAL_NEEDS_TO_BE_POTION.get());
                            return false;
                        }
                        Optional<PotionTypes> pot = PotionTypes.matchPotionType(this.value.toString());
                        if (!pot.isPresent()) {
                            SendMessage.warnMessage(logger, Lang.ITEM_POTIONTYPE_NULL.get());
                            return false;
                        }
                        if (pot.get().isSupported()) return true;
                        SendMessage.warnMessage(logger, Lang.POTIONTYPE_NOT_SUPPORTED.get());
                        return false;
                    }
                    case "item.skullowner": {
                        if (mat == XMaterial.PLAYER_HEAD.parseMaterial()) return true;
                        SendMessage.warnMessage(logger, Lang.MATERIAL_NEEDS_TO_BE_SKULL.get());
                        return false;
                    }
                }
                if (!this.key.startsWith("pages")) return true;
                String[] parts = this.key.split("\\.");
                if (parts.length < 2) return true;
                switch (parts[2]) {
                    case "gui-rows": {
                        try {
                            this.value = Integer.parseInt(this.value.toString());
                        }
                        catch (NumberFormatException e) {
                            SendMessage.warnMessage(logger, Lang.NO_VALID_AMOUNT.get());
                            return false;
                        }
                        if ((Integer)this.value >= 1) {
                            if ((Integer)this.value <= 6) return true;
                        }
                        SendMessage.warnMessage(logger, "Invalid inventory rows amount, should be a number between 1-6");
                        return false;
                    }
                    case "title": {
                        return true;
                    }
                }
                return true;
            }
            SendMessage.sendMessage(logger, Lang.EDIT_ITEM_MATERIAL_INVALID.get().replace("%material%", materialName));
            return false;
        }
        SendMessage.sendMessage(logger, Lang.EDIT_ITEM_MATERIAL_INVALID.get().replace("%material%", materialName));
        return false;
    }

    private List<String> getAvailableKeys() {
        try {
            ArrayList<String> availableKeys = new ArrayList<String>(this.allKeys);
            if (this.action.equalsIgnoreCase("add")) {
                availableKeys.removeAll(this.getDefined());
                availableKeys.addAll(this.listKeys);
                return availableKeys;
            }
            if (this.action.equalsIgnoreCase("remove")) {
                List<String> definedKeys = this.getDefined();
                definedKeys.addAll(this.getPageKeys());
                return definedKeys;
            }
            if (this.action.equalsIgnoreCase("set")) {
                ArrayList<String> l = new ArrayList<String>(this.allKeys);
                l.addAll(this.getPageKeys());
                l.addAll(this.listKeys);
                return l;
            }
        }
        catch (NullPointerException nullPointerException) {
            
        }
        return new ArrayList<String>();
    }

    private List<String> getDefined() {
        ArrayList<String> defined = new ArrayList<String>(ConfigManager.getSection(this.section).getKeys(false));
        if (defined.contains("item")) {
            defined.addAll(ConfigManager.getSection(this.section).getConfigurationSection("item").getKeys(false));
        }
        return defined;
    }

    private List<String> getPageKeys() {
        ArrayList<String> keys = new ArrayList<String>();
        for (String page : this.plugin.getConfigManager().getPages(this.section, true)) {
            keys.addAll(Arrays.asList("pages." + page + ".title", "pages." + page + ".gui-rows"));
        }
        return keys;
    }

    private List<String> getValues(String section, String key) {
        return ConfigManager.getSection(section).getStringList(key);
    }

    private void updateOption(Object logger) {
        SendMessage.sendMessage(logger, Lang.EDITSHOP_EDITING_SECTION.get());
        if (this.listKeys.contains(this.key)) {
            if (this.action.equals("add")) {
                List<String> values = this.getValues(this.section, this.key);
                values.add(this.value.toString());
                this.update(values);
            } else if (this.action.equalsIgnoreCase("set")) {
                this.update(Collections.singletonList(this.value.toString()));
            } else if (this.action.equalsIgnoreCase("remove")) {
                List<String> values = this.getValues(this.section, this.key);
                if (values.size() == 1) {
                    this.update(null);
                } else if (values.size() >= 1) {
                    values.remove(this.value.toString());
                    this.update(values);
                }
            }
        } else {
            this.update(this.value);
        }
        SendMessage.sendMessage(logger, Lang.EDITSHOP_EDIT_ITEM_SUCCESSFUL.get().replace("%itemPath%", this.key));
        SendMessage.sendMessage(logger, Lang.EDITSHOP_RELOAD_SHOP_TO_SEE_CHANGES.get());
    }

    private <O> void update(O value) {
        if (this.key.startsWith("pages")) {
            ConfigManager.getShop(this.section).set(this.key, value);
            ConfigManager.saveShop(this.section);
        } else {
            ConfigManager.getSection(this.section).set(this.key, value);
            ConfigManager.saveSection(this.section);
        }
    }
}

