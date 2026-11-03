

package com.siliconstudios.dynamicshopguiplus.commands.editshop.subcommands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.Methods;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.SubCommad;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.subcommands.transactionLogs.Export;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class TransactionLogs
extends SubCommad {
    private final ArrayList<SubCommad> subCommads = new ArrayList();

    public TransactionLogs(DynamicShopGUIPlus plugin, Methods methods) {
        this.subCommads.add(new Export(plugin));
    }

    @Override
    public void perform(Object logger, String[] args) {
        if (logger instanceof Player) {
            Player player = (Player)logger;
            if (args.length > 1) {
                for (SubCommad subCommad : this.subCommads) {
                    if (!args[1].equalsIgnoreCase(subCommad.getName())) continue;
                    if (subCommad.hasPermission((CommandSender)player)) {
                        subCommad.perform(logger, args);
                    } else {
                        SendMessage.chatToPlayer(player, Lang.NO_PERMISSIONS.get());
                    }
                    return;
                }
            }
            if (this.hasPermission((CommandSender)player)) {
                this.sendAllSyntaxes(logger);
            } else {
                SendMessage.chatToPlayer(player, Lang.NO_PERMISSIONS.get());
            }
        } else {
            if (args.length > 1) {
                for (SubCommad subCommad : this.subCommads) {
                    if (!args[1].equalsIgnoreCase(subCommad.getName())) continue;
                    subCommad.perform(logger, args);
                    return;
                }
            }
            this.sendAllSyntaxes(logger);
        }
    }

    @Override
    public String getName() {
        return "logs";
    }

    @Override
    public String getDescription() {
        return ChatColor.GREEN + "View/delete logged transactions";
    }

    @Override
    public String getSyntax() {
        return ChatColor.GREEN + "/editshop logs <export> ...";
    }

    @Override
    public boolean hasPermission(CommandSender source) {
        return this.subCommads.stream().anyMatch(c -> c.hasPermission(source));
    }

    private void sendAllSyntaxes(Object logger) {
        SendMessage.sendMessage(logger, Lang.SELLALL_COMMAND_USAGES.get());
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.subCommads.size(); ++i) {
            SubCommad subCommad = this.subCommads.get(i);
            if (!subCommad.hasPermission((CommandSender)logger) || logger instanceof ConsoleCommandSender) continue;
            builder.append(ChatColor.WHITE + " - " + ChatColor.RESET);
            builder.append(subCommad.getSyntax()).append(ChatColor.DARK_GRAY).append(" - ").append(ChatColor.RESET).append(subCommad.getDescription());
            if (i == this.subCommads.size()) continue;
            builder.append("\n");
        }
        SendMessage.sendMessage(logger, builder.toString());
    }

    @Override
    public List<String> getTabCompletion(String[] args) {
        if (args.length == 2) {
            ArrayList<String> tabCompletions = new ArrayList<String>();
            for (SubCommad subCommad : this.subCommads) {
                tabCompletions.add(subCommad.getName());
            }
            if (!args[1].isEmpty()) {
                ArrayList<String> completions = new ArrayList<String>();
                StringUtil.copyPartialMatches((String)args[1], tabCompletions, completions);
                Collections.sort(completions);
                return completions;
            }
            return tabCompletions;
        }
        for (SubCommad subCommad : this.subCommads) {
            if (!args[1].equalsIgnoreCase(subCommad.getName())) continue;
            return subCommad.getTabCompletion(args);
        }
        return null;
    }
}

