package com.silicon_studios.dynamic_shopgui_plus.commands;

import com.silicon_studios.dynamic_shopgui_plus.DynamicShopGUIPlus;
import com.silicon_studios.dynamic_shopgui_plus.shop.ShopItem;
import com.silicon_studios.dynamic_shopgui_plus.shop.ShopManager;
import com.silicon_studios.dynamic_shopgui_plus.shop.ShopSection;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopGiveCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /shopgive <section> <item> [player] [amount]");
            return true;
        }
        ShopManager sm = DynamicShopGUIPlus.getInstance().getShopManager();
        ShopSection sec = sm.getSection(args[0]);
        if (sec == null) {
            sender.sendMessage("Section not found.");
            return true;
        }
        ShopItem target = null;
        for (ShopItem it : sec.getItems()) {
            if (it.getId().equalsIgnoreCase(args[1])) {
                target = it;
                break;
            }
        }
        if (target == null) {
            sender.sendMessage("Item not found.");
            return true;
        }
        Player targetPlayer = args.length > 2 ? org.bukkit.Bukkit.getPlayer(args[2]) : (sender instanceof Player ? (Player) sender : null);
        if (targetPlayer == null) {
            sender.sendMessage("Player not found.");
            return true;
        }
        int amount = args.length > 3 ? Integer.parseInt(args[3]) : 1;
        if (target.getItemStack() != null) {
            org.bukkit.inventory.ItemStack is = target.getItemStack().clone();
            is.setAmount(amount);
            targetPlayer.getInventory().addItem(is);
            sender.sendMessage("Given " + amount + " to " + targetPlayer.getName());
        }
        return true;
    }
}