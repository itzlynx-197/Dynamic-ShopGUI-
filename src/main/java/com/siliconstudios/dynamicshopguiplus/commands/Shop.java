

package com.siliconstudios.dynamicshopguiplus.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.commands.base.PluginCommands;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.objects.MainMenu;
import com.siliconstudios.dynamicshopguiplus.objects.TransactionMenu;
import com.siliconstudios.dynamicshopguiplus.objects.shops.ShopSection;
import com.siliconstudios.dynamicshopguiplus.util.BackButton;
import com.siliconstudios.dynamicshopguiplus.util.PermissionsCache;
import com.siliconstudios.dynamicshopguiplus.util.Transaction;
import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class Shop
extends PluginCommands {
    public Shop(DynamicShopGUIPlus plugin, List<String> aliases, List<String> disabledWorlds) {
        super(plugin, aliases.get(0), "Opens the server shop", "/" + aliases.remove(0) + " <section/search>", "DynamicShopGUIPlus.shop", aliases, disabledWorlds);
    }

    
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (this.plugin.badYMLParse != null) {
            SendMessage.warnMessage((Object)sender, "This command cannot be executed now, please fix the configuration formatting first!");
            return true;
        }
        if (sender instanceof Player) {
            Player player = (Player)sender;
            if (!this.canUse(player)) return true;
            if (args.length == 0) {
                MainMenu.open(player);
                return true;
            } else {
                if (args.length > 2) return false;
                if (this.plugin.getShopSections().contains(args[0]) && !this.plugin.getSection(args[0]).isSubSection()) {
                    if (PermissionsCache.hasPermission(player, "DynamicShopGUIPlus.shop." + args[0])) {
                        if (args.length == 2) {
                            try {
                                int page = Integer.parseInt(args[1]);
                                ShopSection section = this.plugin.getSection(args[0]);
                                if (section.getShopPageItems(page) == null) {
                                    throw new NumberFormatException();
                                }
                                section.openShopSection(player, page, BackButton.DIRECT);
                                return true;
                            }
                            catch (NumberFormatException e) {
                                SendMessage.warnMessage((Object)player, Lang.NO_SHOP_PAGE_FOUND.get(player).replace("%page%", args[1]));
                            }
                            return true;
                        } else {
                            this.plugin.getSection(args[0]).openShopSection(player, BackButton.DIRECT);
                        }
                        return true;
                    } else {
                        SendMessage.chatToPlayer(player, Lang.NO_PERMISSIONS_TO_OPEN_SHOP.get(player));
                    }
                    return true;
                } else {
                    SendMessage.chatToPlayer(player, Lang.NO_SHOP_FOUND.get(player));
                }
            }
            return true;
        } else {
            if (!(sender instanceof ConsoleCommandSender) && !(sender instanceof BlockCommandSender) && !(sender instanceof RemoteConsoleCommandSender)) return true;
            if (args.length == 0) {
                SendMessage.warnMessage((Object)sender, "Not enough params: /shop <section> <player> [page]");
                return true;
            } else if (args.length == 1) {
                Player p = this.plugin.getServer().getPlayer(args[0]);
                if (p != null) {
                    MainMenu.open(p);
                    return true;
                } else {
                    SendMessage.warnMessage((Object)sender, Lang.PLAYER_NOT_ONLINE.get());
                }
                return true;
            } else {
                if (args.length < 2) return false;
                Player p = this.plugin.getServer().getPlayer(args[1]);
                if (p != null) {
                    if (this.plugin.getShopSections().contains(args[0])) {
                        if (args.length >= 3) {
                            if (args[2].matches("-?\\d+")) {
                                try {
                                    int page = Integer.parseInt(args[2]);
                                    ShopSection section = this.plugin.getSection(args[0]);
                                    if (section.getShopPageItems(page) == null) {
                                        throw new NumberFormatException();
                                    }
                                    section.openShopSection(p, page, BackButton.DIRECT_CONSOLE);
                                    return true;
                                }
                                catch (NumberFormatException e) {
                                    SendMessage.warnMessage((Object)sender, Lang.NO_SHOP_PAGE_FOUND.get().replace("%page%", args[2]));
                                }
                                return true;
                            } else if (this.plugin.getSection(args[0]).getItemLocs().contains(args[2])) {
                                if (args.length >= 4) {
                                    try {
                                        Transaction.Mode mode = Transaction.Mode.valueOf(args[3].toUpperCase());
                                        new TransactionMenu(p, this.plugin.getShopItem(args[0] + "." + args[2]), null, BackButton.DIRECT_CONSOLE, mode, mode == Transaction.Mode.BUY ? Transaction.Type.BUY_SCREEN : Transaction.Type.SELL_SCREEN, 1).open();
                                        return true;
                                    }
                                    catch (IllegalArgumentException e) {
                                        SendMessage.warnMessage((Object)sender, ChatColor.RED + "Invalid transaction mode, should be buy/sell");
                                    }
                                    return true;
                                } else {
                                    SendMessage.warnMessage((Object)sender, ChatColor.RED + "/shop <section> <player> <item> <buy/sell>");
                                }
                                return true;
                            } else {
                                SendMessage.warnMessage((Object)sender, Lang.SECTION_DOES_NOT_CONTAIN_ITEM.get().replace("%shopsection%", args[0]).replace("%itemLoc%", args[2]));
                            }
                            return true;
                        } else {
                            this.plugin.getSection(args[0]).openShopSection(p, BackButton.DIRECT_CONSOLE);
                        }
                        return true;
                    } else {
                        SendMessage.warnMessage((Object)sender, Lang.NO_SHOP_FOUND.get());
                    }
                    return true;
                } else {
                    SendMessage.warnMessage((Object)sender, Lang.PLAYER_NOT_ONLINE.get());
                }
            }
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender commandSender, String s, String[] args) {
        if (args.length == 1) {
            List<String> tabCompletions = this.plugin.getSections().values().stream().filter(section -> !section.isSubSection() && PermissionsCache.hasPermission(commandSender, "DynamicShopGUIPlus.shop." + section.getSection())).map(ShopSection::getSection).collect(Collectors.toList());
            if (!args[0].isEmpty()) {
                ArrayList<String> completions = new ArrayList<String>();
                StringUtil.copyPartialMatches((String)args[0], tabCompletions, completions);
                Collections.sort(completions);
                return completions;
            }
            return tabCompletions;
        }
        return Collections.emptyList();
    }
}

