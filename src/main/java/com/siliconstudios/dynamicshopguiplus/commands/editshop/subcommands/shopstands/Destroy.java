

package com.siliconstudios.dynamicshopguiplus.commands.editshop.subcommands.shopstands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.Methods;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.SubCommad;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.objects.stands.Stand;
import com.siliconstudios.dynamicshopguiplus.util.PermissionsCache;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

public class Destroy
extends SubCommad {
    private final Methods methods;
    private final DynamicShopGUIPlus plugin;

    public Destroy(DynamicShopGUIPlus plugin, Methods methods) {
        this.plugin = plugin;
        this.methods = methods;
    }

    @Override
    public String getName() {
        return "destroy";
    }

    @Override
    public String getDescription() {
        return Lang.EDITSHOP_SHOPSTANDS_DESTROY_SUBCOMMAND_DESC.get().getLegacy();
    }

    @Override
    public String getSyntax() {
        return Lang.EDITSHOP_SHOPSTANDS_DESTROY_SUBCOMMAND_SYNTAX.get().getLegacy();
    }

    @Override
    public boolean hasPermission(CommandSender source) {
        return PermissionsCache.hasPermission(source, "DynamicShopGUIPlus.eshop.shopstands." + this.getName());
    }

    @Override
    public void perform(Object logger, String[] args) {
        int id;
        if (args.length > 2) {
            try {
                id = Integer.parseInt(args[2]);
            }
            catch (NumberFormatException e) {
                SendMessage.warnMessage(logger, Lang.NO_VALID_AMOUNT.get());
                return;
            }
        } else {
            SendMessage.sendMessage(logger, this.getSyntax());
            return;
        }
        Stand stand = this.plugin.getStandProvider().getStand(id);
        if (stand == null) {
            SendMessage.warnMessage(logger, Lang.UNKNOWN_SHOP_STAND_ID.get().replace("%id%", String.valueOf(id)));
            return;
        }
        this.plugin.getStandProvider().destroy(stand);
        SendMessage.infoMessage(logger, Lang.REMOVED_SHOP_STAND.get().replace("%location%", stand.getLoc().toString()));
    }

    @Override
    public List<String> getTabCompletion(String[] args) {
        switch (args.length) {
            case 3: {
                if (!args[2].isEmpty()) {
                    ArrayList<String> completions = new ArrayList<String>();
                    StringUtil.copyPartialMatches((String)args[2], (Iterable)this.plugin.getStandProvider().getStands().stream().map(s -> String.valueOf(s.getId())).collect(Collectors.toList()), completions);
                    Collections.sort(completions);
                    return completions;
                }
                return this.plugin.getStandProvider().getStands().stream().map(s -> String.valueOf(s.getId())).collect(Collectors.toList());
            }
        }
        return null;
    }
}

