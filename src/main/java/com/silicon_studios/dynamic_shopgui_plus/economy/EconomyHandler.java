package com.silicon_studios.dynamic_shopgui_plus.economy;

import com.silicon_studios.dynamic_shopgui_plus.DynamicShopGUIPlus;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyHandler {
    private Economy economy;

    public boolean setup() {
        if (DynamicShopGUIPlus.getInstance().getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = DynamicShopGUIPlus.getInstance().getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public Economy getEconomy() {
        return economy;
    }

    public boolean hasEconomy() {
        return economy != null;
    }

    public double getBalance(org.bukkit.entity.Player player) {
        return economy.getBalance(player);
    }

    public boolean withdraw(org.bukkit.entity.Player player, double amount) {
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public boolean deposit(org.bukkit.entity.Player player, double amount) {
        return economy.depositPlayer(player, amount).transactionSuccess();
    }
}