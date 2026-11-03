

package com.siliconstudios.dynamicshopguiplus.commands.editshop.subcommands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.Methods;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.SubCommad;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.util.PermissionsCache;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

public class DeleteItem
extends SubCommad {
    private final Methods methods;
    private final DynamicShopGUIPlus plugin;
    String section;
    String itemLoc;

    public DeleteItem(DynamicShopGUIPlus plugin, Methods methods) {
        this.plugin = plugin;
        this.methods = methods;
    }

    @Override
    public String getName() {
        return "deleteitem";
    }

    @Override
    public String getDescription() {
        return Lang.EDITSHOP_REMOVE_ITEM_SUBCOMMAND_DESC.get().getLegacy();
    }

    @Override
    public String getSyntax() {
        return Lang.EDITSHOP_REMOVE_ITEM_SUBCOMMAND_SYNTAX.get().getLegacy();
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
        this.removeItem(logger);
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
                if (this.isSection(args[1])) {
                    if (!args[2].isEmpty()) {
                        ArrayList<String> completions = new ArrayList<String>();
                        StringUtil.copyPartialMatches((String)args[2], this.plugin.getSection(args[1]).getItemLocs(), completions);
                        Collections.sort(completions);
                        return completions;
                    }
                    return this.plugin.getSection(args[1]).getItemLocs();
                }
                return null;
            }
        }
        return null;
    }

    private boolean isSection(String section) {
        return this.plugin.getShopSections().contains(section);
    }

    private List<String> getClosest(String current, Set<String> collection) {
        ArrayList<String> completions = new ArrayList<String>();
        StringUtil.copyPartialMatches((String)current, collection, completions);
        Collections.sort(completions);
        return completions;
    }

    private void removeItem(Object logger) {
        SendMessage.sendMessage(logger, Lang.EDITSHOP_REMOVING_ITEM.get());
        ConfigManager.getShop(this.section).set("pages." + this.itemLoc, null);
        ConfigManager.saveShop(this.section);
        SendMessage.sendMessage(logger, Lang.EDITSHOP_REMOVE_ITEM_SUCCESSFUL.get().replace("%itemPath%", this.section + "." + this.itemLoc));
        SendMessage.sendMessage(logger, Lang.EDITSHOP_RELOAD_SHOP_TO_SEE_CHANGES.get());
    }
}

