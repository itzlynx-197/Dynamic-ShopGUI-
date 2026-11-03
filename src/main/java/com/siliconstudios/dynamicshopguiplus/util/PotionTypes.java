

package com.siliconstudios.dynamicshopguiplus.util;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.util.ServerInfo;
import com.siliconstudios.dynamicshopguiplus.util.XMaterial;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public enum PotionTypes {
    WATER("WATER", 0, false, 0, false, "EMPTY"),
    MUNDANE("MUNDANE", 0, false, 0, false, new String[0]),
    THICK("THICK", 0, false, 0, false, new String[0]),
    AWKWARD("AWKWARD", 0, false, 0, false, new String[0]),
    FIRE_RESISTANCE("FIRE_RESISTANCE", 3600, false, 0, false, "RESISTANCE_FIRE"),
    LONG_FIRE_RESISTANCE("FIRE_RESISTANCE", 9600, true, 0, false, "FIRE_RESISTANCE_LONG", "RESISTANCE_FIRE_LONG"),
    HARMING("INSTANT_DAMAGE", 1, false, 0, false, "INSTANT_DAMAGE", "HARM", "DAMAGE_INSTANT"),
    STRONG_HARMING("INSTANT_DAMAGE", 1, false, 1, true, "STRONG_INSTANT_DAMAGE", "STRONG_HARM", "STRONG_DAMAGE_INSTANT"),
    HEALING("INSTANT_HEALTH", "INSTANT_HEAL", 1, false, 0, false, "INSTANT_HEALTH", "HEAL"),
    STRONG_HEALING("INSTANT_HEALTH", "INSTANT_HEAL", 1, false, 1, true, "STRONG_INSTANT_HEALTH", "STRONG_HEAL"),
    INVISIBILITY("INVISIBILITY", 3600, false, 0, false, "INVIS"),
    LONG_INVISIBILITY("INVISIBILITY", 9600, true, 0, false, "LONG_INVIS"),
    LEAPING("JUMP_BOOST", "JUMP", 3600, false, 0, false, "JUMP_BOOST", "JUMP"),
    LONG_LEAPING("JUMP_BOOST", "JUMP", 9600, true, 0, false, "LONG_JUMP_BOOST", "LONG_JUMP"),
    STRONG_LEAPING("JUMP_BOOST", "JUMP", 1800, false, 1, true, "STRONG_JUMP_BOOST", "STRONG_JUMP"),
    LUCK("LUCK", 6000, false, 0, false, "LUCKY"),
    NIGHT_VISION("NIGHT_VISION", 3600, false, 0, false, new String[0]),
    LONG_NIGHT_VISION("NIGHT_VISION", 9600, true, 0, false, new String[0]),
    POISON("POISON", 900, false, 0, false, new String[0]),
    LONG_POISON("POISON", 2400, true, 0, false, new String[0]),
    STRONG_POISON("POISON", 440, false, 0, true, new String[0]),
    REGENERATION("REGENERATION", "REGEN", 900, false, 0, false, "REGEN"),
    LONG_REGENERATION("REGENERATION", "REGEN", 2400, true, 0, false, "LONG_REGEN"),
    STRONG_REGENERATION("REGENERATION", "REGEN", 440, false, 1, true, "STRONG_REGEN"),
    SLOW_FALLING("SLOW_FALLING", 1800, false, 0, false, new String[0]),
    LONG_SLOW_FALLING("SLOW_FALLING", 4800, true, 0, false, new String[0]),
    SLOWNESS("SLOWNESS", 1800, false, 0, false, "SLOW"),
    LONG_SLOWNESS("SLOWNESS", 4800, true, 0, false, "LONG_SLOW"),
    STRONG_SLOWNESS("SLOWNESS", 200, false, 3, true, "STRONG_SLOW"),
    STRENGTH("STRENGTH", 3600, false, 0, false, new String[0]),
    LONG_STRENGTH("STRENGTH", 9600, true, 0, false, new String[0]),
    STRONG_STRENGTH("STRENGTH", 1800, false, 1, true, new String[0]),
    SWIFTNESS("SPEED", 3600, false, 0, false, "SPEED"),
    LONG_SWIFTNESS("SPEED", 9600, true, 0, false, "LONG_SPEED"),
    STRONG_SWIFTNESS("SPEED", 1800, false, 1, true, "STRONG_SPEED"),
    TURTLE_MASTER("TURTLE_MASTER", 400, false, 0, false, new String[0]),
    LONG_TURTLE_MASTER("TURTLE_MASTER", 800, true, 0, false, new String[0]),
    STRONG_TURTLE_MASTER("TURTLE_MASTER", 400, false, 1, true, new String[0]),
    WATER_BREATHING("WATER_BREATHING", 3600, false, 0, false, new String[0]),
    LONG_WATER_BREATHING("WATER_BREATHING", 9600, true, 0, false, new String[0]),
    WEAKNESS("WEAKNESS", 1800, false, 0, false, new String[0]),
    LONG_WEAKNESS("WEAKNESS", 4800, true, 0, false, new String[0]),
    INFESTED("INFESTED", 3600, false, 0, false, new String[0]),
    OOZING("OOZING", 3600, false, 0, false, new String[0]),
    WEAVING("WEAVING", 3600, false, 0, false, new String[0]),
    WIND_CHARGED("WIND_CHARGED", 3600, false, 0, false, new String[0]);

    private final String type;
    private final int ticks;
    private final boolean extended;
    private final int level;
    private final boolean upgraded;
    private final String[] aliases;
    public static final EnumSet<PotionTypes> VALUES;

    private PotionTypes(String type, int ticks, boolean extended, int level, boolean upgraded, String ... aliases) {
        this.type = type;
        this.ticks = ticks;
        this.extended = extended;
        this.level = level;
        this.upgraded = upgraded;
        this.aliases = aliases;
    }

    private PotionTypes(String type, String legacyType, int ticks, boolean extended, int level, boolean upgraded, String ... aliases) {
        this.type = ServerInfo.supportsComponents() ? type : legacyType;
        this.ticks = ticks;
        this.extended = extended;
        this.level = level;
        this.upgraded = upgraded;
        this.aliases = aliases;
    }

    public static Optional<PotionTypes> matchPotionType(String name) {
        name = name.replace(" ", "_").toUpperCase(Locale.ENGLISH);
        for (PotionTypes potion : VALUES) {
            if (!potion.name().equalsIgnoreCase(name) && !potion.anyMatchAliases(name)) continue;
            return Optional.of(potion);
        }
        return Optional.empty();
    }

    public static Optional<PotionTypes> getFromType(String name) {
        name = name.replace(" ", "_").toUpperCase(Locale.ENGLISH);
        for (PotionTypes potion : VALUES) {
            if (!potion.type.equalsIgnoreCase(name) && !potion.anyMatchAliases(name)) continue;
            return Optional.of(potion);
        }
        return Optional.empty();
    }

    public static PotionTypes matchPotionType(PotionEffect effectType) {
        for (PotionTypes potionType : VALUES) {
            if (!potionType.isSupported() || !potionType.hasEffect() || !potionType.parsePotionEffectType().equals(effectType.getType()) || potionType.ticks != effectType.getDuration() || potionType.level != effectType.getAmplifier()) continue;
            return potionType;
        }
        return WATER;
    }

    public static PotionTypes matchPotionType(PotionType type) {
        for (PotionTypes potionType : VALUES) {
            if (!potionType.isSupported() || !potionType.parsePotionType().equals((Object)type)) continue;
            return potionType;
        }
        return WATER;
    }

    public static Optional<PotionTypes> matchPotionDataLegacy(PotionType type, boolean extended, boolean upgraded) {
        for (PotionTypes potionType : VALUES) {
            if (!potionType.isSupported() || !potionType.parsePotionType().equals((Object)type) || potionType.extended != extended || potionType.upgraded != upgraded) continue;
            return Optional.of(potionType);
        }
        return Optional.empty();
    }

    public static Optional<PotionTypes> matchPotionData(PotionType type) {
        for (PotionTypes potionType : VALUES) {
            if (!potionType.isSupported() || !potionType.name().equals(type.name())) continue;
            return Optional.of(potionType);
        }
        return Optional.empty();
    }

    public static boolean canHaveEffects(Material mat) {
        return XMaterial.matchXMaterial(mat).equals((Object)XMaterial.POTION) || XMaterial.matchXMaterial(mat).equals((Object)XMaterial.LINGERING_POTION) || XMaterial.matchXMaterial(mat).equals((Object)XMaterial.SPLASH_POTION) || XMaterial.matchXMaterial(mat).equals((Object)XMaterial.TIPPED_ARROW);
    }

    public PotionEffectType parsePotionEffectType() {
        return this.hasEffect() ? this.parsePotionType().getEffectType() : null;
    }

    public PotionType parsePotionType() {
        return PotionType.valueOf((String)(ServerInfo.supportsComponents() ? this.name() : this.type));
    }

    public PotionEffect parsePotionEffect() {
        return this.hasEffect() ? new PotionEffect(this.parsePotionEffectType(), this.ticks, this.level, false) : null;
    }

    public static List<PotionTypes> getPotionTypes(ItemStack item) {
        ArrayList<PotionTypes> potionTypes = new ArrayList<PotionTypes>();
        if (item == null) {
            return potionTypes;
        }
        PotionMeta meta = (PotionMeta)item.getItemMeta();
        if (meta == null) {
            return potionTypes;
        }
        if (DynamicShopGUIPlus.getInstance().version >= 19) {
            Optional<PotionTypes> base = DynamicShopGUIPlus.getInstance().versionHandler.getBasePotion(item);
            if (base.isPresent()) {
                potionTypes.add(base.get());
            }
            for (PotionEffect pe : meta.getCustomEffects()) {
                potionTypes.add(PotionTypes.matchPotionType(pe));
            }
        } else {
            for (PotionEffect pe : meta.getCustomEffects()) {
                potionTypes.add(PotionTypes.matchPotionType(pe));
            }
        }
        return potionTypes;
    }

    public boolean isExtended() {
        return this.extended;
    }

    public int getDuration() {
        return this.ticks;
    }

    public int getAmplifier() {
        return this.level;
    }

    public boolean isUpgraded() {
        return this.upgraded;
    }

    public boolean isSupported() {
        try {
            return this.parsePotionType() != null || !this.hasEffect();
        }
        catch (Exception ignored) {
            return false;
        }
    }

    private boolean anyMatchAliases(String name) {
        for (String alias : this.aliases) {
            if (!name.equals(alias) && !name.equals(StringUtils.remove((String)alias, (char)'_'))) continue;
            return true;
        }
        return false;
    }

    public static List<String> getNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (PotionTypes potionType : PotionTypes.values()) {
            if (!potionType.isSupported()) continue;
            names.add(potionType.type);
        }
        return names;
    }

    public ItemStack addEffect(ItemStack item, boolean splash) {
        if (DynamicShopGUIPlus.getInstance().version != 18) {
            if (DynamicShopGUIPlus.getInstance().versionHandler != null) {
                item = DynamicShopGUIPlus.getInstance().versionHandler.setPotionType(item, this);
            } else {
                PotionMeta meta = (PotionMeta)item.getItemMeta();
                if (ServerInfo.getVersion().newerOrEqualAs(ServerInfo.Version.v1_20_R2) && meta.getBasePotionType().getEffectType() == null) {
                    meta.setBasePotionType(PotionType.valueOf((String)this.name()));
                } else {
                    if (!this.hasEffect()) {
                        return item;
                    }
                    meta.addCustomEffect(new PotionEffect(this.parsePotionEffectType(), this.getDuration(), this.getAmplifier(), false, true, true), false);
                }
                item.setItemMeta((ItemMeta)meta);
            }
        } else if (this.hasEffect()) {
            this.createOldPotion(item, splash);
        }
        return item;
    }

    private ItemStack createOldPotion(ItemStack item, boolean splash) {
        PotionMeta meta = (PotionMeta)item.getItemMeta();
        if (!this.hasEffect()) {
            return item;
        }
        if (meta.hasCustomEffects()) {
            meta.addCustomEffect(new PotionEffect(this.parsePotionEffectType(), this.getDuration(), this.getAmplifier()), false);
        } else {
            Potion pot = new Potion(this.parsePotionType(), this.getAmplifier() + 1, splash, this.isExtended());
            pot.apply(item);
            meta.addCustomEffect(new PotionEffect(this.parsePotionEffectType(), this.getDuration(), this.getAmplifier()), false);
            meta.setMainEffect(this.parsePotionEffectType());
        }
        return item;
    }

    public boolean hasEffect() {
        return this.parsePotionType().getEffectType() != null;
    }

    static {
        VALUES = EnumSet.allOf(PotionTypes.class);
    }
}

