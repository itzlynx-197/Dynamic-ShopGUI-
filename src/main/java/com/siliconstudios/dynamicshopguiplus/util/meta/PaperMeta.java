

package com.siliconstudios.dynamicshopguiplus.util.meta;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.files.Translatable;
import com.siliconstudios.dynamicshopguiplus.files.lang.TranslatableComponent;
import com.siliconstudios.dynamicshopguiplus.objects.ShopItem;
import com.siliconstudios.dynamicshopguiplus.util.AdventureUtil;
import com.siliconstudios.dynamicshopguiplus.util.ChatUtil;
import com.siliconstudios.dynamicshopguiplus.util.EcoType;
import com.siliconstudios.dynamicshopguiplus.util.MetaUtils;
import com.siliconstudios.dynamicshopguiplus.util.ServerInfo;
import com.siliconstudios.dynamicshopguiplus.util.Transaction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PaperMeta
implements MetaUtils {
    public static final Map<Character, TextColor> COLOR_MAP = new HashMap<Character, TextColor>();
    private static final Component RESET = ((TextComponent)((TextComponent)Component.text((String)"").color(null)).decoration(TextDecoration.BOLD, false)).decoration(TextDecoration.ITALIC, false);
    private static final Config config = new Config();
    private final ConsoleCommandSender logger;
    private final DynamicShopGUIPlus plugin;
    private Utils utils;

    public PaperMeta(DynamicShopGUIPlus plugin) {
        this.logger = plugin.getServer().getConsoleSender();
        this.plugin = plugin;
    }

    @Override
    public void update() {
        if (ConfigManager.getConfig().getBoolean("use-paper-meta", true) && ServerInfo.getVersion().newerOrEqualAs(ServerInfo.Version.v1_21_R2)) {
            this.utils = new PaperUtils();
            this.plugin.paperMeta = true;
        } else {
            this.utils = new BukkitUtils();
            this.plugin.paperMeta = false;
        }
        config.update();
    }

    private void send(Player p, Component msg) {
        p.sendMessage(PaperMeta.config.prefix.append(msg));
    }

    @Override
    public void send(Player p, Translatable msg) {
        p.sendMessage(PaperMeta.config.prefix.append((Component)msg.get()));
    }

    @Override
    public void send(Object logger, Translatable msg) {
        if (logger instanceof Player) {
            ((Player)logger).sendMessage(PaperMeta.config.prefix.append((Component)msg.get()));
        } else {
            ((CommandSender)logger).sendMessage((Component)msg.get());
        }
    }

    @Override
    public void send(Translatable msg) {
        this.logger.sendMessage((Component)msg.get());
    }

    @Override
    public void sendLegacy(Player p, String msg) {
        p.sendMessage(AdventureUtil.toLegacy(PaperMeta.config.prefix) + msg);
    }

    @Override
    public void sendLegacy(Object logger, String msg) {
        if (logger instanceof Player) {
            ((Player)logger).sendMessage(AdventureUtil.toLegacy(PaperMeta.config.prefix) + msg);
        } else {
            ((CommandSender)logger).sendMessage(msg);
        }
    }

    @Override
    public void sendLegacy(String msg) {
        this.logger.sendMessage(msg);
    }

    @Override
    public void sendTransactionMessage(Player p, int amount, double price, ShopItem shopItem, Transaction.Mode mode, Transaction.Type type) {
        Translatable message = mode == Transaction.Mode.BUY ? Lang.PAY_CONFIRMATION.get(p) : Lang.SELL_CONFIRMATION.get(p);
        String resetColor = AdventureUtil.getLastColor(message.toString().split("%material%")[0]);
        this.send(p, (Component)message.replace("%amounttopay%", this.plugin.formatPrice(shopItem.getEcoType(), price)).replace("%amount%", String.valueOf(amount)).replace("%material%", (shopItem instanceof ShopItem.Shulker ? shopItem.getDisplayname() + ChatColor.GRAY + ((ShopItem.Shulker)shopItem).getContents().entrySet().stream().map(entry -> entry.getValue() + "x " + ((ShopItem)entry.getKey()).getDisplayname() + ChatColor.GRAY).collect(Collectors.toList()) : shopItem.getDisplayname()) + resetColor).get());
    }

    @Override
    public void sendTransactionMessage(Player p, int amount, Map<EcoType, Double> prices, Map<ShopItem, Integer> items, Transaction.Type type) {
        Translatable message = Lang.SELL_CONFIRMATION_MULTIPLE_ITEMS.get(p);
        String resetColor = AdventureUtil.getLastColor(message.toString().split("%items%")[0]);
        String itemDisplays = this.getItemDisplay(resetColor, items);
        HoverEvent hover = HoverEvent.showText((Component)ChatUtil.getAdventureUtils().formatLegacyAndMini("\u00a77" + this.getItemDisplay("\u00a77", items))).asHoverEvent();
        this.send(p, ((Component)message.replace("%items%", itemDisplays).replace("%amount%", String.valueOf(amount)).replace("%amounttopay%", this.getCurrencyDisplay(resetColor, prices)).get()).hoverEvent((HoverEventSource)hover));
    }

    private String getItemDisplay(String resetColor, Map<ShopItem, Integer> items) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Map.Entry<ShopItem, Integer> entry : items.entrySet()) {
            if (entry.getKey() instanceof ShopItem.Shulker) {
                Map<ShopItem, Integer> contents = ((ShopItem.Shulker)entry.getKey()).getContents();
                sb.append(entry.getValue()).append("x ").append(entry.getKey().getDisplayname());
                sb.append(contents.entrySet().stream().map(e -> e.getValue() + "x " + ((ShopItem)e.getKey()).getDisplayname()).collect(Collectors.toList()));
            } else {
                sb.append(entry.getValue()).append("x ").append(entry.getKey().getDisplayname());
            }
            sb.append(resetColor);
            if (i != items.size() - 1) {
                sb.append(", ");
            }
            ++i;
        }
        return sb.toString();
    }

    private String getCurrencyDisplay(String resetColor, Map<EcoType, Double> prices) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Map.Entry<EcoType, Double> entry : prices.entrySet()) {
            sb.append(this.plugin.formatPrice(entry.getKey(), entry.getValue()));
            sb.append(resetColor);
            if (i != prices.size() - 1) {
                sb.append(", ");
            }
            ++i;
        }
        return sb.toString();
    }

    @Override
    public Inventory createInventory(InventoryHolder holder, int size, Translatable title) {
        return this.utils.createInventory(holder, size, title);
    }

    @Override
    public void setRawDisplayName(ItemMeta meta, String s) {
        this.utils.setRawDisplayName(meta, s);
    }

    @Override
    public void setDisplayName(ItemMeta meta, Translatable s) {
        this.utils.setDisplayName(meta, s);
    }

    @Override
    public String getRawDisplayName(ItemMeta meta) {
        return this.utils.getRawDisplayName(meta);
    }

    @Override
    public Translatable getDisplayName(ItemMeta meta) {
        return this.utils.getDisplayName(meta);
    }

    public ItemStack setDisplayname(ItemStack item, Object name) {
        item.setData(DataComponentTypes.ITEM_NAME, ((Component)name).decoration(TextDecoration.ITALIC, false));
        return item;
    }

    @Override
    public void setRawLore(ItemMeta meta, List<String> lore) {
        this.utils.setRawLore(meta, lore);
    }

    @Override
    public List<String> getRawLore(ItemMeta meta) {
        return this.utils.getRawLore(meta);
    }

    @Override
    public void setLore(ItemMeta meta, List<Translatable> lore) {
        this.utils.setLore(meta, lore);
    }

    @Override
    public void addLore(ItemMeta meta, Translatable line) {
        this.utils.addLore(meta, line);
    }

    @Override
    public List<Translatable> getLore(ItemMeta meta) {
        return this.utils.getLore(meta);
    }

    public static ItemStack setLore(ItemStack item, ArrayList<String> lore) {
        item.setData(DataComponentTypes.LORE, ItemLore.lore(lore.stream().map(s -> AdventureUtil.fromGson(s).decoration(TextDecoration.ITALIC, false)).collect(Collectors.toList())));
        return item;
    }

    public static ItemStack setRawLore(ItemStack item, ArrayList<String> lore) {
        item.setData(DataComponentTypes.LORE, ItemLore.lore(lore.stream().map(s -> ChatUtil.getAdventureUtils().formatLegacyAndMini((String)s).decoration(TextDecoration.ITALIC, false)).collect(Collectors.toList())));
        return item;
    }

    @Override
    public void broadcast(Translatable msg) {
        this.plugin.getServer().broadcast(PaperMeta.config.prefix.append((Component)msg.get()));
    }

    @Override
    public void logDevDebugMessage(Translatable msg) {
        this.logger.sendMessage(PaperMeta.config.consolePrefix.append(PaperMeta.config.debug).append((Component)msg.get()));
    }

    @Override
    public void logPlayerTransaction(Translatable msg) {
        this.logger.sendMessage(PaperMeta.config.prefix.append((Component)msg.get()));
    }

    @Override
    public void logPlayerTransaction(String msg) {
        this.logger.sendMessage(PaperMeta.config.prefix.append((Component)Component.text((String)msg)));
    }

    @Override
    public void infoMessage(Translatable msg) {
        this.logger.sendMessage(PaperMeta.config.consolePrefix.append(PaperMeta.config.info).append((Component)msg.get()));
    }

    @Override
    public void logDebugMessage(Translatable msg) {
        this.logger.sendMessage(PaperMeta.config.consolePrefix.append(PaperMeta.config.debug).append((Component)msg.get()));
    }

    @Override
    public void warnMessage(Translatable msg) {
        this.logger.sendMessage(PaperMeta.config.consolePrefix.append(PaperMeta.config.warn).append((Component)msg.get()));
    }

    @Override
    public void errorMessage(Translatable msg) {
        this.logger.sendMessage(PaperMeta.config.consolePrefix.append(PaperMeta.config.error).append((Component)msg.get()));
    }

    @Override
    public void infoLegacyMessage(String msg) {
        this.logger.sendMessage(PaperMeta.config.consolePrefix.append(PaperMeta.config.info).append((Component)Component.text((String)msg)));
    }

    @Override
    public void logLegacyDebugMessage(String msg) {
        this.logger.sendMessage(PaperMeta.config.consolePrefix.append(PaperMeta.config.debug).append((Component)Component.text((String)msg)));
    }

    @Override
    public void warnLegacyMessage(String msg) {
        this.logger.sendMessage(PaperMeta.config.consolePrefix.append(PaperMeta.config.warn).append((Component)Component.text((String)msg)));
    }

    @Override
    public void errorLegacyMessage(String msg) {
        this.logger.sendMessage(PaperMeta.config.consolePrefix.append(PaperMeta.config.error).append((Component)Component.text((String)msg)));
    }

    @Override
    public void infoMessage(Object logger, Translatable msg) {
        if (logger instanceof Player) {
            ((Player)logger).sendMessage(PaperMeta.config.prefix.color(COLOR_MAP.get(Character.valueOf('a'))).append((Component)msg.get()));
        } else {
            ((CommandSender)logger).sendMessage(PaperMeta.config.consolePrefix.append(PaperMeta.config.info).append((Component)msg.get()));
        }
    }

    @Override
    public void warnMessage(Object logger, Translatable msg) {
        if (logger instanceof Player) {
            ((Player)logger).sendMessage(PaperMeta.config.prefix.color(COLOR_MAP.get(Character.valueOf('c'))).append((Component)msg.get()));
        } else {
            ((CommandSender)logger).sendMessage(PaperMeta.config.consolePrefix.append(PaperMeta.config.warn).append((Component)msg.get()));
        }
    }

    @Override
    public void errorMessage(Object logger, Translatable msg) {
        if (logger instanceof Player) {
            ((Player)logger).sendMessage(PaperMeta.config.prefix.color(COLOR_MAP.get(Character.valueOf('c'))).append((Component)msg.get()));
        } else {
            ((CommandSender)logger).sendMessage(PaperMeta.config.consolePrefix.append(PaperMeta.config.error).append((Component)msg.get()));
        }
    }

    @Override
    public void infoLegacyMessage(Object logger, String msg) {
        if (logger instanceof Player) {
            ((Player)logger).sendMessage(AdventureUtil.toLegacy(PaperMeta.config.prefix) + ChatColor.GREEN + msg);
        } else if (logger instanceof CommandSender) {
            ((CommandSender)logger).sendMessage(AdventureUtil.toLegacy(PaperMeta.config.consolePrefix) + PaperMeta.config.info + msg);
        }
    }

    @Override
    public void warnLegacyMessage(Object logger, String msg) {
        if (logger instanceof Player) {
            ((Player)logger).sendMessage(AdventureUtil.toLegacy(PaperMeta.config.prefix) + ChatColor.RED + msg);
        } else if (logger instanceof CommandSender) {
            ((CommandSender)logger).sendMessage(AdventureUtil.toLegacy(PaperMeta.config.consolePrefix) + PaperMeta.config.warn + msg);
        }
    }

    @Override
    public void errorLegacyMessage(Object logger, String msg) {
        if (logger instanceof Player) {
            ((Player)logger).sendMessage(AdventureUtil.toLegacy(PaperMeta.config.prefix) + ChatColor.RED + msg);
        } else if (logger instanceof CommandSender) {
            ((CommandSender)logger).sendMessage(AdventureUtil.toLegacy(PaperMeta.config.consolePrefix) + PaperMeta.config.error + msg);
        }
    }

    static {
        COLOR_MAP.put(Character.valueOf('0'), TextColor.fromHexString((String)"#000000"));
        COLOR_MAP.put(Character.valueOf('1'), TextColor.fromHexString((String)"#0000AA"));
        COLOR_MAP.put(Character.valueOf('2'), TextColor.fromHexString((String)"#00AA00"));
        COLOR_MAP.put(Character.valueOf('3'), TextColor.fromHexString((String)"#00AAAA"));
        COLOR_MAP.put(Character.valueOf('4'), TextColor.fromHexString((String)"#AA0000"));
        COLOR_MAP.put(Character.valueOf('5'), TextColor.fromHexString((String)"#AA00AA"));
        COLOR_MAP.put(Character.valueOf('6'), TextColor.fromHexString((String)"#FFAA00"));
        COLOR_MAP.put(Character.valueOf('7'), TextColor.fromHexString((String)"#A9A9A9"));
        COLOR_MAP.put(Character.valueOf('8'), TextColor.fromHexString((String)"#555555"));
        COLOR_MAP.put(Character.valueOf('9'), TextColor.fromHexString((String)"#5555FF"));
        COLOR_MAP.put(Character.valueOf('a'), TextColor.fromHexString((String)"#55FF55"));
        COLOR_MAP.put(Character.valueOf('b'), TextColor.fromHexString((String)"#55FFFF"));
        COLOR_MAP.put(Character.valueOf('c'), TextColor.fromHexString((String)"#FF5555"));
        COLOR_MAP.put(Character.valueOf('d'), TextColor.fromHexString((String)"#FF55FF"));
        COLOR_MAP.put(Character.valueOf('e'), TextColor.fromHexString((String)"#FFFF55"));
        COLOR_MAP.put(Character.valueOf('f'), TextColor.fromHexString((String)"#FFFFFF"));
    }

    private static final class PaperUtils
    implements Utils {
        private static final Method GET_DISPLAYNAME;
        private static final Method SET_DISPLAYNAME;
        private static final Method GET_LORE;
        private static final Method SET_LORE;

        private PaperUtils() {
        }

        @Override
        public Inventory createInventory(InventoryHolder holder, int size, Translatable title) {
            return Bukkit.createInventory((InventoryHolder)holder, (int)size, (Component)((Component)title.get()));
        }

        @Override
        public void setRawDisplayName(ItemMeta meta, String s) {
            try {
                SET_DISPLAYNAME.invoke((Object)meta, ChatUtil.getAdventureUtils().formatLegacyAndMini(s).decoration(TextDecoration.ITALIC, false));
            }
            catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void setDisplayName(ItemMeta meta, Translatable s) {
            try {
                SET_DISPLAYNAME.invoke((Object)meta, ((Component)s.get()).decoration(TextDecoration.ITALIC, false));
            }
            catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        @Override
        public String getRawDisplayName(ItemMeta meta) {
            try {
                return AdventureUtil.serialize((Component)GET_DISPLAYNAME.invoke((Object)meta, new Object[0]));
            }
            catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public Translatable getDisplayName(ItemMeta meta) {
            try {
                return new TranslatableComponent((Component)GET_DISPLAYNAME.invoke((Object)meta, new Object[0]));
            }
            catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public void setRawLore(ItemMeta meta, List<String> lore) {
            try {
                SET_LORE.invoke((Object)meta, lore.stream().map(s -> ChatUtil.getAdventureUtils().formatLegacyAndMini((String)s).decoration(TextDecoration.ITALIC, false)).collect(Collectors.toList()));
            }
            catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void setLore(ItemMeta meta, List<Translatable> lore) {
            try {
                SET_LORE.invoke((Object)meta, lore.stream().map(t -> ((Component)t.get()).decoration(TextDecoration.ITALIC, false)).collect(Collectors.toList()));
            }
            catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void addLore(ItemMeta meta, Translatable line) {
            try {
                ArrayList<Component> lore = (ArrayList<Component>)GET_LORE.invoke((Object)meta, new Object[0]);
                if (lore == null) {
                    lore = new ArrayList<Component>();
                }
                lore.add(((Component)line.get()).decoration(TextDecoration.ITALIC, false));
                SET_LORE.invoke((Object)meta, lore);
            }
            catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        @Override
        public List<String> getRawLore(ItemMeta meta) {
            try {
                ArrayList<Object> lore = (ArrayList<Object>)GET_LORE.invoke((Object)meta, new Object[0]);
                if (lore == null) {
                    lore = new ArrayList();
                }
                return lore.stream().map(c -> AdventureUtil.serialize((Component)c)).collect(Collectors.toList());
            }
            catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public List<Translatable> getLore(ItemMeta meta) {
            try {
                ArrayList<Object> lore = (ArrayList<Object>)GET_LORE.invoke((Object)meta, new Object[0]);
                if (lore == null) {
                    lore = new ArrayList();
                }
                return lore.stream().map(c -> new TranslatableComponent((Component)c)).collect(Collectors.toList());
            }
            catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        }

        static {
            try {
                SET_DISPLAYNAME = ItemMeta.class.getDeclaredMethod("displayName", Component.class);
                SET_DISPLAYNAME.setAccessible(true);
                GET_DISPLAYNAME = ItemMeta.class.getDeclaredMethod("displayName", new Class[0]);
                GET_DISPLAYNAME.setAccessible(true);
                SET_LORE = ItemMeta.class.getDeclaredMethod("lore", List.class);
                SET_LORE.setAccessible(true);
                GET_LORE = ItemMeta.class.getDeclaredMethod("lore", new Class[0]);
                GET_LORE.setAccessible(true);
            }
            catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static interface Utils {
        public Inventory createInventory(InventoryHolder var1, int var2, Translatable var3);

        public void setRawDisplayName(ItemMeta var1, String var2);

        public void setDisplayName(ItemMeta var1, Translatable var2);

        public String getRawDisplayName(ItemMeta var1);

        public Translatable getDisplayName(ItemMeta var1);

        public void setRawLore(ItemMeta var1, List<String> var2);

        public void setLore(ItemMeta var1, List<Translatable> var2);

        public void addLore(ItemMeta var1, Translatable var2);

        public List<String> getRawLore(ItemMeta var1);

        public List<Translatable> getLore(ItemMeta var1);
    }

    private static final class BukkitUtils
    implements Utils {
        private BukkitUtils() {
        }

        @Override
        public Inventory createInventory(InventoryHolder holder, int size, Translatable title) {
            return Bukkit.createInventory((InventoryHolder)holder, (int)size, (String)title.getLegacy());
        }

        @Override
        public void setRawDisplayName(ItemMeta meta, String s) {
            meta.setDisplayName(s);
        }

        @Override
        public void setDisplayName(ItemMeta meta, Translatable s) {
            meta.setDisplayName(s.getLegacy());
        }

        @Override
        public String getRawDisplayName(ItemMeta meta) {
            return meta.getDisplayName();
        }

        @Override
        public Translatable getDisplayName(ItemMeta meta) {
            return new TranslatableComponent(meta.getDisplayName());
        }

        @Override
        public void setRawLore(ItemMeta meta, List<String> lore) {
            meta.setLore(lore);
        }

        @Override
        public void setLore(ItemMeta meta, List<Translatable> lore) {
            meta.setLore(lore.stream().map(Translatable::getLegacy).collect(Collectors.toList()));
        }

        @Override
        public void addLore(ItemMeta meta, Translatable line) {
            List list = meta.hasLore() ? meta.getLore() : new ArrayList();
            list.add(line.getLegacy());
            meta.setLore(list);
        }

        @Override
        public List<String> getRawLore(ItemMeta meta) {
            return meta.getLore();
        }

        @Override
        public List<Translatable> getLore(ItemMeta meta) {
            return meta.getLore().stream().map(TranslatableComponent::new).collect(Collectors.toList());
        }
    }

    private static final class Config {
        public final Component info = ChatUtil.getAdventureUtils().formatLegacyAndMini("\u00a78[\u00a77INFO\u00a78]\u00a7r: ");
        public final Component debug = ChatUtil.getAdventureUtils().formatLegacyAndMini("\u00a78[\u00a76DEBUG\u00a78]\u00a7r: ");
        public final Component warn = ChatUtil.getAdventureUtils().formatLegacyAndMini("\u00a78[\u00a7cWARN\u00a78]\u00a7r: ");
        public final Component error = ChatUtil.getAdventureUtils().formatLegacyAndMini("\u00a78[\u00a74ERROR\u00a78]\u00a7r: ");
        public final Component consolePrefix = ChatUtil.getAdventureUtils().formatLegacyAndMini("\u00a78[\u00a73DynamicShopGUIPlus\u00a78]\u00a7r ");
        public Component prefix;

        private Config() {
        }

        public void update() {
            this.prefix = ((Component)Lang.SHOP_PREFIX.get().get()).append(RESET);
        }
    }
}

