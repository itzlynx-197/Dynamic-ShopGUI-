

package com.siliconstudios.dynamicshopguiplus.providers;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.events.SpawnerBreakEvent;
import com.siliconstudios.dynamicshopguiplus.events.SpawnerInteractEvent;
import com.siliconstudios.dynamicshopguiplus.events.SpawnerPlaceEvent;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.files.Translatable;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.providers.SpawnerProvider;
import com.siliconstudios.dynamicshopguiplus.providers.spawners.DefaultSpawnerProvider;
import com.siliconstudios.dynamicshopguiplus.util.ServerInfo;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

public class SpawnerManager {
    private SpawnerInteractEvent interactEvent;
    private SpawnerBreakEvent breakEvent;
    private SpawnerPlaceEvent placeEvent;
    private final DynamicShopGUIPlus plugin;
    private SpawnerProvider provider;

    public SpawnerManager(DynamicShopGUIPlus plugin) {
        this.plugin = plugin;
    }

    public void init() {
        Provider providerName;
        SpawnerProvider provider;
        this.provider = null;
        if (this.breakEvent != null) {
            BlockBreakEvent.getHandlerList().unregister((Listener)this.breakEvent);
        }
        if (this.placeEvent != null) {
            BlockPlaceEvent.getHandlerList().unregister((Listener)this.placeEvent);
        }
        if (this.interactEvent != null) {
            PlayerInteractEvent.getHandlerList().unregister((Listener)this.interactEvent);
        }
        if ((provider = this.setup(providerName = Provider.getFromString(ConfigManager.getConfig().getString("spawner-provider", "DEFAULT")), false)) == null) {
            this.provider = this.initProvider(Provider.DEFAULT, null);
            SendMessage.infoMessage(Lang.USING_DEFAULT_SPAWNERS.get());
        }
    }

    private SpawnerProvider setup(Provider providerName, boolean silent) {
        switch (providerName.ordinal()) {
            case 0: 
            case 3: 
            case 4: 
            case 5: 
            case 6: 
            case 7: 
            case 8: {
                if (this.plugin.getServer().getPluginManager().getPlugin(providerName.getPluginName()) != null) {
                    SendMessage.infoMessage(SpawnerManager.getLog(Lang.SPAWNER_PROVIDER_FOUND.get(), providerName.getName()));
                    if (this.plugin.getServer().getPluginManager().getPlugin(providerName.getPluginName()).isEnabled()) {
                        this.provider = this.initProvider(providerName, null);
                        SendMessage.infoMessage(SpawnerManager.getLog(Lang.USING_EXTERNAL_SPAWNERS.get(), providerName.getName()));
                        break;
                    }
                    SendMessage.warnMessage(SpawnerManager.getLog(Lang.FAILED_SPAWNER_PROVIDER_INTEGRATION.get(), providerName.getName()));
                    break;
                }
                if (silent) break;
                SendMessage.warnMessage(SpawnerManager.getLog(Lang.SPAWNER_PROVIDER_NOT_FOUND.get(), providerName.getName()));
                break;
            }
            case 1: {
                if (this.plugin.getServer().getPluginManager().getPlugin(providerName.getPluginName()) != null) {
                    SendMessage.infoMessage(SpawnerManager.getLog(Lang.SPAWNER_PROVIDER_FOUND.get(), providerName.getName()));
                    SendMessage.warnMessage(SpawnerManager.getLog(Lang.FAILED_SPAWNER_PROVIDER_INTEGRATION.get(), providerName.getName()));
                    break;
                }
                if (silent) break;
                SendMessage.warnMessage(SpawnerManager.getLog(Lang.SPAWNER_PROVIDER_NOT_FOUND.get(), providerName.getName()));
                break;
            }
            case 2: {
                if (this.plugin.getServer().getPluginManager().getPlugin(providerName.getPluginName()) != null) {
                    SendMessage.infoMessage(SpawnerManager.getLog(Lang.SPAWNER_PROVIDER_FOUND.get(), providerName.getName()));
                    try {
                        SendMessage.warnMessage(SpawnerManager.getLog(Lang.FAILED_SPAWNER_PROVIDER_INTEGRATION.get(), providerName.getName()));
                    }
                    catch (NoClassDefFoundError e) {
                        try {
                            Class.forName("de.corneliusmay.silkspawners.plugin.SilkSpawners");
                            SendMessage.warnMessage("SilkSpawners_V1 not found but instead found SilkSpawners_V2, updating config setting to use SilkSpawnersV2...");
                            if (this.plugin.getServer().getPluginManager().getPlugin(Provider.SILKSPAWNERSV2.getPluginName()).isEnabled()) {
                                this.provider = this.initProvider(Provider.SILKSPAWNERSV2, null);
                            } else {
                                SendMessage.warnMessage(SpawnerManager.getLog(Lang.FAILED_SPAWNER_PROVIDER_INTEGRATION.get(), Provider.SILKSPAWNERSV2.getName()));
                            }
                            ConfigManager.getConfig().set("spawner-provider", Provider.SILKSPAWNERSV2.name());
                            ConfigManager.saveConfig();
                        }
                        catch (ClassNotFoundException exception) {
                            SendMessage.warnMessage(SpawnerManager.getLog(Lang.FAILED_SPAWNER_PROVIDER_INTEGRATION.get(), providerName.getName()));
                        }
                    }
                    break;
                }
                if (silent) break;
                SendMessage.warnMessage(SpawnerManager.getLog(Lang.SPAWNER_PROVIDER_NOT_FOUND.get(), providerName.getName()));
                break;
            }
            case 9: {
                File purpurFile = new File(this.plugin.getDataFolder().getParentFile().getParentFile(), "purpur.yml");
                if (purpurFile.exists() && ServerInfo.isClassPresent("org.purpurmc.purpur.PurpurConfig")) {
                    SendMessage.infoMessage(SpawnerManager.getLog(Lang.SPAWNER_PROVIDER_FOUND.get(), providerName.getName()));
                    YamlConfiguration purpurConf = DynamicShopGUIPlus.getInstance().loadConfiguration(purpurFile, "purpur.yml");
                    if (purpurConf != null && purpurConf.getBoolean("world-settings.default.gameplay-mechanics.silk-touch.enabled", false)) {
                        this.provider = this.initProvider(providerName, purpurConf);
                        SendMessage.infoMessage(SpawnerManager.getLog(Lang.USING_EXTERNAL_SPAWNERS.get(), providerName.getName()));
                        break;
                    }
                    SendMessage.warnMessage(SpawnerManager.getLog(Lang.FAILED_SPAWNER_PROVIDER_INTEGRATION.get(), providerName.getName()));
                    break;
                }
                if (silent) break;
                SendMessage.warnMessage(SpawnerManager.getLog(Lang.SPAWNER_PROVIDER_NOT_FOUND.get(), providerName.getName()));
                break;
            }
            case 10: {
                this.provider = this.initProvider(providerName, null);
                SendMessage.infoMessage(Lang.SPAWNER_PROVIDER_DISABLED.get());
                break;
            }
            case 11: {
                this.provider = this.initProvider(providerName, null);
                SendMessage.infoMessage(Lang.USING_DEFAULT_SPAWNERS.get());
                break;
            }
            case 12: {
                Provider provider;
                SpawnerProvider spawnerProvider;
                SendMessage.infoMessage("Automatically searching for compatible spawner provider....");
                Iterator<Provider> iterator = Arrays.asList(Provider.WILDSTACKER, Provider.ROSESTACKER, Provider.ULTIMATESTACKER, Provider.SILKSPAWNERSV2, Provider.SILKSPAWNERS, Provider.MINEABLESPAWNERS, Provider.SPAWNERMETA, Provider.SPAWNERLEGACY, Provider.SMARTSPAWNER, Provider.PURPUR).iterator();
                while (iterator.hasNext() && (spawnerProvider = this.setup(provider = iterator.next(), true)) == null) {
                }
                if (this.provider != null) break;
                SendMessage.infoMessage("Failed to automatically find compatible spawner provider, using default...");
                this.provider = this.initProvider(Provider.DEFAULT, null);
            }
        }
        return this.provider;
    }

    public SpawnerProvider getProvider() {
        return this.provider;
    }

    private static Translatable getLog(Translatable message, String provider) {
        return message.replace("%provider%", provider);
    }

    public static Translatable getDefaultName(EntityType type) {
        Translatable name = DynamicShopGUIPlus.getInstance().getSpawnerName(type);
        if (name != null) {
            return name;
        }
        String[] arr = type.name().toLowerCase().replace("_", " ").split(" ");
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < arr.length; ++i) {
            sb.append(Character.toUpperCase(arr[i].charAt(0))).append(arr[i].substring(1)).append(" ");
        }
        return Lang.DEFAULT_SPAWNER_NAME.get().replace("%spawner-type%", sb.toString().trim());
    }

    private SpawnerProvider initProvider(Provider provider, @Nullable Object api) {
        switch (provider.ordinal()) {
            case 0: {
                return null;
            }
            case 1: {
                return null;
            }
            case 2: {
                return null;
            }
            case 4: {
                return null;
            }
            case 5: {
                return null;
            }
            case 3: {
                return this.getSilkSpawnersV2Hook();
            }
            case 6: {
                return null;
            }
            case 7: {
                return this.getSpawnerLegacyHook();
            }
            case 8: {
                return null;
            }
            case 9: {
                return null;
            }
            case 10: {
                return new DefaultSpawnerProvider(this.plugin, null, null, null);
            }
            case 11: {
                return new DefaultSpawnerProvider(this.plugin, !ConfigManager.getConfig().getBoolean("allow-spawner-type-change") ? (this.interactEvent = new SpawnerInteractEvent()) : null, ConfigManager.getConfig().getBoolean("enable-spawnerplace") ? (this.placeEvent = new SpawnerPlaceEvent(this.plugin)) : null, ConfigManager.getConfig().getBoolean("enable-spawnerbreak") ? (this.breakEvent = new SpawnerBreakEvent(this.plugin)) : null);
            }
        }
        return new DefaultSpawnerProvider(this.plugin, !ConfigManager.getConfig().getBoolean("allow-spawner-type-change") ? (this.interactEvent = new SpawnerInteractEvent()) : null, ConfigManager.getConfig().getBoolean("enable-spawnerplace") ? (this.placeEvent = new SpawnerPlaceEvent(this.plugin)) : null, ConfigManager.getConfig().getBoolean("enable-spawnerbreak") ? (this.breakEvent = new SpawnerBreakEvent(this.plugin)) : null);
    }

    private SpawnerProvider getSpawnerLegacyHook() {
        try {
            Class.forName("mc.rellox.spawnerlegacyapi.price.IItemPrice");
            return null;
        }
        catch (Exception exception) {
            try {
                Class.forName("mc.rellox.spawnerlegacyapi.SLAPI");
                return (SpawnerProvider)Class.forName("com.siliconstudios.dynamicshopguiplus.providers.spawners.SpawnerLegacyHookV3").newInstance();
            }
            catch (Exception exception2) {
                try {
                    Class.forName("mc.rellox.spawnerlegacy.lib.spawnerlegacyapi.SLAPI");
                    return (SpawnerProvider)Class.forName("com.siliconstudios.dynamicshopguiplus.providers.spawners.SpawnerLegacyHookV2").newInstance();
                }
                catch (Exception ignored) {
                    try {
                        return (SpawnerProvider)Class.forName("com.siliconstudios.dynamicshopguiplus.providers.spawners.SpawnerLegacyHookV1").newInstance();
                    }
                    catch (Exception e) {
                        SendMessage.errorMessage(SpawnerManager.getLog(Lang.FAILED_SPAWNER_PROVIDER_INTEGRATION.get(), "SpawnerLegacy"));
                        e.printStackTrace();
                        return null;
                    }
                }
            }
        }
    }

    private SpawnerProvider getSilkSpawnersV2Hook() {
        try {
            Class.forName("de.corneliusmay.silkspawners.api.SilkSpawnersAPI");
            return null;
        }
        catch (Exception ignored) {
            try {
                return (SpawnerProvider)Class.forName("com.siliconstudios.dynamicshopguiplus.providers.spawners.SilkSpawnersV2HookV1").newInstance();
            }
            catch (Exception e) {
                SendMessage.errorMessage(SpawnerManager.getLog(Lang.FAILED_SPAWNER_PROVIDER_INTEGRATION.get(), "SilkSpawners_v2"));
                e.printStackTrace();
                return null;
            }
        }
    }

    private static enum Provider {
        ULTIMATESTACKER("UltimateStacker"),
        WILDSTACKER("WildStacker"),
        SILKSPAWNERS("SilkSpawners"),
        SILKSPAWNERSV2("SilkSpawners_v2"),
        ROSESTACKER("RoseStacker"),
        MINEABLESPAWNERS("MineableSpawners"),
        SPAWNERMETA("SpawnerMeta"),
        SPAWNERLEGACY("SpawnerLegacy"),
        SMARTSPAWNER("SmartSpawner"),
        PURPUR("PURPUR"),
        DISABLED("DISABLED"),
        DEFAULT("DEFAULT"),
        AUTO("AUTO");

        private final String name;

        private Provider(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public String getPluginName() {
            return this.name;
        }

        public static Provider getFromString(String rawName) {
            try {
                SendMessage.infoMessage(SpawnerManager.getLog(Lang.SPAWNER_COMPATIBILITY_ENABLED.get(), rawName));
                return Provider.valueOf(rawName.toUpperCase(Locale.ENGLISH));
            }
            catch (IllegalArgumentException e) {
                SendMessage.warnMessage("Failed to find a spawner provider using name '" + rawName + "'");
                return AUTO;
            }
        }
    }
}

