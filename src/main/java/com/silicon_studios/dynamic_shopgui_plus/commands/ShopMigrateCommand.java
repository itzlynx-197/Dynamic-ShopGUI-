package com.silicon_studios.dynamic_shopgui_plus.commands;

import com.silicon_studios.dynamic_shopgui_plus.DynamicShopGUIPlus;
import com.silicon_studios.dynamic_shopgui_plus.shop.ShopItem;
import com.silicon_studios.dynamic_shopgui_plus.shop.ShopManager;
import com.silicon_studios.dynamic_shopgui_plus.shop.ShopSection;
import com.silicon_studios.dynamic_shopgui_plus.storage.SharedStorageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ShopMigrateCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        SharedStorageManager ssm = DynamicShopGUIPlus.getInstance().getSharedStorageManager();
        if (ssm == null || !ssm.isProxyMode()) {
            sender.sendMessage("Shared storage is not enabled. Set proxy: true in config.yml first.");
            return true;
        }
        if (!ssm.isConnected()) {
            sender.sendMessage("Not connected to MySQL. Check your shared-storage config.");
            return true;
        }

        sender.sendMessage("Starting migration of standalone shop data to shared MySQL...");
        sender.sendMessage("Creating backup first...");

        java.io.File backupDir = new java.io.File(DynamicShopGUIPlus.getInstance().getDataFolder(), "backups");
        if (!backupDir.exists()) backupDir.mkdirs();
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new java.util.Date());
        java.io.File backupFile = new java.io.File(backupDir, "pre-migration_" + timestamp + ".zip");
        try {
            java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(backupFile));
            java.io.File dataFolder = DynamicShopGUIPlus.getInstance().getDataFolder();
            for (java.io.File f : dataFolder.listFiles()) {
                if (f.isDirectory() && (f.getName().equals("shops") || f.getName().equals("sections"))) {
                    addDirectoryToZip(zos, f, f.getName());
                }
            }
            zos.close();
            sender.sendMessage("Backup created: " + backupFile.getName());
        } catch (Exception e) {
            sender.sendMessage("Warning: Backup creation failed: " + e.getMessage());
        }

        try (Connection conn = ssm.getConnection()) {
            if (conn == null) {
                sender.sendMessage("Failed to get database connection.");
                return true;
            }

            conn.setAutoCommit(false);

            int sectionsMigrated = 0;
            int itemsMigrated = 0;
            int conflicts = 0;

            ShopManager sm = DynamicShopGUIPlus.getInstance().getShopManager();

            for (ShopSection sec : sm.getAllSections().values()) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*) FROM dsg_shop_sections WHERE name = ?")) {
                    ps.setString(1, sec.getName());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            conflicts++;
                            continue;
                        }
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO dsg_shop_sections (name, slot, title, hidden, sub_section, display_item, economy, icon_material, version, updated_at) " +
                                "VALUES (?,?,?,?,?,?,?,?,?,?)")) {
                    ps.setString(1, sec.getName());
                    ps.setInt(2, sec.getSlot());
                    ps.setString(3, sec.getTitle());
                    ps.setBoolean(4, sec.isHidden());
                    ps.setBoolean(5, sec.isSubSection());
                    ps.setBoolean(6, sec.isDisplayItem());
                    ps.setString(7, sec.getEconomy());
                    ps.setString(8, sec.getIconMaterial() != null ? sec.getIconMaterial().name() : "CHEST");
                    ps.setInt(9, 1);
                    ps.setLong(10, System.currentTimeMillis());
                    ps.executeUpdate();
                    sectionsMigrated++;
                }
            }

            for (String secName : sm.getSections()) {
                ShopSection sec = sm.getSection(secName);
                if (sec == null) continue;
                for (ShopItem item : sec.getItems()) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT COUNT(*) FROM dsg_shop_items WHERE id = ? AND section = ?")) {
                        ps.setString(1, item.getId());
                        ps.setString(2, item.getSection());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next() && rs.getInt(1) > 0) {
                                conflicts++;
                                continue;
                            }
                        }
                    }

                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO dsg_shop_items (id, section, material, display_name, lore, buy_price, sell_price, " +
                                    "stack_size, min_buy, max_buy, min_sell, max_sell, hidden, close_menu, sub_section, version, updated_at) " +
                                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
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
                        ps.setInt(16, 1);
                        ps.setLong(17, System.currentTimeMillis());
                        ps.executeUpdate();
                        itemsMigrated++;
                    }
                }
            }

            ssm.recordChange("migration", "full", "CREATE");
            conn.commit();

            sender.sendMessage("Migration complete!");
            sender.sendMessage("Sections migrated: " + sectionsMigrated);
            sender.sendMessage("Items migrated: " + itemsMigrated);
            if (conflicts > 0) {
                sender.sendMessage("Conflicts (skipped): " + conflicts);
            }
        } catch (SQLException e) {
            sender.sendMessage("Migration failed: " + e.getMessage());
            DynamicShopGUIPlus.getInstance().getLogger().severe(e.getMessage());
        }
        return true;
    }

    private void addDirectoryToZip(java.util.zip.ZipOutputStream zos, java.io.File dir, String basePath) throws Exception {
        for (java.io.File f : dir.listFiles()) {
            if (f.isDirectory()) {
                addDirectoryToZip(zos, f, basePath + "/" + f.getName());
            } else {
                java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(basePath + "/" + f.getName());
                zos.putNextEntry(entry);
                java.io.FileInputStream fis = new java.io.FileInputStream(f);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                fis.close();
                zos.closeEntry();
            }
        }
    }
}