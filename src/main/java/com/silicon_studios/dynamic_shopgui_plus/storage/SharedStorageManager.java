package com.silicon_studios.dynamic_shopgui_plus.storage;

import com.silicon_studios.dynamic_shopgui_plus.DynamicShopGUIPlus;
import com.silicon_studios.dynamic_shopgui_plus.shop.ShopItem;
import com.silicon_studios.dynamic_shopgui_plus.shop.ShopManager;
import com.silicon_studios.dynamic_shopgui_plus.shop.ShopSection;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

public class SharedStorageManager {

    private final DynamicShopGUIPlus plugin;
    private HikariDataSource dataSource;
    private boolean connected = false;
    private boolean proxyMode = false;

    private final AtomicLong lastProcessedChangeId = new AtomicLong(0);
    private final String backendId = java.util.UUID.randomUUID().toString().substring(0, 8);

    private volatile boolean dbAvailable = true;
    private long lastErrorLogTime = 0;
    private static final long ERROR_LOG_INTERVAL = 30000;

    private long syncInterval = 2000;
    private volatile boolean syncing = false;

    private final Map<String, Integer> itemVersions = new ConcurrentHashMap<>();
    private final Map<String, Integer> sectionVersions = new ConcurrentHashMap<>();

    public SharedStorageManager(DynamicShopGUIPlus plugin) {
        this.plugin = plugin;
    }

    public void init() {
        FileConfiguration config = plugin.getConfig();
        proxyMode = config.getBoolean("proxy", false);
        if (!proxyMode) {
            plugin.getLogger().info("Proxy mode disabled. Using standalone storage.");
            return;
        }

        if (!initConnection(config)) {
            plugin.getLogger().severe("Failed to connect to shared MySQL. Shared storage disabled.");
            return;
        }

        if (!initSchema()) {
            plugin.getLogger().severe("Failed to initialize shared database schema.");
            close();
            return;
        }

        migrateSchemaVersion();
        loadSharedData();
        plugin.getLogger().info("Shared MySQL storage initialized successfully.");
    }

    private boolean initConnection(FileConfiguration config) {
        try {
            HikariConfig hikariConfig = new HikariConfig();
            String host = config.getString("shared-storage.mysql.host", "localhost");
            int port = config.getInt("shared-storage.mysql.port", 3306);
            String database = config.getString("shared-storage.mysql.database", "dynamicshop");
            String username = config.getString("shared-storage.mysql.username", "dynamicshop");
            String password = config.getString("shared-storage.mysql.password", "change-me");
            boolean useSsl = config.getBoolean("shared-storage.mysql.use-ssl", false);

            hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database
                    + "?useSSL=" + useSsl + "&allowPublicKeyRetrieval=true&serverTimezone=UTC");
            hikariConfig.setUsername(username);
            hikariConfig.setPassword(password);
            hikariConfig.setMaximumPoolSize(config.getInt("shared-storage.mysql.connection-pool.maximum-size", 5));
            hikariConfig.setMinimumIdle(config.getInt("shared-storage.mysql.connection-pool.minimum-idle", 1));
            hikariConfig.setConnectionTimeout(config.getLong("shared-storage.mysql.connection-pool.connection-timeout", 5000));
            hikariConfig.setPoolName("DynamicShopGUI-SharedStorage");
            hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(hikariConfig);
            connected = true;
            dbAvailable = true;
            syncInterval = config.getLong("shared-storage.mysql.synchronization.interval", 2000);
            return true;
        } catch (Exception e) {
            logError("Failed to initialize MySQL connection pool", e);
            connected = false;
            dbAvailable = false;
            return false;
        }
    }

    private boolean initSchema() {
        try (Connection conn = getConnection()) {
            if (conn == null) return false;
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS dsg_schema_version (" +
                        "version INT PRIMARY KEY, " +
                        "applied_at BIGINT NOT NULL)");

                stmt.execute("CREATE TABLE IF NOT EXISTS dsg_shop_items (" +
                        "id VARCHAR(128) PRIMARY KEY, " +
                        "section VARCHAR(64) NOT NULL, " +
                        "material VARCHAR(64) NOT NULL, " +
                        "display_name VARCHAR(256), " +
                        "lore TEXT, " +
                        "buy_price DOUBLE NOT NULL DEFAULT -1, " +
                        "sell_price DOUBLE NOT NULL DEFAULT -1, " +
                        "stack_size INT NOT NULL DEFAULT 1, " +
                        "min_buy INT NOT NULL DEFAULT 0, " +
                        "max_buy INT NOT NULL DEFAULT 0, " +
                        "min_sell INT NOT NULL DEFAULT 0, " +
                        "max_sell INT NOT NULL DEFAULT 0, " +
                        "hidden BOOLEAN NOT NULL DEFAULT FALSE, " +
                        "close_menu BOOLEAN NOT NULL DEFAULT FALSE, " +
                        "sub_section VARCHAR(64), " +
                        "version INT NOT NULL DEFAULT 1, " +
                        "updated_at BIGINT NOT NULL, " +
                        "INDEX idx_section (section))");

                stmt.execute("CREATE TABLE IF NOT EXISTS dsg_shop_sections (" +
                        "name VARCHAR(64) PRIMARY KEY, " +
                        "slot INT NOT NULL DEFAULT 0, " +
                        "title VARCHAR(256), " +
                        "hidden BOOLEAN NOT NULL DEFAULT FALSE, " +
                        "sub_section BOOLEAN NOT NULL DEFAULT FALSE, " +
                        "display_item BOOLEAN NOT NULL DEFAULT FALSE, " +
                        "economy VARCHAR(64) NOT NULL DEFAULT 'VAULT', " +
                        "icon_material VARCHAR(64), " +
                        "version INT NOT NULL DEFAULT 1, " +
                        "updated_at BIGINT NOT NULL)");

                stmt.execute("CREATE TABLE IF NOT EXISTS dsg_changes (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                        "object_type VARCHAR(32) NOT NULL, " +
                        "object_id VARCHAR(128) NOT NULL, " +
                        "action VARCHAR(16) NOT NULL, " +
                        "origin_backend VARCHAR(16) NOT NULL, " +
                        "timestamp BIGINT NOT NULL, " +
                        "INDEX idx_timestamp (timestamp), " +
                        "INDEX idx_object (object_type, object_id))");

                stmt.execute("CREATE TABLE IF NOT EXISTS dsg_backend_progress (" +
                        "backend_id VARCHAR(16) PRIMARY KEY, " +
                        "last_change_id BIGINT NOT NULL DEFAULT 0, " +
                        "last_sync_at BIGINT NOT NULL)");
            }
            return true;
        } catch (SQLException e) {
            logError("Failed to initialize shared database schema", e);
            return false;
        }
    }

    private void migrateSchemaVersion() {
        try (Connection conn = getConnection()) {
            if (conn == null) return;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO dsg_schema_version (version, applied_at) VALUES (?, ?) " +
                            "ON DUPLICATE KEY UPDATE applied_at = ?")) {
                ps.setInt(1, 1);
                ps.setLong(2, System.currentTimeMillis());
                ps.setLong(3, System.currentTimeMillis());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            logError("Failed to migrate schema version", e);
        }
    }

    private void loadSharedData() {
        if (!isConnected()) return;
        try (Connection conn = getConnection()) {
            if (conn == null) return;
            ShopManager sm = plugin.getShopManager();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT name, slot, title, hidden, sub_section, display_item, economy, icon_material, version FROM dsg_shop_sections")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String name = rs.getString("name");
                        ShopSection sec = sm.getSection(name);
                        if (sec == null) {
                            sec = new ShopSection(name);
                            sm.getAllSections().put(name, sec);
                        }
                        sec.setSlot(rs.getInt("slot"));
                        sec.setTitle(rs.getString("title"));
                        sec.setHidden(rs.getBoolean("hidden"));
                        sec.setSubSection(rs.getBoolean("sub_section"));
                        sec.setDisplayItem(rs.getBoolean("display_item"));
                        sec.setEconomy(rs.getString("economy"));
                        sectionVersions.put(name, rs.getInt("version"));
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, section, material, display_name, lore, buy_price, sell_price, stack_size, " +
                            "min_buy, max_buy, min_sell, max_sell, hidden, close_menu, sub_section, version FROM dsg_shop_items")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String id = rs.getString("id");
                        String section = rs.getString("section");
                        ShopSection sec = sm.getSection(section);
                        if (sec == null) continue;
                        ShopItem item = new ShopItem(section, id);
                        try {
                            item.setMaterial(org.bukkit.Material.valueOf(rs.getString("material")));
                        } catch (Exception ignored) {}
                        item.setDisplayName(rs.getString("display_name"));
                        String loreStr = rs.getString("lore");
                        if (loreStr != null && !loreStr.isEmpty()) {
                            item.setLore(java.util.Arrays.asList(loreStr.split("\\|\\|")));
                        }
                        item.setBuy(rs.getDouble("buy_price"));
                        item.setSell(rs.getDouble("sell_price"));
                        item.setStackSize(rs.getInt("stack_size"));
                        item.setMinBuy(rs.getInt("min_buy"));
                        item.setMaxBuy(rs.getInt("max_buy"));
                        item.setMinSell(rs.getInt("min_sell"));
                        item.setMaxSell(rs.getInt("max_sell"));
                        item.setHidden(rs.getBoolean("hidden"));
                        item.setCloseMenu(rs.getBoolean("close_menu"));
                        item.setSubSection(rs.getString("sub_section"));
                        if (item.getMaterial() != null) {
                            item.setItemStack(new org.bukkit.inventory.ItemStack(item.getMaterial(), item.getStackSize()));
                        }
                        sec.getItems().add(item);
                        itemVersions.put(section + "." + id, rs.getInt("version"));
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT last_change_id FROM dsg_backend_progress WHERE backend_id = ?")) {
                ps.setString(1, backendId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        lastProcessedChangeId.set(rs.getLong("last_change_id"));
                    }
                }
            }
        } catch (SQLException e) {
            logError("Failed to load shared data from MySQL", e);
        }
    }

    public void saveItem(ShopItem item, int expectedVersion) {
        if (!isConnected()) return;
        plugin.getScheduler().runAsync(() -> {
            Connection conn = null;
            try {
                conn = getConnection();
                if (conn == null) return;
                conn.setAutoCommit(false);
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT version FROM dsg_shop_items WHERE id = ? AND section = ?")) {
                    ps.setString(1, item.getId());
                    ps.setString(2, item.getSection());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            int dbVersion = rs.getInt("version");
                            if (dbVersion != expectedVersion) {
                                plugin.getLogger().warning("Item " + item.getId() + " has been modified by another administrator. Please refresh before editing.");
                                conn.rollback();
                                return;
                            }
                        }
                    }
                }
                int newVersion = expectedVersion + 1;
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO dsg_shop_items (id, section, material, display_name, lore, buy_price, sell_price, " +
                                "stack_size, min_buy, max_buy, min_sell, max_sell, hidden, close_menu, sub_section, version, updated_at) " +
                                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) " +
                                "ON DUPLICATE KEY UPDATE material=?, display_name=?, lore=?, buy_price=?, sell_price=?, " +
                                "stack_size=?, min_buy=?, max_buy=?, min_sell=?, max_sell=?, hidden=?, close_menu=?, " +
                                "sub_section=?, version=?, updated_at=?")) {
                    ps.setString(1, item.getId());
                    ps.setString(2, item.getSection());
                    ps.setString(3, item.getMaterial() != null ? item.getMaterial().name() : "STONE");
                    ps.setString(4, item.getDisplayName());
                    ps.setString(5, item.getLore() != null ? String.join("||", item.getLore()) : "");
                    ps.setDouble(6, item.getBuy());
                    ps.setDouble(7, item.getSell());
                    ps.setInt(8, item.getStackSize());
                    ps.setInt(9, item.getMinBuy());
                    ps.setInt(10, item.getMaxBuy());
                    ps.setInt(11, item.getMinSell());
                    ps.setInt(12, item.getMaxSell());
                    ps.setBoolean(13, item.isHidden());
                    ps.setBoolean(14, item.isCloseMenu());
                    ps.setString(15, item.getSubSection());
                    ps.setInt(16, newVersion);
                    ps.setLong(17, System.currentTimeMillis());
                    ps.setString(18, item.getMaterial() != null ? item.getMaterial().name() : "STONE");
                    ps.setString(19, item.getDisplayName());
                    ps.setString(20, item.getLore() != null ? String.join("||", item.getLore()) : "");
                    ps.setDouble(21, item.getBuy());
                    ps.setDouble(22, item.getSell());
                    ps.setInt(23, item.getStackSize());
                    ps.setInt(24, item.getMinBuy());
                    ps.setInt(25, item.getMaxBuy());
                    ps.setInt(26, item.getMinSell());
                    ps.setInt(27, item.getMaxSell());
                    ps.setBoolean(28, item.isHidden());
                    ps.setBoolean(29, item.isCloseMenu());
                    ps.setString(30, item.getSubSection());
                    ps.setInt(31, newVersion);
                    ps.setLong(32, System.currentTimeMillis());
                    ps.executeUpdate();
                }
                recordChange("item", item.getSection() + "." + item.getId(), "UPDATE");
                conn.commit();
                itemVersions.put(item.getSection() + "." + item.getId(), newVersion);
            } catch (SQLException e) {
                try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
                logError("Failed to save item " + item.getId(), e);
            } finally {
                if (conn != null) {
                    try { conn.close(); } catch (SQLException ignored) {}
                }
            }
        });
    }

    public void deleteItem(String section, String itemId, int expectedVersion) {
        if (!isConnected()) return;
        plugin.getScheduler().runAsync(() -> {
            Connection conn = null;
            try {
                conn = getConnection();
                if (conn == null) return;
                conn.setAutoCommit(false);
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM dsg_shop_items WHERE id = ? AND section = ? AND version = ?")) {
                    ps.setString(1, itemId);
                    ps.setString(2, section);
                    ps.setInt(3, expectedVersion);
                    int affected = ps.executeUpdate();
                    if (affected == 0) {
                        plugin.getLogger().warning("Item " + itemId + " has been modified by another administrator. Please refresh before deleting.");
                        conn.rollback();
                        return;
                    }
                }
                recordChange("item", section + "." + itemId, "DELETE");
                conn.commit();
                itemVersions.remove(section + "." + itemId);
            } catch (SQLException e) {
                try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
                logError("Failed to delete item " + itemId, e);
            } finally {
                if (conn != null) {
                    try { conn.close(); } catch (SQLException ignored) {}
                }
            }
        });
    }

    public void saveSection(ShopSection section, int expectedVersion) {
        if (!isConnected()) return;
        plugin.getScheduler().runAsync(() -> {
            Connection conn = null;
            try {
                conn = getConnection();
                if (conn == null) return;
                conn.setAutoCommit(false);
                int newVersion = expectedVersion + 1;
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO dsg_shop_sections (name, slot, title, hidden, sub_section, display_item, economy, icon_material, version, updated_at) " +
                                "VALUES (?,?,?,?,?,?,?,?,?,?) " +
                                "ON DUPLICATE KEY UPDATE slot=?, title=?, hidden=?, sub_section=?, display_item=?, economy=?, icon_material=?, version=?, updated_at=?")) {
                    ps.setString(1, section.getName());
                    ps.setInt(2, section.getSlot());
                    ps.setString(3, section.getTitle());
                    ps.setBoolean(4, section.isHidden());
                    ps.setBoolean(5, section.isSubSection());
                    ps.setBoolean(6, section.isDisplayItem());
                    ps.setString(7, section.getEconomy());
                    ps.setString(8, section.getIconMaterial() != null ? section.getIconMaterial().name() : "CHEST");
                    ps.setInt(9, newVersion);
                    ps.setLong(10, System.currentTimeMillis());
                    ps.setInt(11, section.getSlot());
                    ps.setString(12, section.getTitle());
                    ps.setBoolean(13, section.isHidden());
                    ps.setBoolean(14, section.isSubSection());
                    ps.setBoolean(15, section.isDisplayItem());
                    ps.setString(16, section.getEconomy());
                    ps.setString(17, section.getIconMaterial() != null ? section.getIconMaterial().name() : "CHEST");
                    ps.setInt(18, newVersion);
                    ps.setLong(19, System.currentTimeMillis());
                    ps.executeUpdate();
                }
                recordChange("section", section.getName(), "UPDATE");
                conn.commit();
                sectionVersions.put(section.getName(), newVersion);
            } catch (SQLException e) {
                try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
                logError("Failed to save section " + section.getName(), e);
            } finally {
                if (conn != null) {
                    try { conn.close(); } catch (SQLException ignored) {}
                }
            }
        });
    }

    public int getItemVersion(String section, String itemId) {
        return itemVersions.getOrDefault(section + "." + itemId, 0);
    }

    public int getSectionVersion(String sectionName) {
        return sectionVersions.getOrDefault(sectionName, 0);
    }

    public boolean isProxyMode() { return proxyMode; }
    public boolean isConnected() { return connected && dbAvailable; }
    public boolean isDbAvailable() { return dbAvailable; }
    public String getBackendId() { return backendId; }
    public AtomicLong getLastProcessedChangeId() { return lastProcessedChangeId; }

    public void recordChange(String objectType, String objectId, String action) {
        if (!isConnected()) return;
        try (Connection conn = getConnection()) {
            if (conn == null) return;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO dsg_changes (object_type, object_id, action, origin_backend, timestamp) VALUES (?,?,?,?,?)")) {
                ps.setString(1, objectType);
                ps.setString(2, objectId);
                ps.setString(3, action);
                ps.setString(4, backendId);
                ps.setLong(5, System.currentTimeMillis());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            logError("Failed to record change", e);
        }
    }

    private void processChange(String objectType, String objectId, String action) {
        SharedShopDataManager dataManager = new SharedShopDataManager(plugin, this);
        if (objectType.equals("item")) {
            String[] parts = objectId.split("\\.", 2);
            if (parts.length == 2) {
                if (action.equals("DELETE")) {
                    dataManager.removeItemFromCache(parts[0], parts[1]);
                } else {
                    dataManager.updateItemInCache(parts[0], parts[1]);
                }
            }
        } else if (objectType.equals("section")) {
            if (action.equals("DELETE")) {
                plugin.getShopManager().getAllSections().remove(objectId);
            } else {
                dataManager.updateSectionInCache(objectId);
            }
        }
    }

    public void startSync() {
        if (!proxyMode || !isConnected()) return;
        syncing = true;
        plugin.getScheduler().runTimer(() -> {
            if (!syncing || !isConnected()) return;
            syncChanges();
        }, syncInterval, syncInterval);
    }

    public void stopSync() {
        syncing = false;
    }

    private void syncChanges() {
        try (Connection conn = getConnection()) {
            if (conn == null) return;
            long lastId = lastProcessedChangeId.get();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, object_type, object_id, action, origin_backend FROM dsg_changes WHERE id > ? ORDER BY id ASC LIMIT 100")) {
                ps.setLong(1, lastId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        long changeId = rs.getLong("id");
                        String originBackend = rs.getString("origin_backend");
                        if (originBackend.equals(backendId)) {
                            lastProcessedChangeId.set(changeId);
                            continue;
                        }
                        String objectType = rs.getString("object_type");
                        String objectId = rs.getString("object_id");
                        String action = rs.getString("action");
                        processChange(objectType, objectId, action);
                        lastProcessedChangeId.set(changeId);
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO dsg_backend_progress (backend_id, last_change_id, last_sync_at) VALUES (?,?,?) " +
                            "ON DUPLICATE KEY UPDATE last_change_id=?, last_sync_at=?")) {
                ps.setString(1, backendId);
                ps.setLong(2, lastProcessedChangeId.get());
                ps.setLong(3, System.currentTimeMillis());
                ps.setLong(4, lastProcessedChangeId.get());
                ps.setLong(5, System.currentTimeMillis());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            logError("Synchronization failed", e);
        }
    }

    public Connection getConnection() {
        if (dataSource == null || dataSource.isClosed()) return null;
        try {
            Connection conn = dataSource.getConnection();
            dbAvailable = true;
            return conn;
        } catch (SQLException e) {
            dbAvailable = false;
            logError("Failed to get database connection", e);
            return null;
        }
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
        connected = false;
        dbAvailable = false;
    }

    private void logError(String message, Exception e) {
        long now = System.currentTimeMillis();
        if (now - lastErrorLogTime > ERROR_LOG_INTERVAL) {
            plugin.getLogger().log(Level.SEVERE, message, e);
            lastErrorLogTime = now;
        }
    }
}