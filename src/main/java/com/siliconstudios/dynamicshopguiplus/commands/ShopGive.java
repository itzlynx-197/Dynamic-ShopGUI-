

package com.siliconstudios.dynamicshopguiplus.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.commands.base.PluginCommand;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

public class ShopGive
extends PluginCommand {
    public ShopGive(DynamicShopGUIPlus plugin, List<String> disabledWorlds) {
        super(plugin, "shopgive", "Give a shop item to a player", "/shopgive <section> <itemIndex> [player] [amount]", "DynamicShopGUIPlus.shopgive", disabledWorlds);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (this.plugin.badYMLParse != null) {
            SendMessage.warnMessage((Object)sender, "This command cannot be executed now, please fix the configuration formatting first!");
            return true;
        }
        if (!this.hasAccessInWorld(sender)) {
            return true;
        }
        if (args.length > 0) {
            if (this.plugin.getShopSections().stream().noneMatch(s -> s.equals(args[0]))) {
                SendMessage.sendMessage((Object)sender, sender instanceof Player ? Lang.NO_SHOP_FOUND.get((Player)sender) : Lang.NO_SHOP_FOUND.get());
                return true;
            }
        } else {
            SendMessage.infoMessage((Object)sender, "/shopgive <section> <itemIndex> [player] [amount]");
            return true;
        }
        if (args.length > 1) {
            if (this.plugin.getSection(args[0]).getShopItemLocs().stream().noneMatch(s -> s.equals(args[1]))) {
                SendMessage.sendMessage((Object)sender, (sender instanceof Player ? Lang.SECTION_DOES_NOT_CONTAIN_ITEM.get((Player)sender) : Lang.SECTION_DOES_NOT_CONTAIN_ITEM.get()).replace("%shopsection%", args[0]).replace("%itemLoc%", args[1]));
                return true;
            }
        } else {
            SendMessage.sendMessage((Object)sender, ChatColor.GREEN + "/shopgive <section> <itemIndex> [player] [amount]");
            return true;
        }
        if (args.length > 2 && !this.isInt(args[2])) {
            Player p = this.plugin.getServer().getPlayer(args[2]);
            if (p != null) {
                ItemStack item = this.plugin.getSection(args[0]).getShopItem(args[0] + "." + args[1]).getItemToGive();
                if (args.length > 3) {
                    this.setAmount(item, args[3]);
                }
                SendMessage.sendMessage((Object)sender, (sender instanceof Player ? Lang.SHOP_ITEM_GIVEN.get((Player)sender) : Lang.SHOP_ITEM_GIVEN.get()).replace("%amount%", String.valueOf(item.getAmount())).replace("%section%", args[0]).replace("%itemIndex%", args[1]).replace("%material%", this.plugin.getMaterialName(item.getType().toString())));
                this.giveItem(p, item);
            } else {
                SendMessage.warnMessage((Object)sender, sender instanceof Player ? Lang.PLAYER_NOT_ONLINE.get((Player)sender) : Lang.PLAYER_NOT_ONLINE.get());
            }
        } else if (sender instanceof Player) {
            ItemStack item = this.plugin.getSection(args[0]).getShopItem(args[0] + "." + args[1]).getItemToGive();
            if (args.length > 2) {
                this.setAmount(item, args[2]);
            }
            SendMessage.sendMessage((Object)sender, Lang.SHOP_ITEM_RECEIVED.get((Player)sender).replace("%amount%", String.valueOf(item.getAmount())).replace("%section%", args[0]).replace("%itemIndex%", args[1]).replace("%material%", this.plugin.getMaterialName(item.getType().toString())));
            this.giveItem((Player)sender, item);
        } else {
            SendMessage.sendMessage((Object)sender, ChatColor.GREEN + "/shopgive <section> <itemIndex> [player] [amount]");
        }
        return true;
    }

    private void giveItem(Player p, ItemStack item) {
        ArrayList items = new ArrayList(p.getInventory().addItem(new ItemStack[]{item}).values());
        for (int i = 0; i < items.size() && i != 2; ++i) {
            p.getWorld().dropItem(p.getLocation(), (ItemStack)items.get(i));
        }
    }

    private ItemStack[] getStacks(ItemStack item) {
        int amount = item.getAmount();
        int maxStackSize = item.getMaxStackSize();
        double div = (double)amount / (double)maxStackSize;
        int fullStacks = (int)Math.floor(div);
        int stacks = div == Math.floor(div) ? fullStacks : (int)Math.ceil(div);
        int amountLeft = amount;
        ItemStack[] items = new ItemStack[stacks];
        if (fullStacks >= 1) {
            for (int i = 0; i < fullStacks; ++i) {
                ItemStack is = new ItemStack(item);
                is.setAmount(maxStackSize);
                items[i] = is;
                amountLeft -= maxStackSize;
            }
        }
        if (amountLeft >= 1) {
            ItemStack is = new ItemStack(item);
            is.setAmount(amountLeft);
            items[stacks - 1] = is;
        }
        return items;
    }

    private void setAmount(ItemStack item, String s) {
        try {
            item.setAmount(Integer.parseInt(s));
        }
        catch (NumberFormatException numberFormatException) {
            
        }
    }

    private boolean isLimit(String s, ItemStack item) {
        try {
            return Integer.parseInt(s) > 128;
        }
        catch (NumberFormatException numberFormatException) {
            return true;
        }
    }

    private boolean isInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean hasAccessInWorld(CommandSender sender) {
        if (!(sender instanceof Player)) {
            return true;
        }
        return this.hasAccessInWorld((Player)sender);
    }

    @Override
    public List<String> tabComplete(CommandSender commandSender, String s, String[] args) {
        switch (args.length) {
            case 1: {
                if (!args[0].isEmpty()) {
                    ArrayList<String> completions = new ArrayList<String>();
                    StringUtil.copyPartialMatches((String)args[0], this.plugin.getShopSections(), completions);
                    Collections.sort(completions);
                    return completions;
                }
                return this.plugin.getShopSections();
            }
            case 2: {
                if (!this.isSection(args[0])) {
                    return null;
                }
                if (!args[1].isEmpty()) {
                    ArrayList<String> completions = new ArrayList<String>();
                    StringUtil.copyPartialMatches((String)args[1], this.plugin.getSection(args[0]).getShopItemLocs(), completions);
                    Collections.sort(completions);
                    return completions;
                }
                return this.plugin.getSection(args[0]).getShopItemLocs();
            }
            case 3: {
                return this.plugin.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            }
            case 4: {
                return Arrays.asList("1", "16", "32", "64", "128");
            }
        }
        return null;
    }

    private boolean isSection(String section) {
        return this.plugin.getShopSections().contains(section);
    }
}

