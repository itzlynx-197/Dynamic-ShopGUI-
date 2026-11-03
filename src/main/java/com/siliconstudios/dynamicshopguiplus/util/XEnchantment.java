

package com.siliconstudios.dynamicshopguiplus.util;

import com.google.common.base.Enums;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.util.ServerInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

public final class XEnchantment {
    public static final  XEnchantment ARROW_DAMAGE;
    public static final  XEnchantment ARROW_FIRE;
    public static final  XEnchantment ARROW_INFINITE;
    public static final  XEnchantment ARROW_KNOCKBACK;
    public static final  XEnchantment BINDING_CURSE;
    public static final  XEnchantment CHANNELING;
    public static final  XEnchantment DAMAGE_ALL;
    public static final  XEnchantment DAMAGE_ARTHROPODS;
    public static final  XEnchantment DAMAGE_UNDEAD;
    public static final  XEnchantment DEPTH_STRIDER;
    public static final  XEnchantment DIG_SPEED;
    public static final  XEnchantment DURABILITY;
    public static final  XEnchantment FIRE_ASPECT;
    public static final  XEnchantment FROST_WALKER;
    public static final  XEnchantment IMPALING;
    public static final  XEnchantment SOUL_SPEED;
    public static final  XEnchantment SWIFT_SNEAK;
    public static final  XEnchantment KNOCKBACK;
    public static final  XEnchantment LOOT_BONUS_BLOCKS;
    public static final  XEnchantment LOOT_BONUS_MOBS;
    public static final  XEnchantment LOYALTY;
    public static final  XEnchantment LUCK;
    public static final  XEnchantment LURE;
    public static final  XEnchantment MENDING;
    public static final  XEnchantment MULTISHOT;
    public static final  XEnchantment OXYGEN;
    public static final  XEnchantment PIERCING;
    public static final  XEnchantment PROTECTION_ENVIRONMENTAL;
    public static final  XEnchantment PROTECTION_EXPLOSIONS;
    public static final  XEnchantment PROTECTION_FALL;
    public static final  XEnchantment PROTECTION_FIRE;
    public static final  XEnchantment PROTECTION_PROJECTILE;
    public static final  XEnchantment QUICK_CHARGE;
    public static final  XEnchantment RIPTIDE;
    public static final  XEnchantment SILK_TOUCH;
    public static final  XEnchantment SWEEPING_EDGE;
    public static final  XEnchantment THORNS;
    public static final  XEnchantment VANISHING_CURSE;
    public static final  XEnchantment WATER_WORKER;
    public static final  XEnchantment BREACH;
    public static final  XEnchantment DENSITY;
    public static final  XEnchantment WIND_BURST;
    public static final  XEnchantment LUNGE;
    public static final java.util.Set<XEnchantment> VALUES = new java.util.HashSet<>();
    private static final boolean ISFLAT;
    private static final Pattern FORMAT_PATTERN;
    private final boolean self;
    private final String[] aliases;
    private static final  XEnchantment[] $VALUES;

    public static XEnchantment[] values() {
        return (XEnchantment[])$VALUES.clone();
    }

    public static XEnchantment valueOf(String name) {
        for (XEnchantment e : values()) { if (e.name().equalsIgnoreCase(name)) return e; } return null;
    }

    private XEnchantment(String ... names) {
        this(false, names);
    }

    private XEnchantment(boolean self, String ... aliases) {
        this.self = self;
        this.aliases = aliases;
    }

    private XEnchantment(boolean special, boolean self, String ... aliases) {
        this.self = ServerInfo.supportsComponents();
        this.aliases = aliases;
    }

    public static boolean isSmiteEffectiveAgainst(EntityType type) {
        return Arrays.asList(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.WITHER, EntityType.WITHER_SKELETON, EntityType.SKELETON_HORSE, EntityType.STRAY, EntityType.HUSK, EntityType.PHANTOM, EntityType.DROWNED).contains(type);
    }

    public static boolean isArthropodsEffectiveAgainst(EntityType type) {
        if (Arrays.asList(EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.SILVERFISH, EntityType.ENDERMITE).contains(type)) {
            return true;
        }
        if (Enums.getIfPresent(EntityType.class, (String)"BEE").isPresent()) {
            return type == EntityType.BEE;
        }
        return false;
    }

    @Nonnull
    private static String format(@Nonnull String name) {
        return FORMAT_PATTERN.matcher(name.trim().replace('-', '_').replace(' ', '_')).replaceAll("").toUpperCase(Locale.ENGLISH);
    }

    @Nonnull
    public static Optional<XEnchantment> matchXEnchantment(@Nonnull String enchantment) {
        Validate.notEmpty((String)enchantment, (String)"Enchantment name cannot be null or empty");
        enchantment = XEnchantment.format(enchantment);
        for (XEnchantment value : VALUES) {
            if (!value.name().equals(enchantment) && !value.anyMatchAliases(enchantment)) continue;
            return Optional.of(value);
        }
        return Optional.empty();
    }

    public static XEnchantment matchXEnchantment(@Nonnull Enchantment enchantment) {
        Objects.requireNonNull(enchantment, "Cannot parse XEnchantment of a null enchantment");
        try {
            return ServerInfo.getVersion().newerOrEqualAs(ServerInfo.Version.v1_20_R4) ? XEnchantment.matchCorrectEnch(enchantment) : XEnchantment.valueOf(enchantment.getName());
        }
        catch (IllegalArgumentException ex) {
            SendMessage.errorMessage("Could not find enchantment '" + enchantment.getName() + "'");
            return null;
        }
    }

    private static XEnchantment matchCorrectEnch(@Nonnull Enchantment enchantment) {
        String name = enchantment.getKey().getKey().toUpperCase(Locale.ROOT);
        for (XEnchantment ench : VALUES) {
            if (!(!ench.self ? ench.aliases[0].equals(name) : ench.name().equals(name))) continue;
            return ench;
        }
        return null;
    }

    @Nonnull
    public static ItemStack addEnchantFromString(ItemStack item, String enchantment) {
        Optional<XEnchantment> enchantOpt;
        Objects.requireNonNull(item, "Cannot add enchantment to null ItemStack");
        if (Strings.isNullOrEmpty((String)enchantment) || enchantment.equalsIgnoreCase("none")) {
            return item;
        }
        String[] split = StringUtils.split((String)StringUtils.deleteWhitespace((String)enchantment), (char)',');
        if (split.length == 0) {
            split = StringUtils.split((String)enchantment, (char)' ');
        }
        if ((enchantOpt = XEnchantment.matchXEnchantment(split[0])).isPresent()) {
            return item;
        }
        Enchantment enchant = enchantOpt.get().parseEnchantment();
        if (enchant == null) {
            return null;
        }
        int lvl = 1;
        try {
            if (split.length > 1) {
                lvl = Integer.parseInt(split[1]);
            }
        }
        catch (NumberFormatException numberFormatException) {
            
        }
        item.addUnsafeEnchantment(enchant, lvl);
        return item;
    }

    @Nonnull
    public ItemStack getBook(int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta)book.getItemMeta();
        meta.addStoredEnchant(this.parseEnchantment(), level, true);
        book.setItemMeta((ItemMeta)meta);
        return book;
    }

    private boolean anyMatchAliases(String enchantment) {
        for (String alias : this.aliases) {
            if (!enchantment.equals(alias) && !enchantment.equals(StringUtils.remove((String)alias, (char)'_'))) continue;
            return true;
        }
        return false;
    }

    public String name() {
        for (java.lang.reflect.Field f : XEnchantment.class.getDeclaredFields()) {
            if (f.getType() == XEnchantment.class) {
                try {
                    if (f.get(null) == this) return f.getName();
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }

    @Nonnull
    public String getVanillaName() {
        return this.self ? this.name() : this.aliases[0];
    }

    @Nullable
    public Enchantment parseEnchantment() {
        return ISFLAT ? Enchantment.getByKey((NamespacedKey)NamespacedKey.minecraft((String)this.getVanillaName().toLowerCase(Locale.ENGLISH))) : Enchantment.getByName((String)this.name());
    }

    public boolean isSupported() {
        return this.parseEnchantment() != null;
    }

    @Nonnull
    public String[] getAliases() {
        return this.aliases;
    }

    public String toString() {
        return WordUtils.capitalize((String)this.name().replace('_', ' ').toLowerCase(Locale.ENGLISH));
    }

    public static List<String> getNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (XEnchantment enchantment : XEnchantment.values()) {
            if (!enchantment.isSupported()) continue;
            names.add(enchantment.getVanillaName() + ":1");
        }
        return names;
    }

    private static  XEnchantment[] $values() {
        return new XEnchantment[]{ARROW_DAMAGE, ARROW_FIRE, ARROW_INFINITE, ARROW_KNOCKBACK, BINDING_CURSE, CHANNELING, DAMAGE_ALL, DAMAGE_ARTHROPODS, DAMAGE_UNDEAD, DEPTH_STRIDER, DIG_SPEED, DURABILITY, FIRE_ASPECT, FROST_WALKER, IMPALING, SOUL_SPEED, SWIFT_SNEAK, KNOCKBACK, LOOT_BONUS_BLOCKS, LOOT_BONUS_MOBS, LOYALTY, LUCK, LURE, MENDING, MULTISHOT, OXYGEN, PIERCING, PROTECTION_ENVIRONMENTAL, PROTECTION_EXPLOSIONS, PROTECTION_FALL, PROTECTION_FIRE, PROTECTION_PROJECTILE, QUICK_CHARGE, RIPTIDE, SILK_TOUCH, SWEEPING_EDGE, THORNS, VANISHING_CURSE, WATER_WORKER, BREACH, DENSITY, WIND_BURST, LUNGE};
    }

    static {
        boolean flat;
        ARROW_DAMAGE = new XEnchantment("POWER", "ARROW_DAMAGE", "ARROW_POWER", "AD");
        ARROW_FIRE = new XEnchantment("FLAME", "FLAME_ARROW", "FIRE_ARROW", "AF");
        ARROW_INFINITE = new XEnchantment("INFINITY", "INF_ARROWS", "INFINITE_ARROWS", "INFINITE", "UNLIMITED", "UNLIMITED_ARROWS", "AI");
        ARROW_KNOCKBACK = new XEnchantment("PUNCH", "ARROW_KNOCKBACK", "ARROWKB", "ARROW_PUNCH", "AK");
        BINDING_CURSE = new XEnchantment(true, "BINDING_CURSE", "BIND_CURSE", "BINDING", "BIND");
        CHANNELING = new XEnchantment(true, "CHANNELLING", "CHANELLING", "CHANELING", "CHANNEL");
        DAMAGE_ALL = new XEnchantment("SHARPNESS", "ALL_DAMAGE", "ALL_DMG", "SHARP", "DAL");
        DAMAGE_ARTHROPODS = new XEnchantment("BANE_OF_ARTHROPODS", "ARDMG", "BANE_OF_ARTHROPOD", "ARTHROPOD", "DAR");
        DAMAGE_UNDEAD = new XEnchantment("SMITE", "UNDEAD_DAMAGE", "DU");
        DEPTH_STRIDER = new XEnchantment(true, "DEPTH", "STRIDER");
        DIG_SPEED = new XEnchantment("EFFICIENCY", "MINE_SPEED", "CUT_SPEED", "DS", "EFF");
        DURABILITY = new XEnchantment("UNBREAKING", "DURA");
        FIRE_ASPECT = new XEnchantment(true, "FIRE", "MELEE_FIRE", "MELEE_FLAME", "FA");
        FROST_WALKER = new XEnchantment(true, "FROST", "WALKER");
        IMPALING = new XEnchantment(true, "IMPALE", "OCEAN_DAMAGE", "OCEAN_DMG");
        SOUL_SPEED = new XEnchantment(true, "SPEED_SOUL", "SOUL_RUNNER");
        SWIFT_SNEAK = new XEnchantment(true, "SPEED_SNEAK");
        KNOCKBACK = new XEnchantment(true, "K_BACK", "KB");
        LOOT_BONUS_BLOCKS = new XEnchantment("FORTUNE", "BLOCKS_LOOT_BONUS", "FORT", "LBB");
        LOOT_BONUS_MOBS = new XEnchantment("LOOTING", "MOB_LOOT", "MOBS_LOOT_BONUS", "LBM");
        LOYALTY = new XEnchantment(true, "LOYAL", "RETURN");
        LUCK = new XEnchantment("LUCK_OF_THE_SEA", "LUCK_OF_SEA", "LUCK_OF_SEAS", "ROD_LUCK");
        LURE = new XEnchantment(true, "ROD_LURE");
        MENDING = new XEnchantment(true, new String[0]);
        MULTISHOT = new XEnchantment(true, "TRIPLE_SHOT");
        OXYGEN = new XEnchantment("RESPIRATION", "BREATH", "BREATHING", "O2", "O");
        PIERCING = new XEnchantment(true, new String[0]);
        PROTECTION_ENVIRONMENTAL = new XEnchantment("PROTECTION", "PROTECT", "PROT");
        PROTECTION_EXPLOSIONS = new XEnchantment("BLAST_PROTECTION", "BLAST_PROTECT", "EXPLOSIONS_PROTECTION", "EXPLOSION_PROTECTION", "BLAST_PROTECTION", "PE");
        PROTECTION_FALL = new XEnchantment("FEATHER_FALLING", "FALL_PROT", "FEATHER_FALL", "FALL_PROTECTION", "FEATHER_FALLING", "PFA");
        PROTECTION_FIRE = new XEnchantment("FIRE_PROTECTION", "FIRE_PROT", "FIRE_PROTECT", "FIRE_PROTECTION", "FLAME_PROTECTION", "FLAME_PROTECT", "FLAME_PROT", "PF");
        PROTECTION_PROJECTILE = new XEnchantment("PROJECTILE_PROTECTION", "PROJECTILE_PROTECTION", "PROJ_PROT", "PP");
        QUICK_CHARGE = new XEnchantment(true, "QUICKCHARGE", "QUICK_DRAW", "FAST_CHARGE", "FAST_DRAW");
        RIPTIDE = new XEnchantment(true, "RIP", "TIDE", "LAUNCH");
        SILK_TOUCH = new XEnchantment(true, "SOFT_TOUCH", "ST");
        SWEEPING_EDGE = new XEnchantment(true, false, "SWEEPING", "SWEEP_EDGE");
        THORNS = new XEnchantment(true, "HIGHCRIT", "THORN", "HIGHERCRIT", "T");
        VANISHING_CURSE = new XEnchantment(true, "VANISHING_CURSE", "VANISH_CURSE", "VANISHING", "VANISH");
        WATER_WORKER = new XEnchantment("AQUA_AFFINITY", "WATER_WORKER", "AQUA_AFFINITY", "WATER_MINE", "WW");
        BREACH = new XEnchantment(true, new String[0]);
        DENSITY = new XEnchantment(true, new String[0]);
        WIND_BURST = new XEnchantment(true, new String[0]);
        LUNGE = new XEnchantment(true, new String[0]);
        $VALUES = XEnchantment.$values();
        VALUES.addAll(java.util.Arrays.asList($VALUES));
        FORMAT_PATTERN = Pattern.compile("\\d+|\\W+");
        try {
            Class<?> namespacedKeyClass = Class.forName("org.bukkit.NamespacedKey");
            Class<?> enchantmentClass = Class.forName("org.bukkit.enchantments.Enchantment");
            enchantmentClass.getDeclaredMethod("getByKey", namespacedKeyClass);
            flat = true;
        }
        catch (ClassNotFoundException | NoSuchMethodException ex) {
            flat = false;
        }
        ISFLAT = flat;
    }
}

