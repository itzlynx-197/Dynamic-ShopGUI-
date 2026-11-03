

package com.siliconstudios.dynamicshopguiplus.commands.editshop.subcommands.shopstands;

import java.util.List;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.Methods;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.SubCommad;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.objects.inventorys.StandBrowser;
import com.siliconstudios.dynamicshopguiplus.util.PermissionsCache;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Browse
extends SubCommad {
    private final Methods methods;
    private final DynamicShopGUIPlus plugin;

    public Browse(DynamicShopGUIPlus plugin, Methods methods) {
        this.plugin = plugin;
        this.methods = methods;
    }

    @Override
    public String getName() {
        return "browse";
    }

    @Override
    public String getDescription() {
        return Lang.EDITSHOP_SHOPSTANDS_BROWSE_SUBCOMMAND_DESC.get().getLegacy();
    }

    @Override
    public String getSyntax() {
        return Lang.EDITSHOP_SHOPSTANDS_BROWSE_SUBCOMMAND_SYNTAX.get().getLegacy();
    }

    @Override
    public boolean hasPermission(CommandSender source) {
        return PermissionsCache.hasPermission(source, "DynamicShopGUIPlus.eshop.shopstands." + this.getName());
    }

    @Override
    public void perform(Object logger, String[] args) {
        if (!(logger instanceof Player)) {
            return;
        }
        new StandBrowser(this.plugin, (Player)logger);
    }

    @Override
    public List<String> getTabCompletion(String[] args) {
        return null;
    }
}

