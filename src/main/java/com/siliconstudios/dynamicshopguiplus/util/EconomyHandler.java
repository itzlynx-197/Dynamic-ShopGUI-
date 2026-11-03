

package com.siliconstudios.dynamicshopguiplus.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.methodes.UpdateChecker;
import com.siliconstudios.dynamicshopguiplus.providers.EconomyProvider;
import com.siliconstudios.dynamicshopguiplus.providers.economys.ExpEconomy;
import com.siliconstudios.dynamicshopguiplus.providers.economys.VaultEconomy;
import com.siliconstudios.dynamicshopguiplus.providers.economys.formatter.PriceFormatter;
import com.siliconstudios.dynamicshopguiplus.util.EcoType;
import com.siliconstudios.dynamicshopguiplus.util.EconomyType;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.EconomyLoadException;

public class EconomyHandler {
    private final DynamicShopGUIPlus plugin;
    private EconomyProvider defaultProvider;
    private Map<String, EconomyProvider> shopPaymentTypes = new HashMap<String, EconomyProvider>();
    private Map<EcoType, EconomyProvider> registeredPaymentOptions = new HashMap<EcoType, EconomyProvider>();
    private static PriceFormatter.Format defaultFormatter;

    public EconomyHandler(DynamicShopGUIPlus plugin) {
        this.plugin = plugin;
    }

    public static PriceFormatter.Format getDefaultFormatter() {
        return defaultFormatter;
    }

    public boolean registerPaymentSolutions() {
        defaultFormatter = PriceFormatter.getFormatter();
        if (this.loadDefaultEconomy() == null) {
            return false;
        }
        int i = 0;
        for (String section : this.plugin.getShopSections()) {
            EcoType economyType = this.getEconomyType(section);
            if (economyType != null) {
                EconomyProvider provider = this.registeredPaymentOptions.get(economyType);
                if (provider == null) {
                    try {
                        this.shopPaymentTypes.put(section, this.initProvider(economyType));
                        SendMessage.logDebugMessage(Lang.SECTION_ECONOMY_PROVIDER_LOADED.get().replace("%economy%", economyType.toString()).replace("%shop%", section));
                    }
                    catch (EconomyLoadException e) {
                        SendMessage.errorMessage(Lang.CANNOT_LOAD_ECONOMY_PROVIDER.get().replace("%shop%", section).replace("%reason%", e.getMessage()));
                    }
                } else {
                    this.shopPaymentTypes.put(section, provider);
                    SendMessage.logDebugMessage(Lang.SECTION_ECONOMY_PROVIDER_LOADED.get().replace("%economy%", economyType.toString()).replace("%shop%", section));
                }
            }
            ++i;
        }
        SendMessage.infoMessage(Lang.ECONOMY_PROVIDERS_LOADED.get().replace("%total%", String.valueOf(this.registeredPaymentOptions.size())).replace("%shops%", String.valueOf(i)));
        return true;
    }

    private EcoType getEconomyType(String section) {
        String type = ConfigManager.getSection(section).getString("economy");
        if (type == null) {
            return null;
        }
        EcoType economyType = EconomyType.getFromString(type);
        if (economyType == null) {
            SendMessage.warnMessage(Lang.CANNOT_FIND_ECONOMY_PROVIDER.get().replace("%shop%", section).replace("%type%", type));
            return null;
        }
        return economyType;
    }

    private EconomyProvider loadDefaultEconomy() {
        EcoType economyType = EconomyType.getFromString(ConfigManager.getConfig().getString("economy-provider"));
        if (economyType == null) {
            SendMessage.warnMessage(Lang.CANNOT_FIND_DEFAULT_ECONOMY_PROVIDER.get().replace("%economy%", ConfigManager.getConfig().getString("economy-provider")));
            this.defaultProvider = this.getAvailableEcon();
            return this.defaultProvider != null ? this.defaultProvider : null;
        }
        try {
            this.defaultProvider = this.initProvider(economyType);
        }
        catch (EconomyLoadException e) {
            SendMessage.warnMessage(Lang.CANNOT_LOAD_DEFAULT_ECONOMY_PROVIDER.get().replace("%reason%", e.getMessage()));
            this.defaultProvider = this.getAvailableEcon();
            return this.defaultProvider != null ? this.defaultProvider : null;
        }
        return this.defaultProvider;
    }

    private EconomyProvider initProvider(EcoType type) throws EconomyLoadException {
        EconomyProvider provider = this.getProvider(type);
        provider.setup(this.plugin);
        this.registeredPaymentOptions.put(type, provider);
        return provider;
    }

    private EconomyProvider getProvider(EcoType type) {
        if (type == null) {
            return null;
        }
        switch (type.getType()) {
            case PLAYER_POINTS: {
                return null;
            }
            case VAULT: {
                return new VaultEconomy();
            }
            case VAULTUNLOCKED: {
                return null;
            }
            case EXCELLENT_ECONOMY: {
                return null;
            }
            case GEMS_ECONOMY: {
                return null;
            }
            case ULTRA_ECONOMY: {
                return null;
            }
            case VOTING_PLUGIN: {
                return null;
            }
            case EXP: {
                return new ExpEconomy();
            }
            case ZESSENTIALS: {
                return null;
            }
        }
        return null;
    }

    private EconomyProvider getAvailableEcon() {
        EconomyProvider provider = null;
        if (this.plugin.getServer().getPluginManager().isPluginEnabled("Vault")) {
            SendMessage.infoMessage("Detected Vault, initializing...");
            provider = new VaultEconomy();
        }
        if (provider != null) {
            try {
                provider.setup(this.plugin);
                this.registeredPaymentOptions.put(provider.getType(), provider);
                return provider;
            }
            catch (EconomyLoadException economyLoadException) {
                
            }
        }
        SendMessage.warnMessage("===================================================");
        SendMessage.warnMessage(" ");
        SendMessage.warnMessage("Failed to automatically load an available economy provider, this plugin requires one, disabling...");
        SendMessage.warnMessage(" ");
        SendMessage.warnMessage("===================================================");
        return null;
    }

    private EconomyProvider getCoinsEngineInstance(EcoType type) {
        return null;
    }

    public boolean relead() {
        if (this.shopPaymentTypes.size() > 0) {
            this.shopPaymentTypes.clear();
        }
        if (this.registeredPaymentOptions.size() > 0) {
            this.registeredPaymentOptions.clear();
        }
        this.defaultProvider = null;
        return this.registerPaymentSolutions();
    }

    public EconomyProvider getEcon(String section) {
        return this.shopPaymentTypes.containsKey(section) ? this.shopPaymentTypes.get(section) : this.defaultProvider;
    }

    public EconomyProvider getEcon(EcoType type) {
        if (type == null) {
            return this.defaultProvider;
        }
        return this.registeredPaymentOptions.get(type);
    }

    public EconomyProvider getDefaultProvider() {
        return this.defaultProvider;
    }

    public EconomyProvider getLoaded(String eco) {
        EcoType type = EconomyType.getFromString(eco);
        return this.registeredPaymentOptions.get(type);
    }
}

