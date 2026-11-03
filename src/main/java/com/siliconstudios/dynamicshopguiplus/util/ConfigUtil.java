

package com.siliconstudios.dynamicshopguiplus.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.files.config.Config;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.yaml.snakeyaml.Yaml;

public class ConfigUtil {
    public static void updateConfigs() {
        int pluginVer;
        int configVer;
        try {
            configVer = Integer.parseInt(ConfigManager.getConfig().getString("config-version", "2.0.0.0").replace(".", ""));
            if (String.valueOf(configVer).length() < 4) {
                while (String.valueOf(configVer).length() != 4) {
                    configVer = Integer.parseInt(configVer + "0");
                }
            } else if (String.valueOf(configVer).length() > 4) {
                while (String.valueOf(configVer).length() != 4) {
                    configVer = Integer.parseInt(String.valueOf(configVer).substring(0, String.valueOf(configVer).length() - 1));
                }
            }
        }
        catch (NumberFormatException e) {
            SendMessage.errorMessage("Failed to check for config updates because the version is invalid(" + ConfigManager.getConfig().getString("config-version", "1.0.0") + "), please do not change this version as it might break the plugin");
            return;
        }
        if (configVer != (pluginVer = Integer.parseInt(DynamicShopGUIPlus.getInstance().loadConfiguration(new BufferedReader(new InputStreamReader(DynamicShopGUIPlus.getInstance().getResource("config.yml"), StandardCharsets.UTF_8)), "config.yml").getString("config-version").replace(".", "")))) {
            SendMessage.infoMessage(Lang.UPDATING_CONFIGS.get());
            try {
                if (configVer <= 1270) {
                    ConfigUtil.migrateV1toV2();
                    configVer = 2000;
                }
                ConfigUtil.updateV2Config(configVer);
            }
            catch (Exception e) {
                SendMessage.errorMessage("Failed to update the configs");
                e.printStackTrace();
            }
        }
    }

    private static void updateV2Config(int configVer) {
        Collection<String> keys;
        if (configVer == 2000) {
            for (String string : ConfigManager.getShops()) {
                boolean updated = false;
                for (String itemLoc : DynamicShopGUIPlus.getInstance().getConfigManager().getItemsRaw(string)) {
                    keys = ConfigManager.getShop(string).getConfigurationSection("pages." + itemLoc).getKeys(false);
                    if (keys.contains("max-buy") && ConfigManager.getShop(string).getInt("pages." + itemLoc + ".max-buy") == 1) {
                        ConfigManager.getShop(string).set("pages." + itemLoc + ".max-buy", -1);
                        updated = true;
                    }
                    if (!keys.contains("max-sell") || ConfigManager.getShop(string).getInt("pages." + itemLoc + ".max-sell") != 1) continue;
                    ConfigManager.getShop(string).set("pages." + itemLoc + ".max-sell", -1);
                    updated = true;
                }
                if (!updated) continue;
                ConfigManager.saveShop(string);
            }
            configVer = 2001;
        }
        if (configVer == 2001) {
            if (ConfigManager.getConfig().contains("season-price-modifiers")) {
                boolean updated = false;
                for (String season : ConfigManager.getConfig().getConfigurationSection("season-price-modifiers").getKeys(false)) {
                    block5: for (String itemLoc : ConfigManager.getConfig().getConfigurationSection("season-price-modifiers." + season).getKeys(false)) {
                        if (ConfigManager.getConfig().getConfigurationSection("season-price-modifiers." + season + "." + itemLoc) != null || !DynamicShopGUIPlus.getInstance().getShopSections().contains(itemLoc.split(":")[0])) continue;
                        if (itemLoc.split(":").length == 2) {
                            keys = DynamicShopGUIPlus.getInstance().getConfigManager().getItemsRaw(itemLoc.split(":")[0]);
                            for (String string : keys) {
                                if (!string.split("\\.")[2].equalsIgnoreCase(itemLoc.split(":")[1])) continue;
                                updated = true;
                                ConfigManager.getConfig().set("season-price-modifiers." + season + "." + itemLoc.split(":")[0] + "." + string.replace(".", ":"), ConfigManager.getConfig().get("season-price-modifiers." + season + "." + itemLoc));
                                ConfigManager.getConfig().set("season-price-modifiers." + season + "." + itemLoc, null);
                                continue block5;
                            }
                            continue;
                        }
                        if (itemLoc.split(":").length != 1) continue;
                        updated = true;
                        ConfigManager.getConfig().set("season-price-modifiers." + season + "." + itemLoc + ".all", ConfigManager.getConfig().get("season-price-modifiers." + season + "." + itemLoc));
                    }
                }
                if (updated) {
                    ConfigManager.saveConfig();
                }
            }
            configVer = 2002;
        }
        if (configVer == 2002) {
            Object contents;
            ConfigUtil.createBackup(ConfigManager.getConfig().getFilePath());
            if (!ConfigManager.getConfig().contains("main-menu-nav-bar.items") && (contents = ConfigManager.getConfig().getConfigurationSection("main-menu-nav-bar")) != null) {
                ConfigManager.getConfig().set("main-menu-nav-bar", null);
                ConfigManager.getConfig().set("main-menu-nav-bar.enabled", ((ConfigurationSection)contents).getBoolean("enabled", true));
                ((ConfigurationSection)contents).set("enabled", null);
                ConfigUtil.setSection(ConfigManager.getConfig(), (ConfigurationSection)contents, "main-menu-nav-bar.items");
            }
            if (!ConfigManager.getConfig().contains("shops-nav-bar.items") && (contents = ConfigManager.getConfig().getConfigurationSection("shops-nav-bar")) != null) {
                ConfigManager.getConfig().set("shops-nav-bar", null);
                ConfigManager.getConfig().set("shops-nav-bar.enabled", true);
                ConfigUtil.setSection(ConfigManager.getConfig(), (ConfigurationSection)contents, "shops-nav-bar.items");
            }
            if (!ConfigManager.getConfig().contains("transaction-screens-nav-bar.items") && (contents = ConfigManager.getConfig().getConfigurationSection("transaction-screens-nav-bar")) != null) {
                ConfigManager.getConfig().set("transaction-screens-nav-bar", null);
                ConfigManager.getConfig().set("transaction-screens-nav-bar.enabled", true);
                ConfigUtil.setSection(ConfigManager.getConfig(), (ConfigurationSection)contents, "transaction-screens-nav-bar.items");
            }
            if (!ConfigManager.getConfig().contains("sellgui-nav-bar.items") && (contents = ConfigManager.getConfig().getConfigurationSection("sellgui-nav-bar")) != null) {
                ConfigManager.getConfig().set("sellgui-nav-bar", null);
                ConfigManager.getConfig().set("sellgui-nav-bar.enabled", true);
                ConfigUtil.setSection(ConfigManager.getConfig(), (ConfigurationSection)contents, "sellgui-nav-bar.items");
            }
            configVer = 2010;
        }
        if (configVer == 2010) {
            if (ConfigManager.getConfig().getBoolean("disableBackButtonWithShopSectionCommand")) {
                ConfigManager.getConfig().set("disable-back.direct-shop-command", true);
                ConfigManager.getConfig().set("disable-back.direct-shop-command-console", true);
                ConfigManager.getConfig().set("disable-back.sub-sections", false);
            }
            configVer = 2011;
        }
        if (configVer == 2011) {
            for (String string : ConfigManager.getConfig().getConfigurationSection("buy-screen.items").getKeys(false)) {
                ConfigurationSection item = ConfigManager.getConfig().getConfigurationSection("buy-screen.items." + string);
                if (item == null || !item.getString("type", "NORMAL").equals("SELECTED_ITEM") || item.contains("lore")) continue;
                ConfigManager.getConfig().set("buy-screen.items." + string + ".lore", Lang.TRANSACTION_SCREEN_TOTAL_AMOUNT.get().getLines());
            }
            for (String string : ConfigManager.getConfig().getConfigurationSection("sell-screen.items").getKeys(false)) {
                ConfigurationSection item = ConfigManager.getConfig().getConfigurationSection("sell-screen.items." + string);
                if (item == null || !item.getString("type", "NORMAL").equals("SELECTED_ITEM") || item.contains("lore")) continue;
                ConfigManager.getConfig().set("sell-screen.items." + string + ".lore", Lang.TRANSACTION_SCREEN_TOTAL_AMOUNT.get().getLines());
            }
            for (String string : ConfigManager.getConfig().getConfigurationSection("buy-stacks-screen.items").getKeys(false)) {
                ConfigurationSection item = ConfigManager.getConfig().getConfigurationSection("buy-stacks-screen.items." + string);
                if (item == null || !item.getString("type", "NORMAL").equals("SELECTED_ITEM") || item.contains("lore")) continue;
                ConfigManager.getConfig().set("buy-stacks-screen.items." + string + ".lore", Lang.TRANSACTION_SCREEN_TOTAL_AMOUNT.get().getLines());
            }
            configVer = 2012;
        }
        if (configVer == 2012) {
            for (String string : ConfigManager.getSections()) {
                try {
                    Config c = ConfigManager.getSection(string);
                    String slots = c.getString("slot");
                    if (slots != null) {
                        ArrayList<Integer> slot = DynamicShopGUIPlus.getInstance().calculateAmount.getSlots(slots);
                        if (slot.size() == 1) {
                            c.set("slot", slot.get(0) + 2);
                        } else {
                            c.set("slot", slot.stream().map(i -> String.valueOf(i + 2)).collect(Collectors.joining(",")));
                        }
                    }
                    c.save();
                }
                catch (Exception exception) {}
            }
            configVer = 2013;
        }
        if (configVer == 2013) {
            ConfigManager.getConfig().set("use-paper-meta", false);
            configVer = 2014;
        }
        if (configVer == 2014) {
            ConfigManager.getConfig().set("sort-items-by", "SHOP_LOAD_ORDER");
            configVer = 2015;
        }
        if (configVer == 2015) {
            boolean shopStands = ConfigManager.getConfig().getBoolean("shop-stands");
            ConfigManager.getConfig().set("shop-stands.enable", shopStands);
            ConfigManager.getConfig().set("shop-stands.holograms", true);
            configVer = 2016;
        }
        if (configVer == 2016) {
            ConfigManager.getConfig().set("damaged-items.enabled", false);
            configVer = 2017;
        }
        if (configVer == 2017) {
            ConfigurationSection configurationSection;
            String ecoType = ConfigManager.getConfig().getString("economy-provider", "VAULT");
            if (ecoType.equalsIgnoreCase("COINS_ENGINE")) {
                ConfigManager.getConfig().set("economy-provider", "EXCELLENT_ECONOMY");
            }
            if ((configurationSection = ConfigManager.getConfig().getConfigurationSection("currency-formats")) != null) {
                for (String key : configurationSection.getKeys(false)) {
                    ConfigurationSection format;
                    if (!key.split(":")[0].equalsIgnoreCase("coins-engine") || (format = configurationSection.getConfigurationSection(key)) == null) continue;
                    ConfigUtil.setSection(ConfigManager.getConfig(), format, "currency-formats.excellent-economy" + (key.contains(":") ? ":" + key.split(":", 2)[1] : ""));
                    ConfigManager.getConfig().set("currency-formats." + key, null);
                }
            }
            for (String section : ConfigManager.getSections()) {
                boolean updated = false;
                Config config = ConfigManager.getSection(section);
                if (config.getString("economy", "VAULT").equalsIgnoreCase("COINS_ENGINE")) {
                    config.set("economy", "EXCELLENT_ECONOMY");
                    updated = true;
                }
                if (!updated) continue;
                ConfigManager.saveSection(section);
            }
            configVer = 2018;
        }
        if (configVer == 2018) {
            File file;
            File czLang = new File(DynamicShopGUIPlus.getInstance().getDataFolder() + File.separator + "LanguageFiles", "lang-cz.yml");
            if (czLang.exists() && !new File(DynamicShopGUIPlus.getInstance().getDataFolder() + File.separator + "LanguageFiles", "lang-cs.yml").exists()) {
                czLang.renameTo(new File(DynamicShopGUIPlus.getInstance().getDataFolder() + File.separator + "LanguageFiles", "lang-cs.yml"));
            }
            if ((file = new File(DynamicShopGUIPlus.getInstance().getDataFolder() + File.separator + "LanguageFiles", "lang-jp.yml")).exists() && !new File(DynamicShopGUIPlus.getInstance().getDataFolder() + File.separator + "LanguageFiles", "lang-ja.yml").exists()) {
                file.renameTo(new File(DynamicShopGUIPlus.getInstance().getDataFolder() + File.separator + "LanguageFiles", "lang-ja.yml"));
            }
            ConfigManager.getConfig().set("bedrock-forms.enable", false);
            configVer = 2019;
        }
        String cVer = ConfigUtil.getConfigVersion(configVer);
        ConfigManager.getConfig().set("config-version", cVer);
        ConfigManager.saveConfig();
        SendMessage.infoMessage("Successfully updated configs to v" + cVer);
    }

    private static void setSection(Config config, ConfigurationSection section, String path) {
        for (Map.Entry key : section.getValues(true).entrySet()) {
            if (key.getValue() instanceof MemorySection) continue;
            config.set(path + "." + (String)key.getKey(), key.getValue());
        }
    }

    private static void migrateV1toV2() {
        if (ConfigManager.getConfig().contains("mainshop-size")) {
            ConfigManager.getConfig().set("main-menu.gui-rows", ConfigManager.getConfig().get("mainshop-size"));
            ConfigManager.getConfig().set("mainshop-size", null);
        }
        if (!ConfigManager.getConfig().contains("main-menu-nav-bar.enabled")) {
            ConfigManager.getConfig().set("main-menu-nav-bar.enabled", true);
        }
        if (ConfigManager.getConfig().contains("mainshop-size")) {
            HashMap<String, Map> modifiers = new HashMap<String, Map>();
            for (String season : ConfigManager.getConfig().getConfigurationSection("season-price-modifiers").getKeys(false)) {
                for (String modifier : ConfigManager.getConfig().getConfigurationSection("season-price-modifiers." + season).getKeys(false)) {
                    Map shopModifiers;
                    if (modifier.contains(":")) {
                        shopModifiers = modifiers.getOrDefault(season + "." + modifier.split(":")[0], new HashMap());
                        shopModifiers.put(modifier.split(":")[1], ConfigManager.getConfig().getString(modifier));
                        modifiers.put(season + "." + modifier.split(":")[0], shopModifiers);
                        continue;
                    }
                    shopModifiers = modifiers.getOrDefault(season + "." + modifier, new HashMap());
                    shopModifiers.put("all", ConfigManager.getConfig().getString(modifier));
                    modifiers.put(season + "." + modifier, shopModifiers);
                }
            }
            if (!modifiers.isEmpty()) {
                for (String shop : modifiers.keySet()) {
                    ConfigManager.getConfig().set("season-price-modifiers." + shop, modifiers.get(shop));
                }
            }
        }
        ConfigManager.getConfig().set("config-version", "2.0.0.0");
        ConfigManager.saveConfig();
    }

    private static String getConfigVersion(int version) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < String.valueOf(version).toCharArray().length; ++i) {
            if (String.valueOf(version).toCharArray().length != i + 1) {
                builder.append(String.valueOf(version).toCharArray()[i]).append(".");
                continue;
            }
            builder.append(String.valueOf(version).toCharArray()[i]);
        }
        return builder.toString();
    }

    public static void createBackup(Path path) {
        String fileName = path.getFileName().toString();
        SendMessage.infoMessage(Lang.CREATING_BACKUP.get().replace("%fileName%", fileName));
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
        LocalDateTime now = LocalDateTime.now();
        Path backupDir = Paths.get(DynamicShopGUIPlus.getInstance().getDataFolder() + "/backups/", new String[0]);
        if (!Files.exists(backupDir, new LinkOption[0]) || !Files.isDirectory(backupDir, new LinkOption[0])) {
            try {
                Files.createDirectory(backupDir, new FileAttribute[0]);
            }
            catch (FileAlreadyExistsException fileAlreadyExistsException) {
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            try {
                Files.copy(path, Paths.get(backupDir.toString(), fileName.split("\\.")[0] + " " + dtf.format(now) + ".yml"), new CopyOption[0]);
            }
            catch (FileAlreadyExistsException e) {
                int i = 1;
                String newFileName = fileName.split("\\.")[0] + " " + dtf.format(now) + " " + i + ".yml";
                while (new File(backupDir.toString(), newFileName).exists()) {
                    ++i;
                }
                Files.copy(path, Paths.get(backupDir.toString(), newFileName), new CopyOption[0]);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    public static boolean loadConfig() {
        if (new File(DynamicShopGUIPlus.getInstance().getDataFolder(), "config.yml").exists()) {
            try {
                YamlConfiguration config = DynamicShopGUIPlus.getInstance().loadConfiguration(new File(DynamicShopGUIPlus.getInstance().getDataFolder(), "config.yml"), "config.yml");
                if (config == null) {
                    return false;
                }
                BufferedReader input = new BufferedReader(new InputStreamReader(DynamicShopGUIPlus.getInstance().getResource("config.yml"), StandardCharsets.UTF_8));
                List<String> defaults = input.lines().collect(Collectors.toList());
                input.close();
                StringBuilder builder = new StringBuilder();
                for (String s : defaults) {
                    builder.append(s).append("\n");
                }
                Files.write(Paths.get(DynamicShopGUIPlus.getInstance().getDataFolder() + "/config.yml", new String[0]), builder.toString().getBytes(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                File file = new File(DynamicShopGUIPlus.getInstance().getDataFolder(), "config.yml");
                YamlConfiguration conf = DynamicShopGUIPlus.getInstance().loadConfiguration(file, "config.yml");
                for (String str : config.getKeys(false)) {
                    conf.set(str, config.get(str));
                }
                ConfigUtil.save((FileConfiguration)conf, file);
                DynamicShopGUIPlus.getInstance().reloadConfig();
                if (DynamicShopGUIPlus.getInstance().badYMLParse == null || !DynamicShopGUIPlus.getInstance().badYMLParse.equals("config.yml")) return true;
                DynamicShopGUIPlus.getInstance().badYMLParse = null;
                return true;
            }
            catch (IOException e) {
                SendMessage.errorMessage("Cannot read config.yml config because it is mis-configured, use a online Yaml parser with the error underneath here to find out the cause of the problem and to solve it. If you cannot find the cause yourself, join our discord support server that can be found at a plugin page of DynamicShopGUIPlus.");
                e.printStackTrace();
                DynamicShopGUIPlus.getInstance().badYMLParse = "config.yml";
                return false;
            }
        } else {
            DynamicShopGUIPlus.getInstance().saveDefaultConfig();
        }
        return true;
    }

    public static void save(FileConfiguration fileConfiguration, File file) {
        if (fileConfiguration == null) {
            SendMessage.errorMessage(Lang.COULD_NOT_SAVE_CONFIG.get().replace("%fileName%", file.getName()));
            return;
        }
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader((InputStream)new FileInputStream(file), StandardCharsets.UTF_8));
            Map<String, String> comments = ConfigUtil.getComments(input.lines().collect(Collectors.toList()));
            input.close();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter((OutputStream)new FileOutputStream(file), StandardCharsets.UTF_8));
            for (String key : fileConfiguration.getKeys(true)) {
                Object obj;
                String[] keys = key.split("\\.");
                String actualKey = keys[keys.length - 1];
                String comment = comments.remove(key);
                StringBuilder prefixBuilder = new StringBuilder();
                int indents = keys.length - 1;
                ConfigUtil.appendPrefixSpaces(prefixBuilder, indents);
                String prefixSpaces = prefixBuilder.toString();
                if (comment != null) {
                    writer.write(comment);
                }
                if ((obj = fileConfiguration.get(key)) instanceof ConfigurationSerializable) {
                    writer.write(prefixSpaces + actualKey + ": " + new Yaml().dump((Object)((ConfigurationSerializable)obj).serialize()));
                    continue;
                }
                if (obj instanceof String || obj instanceof Character) {
                    if (obj instanceof String) {
                        String s = (String)obj;
                        obj = s.replace("\n", "\\n");
                    }
                    writer.write(prefixSpaces + actualKey + ": " + new Yaml().dump(obj).replace("\n ", ""));
                    continue;
                }
                if (obj instanceof List) {
                    writer.write(ConfigUtil.getListAsString((List)obj, actualKey, prefixSpaces));
                    continue;
                }
                if (obj instanceof MemorySection) {
                    writer.write(prefixSpaces + actualKey + ":\n");
                    continue;
                }
                writer.write(prefixSpaces + actualKey + ": " + new Yaml().dump(obj));
            }
            String danglingComments = comments.get(null);
            if (danglingComments != null) {
                writer.write(danglingComments);
            }
            writer.close();
        }
        catch (IOException e) {
            SendMessage.errorMessage(Lang.COULD_NOT_SAVE_CONFIG.get().replace("%fileName%", file.getName()));
            e.printStackTrace();
        }
    }

    private static Map<String, String> getComments(List<String> lines) {
        HashMap<String, String> comments = new HashMap<String, String>();
        StringBuilder builder = new StringBuilder();
        StringBuilder keyBuilder = new StringBuilder();
        int lastLineIndentCount = 0;
        for (String line : lines) {
            if (line != null && line.trim().startsWith("-")) continue;
            if (line == null || line.trim().equals("") || line.trim().startsWith("#")) {
                builder.append(line).append("\n");
                continue;
            }
            lastLineIndentCount = ConfigUtil.setFullKey(keyBuilder, line, lastLineIndentCount);
            if (keyBuilder.length() <= 0) continue;
            comments.put(keyBuilder.toString(), builder.toString());
            builder.setLength(0);
        }
        if (builder.length() > 0) {
            comments.put(null, builder.toString());
        }
        return comments;
    }

    private static int setFullKey(StringBuilder keyBuilder, String configLine, int lastLineIndentCount) {
        int currentIndents = ConfigUtil.countIndents(configLine);
        String key = configLine.trim().split(":")[0];
        if (keyBuilder.length() == 0) {
            keyBuilder.append(key);
        } else if (currentIndents == lastLineIndentCount) {
            ConfigUtil.removeLastKey(keyBuilder);
            if (keyBuilder.length() > 0) {
                keyBuilder.append(".");
            }
            keyBuilder.append(key);
        } else if (currentIndents > lastLineIndentCount) {
            keyBuilder.append(".").append(key);
        } else {
            int difference = lastLineIndentCount - currentIndents;
            for (int i = 0; i < difference + 1; ++i) {
                ConfigUtil.removeLastKey(keyBuilder);
            }
            if (keyBuilder.length() > 0) {
                keyBuilder.append(".");
            }
            keyBuilder.append(key);
        }
        return currentIndents;
    }

    private static String getListAsString(List list, String actualKey, String prefixSpaces) {
        StringBuilder builder = new StringBuilder(prefixSpaces).append(actualKey).append(":");
        if (list.isEmpty()) {
            builder.append(" []\n");
            return builder.toString();
        }
        builder.append("\n");
        for (int i = 0; i < list.size(); ++i) {
            Object o = list.get(i);
            if (o instanceof String || o instanceof Character) {
                String s = o.toString();
                if (s.contains("\"") || s.contains("'")) {
                    s = s.replace("'", "''");
                }
                builder.append(prefixSpaces).append("- '").append(s).append("'");
            } else if (o instanceof List) {
                builder.append(prefixSpaces).append("- ").append(new Yaml().dump(o));
            } else {
                builder.append(prefixSpaces).append("- ").append(o);
            }
            if (i == list.size()) continue;
            builder.append("\n");
        }
        return builder.toString();
    }

    private static int countIndents(String s) {
        int spaces = 0;
        for (char c : s.toCharArray()) {
            if (c != ' ') break;
            ++spaces;
        }
        return spaces / 2;
    }

    private static void removeLastKey(StringBuilder keyBuilder) {
        String temp = keyBuilder.toString();
        String[] keys = temp.split("\\.");
        if (keys.length == 1) {
            keyBuilder.setLength(0);
            return;
        }
        temp = temp.substring(0, temp.length() - keys[keys.length - 1].length() - 1);
        keyBuilder.setLength(temp.length());
    }

    private static String getPrefixSpaces(int indents) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < indents; ++i) {
            builder.append("  ");
        }
        return builder.toString();
    }

    private static void appendPrefixSpaces(StringBuilder builder, int indents) {
        builder.append(ConfigUtil.getPrefixSpaces(indents));
    }
}

