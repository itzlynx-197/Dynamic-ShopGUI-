

package com.siliconstudios.dynamicshopguiplus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.siliconstudios.dynamicshopguiplus.VersionHandler;
import com.siliconstudios.dynamicshopguiplus.api.DynamicShopGUIPlusHook;
import com.siliconstudios.dynamicshopguiplus.commands.SellGUI;
import com.siliconstudios.dynamicshopguiplus.commands.Sellall;
import com.siliconstudios.dynamicshopguiplus.commands.Shop;
import com.siliconstudios.dynamicshopguiplus.commands.ShopGive;
import com.siliconstudios.dynamicshopguiplus.commands.Sreload;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.CommandManager;
import com.siliconstudios.dynamicshopguiplus.events.JoinEvent;
import com.siliconstudios.dynamicshopguiplus.events.LevelEvent;
import com.siliconstudios.dynamicshopguiplus.events.LocaleChangeEvent;
import com.siliconstudios.dynamicshopguiplus.events.MenuHandler;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.files.TransactionLog;
import com.siliconstudios.dynamicshopguiplus.files.Translatable;
import com.siliconstudios.dynamicshopguiplus.methodes.CalculateAmount;
import com.siliconstudios.dynamicshopguiplus.methodes.CreateItem;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.methodes.StartupReload;
import com.siliconstudios.dynamicshopguiplus.methodes.UpdateChecker;
import com.siliconstudios.dynamicshopguiplus.objects.DisplayItem;
import com.siliconstudios.dynamicshopguiplus.objects.LoreFormatter;
import com.siliconstudios.dynamicshopguiplus.objects.SectionItem;
import com.siliconstudios.dynamicshopguiplus.objects.ShopInventory;
import com.siliconstudios.dynamicshopguiplus.objects.ShopItem;
import com.siliconstudios.dynamicshopguiplus.objects.bedrock.BedrockInventorys;
import com.siliconstudios.dynamicshopguiplus.objects.mappings.ClickMappings;
import com.siliconstudios.dynamicshopguiplus.objects.navbar.NavBar;
import com.siliconstudios.dynamicshopguiplus.objects.shops.ShopSection;
import com.siliconstudios.dynamicshopguiplus.providers.EconomyProvider;
import com.siliconstudios.dynamicshopguiplus.providers.EnchantCostProvider;
import com.siliconstudios.dynamicshopguiplus.providers.GeyserModule;
import com.siliconstudios.dynamicshopguiplus.providers.ModifierManager;
import com.siliconstudios.dynamicshopguiplus.providers.RequirementManager;
import com.siliconstudios.dynamicshopguiplus.providers.SpawnerManager;
import com.siliconstudios.dynamicshopguiplus.providers.StandProvider;
import com.siliconstudios.dynamicshopguiplus.providers.UserManager;
import com.siliconstudios.dynamicshopguiplus.providers.placeholders.PAPIExpansion;
import com.siliconstudios.dynamicshopguiplus.util.ChatUtil;
import com.siliconstudios.dynamicshopguiplus.util.ConfigUtil;
import com.siliconstudios.dynamicshopguiplus.util.EcoType;
import com.siliconstudios.dynamicshopguiplus.util.EconomyHandler;
import com.siliconstudios.dynamicshopguiplus.util.EconomyType;
import com.siliconstudios.dynamicshopguiplus.util.InventoryUtil;
import com.siliconstudios.dynamicshopguiplus.util.MetaUtils;
import com.siliconstudios.dynamicshopguiplus.util.ServerInfo;
import com.siliconstudios.dynamicshopguiplus.util.SkullUtil;
import com.siliconstudios.dynamicshopguiplus.util.XMaterial;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.ConfigLoadException;
import com.siliconstudios.dynamicshopguiplus.util.meta.AdventureMeta;
import com.siliconstudios.dynamicshopguiplus.util.meta.BukkitMeta;
import com.siliconstudios.dynamicshopguiplus.util.meta.PaperMeta;
import com.siliconstudios.dynamicshopguiplus.util.scheduler.ScheduledTask;
import com.siliconstudios.dynamicshopguiplus.util.scheduler.ServerScheduler;
import com.siliconstudios.dynamicshopguiplus.util.scheduler.schedulers.BukkitScheduler;
import com.siliconstudios.dynamicshopguiplus.util.scheduler.schedulers.FoliaScheduler;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class DynamicShopGUIPlus
extends JavaPlugin {
    public final int version = ServerInfo.getVersion().getVersion();
    public final boolean spigot = this.isSpigotServer();
    private Map<String, SectionItem> sectionItems = new HashMap<String, SectionItem>();
    private Map<Integer, String> mainMenuItemSlots = new HashMap<Integer, String>();
    private Map<String, ShopSection> sections = new HashMap<String, ShopSection>();
    private List<String> shopSections = new ArrayList<String>();
    private Map<String, Translatable> sectionTitles = new HashMap<String, Translatable>();
    private Map<String, Translatable> bedrockSectionTitles = new HashMap<String, Translatable>();
    public final List<String> supportedMatNames = this.setSupportedMatNames();
    public Map<String, List<ShopItem>> shopItemsByMaterialName;
    public List<String> ignoredNBTData;
    private final Map<String, HashMap<String, Double>> sellMultipliers = new HashMap<String, HashMap<String, Double>>();
    private final Map<String, HashMap<String, Double>> discounts = new HashMap<String, HashMap<String, Double>>();
    public List<GameMode> bannedGamemodes = new ArrayList<GameMode>();
    public Map<EntityType, Translatable> spawnerNames = new HashMap<EntityType, Translatable>();
    private final EconomyHandler ecoHandler = new EconomyHandler(this);
    public Permission permissions;
    public boolean boughtItemsLore;
    public boolean dropItemsOnGround;
    public boolean allowIllegalStacks;
    public int maxShopSize;
    public int mainMenuSize;
    public boolean shopStands;
    public boolean allowUnsafeEnchants;
    public boolean allowEnchanted;
    public boolean discountsActive;
    public boolean multipliers;
    public boolean useItemName;
    public boolean bedrock;
    public boolean resizeGUI;
    public boolean bedrockMenus;
    public boolean prefixSuffix = true;
    public boolean prioritizeItemLore;
    public boolean sellShulkers;
    public boolean MMB;
    public boolean seasonalLore;
    public boolean matchMeta;
    public boolean paperMeta;
    public boolean perPlayerLanguages;
    public String badYMLParse = null;
    private final Pattern rgbPattern = Pattern.compile("#[a-fA-F0-9]{6}");
    private static DynamicShopGUIPlus instance;
    public VersionHandler versionHandler;
    public StartupReload startupReload = new StartupReload(this);
    private SendMessage sendMessage = new SendMessage(this);
    public CalculateAmount calculateAmount = new CalculateAmount(this);
    private final EnchantCostProvider enchantCostManager = new EnchantCostProvider(this);
    public CreateItem createItem = new CreateItem(this);
    public NavBar navBar = new NavBar(this);
    private JoinEvent joinEvent;
    private MenuHandler menuHandler;
    public UpdateChecker updateChecker;
    private final DynamicShopGUIPlusHook api = new DynamicShopGUIPlusHook(this);
    private final TransactionLog transactionLog = new TransactionLog(this);
    private final UserManager userManager = new UserManager(this);
    private final ConfigManager configManager = new ConfigManager(this);
    private final ServerScheduler scheduler = this.getScheduler();
    private final RequirementManager requirementManager = new RequirementManager(this);
    private StandProvider standProvider;
    private BedrockInventorys bedrockInventorys;
    private GeyserModule geyserModule;
    private PAPIExpansion papiExpansion;
    private LoreFormatter loreFormatter;
    private ModifierManager modifierManager;
    private SkullUtil skullUtil;
    private MetaUtils metaUtils;
    private ClickMappings mappings;
    private Chat chat;
    private final SpawnerManager spawnerManager = new SpawnerManager(this);

    public static DynamicShopGUIPlus getInstance() {
        return instance;
    }

    public void onEnable() {
        long start = System.currentTimeMillis();
        instance = this;
        this.loadMetaUtils();
        this.configManager.loadConfig();
        Lang.CONFIGS_RELOADED.reload();
        this.metaUtils.update();
        this.configManager.init();
        this.getConfigManager().validate();
        this.enableGeyserExtension();
        this.enableBedrockMenus();
        if (this.badYMLParse == null) {
            if (!this.ecoHandler.registerPaymentSolutions()) {
                this.getServer().getPluginManager().disablePlugin((Plugin)this);
                return;
            }
            ConfigUtil.updateConfigs();
            this.mappings = new ClickMappings(null);
            this.startupReload.setupPluginVersion();
            this.spawnerManager.init();
            this.setSkullUtil(new SkullUtil(this));
            this.userManager.init();
            this.setModifierManager(new ModifierManager(this));
            this.setupPlaceholderExpansion();
            this.startupReload.checkDebugMode();
            this.startupReload.loadInventoryTitles();
            this.startupReload.loadItems();
            if (ConfigManager.getConfig().getBoolean("update-checking", true)) {
                this.updateChecker = new UpdateChecker();
            }
            SendMessage.infoMessage(Lang.DONE.get().replace("%millis%", String.valueOf(System.currentTimeMillis() - start)));
        } else {
            SendMessage.errorMessage("Skipping all settings and item loading because the configuration files could not be loaded...");
        }
        this.registerPermissions();
        this.registerEvents();
        this.registerCommands();
        this.reloadPlayerData();
    }

    public void onDisable() {
        this.closeOpenGUIS();
        this.transactionLog.saveUnsaved();
        if (this.skullUtil != null) {
            this.skullUtil.saveCache();
        }
        if (this.standProvider != null) {
            this.standProvider.stop();
        }
        this.sections.clear();
        this.supportedMatNames.clear();
        this.discounts.clear();
        this.spawnerNames.clear();
        Lang.DISABLING_PLUGIN.clearMessages();
        Bukkit.getPluginManager().disablePlugin((Plugin)instance);
        instance = null;
    }

    public MenuHandler getMenuHandler() {
        return this.menuHandler;
    }

    public ShopItem getShopItem(String itemPath) {
        return this.sections.get(itemPath.split("\\.")[0]).getShopItem(itemPath);
    }

    public DisplayItem getDisplayItem(String itemPath) {
        return this.sections.get(itemPath.split("\\.")[0]).getDisplayItem(itemPath);
    }

    public DisplayItem getDisplayItem(String section, String itemLoc) {
        return this.sections.get(section).getDisplayItem(section + "." + itemLoc);
    }

    public TransactionLog getTransactionLog() {
        return this.transactionLog;
    }

    public Map<Integer, String> getMainMenuItemSlots() {
        return this.mainMenuItemSlots;
    }

    public String getMainMenuSectionForSlot(int slot) {
        return this.mainMenuItemSlots.get(slot);
    }

    public void setMainMenuItemSlots(Map<Integer, String> newMainMenuItemSlots) {
        this.mainMenuItemSlots = newMainMenuItemSlots;
    }

    public Map<String, SectionItem> getSectionItems() {
        return this.sectionItems;
    }

    public void setSectionItems(Map<String, SectionItem> newSectionItems) {
        this.sectionItems = newSectionItems;
    }

    public SectionItem getSectionItem(String section) {
        return this.sectionItems.get(section);
    }

    public void addSection(String section, ShopSection shopSection) {
        this.sections.put(section, shopSection);
    }

    public Map<String, ShopSection> getSections() {
        return this.sections;
    }

    public ShopSection getSection(String section) {
        return this.sections.get(section);
    }

    public void setShopSections(List<String> newShopSections) {
        this.shopSections = newShopSections;
    }

    public List<String> getShopSections() {
        return this.shopSections;
    }

    public void removeShopSection(String section) {
        this.shopSections.remove(section);
    }

    public List<String> getSupportedMatNames() {
        return this.supportedMatNames;
    }

    public void addSpawnerName(EntityType entityType, Translatable name) {
        this.spawnerNames.put(entityType, name);
    }

    public Translatable getSpawnerName(EntityType entityType) {
        return this.spawnerNames.get(entityType);
    }

    public boolean isSeasonalPricing() {
        return this.modifierManager.hasSeasonalPrices();
    }

    private void setModifierManager(ModifierManager modifierManager) {
        this.modifierManager = modifierManager;
    }

    public void reloadModifiers() {
        this.modifierManager.reloadModifiers();
    }

    public void setStandProvider(StandProvider standProvider) {
        this.standProvider = standProvider;
    }

    public StandProvider getStandProvider() {
        return this.standProvider;
    }

    private void setSkullUtil(SkullUtil skullUtil) {
        this.skullUtil = skullUtil;
    }

    public HashMap<String, Double> getDiscounts(String section) {
        return this.discounts.get(section);
    }

    public void setDiscounts(HashMap<String, HashMap<String, Double>> discounts) {
        if (!this.discounts.isEmpty()) {
            this.discounts.clear();
        }
        this.discounts.putAll(discounts);
    }

    public boolean hasDiscount(String section) {
        return this.discounts.containsKey(section);
    }

    public HashMap<String, Double> getMultipliers(String section) {
        return this.sellMultipliers.get(section);
    }

    public boolean hasMultiplier(String section) {
        return this.sellMultipliers.containsKey(section);
    }

    public void setSellMultipliers(HashMap<String, HashMap<String, Double>> sellMultipliers) {
        if (!this.sellMultipliers.isEmpty()) {
            this.sellMultipliers.clear();
        }
        this.sellMultipliers.putAll(sellMultipliers);
    }

    public Map<String, Translatable> getSectionTitles() {
        return this.sectionTitles;
    }

    public void setSectionTitles(HashMap<String, Translatable> newSectionTitles) {
        this.sectionTitles = newSectionTitles;
    }

    public Translatable getSectionTitle(String section) {
        return this.sectionTitles.get(section);
    }

    public Map<String, Translatable> getBedrockSectionTitles() {
        return this.bedrockSectionTitles;
    }

    public void setBedrockSectionTitles(HashMap<String, Translatable> newBedrockSectionTitles) {
        this.bedrockSectionTitles = newBedrockSectionTitles;
    }

    public Translatable getBedrockSectionTitle(String section) {
        return this.bedrockSectionTitles.get(section);
    }

    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    public SpawnerManager getSpawnerManager() {
        return this.spawnerManager;
    }

    public EnchantCostProvider getEnchantCostManager() {
        return this.enchantCostManager;
    }

    public Chat getChat() {
        return this.chat;
    }

    public void setLoreFormatter(LoreFormatter loreFormatter) {
        this.loreFormatter = loreFormatter;
    }

    public LoreFormatter getLoreFormatter() {
        return this.loreFormatter;
    }

    public MetaUtils getMetaUtils() {
        return this.metaUtils;
    }

    public BedrockInventorys getBedrockInventorys() {
        return this.bedrockInventorys;
    }

    private void enableBedrockMenus() {
        if (!ConfigManager.getConfig().getBoolean("bedrock-forms.enable", false)) {
            return;
        }
        if (this.getServer().getPluginManager().getPlugin("Floodgate") == null) {
            return;
        }
        this.bedrockInventorys = new BedrockInventorys(this);
    }

    private void enableGeyserExtension() {
        if (!ConfigManager.getConfig().getBoolean("pre-register-skulls", false)) {
            return;
        }
        if (this.getServer().getPluginManager().getPlugin("Geyser-Spigot") == null) {
            SendMessage.errorMessage("Cannot enable Geyser hook: Failed to find Geyser-Spigot");
            return;
        }
        this.geyserModule = new GeyserModule(this);
    }

    public String getDisplayName(Player p, String s) {
        try {
            if (this.chat != null && this.prefixSuffix) {
                return this.getPlayerPrefix(p) + ChatColor.getLastColors((String)s.split("%player_displayname%")[0]) + p.getDisplayName() + this.getPlayerSuffix(p);
            }
            return p.getDisplayName();
        }
        catch (ArrayIndexOutOfBoundsException e) {
            return this.getPlayerPrefix(p) + p.getDisplayName() + this.getPlayerSuffix(p);
        }
    }

    public String getDisplayName(Player p) {
        if (this.chat != null && this.prefixSuffix) {
            return this.getPlayerPrefix(p) + p.getDisplayName() + this.getPlayerSuffix(p);
        }
        return p.getDisplayName();
    }

    private String getPlayerPrefix(Player p) {
        if (!this.chat.getPlayerPrefix(p).isEmpty()) {
            return ChatUtil.formatColors(this.chat.getPlayerPrefix(p) + "&r ");
        }
        return "";
    }

    private String getPlayerSuffix(Player p) {
        if (!this.chat.getPlayerSuffix(p).isEmpty()) {
            return ChatUtil.formatColors("&r " + this.chat.getPlayerSuffix(p));
        }
        return "";
    }

    public RequirementManager getRequirementManager() {
        return this.requirementManager;
    }

    public EconomyHandler getEcoHandler() {
        return this.ecoHandler;
    }

    public void updateClickMappings() {
        this.mappings = new ClickMappings(null);
    }

    public ClickMappings getClickMappings() {
        return this.mappings;
    }

    private void setupPlaceholderExpansion() {
        if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.papiExpansion = new PAPIExpansion(this);
        }
    }

    public void reloadPlayerData() {
        this.userManager.reloadPlayerData();
    }

    public void reloadPermissions() {
        org.bukkit.permissions.Permission perm = this.getServer().getPluginManager().getPermission("DynamicShopGUIPlus.shop.all");
        if (perm != null) {
            this.shopSections.forEach(s -> {
                String node = "DynamicShopGUIPlus.shop." + s.toLowerCase(Locale.ENGLISH);
                if (!perm.getChildren().containsKey(node)) {
                    perm.getChildren().put(node, true);
                }
            });
        }
    }

    private void registerPermissions() {
        try {
            Bukkit.getPluginManager().addPermission(new org.bukkit.permissions.Permission("DynamicShopGUIPlus.shop.all", "Gives access to all shop category's", PermissionDefault.TRUE, this.shopSections.stream().map(s -> "DynamicShopGUIPlus.shop." + s.toLowerCase(Locale.ENGLISH)).collect(Collectors.toMap(Function.identity(), s -> true))));
            Bukkit.getPluginManager().addPermission(new org.bukkit.permissions.Permission("DynamicShopGUIPlus.sellall.all", "Allows players to sell items to every shop section with the /sellall command", PermissionDefault.TRUE, this.shopSections.stream().map(s -> "DynamicShopGUIPlus.sellall." + s.toLowerCase(Locale.ENGLISH)).collect(Collectors.toMap(Function.identity(), s -> true))));
            Bukkit.getPluginManager().addPermission(new org.bukkit.permissions.Permission("DynamicShopGUIPlus.sellallitem.all", "Allows players to sell items to every shop section with the /sellall <item> command", PermissionDefault.TRUE, this.shopSections.stream().map(s -> "DynamicShopGUIPlus.sellallitem." + s.toLowerCase(Locale.ENGLISH)).collect(Collectors.toMap(Function.identity(), s -> true))));
            Bukkit.getPluginManager().addPermission(new org.bukkit.permissions.Permission("DynamicShopGUIPlus.sellallhand.all", "Allows players to sell items to every shop section with the /sellall hand command", PermissionDefault.TRUE, this.shopSections.stream().map(s -> "DynamicShopGUIPlus.sellallhand." + s.toLowerCase(Locale.ENGLISH)).collect(Collectors.toMap(Function.identity(), s -> true))));
            Bukkit.getPluginManager().addPermission(new org.bukkit.permissions.Permission("DynamicShopGUIPlus.sellgui.all", "Allows players to sell items to every shop section with the SellGUI screen", PermissionDefault.TRUE, this.shopSections.stream().map(s -> "DynamicShopGUIPlus.sellgui." + s.toLowerCase(Locale.ENGLISH)).collect(Collectors.toMap(Function.identity(), s -> true))));
        }
        catch (IllegalArgumentException e) {
            SendMessage.logDebugMessage("Failed to register permissions, ignore this if you reloaded the plugin.");
            SendMessage.logDebugMessage(e.getMessage());
        }
    }

    private void registerEvents() {
        this.menuHandler = new MenuHandler(this);
        this.getServer().getPluginManager().registerEvents((Listener)this.menuHandler, (Plugin)this);
        this.getServer().getPluginManager().registerEvents((Listener)new LocaleChangeEvent(), (Plugin)this);
        if (this.badYMLParse == null) {
            if (ConfigManager.getConfig().getBoolean("enable-levelevent")) {
                this.getServer().getPluginManager().registerEvents((Listener)new LevelEvent(), (Plugin)this);
            }
            this.joinEvent = new JoinEvent(this, ConfigManager.getConfig().getBoolean("enable-joinmessage"));
            this.getServer().getPluginManager().registerEvents((Listener)this.joinEvent, (Plugin)this);
        }
    }

    private void registerCommands() {
        try {
            Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap)bukkitCommandMap.get(Bukkit.getServer());
            if (this.badYMLParse != null || ConfigManager.getConfig().getBoolean("commands.shopgive", true)) {
                commandMap.register("dynamicshopguiplus", (Command)new ShopGive(this, this.getDisabledWorlds("shopgive")));
            }
            if (this.badYMLParse != null || ConfigManager.getConfig().getBoolean("commands.editshop", true)) {
                commandMap.register("dynamicshopguiplus", (Command)new CommandManager(this, this.getDisabledWorlds("editshop")));
            }
            if (this.badYMLParse != null || ConfigManager.getConfig().getBoolean("commands.sreload", true)) {
                commandMap.register("dynamicshopguiplus", (Command)new Sreload(this, this.getDisabledWorlds("sreload")));
            }
            if (this.badYMLParse != null || ConfigManager.getConfig().getBoolean("commands.shop", true)) {
                List<String> shopAliases;
                List list = shopAliases = this.badYMLParse != null ? new ArrayList() : ConfigManager.getConfig().getStringList("shop-commands");
                if (shopAliases.isEmpty()) {
                    shopAliases.add("shop");
                }
                commandMap.register("dynamicshopguiplus", (Command)new Shop(this, shopAliases, this.getDisabledWorlds("shop")));
            }
            if (this.badYMLParse != null || ConfigManager.getConfig().getBoolean("commands.sellall", true)) {
                List<String> sellallAliases;
                List list = sellallAliases = this.badYMLParse != null ? new ArrayList() : ConfigManager.getConfig().getStringList("sellall-commands");
                if (sellallAliases.isEmpty()) {
                    sellallAliases.add("sellall");
                }
                if (this.badYMLParse == null && Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
                    this.overrideSellCommand(ConfigManager.getConfig().getBoolean("override-sell-command", true));
                }
                commandMap.register("dynamicshopguiplus", (Command)new Sellall(this, sellallAliases, this.getDisabledWorlds("sellall")));
            }
            if (this.badYMLParse != null || ConfigManager.getConfig().getBoolean("commands.sellgui", true)) {
                List<String> sellguiAliases;
                List list = sellguiAliases = this.badYMLParse != null ? new ArrayList() : ConfigManager.getConfig().getStringList("sellgui-commands");
                if (sellguiAliases.isEmpty()) {
                    sellguiAliases.add("sellgui");
                }
                commandMap.register("dynamicshopguiplus", (Command)new SellGUI(this, sellguiAliases, this.getDisabledWorlds("sellgui")));
            }
        }
        catch (IllegalAccessException | NoSuchFieldException e) {
            SendMessage.errorMessage("Error registering commands.");
            e.printStackTrace();
        }
    }

    private List<String> getDisabledWorlds(String commandName) {
        if (this.badYMLParse == null && ConfigManager.getConfig().getBoolean("enable-disabled-worlds", false)) {
            return ConfigManager.getConfig().getStringList("disabled-worlds-per-command." + commandName);
        }
        return new ArrayList<String>();
    }

    public void loadChat() {
        RegisteredServiceProvider rsp = this.getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp != null) {
            this.chat = (Chat)rsp.getProvider();
        }
    }

    public String initLore(String s) {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.setLore(Collections.singletonList(s));
        item.setItemMeta(meta);
        return this.versionHandler.getLoreAsNBT(item);
    }

    public YamlConfiguration loadConfiguration(File file) throws ConfigLoadException {
        Validate.notNull((Object)file, (String)("Cannot load " + file.getName() + " config file."));
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load((Reader)new InputStreamReader((InputStream)new FileInputStream(file), StandardCharsets.UTF_8));
            if (ConfigManager.badYaml != null && ConfigManager.badYaml.equals(file.getName())) {
                ConfigManager.badYaml = null;
            }
            return config;
        }
        catch (FileNotFoundException fileNotFoundException) {
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InvalidConfigurationException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            throw new ConfigLoadException("Cannot read " + file.getName() + " config because it is mis-configured, use a online Yaml parser with the error underneath here to find out the cause of the problem and to solve it.\n" + sw.toString());
        }
        ConfigManager.badYaml = file.getName();
        return null;
    }

    public YamlConfiguration loadConfiguration(File file, String fileName) {
        Validate.notNull((Object)file, (String)("Cannot load " + fileName + " config file."));
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load((Reader)new InputStreamReader((InputStream)new FileInputStream(file), StandardCharsets.UTF_8));
            if (this.badYMLParse != null && this.badYMLParse.equals(fileName)) {
                this.badYMLParse = null;
            }
            return config;
        }
        catch (FileNotFoundException fileNotFoundException) {
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InvalidConfigurationException e) {
            SendMessage.errorMessage("Cannot read " + fileName + " config because it is mis-configured, use a online Yaml parser with the error underneath here to find out the cause of the problem and to solve it. If you cannot find the cause yourself, join our discord support server that can be found at a plugin page of DynamicShopGUIPlus.");
            e.printStackTrace();
        }
        ConfigManager.badYaml = fileName;
        return null;
    }

    public YamlConfiguration loadConfiguration(BufferedReader reader, String fileName) {
        Validate.notNull((Object)reader, (String)("Cannot load " + fileName + " config file."));
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load((Reader)reader);
            if (this.badYMLParse != null && this.badYMLParse.equals(fileName)) {
                this.badYMLParse = null;
            }
            return config;
        }
        catch (FileNotFoundException fileNotFoundException) {
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InvalidConfigurationException e) {
            SendMessage.errorMessage("Cannot read " + fileName + " config because it is mis-configured, use a online Yaml parser with the error underneath here to find out the cause of the problem and to solve it. If you cannot find the cause yourself, join our discord support server that can be found at a plugin page of DynamicShopGUIPlus.");
            e.printStackTrace();
        }
        ConfigManager.badYaml = fileName;
        return null;
    }

    public void createConfigBackup() {
        String fileName = "config.yml";
        SendMessage.infoMessage(Lang.CREATING_BACKUP.get().replace("%fileName%", fileName));
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
        LocalDateTime now = LocalDateTime.now();
        Path backupDir = Paths.get(DynamicShopGUIPlus.getInstance().getDataFolder() + "/backups/", new String[0]);
        if (!Files.exists(backupDir, new LinkOption[0]) || !Files.isDirectory(backupDir, new LinkOption[0])) {
            try {
                Files.createDirectory(backupDir, new FileAttribute[0]);
            }
            catch (FileAlreadyExistsException fileAlreadyExistsException) {
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        File file = new File(DynamicShopGUIPlus.getInstance().getDataFolder(), fileName);
        try {
            try {
                Files.copy(file.toPath(), Paths.get(DynamicShopGUIPlus.getInstance().getDataFolder() + "/backups/", "config " + dtf.format(now) + ".yml"), new CopyOption[0]);
            }
            catch (FileAlreadyExistsException fileAlreadyExistsException) {}
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createConfigBackup(String fileName) {
        SendMessage.logDebugMessage(Lang.CREATING_BACKUP.get().replace("%fileName%", fileName));
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
        LocalDateTime now = LocalDateTime.now();
        Path backupDir = Paths.get(DynamicShopGUIPlus.getInstance().getDataFolder() + "/backups/", new String[0]);
        if (!Files.exists(backupDir, new LinkOption[0]) || !Files.isDirectory(backupDir, new LinkOption[0])) {
            try {
                Files.createDirectory(backupDir, new FileAttribute[0]);
            }
            catch (FileAlreadyExistsException fileAlreadyExistsException) {
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        File file = new File(DynamicShopGUIPlus.getInstance().getDataFolder(), fileName);
        try {
            try {
                Files.copy(file.toPath(), Paths.get(DynamicShopGUIPlus.getInstance().getDataFolder() + "/backups/", fileName.substring(0, fileName.indexOf(".")) + " " + dtf.format(now) + ".yml"), new CopyOption[0]);
            }
            catch (FileAlreadyExistsException fileAlreadyExistsException) {}
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ScheduledTask runTask(Runnable run) {
        return this.scheduler.runTask(this, run);
    }

    public ScheduledTask runTaskLater(Runnable run, long delay) {
        return this.scheduler.runTaskLater(this, run, delay);
    }

    public ScheduledTask runTaskLaterAsync(Runnable run, long delay) {
        return this.scheduler.runTaskLaterAsync(this, run, delay);
    }

    public ScheduledTask runTaskAsyncTimer(Runnable run, long delay, long period) {
        return this.scheduler.runTaskAsyncTimer(this, run, delay, period);
    }

    public ScheduledTask runTaskAsync(Runnable run) {
        return this.scheduler.runTaskAsync(this, run);
    }

    public void runKillableTask(Runnable run) {
        this.scheduler.runKillableTask(run);
    }

    public void runKillableTaskLater(Runnable run, long delay) {
        this.scheduler.runKillableTaskLater(run, delay);
    }

    public String formatNullablePrice(String ecoType, double amount) {
        EcoType type = EconomyType.getFromString(ecoType);
        if (type == null) {
            return null;
        }
        EconomyProvider provider = this.ecoHandler.getEcon(type);
        if (provider == null) {
            return null;
        }
        return provider.formatPrice(amount);
    }

    public String formatPrice(EcoType type, Double amount) {
        if (type != null && !this.ecoHandler.getEcon(type).isDecimal()) {
            amount = (double) Math.round(amount);
        }
        return this.ecoHandler.getEcon(type).formatPrice(amount);
    }

    private boolean checkForGson() {
        return ServerInfo.getVersion() != ServerInfo.Version.v1_8_R1 && ServerInfo.getVersion() != ServerInfo.Version.v1_8_R2;
    }

    private boolean isSpigotServer() {
        try {
            Class.forName("org.spigotmc.SpigotConfig");
            return true;
        }
        catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void overrideSellCommand(boolean override) {
        boolean modified = false;
        String path = new File("commands.yml").getAbsolutePath();
        YamlConfiguration config = this.loadConfiguration(new File(path), path);
        if (override) {
            if (!config.contains("aliases.sell")) {
                config.set("aliases.sell", Collections.singletonList("dynamicshopguiplus:sellall $1-"));
                modified = true;
            } else if (!config.getStringList("aliases.sell").contains("dynamicshopguiplus:sellall $1-")) {
                List aliases = config.getStringList("aliases.sell");
                aliases.add("dynamicshopguiplus:sellall $1-");
                config.set("aliases.sell", (Object)aliases);
                modified = true;
            }
        } else if (config.getStringList("aliases.sell").contains("dynamicshopguiplus:sellall $1-")) {
            if (config.getStringList("aliases.sell").size() == 1) {
                config.set("aliases.sell", null);
                modified = true;
            } else {
                List aliases = config.getStringList("aliases.sell");
                aliases.remove("dynamicshopguiplus:sellall $1-");
                config.set("aliases.sell", (Object)aliases);
                modified = true;
            }
        }
        if (!modified) {
            return;
        }
        try {
            config.save(path);
        }
        catch (IOException e) {
            SendMessage.errorMessage("Failed to save " + path);
        }
    }

    public String getInvTitle(String title) {
        if (this.version == 18) {
            return title.length() > 32 ? title.substring(0, 32) : title;
        }
        return this.version == 113 ? this.getTitle(title) : title;
    }

    private String getTitle(String title) {
        return this.versionHandler.getTitle(Bukkit.createInventory(null, (int)9, (String)title));
    }

    private ServerScheduler getScheduler() {
        return ServerInfo.isFolia() ? new FoliaScheduler(this) : new BukkitScheduler(this);
    }

    private void loadMetaUtils() {
        this.metaUtils = ServerInfo.supportsPaper() && ServerInfo.getVersion().newerOrEqualAs(ServerInfo.Version.v1_19_R1) ? new PaperMeta(this) : (ChatUtil.getAdventureUtils() != null && ServerInfo.supportsMiniMessageBukkit() ? new AdventureMeta(this) : new BukkitMeta(this));
        SendMessage.infoMessage("Initializing " + (this.metaUtils instanceof PaperMeta ? "paper" : (this.metaUtils instanceof AdventureMeta ? "adventure" : "bukkit")) + " meta...");
    }

    public boolean isSimilar(ItemStack itemOne, ItemStack itemTwo) {
        if (itemOne == itemTwo) {
            return true;
        }
        return this.versionHandler.isSimilar(new ItemStack(itemOne), new ItemStack(itemTwo), this.ignoredNBTData);
    }

    public boolean isSimilar(ItemStack itemOne, ItemStack itemTwo, List<String> ignoredNBTData) {
        if (itemOne == itemTwo) {
            return true;
        }
        ignoredNBTData.addAll(this.ignoredNBTData);
        return this.versionHandler.isSimilar(new ItemStack(itemOne), new ItemStack(itemTwo), ignoredNBTData);
    }

    public String getMaterialName(String mat) {
        String[] arr = mat.toLowerCase().replace("_", " ").split(" ");
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < arr.length; ++i) {
            sb.append(Character.toUpperCase(arr[i].charAt(0))).append(arr[i].substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    public List<String> getExamplePrices() {
        return new ArrayList<String>(Arrays.asList("-1", "24.99", "100"));
    }

    private List<String> setSupportedMatNames() {
        ArrayList<String> supportedMatNames = new ArrayList<String>();
        for (XMaterial mat : XMaterial.values()) {
            if (!mat.isSupported()) continue;
            supportedMatNames.add(mat.name());
        }
        return supportedMatNames;
    }

    public void closeOpenGUIS() {
        try {
            for (Player p : this.getServer().getOnlinePlayers()) {
                if (!(InventoryUtil.getTopHolder(p) instanceof ShopInventory)) continue;
                p.closeInventory();
            }
        }
        catch (Exception e) {
            SendMessage.logDebugMessage("Failed to close shop menu(s) for currently browsing player(s)");
            e.printStackTrace();
        }
    }
}

