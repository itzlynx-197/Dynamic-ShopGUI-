

package com.siliconstudios.dynamicshopguiplus.commands.editshop.subcommands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.Methods;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.SubCommad;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.objects.ShopItem;
import com.siliconstudios.dynamicshopguiplus.objects.shops.ShopSection;
import com.siliconstudios.dynamicshopguiplus.util.PermissionsCache;
import com.siliconstudios.dynamicshopguiplus.util.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

public class Import
extends SubCommad {
    private final Methods methods;
    private final DynamicShopGUIPlus plugin;
    private Object ess;
    private final Map<String, List<String>> supported;
    String pluginName;
    String fileName;
    File file;

    public Import(DynamicShopGUIPlus plugin, Methods methods) {
        this.plugin = plugin;
        this.methods = methods;
        this.supported = this.getSupportedFiles();
    }

    @Override
    public String getName() {
        return "import";
    }

    @Override
    public String getDescription() {
        return Lang.EDITSHOP_IMPORT_SUBCOMMAND_DESC.get().getLegacy();
    }

    @Override
    public String getSyntax() {
        return Lang.EDITSHOP_IMPORT_SUBCOMMAND_SYNTAX.get().getLegacy();
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
        if (args.length > 2) {
            this.fileName = args[2].toLowerCase(Locale.ENGLISH);
            this.file = this.getFile(this.pluginName, this.fileName);
            if (this.file == null || !this.supported.get(this.pluginName).contains(this.fileName)) {
                SendMessage.sendMessage(logger, Lang.FILE_NOT_FOUND.get().replace("%fileName%", this.fileName).replace("%pluginName%", this.pluginName));
                return;
            }
        } else {
            SendMessage.sendMessage(logger, this.getSyntax());
            return;
        }
        this.importChanges(logger);
    }

    private Map<String, List<String>> getSupportedFiles() {
        HashMap<String, List<String>> supported = new HashMap<String, List<String>>();
        File dataFolder = new File(this.plugin.getDataFolder().getParent() + "/Essentials/");
        if (dataFolder.exists() && dataFolder.isDirectory()) {
            supported.put("essentials", Arrays.asList("worth.yml"));
        }
        return supported;
    }

    private File getFile(String pluginName, String fileName) {
        switch (pluginName.toLowerCase(Locale.ENGLISH)) {
            case "essentials": {
                File config = new File(this.plugin.getDataFolder().getParent() + "/Essentials/" + fileName);
                return config.exists() ? config : null;
            }
            case "dynamicshopguiplus": {
                File config1 = new File(this.plugin.getDataFolder() + "/" + fileName);
                return config1.exists() ? config1 : null;
            }
        }
        return null;
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

    private XMaterial getMaterial(String item) {
        try {
            ItemStack is = null;
            return XMaterial.matchXMaterial(is);
        }
        catch (Exception e) {
            return null;
        }
    }

    private List<XMaterial> getMaterials(ConfigurationSection config, String item) {
        Optional<XMaterial> mat;
        ArrayList<XMaterial> mats = new ArrayList<XMaterial>();
        int d = 0;
        while ((mat = XMaterial.matchXMaterial(item + ":" + d)).isPresent() && mat.get().parseItem() != null && config.getDouble("worth." + item + ".*", -5.0) != -5.0) {
            if (!config.getConfigurationSection("worth." + item).getKeys(false).contains(String.valueOf(d))) {
                mats.add(mat.get());
            }
            ++d;
        }
        return mats;
    }

    private XMaterial getMaterial(ConfigurationSection config, String item, String data) {
        Optional<XMaterial> mat = XMaterial.matchXMaterial(item + ":" + data);
        if (!mat.isPresent() || mat.get().parseItem() == null || config.getDouble("worth." + item + "." + data, -5.0) == -5.0) {
            return null;
        }
        return mat.get();
    }

    private void importChanges(Object logger) {
        if (false) {
            if (false) {
                SendMessage.sendMessage(logger, ChatColor.RED + "Could not load essentials, please make sure EssentialsX is installed with the latest version.");
                return;
            }
            SendMessage.infoMessage(logger, "Creating backup of current shop layout...");
            try {
                FileOutputStream outputStream = new FileOutputStream(this.createZipFile() + ".zip");
                ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
                this.zipLayout(zipOutputStream);
                zipOutputStream.close();
                outputStream.close();
            }
            catch (IOException e) {
                SendMessage.warnMessage(logger, "Failed to create backup of current shop layout.");
                e.printStackTrace();
                return;
            }
            switch (this.fileName) {
                case "worth.yml": {
                    YamlConfiguration config1 = this.plugin.loadConfiguration(this.file, this.fileName);
                    HashMap<XMaterial, Double> mats = new HashMap<XMaterial, Double>();
                    for (String item : config1.getConfigurationSection("worth").getKeys(false)) {
                        if (config1.getDouble("worth." + item, -5.0) == -5.0) {
                            for (String data : config1.getConfigurationSection("worth." + item).getKeys(false)) {
                                if (data.equals("*")) {
                                    this.getMaterials((ConfigurationSection)config1, item).forEach(arg_0 -> Import.lambda$importChanges$0(mats, (FileConfiguration)config1, item, arg_0));
                                    continue;
                                }
                                XMaterial mat = this.getMaterial((ConfigurationSection)config1, item, data);
                                if (mat == null) continue;
                                mats.put(mat, config1.getDouble("worth." + item + "." + data));
                            }
                            continue;
                        }
                        XMaterial mat = this.getMaterial(item);
                        if (mat == null) continue;
                        mats.put(mat, config1.getDouble("worth." + item));
                    }
                    int count = 0;
                    HashSet<String> updated = new HashSet<String>();
                    for (ShopSection section : this.plugin.getSections().values()) {
                        for (ShopItem shopItem : section.getShopItems()) {
                            if (shopItem.hasItemError() || shopItem.isDisplayItem() || !mats.containsKey((Object)XMaterial.matchXMaterial(shopItem.getItemToGive()))) continue;
                            ConfigManager.getShop(section.getSection()).set("pages." + shopItem.itemLoc + ".buy", -ConfigManager.getShop(section.getSection()).getDouble("pages." + shopItem.itemLoc + ".buy", 1.0));
                            ConfigManager.getShop(section.getSection()).set("pages." + shopItem.itemLoc + ".sell", mats.get((Object)XMaterial.matchXMaterial(shopItem.getItemToGive())));
                            SendMessage.infoMessage("Imported item '" + XMaterial.matchXMaterial(shopItem.getItemToGive()).name() + "' from worth.yml with a sell price of '" + mats.get((Object)XMaterial.matchXMaterial(shopItem.getItemToGive())) + "'");
                            updated.add(section.getSection());
                            ++count;
                        }
                    }
                    updated.forEach(ConfigManager::saveShop);
                    SendMessage.sendMessage(logger, ChatColor.GREEN + "Import success, " + count + " items where imported. See the console for more details.");
                    break;
                }
                default: {
                    SendMessage.sendMessage(logger, Lang.FILE_NOT_FOUND.get());
                }
            }
            SendMessage.sendMessage(logger, Lang.EDITSHOP_RELOAD_SHOP_TO_SEE_CHANGES.get());
        }
    }

    private Path createZipFile() throws IOException {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        LocalDateTime now = LocalDateTime.now();
        File backupFolder = new File(this.plugin.getDataFolder() + "/backups");
        if (!backupFolder.exists()) {
            backupFolder.mkdirs();
        }
        String path = this.plugin.getDataFolder() + "/backups/layout - " + dtf.format(now);
        int i = 0;
        while (new File(path + (i > 0 ? "(" + i + ")" : "")).exists()) {
            ++i;
        }
        return Paths.get(path + (i > 0 ? "(" + i + ")" : ""), new String[0]);
    }

    private void zipLayout(ZipOutputStream zipOutputStream) throws IOException {
        for (String file : Arrays.asList("shops", "sections", "config.yml", "stands.json")) {
            String path = this.plugin.getDataFolder().getPath() + File.separator + file;
            if (path.contains("\\.")) {
                int length;
                FileInputStream fis = new FileInputStream(path);
                zipOutputStream.putNextEntry(new ZipEntry(file));
                byte[] bytes = new byte[1024];
                while ((length = fis.read(bytes)) >= 0) {
                    zipOutputStream.write(bytes, 0, length);
                }
                fis.close();
                continue;
            }
            this.zipFiles(zipOutputStream, file);
        }
    }

    private void zipFiles(ZipOutputStream zipOutputStream, String dir) throws IOException {
        File directory = new File(this.plugin.getDataFolder() + File.separator + dir);
        if (directory.exists() && directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                int length;
                if (file.isDirectory()) continue;
                String path = file.getPath();
                FileInputStream fis = new FileInputStream(path);
                zipOutputStream.putNextEntry(new ZipEntry(dir + "/" + file.toPath().getFileName().toString()));
                byte[] bytes = new byte[1024];
                while ((length = fis.read(bytes)) >= 0) {
                    zipOutputStream.write(bytes, 0, length);
                }
                fis.close();
            }
        }
    }

    private static  void lambda$importChanges$0(Map mats, FileConfiguration config1, String item, XMaterial mat) {
        mats.put(mat, config1.getDouble("worth." + item + ".*"));
    }
}

