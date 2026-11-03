

package com.siliconstudios.dynamicshopguiplus.objects.inventorys;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.CalculateAmount;
import com.siliconstudios.dynamicshopguiplus.objects.ShopInventory;
import com.siliconstudios.dynamicshopguiplus.util.ItemBuilder;
import com.siliconstudios.dynamicshopguiplus.util.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class FileUploader
extends ShopInventory {
    private final Inventory inv;
    private final Listen listener = new Listen();
    private final DynamicShopGUIPlus plugin = DynamicShopGUIPlus.getInstance();
    private final ArrayList<String> originalShops = this.initShopFiles();
    private final ArrayList<String> shops = new ArrayList<String>(this.originalShops);
    private ShopSelect shopSelect = new ShopSelect();
    private final ArrayList<String> files = new ArrayList();
    private final Consumer<ArrayList<String>> onComplete;
    private final Player player;
    private Class<? extends ShopInventory> current = FileUploader.class;

    public FileUploader(Player p, Consumer<ArrayList<String>> onComplete) {
        this.inv = Bukkit.createInventory((InventoryHolder)this, (int)45, (String)"\u00a78\u00a7lSelect files to upload");
        this.onComplete = onComplete;
        this.player = p;
        this.buildPane();
        Bukkit.getPluginManager().registerEvents((Listener)this.listener, (Plugin)this.plugin);
        p.openInventory(this.inv);
    }

    private ArrayList<String> initShopFiles() {
        Set<String> categorys = ConfigManager.getShops();
        ArrayList<String> shops = new ArrayList<String>();
        File shopFolder = new File(this.plugin.getDataFolder() + File.separator + "shops");
        if (shopFolder.exists() && shopFolder.isDirectory()) {
            Arrays.stream(shopFolder.listFiles()).forEach(f -> {
                if (f.isDirectory()) {
                    for (File nested : f.listFiles()) {
                        if (!categorys.contains(nested.getName().split("\\.")[0])) continue;
                        shops.add(f.getName() + "/" + nested.getName().split("\\.")[0]);
                    }
                } else if (categorys.contains(f.getName().split("\\.")[0])) {
                    shops.add(f.getName().split("\\.")[0]);
                }
            });
        }
        return shops;
    }

    private void buildPane() {
        this.inv.setItem(12, new ItemBuilder(XMaterial.FILLED_MAP.parseItem()).withDisplayName("\u00a7a\u00a7lshops").hideFlags().withLore(CalculateAmount.splitLongString("\u00a76\u00a7lShops selected for upload: \u00a7r\u00a7c" + this.shops.size() + " \n \n \u00a7aLeft click to select/deselect shop files \n \u00a7aRight click to select/deselect every shop file")).build());
        this.inv.setItem(14, new ItemBuilder(XMaterial.PAPER.parseItem()).hideFlags().withDisplayName("\u00a7c\u00a7lconfig.yml").withLore(CalculateAmount.splitLongString("\u00a76This file won't be uploaded by default, though, if anything related to \u00a7ftransaction screens\u00a76, \u00a7foption items\u00a76, \u00a7fnavigation bars\u00a76, \u00a7finventory sizes\u00a76, \u00a7fetc... \u00a76is customized in your shop layout, you may opt-in to also include this file in the upload process.")).build());
        this.inv.setItem(32, new ItemBuilder(XMaterial.GREEN_WOOL.parseItem()).withDisplayName("\u00a7a\u00a7lComplete upload process").build());
        this.inv.setItem(30, new ItemBuilder(XMaterial.RED_WOOL.parseItem()).withDisplayName("\u00a7c\u00a7lCancel upload process").build());
        this.fillEmpty();
    }

    private void fillEmpty() {
        if (this.plugin.createItem.fillItem == null) {
            return;
        }
        for (int i = 0; i < this.inv.getSize(); ++i) {
            if (this.inv.getItem(i) != null) continue;
            this.inv.setItem(i, this.plugin.createItem.fillItem);
        }
    }

    private void updateShops() {
        this.inv.setItem(12, new ItemBuilder(XMaterial.FILLED_MAP.parseItem()).withDisplayName("\u00a7a\u00a7lshops").hideFlags().withLore(CalculateAmount.splitLongString("\u00a76\u00a7lShops selected for upload: \u00a7r\u00a7c" + this.shops.size() + " \n \n \u00a7aLeft click to select/deselect shop files \n \u00a7aRight click to select/deselect every shop file")).build());
    }

    private void quit() {
        InventoryClickEvent.getHandlerList().unregister((Listener)this.listener);
    }

    private class ShopSelect
    extends ShopInventory {
        public final Inventory inv = Bukkit.createInventory((InventoryHolder)this, (int)54, (String)"\u00a78\u00a7lSelect shops to upload");

        public void open(Player p) {
            if (!this.inv.isEmpty()) {
                this.inv.clear();
            }
            this.update();
            this.buildPane(p);
            p.openInventory(this.inv);
        }

        private void update() {
            for (String shop : FileUploader.this.originalShops) {
                ItemBuilder builder;
                if (FileUploader.this.shops.contains(shop)) {
                    builder = new ItemBuilder(XMaterial.FILLED_MAP.parseMaterial());
                    builder.withEnchantGlint();
                    builder.hideFlags();
                    builder.withDisplayName("\u00a7a\u00a7l" + shop);
                    builder.withLore("\u00a76Click to deselect");
                } else {
                    builder = new ItemBuilder(XMaterial.PAPER.parseMaterial());
                    builder.hideFlags();
                    builder.withDisplayName("\u00a7c\u00a7l" + shop);
                    builder.withLore("\u00a76Click to select");
                }
                this.inv.addItem(new ItemStack[]{builder.build()});
            }
        }

        private void buildPane(Player p) {
            ItemStack profile = new ItemBuilder(XMaterial.PLAYER_HEAD.parseMaterial()).withSkullOwner(p.getName()).withLore(Lang.NAME.get() + ": " + ChatColor.RED + p.getDisplayName(), Lang.MONEY.get() + ": " + ChatColor.RED + FileUploader.this.plugin.formatPrice(FileUploader.this.plugin.getEcoHandler().getDefaultProvider().getType(), FileUploader.this.plugin.getEcoHandler().getDefaultProvider().getBalance((OfflinePlayer)p)), Lang.LEVEL.get() + ": " + ChatColor.RED + p.getLevel()).build();
            this.inv.setItem(45, profile);
            this.inv.setItem(53, new ItemBuilder(Material.BARRIER).withDisplayName(Lang.BACK.get()).build());
        }

        public void toggle(int slot) {
            ItemStack item = this.inv.getItem(slot);
            String shop = ChatColor.stripColor((String)item.getItemMeta().getDisplayName());
            if (FileUploader.this.shops.contains(shop)) {
                ItemBuilder builder = new ItemBuilder(XMaterial.PAPER.parseMaterial());
                builder.hideFlags();
                builder.withDisplayName("\u00a7c\u00a7l" + shop);
                builder.withLore("\u00a76Click to select");
                this.inv.setItem(slot, builder.build());
                FileUploader.this.shops.remove(shop);
            } else {
                ItemBuilder builder = new ItemBuilder(XMaterial.FILLED_MAP.parseMaterial());
                builder.withEnchantGlint();
                builder.withDisplayName("\u00a7a\u00a7l" + shop);
                builder.withLore("\u00a76Click to deselect");
                this.inv.setItem(slot, builder.build());
                FileUploader.this.shops.add(shop);
            }
        }
    }

    private class Listen
    implements Listener {
        private Listen() {
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent e) {
            if (e.getClickedInventory() == null || !(e.getClickedInventory().getHolder() instanceof ShopInventory) || e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
                return;
            }
            e.setCancelled(true);
            int slot = e.getRawSlot();
            if (e.getClickedInventory() == FileUploader.this.inv) {
                if (slot == 14) {
                    ItemBuilder builder;
                    if (!FileUploader.this.files.contains("config.yml")) {
                        builder = new ItemBuilder(XMaterial.FILLED_MAP.parseItem());
                        builder.hideFlags();
                        builder.withDisplayName("\u00a7a\u00a7lconfig.yml").withEnchantGlint();
                        FileUploader.this.files.add("config.yml");
                    } else {
                        builder = new ItemBuilder(XMaterial.PAPER.parseItem());
                        builder.withDisplayName("\u00a7c\u00a7lconfig.yml").removeEnchants();
                        FileUploader.this.files.remove("config.yml");
                    }
                    builder.withLore(CalculateAmount.splitLongString("\u00a76This file won't be uploaded by default, though, if anything related to \u00a7ftransaction screens\u00a76, \u00a7foption items\u00a76, \u00a7fnavigation bars\u00a76, \u00a7finventory sizes\u00a76, \u00a7fetc... \u00a76is customized in your shop layout, you may opt-in to also include this file in the upload process."));
                    FileUploader.this.inv.setItem(14, builder.build());
                } else if (slot == 12) {
                    if (e.getClick() == ClickType.LEFT) {
                        FileUploader.this.current = ShopSelect.class;
                        FileUploader.this.shopSelect.open(FileUploader.this.player);
                    } else if (e.getClick() == ClickType.RIGHT) {
                        if (!FileUploader.this.shops.isEmpty()) {
                            FileUploader.this.shops.clear();
                        } else {
                            FileUploader.this.shops.addAll(ConfigManager.getShops());
                        }
                        FileUploader.this.updateShops();
                    }
                } else if (slot == 30) {
                    FileUploader.this.quit();
                    e.getWhoClicked().closeInventory();
                    FileUploader.this.onComplete.accept(null);
                } else if (slot == 32) {
                    if (FileUploader.this.files.isEmpty() && FileUploader.this.shops.isEmpty()) {
                        return;
                    }
                    FileUploader.this.quit();
                    for (String shop : FileUploader.this.shops) {
                        FileUploader.this.files.add("shops/" + shop + ".yml");
                        FileUploader.this.files.add("sections/" + shop + ".yml");
                    }
                    FileUploader.this.onComplete.accept(FileUploader.this.files);
                }
            } else if (e.getClickedInventory().getHolder() instanceof ShopSelect) {
                if (slot == 53) {
                    FileUploader.this.updateShops();
                    FileUploader.this.current = FileUploader.class;
                    FileUploader.this.player.openInventory(FileUploader.this.inv);
                } else {
                    switch (XMaterial.matchXMaterial(e.getCurrentItem())) {
                        case PAPER: 
                        case FILLED_MAP: {
                            FileUploader.this.shopSelect.toggle(slot);
                        }
                    }
                }
            }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent e) {
            if (e.getInventory() == FileUploader.this.inv && FileUploader.this.current == FileUploader.class) {
                FileUploader.this.quit();
            } else if (e.getInventory().getHolder() instanceof ShopSelect && FileUploader.this.current == ShopSelect.class) {
                FileUploader.this.updateShops();
                FileUploader.this.current = FileUploader.class;
                FileUploader.this.player.openInventory(FileUploader.this.inv);
            }
        }
    }
}

