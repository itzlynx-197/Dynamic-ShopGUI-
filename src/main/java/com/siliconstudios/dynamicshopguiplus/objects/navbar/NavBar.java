

package com.siliconstudios.dynamicshopguiplus.objects.navbar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.files.Translatable;
import com.siliconstudios.dynamicshopguiplus.methodes.CreateItem;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.objects.ActionItem;
import com.siliconstudios.dynamicshopguiplus.objects.MainMenu;
import com.siliconstudios.dynamicshopguiplus.objects.ShopItem;
import com.siliconstudios.dynamicshopguiplus.objects.ShopPage;
import com.siliconstudios.dynamicshopguiplus.objects.TransactionMenu;
import com.siliconstudios.dynamicshopguiplus.objects.bedrock.BedrockInventory;
import com.siliconstudios.dynamicshopguiplus.objects.bedrock.inventorys.ShopSection;
import com.siliconstudios.dynamicshopguiplus.objects.navbar.NavBarItem;
import com.siliconstudios.dynamicshopguiplus.objects.navbar.NavItem;
import com.siliconstudios.dynamicshopguiplus.objects.navbar.PlayerProfile;
import com.siliconstudios.dynamicshopguiplus.providers.UserManager;
import com.siliconstudios.dynamicshopguiplus.util.BackButton;
import com.siliconstudios.dynamicshopguiplus.util.EcoType;
import com.siliconstudios.dynamicshopguiplus.util.LocalizedItemStack;
import com.siliconstudios.dynamicshopguiplus.util.Transaction;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.ItemLoadException;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NavBar
extends CreateItem {
    private final DynamicShopGUIPlus plugin;
    private final Map<String, InventoryNavBar> shopNavBars = new HashMap<String, InventoryNavBar>();
    private InventoryNavBar mainMenuNavBar;
    private InventoryNavBar shopsNavBar;
    private InventoryNavBar transactionNavBar;
    private InventoryNavBar shopStandsNavBar;
    private InventoryNavBar sellGUINavBar;
    private LocalizedItemStack customPlayerProfile;

    public NavBar(DynamicShopGUIPlus plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    public boolean isEnabled(String shop) {
        if (this.shopNavBars.containsKey(shop)) {
            return this.shopNavBars.get(shop).enabled;
        }
        return this.shopsNavBar.enabled;
    }

    public boolean isEnableMainNav() {
        return this.mainMenuNavBar.enabled;
    }

    public boolean isEnableTransactionNav() {
        return this.transactionNavBar.enabled;
    }

    public boolean isEnableShopStandsNav() {
        return this.shopStandsNavBar.enabled;
    }

    public boolean isEnableSellGUINav() {
        return this.sellGUINavBar.enabled;
    }

    public ItemStack getCustomPlayerProfile(Player p) {
        return this.customPlayerProfile != null ? this.customPlayerProfile.getItem(p) : null;
    }

    public void execute(Player player, ShopPage page, int i, int cp, boolean disabledBackButton) {
        NavItem item = this.getItem(page.getSection(), i - (page.getSize() - 9));
        if (item == null || item.getAction() == null) {
            return;
        }
        switch (item.getAction(cp, page.getAllPages())) {
            case "BACK": {
                if (disabledBackButton && (!page.isSubSection() || BackButton.SUB_SECTIONS)) break;
                if (page.isSubSection()) {
                    this.plugin.getSection(page.getRootSection()).openShopSection(player, disabledBackButton);
                    break;
                }
                new MainMenu(player);
                break;
            }
            case "PAGE_BACK": {
                if (cp <= 1) break;
                this.plugin.getSection(page.getSection()).openShopSection(player, cp - 1, disabledBackButton, page.getRootSection());
                break;
            }
            case "PAGE_NEXT": {
                if (cp == page.getAllPages()) break;
                this.plugin.getSection(page.getSection()).openShopSection(player, cp + 1, disabledBackButton, page.getRootSection());
                break;
            }
            case "CLOSE": {
                this.close(player);
            }
        }
    }

    public void execute(Player player, ShopPage page, ActionItem.Action action, int cp, boolean disabledBackButton) {
        switch (action) {
            case BACK: {
                if (disabledBackButton && (!page.isSubSection() || BackButton.SUB_SECTIONS)) break;
                if (page.isSubSection()) {
                    this.plugin.getSection(page.getRootSection()).openShopSection(player, disabledBackButton);
                    break;
                }
                new MainMenu(player);
                break;
            }
            case PAGE_BACK: {
                if (cp <= 1) break;
                this.plugin.getSection(page.getSection()).openShopSection(player, cp - 1, disabledBackButton, page.getRootSection());
                break;
            }
            case PAGE_NEXT: {
                if (cp == page.getAllPages()) break;
                this.plugin.getSection(page.getSection()).openShopSection(player, cp + 1, disabledBackButton, page.getRootSection());
                break;
            }
            case CLOSE: {
                this.close(player);
            }
        }
    }

    private NavItem getItem(String shop, int i) {
        return this.shopNavBars.containsKey(shop) ? this.shopNavBars.get(shop).get(i) : this.shopsNavBar.get(i);
    }

    public ItemStack[] getShopNavBar(String section, Player p, int page, int allpages) {
        if (this.shopNavBars.containsKey(section)) {
            return this.shopNavBars.get(section).get(p, section, page, allpages);
        }
        return this.shopsNavBar.get(p, section, page, allpages);
    }

    public String[] getShopNavBarTextures(String section, int page, int allpages) {
        if (this.shopNavBars.containsKey(section)) {
            return this.shopNavBars.get(section).getBedrockTextures(page, allpages);
        }
        return this.shopsNavBar.getBedrockTextures(page, allpages);
    }

    public boolean execute(ShopSection page, Player player, int i, int cp) {
        NavItem item = this.getItem(page.getSection(), i);
        if (item == null) {
            return true;
        }
        switch (item.getAction(cp, page.getAllPages())) {
            case "BACK": {
                if (!page.isDisabledBackButton() || page.isSubSection() && !BackButton.SUB_SECTIONS) {
                    if (page.isSubSection()) {
                        this.plugin.getSection(page.getRootSection()).openShopSection(player, page.isDisabledBackButton());
                    } else {
                        MainMenu.open(player);
                    }
                    return true;
                }
                return false;
            }
            case "PAGE_BACK": {
                if (cp > 1) {
                    this.plugin.getSection(page.getSection()).openShopSection(player, cp - 1, page.isDisabledBackButton(), page.getRootSection());
                    return true;
                }
                return false;
            }
            case "PAGE_NEXT": {
                if (cp != page.getAllPages()) {
                    this.plugin.getSection(page.getSection()).openShopSection(player, cp + 1, page.isDisabledBackButton(), page.getRootSection());
                    return true;
                }
                return false;
            }
            case "CLOSE": {
                this.close(player);
                return true;
            }
        }
        return false;
    }

    public boolean execute(ShopSection page, Player player, ActionItem.Action action, int cp) {
        switch (action) {
            case BACK: {
                if (!page.isDisabledBackButton() || page.isSubSection() && !BackButton.SUB_SECTIONS) {
                    if (page.isSubSection()) {
                        this.plugin.getSection(page.getRootSection()).openShopSection(player, page.isDisabledBackButton());
                    } else {
                        MainMenu.open(player);
                    }
                    return true;
                }
                return false;
            }
            case PAGE_BACK: {
                if (cp > 1) {
                    this.plugin.getSection(page.getSection()).openShopSection(player, cp - 1, page.isDisabledBackButton(), page.getRootSection());
                    return true;
                }
                return false;
            }
            case PAGE_NEXT: {
                if (cp != this.plugin.getSection(page.getSection()).getPages()) {
                    this.plugin.getSection(page.getSection()).openShopSection(player, cp + 1, page.isDisabledBackButton(), page.getRootSection());
                    return true;
                }
                return false;
            }
            case CLOSE: {
                return true;
            }
        }
        return false;
    }

    public void execute(InventoryHolder holder, Player player, int i) {
        NavItem item;
        NavItem navItem = item = holder instanceof MainMenu ? this.mainMenuNavBar.get(i) : this.sellGUINavBar.get(i);
        if (item == null) {
            return;
        }
        switch (item.getAction()) {
            case "BACK": 
            case "CLOSE": {
                this.close(player);
            }
        }
    }

    public boolean execute(BedrockInventory inv, int i) {
        NavItem item;
        NavItem navItem = item = inv instanceof com.siliconstudios.dynamicshopguiplus.objects.bedrock.inventorys.MainMenu ? this.mainMenuNavBar.get(i) : this.sellGUINavBar.get(i);
        if (item == null) {
            return true;
        }
        switch (item.getAction()) {
            case "BACK": 
            case "CLOSE": {
                return true;
            }
        }
        return false;
    }

    public void execute(Player player, ShopItem shopItem, int i, String rootSection, boolean disabledBackButton, Transaction.Mode transactionMode, Transaction.Type transactionType, int amount) {
        NavItem item = this.transactionNavBar.get(i);
        if (item == null) {
            return;
        }
        switch (item.getAction()) {
            case "TOGGLE_MODE": {
                if (item.isOnlyGeyser() && !UserManager.getUser(player).isBedrock()) break;
                new TransactionMenu(player, shopItem, rootSection, disabledBackButton, transactionMode == Transaction.Mode.BUY ? Transaction.Mode.SELL : Transaction.Mode.BUY, transactionMode == Transaction.Mode.BUY ? Transaction.Type.SELL_SCREEN : Transaction.Type.BUY_SCREEN, amount).open();
                break;
            }
            case "BACK": {
                if (transactionType != Transaction.Type.BUY_STACKS_SCREEN) {
                    com.siliconstudios.dynamicshopguiplus.objects.shops.ShopSection section = this.plugin.getSection(shopItem.section);
                    section.openShopSection(player, section.getPageForShopItem(shopItem.itemLoc), disabledBackButton, rootSection);
                    break;
                }
                new TransactionMenu(player, shopItem, rootSection, disabledBackButton, transactionMode, Transaction.Type.BUY_SCREEN, amount).open();
                break;
            }
            case "CLOSE": {
                this.close(player);
            }
        }
    }

    public void execute(Player player, ShopItem shopItem, int i, Transaction.Mode transactionMode, int amount) {
        NavItem item = this.transactionNavBar.get(i);
        if (item == null) {
            return;
        }
        switch (item.getAction()) {
            case "TOGGLE_MODE": {
                if (item.isOnlyGeyser() && !UserManager.getUser(player).isBedrock()) break;
                new TransactionMenu(player, shopItem, null, false, transactionMode == Transaction.Mode.BUY ? Transaction.Mode.SELL : Transaction.Mode.BUY, transactionMode == Transaction.Mode.BUY ? Transaction.Type.SHOPSTAND_SELL_SCREEN : Transaction.Type.SHOPSTAND_BUY_SCREEN, amount).open();
                break;
            }
            case "CLOSE": {
                this.close(player);
            }
        }
    }

    private void close(Player player) {
        UserManager.getUser(player).setOpenNewGUI(true);
        player.closeInventory();
    }

    public void loadNavBars() {
        this.mainMenuNavBar = new InventoryNavBar(ConfigManager.getConfig().getConfigurationSection("main-menu-nav-bar"));
        this.transactionNavBar = new InventoryNavBar(ConfigManager.getConfig().getConfigurationSection("transaction-screens-nav-bar"));
        this.sellGUINavBar = new InventoryNavBar(ConfigManager.getConfig().getConfigurationSection("sellgui-nav-bar"));
        this.loadShopNavBars();
        if (this.plugin.shopStands) {
            this.shopStandsNavBar = new InventoryNavBar(ConfigManager.getConfig().getConfigurationSection("shopstands-transaction-screens-nav-bar"));
        }
    }

    private void loadShopNavBars() {
        if (!this.shopNavBars.isEmpty()) {
            this.shopNavBars.clear();
        }
        this.shopsNavBar = new InventoryNavBar(ConfigManager.getConfig().getConfigurationSection("shops-nav-bar"));
        this.plugin.getShopSections().forEach(this::loadShopNavBar);
    }

    private void loadShopNavBar(String section) {
        if (ConfigManager.getSection(section).getString("nav-bar.mode", "INHERIT").equalsIgnoreCase("INHERIT")) {
            return;
        }
        this.shopNavBars.put(section, new InventoryNavBar(section));
    }

    private NavItem getDefaultItem(String path, ItemStack fillItem) throws ItemLoadException {
        try {
            return this.getNavBarItem(ConfigManager.getConfig().getConfigurationSection(path), fillItem);
        }
        catch (ItemLoadException e) {
            SendMessage.logDebugMessage(e.getMessage());
            SendMessage.errorItemConfig(path);
            return new NavBarItem(ConfigManager.getConfig().getDef().getConfigurationSection(path), fillItem);
        }
    }

    private NavItem getShopItem(String shop, String path, ItemStack fillItem) {
        try {
            return this.getNavBarItem(ConfigManager.getSection(shop).getConfigurationSection(path), fillItem);
        }
        catch (ItemLoadException e) {
            SendMessage.logDebugMessage(e.getMessage());
            SendMessage.errorSections(shop, path);
            return null;
        }
    }

    private NavItem getNavBarItem(ConfigurationSection section, ItemStack fillItem) throws ItemLoadException {
        if (section == null || section.getKeys(false).isEmpty()) {
            throw new ItemLoadException("Could not create the navigation-bar item because it was not found.");
        }
        String skull = section.getString("skullowner");
        if (skull != null && skull.equals("%player_name%")) {
            return new PlayerProfile(section, fillItem);
        }
        return new NavBarItem(section, fillItem);
    }

    public Inventory addShopsNavBar(Inventory inv, Player p, String section, int page, int allpages) {
        if (this.shopNavBars.containsKey(section)) {
            return this.shopNavBars.get(section).load(inv, p, section, page, allpages);
        }
        return this.shopsNavBar.load(inv, p, section, page, allpages);
    }

    public Inventory updateShopsNavBar(Inventory inv, Player p, String section, int page, int allpages) {
        if (this.shopNavBars.containsKey(section)) {
            return this.shopNavBars.get(section).update(inv, p, section, page, allpages);
        }
        return this.shopsNavBar.update(inv, p, section, page, allpages);
    }

    public Inventory addMainMenuNavBar(Inventory inv, Player p) {
        return this.mainMenuNavBar.load(inv, p, null);
    }

    public ItemStack[] getMainMenuNavBar(Player p) {
        return this.mainMenuNavBar.get(p);
    }

    public String[] getMainMenuNavBarTextures() {
        return this.mainMenuNavBar.getBedrockTextures();
    }

    public Inventory addTransactionNavBar(Inventory inv, Player p, EcoType type) {
        return this.transactionNavBar.load(inv, p, type);
    }

    public Inventory addShopStandsNavBar(Inventory inv, Player p, EcoType type) {
        return this.shopStandsNavBar.load(inv, p, type);
    }

    public Inventory addSellGUINavBar(Inventory inv, Player p) {
        return this.sellGUINavBar.load(inv, p, null);
    }

    public void createCustomPlayerProfile() {
        if (!ConfigManager.getConfig().getBoolean("custom-bedrock-player-profile.enabled", true)) {
            this.customPlayerProfile = null;
            return;
        }
        ConfigurationSection section = ConfigManager.getConfig().getConfigurationSection("custom-bedrock-player-profile.item");
        if (section == null || section.getKeys(false).isEmpty()) {
            this.customPlayerProfile = null;
            return;
        }
        LocalizedItemStack item = this.createFillItem(null, section);
        if (item == null) {
            this.customPlayerProfile = null;
            return;
        }
        HashMap<Locale, ItemStack> variants = new HashMap<Locale, ItemStack>();
        for (Map.Entry<Locale, ItemStack> entry : item.getVariants().entrySet()) {
            variants.put(entry.getKey(), this.translateItem(entry.getValue(), entry.getKey()));
        }
        this.customPlayerProfile = new LocalizedItemStack(variants);
    }

    private ItemStack translateItem(ItemStack item, Locale locale) {
        if (!item.hasItemMeta()) {
            return item;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta.hasDisplayName()) {
            this.plugin.getMetaUtils().setDisplayName(meta, this.replaceTranslations(meta.getDisplayName(), locale));
        }
        if (meta.hasLore()) {
            this.plugin.getMetaUtils().setLore(meta, meta.getLore().stream().map(s -> this.replaceTranslations((String)s, locale)).collect(Collectors.toList()));
        }
        item.setItemMeta(meta);
        return item;
    }

    private Translatable replaceTranslations(String s, Locale locale) {
        return Lang.fromConfig(s.replace("%translations-previous-page%", Lang.PREVIOUS_PAGE.get(locale).toString()).replace("%translations-current-page%", Lang.CURRENT_PAGE.get(locale).getLegacy().contains("%page%") ? Lang.CURRENT_PAGE.get(locale).toString() : Lang.CURRENT_PAGE.get(locale).builder().append(" %page%/%pages%").build().toString()).replace("%translations-next-page%", Lang.NEXT_PAGE.get(locale).toString()).replace("%translations-money%", Lang.MONEY.get(locale).toString()).replace("%translations-cancel%", Lang.CANCEL.get(locale).toString()).replace("%translations-back%", Lang.BACK.get(locale).toString()));
    }

    private class InventoryNavBar {
        private boolean enabled;
        private final ItemStack fillItem;
        private final Set<Integer> fillItems = new HashSet<Integer>();
        private final Map<Integer, NavItem> items = new HashMap<Integer, NavItem>();

        public NavItem get(int i) {
            return this.items.get(i);
        }

        public InventoryNavBar(String shop) {
            this.enabled = !ConfigManager.getSection(shop).getString("nav-bar.mode", "INHERIT").equalsIgnoreCase("DISABLED");
            LocalizedItemStack fillItem = ConfigManager.getSection(shop).contains("nav-bar.fill-item") ? ((NavBar)NavBar.this).plugin.createItem.createFillItem(shop, ConfigManager.getSection(shop).getConfigurationSection("nav-bar.fill-item")) : null;
            this.fillItem = fillItem == null ? null : fillItem.getItem();
            ConfigurationSection section = ConfigManager.getSection(shop).getConfigurationSection("nav-bar.items");
            if (section == null || section.getKeys(false).isEmpty()) {
                this.enabled = false;
                return;
            }
            int i = 1;
            for (String item : section.getKeys(false)) {
                int slot;
                NavItem nbi;
                block12: {
                    nbi = NavBar.this.getShopItem(shop, "nav-bar.items." + item, this.fillItem);
                    if (nbi == null) {
                        nbi = NavBar.this.shopsNavBar.get(i);
                    }
                    slot = 0;
                    try {
                        if (ConfigManager.getSection(shop).contains("nav-bar.items." + item + ".slot")) {
                            slot = ConfigManager.getSection(shop).getInt("nav-bar.items." + item + ".slot");
                            break block12;
                        }
                        try {
                            slot = Integer.parseInt(item);
                            while (this.items.containsKey(slot - 1)) {
                                ++slot;
                            }
                        }
                        catch (NumberFormatException e) {
                            slot = i;
                            while (this.items.containsKey(slot - 1)) {
                                ++slot;
                            }
                        }
                    }
                    catch (NumberFormatException e) {
                        SendMessage.warnMessage("Cannot create navigation bar item for shop '" + shop + "' at 'nav-bar.items." + item + "' because the slot is not a valid integer. Got '" + ConfigManager.getSection(shop).getInt("nav-bar.items." + item + ".slot") + "'");
                    }
                }
                if (slot > 9 || slot < 1) {
                    SendMessage.warnMessage("Cannot create navigation bar item for shop '" + shop + "' at 'nav-bar.items." + item + "' because the slot is out of bounds for " + slot + ". Valid values should be in range of 1-9");
                    continue;
                }
                this.items.put(slot - 1, nbi);
                ++i;
            }
            if (this.items.isEmpty()) {
                this.enabled = false;
            }
        }

        public InventoryNavBar(ConfigurationSection section) {
            this.enabled = section.getBoolean("enabled", true);
            LocalizedItemStack fillItem = section.contains("fill-item") ? ((NavBar)NavBar.this).plugin.createItem.createFillItem(null, section.getConfigurationSection("fill-item")) : null;
            this.fillItem = fillItem == null ? null : fillItem.getItem();
            section = section.getConfigurationSection("items");
            if (section == null || section.getKeys(false).isEmpty()) {
                this.enabled = false;
                return;
            }
            int i = 1;
            for (String item : section.getKeys(false)) {
                try {
                    int slot;
                    NavItem nbi;
                    block13: {
                        nbi = NavBar.this.getDefaultItem(section.getCurrentPath() + "." + item, this.fillItem);
                        slot = 0;
                        try {
                            if (section.contains(item + ".slot")) {
                                slot = section.getInt(item + ".slot");
                                break block13;
                            }
                            try {
                                slot = Integer.parseInt(item);
                                while (this.items.containsKey(slot - 1)) {
                                    ++slot;
                                }
                            }
                            catch (NumberFormatException e) {
                                slot = i;
                                while (this.items.containsKey(slot - 1)) {
                                    ++slot;
                                }
                            }
                        }
                        catch (NumberFormatException e) {
                            SendMessage.warnMessage("Cannot create navigation bar item at '" + section.getCurrentPath() + "." + item + "' because the slot is not a valid integer. Got '" + section.getString(item + ".slot") + "'");
                        }
                    }
                    if (slot > 9 || slot < 1) {
                        SendMessage.warnMessage("Cannot create navigation bar item at '" + section.getCurrentPath() + "." + item + "' because the slot is out of bounds for " + slot + ". Valid values should be in range of 1-9");
                        continue;
                    }
                    this.items.put(slot - 1, nbi);
                    ++i;
                }
                catch (ItemLoadException itemLoadException) {}
            }
            if (this.items.isEmpty()) {
                this.enabled = false;
            }
        }

        public Inventory load(Inventory inv, Player p, String section, int page, int allpages) {
            if (!this.enabled) {
                return inv;
            }
            int size = inv.getSize();
            for (int i = 0; i < 9; ++i) {
                if (this.items.containsKey(i)) {
                    inv.setItem(size - 9 + i, this.items.get(i).getItem(p, NavBar.this.plugin.getEcoHandler().getEcon(section).getType(), page, allpages));
                    continue;
                }
                if (this.fillItem != null) {
                    inv.setItem(size - 9 + i, this.fillItem);
                    this.fillItems.add(i);
                    continue;
                }
                inv.setItem(size - 9 + i, new ItemStack(Material.AIR));
            }
            return inv;
        }

        public ItemStack[] get(Player p, String section, int page, int allpages) {
            if (!this.enabled) {
                return null;
            }
            ItemStack[] row = new ItemStack[9];
            for (Map.Entry<Integer, NavItem> e : this.items.entrySet()) {
                row[e.getKey().intValue()] = e.getValue().getItem(p, NavBar.this.plugin.getEcoHandler().getEcon(section).getType(), page, allpages);
            }
            if (this.fillItem != null) {
                for (int i = 0; i < 9; ++i) {
                    if (this.items.containsKey(i)) continue;
                    row[i] = this.fillItem;
                }
            }
            return row;
        }

        public String[] getBedrockTextures(int page, int allpages) {
            if (!this.enabled) {
                return null;
            }
            String[] row = new String[9];
            for (Map.Entry<Integer, NavItem> e : this.items.entrySet()) {
                row[e.getKey().intValue()] = e.getValue().getBedrockTexture(page, allpages);
            }
            return row;
        }

        public ItemStack[] get(Player p) {
            if (!this.enabled) {
                return null;
            }
            ItemStack[] row = new ItemStack[9];
            for (Map.Entry<Integer, NavItem> e : this.items.entrySet()) {
                row[e.getKey().intValue()] = e.getValue().getItem(p, null);
            }
            if (this.fillItem != null) {
                for (int i = 0; i < 9; ++i) {
                    if (this.items.containsKey(i)) continue;
                    row[i] = this.fillItem;
                }
            }
            return row;
        }

        public String[] getBedrockTextures() {
            if (!this.enabled) {
                return null;
            }
            String[] row = new String[9];
            for (Map.Entry<Integer, NavItem> e : this.items.entrySet()) {
                row[e.getKey().intValue()] = e.getValue().getBedrockTexture();
            }
            return row;
        }

        public Inventory update(Inventory inv, Player p, String section, int page, int allpages) {
            if (!this.enabled) {
                return inv;
            }
            int size = inv.getSize();
            for (int i = 0; i < 9; ++i) {
                if (this.items.containsKey(i)) {
                    NavItem item = this.items.get(i);
                    if (item.isStatic(page, allpages)) continue;
                    inv.setItem(size - 9 + i, item.getItem(p, NavBar.this.plugin.getEcoHandler().getEcon(section).getType(), page, allpages));
                    continue;
                }
                if (this.fillItem != null) {
                    if (this.fillItems.contains(i)) continue;
                    inv.setItem(size - 9 + i, this.fillItem);
                    continue;
                }
                inv.setItem(size - 9 + i, new ItemStack(Material.AIR));
            }
            return inv;
        }

        public Inventory load(Inventory inv, Player p, EcoType type) {
            if (!this.enabled) {
                return inv;
            }
            for (Map.Entry<Integer, NavItem> e : this.items.entrySet()) {
                NavItem item = e.getValue();
                if (item.isDisabled()) continue;
                inv.setItem(inv.getSize() - 9 + e.getKey(), item.getItem(p, type));
            }
            if (this.fillItem != null) {
                for (int i = 0; i < 9; ++i) {
                    if (this.items.containsKey(i)) continue;
                    inv.setItem(inv.getSize() - 9 + i, this.fillItem);
                }
            }
            return inv;
        }
    }
}

