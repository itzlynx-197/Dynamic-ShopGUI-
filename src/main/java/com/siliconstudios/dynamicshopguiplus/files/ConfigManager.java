

package com.siliconstudios.dynamicshopguiplus.files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.files.config.CommentedConfig;
import com.siliconstudios.dynamicshopguiplus.files.config.Config;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.util.ConfigUtil;
import com.siliconstudios.dynamicshopguiplus.util.NBTMaps;
import com.siliconstudios.dynamicshopguiplus.util.ServerInfo;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.ConfigLoadException;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.ConfigSaveException;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigManager {
    private final DynamicShopGUIPlus plugin;
    public static String badYaml = null;
    private static Config config;
    private final File sectionsFolder;
    private final File shopsFolder;
    private static final Map<String, Config> sections;
    private static final Map<String, Config> shops;

    public ConfigManager(DynamicShopGUIPlus plugin) {
        this.plugin = plugin;
        this.sectionsFolder = new File(this.plugin.getDataFolder() + "/sections/");
        this.shopsFolder = new File(this.plugin.getDataFolder() + "/shops/");
    }

    public static Config getConfig() {
        return config;
    }

    public static String getPathIgnoreCase(String path) {
        String[] parts = path.split("\\.");
        ConfigurationSection section = config;
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < parts.length; ++i) {
            String part = parts[i];
            if (section != null && !section.contains(part)) {
                for (String s : section.getKeys(false)) {
                    if (!s.replace('_', '-').equalsIgnoreCase(part)) continue;
                    part = s;
                    break;
                }
            }
            if (i > 0) {
                key.append('.');
            }
            key.append(part);
            if (section == null) continue;
            section = section.getConfigurationSection(part);
        }
        return key.toString();
    }

    public static void saveConfig() {
        try {
            config.save();
            config.reload();
        }
        catch (ConfigLoadException | ConfigSaveException e) {
            SendMessage.errorMessage(e.getMessage());
            badYaml = "config";
        }
    }

    public static Config getSection(String section) {
        return sections.get(section);
    }

    public static Set<String> getSections() {
        return sections.keySet();
    }

    public static void saveSection(String section) {
        try {
            Config config = sections.get(section);
            config.save();
            config.reload();
        }
        catch (ConfigLoadException | ConfigSaveException e) {
            SendMessage.errorMessage(e.getMessage());
            badYaml = section;
        }
    }

    public Config createSectionConfig(String section) {
        File file = new File(this.plugin.getDataFolder() + File.separator + "sections" + File.separator, section + ".yml");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            sections.put(section, this.loadConf(file, false));
            return sections.get(section);
        }
        catch (IOException e) {
            SendMessage.warnMessage("A unknown IOE exception occurred while creating a new section config at " + file.getAbsolutePath());
            return null;
        }
    }

    public void deleteSection(String section) {
        try {
            Config config = sections.remove(section);
            ConfigUtil.createBackup(config.getFilePath());
            Files.deleteIfExists(config.getFilePath());
        }
        catch (IOException e) {
            SendMessage.errorMessage("Failed to delete '" + section + "' sections config");
            e.printStackTrace();
        }
    }

    public static Config getShop(String shop) {
        return shops.get(shop);
    }

    public static Set<String> getShops() {
        return shops.keySet();
    }

    public static void saveShop(String shop) {
        try {
            Config config = shops.get(shop);
            config.save();
            config.reload();
        }
        catch (ConfigLoadException | ConfigSaveException e) {
            SendMessage.errorMessage(e.getMessage());
            badYaml = shop;
        }
    }

    public List<String> getItemsRaw(String shop) {
        ArrayList<String> raw = new ArrayList<String>();
        for (String p : this.getPages(shop, false)) {
            shops.get(shop).getConfigurationSection("pages." + p + ".items").getKeys(false).forEach(i -> raw.add(p + ".items." + i));
        }
        return raw;
    }

    public List<String> getPages(String shop, boolean canBeEmpty) {
        LinkedList<String> pages = new LinkedList<String>();
        Config config = shops.get(shop);
        if (config.contains("pages")) {
            for (String p : config.getConfigurationSection("pages").getKeys(false)) {
                if (!canBeEmpty && !config.contains("pages." + p + ".items")) continue;
                pages.add(p);
            }
        }
        return pages;
    }

    public void deleteShop(String itemPath) {
        try {
            Config config = shops.remove(itemPath.split("\\.")[0]);
            ConfigUtil.createBackup(config.getFilePath());
            Files.deleteIfExists(config.getFilePath());
        }
        catch (IOException e) {
            SendMessage.errorMessage("Failed to delete '" + itemPath.split("\\.")[0] + "' shops config");
            e.printStackTrace();
        }
    }

    public Config createShopConfig(String shop) {
        File file = new File(this.plugin.getDataFolder() + File.separator + "shops" + File.separator, shop + ".yml");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            shops.put(shop, this.loadConf(file, false));
            return shops.get(shop);
        }
        catch (IOException e) {
            SendMessage.warnMessage("A unknown IOE exception occurred while creating a new shop config at " + file.getAbsolutePath());
            return null;
        }
    }

    public void deleteShopsConfig(String shop) {
        try {
            Config config = shops.remove(shop);
            ConfigUtil.createBackup(config.getFilePath());
            Files.deleteIfExists(config.getFilePath());
        }
        catch (IOException e) {
            SendMessage.errorMessage("Failed to delete '" + shop + "' shops config");
            e.printStackTrace();
        }
    }

    public static String getBadYaml() {
        return badYaml;
    }

    public boolean init() {
        if (badYaml == null) {
            config.setDef((Configuration)YamlConfiguration.loadConfiguration((Reader)new InputStreamReader(this.plugin.getResource("config.yml"))));
        }
        try {
            this.loadSections();
            this.loadShops();
        }
        catch (ConfigLoadException | ConfigSaveException e) {
            SendMessage.errorMessage(e.getMessage());
            return false;
        }
        return true;
    }

    public boolean reload() {
        try {
            if (config != null) {
                File conf = config.getFilePath().toFile();
                if (!conf.exists()) {
                    this.plugin.saveResource(conf.getName(), false);
                    config.reload();
                    this.initIgnoredTags();
                } else {
                    config.reload();
                    config.save();
                }
            } else {
                this.loadConfig();
            }
            if (badYaml != null && badYaml.equals("config")) {
                badYaml = null;
            }
        }
        catch (ConfigLoadException | ConfigSaveException e) {
            SendMessage.errorMessage(e.getMessage());
            badYaml = "config";
            return false;
        }
        try {
            this.loadSections();
            this.loadShops();
        }
        catch (ConfigLoadException | ConfigSaveException e) {
            SendMessage.errorMessage(e.getMessage());
            return false;
        }
        return true;
    }

    public void loadConfig() {
        boolean created = false;
        File conf = new File(this.plugin.getDataFolder(), "config.yml");
        if (!conf.exists()) {
            this.plugin.saveResource("config.yml", false);
            created = true;
        }
        if ((config = this.loadConf(conf, true)) == null) {
            return;
        }
        try {
            config.reload();
            config.save();
        }
        catch (ConfigLoadException | ConfigSaveException e) {
            SendMessage.errorMessage(e.getMessage());
            badYaml = "config";
        }
        if (created) {
            this.initIgnoredTags();
        }
    }

    private void initIgnoredTags() {
        if (!ServerInfo.supportsComponents()) {
            return;
        }
        List ignoredTags = config.getStringList("sold-items-ignored-NBTtags");
        for (int i = 0; i < ignoredTags.size(); ++i) {
            String tag = (String)ignoredTags.get(i);
            ignoredTags.set(i, NBTMaps.getTag(tag, null, "sold-items-ignored-NBTtags", false));
        }
        config.set("sold-items-ignored-NBTtags", ignoredTags);
        ConfigManager.saveConfig();
    }

    private void loadSections() throws ConfigSaveException, ConfigLoadException {
        if (!sections.isEmpty()) {
            sections.clear();
        }
        int loaded = 0;
        if (!this.sectionsFolder.exists() || !this.sectionsFolder.isDirectory()) {
            String layoutVersion = ServerInfo.getVersion().newerOrEqualAs(ServerInfo.Version.v26_1) ? "2" : "1";
            this.saveResources(this.sectionsFolder, layoutVersion + "-sections");
            this.createExampleSection();
            try {
                Files.move(new File(this.plugin.getDataFolder() + File.separator + layoutVersion + "-sections" + File.separator).toPath(), this.sectionsFolder.toPath(), new CopyOption[0]);
            }
            catch (IOException e) {
                throw new ConfigLoadException("A unknown IOE occurred while renaming sections directory.\n" + this.stackTraceToString(e));
            }
        }
        for (File file : this.sectionsFolder.listFiles()) {
            Config section;
            if (file.isDirectory()) {
                Map<String, Config> nested = this.loadFromDir(file, false, 1);
                loaded += nested.size();
                sections.putAll(nested);
                continue;
            }
            if (!file.getName().split("\\.")[1].equals("yml") || (section = this.loadConf(file, false)) == null) continue;
            ++loaded;
            sections.put(file.getName().split("\\.")[0], section);
        }
        SendMessage.infoMessage(Lang.LOADED_SECTION_CONFIGURATIONS.get().replace("%total%", String.valueOf(loaded)));
    }

    private void saveSections() throws ConfigSaveException {
        try {
            int len;
            InputStream stream = this.getClass().getClassLoader().getResourceAsStream("example.yml");
            FileOutputStream out = new FileOutputStream(this.plugin.getDataFolder().getAbsolutePath() + "/example.yml");
            byte[] buf = new byte[1024];
            while ((len = stream.read(buf)) > 0) {
                ((OutputStream)out).write(buf, 0, len);
            }
            ((OutputStream)out).close();
            stream.close();
            SendMessage.infoMessage(Lang.DEFAULT_SECTIONS_CREATED.get().replace("%path%", this.sectionsFolder.getAbsolutePath()));
        }
        catch (IOException e) {
            throw new ConfigSaveException("A unknown IOE exception occurred while creating the default sections folder at " + this.sectionsFolder.getPath() + ":\n" + this.stackTraceToString(e));
        }
    }

    private void loadShops() throws ConfigSaveException, ConfigLoadException {
        if (!shops.isEmpty()) {
            shops.clear();
        }
        int loaded = 0;
        if (!this.shopsFolder.exists() || !this.shopsFolder.isDirectory()) {
            String fileName = this.plugin.version + "-shops";
            if (this.getClass().getClassLoader().getResourceAsStream(fileName) == null) {
                fileName = (ServerInfo.getVersion().newerOrEqualAs(ServerInfo.Version.v26_1) ? "261" : "121") + "-shops";
                SendMessage.warnMessage(Lang.CREATING_LATEST_SHOPS.get().replace("%version%", String.valueOf(this.plugin.version)));
            }
            this.saveResources(this.shopsFolder, fileName);
            try {
                Files.move(new File(this.plugin.getDataFolder() + File.separator + fileName + File.separator).toPath(), this.shopsFolder.toPath(), new CopyOption[0]);
                SendMessage.infoMessage(Lang.DEFAULT_SHOPS_CREATED.get().replace("%version%", String.valueOf(this.plugin.version)).replace("%path%", this.shopsFolder.getAbsolutePath()));
            }
            catch (IOException e) {
                throw new ConfigLoadException("A unknown IOE occurred while renaming shops directory.\n" + this.stackTraceToString(e));
            }
            this.createExampleShop();
        }
        for (File file : this.shopsFolder.listFiles()) {
            Config shop;
            if (file.isDirectory()) {
                Map<String, Config> nested = this.loadFromDir(file, false, 1);
                loaded += nested.size();
                shops.putAll(nested);
                continue;
            }
            if (!file.getName().split("\\.")[1].equals("yml") || (shop = this.loadConf(file, false)) == null) continue;
            ++loaded;
            shops.put(file.getName().split("\\.")[0], shop);
        }
        SendMessage.infoMessage(Lang.LOADED_SHOP_CONFIGURATIONS.get().replace("%total%", String.valueOf(loaded)));
    }

    private Map<String, Config> loadFromDir(File dir, boolean comments, int deep) {
        HashMap<String, Config> configs = new HashMap<String, Config>();
        for (File file : dir.listFiles()) {
            Config conf;
            if (file.isDirectory() && deep <= 2) {
                return this.loadFromDir(file, comments, ++deep);
            }
            if (!file.getName().split("\\.")[1].equals("yml") || (conf = this.loadConf(file, comments)) == null) continue;
            configs.put(file.getName().split("\\.")[0], conf);
        }
        return configs;
    }

    public void validate() {
        SendMessage.infoMessage(Lang.UPDATING_SHOP_SETTINGS.get());
        ArrayList<String> shopShops = new ArrayList<String>(ConfigManager.getShops());
        ArrayList<String> shopSections = new ArrayList<String>(ConfigManager.getSections());
        Set displayItems = shopSections.stream().filter(s -> sections.get(s).getBoolean("display-item")).collect(Collectors.toSet());
        if (!displayItems.isEmpty()) {
            shopSections.removeAll(displayItems);
            shopShops.removeAll(displayItems);
        }
        if (shopShops.size() != shopSections.size()) {
            Config config;
            if (shopShops.size() < shopSections.size()) {
                for (String section : shopSections) {
                    if (shopShops.contains(section)) continue;
                    SendMessage.warnMessage(Lang.CREATING_NEW_SHOP_CONFIG.get().replace("%section%", section));
                    config = this.createShopConfig(section);
                    if (config == null) continue;
                    config.set("pages.page1.items.example-item", this.getExampleItem(section));
                    ConfigManager.saveShop(section);
                    shopShops.add(section);
                    shops.put(section, config);
                }
            } else {
                for (String section : shopShops) {
                    if (shopSections.contains(section)) continue;
                    SendMessage.warnMessage(Lang.CREATING_NEW_SECTION_CONFIG.get().replace("%section%", section));
                    config = this.createSectionConfig(section);
                    if (config == null) {
                        return;
                    }
                    Config finalConfig = config;
                    this.getExampleShop(section).forEach((arg_0, arg_1) -> ((Config)finalConfig).set(arg_0, arg_1));
                    ConfigManager.saveSection(section);
                    shopSections.add(section);
                    sections.put(section, config);
                    this.plugin.getSectionTitles().put(section, Lang.fromConfig("&a" + section));
                }
            }
        }
        for (String config : new ArrayList<String>(shopSections)) {
            if (shopShops.contains(config)) continue;
            String misMatchedFile = shopShops.stream().filter(s -> !shopSections.contains(s)).findFirst().orElse(null);
            if (misMatchedFile != null) {
                shops.remove(misMatchedFile);
            }
            shopSections.remove(config);
            sections.remove(config);
            SendMessage.errorMessage("Failed to match section file with shop file " + config + ".yml");
            SendMessage.errorMessage("Both files inside the /shops/ and /sections/ directory should have the same naming schema for this shop category, skipping...");
        }
        ArrayList<String> disabledSections = new ArrayList<String>();
        for (String section : shopSections) {
            if (sections.get(section).getBoolean("enable", true)) continue;
            disabledSections.add(section);
        }
        shopSections.removeAll(disabledSections);
        shopSections.addAll(displayItems);
        this.plugin.setShopSections(shopSections);
    }

    private Map<String, Object> getExampleShop(String section) {
        HashMap<String, Object> keys = new HashMap<String, Object>();
        keys.put("enable", true);
        keys.put("slot", 0);
        keys.put("display-item", false);
        keys.put("hidden", false);
        keys.put("title", "&c&lExample section");
        keys.put("fill-item.material", "AIR");
        keys.put("nav-bar.mode", "INHERIT");
        keys.put("item.material", "BEACON");
        keys.put("item.displayname", "&a" + section);
        keys.put("item.enchantment-glint", false);
        keys.put("item.lore", Arrays.asList("&4This example section was automatically added", "&4because the shops config still exists,", "&4but there was no sections config found", "&4for section &c'" + section + "'.", "&fThis is a commun mistake while removing shops,", "&fmake sure both section and shop config files are removed."));
        return keys;
    }

    private Map<String, Object> getExampleItem(String section) {
        HashMap<String, Object> keys = new HashMap<String, Object>();
        keys.put("material", "STICK");
        keys.put("name", "&c&lPower stick v2");
        keys.put("displayname", "&c&lExample power stick");
        keys.put("buy", 1234);
        keys.put("sell", 123);
        keys.put("lore", Arrays.asList("&4This example item", "&4was added to shop", "&4because the shops.yml config", "&4did not contain section &c" + section));
        keys.put("enchantments", Arrays.asList("KNOCKBACK:2", "SHARPNESS:5", "FIRE_ASPECT:5", "BANE_OF_ARTHROPODS:5", "SMITE:5", "UNBREAKING:3", "LOOTING:5", "MENDING", "SWEEPING_EDGE:3"));
        keys.put("slot", 5);
        keys.put("stack-size", 2);
        keys.put("hidePricingLore", true);
        return keys;
    }

    private Config loadConf(File file, boolean comments) {
        try {
            if (badYaml != null && badYaml.equals(file.getName())) {
                badYaml = null;
            }
            return comments ? new CommentedConfig(this.plugin, file, this.plugin.getResource(file.getName())) : new Config(file);
        }
        catch (ConfigLoadException e) {
            SendMessage.errorMessage(e.getMessage());
            badYaml = file.getName();
            return null;
        }
    }

    public void saveResources(File folder, String path) throws ConfigSaveException {
        try {
            JarFile file = new JarFile(DynamicShopGUIPlus.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            Enumeration<JarEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                JarEntry je = entries.nextElement();
                if (!je.getName().startsWith(path) || je.getName().endsWith("/")) continue;
                this.plugin.saveResource(je.getName(), false);
            }
        }
        catch (IOException | URISyntaxException e) {
            throw new ConfigSaveException("A unknown IOE exception occurred while saving the default " + path + " folder at " + folder.getAbsolutePath() + ":\n" + this.stackTraceToString(e));
        }
    }

    private void createExampleShop() {
        if (!new File(this.plugin.getDataFolder(), "ExampleShop.yml").exists()) {
            this.plugin.saveResource("ExampleShop.yml", false);
        }
    }

    private void createExampleSection() {
        if (!new File(this.plugin.getDataFolder(), "ExampleSection.yml").exists()) {
            this.plugin.saveResource("ExampleSection.yml", false);
        }
    }

    private String stackTraceToString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    static {
        sections = new HashMap<String, Config>();
        shops = new HashMap<String, Config>();
    }

    public static final class CFile {
        final Type type;
        final String name;

        public CFile(Type type, String name) {
            this.type = type;
            this.name = name;
        }

        public String getName() {
            switch (this.type.ordinal()) {
                case 2: {
                    return "shops/" + this.name + ".yml";
                }
                case 1: {
                    return "sections/" + this.name + ".yml";
                }
            }
            return this.name + ".yml";
        }

        public void save() {
            switch (this.type.ordinal()) {
                case 2: {
                    ConfigManager.saveShop(this.name);
                }
                case 1: {
                    ConfigManager.saveSection(this.name);
                }
                case 0: {
                    ConfigManager.saveConfig();
                }
            }
        }

        public static enum Type {
            CONFIG,
            SECTION,
            SHOP;

        }
    }
}

