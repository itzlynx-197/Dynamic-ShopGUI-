

package com.siliconstudios.dynamicshopguiplus.commands.editshop.subcommands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.Methods;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.SubCommad;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.util.PermissionsCache;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

public class DeleteSection
extends SubCommad {
    private final Methods methods;
    private final DynamicShopGUIPlus plugin;
    private String section;

    public DeleteSection(DynamicShopGUIPlus plugin, Methods methods) {
        this.plugin = plugin;
        this.methods = methods;
    }

    @Override
    public String getName() {
        return "deletesection";
    }

    @Override
    public String getDescription() {
        return Lang.EDITSHOP_REMOVE_SECTION_SUBCOMMAND_DESC.get().getLegacy();
    }

    @Override
    public String getSyntax() {
        return Lang.EDITSHOP_REMOVE_SECTION_SUBCOMMAND_SYNTAX.get().getLegacy();
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
            SendMessage.infoMessage(logger, this.getSyntax());
            return;
        }
        this.deleteSection(logger);
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
        }
        return null;
    }

    private void deleteSection(Object logger) {
        SendMessage.sendMessage(logger, Lang.EDITSHOP_REMOVING_SECTION.get());
        this.plugin.getConfigManager().deleteSection(this.section);
        this.plugin.getConfigManager().deleteShopsConfig(this.section);
        SendMessage.sendMessage(logger, Lang.EDITSHOP_REMOVED_SECTION.get().replace("%itemPath%", "ShopSections." + this.section));
        SendMessage.sendMessage(logger, Lang.EDITSHOP_RELOAD_SHOP_TO_SEE_CHANGES.get());
    }
}

