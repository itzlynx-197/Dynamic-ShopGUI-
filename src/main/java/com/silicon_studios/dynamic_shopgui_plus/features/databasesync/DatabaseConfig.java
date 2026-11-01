package com.silicon_studios.dynamic_shopgui_plus.features.databasesync;

public class DatabaseConfig {
    public enum Type { SQLITE, MYSQL, POSTGRES }

    public Type type = Type.SQLITE;
    public String host = "localhost";
    public int port = 3306;
    public String database = "dynamicshopgui";
    public String username = "root";
    public String password = "";
    public String tablePrefix = "dsg_";
    public boolean syncTransactions = true;
    public boolean syncStock = true;
    public boolean syncPrices = true;
}