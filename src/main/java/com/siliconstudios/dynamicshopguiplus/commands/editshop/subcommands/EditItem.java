

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
import com.siliconstudios.dynamicshopguiplus.objects.ActionItem;
import com.siliconstudios.dynamicshopguiplus.util.FireworkUtil;
import com.siliconstudios.dynamicshopguiplus.util.PermissionsCache;
import com.siliconstudios.dynamicshopguiplus.util.PotionTypes;
import com.siliconstudios.dynamicshopguiplus.util.XEnchantment;
import com.siliconstudios.dynamicshopguiplus.util.XMaterial;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.ItemLoadException;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.util.StringUtil;

public class EditItem
extends SubCommad {
    private final Methods methods;
    private final DynamicShopGUIPlus plugin;
    private final List<String> allKeys = this.getAllKeys();
    private final List<String> actionKeys = Arrays.asList("material", "displayname", "name", "slot", "stack-size", "action", "skullowner", "armorcolor", "enchantment-glint", "stack-size");
    private final List<String> availableActions = this.getAvailableActions();
    String section;
    String itemLoc;
    String action;
    String key;
    Object value;
    private final List<String> listKeys = this.getListKeys();

    public EditItem(DynamicShopGUIPlus plugin, Methods methods) {
        this.plugin = plugin;
        this.methods = methods;
    }

    @Override
    public String getName() {
        return "edititem";
    }

    @Override
    public String getDescription() {
        return Lang.EDITSHOP_EDIT_ITEM_SUBCOMMAND_DESC.get().getLegacy();
    }

    @Override
    public String getSyntax() {
        return Lang.EDITSHOP_EDIT_ITEM_SUBCOMMAND_SYNTAX.get().getLegacy();
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
            this.itemLoc = this.methods.getItemLoc(logger, this.section, args[2]);
            if (this.itemLoc == null) {
                return;
            }
        } else {
            SendMessage.sendMessage(logger, this.getSyntax());
            return;
        }
        if (args.length > 3) {
            boolean found = false;
            for (String action2 : this.availableActions) {
                if (!args[3].equalsIgnoreCase(action2)) continue;
                this.action = action2;
                found = true;
                break;
            }
            if (!found) {
                SendMessage.sendMessage(logger, Lang.NO_VALID_ACTION.get().replace("%action%", args[3]).replace("%actions%", Arrays.toString(this.availableActions.toArray())));
                return;
            }
        } else {
            SendMessage.sendMessage(logger, this.getSyntax());
            return;
        }
        if (args.length > 4) {
            this.key = args[4];
            if (this.getAvailableKeys().stream().noneMatch(this.key::equalsIgnoreCase)) {
                SendMessage.sendMessage(logger, Lang.NO_VALID_KEY.get().replace("%key%", args[4]).replace("%keys%", Arrays.toString(this.getAvailableKeys().toArray())));
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
        if (args.length > 5) {
            if (args.length > 6) {
                StringBuilder value2 = new StringBuilder();
                for (int i = 5; i < args.length; ++i) {
                    value2.append(args[i]).append(" ");
                }
                this.value = value2.substring(0, value2.length() - 1);
            } else {
                this.value = args[5];
            }
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
                if (!this.isSection(args[1])) {
                    return null;
                }
                if (!args[2].isEmpty()) {
                    ArrayList<String> completions = new ArrayList<String>();
                    StringUtil.copyPartialMatches((String)args[2], this.plugin.getSection(args[1]).getItemLocs(), completions);
                    Collections.sort(completions);
                    return completions;
                }
                return this.plugin.getSection(args[1]).getItemLocs();
            }
            case 4: {
                if (!args[3].isEmpty()) {
                    ArrayList<String> completions = new ArrayList<String>();
                    StringUtil.copyPartialMatches((String)args[3], this.getAvailableActions(), completions);
                    Collections.sort(completions);
                    return completions;
                }
                return this.getAvailableActions();
            }
            case 5: {
                if (!(this.isSection(args[1]) && this.isItemLoc(args[2]) && this.isAction(args[3]))) {
                    return null;
                }
                if (!args[4].isEmpty()) {
                    ArrayList<String> completions = new ArrayList<String>();
                    StringUtil.copyPartialMatches((String)args[4], this.getAvailableKeys(), completions);
                    Collections.sort(completions);
                    return completions;
                }
                return this.getAvailableKeys();
            }
            case 6: {
                if (!this.isKey(args[4])) {
                    return null;
                }
                if (!args[5].isEmpty()) {
                    ArrayList<String> completions = new ArrayList<String>();
                    StringUtil.copyPartialMatches((String)args[5], this.getTabcompletionForValue(args[1], args[2], args[3], args[4]), completions);
                    Collections.sort(completions);
                    return completions;
                }
                return this.getTabcompletionForValue(args[1], args[2], args[3], args[4]);
            }
        }
        return null;
    }

    private boolean isSection(String section2) {
        if (this.plugin.getShopSections().contains(section2)) {
            this.section = section2;
            return true;
        }
        return false;
    }

    private boolean isItemLoc(String itemLoc2) {
        if (this.plugin.getSection(this.section).getItemLocs().contains(itemLoc2)) {
            this.itemLoc = itemLoc2;
            return true;
        }
        return false;
    }

    private boolean isAction(String action2) {
        if (this.availableActions.contains(action2)) {
            this.action = action2;
            return true;
        }
        return false;
    }

    private boolean isKey(String key2) {
        if (this.getAvailableKeys().contains(key2)) {
            this.key = key2;
            return true;
        }
        return false;
    }

    
    private boolean validate(Object logger) {
        if (this.action.equalsIgnoreCase("add")) {
            if (!this.getAvailableKeys().contains(this.key)) {
                SendMessage.sendMessage(logger, Lang.KEY_ALREADY_DEFINED.get().replace("%key%", this.key));
                return false;
            }
        } else if (this.action.equalsIgnoreCase("remove") && !this.getAvailableKeys().contains(this.key)) {
            SendMessage.sendMessage(logger, Lang.KEY_IS_NOT_FOUND.get().replace("%key%", this.key));
            return false;
        }
        if ((this.action.equalsIgnoreCase("remove") || this.action.equalsIgnoreCase("add")) && !this.listKeys.contains(this.key) && this.key.equalsIgnoreCase("material")) {
            SendMessage.sendMessage(logger, Lang.ACTION_NOT_VALID_ON_KEY.get().replace("%action%", this.action).replace("%key%", this.key));
            return false;
        }
        if (this.plugin.getSection(this.section).isActionItem(this.section, this.itemLoc)) {
            ArrayList<String> keys = new ArrayList<String>(this.actionKeys);
            keys.addAll(Arrays.asList("lore", "displaylore", "enchantments", "potiontypes", "item-flags"));
            if (keys.stream().noneMatch(s -> s.equals(this.key))) {
                SendMessage.sendMessage(logger, Lang.EDIT_KEY_UNAVAILABLE.get().replace("%key%", this.key));
                return false;
            }
        }
        if (this.value == null) return true;
        String materialName = ConfigManager.getShop(this.section).getString("pages." + this.itemLoc + ".material");
        if (materialName != null && !materialName.isEmpty()) {
            Optional<XMaterial> xMaterial = XMaterial.matchXMaterial(materialName);
            if (xMaterial.isPresent() && xMaterial.get().parseItem() != null) {
                Material mat = xMaterial.get().parseItem().getType();
                switch (this.key) {
                    case "material": {
                        if (this.action.equalsIgnoreCase("remove") || this.action.equalsIgnoreCase("add")) {
                            SendMessage.sendMessage(logger, Lang.ACTION_NOT_VALID_ON_KEY.get().replace("%action%", this.action).replace("%key%", this.key));
                            return false;
                        }
                        if (this.methods.getMaterial(logger, this.value.toString()) == null) return false;
                        return true;
                    }
                    case "displayname": 
                    case "name": 
                    case "lore": 
                    case "displaylore": {
                        return true;
                    }
                    case "buy": 
                    case "sell": {
                        this.value = this.methods.getPrice(logger, this.value);
                        if (this.value == null) return false;
                        return true;
                    }
                    case "slot": {
                        try {
                            int slot = Integer.parseInt(this.value.toString());
                            if (this.plugin.maxShopSize * 45 < slot) throw new NumberFormatException();
                            if (1 > slot) {
                                throw new NumberFormatException();
                            }
                            this.value = slot;
                            return true;
                        }
                        catch (NumberFormatException e) {
                            SendMessage.sendMessage(logger, Lang.NO_VALID_SLOT_FOR_ITEM.get().replace("%slot%", this.value.toString()).replace("%maxShopSize%", String.valueOf(this.plugin.maxShopSize * 45)));
                            return false;
                        }
                    }
                    case "ominous-strength": {
                        try {
                            int str = Integer.parseInt(this.value.toString());
                            if (5 <= str) throw new NumberFormatException();
                            if (1 >= str) {
                                throw new NumberFormatException();
                            }
                            this.value = str;
                            return true;
                        }
                        catch (NumberFormatException e) {
                            SendMessage.sendMessage(logger, Lang.INVALID_OMINOUS_BOTTLE_STRENGTH.get().replace("%strength%", this.value.toString()));
                            return false;
                        }
                    }
                    case "enchantments": {
                        String enchantmentName = this.value.toString().contains(":") ? this.value.toString().split(":")[0] : this.value.toString();
                        Optional<XEnchantment> ench = XEnchantment.matchXEnchantment(enchantmentName);
                        if (!ench.isPresent()) {
                            SendMessage.warnMessage(logger, Lang.ITEM_ENCHANTMENT_NULL.get());
                            return false;
                        }
                        if (ench.get().parseEnchantment() == null) {
                            SendMessage.warnMessage(logger, Lang.ITEM_ENCHANTMENT_NOT_SUPPORTED.get());
                            return false;
                        }
                        try {
                            if (!this.value.toString().contains(":")) return true;
                            Integer.parseInt(this.value.toString().split(":")[1]);
                            return true;
                        }
                        catch (NumberFormatException e) {
                            SendMessage.warnMessage(logger, Lang.ITEM_ENCHANTMENT_STRENGTH_NULL.get());
                            return false;
                        }
                    }
                    case "enchantment-glint": 
                    case "hidePricingLore": 
                    case "hidden": 
                    case "prior-lore": 
                    case "close-menu": {
                        this.value = Boolean.valueOf(this.value.toString());
                        return true;
                    }
                    case "flicker": 
                    case "trail": {
                        if (ConfigManager.getShop(this.section).getStringList("pages." + this.itemLoc + ".colors").isEmpty()) {
                            SendMessage.sendMessage(logger, ChatColor.RED + "To apply a flicker, trail or firework shape you need to give the firework at least one color using the 'colors' option");
                            return false;
                        }
                        this.value = Boolean.valueOf(this.value.toString());
                        return true;
                    }
                    case "potiontypes": {
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
                    case "spawnertype": {
                        if (mat != XMaterial.SPAWNER.parseMaterial()) {
                            SendMessage.warnMessage(logger, Lang.MATERIAL_NEEDS_TO_BE_SPAWNER.get());
                            return false;
                        }
                        for (EntityType entity : EntityType.values()) {
                            if (!this.value.toString().equalsIgnoreCase(entity.toString())) continue;
                            return true;
                        }
                        SendMessage.warnMessage(logger, Lang.ITEM_SPAWNERTYPE_NULL.get());
                        return false;
                    }
                    case "armorcolor": {
                        try {
                            Color.fromRGB((int)Integer.parseInt(this.value.toString()));
                            return true;
                        }
                        catch (IllegalArgumentException e) {
                            SendMessage.warnMessage(logger, Lang.RGB_COLOR_FORMATTED_WRONG.get().replace("%path%", this.section + "." + this.itemLoc + "." + this.key));
                        }
                        return true;
                    }
                    case "recipes": {
                        if (mat != XMaterial.KNOWLEDGE_BOOK.parseMaterial()) {
                            SendMessage.warnMessage(logger, Lang.MATERIAL_NEEDS_TO_BE_BOOK.get());
                            return false;
                        }
                        if (XMaterial.matchXMaterial(this.value.toString()).isPresent()) {
                            if (XMaterial.matchXMaterial(this.value.toString()).get().parseMaterial() != null) return true;
                        }
                        SendMessage.warnMessage(logger, Lang.CANNOT_GET_RECIPE_MATERIAL.get());
                        return false;
                    }
                    case "skullowner": {
                        if (mat == XMaterial.PLAYER_HEAD.parseMaterial()) return true;
                        SendMessage.warnMessage(logger, Lang.MATERIAL_NEEDS_TO_BE_SKULL.get());
                        return false;
                    }
                    case "colors": 
                    case "fade-colors": {
                        if (Arrays.stream(FireworkUtil.FireworkColor.values()).map(color -> color.name()).collect(Collectors.toList()).contains(this.value.toString().toUpperCase(Locale.ENGLISH))) return true;
                        SendMessage.warnMessage(logger, "Could not get the color '" + this.value.toString() + "'");
                        return false;
                    }
                    case "shape": {
                        if (ConfigManager.getShop(this.section).getStringList("pages." + this.itemLoc + ".colors").isEmpty()) {
                            SendMessage.warnMessage(logger, "To apply a flicker, trail or firework shape you need to give the firework at least one color using the 'colors' option");
                            return false;
                        }
                        try {
                            FireworkEffect.Type.valueOf((String)this.value.toString().toUpperCase(Locale.ENGLISH));
                            return true;
                        }
                        catch (IllegalArgumentException ignored) {
                            SendMessage.sendMessage(logger, "Could not get the firework shape '" + this.value.toString() + "'");
                            return false;
                        }
                    }
                    case "duration": {
                        try {
                            Integer.parseInt(this.value.toString());
                            return true;
                        }
                        catch (NumberFormatException e) {
                            SendMessage.warnMessage(logger, Lang.NO_VALID_AMOUNT.get());
                            return false;
                        }
                    }
                    case "stack-size": 
                    case "max-buy": 
                    case "max-sell": 
                    case "min-buy": 
                    case "min-sell": {
                        try {
                            this.value = Integer.parseInt(this.value.toString());
                        }
                        catch (NumberFormatException e) {
                            SendMessage.warnMessage(logger, Lang.NO_VALID_AMOUNT.get());
                            return false;
                        }
                        if (!this.key.equals("stack-size") || this.plugin.allowIllegalStacks) {
                            if (this.key.equals("min-buy") || this.key.equals("min-sell")) {
                                if ((Integer)this.value < 0) {
                                    SendMessage.warnMessage(logger, "The " + this.key + " needs to be a positive value!");
                                    return false;
                                }
                                if (this.key.equals("min-buy")) {
                                    if ((Integer)this.value < this.plugin.getShopItem(this.section + "." + this.itemLoc).getStackSize()) {
                                        SendMessage.warnMessage(logger, "The " + this.key + " should be larger as the stack size of this shop item.");
                                        return false;
                                    }
                                    if (!this.plugin.getShopItem(this.section + "." + this.itemLoc).isMaxBuy((Integer)this.value)) return true;
                                    SendMessage.warnMessage(logger, "The " + this.key + " should be smaller as the max-buy option of this item.");
                                    return false;
                                }
                                if (!this.key.equals("min-sell")) return true;
                                if (!this.plugin.getShopItem(this.section + "." + this.itemLoc).isMaxSell((Integer)this.value)) return true;
                                SendMessage.warnMessage(logger, "The " + this.key + " should be smaller as the max-sell option of this item.");
                                return false;
                            }
                            if (!this.key.equals("max-buy")) {
                                if (!this.key.equals("max-sell")) return true;
                            }
                            if (this.key.equals("max-buy")) {
                                if (!this.plugin.getShopItem(this.section + "." + this.itemLoc).isMinBuy((Integer)this.value)) return true;
                                SendMessage.warnMessage(logger, "The " + this.key + " should be larger as the min-buy option of this item.");
                                return false;
                            }
                            if (!this.key.equals("max-sell")) return true;
                            if (!this.plugin.getShopItem(this.section + "." + this.itemLoc).isMinSell((Integer)this.value)) return true;
                            SendMessage.warnMessage(logger, "The " + this.key + " should be larger as the min-sell option of this item.");
                            return false;
                        }
                        if ((Integer)this.value >= mat.getMaxStackSize()) return true;
                        SendMessage.warnMessage(logger, "Cannot set the stack size of item %material% to %stack-size% because it would exceed the vanilla max stack size.".replace("%material%", mat.toString()).replace("%stack-size%", this.value.toString()));
                        return true;
                    }
                    case "instrument": {
                        if (mat != XMaterial.GOAT_HORN.parseMaterial()) {
                            SendMessage.warnMessage(logger, "To apply a horn sound type, the material needs to be a goat horn");
                            return false;
                        }
                        try {
                            CreateItemMethodes.Horn.setInstrument(new ItemStack(Material.GOAT_HORN), this.value.toString());
                            return true;
                        }
                        catch (ItemLoadException e) {
                            SendMessage.warnMessage(logger, "Could not find a goat horn instrument like '" + this.value.toString() + "', valid values are:\n" + ChatColor.RED + CreateItemMethodes.Horn.getValues());
                            return false;
                        }
                    }
                    case "section": {
                        return this.plugin.getShopSections().contains(this.value.toString());
                    }
                    case "stew-effect": {
                        if (mat != XMaterial.MUSHROOM_STEW.parseMaterial()) {
                            SendMessage.warnMessage(logger, "To add a stew effect to an item, the material needs to be a 'SUSPICIOUS_STEW'.");
                            return false;
                        }
                        CreateItemMethodes.Stew stew = CreateItemMethodes.Stew.get(this.value.toString());
                        if (stew != null) return true;
                        SendMessage.warnMessage("Could not find a stew effect like '" + this.value.toString() + "', valid values are:\n" + ChatColor.RED + Arrays.stream(CreateItemMethodes.Stew.values()).map(Enum::name).collect(Collectors.toList()));
                        return false;
                    }
                    case "armor-trim.type": {
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
                    case "armor-trim.pattern": {
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
                    case "action": {
                        if (!Arrays.stream(ActionItem.Action.values()).noneMatch(a -> a.name().equalsIgnoreCase(this.value.toString()))) return true;
                        SendMessage.warnMessage(logger, Lang.INVALID_ITEM_ACTION.get().replace("%action%", this.value.toString()));
                        return false;
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

    private List<String> getTabcompletionForValue(String section, String itemLoc, String action, String key) {
        if (!(this.isSection(section) && this.isItemLoc(itemLoc) && this.isAction(action))) {
            return new ArrayList<String>();
        }
        if (action.equalsIgnoreCase("remove") && this.listKeys.contains(key)) {
            return ConfigManager.getShop(this.section).getConfigurationSection("pages." + itemLoc).getStringList(key);
        }
        if (action.equalsIgnoreCase("remove")) {
            ArrayList<String> values = new ArrayList<String>();
            values.add(ConfigManager.getShop(this.section).get("pages." + itemLoc + "." + key, "").toString());
            return values;
        }
        switch (key) {
            case "material": 
            case "recipes": {
                return this.plugin.getSupportedMatNames();
            }
            case "displayname": 
            case "name": 
            case "lore": 
            case "displaylore": {
                return this.methods.getExampleItemNames();
            }
            case "buy": 
            case "sell": {
                return this.plugin.getExamplePrices();
            }
            case "enchantments": {
                return XEnchantment.getNames();
            }
            case "potiontypes": {
                return PotionTypes.getNames();
            }
            case "armorcolor": {
                return this.methods.getExampleRGBColors();
            }
            case "armor-trim.type": {
                return new CreateItemMethodes.ArmorTrim().getTypes();
            }
            case "armor-trim.pattern": {
                return new CreateItemMethodes.ArmorTrim().getPatterns();
            }
            case "spawnertype": {
                return this.getEntityNames();
            }
            case "skullowner": {
                return this.getExamplePlayerNames();
            }
            case "hidePricingLore": 
            case "enchantment-glint": 
            case "flicker": 
            case "trail": 
            case "hidden": {
                return Arrays.asList("true", "false");
            }
            case "slot": {
                return Arrays.asList("1", "9", "73", "108");
            }
            case "ominous-strength": {
                return Arrays.asList("1", "2", "3", "4", "5");
            }
            case "max-buy": 
            case "max-sell": 
            case "min-buy": 
            case "min-sell": {
                return Arrays.asList("1", "2", "8", "16", "32", "64", "128");
            }
            case "colors": 
            case "fade-colors": {
                return Arrays.stream(FireworkUtil.FireworkColor.values()).map(color -> color.name()).collect(Collectors.toList());
            }
            case "shape": {
                return Arrays.stream(FireworkEffect.Type.values()).map(type -> type.name()).collect(Collectors.toList());
            }
            case "duration": {
                return Arrays.asList("1", "2", "3");
            }
            case "stack-size": {
                ArrayList<String> array = new ArrayList<String>();
                for (int i = 1; i < 64; ++i) {
                    array.add(String.valueOf(i));
                }
                return array;
            }
            case "instrument": {
                return CreateItemMethodes.Horn.getValues();
            }
            case "section": {
                return this.plugin.getShopSections();
            }
            case "stew-effect": {
                return Arrays.stream(CreateItemMethodes.Stew.values()).map(Enum::name).collect(Collectors.toList());
            }
            case "action": {
                return Arrays.stream(ActionItem.Action.values()).map(Enum::name).collect(Collectors.toList());
            }
        }
        return new ArrayList<String>();
    }

    private List<String> getAvailableKeys() {
        try {
            ArrayList<String> availableKeys = new ArrayList<String>(this.plugin.getSection(this.section).isActionItem(this.section, this.itemLoc) ? this.actionKeys : this.allKeys);
            ArrayList<String> definedKeys = new ArrayList<String>(ConfigManager.getShop(this.section).getConfigurationSection("pages." + this.itemLoc).getKeys(false));
            if (this.action.equalsIgnoreCase("add")) {
                availableKeys.removeAll(definedKeys);
                availableKeys.addAll(this.plugin.getSection(this.section).isActionItem(this.section, this.itemLoc) ? Arrays.asList("lore", "displaylore", "enchantments", "potiontypes") : this.listKeys);
                return availableKeys;
            }
            if (this.action.equalsIgnoreCase("remove")) {
                return definedKeys;
            }
            if (this.action.equalsIgnoreCase("set")) {
                availableKeys.addAll(this.plugin.getSection(this.section).isActionItem(this.section, this.itemLoc) ? Arrays.asList("lore", "displaylore", "enchantments", "potiontypes") : this.listKeys);
                return availableKeys;
            }
        }
        catch (NullPointerException nullPointerException) {
            
        }
        return new ArrayList<String>();
    }

    private List<String> getAllKeys() {
        ArrayList<String> allKeys = new ArrayList<String>();
        allKeys.add("material");
        allKeys.add("buy");
        allKeys.add("sell");
        allKeys.add("name");
        allKeys.add("displayname");
        allKeys.add("hidePricingLore");
        allKeys.add("slot");
        allKeys.add("stack-size");
        allKeys.add("enchantment-glint");
        allKeys.add("spawnertype");
        allKeys.add("skullowner");
        allKeys.add("armorcolor");
        allKeys.add("duration");
        allKeys.add("armor-trim.pattern");
        allKeys.add("armor-trim.type");
        allKeys.add("flicker");
        allKeys.add("trail");
        allKeys.add("shape");
        allKeys.add("instrument");
        allKeys.add("section");
        allKeys.add("stew-effect");
        allKeys.add("ominous-strength");
        allKeys.add("hidden");
        allKeys.add("max-buy");
        allKeys.add("max-sell");
        allKeys.add("min-buy");
        allKeys.add("min-sell");
        allKeys.add("prior-lore");
        allKeys.add("close-menu");
        allKeys.add("action");
        return allKeys;
    }

    private List<String> getListKeys() {
        ArrayList<String> listKeys = new ArrayList<String>();
        listKeys.add("enchantments");
        listKeys.add("potiontypes");
        listKeys.add("lore");
        listKeys.add("displaylore");
        listKeys.add("recipes");
        listKeys.add("colors");
        listKeys.add("fade-colors");
        return listKeys;
    }

    private List<String> getAvailableActions() {
        ArrayList<String> actions = new ArrayList<String>();
        actions.add("add");
        actions.add("set");
        actions.add("remove");
        return actions;
    }

    private List<String> getExamplePlayerNames() {
        ArrayList<String> playerNames = new ArrayList<String>();
        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            playerNames.add(player.getName());
        }
        return playerNames;
    }

    private List<String> getEntityNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (EntityType entity : EntityType.values()) {
            if (!entity.isAlive()) continue;
            try {
                EntityType.valueOf((String)entity.name());
                names.add(entity.name());
            }
            catch (IllegalArgumentException illegalArgumentException) {
                
            }
        }
        return names;
    }

    private List<String> getValues(String section, String itemLoc, String key) {
        return ConfigManager.getShop(section).getStringList("pages." + itemLoc + "." + key);
    }

    private void updateOption(Object logger) {
        SendMessage.sendMessage(logger, Lang.EDITSHOP_EDITING_ITEM.get());
        String locateItem = "pages." + this.itemLoc;
        if (this.listKeys.contains(this.key)) {
            if (this.action.equalsIgnoreCase("add")) {
                List<String> values = this.getValues(this.section, this.itemLoc, this.key);
                values.add(this.value.toString());
                ConfigManager.getShop(this.section).set(locateItem + "." + this.key, values);
            } else if (this.action.equalsIgnoreCase("set")) {
                ConfigManager.getShop(this.section).set(locateItem + "." + this.key, Collections.singletonList(this.value.toString()));
            } else if (this.action.equalsIgnoreCase("remove")) {
                List<String> values = this.getValues(this.section, this.itemLoc, this.key);
                if (values.size() == 1) {
                    ConfigManager.getShop(this.section).set(locateItem + "." + this.key, null);
                } else if (values.size() >= 1) {
                    values.remove(this.value.toString());
                    ConfigManager.getShop(this.section).set(locateItem + "." + this.key, values);
                }
            }
        } else {
            ConfigManager.getShop(this.section).set(locateItem + "." + this.key, this.value);
        }
        ConfigManager.saveShop(this.section);
        SendMessage.sendMessage(logger, Lang.EDITSHOP_EDIT_ITEM_SUCCESSFUL.get().replace("%itemPath%", locateItem));
        SendMessage.sendMessage(logger, Lang.EDITSHOP_RELOAD_SHOP_TO_SEE_CHANGES.get());
    }
}

