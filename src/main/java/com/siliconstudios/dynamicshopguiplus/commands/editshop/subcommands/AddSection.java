

package com.siliconstudios.dynamicshopguiplus.commands.editshop.subcommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.Methods;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.SubCommad;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.util.PermissionsCache;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.StringUtil;

public class AddSection
extends SubCommad {
    private final Methods methods;
    private final DynamicShopGUIPlus plugin;
    private final Pattern p = Pattern.compile("[^a-zA-Z0-9]");
    private String section;
    private String material;
    private String displayname;
    private Integer slot;

    public AddSection(DynamicShopGUIPlus plugin, Methods methods) {
        this.plugin = plugin;
        this.methods = methods;
    }

    @Override
    public String getName() {
        return "addsection";
    }

    @Override
    public String getDescription() {
        return Lang.EDITSHOP_ADD_SECTION_SUBCOMMAND_DESC.get().getLegacy();
    }

    @Override
    public String getSyntax() {
        return Lang.EDITSHOP_ADD_SECTION_SUBCOMMAND_SYNTAX.get().getLegacy();
    }

    @Override
    public boolean hasPermission(CommandSender source) {
        return PermissionsCache.hasPermission(source, "DynamicShopGUIPlus.eshop." + this.getName());
    }

    @Override
    public void perform(Object logger, String[] args) {
        if (args.length > 1) {
            if (this.p.matcher(args[1]).find()) {
                SendMessage.sendMessage(logger, ChatColor.RED + "The section name can only contain letters and numbers.");
                return;
            }
        } else {
            SendMessage.sendMessage(logger, this.getSyntax());
            return;
        }
        this.section = args[1];
        if (args.length > 2) {
            this.material = this.methods.getMaterial(logger, args[2]);
            if (this.material == null) {
                return;
            }
        } else {
            SendMessage.sendMessage(logger, this.getSyntax());
            return;
        }
        if (args.length <= 3) {
            SendMessage.sendMessage(logger, this.getSyntax());
            return;
        }
        this.displayname = args[3];
        if (args.length > 4) {
            this.slot = this.methods.getMainMenuSlot(logger, args[4]);
            if (this.slot == null) {
                return;
            }
        } else {
            SendMessage.sendMessage(logger, this.getSyntax());
            return;
        }
        this.addSectionToSectionsConfig(logger);
    }

    @Override
    public List<String> getTabCompletion(String[] args) {
        switch (args.length) {
            case 2: {
                return Arrays.asList("Wood", "Armor", "Fireworks", "Stones", "CustomItems");
            }
            case 3: {
                if (!args[2].isEmpty()) {
                    ArrayList<String> completions = new ArrayList<String>();
                    StringUtil.copyPartialMatches((String)args[2], this.plugin.getSupportedMatNames(), completions);
                    Collections.sort(completions);
                    return completions;
                }
                return this.plugin.getSupportedMatNames();
            }
            case 4: {
                return Arrays.asList("#A1672D&lWood", "&1&lArmor", "#1B22E8&lF#1BAAE8&li#1BE8E1&lr#1BE896&le#1BE829&lw#E8DA1B&lo#E88F1B&lr#EB0909&lk#EB09DC&ls", "&7&lStones", "#09EBDC&lCustom#89EB09&lItems");
            }
            case 5: {
                return this.methods.getAvailableMainMenuSlots();
            }
        }
        return null;
    }

    private void addSectionToSectionsConfig(Object logger) {
        SendMessage.sendMessage(logger, Lang.EDITSHOP_ADDING_SECTION.get().replace("%section%", this.section));
        this.plugin.getConfigManager().createSectionConfig(this.section);
        ConfigManager.getSection(this.section).set("enable", true);
        ConfigManager.getSection(this.section).set("item.material", this.material);
        ConfigManager.getSection(this.section).set("slot", this.slot);
        ConfigManager.getSection(this.section).set("item.displayname", this.displayname);
        ConfigManager.saveSection(this.section);
        this.reloadPerms();
        SendMessage.sendMessage(logger, Lang.EDITSHOP_ADD_SECTION_SUCCESSFUL.get().replace("%path%", "/sections/" + this.section + ".yml"));
        SendMessage.sendMessage(logger, Lang.EDITSHOP_RELOAD_SHOP_TO_SEE_CHANGES.get());
    }

    private void reloadPerms() {
        for (String root : new String[]{"shop", "sellall", "sellallitem", "sellallhand", "sellgui"}) {
            Permission perm = new Permission("DynamicShopGUIPlus." + root + "." + this.section, PermissionDefault.TRUE);
            if (this.plugin.getServer().getPluginManager().getPermissions().contains(perm)) continue;
            this.plugin.getServer().getPluginManager().addPermission(perm);
        }
    }
}

