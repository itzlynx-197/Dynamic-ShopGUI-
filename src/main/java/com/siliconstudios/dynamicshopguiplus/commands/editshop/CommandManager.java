

package com.siliconstudios.dynamicshopguiplus.commands.editshop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.commands.base.PluginCommands;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.Methods;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.SubCommad;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.subcommands.AddHandItem;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.subcommands.AddItem;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.subcommands.AddSection;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.subcommands.DeleteItem;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.subcommands.DeleteSection;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.subcommands.EditItem;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.subcommands.EditSection;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.subcommands.Import;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.subcommands.Migrate;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.subcommands.ShopStands;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.subcommands.TransactionLogs;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.util.PermissionsCache;
import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class CommandManager
extends PluginCommands {
    private final ArrayList<SubCommad> subCommands = new ArrayList();

    public CommandManager(DynamicShopGUIPlus plugin, List<String> disabledWorlds) {
        super(plugin, "editshop", "Allows to edit the shop with ingame commands", "/editshop <subCommand> ...", "DynamicShopGUIPlus.eshop", Collections.singletonList("eshop"), disabledWorlds);
        Methods methods = new Methods(plugin);
        this.subCommands.add(new AddItem(plugin, methods));
        this.subCommands.add(new EditItem(plugin, methods));
        this.subCommands.add(new DeleteItem(plugin, methods));
        this.subCommands.add(new AddHandItem(plugin, methods));
        this.subCommands.add(new Import(plugin, methods));
        this.subCommands.add(new AddSection(plugin, methods));
        this.subCommands.add(new EditSection(plugin, methods));
        this.subCommands.add(new DeleteSection(plugin, methods));
        this.subCommands.add(new Migrate(plugin, methods));
        this.subCommands.add(new ShopStands(plugin, methods));
        this.subCommands.add(new TransactionLogs(plugin, methods));
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (this.plugin.badYMLParse != null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&4This command cannot be executed now, please fix the configuration formatting first!"));
            return true;
        }
        if (sender instanceof Player) {
            Player player = (Player)sender;
            if (this.hasAccessInWorld(player)) {
                if (args.length > 0) {
                    for (SubCommad subCommad : this.subCommands) {
                        if (!args[0].toLowerCase().equalsIgnoreCase(subCommad.getName())) continue;
                        if (subCommad.hasPermission(sender)) {
                            subCommad.perform(sender, args);
                        } else {
                            SendMessage.chatToPlayer(player, Lang.NO_PERMISSIONS.get());
                        }
                        return true;
                    }
                }
                if (this.hasPermission((CommandSender)player)) {
                    this.sendAllSyntaxes(sender);
                } else {
                    SendMessage.chatToPlayer(player, Lang.NO_PERMISSIONS.get());
                }
            }
        } else if (sender instanceof ConsoleCommandSender || sender instanceof BlockCommandSender || sender instanceof RemoteConsoleCommandSender) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("addhanditem") || args[0].equalsIgnoreCase("uploadLayout") || args[0].equalsIgnoreCase("installLayout")) {
                    SendMessage.warnMessage((Object)sender, Lang.REAL_PLAYER.get());
                    return true;
                }
                for (SubCommad subCommad : this.subCommands) {
                    if (!args[0].toLowerCase().equalsIgnoreCase(subCommad.getName())) continue;
                    subCommad.perform(sender, args);
                    return true;
                }
            }
            this.sendAllSyntaxes(sender);
        }
        return true;
    }

    private boolean hasPermission(CommandSender source) {
        return this.subCommands.stream().anyMatch(c -> c.hasPermission(source));
    }

    @Override
    public List<String> tabComplete(CommandSender commandSender, String s, String[] args) {
        if (!this.hasPermission(commandSender)) {
            return Collections.emptyList();
        }
        ArrayList<String> tabCompletions = new ArrayList<String>();
        if (args.length == 1) {
            for (SubCommad subCommad : this.subCommands) {
                if (!PermissionsCache.hasPermission(commandSender, "DynamicShopGUIPlus.eshop." + subCommad.getName())) continue;
                tabCompletions.add(subCommad.getName());
            }
            if (!args[0].isEmpty()) {
                ArrayList<String> completions = new ArrayList<String>();
                StringUtil.copyPartialMatches((String)args[0], tabCompletions, completions);
                Collections.sort(completions);
                return completions;
            }
            return tabCompletions;
        }
        if (args.length >= 2) {
            for (SubCommad subCommad : this.subCommands) {
                if (!args[0].equalsIgnoreCase(subCommad.getName())) continue;
                return subCommad.getTabCompletion(args);
            }
        }
        return Collections.emptyList();
    }

    private void sendAllSyntaxes(Object logger) {
        SendMessage.sendMessage(logger, ChatColor.DARK_GREEN + "----------------------------------------");
        for (SubCommad subCommad : this.subCommands) {
            if (logger instanceof ConsoleCommandSender && subCommad.getName().equals("addhanditem")) continue;
            SendMessage.sendMessage(logger, " ");
            SendMessage.sendMessage(logger, ChatColor.DARK_GREEN + "- " + ChatColor.GREEN + subCommad.getSyntax() + " - " + subCommad.getDescription());
            SendMessage.sendMessage(logger, " ");
        }
        SendMessage.sendMessage(logger, ChatColor.DARK_GREEN + "----------------------------------------");
    }
}

