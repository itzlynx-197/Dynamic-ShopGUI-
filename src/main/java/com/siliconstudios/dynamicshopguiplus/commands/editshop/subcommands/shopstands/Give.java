

package com.siliconstudios.dynamicshopguiplus.commands.editshop.subcommands.shopstands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.Methods;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.SubCommad;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.objects.stands.StandType;
import com.siliconstudios.dynamicshopguiplus.util.ItemBuilder;
import com.siliconstudios.dynamicshopguiplus.util.PermissionsCache;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.StandLoadException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

public class Give
extends SubCommad {
    private final Methods methods;
    private final DynamicShopGUIPlus plugin;
    StandType type;
    String section;
    String itemLoc;

    public Give(DynamicShopGUIPlus plugin, Methods methods) {
        this.plugin = plugin;
        this.methods = methods;
    }

    @Override
    public String getName() {
        return "give";
    }

    @Override
    public String getDescription() {
        return Lang.EDITSHOP_SHOPSTANDS_GIVE_SUBCOMMAND_DESC.get().getLegacy();
    }

    @Override
    public String getSyntax() {
        return Lang.EDITSHOP_SHOPSTANDS_GIVE_SUBCOMMAND_SYNTAX.get().getLegacy();
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
        if (args.length > 2) {
            try {
                this.type = StandType.fromName(args[2]);
            }
            catch (StandLoadException e) {
                SendMessage.warnMessage(logger, Lang.INVALID_SHOP_STAND_TYPE.get().replace("%values%", String.join((CharSequence)",", StandType.getSupported())));
                return;
            }
            if (!this.type.getType().isSupported()) {
                SendMessage.warnMessage(logger, Lang.STAND_TYPE_NOT_SUPPORTED.get().replace("%type%", this.type.name()));
                return;
            }
        } else {
            SendMessage.sendMessage(logger, this.getSyntax());
            return;
        }
        if (args.length > 3) {
            this.section = this.methods.getSection(logger, args[3]);
            if (this.section == null) {
                return;
            }
        } else {
            SendMessage.sendMessage(logger, this.getSyntax());
            return;
        }
        if (args.length > 4) {
            this.itemLoc = this.methods.getShopItemLoc(logger, this.section, args[4]);
            if (this.itemLoc == null) {
                return;
            }
        } else {
            SendMessage.sendMessage(logger, this.getSyntax());
            return;
        }
        String item = this.section + "." + this.itemLoc;
        ItemBuilder builder = new ItemBuilder(this.type.getType().parseItem());
        builder.setDisplayName(Lang.DEFAULT_SHOP_STAND_ITEM_NAME.get());
        builder.withLore(Lang.DEFAULT_SHOP_STAND_ITEM_LORE.get().replace("%item%", item));
        ItemStack stack = this.plugin.versionHandler.setNBTString(builder.build(), "shopstand_item", item);
        Player player = (Player)logger;
        if (player.getInventory().firstEmpty() == -1) {
            SendMessage.chatToPlayer(player, Lang.NOT_ENOUGH_SPACE_INSIDE_INVENTORY.get());
            return;
        }
        player.getInventory().addItem(new ItemStack[]{stack});
        SendMessage.chatToPlayer(player, Lang.SHOP_STAND_ITEM_RECEIVED.get().replace("%amount%", String.valueOf(1)).replace("%item%", item));
    }

    private boolean isSection(String section2) {
        if (this.plugin.getShopSections().contains(section2)) {
            this.section = section2;
            return true;
        }
        return false;
    }

    @Override
    public List<String> getTabCompletion(String[] args) {
        switch (args.length) {
            case 3: {
                if (!args[2].isEmpty()) {
                    ArrayList<String> completions = new ArrayList<String>();
                    StringUtil.copyPartialMatches((String)args[2], StandType.getSupported(), completions);
                    Collections.sort(completions);
                    return completions;
                }
                return StandType.getSupported();
            }
            case 4: {
                if (!args[3].isEmpty()) {
                    ArrayList<String> completions = new ArrayList<String>();
                    StringUtil.copyPartialMatches((String)args[3], this.plugin.getShopSections(), completions);
                    Collections.sort(completions);
                    return completions;
                }
                return this.plugin.getShopSections();
            }
            case 5: {
                if (!this.isSection(args[3])) {
                    return null;
                }
                if (!args[2].isEmpty()) {
                    ArrayList<String> completions = new ArrayList<String>();
                    StringUtil.copyPartialMatches((String)args[4], this.plugin.getSection(args[3]).getShopItemLocs(), completions);
                    Collections.sort(completions);
                    return completions;
                }
                return this.plugin.getSection(args[1]).getItemLocs();
            }
        }
        return null;
    }
}

