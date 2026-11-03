

package com.siliconstudios.dynamicshopguiplus.providers.economys;

import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.providers.EconomyProvider;
import com.siliconstudios.dynamicshopguiplus.providers.economys.formatter.PriceFormatter;
import com.siliconstudios.dynamicshopguiplus.util.ChatUtil;
import com.siliconstudios.dynamicshopguiplus.util.EcoType;
import com.siliconstudios.dynamicshopguiplus.util.EconomyType;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.EconomyLoadException;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultEconomy
implements EconomyProvider {
    private Economy economy;
    private String friendly;
    private String singular;
    private String plural;
    private boolean decimal;
    private PriceFormatter.Format priceFormat;

    @Override
    public void setup(DynamicShopGUIPlus plugin) throws EconomyLoadException {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            throw new EconomyLoadException("Could not find Vault");
        }
        RegisteredServiceProvider rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            throw new EconomyLoadException("No supported economy provider for Vault detected, please install a plugin such as Essentials");
        }
        this.economy = (Economy)rsp.getProvider();
        String path = ConfigManager.getPathIgnoreCase("currency-formats.vault");
        this.formatSingular(path);
        this.formatPlural(path);
        this.decimal = this.economy.fractionalDigits() == -1 || this.economy.fractionalDigits() > 0;
        this.friendly = ChatUtil.formatColors(ConfigManager.getConfig().getString(path + ".friendly", "money"));
        this.priceFormat = PriceFormatter.getFormatter(path);
        SendMessage.infoMessage("Successfully hooked into Vault and using " + this.economy.getName() + " as economy provider");
    }

    @Override
    public double getBalance(OfflinePlayer p) {
        return this.economy.getBalance(p);
    }

    @Override
    public void depositBalance(OfflinePlayer p, double amount) {
        this.economy.depositPlayer(p, amount);
    }

    @Override
    public void withdrawBalance(OfflinePlayer p, double amount) {
        this.economy.withdrawPlayer(p, amount);
    }

    @Override
    public EcoType getType() {
        return new EcoType(EconomyType.VAULT);
    }

    @Override
    public String getPlural() {
        return this.plural;
    }

    @Override
    public String getSingular() {
        return this.singular;
    }

    @Override
    public String getFriendly() {
        return this.friendly;
    }

    @Override
    public boolean isDecimal() {
        return this.decimal;
    }

    @Override
    public double round(double value) {
        return this.priceFormat.round(value);
    }

    @Override
    public String formatPrice(double price) {
        return this.priceFormat.format(this, price);
    }

    private void formatSingular(String path) {
        String singular = ChatUtil.formatColors(ConfigManager.getConfig().getString(path + ".singular", this.economy.currencyNameSingular()));
        this.singular = !singular.isEmpty() ? (!singular.contains("%currency-symbol%") ? (this.isSymbol(singular) ? Lang.SYMBOL_PRICING_FORMAT.get().replace("%symbol%", singular).getLegacy() : Lang.NAMED_PRICING_FORMAT.get().replace("%currency-format%", singular).getLegacy()) : Lang.CURRENCYSYMBOL.get().getLegacy()) : Lang.CURRENCYSYMBOL.get().getLegacy();
    }

    private void formatPlural(String path) {
        String plural = ChatUtil.formatColors(ConfigManager.getConfig().getString(path + ".plural", this.economy.currencyNamePlural()));
        this.plural = !plural.isEmpty() ? (!plural.contains("%currency-symbol%") ? (this.isSymbol(plural) ? Lang.SYMBOL_PRICING_FORMAT.get().replace("%symbol%", plural).getLegacy() : Lang.NAMED_PRICING_FORMAT.get().replace("%currency-format%", plural).getLegacy()) : Lang.CURRENCYSYMBOL.get().getLegacy()) : Lang.CURRENCYSYMBOL.get().getLegacy();
    }

    private boolean isSymbol(String format) {
        return format.chars().count() == 1L;
    }
}

