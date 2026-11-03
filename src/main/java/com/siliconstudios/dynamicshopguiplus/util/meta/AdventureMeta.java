

package com.siliconstudios.dynamicshopguiplus.util.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.files.Translatable;
import com.siliconstudios.dynamicshopguiplus.files.lang.TranslatableComponent;
import com.siliconstudios.dynamicshopguiplus.objects.ShopItem;
import com.siliconstudios.dynamicshopguiplus.util.AdventureUtil;
import com.siliconstudios.dynamicshopguiplus.util.ChatUtil;
import com.siliconstudios.dynamicshopguiplus.util.EcoType;
import com.siliconstudios.dynamicshopguiplus.util.MetaUtils;
import com.siliconstudios.dynamicshopguiplus.util.Transaction;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class AdventureMeta
implements MetaUtils {
    public static final Map<Character, TextColor> COLOR_MAP = new HashMap<Character, TextColor>();
    private static final Component RESET = ((TextComponent)((TextComponent)Component.text((String)"").color(null)).decoration(TextDecoration.BOLD, false)).decoration(TextDecoration.ITALIC, false);
    private static final Config config = new Config();
    private final BukkitAudiences audiences;
    private final Audience logger;
    private final DynamicShopGUIPlus plugin;

    public AdventureMeta(DynamicShopGUIPlus plugin) {
        this.audiences = BukkitAudiences.create((Plugin)plugin);
        this.logger = this.audiences.sender((CommandSender)plugin.getServer().getConsoleSender());
        this.plugin = plugin;
    }

    @Override
    public void update() {
        config.update();
    }

    private void send(Player p, Component msg) {
        this.audiences.player(p).sendMessage(AdventureMeta.config.prefix.append(msg));
    }

    @Override
    public void send(Player p, Translatable msg) {
        this.audiences.player(p).sendMessage(AdventureMeta.config.prefix.append((Component)msg.get()));
    }

    @Override
    public void send(Object logger, Translatable msg) {
        if (logger instanceof Player) {
            this.audiences.player((Player)logger).sendMessage(AdventureMeta.config.prefix.append((Component)msg.get()));
        } else {
            this.audiences.sender((CommandSender)logger).sendMessage((Component)msg.get());
        }
    }

    @Override
    public void send(Translatable msg) {
        this.logger.sendMessage((Component)msg.get());
    }

    @Override
    public void sendLegacy(Player p, String msg) {
        p.sendMessage(AdventureUtil.toLegacy(AdventureMeta.config.prefix) + msg);
    }

    @Override
    public void sendLegacy(Object logger, String msg) {
        if (logger instanceof Player) {
            ((Player)logger).sendMessage(AdventureUtil.toLegacy(AdventureMeta.config.prefix) + msg);
        } else {
            ((CommandSender)logger).sendMessage(msg);
        }
    }

    @Override
    public void sendLegacy(String msg) {
        this.plugin.getServer().getConsoleSender().sendMessage(msg);
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

    @Override
    public void broadcast(Translatable msg) {
        this.plugin.getServer().broadcast(AdventureMeta.config.prefix.append((Component)msg.get()));
    }

    @Override
    public void logDevDebugMessage(Translatable msg) {
        this.logger.sendMessage(AdventureMeta.config.consolePrefix.append(AdventureMeta.config.debug).append((Component)msg.get()));
    }

    @Override
    public void logPlayerTransaction(Translatable msg) {
        this.logger.sendMessage(AdventureMeta.config.prefix.append((Component)msg.get()));
    }

    @Override
    public void logPlayerTransaction(String msg) {
        this.logger.sendMessage(AdventureMeta.config.prefix.append((Component)Component.text((String)msg)));
    }

    @Override
    public void infoMessage(Translatable msg) {
        this.logger.sendMessage(AdventureMeta.config.consolePrefix.append(AdventureMeta.config.info).append((Component)msg.get()));
    }

    @Override
    public void logDebugMessage(Translatable msg) {
        this.logger.sendMessage(AdventureMeta.config.consolePrefix.append(AdventureMeta.config.debug).append((Component)msg.get()));
    }

    @Override
    public void warnMessage(Translatable msg) {
        this.logger.sendMessage(AdventureMeta.config.consolePrefix.append(AdventureMeta.config.warn).append((Component)msg.get()));
    }

    @Override
    public void errorMessage(Translatable msg) {
        this.logger.sendMessage(AdventureMeta.config.consolePrefix.append(AdventureMeta.config.error).append((Component)msg.get()));
    }

    @Override
    public void infoLegacyMessage(String msg) {
        this.logger.sendMessage(AdventureMeta.config.consolePrefix.append(AdventureMeta.config.info).append((Component)Component.text((String)msg)));
    }

    @Override
    public void logLegacyDebugMessage(String msg) {
        this.logger.sendMessage(AdventureMeta.config.consolePrefix.append(AdventureMeta.config.debug).append((Component)Component.text((String)msg)));
    }

    @Override
    public void warnLegacyMessage(String msg) {
        this.logger.sendMessage(AdventureMeta.config.consolePrefix.append(AdventureMeta.config.warn).append((Component)Component.text((String)msg)));
    }

    @Override
    public void errorLegacyMessage(String msg) {
        this.logger.sendMessage(AdventureMeta.config.consolePrefix.append(AdventureMeta.config.error).append((Component)Component.text((String)msg)));
    }

    @Override
    public void infoMessage(Object logger, Translatable msg) {
        if (logger instanceof Player) {
            this.audiences.player((Player)logger).sendMessage(AdventureMeta.config.prefix.color(COLOR_MAP.get(Character.valueOf('a'))).append((Component)msg.get()));
        } else {
            this.audiences.sender((CommandSender)logger).sendMessage(AdventureMeta.config.consolePrefix.append(AdventureMeta.config.info).append((Component)msg.get()));
        }
    }

    @Override
    public void warnMessage(Object logger, Translatable msg) {
        if (logger instanceof Player) {
            this.audiences.player((Player)logger).sendMessage(AdventureMeta.config.prefix.color(COLOR_MAP.get(Character.valueOf('c'))).append((Component)msg.get()));
        } else {
            this.audiences.sender((CommandSender)logger).sendMessage(AdventureMeta.config.consolePrefix.append(AdventureMeta.config.warn).append((Component)msg.get()));
        }
    }

    @Override
    public void errorMessage(Object logger, Translatable msg) {
        if (logger instanceof Player) {
            this.audiences.player((Player)logger).sendMessage(AdventureMeta.config.prefix.color(COLOR_MAP.get(Character.valueOf('c'))).append((Component)msg.get()));
        } else {
            this.audiences.sender((CommandSender)logger).sendMessage(AdventureMeta.config.consolePrefix.append(AdventureMeta.config.error).append((Component)msg.get()));
        }
    }

    @Override
    public void infoLegacyMessage(Object logger, String msg) {
        if (logger instanceof Player) {
            ((Player)logger).sendMessage(AdventureUtil.toLegacy(AdventureMeta.config.prefix) + ChatColor.GREEN + msg);
        } else if (logger instanceof CommandSender) {
            ((CommandSender)logger).sendMessage(AdventureUtil.toLegacy(AdventureMeta.config.consolePrefix) + AdventureMeta.config.info + msg);
        }
    }

    @Override
    public void warnLegacyMessage(Object logger, String msg) {
        if (logger instanceof Player) {
            ((Player)logger).sendMessage(AdventureUtil.toLegacy(AdventureMeta.config.prefix) + ChatColor.RED + msg);
        } else if (logger instanceof CommandSender) {
            ((CommandSender)logger).sendMessage(AdventureUtil.toLegacy(AdventureMeta.config.consolePrefix) + AdventureMeta.config.warn + msg);
        }
    }

    @Override
    public void errorLegacyMessage(Object logger, String msg) {
        if (logger instanceof Player) {
            ((Player)logger).sendMessage(AdventureUtil.toLegacy(AdventureMeta.config.prefix) + ChatColor.RED + msg);
        } else if (logger instanceof CommandSender) {
            ((CommandSender)logger).sendMessage(AdventureUtil.toLegacy(AdventureMeta.config.consolePrefix) + AdventureMeta.config.error + msg);
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

