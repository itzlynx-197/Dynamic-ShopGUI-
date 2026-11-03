

package com.siliconstudios.dynamicshopguiplus.files;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.objects.ShopItem;
import com.siliconstudios.dynamicshopguiplus.util.EcoType;
import com.siliconstudios.dynamicshopguiplus.util.JsonUtil;
import com.siliconstudios.dynamicshopguiplus.util.Transaction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public class TransactionLog {
    private File file;
    private Database db;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final ArrayList<String> cache = new ArrayList();
    private final DynamicShopGUIPlus plugin;
    private ArrayList<SimpleTransaction> TRANSACTION_CACHE;

    public TransactionLog(DynamicShopGUIPlus plugin) {
        this.plugin = plugin;
    }

    public boolean setup() {
        if (this.db != null) {
            this.saveUnsaved();
        }
        this.file = new File(this.plugin.getDataFolder(), "transaction-log.txt");
        if (!this.file.exists()) {
            try {
                this.file.createNewFile();
            }
            catch (IOException e) {
                SendMessage.errorMessage("Failed to create the transactions log file");
                return false;
            }
        }
        try {
            this.db = new Database();
        }
        catch (IOException e) {
            SendMessage.warnMessage("Failed to create transaction directory...");
            return false;
        }
        return true;
    }

    public void log(String transaction) {
        this.cache.add("[" + this.dateFormatter.format(new Date().getTime()) + "] - " + transaction);
    }

    public void log(UUID uuid, ShopItem item, Map<EcoType, Double> prices, int amount, Transaction.Mode action, Transaction.Type method) {
        this.db.cacheSingle(uuid.toString(), item.getItemPath(), new Gson().toJson(prices), amount, method, action);
        if (this.TRANSACTION_CACHE != null) {
            this.TRANSACTION_CACHE.add(new SimpleTransaction(Instant.now().toEpochMilli(), uuid.toString(), item.getItemPath(), amount, action.name()));
        }
    }

    public void log(UUID uuid, Map<ShopItem, Integer> items, Map<EcoType, Double> prices, int amount, Transaction.Mode action, Transaction.Type method) {
        String i = new Gson().toJson(items.entrySet().stream().map(x$0 -> new Item((Map.Entry<ShopItem, Integer>)x$0)).collect(Collectors.toList()));
        String p = new Gson().toJson(prices);
        this.db.cacheMultiple(uuid.toString(), i, p, amount, method, action);
        if (this.TRANSACTION_CACHE != null) {
            long date = Instant.now().toEpochMilli();
            for (Map.Entry<ShopItem, Integer> entry : items.entrySet()) {
                this.TRANSACTION_CACHE.add(new SimpleTransaction(date, uuid.toString(), entry.getKey().getItemPath(), entry.getValue(), action.name()));
            }
        }
    }

    public Collection<SimpleTransaction> getTransactions(TimeFrame timeFrame, Transaction.Mode mode) {
        long[] range = this.getTimeFrameRange(timeFrame);
        return this.TRANSACTION_CACHE.stream().filter(transaction -> transaction.action == mode && transaction.date >= range[0] && transaction.date <= range[1]).collect(Collectors.toList());
    }

    private long[] getTimeFrameRange(TimeFrame timeFrame) {
        long now = Instant.now().toEpochMilli();
        if (timeFrame == TimeFrame.MONTH) {
            return new long[]{now - TimeUnit.DAYS.toMillis(30L), now};
        }
        if (timeFrame == TimeFrame.WEEK) {
            return new long[]{now - TimeUnit.DAYS.toMillis(7L), now};
        }
        long day = TimeUnit.DAYS.toMillis(1L);
        long min = this.getStartOfDay();
        return new long[]{min, min + day};
    }

    private long getStartOfDay() {
        LocalDateTime current = LocalDateTime.now();
        LocalDateTime start = current.toLocalDate().atTime(LocalTime.MIN);
        Instant instant = start.atZone(ZoneId.systemDefault()).toInstant();
        return instant.toEpochMilli();
    }

    private void save() {
        if (!this.cache.isEmpty()) {
            try {
                Files.write(this.file.toPath(), this.cache, StandardOpenOption.APPEND);
                this.cache.clear();
            }
            catch (IOException e) {
                SendMessage.errorMessage("Failed to create the transactions log file");
            }
        }
    }

    public void saveUnsaved() {
        if (this.db != null) {
            this.db.saveUnsaved();
        }
        this.save();
    }

    public void loadCache() {
        if (this.TRANSACTION_CACHE != null) {
            return;
        }
        this.saveUnsaved();
        try {
            this.TRANSACTION_CACHE = this.db.getAllSimple();
        }
        catch (Exception e) {
            SendMessage.warnMessage("A unknown error occurred while loading transaction cache from database:\n" + this.stackTraceToString(e));
        }
    }

    public String exportLogs() {
        this.loadCache();
        ArrayList<AdvancedTransaction> logs = this.plugin.getTransactionLog().retrieveAllTransactions();
        Gson gson = new Gson();
        StringBuilder generalStats = new StringBuilder();
        generalStats.append("{\"generalStats\":" + gson.toJson((Object)new GeneralStats(logs)) + "},");
        StringBuilder topPlayers = new StringBuilder();
        topPlayers.append("{\"topPlayers\":[{\"PBM\":" + gson.toJson(this.getTopThreePlayers(TimeFrame.MONTH, Transaction.Mode.BUY)) + "},");
        topPlayers.append("{\"PBW\":" + gson.toJson(this.getTopThreePlayers(TimeFrame.WEEK, Transaction.Mode.BUY)) + "},");
        topPlayers.append("{\"PBD\":" + gson.toJson(this.getTopThreePlayers(TimeFrame.DAY, Transaction.Mode.BUY)) + "},");
        topPlayers.append("{\"PSM\":" + gson.toJson(this.getTopThreePlayers(TimeFrame.MONTH, Transaction.Mode.SELL)) + "},");
        topPlayers.append("{\"PSW\":" + gson.toJson(this.getTopThreePlayers(TimeFrame.WEEK, Transaction.Mode.SELL)) + "},");
        topPlayers.append("{\"PSD\":" + gson.toJson(this.getTopThreePlayers(TimeFrame.DAY, Transaction.Mode.SELL)) + "}]},");
        StringBuilder topItems = new StringBuilder();
        topItems.append("{\"topItems\":[{\"IBM\":" + gson.toJson(this.getTopThreeItems(TimeFrame.MONTH, Transaction.Mode.BUY, true)) + "},");
        topItems.append("{\"IBW\":" + gson.toJson(this.getTopThreeItems(TimeFrame.WEEK, Transaction.Mode.BUY, true)) + "},");
        topItems.append("{\"IBD\":" + gson.toJson(this.getTopThreeItems(TimeFrame.DAY, Transaction.Mode.BUY, true)) + "},");
        topItems.append("{\"ISM\":" + gson.toJson(this.getTopThreeItems(TimeFrame.MONTH, Transaction.Mode.SELL, true)) + "},");
        topItems.append("{\"ISW\":" + gson.toJson(this.getTopThreeItems(TimeFrame.WEEK, Transaction.Mode.SELL, true)) + "},");
        topItems.append("{\"ISD\":" + gson.toJson(this.getTopThreeItems(TimeFrame.DAY, Transaction.Mode.SELL, true)) + "}]}");
        String rawLogs = gson.toJson(logs, ArrayList.class);
        return "{\"stats\":[" + generalStats + topPlayers + topItems + "],\"dates\":" + Arrays.toString(this.db.getAllLoaded().stream().map(m -> "\"" + m.toString() + "\"").toArray()) + ",\"logs\":" + rawLogs + "}";
    }

    private List<SimplePrice> getCashFlow(ArrayList<AdvancedTransaction> transactions, Transaction.Mode action) {
        Map<String, Double> sorted = transactions.stream().filter(trans -> trans.action == action).flatMap(trans -> trans.prices.stream()).collect(Collectors.groupingBy(Price::getEcoType, Collectors.summingDouble(Price::getAmount)));
        ArrayList<SimplePrice> prices = new ArrayList<SimplePrice>();
        for (String ecoType : sorted.keySet()) {
            String formatted = this.plugin.formatNullablePrice(ecoType, sorted.get(ecoType));
            if (formatted == null) continue;
            prices.add(new SimplePrice(this.formatHTMLColors(formatted)));
        }
        return prices;
    }

    public ArrayList<TopPlayer> getTopThreePlayers(TimeFrame timeFrame, Transaction.Mode mode) {
        HashMap<UUID, Integer> map = new HashMap<UUID, Integer>();
        for (SimpleTransaction transaction : this.getTransactions(timeFrame, mode)) {
            UUID uuid = transaction.uuid;
            int amount = transaction.amount;
            map.put(uuid, map.getOrDefault(uuid, 0) + amount);
        }
        ArrayList<Map.Entry<UUID,Integer>> entryList = new ArrayList<>(map.entrySet());
        entryList.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        ArrayList<TopPlayer> topThree = new ArrayList<TopPlayer>();
        for (int i = 0; i < 3; ++i) {
            try {
                Map.Entry<UUID,Integer> e = entryList.get(i);
                topThree.add(new TopPlayer(e.getKey(), e.getValue()));
                continue;
            }
            catch (IndexOutOfBoundsException ignored) {
                break;
            }
        }
        return topThree;
    }

    public ArrayList<TopItem> getTopThreeItems(TimeFrame timeFrame, Transaction.Mode mode) {
        return this.getTopThreeItems(timeFrame, mode, false);
    }

    private ArrayList<TopItem> getTopThreeItems(TimeFrame timeFrame, Transaction.Mode mode, boolean html) {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        for (SimpleTransaction transaction : this.getTransactions(timeFrame, mode)) {
            String item = transaction.itemPath;
            int amount = transaction.amount;
            map.put(item, map.getOrDefault(item, 0) + amount);
        }
        ArrayList<Map.Entry<String,Integer>> entryList = new ArrayList<>(map.entrySet());
        entryList.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        ArrayList<TopItem> topThree = new ArrayList<TopItem>();
        for (int i = 0; i < 3; ++i) {
            try {
                Map.Entry<String,Integer> e = entryList.get(i);
                topThree.add(new TopItem((String)e.getKey(), (Integer)e.getValue(), html));
                continue;
            }
            catch (IndexOutOfBoundsException ignored) {
                break;
            }
        }
        return topThree;
    }

    public int getItemTransactions(Transaction.Mode mode, TimeFrame timeFrame, String section, String index, UUID uuid) {
        long[] range = this.getTimeFrameRange(timeFrame);
        int count = 0;
        for (SimpleTransaction transaction : this.TRANSACTION_CACHE) {
            if (transaction.action != mode || transaction.date < range[0] || transaction.date > range[1] || uuid != null && !transaction.uuid.equals(uuid) || section != null && !(index == null ? transaction.itemPath.startsWith(section + ".") : transaction.itemPath.equals(section + "." + index))) continue;
            count += transaction.amount;
        }
        return count;
    }

    public ArrayList<AdvancedTransaction> retrieveAllTransactions() {
        ArrayList<AdvancedTransaction> transactions = new ArrayList<AdvancedTransaction>();
        for (SimpleMonth date : this.db.getAllLoaded()) {
            try {
                transactions.addAll(this.db.getAllAdvanced(date));
            }
            catch (Exception e) {
                SendMessage.warnMessage("Failed to retrieve logged transactions from " + date.month + "/" + date.year);
                e.printStackTrace();
            }
        }
        Collections.sort(transactions, new Comparator<AdvancedTransaction>(){

            @Override
            public int compare(AdvancedTransaction first, AdvancedTransaction second) {
                return Long.compare(second.date, first.date);
            }
        });
        return transactions;
    }

    private String stackTraceToString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    private String formatHTMLColors(String s) {
        StringBuilder builder = new StringBuilder();
        Line currentText = new Line();
        Color lastColor = new Color();
        Format lastFormat = new Format();
        boolean lc = false;
        try {
            for (int i = 0; i < s.length(); ++i) {
                char c = s.charAt(i);
                if (c == '\u00a7') {
                    char id = s.charAt(i + 1);
                    if (id == 'x' && s.charAt(i + 8) == '\u00a7') {
                        if (!lastColor.c.isEmpty()) {
                            if (!lastFormat.start.isEmpty()) {
                                builder.append(lastFormat.complete(currentText.get()));
                            } else {
                                builder.append(currentText.get());
                            }
                            builder.append(lastColor.get());
                        }
                        builder.append("<a style=\"color:" + this.getRGB(s, i) + "\">");
                        lastColor.c = "</a>";
                        i += 13;
                        continue;
                    }
                    ChatColor color = ChatColor.getByChar((char)id);
                    if (color == null) continue;
                    if (color.isColor()) {
                        if (!lastColor.c.isEmpty()) {
                            if (!lastFormat.start.isEmpty()) {
                                builder.append(lastFormat.complete(currentText.get()));
                            } else {
                                builder.append(currentText.get());
                            }
                            builder.append(lastColor.get());
                        }
                        builder.append("<a style=\"color:" + color.name().toLowerCase(Locale.ROOT).replace("_", "") + "\">");
                        lastColor.c = "</a>";
                        ++i;
                        continue;
                    }
                    if (color.isFormat()) {
                        String f = this.getFormat(color);
                        if (f != null) {
                            if (!lastFormat.start.isEmpty()) {
                                builder.append(lastFormat.complete(currentText.get()));
                            } else {
                                builder.append(currentText.get());
                            }
                            lastFormat.start = lastFormat.start + "<" + f + ">";
                            lastFormat.ending = "</" + f + ">" + lastFormat.ending;
                            ++i;
                            continue;
                        }
                        currentText.text = currentText.text + c;
                        continue;
                    }
                    if (color != ChatColor.RESET) continue;
                    this.closeElement(builder, lastColor, lastFormat, currentText);
                    ++i;
                    continue;
                }
                currentText.text = currentText.text + c;
            }
            if (!lastFormat.start.isEmpty()) {
                builder.append(lastFormat.complete(currentText.get()));
            } else {
                builder.append(currentText.get());
            }
            if (!lastColor.c.isEmpty()) {
                builder.append(lastColor.get());
            }
        }
        catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            
        }
        return builder.toString();
    }

    private void closeElement(StringBuilder builder, Color lastColor, Format lastFormat, Line currentText) {
        if (!lastColor.c.isEmpty()) {
            if (!lastFormat.start.isEmpty()) {
                builder.append(lastFormat.complete(currentText.get()));
            } else {
                builder.append(currentText.get());
            }
            builder.append(lastColor.get());
        } else {
            if (!lastFormat.start.isEmpty()) {
                builder.append(lastFormat.complete(currentText.get()));
            } else {
                builder.append(currentText.get());
            }
            builder.append(currentText.get());
        }
    }

    private String getFormat(ChatColor color) {
        switch (color) {
            case ITALIC: {
                return "i";
            }
            case BOLD: {
                return "b";
            }
            case STRIKETHROUGH: {
                return "del";
            }
            case UNDERLINE: {
                return "u";
            }
        }
        return null;
    }

    private String getRGB(String s, int i) {
        return s.substring(i, i + 14).replace("\u00a7", "").replace("x", "#");
    }

    private ArrayList<Price> deserializePrices(String prices) {
        JsonObject obj = (JsonObject)new Gson().fromJson(prices, JsonObject.class);
        return obj.entrySet().stream().map(e -> new Price((String)e.getKey(), Double.parseDouble(((JsonElement)e.getValue()).getAsString()))).collect(Collectors.toCollection(ArrayList::new));
    }

    private List<MultipleItem> deserializeItems(String items) {
        try {
            JSONArray list = JsonUtil.parseArray(items);
            return (List<MultipleItem>) (Object) list.stream().map(e -> {
                try {
                    JSONObject obj = JsonUtil.parseJson(e.toString());
                    return new MultipleItem(Integer.valueOf(obj.get((Object)"amount").toString()), obj.get((Object)"itemPath").toString());
                }
                catch (ParseException error) {
                    SendMessage.warnMessage("Failed to parse item '" + items + "' for transaction, skipping...");
                    error.printStackTrace();
                    return new MultipleItem(0, "unknown");
                }
            }).collect(Collectors.toList());
        }
        catch (ParseException e2) {
            SendMessage.errorMessage("Failed to parse item(s) '" + items + "' for transaction, skipping...");
            e2.printStackTrace();
            return null;
        }
    }

    private class Database {
        private Connection conn;
        private final String table;
        private final LinkedList<Table> tables = new LinkedList();
        private final ArrayList<String> multiple_item_transactions = new ArrayList();
        private final ArrayList<String> single_item_transactions = new ArrayList();

        public List<SimpleMonth> getAllLoaded() {
            return this.tables.stream().map(t -> t.date).collect(Collectors.toList());
        }

        public List<SimpleMonth> getAllSince(SimpleMonth since) {
            return this.getAllLoaded().stream().filter(d -> d.equals(since) || d.isLaterThen(since)).collect(Collectors.toList());
        }

        public Database() throws IOException {
            Calendar date = Calendar.getInstance();
            this.table = "TRANSACTIONS_" + (date.get(2) + 1) + "_" + date.get(1);
            try {
                Files.createDirectory(Paths.get(TransactionLog.this.plugin.getDataFolder() + File.separator + "transactions", new String[0]), new FileAttribute[0]);
            }
            catch (FileAlreadyExistsException fileAlreadyExistsException) {
                
            }
            this.connect();
        }

        public boolean connect() {
            try {
                Class.forName("org.sqlite.JDBC");
                this.conn = DriverManager.getConnection("jdbc:sqlite:" + TransactionLog.this.plugin.getDataFolder() + File.separator + "transactions" + File.separator + "transactions.db");
                this.createTable();
                this.loadTables();
                this.checkTableConstraints();
                return true;
            }
            catch (Exception e) {
                SendMessage.errorMessage("A error occurred while trying to connect to the transactions database");
                e.printStackTrace();
                return false;
            }
        }

        private void checkTableConstraints() {
            try {
                ResultSet rs2;
                boolean updated = false;
                Statement stmt = this.conn.createStatement();
                ResultSet rs = stmt.executeQuery("PRAGMA index_list('MULTIPLE_ITEM_" + this.table + "');");
                if (rs.next()) {
                    stmt.executeUpdate("CREATE TABLE IF NOT EXISTS MULTIPLE_ITEM_" + this.table + "_NEW (date REAL NOT NULL,uuid TEXT NOT NULL,items TEXT NOT NULL,prices TEXT NOT NULL,amount INTEGER NOT NULL,action TEXT NOT NULL,method TEXT NOT NULL);");
                    stmt.executeUpdate("INSERT INTO MULTIPLE_ITEM_" + this.table + "_NEW (date, uuid, items, prices, amount, action, method) SELECT date, uuid, items, prices, amount, action, method FROM MULTIPLE_ITEM_" + this.table + ";");
                    stmt.executeUpdate("DROP TABLE MULTIPLE_ITEM_" + this.table + ";");
                    stmt.executeUpdate("ALTER TABLE MULTIPLE_ITEM_" + this.table + "_NEW RENAME TO MULTIPLE_ITEM_" + this.table + ";");
                    updated = true;
                }
                if ((rs2 = stmt.executeQuery("PRAGMA index_list('SINGLE_ITEM_" + this.table + "');")).next()) {
                    stmt.executeUpdate("CREATE TABLE IF NOT EXISTS SINGLE_ITEM_" + this.table + "_NEW (date REAL NOT NULL,uuid TEXT NOT NULL,item TEXT NOT NULL,prices TEXT NOT NULL,amount INTEGER NOT NULL,action TEXT NOT NULL,method TEXT NOT NULL);");
                    stmt.executeUpdate("INSERT INTO SINGLE_ITEM_" + this.table + "_NEW (date, uuid, item, prices, amount, action, method) SELECT date, uuid, item, prices, amount, action, method FROM SINGLE_ITEM_" + this.table + ";");
                    stmt.executeUpdate("DROP TABLE SINGLE_ITEM_" + this.table + ";");
                    stmt.executeUpdate("ALTER TABLE SINGLE_ITEM_" + this.table + "_NEW RENAME TO SINGLE_ITEM_" + this.table + ";");
                    updated = true;
                }
                if (updated) {
                    SendMessage.infoMessage("Successfully removed unique constraint from transaction db tables");
                }
            }
            catch (SQLException e) {
                SendMessage.warnMessage("Failed to migrate database to new format for " + this.table);
                e.printStackTrace();
            }
        }

        private void createTable() {
            try {
                Statement stmt = this.conn.createStatement();
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS MULTIPLE_ITEM_" + this.table + " (date REAL NOT NULL,uuid TEXT NOT NULL,items TEXT NOT NULL,prices TEXT NOT NULL,amount INTEGER NOT NULL,action TEXT NOT NULL,method TEXT NOT NULL);");
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS SINGLE_ITEM_" + this.table + " (date REAL NOT NULL,uuid TEXT NOT NULL,item TEXT NOT NULL,prices TEXT NOT NULL,amount INTEGER NOT NULL,action TEXT NOT NULL,method TEXT NOT NULL);");
                stmt.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void loadTables() {
            HashSet<String> tables = new HashSet<String>();
            try (Statement stmt = this.conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type = 'table';");){
                while (rs.next()) {
                    tables.add(rs.getString(1));
                }
            }
            catch (SQLException e) {
                SendMessage.errorMessage("Failed to load transaction tables");
                e.printStackTrace();
            }
            for (String s : tables) {
                try {
                    SimpleMonth date = new SimpleMonth(s);
                    Table table = this.tables.stream().filter(t -> t.date.equals(date)).findFirst().orElse(new Table(date));
                    if (s.contains("MULTIPLE")) {
                        table.multiple_items = s;
                    } else if (s.contains("SINGLE")) {
                        table.single_items = s;
                    }
                    this.tables.remove(table);
                    this.tables.add(table);
                }
                catch (ArrayIndexOutOfBoundsException | NumberFormatException runtimeException) {}
            }
            Collections.sort(this.tables, new Comparator<Table>(){

                @Override
                public int compare(Table first, Table second) {
                    return first.date.compareTo(second.date);
                }
            });
        }

        private void executeBatch(List<String> queries) {
            try {
                Statement stmt = this.conn.createStatement();
                for (String query : new ArrayList<String>(queries)) {
                    stmt.executeUpdate(query);
                }
                queries.clear();
                stmt.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void cacheMultiple(String uuid, String items, String prices, int amount, Transaction.Type method, Transaction.Mode action) {
            this.multiple_item_transactions.add(String.format("INSERT INTO %1$s VALUES(%2$d,'%3$s','%4$s','%5$s',%6$d,'%7$s','%8$s')", "MULTIPLE_ITEM_" + this.table, Instant.now().toEpochMilli(), uuid, items, prices, amount, action.name(), method.enumName()));
            if (this.multiple_item_transactions.size() == 10) {
                TransactionLog.this.plugin.runTaskAsync(() -> this.executeBatch(this.multiple_item_transactions));
            }
        }

        private void cacheSingle(String uuid, String item, String prices, int amount, Transaction.Type method, Transaction.Mode action) {
            this.single_item_transactions.add(String.format("INSERT INTO %1$s VALUES(%2$d,'%3$s','%4$s','%5$s',%6$d,'%7$s','%8$s')", "SINGLE_ITEM_" + this.table, Instant.now().toEpochMilli(), uuid, item, prices, amount, action.name(), method.enumName()));
            if (this.single_item_transactions.size() == 10) {
                TransactionLog.this.plugin.runTaskAsync(() -> this.executeBatch(this.single_item_transactions));
            }
        }

        private ArrayList<SimpleTransaction> getAllSimple() throws Exception {
            ArrayList<SimpleTransaction> CACHE = new ArrayList<SimpleTransaction>();
            LocalDate earliest = LocalDate.now().minusDays(30L);
            SimpleMonth earliestMonth = new SimpleMonth(earliest.getMonthValue(), earliest.getYear());
            for (Table table : this.tables) {
                if (table.date.isOlderThen(earliestMonth)) continue;
                this.appendSimpleTransactions(CACHE, table);
            }
            return CACHE;
        }

        
        private void appendSimpleTransactions(ArrayList<SimpleTransaction> CACHE, Table table) throws Exception {
            Statement stmt = null;
            ResultSet rs = null;
            try {
                stmt = this.conn.createStatement();
                if (table.single_items != null) {
                    rs = stmt.executeQuery("SELECT * FROM " + table.single_items + ";");
                    while (rs.next()) {
                        CACHE.add(new SimpleTransaction(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getInt(5), rs.getString(6)));
                    }
                }
                if (table.multiple_items != null) {
                    rs = stmt.executeQuery("SELECT * FROM " + table.multiple_items + ";");
                    while (rs.next()) {
                        long date = rs.getLong(1);
                        String uuid = rs.getString(2);
                        String action = rs.getString(6);
                        for (Item item : (Item[])new Gson().fromJson(rs.getString(3), Item[].class)) {
                            CACHE.add(new SimpleTransaction(date, uuid, item.itemPath, item.amount, action));
                        }
                    }
                }
            }
            finally {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            }
        }

        private Table getTableFrom(SimpleMonth date) throws NoSuchElementException {
            return this.tables.stream().filter(t -> t.date.equals(date)).findFirst().get();
        }

        
        private ArrayList<AdvancedTransaction> getAllAdvanced(SimpleMonth timestamp) throws Exception {
            Table table = this.getTableFrom(timestamp);
            ArrayList<AdvancedTransaction> CACHE = new ArrayList<AdvancedTransaction>();
            Statement stmt = null;
            ResultSet rs = null;
            try {
                stmt = this.conn.createStatement();
                if (table.single_items != null) {
                    rs = stmt.executeQuery("SELECT * FROM " + table.single_items + ";");
                    while (rs.next()) {
                        CACHE.add(new AdvancedTransaction(rs.getLong(1), rs.getString(2), new SingleItem(rs.getString(3), true), TransactionLog.this.deserializePrices(rs.getString(4)), rs.getInt(5), rs.getString(6), rs.getString(7)));
                    }
                }
                if (table.multiple_items != null) {
                    rs = stmt.executeQuery("SELECT * FROM " + table.multiple_items + ";");
                    while (rs.next()) {
                        CACHE.add(new AdvancedTransaction(rs.getLong(1), rs.getString(2), TransactionLog.this.deserializeItems(rs.getString(3)), TransactionLog.this.deserializePrices(rs.getString(4)), rs.getInt(5), rs.getString(6), rs.getString(7)));
                    }
                }
            }
            finally {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            }
            return CACHE;
        }

        public void saveUnsaved() {
            if (!this.multiple_item_transactions.isEmpty()) {
                this.executeBatch(this.multiple_item_transactions);
            }
            if (!this.single_item_transactions.isEmpty()) {
                this.executeBatch(this.single_item_transactions);
            }
        }
    }

    public final class SimpleTransaction {
        public final long date;
        public final UUID uuid;
        public final int amount;
        public final String itemPath;
        public final Transaction.Mode action;

        public SimpleTransaction(long date, String uuid, String itemPath, int amount, String action) {
            this.date = date;
            this.uuid = UUID.fromString(uuid);
            this.itemPath = itemPath;
            this.amount = amount;
            this.action = Transaction.Mode.valueOf(action);
        }
    }

    public static enum TimeFrame {
        DAY,
        WEEK,
        MONTH;

    }

    public final class GeneralStats {
        int transactions;
        List<SimplePrice> cashIn;
        List<SimplePrice> cashOut;

        public GeneralStats(ArrayList<AdvancedTransaction> logs) {
            this.transactions = logs.size();
            this.cashIn = TransactionLog.this.getCashFlow(logs, Transaction.Mode.BUY);
            this.cashOut = TransactionLog.this.getCashFlow(logs, Transaction.Mode.SELL);
        }
    }

    private final class SimplePrice {
        String formatted;

        public SimplePrice(String formatted) {
            this.formatted = formatted;
        }
    }

    public class TopPlayer {
        public final Player player;
        public final int amount;

        public TopPlayer(UUID uuid, int amount) {
            this.player = new Player(uuid);
            this.amount = amount;
        }
    }

    public class TopItem {
        public final SingleItem item;
        public final int amount;

        public TopItem(String item, int amount, boolean html) {
            this.item = new SingleItem(item, html);
            this.amount = amount;
        }
    }

    public class SimpleMonth
    implements Comparable<SimpleMonth> {
        private final int month;
        private final int year;

        public SimpleMonth(String table) throws NumberFormatException {
            String date = table.split("_ITEM_TRANSACTIONS_")[1];
            this.month = Integer.parseInt(date.split("_")[0]);
            this.year = Integer.parseInt(date.split("_")[1]);
        }

        public SimpleMonth(int month, int year) {
            this.month = month;
            this.year = year;
        }

        public boolean isLaterThen(SimpleMonth date) {
            return this.year > date.year || this.year == date.year && this.month > date.month;
        }

        public boolean isOlderThen(SimpleMonth date) {
            return this.year < date.year || this.year == date.year && this.month < date.month;
        }

        @Override
        public int compareTo(@NotNull SimpleMonth o) {
            return this.isLaterThen(o) ? 1 : (this.isOlderThen(o) ? -1 : 0);
        }

        public String toString() {
            return this.month + "/" + this.year;
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object != null && this.getClass() == object.getClass()) {
                SimpleMonth date = (SimpleMonth)object;
                return this.month == date.month && this.year == date.year;
            }
            return false;
        }

        public int hashCode() {
            return Objects.hash(this.month, this.year);
        }
    }

    private final class Line {
        String text = "";

        public String get() {
            String s = this.text;
            this.text = "";
            return s;
        }
    }

    private final class Color {
        String c = "";

        public String get() {
            String s = this.c;
            this.c = "";
            return s;
        }

        public void addColor(String c) {
            this.c = c + this.c;
        }
    }

    private final class Format {
        String start = "";
        String ending = "";

        public String complete(String currentText) {
            String s = this.start + currentText + this.ending;
            this.start = "";
            this.ending = "";
            return s;
        }
    }

    public final class MultipleItem {
        int amount;
        String name;
        String item;
        Material mat;

        public MultipleItem(int amount, String item) {
            this.amount = amount;
            try {
                ShopItem shopItem = TransactionLog.this.plugin.getShopItem(item);
                this.name = TransactionLog.this.formatHTMLColors(shopItem.getDisplayname());
                this.mat = shopItem.getItemToGive().getType();
            }
            catch (NullPointerException e) {
                this.name = "Unknown item";
                this.mat = Material.BARRIER;
            }
            this.item = item;
        }
    }

    public final class Price {
        String formatted;
        String ecoType;
        double amount;

        public Price(String ecoType, Double amount) {
            try {
                this.formatted = TransactionLog.this.formatHTMLColors(TransactionLog.this.plugin.getEcoHandler().getLoaded(ecoType).formatPrice(amount));
            }
            catch (NullPointerException e) {
                this.formatted = amount + " " + ecoType;
            }
            this.amount = amount;
            this.ecoType = ecoType;
        }

        public String getEcoType() {
            return this.ecoType;
        }

        public double getAmount() {
            return this.amount;
        }
    }

    public final class AdvancedTransaction {
        public final long date;
        public final Player player;
        public final Object items;
        public final ArrayList<Price> prices;
        public final int amount;
        public final Transaction.Mode action;
        public final String type;

        public AdvancedTransaction(long date, String uuid, Object items, ArrayList<Price> prices, int amount, String mode, String type) {
            this.date = date;
            this.player = new Player(UUID.fromString(uuid));
            this.items = items;
            this.prices = prices;
            this.amount = amount;
            this.action = Transaction.Mode.valueOf(mode);
            this.type = type;
        }
    }

    private class Item {
        private int amount;
        private String itemPath;

        public Item(Map.Entry<ShopItem, Integer> e) {
            this.amount = e.getValue();
            this.itemPath = e.getKey().getItemPath();
        }
    }

    public class Table {
        public SimpleMonth date;
        public String single_items;
        public String multiple_items;

        public Table(SimpleMonth date) {
            this.date = date;
        }

        public Table withSingle(String single_items) {
            this.single_items = single_items;
            return this;
        }

        public Table withMultiple(String multiple_items) {
            this.multiple_items = multiple_items;
            return this;
        }
    }

    public final class SingleItem {
        public String name;
        String item;
        Material mat;

        public SingleItem(String item, boolean html) {
            try {
                ShopItem shopItem = TransactionLog.this.plugin.getShopItem(item);
                this.name = html ? TransactionLog.this.formatHTMLColors(shopItem.getDisplayname()) : shopItem.getDisplayname();
                this.mat = shopItem.getItemToGive().getType();
            }
            catch (NullPointerException e) {
                this.name = "Unknown item";
                this.mat = Material.BARRIER;
            }
            this.item = item;
        }
    }

    public final class Player {
        public String name;
        UUID uuid;

        public Player(UUID uuid) {
            try {
                this.name = Bukkit.getOfflinePlayer((UUID)uuid).getName();
            }
            catch (NullPointerException e) {
                this.name = "Unknown";
            }
            this.uuid = uuid;
        }
    }
}

