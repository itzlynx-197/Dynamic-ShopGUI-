

package com.siliconstudios.dynamicshopguiplus.util.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.files.Translatable;
import com.siliconstudios.dynamicshopguiplus.files.lang.TranslatableRaw;
import com.siliconstudios.dynamicshopguiplus.objects.ShopItem;
import com.siliconstudios.dynamicshopguiplus.util.EcoType;
import com.siliconstudios.dynamicshopguiplus.util.MetaUtils;
import com.siliconstudios.dynamicshopguiplus.util.Transaction;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.ItemMeta;

public class BukkitMeta
implements MetaUtils {
    private static final Config config = new Config();
    private final ConsoleCommandSender logger;
    private final DynamicShopGUIPlus plugin;

    public BukkitMeta(DynamicShopGUIPlus plugin) {
        this.plugin = plugin;
        this.logger = plugin.getServer().getConsoleSender();
    }

    @Override
    public void update() {
        config.update();
    }

    @Override
    public void send(Player p, Translatable msg) {
        p.sendMessage(BukkitMeta.config.prefix + msg.get());
    }

    @Override
    public void send(Object logger, Translatable msg) {
        if (logger instanceof Player) {
            this.send((Player)logger, msg);
        } else if (logger instanceof CommandSender) {
            ((CommandSender)logger).sendMessage((String)msg.get());
        }
    }

    @Override
    public void send(Translatable msg) {
        this.logger.sendMessage((String)msg.get());
    }

    @Override
    public void sendLegacy(Player p, String msg) {
        p.sendMessage(BukkitMeta.config.prefix + msg);
    }

    @Override
    public void sendLegacy(Object logger, String msg) {
        if (logger instanceof Player) {
            this.sendLegacy((Player)logger, BukkitMeta.config.prefix + msg);
        } else if (logger instanceof CommandSender) {
            ((CommandSender)logger).sendMessage(msg);
        }
    }

    @Override
    public void sendLegacy(String msg) {
        this.logger.sendMessage(msg);
    }

    private void send(Player p, TextComponent msg) {
        TextComponent text = new TextComponent(TextComponent.fromLegacyText((String)((String)Lang.SHOP_PREFIX.get().get())));
        text.addExtra((BaseComponent)msg);
        p.spigot().sendMessage((BaseComponent)text);
    }

    @Override
    public void sendTransactionMessage(Player p, int amount, double price, ShopItem shopItem, Transaction.Mode mode, Transaction.Type type) {
        String message = (String)(mode == Transaction.Mode.BUY ? Lang.PAY_CONFIRMATION.get(p).get() : Lang.SELL_CONFIRMATION.get(p).get());
        this.sendLegacy(p, message.replace("%amount%", String.valueOf(amount)).replace("%material%", shopItem instanceof ShopItem.Shulker ? shopItem.getDisplayname() + ChatColor.GRAY + ((ShopItem.Shulker)shopItem).getContents().entrySet().stream().map(entry -> entry.getValue() + "x " + ((ShopItem)entry.getKey()).getDisplayname() + ChatColor.GRAY).collect(Collectors.toList()) : shopItem.getDisplayname()).replace("%amounttopay%", DynamicShopGUIPlus.getInstance().formatPrice(shopItem.getEcoType(), price)));
    }

    @Override
    public void sendTransactionMessage(Player p, int amount, Map<EcoType, Double> prices, Map<ShopItem, Integer> items, Transaction.Type type) {
        String itemDisplays = this.getItemDisplay(items, false);
        String rawMessage = (String)Lang.SELL_CONFIRMATION_MULTIPLE_ITEMS.get(p).get();
        String message = rawMessage.replace("%items%", itemDisplays).replace("%amount%", String.valueOf(amount)).replace("%amounttopay%", this.getCurrencyDisplay(rawMessage.split("%amountofmoney%")[0], prices, false));
        TextComponent main = new TextComponent(TextComponent.fromLegacyText((String)message));
        main.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText((String)(ChatColor.GRAY + itemDisplays))));
        if (this.plugin.spigot) {
            this.send(p, main);
        } else {
            this.sendLegacy(p, message);
        }
    }

    private String getItemDisplay(Map<ShopItem, Integer> items, boolean c) {
        return this.getItemDisplay(items, c, false);
    }

    private String getItemDisplay(Map<ShopItem, Integer> items, boolean c, boolean logs) {
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
            sb.append("\u00a7r");
            if (logs) {
                sb.append("(").append(entry.getKey().getItemPath()).append(")");
            }
            if (!c) {
                sb.append("\u00a77");
            }
            if (i != items.size() - 1) {
                sb.append(", ");
            }
            ++i;
        }
        return sb.toString();
    }

    private String getCurrencyDisplay(String message, Map<EcoType, Double> prices, boolean c) {
        String color = ChatColor.getLastColors((String)message);
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Map.Entry<EcoType, Double> entry : prices.entrySet()) {
            sb.append(this.plugin.formatPrice(entry.getKey(), entry.getValue()));
            sb.append("\u00a7r");
            if (!c) {
                sb.append(color);
            }
            if (i != prices.size() - 1) {
                sb.append(", ");
            }
            ++i;
        }
        return sb.toString();
    }

    @Override
    public Inventory createInventory(InventoryHolder holder, int size, Translatable title) {
        return Bukkit.createInventory((InventoryHolder)holder, (int)size, (String)((String)title.get()));
    }

    @Override
    public void setRawDisplayName(ItemMeta meta, String s) {
        meta.setDisplayName(s);
    }

    @Override
    public void setDisplayName(ItemMeta meta, Translatable s) {
        meta.setDisplayName((String)s.get());
    }

    @Override
    public String getRawDisplayName(ItemMeta meta) {
        return meta.getDisplayName();
    }

    @Override
    public Translatable getDisplayName(ItemMeta meta) {
        return new TranslatableRaw(meta.getDisplayName());
    }

    @Override
    public void setRawLore(ItemMeta meta, List<String> lore) {
        meta.setLore(lore);
    }

    @Override
    public void setLore(ItemMeta meta, List<Translatable> lore) {
        meta.setLore(lore.stream().map(t -> (String)t.get()).collect(Collectors.toList()));
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
        return meta.getLore().stream().map(TranslatableRaw::new).collect(Collectors.toList());
    }

    @Override
    public void broadcast(Translatable msg) {
        this.plugin.getServer().broadcastMessage(BukkitMeta.config.prefix + msg.get());
    }

    @Override
    public void logDevDebugMessage(Translatable msg) {
        this.logger.sendMessage(BukkitMeta.config.debug + msg.get());
    }

    @Override
    public void logPlayerTransaction(Translatable msg) {
        this.logger.sendMessage(BukkitMeta.config.prefix + msg.get());
    }

    @Override
    public void logPlayerTransaction(String msg) {
        this.logger.sendMessage(BukkitMeta.config.prefix + msg);
    }

    @Override
    public void infoMessage(Translatable msg) {
        this.logger.sendMessage(BukkitMeta.config.consolePrefix + BukkitMeta.config.info + msg.get());
    }

    @Override
    public void logDebugMessage(Translatable msg) {
        this.logger.sendMessage(BukkitMeta.config.consolePrefix + BukkitMeta.config.debug + msg.get());
    }

    @Override
    public void warnMessage(Translatable msg) {
        this.logger.sendMessage(BukkitMeta.config.consolePrefix + BukkitMeta.config.warn + msg.get());
    }

    @Override
    public void errorMessage(Translatable msg) {
        this.logger.sendMessage(BukkitMeta.config.consolePrefix + BukkitMeta.config.error + msg.get());
    }

    @Override
    public void infoLegacyMessage(String msg) {
        this.logger.sendMessage(BukkitMeta.config.consolePrefix + BukkitMeta.config.info + msg);
    }

    @Override
    public void logLegacyDebugMessage(String msg) {
        this.logger.sendMessage(BukkitMeta.config.consolePrefix + BukkitMeta.config.debug + msg);
    }

    @Override
    public void warnLegacyMessage(String msg) {
        this.logger.sendMessage(BukkitMeta.config.consolePrefix + BukkitMeta.config.warn + msg);
    }

    @Override
    public void errorLegacyMessage(String msg) {
        this.logger.sendMessage(BukkitMeta.config.consolePrefix + BukkitMeta.config.error + msg);
    }

    @Override
    public void infoMessage(Object logger, Translatable msg) {
        if (logger instanceof Player) {
            this.sendLegacy((Player)logger, ChatColor.GREEN.toString() + msg.get());
        } else if (logger instanceof CommandSender) {
            ((CommandSender)logger).sendMessage(BukkitMeta.config.consolePrefix + BukkitMeta.config.info + msg.get());
        }
    }

    @Override
    public void warnMessage(Object logger, Translatable msg) {
        if (logger instanceof Player) {
            this.sendLegacy((Player)logger, ChatColor.RED.toString() + msg.get());
        } else if (logger instanceof CommandSender) {
            ((CommandSender)logger).sendMessage(BukkitMeta.config.consolePrefix + BukkitMeta.config.warn + msg.get());
        }
    }

    @Override
    public void errorMessage(Object logger, Translatable msg) {
        if (logger instanceof Player) {
            this.sendLegacy((Player)logger, ChatColor.RED.toString() + msg.get());
        } else if (logger instanceof CommandSender) {
            ((CommandSender)logger).sendMessage(BukkitMeta.config.consolePrefix + BukkitMeta.config.error + msg.get());
        }
    }

    @Override
    public void infoLegacyMessage(Object logger, String msg) {
        if (logger instanceof Player) {
            this.sendLegacy((Player)logger, ChatColor.GREEN + msg);
        } else if (logger instanceof CommandSender) {
            ((CommandSender)logger).sendMessage(BukkitMeta.config.consolePrefix + BukkitMeta.config.info + msg);
        }
    }

    @Override
    public void warnLegacyMessage(Object logger, String msg) {
        if (logger instanceof Player) {
            this.sendLegacy((Player)logger, ChatColor.RED + msg);
        } else if (logger instanceof CommandSender) {
            ((CommandSender)logger).sendMessage(BukkitMeta.config.consolePrefix + BukkitMeta.config.warn + msg);
        }
    }

    @Override
    public void errorLegacyMessage(Object logger, String msg) {
        if (logger instanceof Player) {
            this.sendLegacy((Player)logger, ChatColor.RED + msg);
        } else if (logger instanceof CommandSender) {
            ((CommandSender)logger).sendMessage(BukkitMeta.config.consolePrefix + BukkitMeta.config.error + msg);
        }
    }

    private static final class Config {
        public final String info = "\u00a78[\u00a77INFO\u00a78]\u00a7r: ";
        public final String debug = "\u00a78[\u00a76DEBUG\u00a78]\u00a7r: ";
        public final String warn = "\u00a78[\u00a7cWARN\u00a78]\u00a7r: ";
        public final String error = "\u00a78[\u00a74ERROR\u00a78]\u00a7r: ";
        public final String consolePrefix = "\u00a78[\u00a73DynamicShopGUIPlus\u00a78]\u00a7r ";
        public String prefix;

        private Config() {
        }

        public void update() {
            this.prefix = Lang.SHOP_PREFIX.get().get() + "\u00a7r";
        }
    }
}

