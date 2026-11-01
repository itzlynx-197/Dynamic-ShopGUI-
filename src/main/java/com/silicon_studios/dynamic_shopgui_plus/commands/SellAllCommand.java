package com.silicon_studios.dynamic_shopgui_plus.commands;

import com.silicon_studios.dynamic_shopgui_plus.DynamicShopGUIPlus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SellAllCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        String mode = args.length > 0 ? args[0].toLowerCase() : "inventory";
        int sold = 0;
        double total = 0;
        if (mode.equals("hand")) {
            org.bukkit.inventory.ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand == null || hand.getType() == org.bukkit.Material.AIR) {
                player.sendMessage("Nothing in hand.");
                return true;
            }
            player.sendMessage("Sold hand item.");
            return true;
        }
        for (org.bukkit.inventory.ItemStack is : player.getInventory().getContents()) {
            if (is == null || is.getType() == org.bukkit.Material.AIR) continue;
            sold++;
        }
        player.sendMessage("Sold " + sold + " item types.");
        return true;
    }
}