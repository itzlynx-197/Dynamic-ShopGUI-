package com.silicon_studios.dynamic_shopgui_plus.features.databasesync;

import com.silicon_studios.dynamic_shopgui_plus.DynamicShopGUIPlus;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class DatabaseSyncManager {
    private final DynamicShopGUIPlus plugin;
    private final DatabaseConfig config;
    private Connection connection;
    private boolean connected = false;

    public DatabaseSyncManager(DynamicShopGUIPlus plugin, DatabaseConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public boolean connect() {
        try {
            switch (config.type) {
                case MYSQL:
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    connection = DriverManager.getConnection("jdbc:mysql://" + config.host + ":" + config.port + "/" + config.database + "?useSSL=false&autoReconnect=true", config.username, config.password);
                    break;
                case POSTGRES:
                    Class.forName("org.postgresql.Driver");
                    connection = DriverManager.getConnection("jdbc:postgresql://" + config.host + ":" + config.port + "/" + config.database, config.username, config.password);
                    break;
                case SQLITE:
                default:
                    Class.forName("org.sqlite.JDBC");
                    connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/databasesync.db");
                    break;
            }
            connected = true;
            initSchema();
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("DatabaseSync: failed to connect: " + e.getMessage());
            connected = false;
            return false;
        }
    }

    private void initSchema() throws Exception {
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS " + config.tablePrefix + "transactions (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid TEXT NOT NULL, item TEXT NOT NULL, amount INTEGER NOT NULL, price REAL NOT NULL, action TEXT NOT NULL, ts BIGINT NOT NULL)");
            st.execute("CREATE TABLE IF NOT EXISTS " + config.tablePrefix + "stock (item TEXT PRIMARY KEY, used INTEGER NOT NULL, window_start BIGINT NOT NULL)");
            st.execute("CREATE TABLE IF NOT EXISTS " + config.tablePrefix + "prices (item TEXT PRIMARY KEY, multiplier REAL NOT NULL, updated BIGINT NOT NULL)");
        }
    }

    public boolean isConnected() { return connected; }

    public void logTransaction(String uuid, String item, int amount, double price, String action) {
        if (!connected || !config.syncTransactions) return;
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO " + config.tablePrefix + "transactions (uuid,item,amount,price,action,ts) VALUES (?,?,?,?,?,?)")) {
            ps.setString(1, uuid);
            ps.setString(2, item);
            ps.setInt(3, amount);
            ps.setDouble(4, price);
            ps.setString(5, action);
            ps.setLong(6, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (Exception e) {
            plugin.getLogger().warning("DatabaseSync: logTransaction failed: " + e.getMessage());
        }
    }

    public void syncStock(String item, int used, long windowStart) {
        if (!connected || !config.syncStock) return;
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO " + config.tablePrefix + "stock (item,used,window_start) VALUES (?,?,?) ON CONFLICT(item) DO UPDATE SET used=?,window_start=?")) {
            ps.setString(1, item);
            ps.setInt(2, used);
            ps.setLong(3, windowStart);
            ps.setInt(4, used);
            ps.setLong(5, windowStart);
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }

    public void syncPrice(String item, double multiplier) {
        if (!connected || !config.syncPrices) return;
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO " + config.tablePrefix + "prices (item,multiplier,updated) VALUES (?,?,?) ON CONFLICT(item) DO UPDATE SET multiplier=?,updated=?")) {
            ps.setString(1, item);
            ps.setDouble(2, multiplier);
            ps.setLong(3, System.currentTimeMillis());
            ps.setDouble(4, multiplier);
            ps.setLong(5, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }

    public void close() {
        try { if (connection != null) connection.close(); } catch (Exception ignored) {}
        connected = false;
    }
}