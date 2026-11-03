

package com.siliconstudios.dynamicshopguiplus.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.util.ServerInfo;

public enum NBTMaps {
    MAX_STACK_SIZE(null, "max_stack_size"),
    MAX_DAMAGE(null, "max_damage"),
    DAMAGE("Damage", "damage"),
    UNBREAKABLE("Unbreakable", "unbreakable"),
    NAME("display::Name", "item_name"),
    DISPLAYNAME("display::Name", "custom_name"),
    LORE("display::Lore", "lore"),
    RARITY(null, "rarity"),
    ENCHANTMENTS("Enchantments", "enchantments"),
    CAN_PLACE_ON("CanPlaceOn", "can_place_on"),
    CAN_BREAK("CanDestroy", "can_break"),
    ATTRIBUTE_MODIFIERS("AttributeModifiers", "attribute_modifiers"),
    CUSTOM_MODEL_DATA("CustomModelData", "custom_model_data"),
    ITEM_MODEL(null, "item_model"),
    HIDE_ADDITIONAL_TOOLTIP(null, "hide_additional_tooltip"),
    HIDE_FLAGS("HideFlags", "1.20.5|hide_tooltip", "1.21.5|tooltip_display"),
    REPAIR_COST("RepairCost", "repair_cost"),
    CREATIVE_SLOT_LOCK(null, "creative_slot_lock"),
    ENCHANTMENT_GLINT(null, "enchantment_glint_override"),
    INTANGIBLE_PROJECTILE(null, "intangible_projectile"),
    FOOD(null, "food"),
    FIRE_RESISTANCE(null, "fire_resistant"),
    TOOL(null, "tool"),
    STORED_ENCHANTMENTS("StoredEnchantments", "stored_enchantments"),
    ARMOR_COLOR("display::color", "dyed_color"),
    MAP_COLOR("display::MapColor", "map_color"),
    MAP_ID("map", "map_id"),
    MAP_DECORATIONS("Decorations", "map_decorations"),
    MAP_POST_PROCESSING("map_scale_direction", "map_post_processing"),
    CHARGED_PROJECTILES("ChargedProjectiles", "charged_projectiles"),
    BUNDLE_CONTENTS("Items", "bundle_contents"),
    BASE_POTION("Potion", "potion_contents::potion"),
    CUSTOM_POTION_EFFECTS("custom_potion_effects", "potion_contents::custom_effects"),
    CUSTOM_POTION_COLOR("CustomPotionColor", "potion_contents::custom_color"),
    SUSPICIOUS_STEW_EFFECTS("effects", "suspicious_stew_effects"),
    WRITEABLE_BOOK_CONTENT(null, "writable_book_content"),
    WRITTEN_BOOK_CONTENT("pages", "written_book_content::pages"),
    BOOK_AUTHOR("author", "written_book_content::author"),
    BOOK_TITLE("title", "written_book_content::title"),
    TRIM_MATERIAL("Trim::material", "trim::material"),
    TRIM_PATTERN("Trim::pattern", "trim::pattern"),
    DEBUG_STICK("DebugProperty", "debug_stick_state"),
    ENTITY_DATA("EntityTag", "entity_data"),
    BUCKET_ENTITY_DATA("BucketVariantTag", "bucket_entity_data"),
    BLOCK_ENTITY_DATA("BlockEntityTag", "block_entity_data"),
    INSTRUMENT("instrument", "instrument"),
    OMINOUS_BOTTLE_AMPLIFIER(null, "ominous_bottle_amplifier"),
    RECIPES("Recipes", "recipes"),
    LODESTONE_TRACKER("LodestoneTracked", "lodestone_tracker"),
    FIREWORK_EXPLOSION("Explosion", "firework_explosion"),
    FIREWORK_EXPLOSIONS("Fireworks::Explosions", "fireworks::explosions"),
    FIREWORK_FLIGHT_DURATION("Fireworks::Flight", "fireworks::flight_duration"),
    SKULLOWNER_ID("SkullOwner::Id", "profile::id"),
    SKULLOWNER_NAME("SkullOwner::Name", "profile::name"),
    SKULLOWNER_PROPERTIES("SkullOwner::Properties", "profile::properties"),
    BANNER_PATTERNS("BlockEntityTag::Patterns", "banner_patterns"),
    BASE_COLOR("BlockEntityTag::Base", "base_color"),
    CUSTOM_DATA("", "custom_data::");

    final String NBT;
    final String COMPOUND;

    private NBTMaps(String nbt, String compound) {
        this.NBT = nbt;
        this.COMPOUND = compound;
    }

    private NBTMaps(String nbt, String ... compound) {
        this.NBT = nbt;
        String s = null;
        for (String data : compound) {
            if (!data.contains("|")) {
                s = data;
                continue;
            }
            if (!ServerInfo.getVersion().newerOrEqualAs(ServerInfo.Version.getFromSimple(data.split("\\|")[0]))) continue;
            s = data.split("\\|")[1];
        }
        this.COMPOUND = s == null ? compound[0] : s;
    }

    public String getTag() {
        return ServerInfo.supportsComponents() ? this.COMPOUND : this.NBT;
    }

    public static List<String> getTags(boolean log, String ... tags) {
        return Arrays.stream(tags).map(i -> NBTMaps.getTag(i, null, null, log)).collect(Collectors.toList());
    }

    public static String getTag(String tag) {
        return NBTMaps.getTag(tag, null, null, true);
    }

    public static String getTag(String tag, String section, String path) {
        return NBTMaps.getTag(tag, section, path, true);
    }

    public static String getTag(String tag, String section, String path, boolean log) {
        String s;
        boolean compound = ServerInfo.supportsComponents();
        NBTMaps mapping = null;
        boolean full = false;
        if (tag.contains("::")) {
            try {
                mapping = NBTMaps.getFromSimple(tag);
            }
            catch (IllegalArgumentException illegalArgumentException) {}
        } else {
            try {
                mapping = NBTMaps.getFromSimple(tag);
            }
            catch (IllegalArgumentException e) {
                for (NBTMaps map : NBTMaps.values()) {
                    if (map.NBT != null && map.NBT.contains("::") && map.NBT.split("::")[0].equals(tag)) {
                        mapping = map;
                        full = true;
                        break;
                    }
                    if (map.COMPOUND == null || !map.COMPOUND.contains("::") || !map.COMPOUND.split("::")[0].equals(tag)) continue;
                    mapping = map;
                    full = true;
                    break;
                }
            }
        }
        if (mapping == null) {
            if (compound && tag.startsWith(NBTMaps.CUSTOM_DATA.COMPOUND)) {
                return tag;
            }
            if (compound && !tag.startsWith(NBTMaps.CUSTOM_DATA.COMPOUND)) {
                if (log) {
                    SendMessage.warnMessage("Found unknown component tag '" + tag + "' for current minecraft version, check spelling or make sure it exists");
                    if (path.startsWith("pages") || path.startsWith("items")) {
                        SendMessage.logDebugMessage(Lang.ITEMS_LOCATION_IN_SHOPS.get().toString().replace("%section%", section).replace("%location%", path));
                    } else if (section == null) {
                        SendMessage.logDebugMessage(Lang.ITEMS_PATH_IN_CONFIG.get().toString().replace("%location%", path));
                    }
                }
                return NBTMaps.CUSTOM_DATA.COMPOUND + tag;
            }
            return tag;
        }
        String string = s = !compound ? mapping.NBT : mapping.COMPOUND;
        if (s == null) {
            if (log) {
                SendMessage.warnMessage("Found incompatible NBT tag: NBT tag for given component tag '" + tag + "' doesn't exist for current MC version");
                if (path.startsWith("pages") || path.startsWith("items")) {
                    SendMessage.logDebugMessage(Lang.ITEMS_LOCATION_IN_SHOPS.get().toString().replace("%section%", section).replace("%location%", path));
                } else if (section == null) {
                    SendMessage.logDebugMessage(Lang.ITEMS_PATH_IN_CONFIG.get().toString().replace("%location%", path));
                }
            }
            return tag;
        }
        String string2 = s = full ? s.split("::")[0] : s;
        if (!tag.equals(s) && log) {
            SendMessage.warnMessage("Found incompatible NBT/component tag for current minecraft version: Did you mean '" + s + "' instead of '" + tag + "'?\nPlease correct the value manually in config to get rid of this message");
            if (path.startsWith("pages") || path.startsWith("items")) {
                SendMessage.logDebugMessage(Lang.ITEMS_LOCATION_IN_SHOPS.get().toString().replace("%section%", section).replace("%location%", path));
            } else if (section == null) {
                SendMessage.logDebugMessage(Lang.ITEMS_PATH_IN_CONFIG.get().toString().replace("%location%", path));
            }
        }
        return s;
    }

    public static NBTMaps getFromSimple(String tag) {
        for (NBTMaps mapping : NBTMaps.values()) {
            if ((mapping.NBT == null || !mapping.NBT.equals(tag)) && (mapping.COMPOUND == null || !mapping.COMPOUND.equals(tag))) continue;
            return mapping;
        }
        throw new IllegalArgumentException();
    }
}

