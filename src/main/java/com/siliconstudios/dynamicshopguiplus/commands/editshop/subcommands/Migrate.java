

package com.siliconstudios.dynamicshopguiplus.commands.editshop.subcommands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.Methods;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.SubCommad;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.CalculateAmount;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.util.PermissionsCache;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class Migrate
extends SubCommad {
    private final Methods methods;
    private final DynamicShopGUIPlus plugin;
    private final Map<String, List<String>> supported = this.getSupportedFiles();
    String pluginName;
    String image;

    public Migrate(DynamicShopGUIPlus plugin, Methods methods) {
        this.plugin = plugin;
        this.methods = methods;
    }

    @Override
    public String getName() {
        return "migrate";
    }

    @Override
    public String getDescription() {
        return Lang.EDITSHOP_MIGRATE_SUBCOMMAND_DESC.get().getLegacy();
    }

    @Override
    public String getSyntax() {
        return Lang.EDITSHOP_MIGRATE_SUBCOMMAND_SYNTAX.get().getLegacy();
    }

    @Override
    public boolean hasPermission(CommandSender source) {
        return PermissionsCache.hasPermission(source, "DynamicShopGUIPlus.eshop." + this.getName());
    }

    @Override
    public void perform(Object logger, String[] args) {
        if (args.length > 1) {
            if (!this.supported.containsKey(args[1].toLowerCase(Locale.ENGLISH))) {
                SendMessage.sendMessage(logger, Lang.PLUGIN_NOT_FOUND.get());
                return;
            }
        } else {
            SendMessage.sendMessage(logger, this.getSyntax());
            return;
        }
        this.pluginName = args[1].toLowerCase(Locale.ENGLISH);
        if (args.length <= 2) {
            SendMessage.sendMessage(logger, this.getSyntax());
            return;
        }
        this.image = args[2].toLowerCase(Locale.ENGLISH);
        boolean override = false;
        boolean confirm = false;
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--override")) {
                override = true;
                continue;
            }
            if (!arg.equalsIgnoreCase("--confirm")) continue;
            confirm = true;
        }
        if (override && !confirm) {
            SendMessage.warnMessage(logger, "This command will override any existing shop data. We suggest creating backups of your current shops.yml and sections.yml before using this command.");
            SendMessage.infoMessage(logger, "Use this command with the " + ChatColor.WHITE + "--confirm " + ChatColor.GREEN + "flag to continue migration using this command.");
            return;
        }
        boolean finalOverride = override;
        this.plugin.runTaskAsync(() -> this.migrateData(logger, finalOverride));
    }

    private Map<String, List<String>> getSupportedFiles() {
        HashMap<String, List<String>> supported = new HashMap<String, List<String>>();
        supported.put("dynamicshopguiplus", Arrays.asList("V1toV2"));
        return supported;
    }

    private void migrateData(Object logger, boolean override) {
        if (this.pluginName.equalsIgnoreCase("dynamicshopguiplus")) {
            File sections;
            int migrated = 0;
            boolean changes = false;
            HashSet<String> exist = new HashSet<String>();
            File shops = new File(this.plugin.getDataFolder(), "shops.yml");
            if (shops.exists()) {
                Object shopFolder;
                SendMessage.logDebugMessage("Started migrating data from shops.yml....");
                YamlConfiguration config = this.plugin.loadConfiguration(shops, "shops.yml");
                if (override && ((File)(shopFolder = new File(this.plugin.getDataFolder() + File.separator + "shops"))).exists() && ((File)shopFolder).isDirectory()) {
                    for (File conf : ((File)shopFolder).listFiles()) {
                        try {
                            Files.delete(conf.toPath());
                            SendMessage.infoMessage("Deleted " + ((File)shopFolder).getName() + " shops config while migrating using the --override flag.");
                        }
                        catch (IOException e) {
                            SendMessage.warnMessage("A unknown exception occurred while removing " + ((File)shopFolder).getName() + " shops config while migrating using the --override flag.");
                            e.printStackTrace();
                        }
                    }
                }
                for (String section : config.getKeys(false)) {
                    if (ConfigManager.getShop(section) != null) {
                        if (!override) {
                            exist.add(section);
                            SendMessage.logDebugMessage("Skipping section " + section + " because it already exists...");
                            continue;
                        }
                        SendMessage.warnMessage(logger, "Overriding " + section + " from shops.yml");
                    }
                    YamlConfiguration newConfig = new YamlConfiguration();
                    int page = 1;
                    LinkedHashMap<String, Object> items = new LinkedHashMap<String, Object>();
                    HashMap pages = new HashMap();
                    boolean max = false;
                    for (String itemLoc : config.getConfigurationSection(section).getKeys(false)) {
                        int slot = config.getInt(section + "." + itemLoc + ".slot");
                        if (slot != 0) {
                            max = true;
                            int p2 = (int)Math.ceil((double)slot / 45.0);
                            if (pages.containsKey(p2)) {
                                ((Map)pages.get(p2)).put(itemLoc, config.get(section + "." + itemLoc));
                            } else {
                                for (int i = 1; i < p2; ++i) {
                                    if (pages.containsKey(i)) continue;
                                    pages.put(i, new HashMap());
                                }
                                LinkedHashMap<String, Object> item = new LinkedHashMap<String, Object>();
                                item.put(itemLoc, config.get(section + "." + itemLoc));
                                pages.put(p2, item);
                            }
                        }
                        if (items.size() == 45) {
                            pages.put(page, items);
                            items = new LinkedHashMap();
                            items.put(itemLoc, config.get(section + "." + itemLoc));
                            ++page;
                            continue;
                        }
                        items.put(itemLoc, config.get(section + "." + itemLoc));
                    }
                    if (!items.isEmpty()) {
                        if (pages.containsKey(page)) {
                            ((Map)pages.get(page)).putAll(items);
                        } else {
                            pages.put(page, items);
                        }
                    }
                    if (pages.isEmpty()) {
                        SendMessage.warnMessage("Skipping " + section + " from shops.yml because no data to migrate could be found.");
                        continue;
                    }
                    changes = true;
                    SendMessage.logDebugMessage("Migrating section " + section + " with " + pages.values().stream().mapToInt(p -> ((java.util.Map<?,?>)p).size()).sum() + " items.");
                    for (int i = 0; i < pages.size(); ++i) {
                        String s = "page" + (i + 1);
                        newConfig.set("pages." + s + ".gui-rows", (Object)(max ? 6 : CalculateAmount.getSlots(((Map)pages.get(i + 1)).size()) / 9));
                        newConfig.set("pages." + s + ".items", pages.get(i + 1));
                        SendMessage.logDebugMessage("Created new page for section " + section + " with " + ((Map)pages.get(i + 1)).size() + " items.");
                    }
                    File newShop = new File(this.plugin.getDataFolder() + File.separator + "shops" + File.separator, section + ".yml");
                    try {
                        newConfig.save(newShop);
                        ++migrated;
                        SendMessage.infoMessage("Migrated " + section + " with " + pages.size() + " pages and a total of " + pages.values().stream().mapToInt(p -> ((java.util.Map<?,?>)p).size()).sum() + " items and saved to " + newShop.getAbsolutePath());
                    }
                    catch (IOException e) {
                        if (logger instanceof Player) {
                            SendMessage.warnMessage("A unknown error occurred while migrating shop '" + section + "', look in your console for more details.");
                        }
                        SendMessage.warnMessage("A unknown error occurred while saving new shop file " + newShop.getName());
                        e.printStackTrace();
                    }
                }
            }
            if ((sections = new File(this.plugin.getDataFolder(), "sections.yml")).exists()) {
                File sectionFolder;
                SendMessage.logDebugMessage("Started migrating data from sections.yml....");
                YamlConfiguration config = this.plugin.loadConfiguration(sections, "sections.yml");
                if (override && (sectionFolder = new File(this.plugin.getDataFolder() + File.separator + "sections")).exists() && sectionFolder.isDirectory()) {
                    for (File conf : sectionFolder.listFiles()) {
                        try {
                            Files.delete(conf.toPath());
                            SendMessage.infoMessage("Deleted " + sectionFolder.getName() + " sections config while migrating using the --override flag.");
                        }
                        catch (IOException e) {
                            SendMessage.warnMessage("A unknown exception occurred while removing " + sectionFolder.getName() + " sections config while migrating using the --override flag.");
                            e.printStackTrace();
                        }
                    }
                }
                if (config.contains("ShopSections")) {
                    for (String section : config.getConfigurationSection("ShopSections").getKeys(false)) {
                        if (ConfigManager.getSection(section) != null) {
                            if (!override) {
                                exist.add(section);
                                SendMessage.logDebugMessage("Skipping section " + section + " because it already exists...");
                                continue;
                            }
                            SendMessage.warnMessage(logger, "Overriding " + section + " from sections.yml");
                        }
                        YamlConfiguration newConfig = new YamlConfiguration();
                        SendMessage.logDebugMessage("Migrating section " + section + "...");
                        Map<String, Object> newData = this.getSectionData(config, section);
                        if (newData.isEmpty()) {
                            SendMessage.warnMessage("Skipping " + section + " from sections.yml because no data to migrate could be found.");
                            continue;
                        }
                        changes = true;
                        for (String key : newData.keySet()) {
                            newConfig.set(key, newData.get(key));
                        }
                        File newSection = new File(this.plugin.getDataFolder() + File.separator + "sections" + File.separator, section + ".yml");
                        try {
                            newConfig.save(newSection);
                            ++migrated;
                            SendMessage.infoMessage("Migrated " + section + " and saved to " + newSection.getAbsolutePath());
                        }
                        catch (IOException e) {
                            if (logger instanceof Player) {
                                SendMessage.warnMessage("A unknown error occurred while migrating shop '" + section + "', look in your console for more details.");
                            }
                            SendMessage.warnMessage("A unknown error occurred while saving new shop file " + newSection.getName());
                            e.printStackTrace();
                        }
                    }
                } else {
                    SendMessage.warnMessage("No sections found in sections.yml");
                }
            }
            if (!override && !exist.isEmpty()) {
                SendMessage.warnMessage(logger, "Failed to migrate data for shops " + Arrays.toString(exist.toArray()) + " because they already exist. If you wish to override them, you can use this command with the " + ChatColor.WHITE + "--override " + ChatColor.RED + "flag.");
            }
            if (!changes) {
                SendMessage.warnMessage(logger, "No changes found to migrate!");
                return;
            }
            SendMessage.infoMessage(logger, "Completed migrating data for " + migrated + " shops and sections which can now be found in " + ChatColor.WHITE + "/sections/<shopName>.yml" + ChatColor.GREEN + " and " + ChatColor.WHITE + "/shops/<shopName.yml>");
            SendMessage.infoMessage(logger, "Use command " + ChatColor.WHITE + "/sreload" + ChatColor.GREEN + " to load the migrated shops.");
        }
    }

    private Map<String, Object> getSectionData(YamlConfiguration config, String section) {
        Set keys = config.getConfigurationSection("ShopSections." + section).getKeys(false);
        LinkedHashMap<String, Object> newData = new LinkedHashMap<String, Object>();
        keys.remove("enable");
        newData.put("enable", config.getBoolean("ShopSections." + section + ".enable", true));
        keys.remove("slot");
        newData.put("slot", config.getInt("ShopSections." + section + ".slot"));
        keys.remove("title");
        newData.put("title", config.getString("ShopSections." + section + ".title", ""));
        keys.remove("hidden");
        newData.put("hidden", config.getBoolean("ShopSections." + section + ".hidden", false));
        keys.remove("subSection");
        newData.put("sub-section", config.getBoolean("ShopSections." + section + ".subSection", false));
        keys.remove("fill-item");
        newData.put("display-item", config.getBoolean("ShopSections." + section + ".fill-item", false));
        if (keys.remove("economy")) {
            newData.put("economy", config.getString("ShopSections." + section + ".economy"));
        }
        if (!keys.contains("fillItem")) {
            newData.put("fill-item.material", "AIR");
        } else {
            for (String key : config.getConfigurationSection("ShopSections." + section + ".fillItem").getKeys(false)) {
                newData.put("fill-item." + key, config.get("ShopSections." + section + ".fillItem." + key));
            }
        }
        newData.put("nav-bar.mode", "INHERIT");
        HashMap<String, Object> data = new HashMap<String, Object>();
        for (Object o : keys) {
            String key = (String) o;
            if (key.equalsIgnoreCase("displayname")) {
                data.put("name", config.getString("ShopSections." + section + ".displayname"));
            }
            data.put(key, config.get("ShopSections." + section + "." + key));
        }
        newData.put("item", data);
        return newData;
    }

    @Override
    public List<String> getTabCompletion(String[] args) {
        switch (args.length) {
            case 2: {
                if (!args[1].isEmpty()) {
                    ArrayList<String> completions = new ArrayList<String>();
                    StringUtil.copyPartialMatches((String)args[1], this.supported.keySet(), completions);
                    Collections.sort(completions);
                    return completions;
                }
                return new ArrayList<String>(this.supported.keySet());
            }
            case 3: {
                if (this.supported.containsKey(args[1].toLowerCase(Locale.ENGLISH))) {
                    if (!args[2].isEmpty()) {
                        ArrayList<String> completions = new ArrayList<String>();
                        StringUtil.copyPartialMatches((String)args[2], (Iterable)this.supported.get(args[1].toLowerCase(Locale.ENGLISH)), completions);
                        Collections.sort(completions);
                        return completions;
                    }
                    return this.supported.get(args[1].toLowerCase(Locale.ENGLISH));
                }
                return null;
            }
        }
        return null;
    }
}

