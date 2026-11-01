package com.silicon_studios.dynamic_shopgui_plus.commands;

import com.silicon_studios.dynamic_shopgui_plus.DynamicShopGUIPlus;
import com.silicon_studios.dynamic_shopgui_plus.files.ConfigManager;
import com.silicon_studios.dynamic_shopgui_plus.shop.ShopManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ConfigManager.reloadConfig();
        DynamicShopGUIPlus.getInstance().getShopManager().load();
        sender.sendMessage("Reloaded.");
        return true;
    }
}