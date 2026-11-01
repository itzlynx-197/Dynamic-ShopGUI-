package com.silicon_studios.dynamic_shopgui_plus;

import com.silicon_studios.dynamic_shopgui_plus.commands.EditShopCommand;
import com.silicon_studios.dynamic_shopgui_plus.commands.ReloadCommand;
import com.silicon_studios.dynamic_shopgui_plus.commands.SellAllCommand;
import com.silicon_studios.dynamic_shopgui_plus.commands.SellGUICommand;
import com.silicon_studios.dynamic_shopgui_plus.commands.ShopCommand;
import com.silicon_studios.dynamic_shopgui_plus.commands.ShopGiveCommand;
import com.silicon_studios.dynamic_shopgui_plus.commands.ShopMigrateCommand;
import com.silicon_studios.dynamic_shopgui_plus.economy.EconomyHandler;
import com.silicon_studios.dynamic_shopgui_plus.features.commanditems.DelayedCommandScheduler;
import com.silicon_studios.dynamic_shopgui_plus.features.databasesync.DatabaseConfig;
import com.silicon_studios.dynamic_shopgui_plus.features.databasesync.DatabaseSyncManager;
import com.silicon_studios.dynamic_shopgui_plus.features.dynamicpricing.DynamicPricingManager;
import com.silicon_studios.dynamic_shopgui_plus.features.guieditor.EditorListener;
import com.silicon_studios.dynamic_shopgui_plus.features.multiprice.MultiPriceManager;
import com.silicon_studios.dynamic_shopgui_plus.features.nbothooks.NbtHookManager;
import com.silicon_studios.dynamic_shopgui_plus.features.stock.StockManager;
import com.silicon_studios.dynamic_shopgui_plus.files.ConfigManager;
import com.silicon_studios.dynamic_shopgui_plus.listeners.MenuHandler;
import com.silicon_studios.dynamic_shopgui_plus.listeners.ProxyMessageListener;
import com.silicon_studios.dynamic_shopgui_plus.shop.ShopManager;
import com.silicon_studios.dynamic_shopgui_plus.storage.SharedShopDataManager;
import com.silicon_studios.dynamic_shopgui_plus.storage.SharedStorageManager;
import com.silicon_studios.dynamic_shopgui_plus.util.Scheduler;
import org.bukkit.plugin.java.JavaPlugin;

public final class DynamicShopGUIPlus extends JavaPlugin {

    private static DynamicShopGUIPlus instance;
    private EconomyHandler economyHandler;
    private ShopManager shopManager;
    private Scheduler scheduler;
    private SharedStorageManager sharedStorageManager;
    private SharedShopDataManager sharedShopDataManager;
    private MultiPriceManager multiPriceManager;
    private StockManager stockManager;
    private DynamicPricingManager dynamicPricingManager;
    private NbtHookManager nbtHookManager;
    private DatabaseSyncManager databaseSyncManager;
    private DelayedCommandScheduler delayedCommandScheduler;

    public static DynamicShopGUIPlus getInstance() {
        return instance;
    }

    public EconomyHandler getEconomyHandler() {
        return economyHandler;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public SharedStorageManager getSharedStorageManager() {
        return sharedStorageManager;
    }

    public SharedShopDataManager getSharedShopDataManager() {
        return sharedShopDataManager;
    }

    public MultiPriceManager getMultiPriceManager() {
        return multiPriceManager;
    }

    public StockManager getStockManager() {
        return stockManager;
    }

    public DynamicPricingManager getDynamicPricingManager() {
        return dynamicPricingManager;
    }

    public NbtHookManager getNbtHookManager() {
        return nbtHookManager;
    }

    public DatabaseSyncManager getDatabaseSyncManager() {
        return databaseSyncManager;
    }

    public DelayedCommandScheduler getDelayedCommandScheduler() {
        return delayedCommandScheduler;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        ConfigManager.loadConfig();
        economyHandler = new EconomyHandler();
        economyHandler.setup();
        shopManager = new ShopManager();
        shopManager.load();
        sharedStorageManager = new SharedStorageManager(this);
        sharedStorageManager.init();
        sharedShopDataManager = new SharedShopDataManager(this, sharedStorageManager);
        if (sharedStorageManager.isProxyMode() && sharedStorageManager.isConnected()) {
            sharedShopDataManager.loadSharedShopData();
        }
        scheduler = new Scheduler(this);
        sharedStorageManager.startSync();
        if (org.bukkit.Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new com.silicon_studios.dynamic_shopgui_plus.providers.PlaceholderExpansion(this).register();
        }
        multiPriceManager = new MultiPriceManager();
        stockManager = new StockManager();
        dynamicPricingManager = new DynamicPricingManager();
        nbtHookManager = new NbtHookManager(this);
        delayedCommandScheduler = new DelayedCommandScheduler(this);
        DatabaseConfig dbCfg = new DatabaseConfig();
        org.bukkit.configuration.file.FileConfiguration c = ConfigManager.getConfig();
        try {
            dbCfg.type = DatabaseConfig.Type.valueOf(c.getString("database-sync.type", "SQLITE").toUpperCase());
            dbCfg.host = c.getString("database-sync.host", "localhost");
            dbCfg.port = c.getInt("database-sync.port", 3306);
            dbCfg.database = c.getString("database-sync.database", "dynamicshopgui");
            dbCfg.username = c.getString("database-sync.username", "root");
            dbCfg.password = c.getString("database-sync.password", "");
            dbCfg.tablePrefix = c.getString("database-sync.table-prefix", "dsg_");
            dbCfg.syncTransactions = c.getBoolean("database-sync.sync-transactions", true);
            dbCfg.syncStock = c.getBoolean("database-sync.sync-stock", true);
            dbCfg.syncPrices = c.getBoolean("database-sync.sync-prices", true);
        } catch (Exception ignored) {}
        try {
            dynamicPricingManager.getConfig().enabled = c.getBoolean("dynamic-pricing.enabled", false);
            dynamicPricingManager.getConfig().sensitivity = c.getDouble("dynamic-pricing.sensitivity", 0.001);
            dynamicPricingManager.getConfig().minMultiplier = c.getDouble("dynamic-pricing.min-multiplier", 0.5);
            dynamicPricingManager.getConfig().maxMultiplier = c.getDouble("dynamic-pricing.max-multiplier", 2.0);
            dynamicPricingManager.getConfig().decayAmount = c.getDouble("dynamic-pricing.decay-amount", 0.05);
        } catch (Exception ignored) {}
        databaseSyncManager = new DatabaseSyncManager(this, dbCfg);
        databaseSyncManager.connect();
        getCommand("shop").setExecutor(new ShopCommand());
        getCommand("sellall").setExecutor(new SellAllCommand());
        getCommand("sellgui").setExecutor(new SellGUICommand());
        getCommand("shopreload").setExecutor(new ReloadCommand());
        getCommand("shopgive").setExecutor(new ShopGiveCommand());
        getCommand("editshop").setExecutor(new EditShopCommand());
        getCommand("shopmigrate").setExecutor(new ShopMigrateCommand());
        getServer().getPluginManager().registerEvents(new MenuHandler(), this);
        getServer().getPluginManager().registerEvents(new EditorListener(this), this);
        getServer().getMessenger().registerIncomingPluginChannel(this, "dynamicshopgui:command", new ProxyMessageListener());
        scheduler.runTimer(() -> {
            if (dynamicPricingManager != null) dynamicPricingManager.decay();
        }, 6000L, 6000L);
        getLogger().info("DynamicShopGUI+ enabled");
    }

    @Override
    public void onDisable() {
        if (delayedCommandScheduler != null) delayedCommandScheduler.cancelAll();
        if (databaseSyncManager != null) databaseSyncManager.close();
        if (sharedStorageManager != null) {
            sharedStorageManager.stopSync();
            sharedStorageManager.close();
        }
        getLogger().info("DynamicShopGUI+ disabled");
        instance = null;
    }
}