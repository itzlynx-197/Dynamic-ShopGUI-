

package com.siliconstudios.dynamicshopguiplus.methodes;

import com.mojang.authlib.GameProfile;
import java.awt.Color;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.files.Translatable;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.objects.DisplayItem;
import com.siliconstudios.dynamicshopguiplus.objects.ShopItem;
import com.siliconstudios.dynamicshopguiplus.util.ChatUtil;
import com.siliconstudios.dynamicshopguiplus.util.EntityTypes;
import com.siliconstudios.dynamicshopguiplus.util.FireworkUtil;
import com.siliconstudios.dynamicshopguiplus.util.PotionTypes;
import com.siliconstudios.dynamicshopguiplus.util.ServerInfo;
import com.siliconstudios.dynamicshopguiplus.util.SkullUtil;
import com.siliconstudios.dynamicshopguiplus.util.XEnchantment;
import com.siliconstudios.dynamicshopguiplus.util.XMaterial;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.ItemLoadException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.MusicInstrument;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.KnowledgeBookMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MusicInstrumentMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CreateItemMethodes {
    public static ItemStack createItemMaterialFromString(String material) throws ItemLoadException {
        ItemStack item;
        if (material != null && !material.isEmpty()) {
            Optional<XMaterial> mat = XMaterial.matchXMaterial(material);
            if (!mat.isPresent()) {
                throw new ItemLoadException(Lang.ITEM_MATERIAL_NULL.get().replace("%material%", material));
            }
            item = mat.get().parseItem();
            if (item == null) {
                throw new ItemLoadException(Lang.MATERIAL_NOT_SUPPORTED.get());
            }
        } else {
            throw new ItemLoadException(Lang.NEED_ITEM_MATERIAL.get());
        }
        return item;
    }

    
    public ItemStack setOption(DisplayItem displayItem, ItemStack item, ConfigurationSection itemConfig, String section, boolean displayableItem) throws ItemLoadException {
        if (itemConfig.contains("enchantments")) {
            item = this.setEnchantments(item, section, itemConfig);
        }
        if (itemConfig.contains("spawnertype")) {
            if (item.getType() != XMaterial.SPAWNER.parseMaterial()) throw new ItemLoadException(Lang.MATERIAL_NEEDS_TO_BE_SPAWNER.get());
            item = this.addSpawnerType(displayableItem && displayItem instanceof ShopItem ? (ShopItem)displayItem : null, item, itemConfig.getString("spawnertype"), itemConfig.getString("name"));
        } else if (itemConfig.contains("potiontypes")) {
            boolean splash = itemConfig.getString("material").equalsIgnoreCase("SPLASH_POTION");
            item = this.setPotionTypes(item, itemConfig.getStringList("potiontypes"), section, itemConfig.getCurrentPath(), splash);
        } else if (itemConfig.contains("recipes")) {
            if (item.getType() != XMaterial.KNOWLEDGE_BOOK.parseMaterial()) throw new ItemLoadException(Lang.MATERIAL_NEEDS_TO_BE_BOOK.get());
            for (String recipe : itemConfig.getStringList("recipes")) {
                if (recipe.isEmpty()) continue;
                item = this.addRecipe(item, new NamespacedKey("minecraft", this.getRecipeMaterial(recipe).toString().toLowerCase(Locale.ENGLISH)));
            }
        } else if (itemConfig.contains("skullowner")) {
            if (item.getType() != XMaterial.PLAYER_HEAD.parseMaterial()) throw new ItemLoadException(Lang.MATERIAL_NEEDS_TO_BE_SKULL.get());
            SkullUtil.setSkullTexture(pr -> displayItem.updateSkullTexture((GameProfile)pr, !displayableItem), item, null, itemConfig.getString("skullowner"));
        } else if (item.getType().equals((Object)XMaterial.FIREWORK_ROCKET.parseMaterial())) {
            item = FireworkUtil.addEffect(item, itemConfig);
        } else if (itemConfig.contains("instrument")) {
            if (DynamicShopGUIPlus.getInstance().version < 119) throw new ItemLoadException("The instrument item option is only supported in version 1.19 and above");
            if (item.getType() != Material.GOAT_HORN) throw new ItemLoadException("To apply a horn sound type, the material needs to be a goat horn");
            item = Horn.setInstrument(item, itemConfig.getString("instrument"));
        } else if (itemConfig.contains("stew-effect")) {
            if (DynamicShopGUIPlus.getInstance().version < 114) throw new ItemLoadException("Effects on SuspiciousStews are only supported in version 1.14 and above");
            if (!(item.getItemMeta() instanceof SuspiciousStewMeta)) throw new ItemLoadException("To add a stew effect to an item, the material needs to be a 'SUSPICIOUS_STEW'.");
            SuspiciousStewMeta meta = (SuspiciousStewMeta)item.getItemMeta();
            meta.addCustomEffect(this.getStewEffect(itemConfig.getString("stew-effect")), true);
            item.setItemMeta((ItemMeta)meta);
        } else if (itemConfig.getBoolean("autosell")) {
            if (DynamicShopGUIPlus.getInstance().version < 116) throw new ItemLoadException("AutoSellChests are only supported on 1.16 and above");
            if (Bukkit.getPluginManager().getPlugin("AutoSellChests") == null) throw new ItemLoadException("AutoSellChests needs to be installed to create this item");
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(new NamespacedKey(Bukkit.getPluginManager().getPlugin("AutoSellChests"), "autosell"), PersistentDataType.INTEGER, 1);
            item.setItemMeta(meta);
        } else if (itemConfig.contains("armor-trim")) {
            if (DynamicShopGUIPlus.getInstance().version < 120) throw new ItemLoadException("ArmorTrim's are only supported in version 1.20 and above");
            new ArmorTrim(itemConfig.getString("armor-trim.type"), itemConfig.getString("armor-trim.pattern")).build(item);
        } else if (itemConfig.contains("ominous-strength")) {
            item = this.setOminousStrength(item, itemConfig.getInt("ominous-strength"));
        }
        if (itemConfig.contains("armorcolor")) {
            if (!(item.getItemMeta() instanceof LeatherArmorMeta)) throw new ItemLoadException(Lang.MATERIAL_NEEDS_TO_BE_LEATHER_ARMOR.get());
            LeatherArmorMeta meta = (LeatherArmorMeta)item.getItemMeta();
            meta.setColor(this.getRGBColor(itemConfig.getString("armorcolor"), itemConfig.getCurrentPath() + ".armorcolor"));
            item.setItemMeta((ItemMeta)meta);
        }
        if (itemConfig.contains("enchantment-glint") && itemConfig.getBoolean("enchantment-glint")) {
            ItemMeta itemMeta = item.getItemMeta();
            if (ServerInfo.supportsComponents()) {
                itemMeta.setEnchantmentGlintOverride(Boolean.valueOf(true));
            } else {
                itemMeta.addEnchant(XEnchantment.WATER_WORKER.parseEnchantment(), 1, false);
                itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
            }
            item.setItemMeta(itemMeta);
        }
        if (itemConfig.contains("stack-size")) {
            int stackSize = itemConfig.getInt("stack-size");
            if (stackSize < 1 || stackSize > 64) throw new ItemLoadException("Cannot set the stack size to %stack-size%, it needs to be a number between 1 and 64.".replace("%stack-size%", String.valueOf(stackSize)));
            if (!DynamicShopGUIPlus.getInstance().allowIllegalStacks && stackSize > item.getMaxStackSize()) {
                throw new ItemLoadException("Cannot set the stack size of item %material% to %stack-size% because it would exceed the vanilla max stack size.".replace("%material%", item.getType().toString()).replace("%stack-size%", String.valueOf(stackSize)));
            }
            item.setAmount(stackSize);
            return item;
        } else {
            if (displayItem == null || !(displayItem instanceof ShopItem) || ((ShopItem)displayItem).getStackSize() <= 1) return item;
            item.setAmount(Math.max(Math.min(((ShopItem)displayItem).getStackSize(), 64), 1));
        }
        return item;
    }

    private PotionEffect getStewEffect(String effect) throws ItemLoadException {
        Stew stew = Stew.get(effect);
        if (stew == null) {
            throw new ItemLoadException("Could not find a stew effect with name/id '" + effect + "'");
        }
        return stew.parseEffect();
    }

    private Material getRecipeMaterial(String recipe) throws ItemLoadException {
        ItemStack item;
        if (!recipe.isEmpty()) {
            Optional<XMaterial> mat = XMaterial.matchXMaterial(recipe);
            if (!mat.isPresent()) {
                throw new ItemLoadException(Lang.CANNOT_GET_RECIPE_MATERIAL.get());
            }
            item = mat.get().parseItem();
            if (item == null) {
                throw new ItemLoadException(Lang.RECIPE_MATERIAL_NOT_SUPPORTED.get());
            }
        } else {
            throw new ItemLoadException(Lang.NEED_RECIPE_MATERIAL.get());
        }
        return item.getType();
    }

    public org.bukkit.Color getRGBColor(String raw, String rgbPath) throws ItemLoadException {
        if ((raw = raw.replace("#", "")).startsWith("0x")) {
            raw = raw.substring(2);
        }
        try {
            Color c = Color.decode("#" + raw);
            return org.bukkit.Color.fromRGB((int)c.getRed(), (int)c.getGreen(), (int)c.getBlue());
        }
        catch (IllegalArgumentException e) {
            throw new ItemLoadException(Lang.RGB_COLOR_FORMATTED_WRONG.get().replace("%path%", rgbPath));
        }
    }

    private ItemStack setSkullOwner(ItemStack item, String skullOwner) throws ItemLoadException {
        if (!skullOwner.isEmpty()) {
            SkullMeta skullMeta = (SkullMeta)item.getItemMeta();
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer((String)skullOwner));
            item.setItemMeta((ItemMeta)skullMeta);
            return item;
        }
        throw new ItemLoadException(Lang.NEED_SKULLOWNER.get());
    }

    
    private ItemStack addSpawnerType(@Nullable ShopItem shopItem, ItemStack item, String entityType, String spawnerName) throws ItemLoadException {
        if (entityType == null) throw new ItemLoadException(Lang.ITEM_SPAWNERTYPE_NULL.get());
        if (entityType.isEmpty()) throw new ItemLoadException(Lang.NEED_ITEM_SPAWNERTYPE.get());
        Optional<EntityType> type = EntityTypes.matchEntityType(entityType);
        if (!type.isPresent()) {
            throw new ItemLoadException(Lang.ITEM_SPAWNERTYPE_NULL.get());
        }
        item = DynamicShopGUIPlus.getInstance().getSpawnerManager().getProvider().setSpawnedType(shopItem, item, type.get());
        if (spawnerName == null || spawnerName.isEmpty()) return item;
        DynamicShopGUIPlus.getInstance().addSpawnerName(type.get(), Lang.fromConfig(spawnerName));
        return item;
    }

    public ItemStack setOminousStrength(ItemStack item, int lvl) throws ItemLoadException {
        if (item.getType() != Material.OMINOUS_BOTTLE) {
            throw new ItemLoadException(Lang.INVALID_MATERIAL_FOR_ITEM_OPTION.get().replace("%material%", "OMINOUS_BOTTLE").replace("%item-option%", "ominous-strength"));
        }
        if (lvl < 1 || lvl > 5) {
            throw new ItemLoadException(Lang.INVALID_OMINOUS_BOTTLE_STRENGTH.get().replace("%strength%", String.valueOf(lvl)));
        }
        try {
            return (ItemStack)ServerInfo.invokeModernVersionMethod("setOminousStrength", item, lvl - 1);
        }
        catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            throw new ItemLoadException(Lang.ERROR_SETTING_ITEM_OPTION.get().replace("%item-option%", "ominous-strength") + "\n" + sw.toString());
        }
    }

    private ItemStack addRecipe(ItemStack item, NamespacedKey materialKey) {
        if (DynamicShopGUIPlus.getInstance().version >= 112) {
            KnowledgeBookMeta meta = (KnowledgeBookMeta)item.getItemMeta();
            if (meta != null) {
                meta.addRecipe(new NamespacedKey[]{materialKey});
            }
            item.setItemMeta((ItemMeta)meta);
        }
        return item;
    }

    public ItemStack setEnchantments(ItemStack item, String section, ConfigurationSection path) throws ItemLoadException {
        List<String> enchantments = path.getStringList(".enchantments");
        if (enchantments.isEmpty()) {
            return item;
        }
        for (String enchantment : enchantments) {
            try {
                Optional<XEnchantment> ench = XEnchantment.matchXEnchantment(enchantment.contains(":") ? enchantment.split(":")[0] : enchantment);
                if (!ench.isPresent()) {
                    throw new ItemLoadException(Lang.ITEM_ENCHANTMENT_NULL.get());
                }
                Enchantment enchant = ench.get().parseEnchantment();
                if (enchant == null) {
                    throw new ItemLoadException(Lang.ITEM_ENCHANTMENT_NOT_SUPPORTED.get());
                }
                try {
                    int strength = enchantment.contains(":") ? Integer.parseInt(enchantment.split(":")[1]) : 1;
                    item = this.setEnchantment(item, enchant, strength);
                }
                catch (NumberFormatException e) {
                    throw new ItemLoadException(Lang.ITEM_ENCHANTMENT_STRENGTH_NULL.get());
                }
            }
            catch (ItemLoadException e) {
                if (enchantments.size() <= 1) {
                    throw e;
                }
                SendMessage.warnMessage(e.getMessage());
                if (section != null) {
                    if (path.getCurrentPath().contains("pages")) {
                        SendMessage.errorShops(section, path.getCurrentPath() + ".enchantments." + enchantment);
                        continue;
                    }
                    SendMessage.errorSections(section, path.getCurrentPath() + ".enchantments." + enchantment);
                    continue;
                }
                SendMessage.errorItemConfig(path.getCurrentPath());
            }
        }
        return item;
    }

    private ItemStack setEnchantment(ItemStack item, Enchantment ench, int strength) {
        int maxLevel = ench.getMaxLevel();
        boolean enchantedBook = item.getType().equals((Object)XMaterial.ENCHANTED_BOOK.parseMaterial());
        if (strength > maxLevel && !DynamicShopGUIPlus.getInstance().allowUnsafeEnchants) {
            strength = maxLevel;
        }
        if (enchantedBook) {
            EnchantmentStorageMeta enchantmentStorageMeta = (EnchantmentStorageMeta)item.getItemMeta();
            enchantmentStorageMeta.addStoredEnchant(ench, strength, true);
            item.setItemMeta((ItemMeta)enchantmentStorageMeta);
        } else {
            item.addUnsafeEnchantment(ench, strength);
        }
        return item;
    }

    public ItemStack setPotionTypes(ItemStack item, List<String> potionTypes, String section, String path, boolean splash) throws ItemLoadException {
        if (potionTypes.isEmpty()) {
            return item;
        }
        for (String potionType : potionTypes) {
            try {
                if (PotionTypes.canHaveEffects(item.getType())) {
                    Optional<PotionTypes> type = PotionTypes.matchPotionType(potionType);
                    if (!type.isPresent()) {
                        throw new ItemLoadException(Lang.ITEM_POTIONTYPE_NULL.get());
                    }
                    if (!type.get().isSupported()) {
                        throw new ItemLoadException(Lang.POTIONTYPE_NOT_SUPPORTED.get());
                    }
                    type.get().addEffect(item, splash);
                    continue;
                }
                throw new ItemLoadException(Lang.MATERIAL_NEEDS_TO_BE_POTION.get());
            }
            catch (ItemLoadException e) {
                if (potionTypes.size() == 1) {
                    throw e;
                }
                SendMessage.warnMessage(e.getMessage());
                if (section != null) {
                    if (path.startsWith("pages") || path.startsWith("items")) {
                        SendMessage.errorShops(section, path + ".potiontypes." + potionType);
                        continue;
                    }
                    SendMessage.errorSections(section, path + ".potiontypes." + potionType);
                    continue;
                }
                SendMessage.errorItemConfig(path);
            }
        }
        return item;
    }

    public List<String> setDisplayLore(List<String> lore, ConfigurationSection itemConfig, @Nullable ShopItem shopItem) {
        List<String> l = new ArrayList<>();
        if (itemConfig.contains("displaylore")) {
            l = this.getLore(itemConfig.getStringList("displaylore"), itemConfig.getCurrentPath(), shopItem);
        } else if (itemConfig.contains("lore")) {
            l = this.getLore(itemConfig.getStringList("lore"), itemConfig.getCurrentPath(), shopItem);
        }
        if (shopItem != null && shopItem.isPriorLore()) {
            lore.addAll(0, l);
        } else {
            lore.addAll(l);
        }
        return lore;
    }

    public List<String> setLore(List<String> lore, ConfigurationSection itemConfig, @Nullable ShopItem shopItem) {
        if (itemConfig.contains("lore")) {
            return this.getLore(itemConfig.getStringList("lore"), itemConfig.getCurrentPath(), shopItem);
        }
        return lore;
    }

    private List<String> getLore(List<String> list, String itemPath, @Nullable ShopItem shopItem) {
        ArrayList<String> lore = new ArrayList<String>();
        if (!list.isEmpty()) {
            list.forEach(s -> {
                if (s.contains("%buyPrice%") && DynamicShopGUIPlus.getInstance().createItem.getBaseBuyPrice(itemPath) != null) {
                    s = s.replace("%buyPrice%", DynamicShopGUIPlus.getInstance().formatPrice(shopItem != null ? shopItem.getEcoType() : null, DynamicShopGUIPlus.getInstance().createItem.getBaseBuyPrice(itemPath)));
                }
                if (s.contains("%sellPrice%") && DynamicShopGUIPlus.getInstance().createItem.getBaseSellPrice(itemPath) != null) {
                    s = s.replace("%sellPrice%", DynamicShopGUIPlus.getInstance().formatPrice(shopItem != null ? shopItem.getEcoType() : null, DynamicShopGUIPlus.getInstance().createItem.getBaseSellPrice(itemPath)));
                }
                lore.add(ChatUtil.formatColors(s));
            });
        }
        return lore;
    }

    public ItemStack setName(ItemStack item, ConfigurationSection itemConfig) throws ItemLoadException {
        if (itemConfig.contains("name")) {
            String name = itemConfig.getString("name");
            if (name != null) {
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatUtil.formatColors(name));
                item.setItemMeta(meta);
                return item;
            }
            throw new ItemLoadException(Lang.ITEM_NAME_NULL.get());
        }
        return item;
    }

    public Translatable getDisplayname(ConfigurationSection itemConfig) throws ItemLoadException {
        if (itemConfig.contains("displayname")) {
            return Lang.fromConfig(itemConfig.getString("displayname"));
        }
        if (itemConfig.contains("name")) {
            return Lang.fromConfig(itemConfig.getString("name"));
        }
        return null;
    }

    public ItemStack setSectionItemOption(ItemStack item, String section, ConfigurationSection itemConfig) throws ItemLoadException {
        boolean splash;
        if (itemConfig.contains("potion-glow") && itemConfig.getBoolean("potion-glow")) {
            if (PotionTypes.canHaveEffects(item.getType())) {
                splash = XMaterial.matchXMaterial(itemConfig.getString("material")).get() == XMaterial.SPLASH_POTION;
                PotionTypes.HEALING.addEffect(item, splash);
            } else {
                throw new ItemLoadException(Lang.MATERIAL_NEEDS_TO_BE_POTION.get());
            }
        }
        if (itemConfig.contains("potiontypes")) {
            splash = itemConfig.getString("material").equalsIgnoreCase("SPLASH_POTION");
            this.setPotionTypes(item, itemConfig.getStringList("potiontypes"), section, itemConfig.getCurrentPath(), splash);
        }
        if (itemConfig.contains("armor-trim")) {
            if (DynamicShopGUIPlus.getInstance().version >= 120) {
                new ArmorTrim(itemConfig.getString("armor-trim.type"), itemConfig.getString("armor-trim.pattern")).build(item);
            } else {
                throw new ItemLoadException("ArmorTrim's are only supported in version 1.20 and above");
            }
        }
        if (itemConfig.contains("enchantment-glint") && itemConfig.getBoolean("enchantment-glint")) {
            ItemMeta itemMeta = item.getItemMeta();
            if (ServerInfo.supportsComponents()) {
                itemMeta.setEnchantmentGlintOverride(Boolean.valueOf(true));
            } else {
                itemMeta.addEnchant(XEnchantment.WATER_WORKER.parseEnchantment(), 1, false);
            }
            item.setItemMeta(itemMeta);
        }
        if (itemConfig.contains("skullowner")) {
            if (item.getType() == XMaterial.PLAYER_HEAD.parseMaterial()) {
                SkullUtil.setSkullTexture(item, null, itemConfig.getString("skullowner"), true);
            } else {
                throw new ItemLoadException(Lang.MATERIAL_NEEDS_TO_BE_SKULL.get());
            }
        }
        if (itemConfig.contains("armorcolor")) {
            if (item.getItemMeta() instanceof LeatherArmorMeta) {
                LeatherArmorMeta meta = (LeatherArmorMeta)item.getItemMeta();
                meta.setColor(this.getRGBColor(itemConfig.getString("armorcolor"), itemConfig.getCurrentPath() + ".armorcolor"));
                item.setItemMeta((ItemMeta)meta);
            } else {
                throw new ItemLoadException(Lang.MATERIAL_NEEDS_TO_BE_LEATHER_ARMOR.get());
            }
        }
        if (itemConfig.contains("stack-size")) {
            int stackSize = itemConfig.getInt("stack-size");
            if (!DynamicShopGUIPlus.getInstance().allowIllegalStacks && stackSize > item.getMaxStackSize()) {
                throw new ItemLoadException("Cannot set the stack size of item %material% to %stack-size% because it would exceed the vanilla max stack size.".replace("%material%", item.getType().toString()).replace("%stack-size%", String.valueOf(stackSize)));
            }
            item.setAmount(stackSize);
        }
        return item;
    }

    public static enum Horn {
        PONDER("ponder_goat_horn"),
        SING("sing_goat_horn"),
        SEEK("seek_goat_horn"),
        FEEL("feel_goat_horn"),
        ADMIRE("admire_goat_horn"),
        CALL("call_goat_horn"),
        YEARN("yearn_goat_horn"),
        DREAM("dream_goat_horn");

        private String key;

        private Horn(String key) {
            this.key = key;
        }

        public String getKey() {
            return "minecraft:" + this.key;
        }

        public static Horn getFromString(String name) throws IllegalArgumentException {
            return Horn.valueOf(name.toUpperCase(Locale.ENGLISH));
        }

        public static ItemStack setInstrument(ItemStack item, String sound) throws ItemLoadException {
            if (ServerInfo.getVersion().newerOrEqualAs(ServerInfo.Version.v1_20_R2)) {
                MusicInstrument instrument = (MusicInstrument)Registry.INSTRUMENT.get(NamespacedKey.minecraft((String)((sound = sound.toLowerCase(Locale.ROOT)) + "_goat_horn")));
                if (instrument == null) {
                    throw new ItemLoadException("Could not find a instrument called by that name '%instrument%'".replace("%instrument%", sound));
                }
                MusicInstrumentMeta meta = (MusicInstrumentMeta)item.getItemMeta();
                meta.setInstrument(instrument);
                item.setItemMeta((ItemMeta)meta);
            } else {
                try {
                    Horn type = Horn.getFromString(sound);
                    item = new ItemStack(DynamicShopGUIPlus.getInstance().versionHandler.setNBTString(item, "instrument", type.getKey()));
                }
                catch (IllegalArgumentException e) {
                    throw new ItemLoadException("Could not find a instrument called by that name '%instrument%'".replace("%instrument%", sound));
                }
            }
            return item;
        }

        public static List<String> getValues() {
            if (ServerInfo.getVersion().newerOrEqualAs(ServerInfo.Version.v1_20_R2)) {
                return Registry.INSTRUMENT.stream().map(instr -> instr.getKey().getKey().split("_goat_horn")[0]).collect(Collectors.toList());
            }
            return Arrays.stream(Horn.values()).map(Enum::name).collect(Collectors.toList());
        }
    }

    public static class ArmorTrim {
        private final TrimMaterial type;
        private final TrimPattern pattern;

        public ArrayList<String> getTypes() {
            ArrayList<String> mats = new ArrayList<String>();
            for (Field field : TrimMaterial.class.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) || !field.getName().equals(field.getName().toUpperCase(Locale.ROOT))) continue;
                mats.add(field.getName());
            }
            return mats;
        }

        public ArrayList<String> getPatterns() {
            ArrayList<String> patterns = new ArrayList<String>();
            for (Field field : TrimPattern.class.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) || !field.getName().equals(field.getName().toUpperCase(Locale.ROOT))) continue;
                patterns.add(field.getName().toUpperCase(Locale.ROOT));
            }
            return patterns;
        }

        public ArmorTrim() {
            this.type = null;
            this.pattern = null;
        }

        public ArmorTrim(String type, String pattern) throws ItemLoadException {
            if (type == null || pattern == null) {
                throw new ItemLoadException("To apply a armor trim, it is required to specify both trim material and trim pattern!");
            }
            this.type = this.getTrimType(type);
            this.pattern = this.getTrimPattern(pattern);
        }

        public TrimMaterial getTrimType(String name) throws ItemLoadException {
            try {
                Field material = TrimMaterial.class.getDeclaredField(name.toUpperCase(Locale.ROOT).replace(" ", "_"));
                material.setAccessible(true);
                return (TrimMaterial)material.get(null);
            }
            catch (IllegalAccessException | NoSuchFieldException e) {
                throw new ItemLoadException("Invalid armor trim type such as '" + name + "'. Valid values are: " + Arrays.toString(this.getTypes().toArray()));
            }
        }

        public TrimPattern getTrimPattern(String name) throws ItemLoadException {
            try {
                Field pattern = TrimPattern.class.getDeclaredField(name.toUpperCase(Locale.ROOT).replace(" ", "_"));
                pattern.setAccessible(true);
                return (TrimPattern)pattern.get(null);
            }
            catch (IllegalAccessException | NoSuchFieldException e) {
                throw new ItemLoadException("Invalid armor trim pattern such as '" + name + "'. Valid values are: " + Arrays.toString(this.getPatterns().toArray()));
            }
        }

        public ItemStack build(ItemStack stack) throws ItemLoadException {
            if (!(stack.getItemMeta() instanceof ArmorMeta)) {
                throw new ItemLoadException("To add a armor trim to an item, the material needs to be a armor piece.");
            }
            ArmorMeta meta = (ArmorMeta)stack.getItemMeta();
            meta.setTrim(new org.bukkit.inventory.meta.trim.ArmorTrim(this.type, this.pattern));
            stack.setItemMeta((ItemMeta)meta);
            return stack;
        }
    }

    public static enum Stew {
        NIGHT_VISION(0, 100, new String[0]),
        JUMP(1, 120, "JUMP_BOOST"),
        WEAKNESS(2, 180, new String[0]),
        BLINDNESS(3, 160, new String[0]),
        POISON(4, 240, new String[0]),
        SATURATION(6, 12, new String[0]),
        FIRE_RESISTANCE(7, 80, new String[0]),
        REGENERATION(8, 160, "REGEN"),
        WITHER(9, 160, new String[0]);

        private final int id;
        private final int duration;
        private final String[] aliases;

        private Stew(int id, int duration, String ... aliases) {
            this.id = id;
            this.duration = duration;
            this.aliases = aliases;
        }

        public static Stew get(String s) {
            for (Stew stew : Stew.values()) {
                if (!stew.name().equalsIgnoreCase(s.replace(" ", "_")) && !stew.anyMatchAliases(s)) continue;
                return stew;
            }
            try {
                int id = Integer.parseInt(s);
                for (Stew stew : Stew.values()) {
                    if (stew.id != id) continue;
                    return stew;
                }
            }
            catch (NumberFormatException numberFormatException) {
                
            }
            return null;
        }

        private boolean anyMatchAliases(String name) {
            for (String alias : this.aliases) {
                if (!name.equalsIgnoreCase(alias.replace(" ", "_"))) continue;
                return true;
            }
            return false;
        }

        public PotionEffect parseEffect() {
            try {
                PotionEffectType type = PotionEffectType.getByName((String)this.name());
                if (type == null) {
                    type = PotionTypes.matchPotionType(this.name()).get().parsePotionEffectType();
                }
                return new PotionEffect(type, this.duration, 1);
            }
            catch (NullPointerException e) {
                return null;
            }
        }
    }
}

