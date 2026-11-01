package com.silicon_studios.dynamic_shopgui_plus.commands;

import com.silicon_studios.dynamic_shopgui_plus.DynamicShopGUIPlus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SellGUICommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        org.bukkit.inventory.Inventory inv = org.bukkit.Bukkit.createInventory(null, 54, "Sell GUI");
        player.openInventory(inv);
        return true;
    }
}