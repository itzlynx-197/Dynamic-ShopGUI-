

package com.siliconstudios.dynamicshopguiplus.commands.editshop.subcommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.Methods;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.SubCommad;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.files.config.Config;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.util.PermissionsCache;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

public class AddHandItem
extends SubCommad {
    private final Methods methods;
    private final DynamicShopGUIPlus plugin;
    String section;
    Double buyPrice;
    Double sellPrice;

    public AddHandItem(DynamicShopGUIPlus plugin, Methods methods) {
        this.plugin = plugin;
        this.methods = methods;
    }

    @Override
    public String getName() {
        return "addhanditem";
    }

    @Override
    public String getDescription() {
        return Lang.EDITSHOP_ADD_HAND_ITEM_SUBCOMMAND_DESC.get().getLegacy();
    }

    @Override
    public String getSyntax() {
        return Lang.EDITSHOP_ADD_HAND_ITEM_SUBCOMMAND_SYNTAX.get().getLegacy();
    }

    @Override
    public boolean hasPermission(CommandSender source) {
        return PermissionsCache.hasPermission(source, "DynamicShopGUIPlus.eshop." + this.getName());
    }

    @Override
    public void perform(Object logger, String[] args) {
        if (!(logger instanceof Player)) {
            return;
        }
        Player player = (Player)logger;
        if (args.length > 1) {
            this.section = this.methods.getSection(player, args[1]);
            if (this.section == null) {
                return;
            }
        } else {
            SendMessage.chatToPlayer(player, this.getSyntax());
            return;
        }
        if (args.length > 2) {
            this.buyPrice = this.methods.getPrice(player, args[2]);
            if (this.buyPrice == null) {
                return;
            }
        } else {
            SendMessage.chatToPlayer(player, this.getSyntax());
            return;
        }
        if (args.length > 3) {
            this.sellPrice = this.methods.getPrice(player, args[3]);
            if (this.sellPrice == null) {
                return;
            }
        } else {
            SendMessage.chatToPlayer(player, this.getSyntax());
            return;
        }
        this.addItemToShopsConfig(player, this.plugin.versionHandler.getItemInHand(player));
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
            case 3: 
            case 4: {
                String currentArg = args[args.length - 1];
                if (!currentArg.isEmpty()) {
                    ArrayList<String> completions = new ArrayList<String>();
                    StringUtil.copyPartialMatches((String)currentArg, this.plugin.getExamplePrices(), completions);
                    Collections.sort(completions);
                    return completions;
                }
                return this.plugin.getExamplePrices();
            }
        }
        return null;
    }

    private void addItemToShopsConfig(Player player, ItemStack itemToAdd) {
        SendMessage.chatToPlayer(player, Lang.EDITSHOP_ADDING_ITEM.get().replace("%shop%", this.section));
        String locateItem = "pages." + this.getNextAvailablePosition();
        HashMap<String, Object> values = this.plugin.createItem.getShopItem(itemToAdd, this.buyPrice, this.sellPrice);
        for (String value : values.keySet()) {
            ConfigManager.getShop(this.section).set(locateItem + "." + value, values.get(value));
        }
        ConfigManager.saveShop(this.section);
        SendMessage.chatToPlayer(player, Lang.EDITSHOP_ADD_ITEM_SUCCESSFUL.get().replace("%shop%", this.section).replace("%itemPath%", locateItem));
        SendMessage.chatToPlayer(player, Lang.EDITSHOP_RELOAD_SHOP_TO_SEE_CHANGES.get());
    }

    private String getNextAvailablePosition() {
        Config config = ConfigManager.getShop(this.section);
        if (!config.contains("pages")) {
            return "page1.items.1";
        }
        int page = config.getConfigurationSection("pages").getKeys(false).size();
        if (page == 0) {
            return "page1.items.1";
        }
        LinkedList pages = new LinkedList(config.getConfigurationSection("pages").getKeys(false));
        String p = (String)pages.get(page - 1);
        int size = config.getInt("pages." + p + ".gui-rows", 6) * 9 - (this.plugin.navBar.isEnabled(this.section) ? 9 : 0);
        List layout = ConfigManager.getSection(this.section).getStringList("item-layout");
        if (!layout.isEmpty()) {
            ArrayList<Boolean> itemLayout = new ArrayList<>();
            for (int row = 0; row < size / 9; ++row) {
                String[] items = ((String)layout.get(row)).split(" ");
                if (items.length != 9) {
                    SendMessage.errorMessage("Invalid item layout for " + this.section + " page '" + page + "': Row " + (row + 1) + " contains less or more symbols, expected 9");
                    return this.getNextAvailablePositionFromShop(config, p, size, pages.size() + 1);
                }
                if (Arrays.stream(items).anyMatch(i -> !i.equals("0") && !i.equals("1"))) {
                    SendMessage.errorMessage("Invalid item layout for " + this.section + " page '" + page + "': Items from row " + (row + 1) + " does not match expected values, expected 0 or 1");
                    return this.getNextAvailablePositionFromShop(config, p, size, pages.size() + 1);
                }
                Arrays.stream(items).forEach(i -> itemLayout.add(i.equals("1")));
            }
            return this.getNextAvailablePositionFromShop(config, p, (int)itemLayout.stream().filter(Boolean::booleanValue).count(), pages.size() + 1);
        }
        return this.getNextAvailablePositionFromShop(config, p, size, pages.size() + 1);
    }

    private String getNextAvailablePositionFromShop(Config config, String p, int size, int newPage) {
        ConfigurationSection page = config.getConfigurationSection("pages." + p + ".items");
        if (page == null) {
            return p + ".items.1";
        }
        if (page.getKeys(false).size() >= size) {
            return "page" + newPage + ".items.1";
        }
        Set keys = page.getKeys(false);
        int pos = keys.size() + 1;
        while (keys.contains(String.valueOf(pos))) {
            ++pos;
        }
        return p + ".items." + pos;
    }
}

