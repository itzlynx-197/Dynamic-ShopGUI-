

package com.siliconstudios.dynamicshopguiplus.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.files.Translatable;
import com.siliconstudios.dynamicshopguiplus.methodes.CalculateAmount;
import com.siliconstudios.dynamicshopguiplus.methodes.CreateItem;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.objects.ActionItem;
import com.siliconstudios.dynamicshopguiplus.objects.DisplayItem;
import com.siliconstudios.dynamicshopguiplus.objects.ShopItem;
import com.siliconstudios.dynamicshopguiplus.objects.ShopPage;
import com.siliconstudios.dynamicshopguiplus.objects.ShopPageItems;
import com.siliconstudios.dynamicshopguiplus.objects.ShopTitle;
import com.siliconstudios.dynamicshopguiplus.objects.User;
import com.siliconstudios.dynamicshopguiplus.objects.mappings.ClickAction;
import com.siliconstudios.dynamicshopguiplus.objects.mappings.ClickMappings;
import com.siliconstudios.dynamicshopguiplus.objects.shops.ShopType;
import com.siliconstudios.dynamicshopguiplus.providers.UserManager;
import com.siliconstudios.dynamicshopguiplus.util.InventoryUtil;
import com.siliconstudios.dynamicshopguiplus.util.ItemLayout;
import com.siliconstudios.dynamicshopguiplus.util.LocalizedItemStack;
import com.siliconstudios.dynamicshopguiplus.util.PermissionsCache;
import com.siliconstudios.dynamicshopguiplus.util.layouts.PageLayout;
import com.siliconstudios.dynamicshopguiplus.util.layouts.ReservedLayout;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class ShopSection
extends CreateItem
implements com.siliconstudios.dynamicshopguiplus.objects.shops.ShopSection {
    private final String section;
    private final ShopTitle title;
    private final boolean staticTitle;
    private final boolean subSection;
    private final boolean navBar;
    private LocalizedItemStack fillItem;
    private final List<String> pages;
    private final Map<Integer, ShopTitle> pageTitles = new HashMap<Integer, ShopTitle>();
    private final Map<Integer, ShopPageItems> shopPages = new HashMap<Integer, ShopPageItems>();
    private final Map<String, DisplayItem> displayItems = new HashMap<String, DisplayItem>();
    private final ClickMappings mappings;
    private final DynamicShopGUIPlus plugin;
    private final boolean nms;
    public final List<String> itemLocs;

    public ShopSection(DynamicShopGUIPlus plugin, String section) throws Exception {
        super(plugin);
        this.nms = DynamicShopGUIPlus.getInstance().versionHandler != null && DynamicShopGUIPlus.getInstance().version >= 113 && ConfigManager.getConfig().getBoolean("use-nms", true) && Bukkit.getPluginManager().getPlugin("EcoEnchants") == null;
        this.itemLocs = new ArrayList<String>();
        this.plugin = plugin;
        this.section = section;
        this.title = new ShopTitle(section);
        this.navBar = this.plugin.navBar.isEnabled(this.section);
        this.itemLocs.addAll(this.plugin.getConfigManager().getItemsRaw(this.section));
        this.subSection = ConfigManager.getSection(section).getBoolean("sub-section");
        this.pages = this.plugin.getConfigManager().getPages(this.section, false);
        this.mappings = ConfigManager.getSection(this.section).contains("click-mappings") ? new ClickMappings(section) : plugin.getClickMappings();
        this.loadShopItems();
        this.staticTitle = this.isStaticTitle();
    }

    @Override
    public Collection<DisplayItem> getDisplayItems() {
        return this.displayItems.values();
    }

    @Override
    public DisplayItem getDisplayItem(String itemPath) {
        return this.displayItems.get(itemPath);
    }

    @Override
    public boolean isActionItem(String section, String itemLoc) {
        return this.displayItems.get(section + "." + itemLoc) instanceof ActionItem;
    }

    @Override
    public Collection<ShopItem> getShopItems() {
        return this.displayItems.values().stream().filter(i -> ShopItem.class.equals(i.getClass())).map(ShopItem.class::cast).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ShopItem getShopItem(String itemPath) {
        DisplayItem item = this.displayItems.get(itemPath);
        return item instanceof ShopItem ? (ShopItem)item : null;
    }

    @Override
    public String getSection() {
        return this.section;
    }

    @Override
    public ShopPageItems getShopPageItems(int page) {
        return this.shopPages.get(page);
    }

    public List<ShopPageItems> getShopPageItems() {
        return new ArrayList<ShopPageItems>(this.shopPages.values());
    }

    @Override
    public int getPages() {
        return this.pages.size();
    }

    public Set<String> getValidItemPaths() {
        return this.displayItems.keySet();
    }

    @Override
    public boolean isSubSection() {
        return this.subSection;
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public boolean isCloseMenu() {
        return false;
    }

    @Override
    public ShopType getType() {
        return ShopType.CATEGORY;
    }

    @Override
    public List<String> getItemLocs() {
        return this.itemLocs;
    }

    @Override
    public List<String> getShopItemLocs() {
        return this.displayItems.values().stream().filter(i -> ShopItem.class.equals(i.getClass())).map(DisplayItem::itemLoc).collect(Collectors.toList());
    }

    @Override
    public ClickAction getClickAction(ClickType clickType) {
        return this.mappings.getAction(clickType);
    }

    @Override
    public int getPageForShopItem(String location) {
        return this.pages.indexOf(location.split("\\.")[0]) + 1;
    }

    private void loadShopItems() {
        try {
            for (String itemLoc : this.itemLocs) {
                this.displayItems.put(this.section + "." + itemLoc, ConfigManager.getShop(this.section).contains("pages." + itemLoc + ".action") ? new ActionItem(this.section, itemLoc) : new ShopItem(this.section, itemLoc));
            }
            if (!this.pages.isEmpty()) {
                this.loadPages();
            } else {
                SendMessage.errorMessage("No pages found for shop " + this.section + ", adding empty page...");
                this.shopPages.put(1, new ShopPageItems(this.section));
                this.pageTitles.put(1, this.title);
            }
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
        if (ConfigManager.getSection(this.section).get("fill-item") != null) {
            this.loadFillItem();
        }
    }

    private void loadFillItem() {
        this.fillItem = this.plugin.createItem.createFillItem(this.section, ConfigManager.getSection(this.section).getConfigurationSection("fill-item"));
    }

    private void loadPages() {
        try {
            int pageN = 1;
            for (String page : this.pages) {
                Integer size = this.getPageSize(page);
                int defSize = this.plugin.navBar.isEnabled(this.section) ? (size == null ? 45 : size) : (size == null ? 54 : size);
                ItemLayout layout = this.getItemLayout(page, defSize);
                List<String> displayItems = this.plugin.calculateAmount.loadItemsInOrder(layout == null ? new PageLayout(this.section) : layout, this.section, page, size, defSize);
                if (size == null) {
                    size = displayItems.size() <= 45 && !displayItems.isEmpty() ? CalculateAmount.getSlots(displayItems.size()) : 54;
                }
                ArrayList<String> itemLocs = new ArrayList<String>();
                LinkedHashMap<Integer, ItemStack> items = new LinkedHashMap<Integer, ItemStack>();
                for (int i = 0; i < displayItems.size(); ++i) {
                    String itemLoc = displayItems.get(i);
                    if (itemLoc != null) {
                        DisplayItem displayItem = this.displayItems.get(this.section + "." + itemLoc);
                        if (displayItem != null) {
                            items.put(i, displayItem.getShopItem());
                        }
                    }
                    itemLocs.add(itemLoc);
                }
                this.shopPages.put(pageN, new ShopPageItems(items, itemLocs, this.section, size));
                this.pageTitles.put(pageN, this.getPageTitle(page));
                ++pageN;
            }
        }
        catch (Exception e) {
            SendMessage.warnMessage("Error occurred while loading shop items for section " + this.section);
            e.printStackTrace();
        }
    }

    private ItemLayout getItemLayout(String page, int size) {
        List layout = ConfigManager.getSection(this.section).getStringList("item-layout");
        if (layout.isEmpty()) {
            return null;
        }
        if (layout.size() < size / 9) {
            SendMessage.errorMessage("Invalid item layout for " + this.section + " page '" + page + "': Expected " + size / 9 + " rows of values, but got " + layout.size() + " instead");
            return null;
        }
        try {
            ArrayList itemLayout = new ArrayList();
            for (int row = 0; row < size / 9; ++row) {
                String[] items = ((String)layout.get(row)).split(" ");
                if (items.length != 9) {
                    SendMessage.errorMessage("Invalid item layout for " + this.section + " page '" + page + "': Row " + (row + 1) + " contains less or more symbols, expected 9");
                    return null;
                }
                if (Arrays.stream(items).anyMatch(i -> !i.equals("0") && !i.equals("1"))) {
                    SendMessage.errorMessage("Invalid item layout for " + this.section + " page '" + page + "': Items from row " + (row + 1) + " does not match expected values, expected 0 or 1");
                    return null;
                }
                Arrays.stream(items).forEach(i -> itemLayout.add(i.equals("1")));
            }
            ReservedLayout reserved = new ReservedLayout(this.section);
            for (int row = 0; row < size / 9; ++row) {
                for (int slot = 0; slot < 9; ++slot) {
                    int index = row * 9 + slot;
                    if (!((Boolean)itemLayout.get(index)).booleanValue()) continue;
                    reserved.reserve(index);
                }
            }
            return reserved;
        }
        catch (IndexOutOfBoundsException e) {
            SendMessage.errorMessage("Unexpected error occurred while loading item layout for " + this.section + " page '" + page + "'");
            e.printStackTrace();
            return null;
        }
    }

    private Integer getPageSize(String page) {
        if (!ConfigManager.getShop(this.section).contains("pages." + page + ".gui-rows")) {
            return null;
        }
        try {
            int size = Integer.parseInt(ConfigManager.getShop(this.section).getString("pages." + page + ".gui-rows"));
            if (size >= 1 && size <= 6) {
                return size * 9;
            }
            SendMessage.warnMessage("Inventory size for page '" + page + "' from shop '" + this.section + "' has to be in range from 1-6, using default...");
        }
        catch (NumberFormatException e) {
            SendMessage.warnMessage("Invalid inventory size for page '" + page + "' from shop '" + this.section + "', using default...");
        }
        return null;
    }

    private ShopTitle getPageTitle(String page) {
        Translatable bedrockTitle = null;
        if (ConfigManager.getShop(this.section).contains("pages." + page + ".bedrock-title")) {
            bedrockTitle = Lang.fromConfig(this.plugin.getInvTitle(ConfigManager.getShop(this.section).getString("pages." + page + ".bedrock-title")));
        }
        if (ConfigManager.getShop(this.section).contains("pages." + page + ".title")) {
            String title = ConfigManager.getShop(this.section).getString("pages." + page + ".title");
            return new ShopTitle(Lang.fromConfig(this.plugin.getInvTitle(title)), bedrockTitle);
        }
        if (bedrockTitle != null) {
            return new ShopTitle(this.title.getTitle(), bedrockTitle);
        }
        return this.title;
    }

    private boolean isStaticTitle() {
        String def = this.title.getRaw();
        for (ShopTitle title : this.pageTitles.values()) {
            if (title.getRaw().equals(def) && title.isStatic()) continue;
            return false;
        }
        return true;
    }

    @Override
    public void openShopSection(Player p, boolean disabledBackButton) {
        this.openShopSection(p, 1, disabledBackButton);
    }

    @Override
    public void openShopSection(Player p, boolean disabledBackButton, String rootSection) {
        this.openShopSection(p, 1, disabledBackButton, rootSection);
    }

    @Override
    public void openShopSection(Player p, int page, boolean disabledBackButton) {
        this.openShopSection(p, page, disabledBackButton, null);
    }

    @Override
    public void openShopSection(Player p, int page, boolean disabledBackButton, @Nullable String rootSection) {
        ShopPage currentPage;
        Map<Integer, ItemStack> items = this.shopPages.get(page).getItems();
        User user = UserManager.getUser(p);
        boolean itemIndex = PermissionsCache.hasPermission(p, "DynamicShopGUIPlus.itemindexes");
        boolean discounts = this.plugin.discountsActive && this.plugin.hasDiscount(this.section);
        boolean multipliers = this.plugin.multipliers && this.plugin.hasMultiplier(this.section);
        this.updateLore(items, p, user.isBedrock() || user.isPr(), itemIndex, discounts, multipliers, page);
        if (this.plugin.bedrock && this.plugin.bedrockMenus && user.isBedrock() && this.plugin.getBedrockInventorys().isEnabledFor(p)) {
            this.plugin.getBedrockInventorys().openShopSection(p, this, page, disabledBackButton, rootSection, items, this.shopPages.get(page).getItemOrder(), this.fillItem == null ? null : this.fillItem.getItem(p), this.pageTitles.get(page).getTitle(p, page, this.pages.size()), this.shopPages.get(page).getSize(), this.navBar);
            return;
        }
        int size = this.plugin.resizeGUI && user.isBedrock() ? 54 : this.shopPages.get(page).getSize();
        InventoryHolder holder = InventoryUtil.getTopHolder(p);
        if (holder instanceof ShopPage && this.section.equals((currentPage = (ShopPage)holder).getSection()) && size == currentPage.getSize()) {
            currentPage.updateContents(p, items, this.fillItem == null ? null : this.fillItem.getItem(p), page, this.navBar);
            if (!this.staticTitle) {
                this.plugin.versionHandler.updateInventoryTitle(p, this.pageTitles.get(page).getTitle(p, page, this.pages.size()));
            }
            return;
        }
        user.setOpenNewGUI(true);
        p.openInventory(new ShopPage(items, this.fillItem == null ? null : this.fillItem.getItem(p), p, this.pageTitles.get(page).getTitle(p, page, this.pages.size()), this.section, page, this.pages.size(), disabledBackButton, this.navBar, size, rootSection).getInventory());
    }

    @Override
    public void reloadItem(String itemPath) {
    }

    private void updateLore(Map<Integer, ItemStack> items, Player p, boolean pr, boolean itemIndex, boolean discounts, boolean multipliers, int page) {
        items.replaceAll((slot, item) -> {
            if (item == null || item.getType() == Material.AIR) {
                return item;
            }
            DisplayItem displayItem = this.displayItems.get(this.section + "." + this.shopPages.get(page).getItemOrder().get((int)slot));
            if (displayItem instanceof ShopItem) {
                return this.updateLore((ShopItem)displayItem, new ItemStack(item), p, pr, discounts, multipliers, itemIndex, this.plugin.seasonalLore && ((ShopItem)displayItem).hasSeasonModifier(p.getWorld().getName()), page);
            }
            return this.updateLore((ActionItem)displayItem, (ItemStack)item, p, pr, itemIndex, page);
        });
    }

    private ItemStack updateLore(ShopItem shopItem, ItemStack item, Player p, boolean pr, boolean discounts, boolean multipliers, boolean itemIndex, boolean seasonal, int page) {
        try {
            return this.plugin.getLoreFormatter().format(shopItem, item, p, this.section, this.nms && !shopItem.rgb, pr, discounts, multipliers, itemIndex, seasonal, page, this.getPages());
        }
        catch (Exception e) {
            SendMessage.warnMessage("Error occurred while updating shop lore of item '" + shopItem.getItemPath() + "'");
            e.printStackTrace();
            return item;
        }
    }

    private ItemStack updateLore(ActionItem actionItem, ItemStack item, Player p, boolean pr, boolean itemIndex, int page) {
        try {
            return this.plugin.getLoreFormatter().format(actionItem, item, p, this.plugin.getEcoHandler().getEcon(this.section).getType(), this.section, this.nms, pr, itemIndex, page, this.getPages());
        }
        catch (Exception e) {
            SendMessage.logDebugMessage("Error occurred while updating shop lore of action item '" + actionItem.getItemPath() + "'");
            e.printStackTrace();
            return item;
        }
    }

    @Override
    public ItemStack updateItem(Player p, ShopItem shopItem) {
        ItemStack item = new ItemStack(shopItem.getShopItem());
        boolean nbt = this.nms && !UserManager.getUser(p).isBedrock() && !UserManager.getUser(p).isPr();
        boolean itemIndex = PermissionsCache.hasPermission(p, "DynamicShopGUIPlus.itemindexes");
        boolean discounts = this.plugin.discountsActive && this.plugin.hasDiscount(this.section);
        boolean multipliers = this.plugin.multipliers && this.plugin.hasMultiplier(this.section);
        return this.updateLore(shopItem, item, p, nbt, discounts, multipliers, itemIndex, this.plugin.seasonalLore && shopItem.hasSeasonModifier(p.getWorld().getName()), this.getPageForShopItem(shopItem.itemLoc()));
    }
}

