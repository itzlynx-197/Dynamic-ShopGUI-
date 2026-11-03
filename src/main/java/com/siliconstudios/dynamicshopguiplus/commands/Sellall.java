

package com.siliconstudios.dynamicshopguiplus.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.api.events.PostTransactionEvent;
import com.siliconstudios.dynamicshopguiplus.api.events.PreTransactionEvent;
import com.siliconstudios.dynamicshopguiplus.commands.base.PluginCommands;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.objects.ShopItem;
import com.siliconstudios.dynamicshopguiplus.util.EcoType;
import com.siliconstudios.dynamicshopguiplus.util.PermissionsCache;
import com.siliconstudios.dynamicshopguiplus.util.SmartStack;
import com.siliconstudios.dynamicshopguiplus.util.Sold;
import com.siliconstudios.dynamicshopguiplus.util.Transaction;
import com.siliconstudios.dynamicshopguiplus.util.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.StringUtil;

public class Sellall
extends PluginCommands {
    private final boolean oldSellallCommand;

    public Sellall(DynamicShopGUIPlus plugin, List<String> aliases, List<String> disabledWorlds) {
        super(plugin, aliases.get(0), "Sell all items from your inventory, hand items or specific items", "/" + aliases.remove(0) + " <inventory/item/hand> [qty]", null, aliases, disabledWorlds);
        this.oldSellallCommand = plugin.badYMLParse == null && ConfigManager.getConfig().getBoolean("old-sellall-command");
    }

    
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (this.plugin.badYMLParse != null) {
            SendMessage.warnMessage((Object)sender, "This command cannot be executed now, please fix the configuration formatting first!");
            return true;
        }
        if (!(sender instanceof Player)) {
            SendMessage.warnMessage((Object)sender, Lang.REAL_PLAYER.get());
            return true;
        }
        Player player = (Player)sender;
        if (!this.canUse(player)) {
            return true;
        }
        if (args.length == 0) {
            if (this.oldSellallCommand) {
                if (PermissionsCache.hasPermission(player, "DynamicShopGUIPlus.sellall")) {
                    this.sellInventory(player);
                    return true;
                }
                SendMessage.chatToPlayer(player, Lang.NO_PERMISSIONS.get(player));
                return true;
            }
            String usages = this.getCommandUsages(player);
            if (!usages.isEmpty()) {
                SendMessage.chatToPlayer(player, Lang.SELLALL_COMMAND_USAGES.get(player));
                SendMessage.chatToPlayer(player, usages);
                return true;
            }
            SendMessage.chatToPlayer(player, Lang.NO_PERMISSIONS.get(player));
            return true;
        }
        switch (args[0].toUpperCase(Locale.ENGLISH)) {
            case "INVENTORY": {
                if (PermissionsCache.hasPermission(player, "DynamicShopGUIPlus.sellall")) {
                    this.sellInventory(player);
                    return true;
                }
                SendMessage.chatToPlayer(player, Lang.NO_PERMISSIONS.get(player));
                return true;
            }
            case "HAND": {
                if (!PermissionsCache.hasPermission(player, "DynamicShopGUIPlus.sellallhand")) {
                    SendMessage.chatToPlayer(player, Lang.NO_PERMISSIONS.get(player));
                    return true;
                }
                int qty = 0;
                if (args.length >= 2) {
                    try {
                        qty = Integer.parseInt(args[1]);
                        if (qty <= 0) {
                            throw new NumberFormatException();
                        }
                    }
                    catch (NumberFormatException e) {
                        SendMessage.chatToPlayer(player, Lang.NO_VALID_AMOUNT.get(player));
                        return true;
                    }
                }
                this.sellHand(player, qty);
                return true;
            }
        }
        if (PermissionsCache.hasPermission(player, "DynamicShopGUIPlus.sellallitem")) {
            int quantity;
            XMaterial mat;
            block28: {
                mat = this.getItem(args[0].toUpperCase(Locale.ENGLISH), player);
                if (mat == null) {
                    return true;
                }
                if (mat == XMaterial.AIR) {
                    SendMessage.chatToPlayer(player, Lang.CANNOT_SELL_AIR.get(player));
                    return true;
                }
                if (args.length >= 2) {
                    try {
                        quantity = Integer.parseInt(args[1]);
                        if (quantity <= 0) {
                            throw new NumberFormatException();
                        }
                        break block28;
                    }
                    catch (NumberFormatException e) {
                        SendMessage.chatToPlayer(player, Lang.NO_VALID_AMOUNT.get(player));
                        return true;
                    }
                }
                quantity = Integer.MAX_VALUE;
            }
            this.removeItems(player, mat, quantity);
            return true;
        }
        SendMessage.chatToPlayer(player, Lang.NO_PERMISSIONS.get(player));
        return true;
    }

    private void sellInventory(Player player) {
        Transaction.Result result = Transaction.Result.SUCCESS;
        HashMap<Integer, SmartStack> toRemove = new HashMap<Integer, SmartStack>();
        HashMap<ShopItem, Sold> CACHE = new HashMap<ShopItem, Sold>();
        int amount = 0;
        for (int i = 0; i <= 35; ++i) {
            Integer sold;
            ItemStack itemInInv = player.getInventory().getItem(i);
            if (itemInInv == null || itemInInv.getType() == Material.AIR) continue;
            ShopItem shopItem = this.plugin.createItem.matchShopItem(itemInInv, player, "sellall");
            if (shopItem != null && shopItem.getSellPrice() >= 0.0) {
                int qty = itemInInv.getAmount();
                if (shopItem.isMaxSell(CACHE.getOrDefault(shopItem, new Sold()).getAmount() + qty)) {
                    if (CACHE.getOrDefault(shopItem, new Sold()).getAmount() >= shopItem.getMaxSell()) continue;
                    qty = shopItem.getMaxSell() - CACHE.getOrDefault(shopItem, new Sold()).getAmount();
                }
                Sold sold2 = CACHE.computeIfAbsent(shopItem, k -> new Sold());
                sold2.addValues(qty, shopItem.getSellPrice(player, itemInInv, qty));
                amount += qty;
                ItemStack itemStack = new ItemStack(itemInInv);
                itemStack.setAmount(itemStack.getAmount() - qty);
                toRemove.put(i, new SmartStack(itemStack));
                sold2.addIndex(i);
                continue;
            }
            if (!this.plugin.sellShulkers || !XMaterial.isShulker(itemInInv.getType()) || (sold = this.sellShulker(CACHE, toRemove, player, itemInInv, "sellall", i)) == null) continue;
            amount += sold.intValue();
        }
        for (Map.Entry<?, ?> sold : new HashSet<>(CACHE.entrySet())) {
            if (!((ShopItem)sold.getKey()).isMinSell(((Sold)sold.getValue()).getAmount())) continue;
            CACHE.remove(sold.getKey());
            amount -= ((Sold)sold.getValue()).getAmount();
            ((Sold)sold.getValue()).getIndexes().forEach(toRemove::remove);
        }
        Map<EcoType, Double> prices = new HashMap<EcoType, Double>();
        HashMap<ShopItem, Integer> items = new HashMap<ShopItem, Integer>();
        if (amount > 0) {
            prices = this.callPreTransactionEvent(this.getItems(items, CACHE), this.getPrices(prices, CACHE), player, amount);
            if (prices != null) {
                this.removeItems((Inventory)player.getInventory(), toRemove);
                prices.forEach((type, price) -> this.plugin.getEcoHandler().getEcon((EcoType)type).depositBalance((OfflinePlayer)player, (double)price));
                SendMessage.sendTransactionMessage(player, amount, prices, items, Transaction.Type.SELL_ALL_COMMAND);
            } else {
                result = Transaction.Result.TRANSACTION_CANCELLED;
            }
        } else {
            result = Transaction.Result.NO_ITEMS_FOUND;
            SendMessage.chatToPlayer(player, Lang.NO_ITEM_FOUND.get(player));
        }
        Bukkit.getPluginManager().callEvent((Event)new PostTransactionEvent(items, prices, player, amount, Transaction.Type.SELL_ALL_COMMAND, result));
    }

    private void sellHand(Player player, int quantity) {
        Transaction.Result result = Transaction.Result.SUCCESS;
        ItemStack itemInHand = this.plugin.versionHandler.getItemInHand(player);
        ShopItem shopItem = null;
        double sellPrice = 0.0;
        if (itemInHand != null && itemInHand.getType() != Material.AIR) {
            shopItem = this.plugin.createItem.matchShopItem(itemInHand, player, "sellallhand");
            if (shopItem != null && shopItem.getSellPrice() >= 0.0) {
                if (quantity == 0 || quantity >= itemInHand.getAmount()) {
                    quantity = itemInHand.getAmount();
                }
                if (shopItem.isMaxSell(quantity)) {
                    quantity = shopItem.getMaxSell();
                }
                sellPrice = this.callPreTransactionEvent(shopItem, player, quantity, shopItem.getSellPrice(player, itemInHand, quantity));
                if (!shopItem.isMinSell(quantity)) {
                    if (sellPrice >= 0.0) {
                        if (quantity >= itemInHand.getAmount()) {
                            quantity = itemInHand.getAmount();
                            if (this.plugin.version <= 110) {
                                player.getInventory().removeItem(new ItemStack[]{itemInHand});
                            } else {
                                itemInHand.setAmount(0);
                            }
                        } else {
                            itemInHand.setAmount(itemInHand.getAmount() - quantity);
                        }
                        this.plugin.getEcoHandler().getEcon(shopItem.getEcoType()).depositBalance((OfflinePlayer)player, sellPrice);
                        SendMessage.sendTransactionMessage(player, quantity, sellPrice, shopItem, Transaction.Mode.SELL, Transaction.Type.SELL_ALL_COMMAND);
                    } else {
                        result = Transaction.Result.TRANSACTION_CANCELLED;
                    }
                } else {
                    SendMessage.chatToPlayer(player, Lang.NOT_ENOUGH_ITEMS_TO_SELL.get(player).replace("%min-sell%", String.valueOf(shopItem.getMinSell())).replace("%quantity%", String.valueOf(quantity)));
                    result = Transaction.Result.NOT_ENOUGH_ITEMS;
                }
            } else if (this.plugin.sellShulkers && XMaterial.isShulker(itemInHand.getType())) {
                HashMap<ShopItem, Sold> CACHE = new HashMap<ShopItem, Sold>();
                HashMap<Integer, SmartStack> toRemove = new HashMap<Integer, SmartStack>();
                Integer sold = this.sellShulker(CACHE, toRemove, player, itemInHand, "sellallhand", player.getInventory().getHeldItemSlot());
                if (sold != null) {
                    Map<EcoType, Double> prices = this.getPrices(CACHE);
                    Map<ShopItem, Integer> items = this.getItems(CACHE);
                    prices = this.callPreTransactionEvent(items, prices, player, sold);
                    if (prices != null) {
                        this.removeItems((Inventory)player.getInventory(), toRemove);
                        prices.forEach((type, price) -> this.plugin.getEcoHandler().getEcon((EcoType)type).depositBalance((OfflinePlayer)player, (double)price));
                        SendMessage.sendTransactionMessage(player, sold, prices, items, Transaction.Type.SELL_ALL_COMMAND);
                    } else {
                        result = Transaction.Result.TRANSACTION_CANCELLED;
                    }
                } else {
                    result = Transaction.Result.NO_ITEMS_FOUND;
                    SendMessage.chatToPlayer(player, Lang.NO_ITEM_FOUND.get(player));
                }
            } else {
                result = Transaction.Result.NO_ITEMS_FOUND;
                SendMessage.chatToPlayer(player, Lang.NO_ITEM_FOUND.get(player));
            }
        } else {
            result = Transaction.Result.NO_ITEMS_FOUND;
            SendMessage.chatToPlayer(player, Lang.CANNOT_SELL_AIR.get(player));
        }
        Bukkit.getPluginManager().callEvent((Event)new PostTransactionEvent(shopItem, player, quantity, sellPrice, Transaction.Type.SELL_ALL_COMMAND, result));
    }

    private XMaterial getItem(String material, Player player) {
        XMaterial mat;
        if (material != null && !material.isEmpty()) {
            Optional<XMaterial> optionalMat = XMaterial.matchXMaterial(material);
            if (!optionalMat.isPresent()) {
                SendMessage.warnMessage((Object)player, Lang.INVALID_ITEM_TYPE.get(player).replace("%material%", material).replace("%type%", material));
                return null;
            }
            mat = optionalMat.get();
            if (!mat.isSupported()) {
                SendMessage.warnMessage((Object)player, Lang.MATERIAL_NOT_SUPPORTED.get(player) + " (" + material + ")");
                return null;
            }
        } else {
            SendMessage.warnMessage((Object)player, Lang.NEED_ITEM_MATERIAL.get(player) + " (" + material + ")");
            return null;
        }
        return mat;
    }

    private void removeItems(Player p, XMaterial mat, int amount) {
        Transaction.Result result = Transaction.Result.SUCCESS;
        HashMap<Integer, SmartStack> toRemove = new HashMap<Integer, SmartStack>();
        HashMap<ShopItem, Sold> CACHE = new HashMap<ShopItem, Sold>();
        int removed = 0;
        for (int i = 0; i < 36; ++i) {
            Integer sold;
            ItemStack is = p.getInventory().getItem(i);
            if (is == null || is.getType().equals((Object)Material.AIR) || !XMaterial.matchXMaterial(is).equals((Object)mat)) continue;
            ShopItem shopItem = this.plugin.createItem.matchShopItem(is, p, "sellallitem");
            if (shopItem != null && shopItem.getSellPrice() >= 0.0) {
                int qty = is.getAmount();
                if (shopItem.isMaxSell(CACHE.getOrDefault(shopItem, new Sold()).getAmount() + qty)) {
                    if (CACHE.getOrDefault(shopItem, new Sold()).getAmount() >= shopItem.getMaxSell()) continue;
                    qty = shopItem.getMaxSell() - CACHE.getOrDefault(shopItem, new Sold()).getAmount();
                }
                if (qty > amount) {
                    qty = amount;
                }
                Sold sold2 = CACHE.computeIfAbsent(shopItem, k -> new Sold());
                sold2.addValues(qty, shopItem.getSellPrice(p, is, qty));
                removed += qty;
                amount -= qty;
                ItemStack itemStack = new ItemStack(is);
                itemStack.setAmount(is.getAmount() - qty);
                toRemove.put(i, new SmartStack(itemStack));
                sold2.addIndex(i);
                if (amount > 0) continue;
                break;
            }
            if (!this.plugin.sellShulkers || !XMaterial.isShulker(is.getType()) || (sold = this.sellShulker(CACHE, toRemove, p, is, "sellallitem", i)) == null) continue;
            removed += sold.intValue();
        }
        for (Map.Entry<?, ?> sold : new HashSet<>(CACHE.entrySet())) {
            if (!((ShopItem)sold.getKey()).isMinSell(((Sold)sold.getValue()).getAmount())) continue;
            removed -= ((Sold)sold.getValue()).getAmount();
            CACHE.remove(sold.getKey());
            ((Sold)sold.getValue()).getIndexes().forEach(toRemove::remove);
        }
        Map<EcoType, Double> prices = new HashMap<EcoType, Double>();
        HashMap<ShopItem, Integer> items = new HashMap<ShopItem, Integer>();
        if (removed > 0) {
            prices = this.callPreTransactionEvent(this.getItems(items, CACHE), this.getPrices(prices, CACHE), p, removed);
            if (prices != null) {
                this.removeItems((Inventory)p.getInventory(), toRemove);
                prices.forEach((type, price) -> this.plugin.getEcoHandler().getEcon((EcoType)type).depositBalance((OfflinePlayer)p, (double)price));
                if (items.size() == 1) {
                    SendMessage.sendTransactionMessage(p, removed, prices.values().stream().mapToDouble(d -> d).sum(), (ShopItem)items.keySet().stream().findFirst().get(), Transaction.Mode.SELL, Transaction.Type.SELL_ALL_COMMAND);
                } else {
                    SendMessage.sendTransactionMessage(p, removed, prices, items, Transaction.Type.SELL_ALL_COMMAND);
                }
            } else {
                result = Transaction.Result.TRANSACTION_CANCELLED;
            }
        } else {
            result = Transaction.Result.NO_ITEMS_FOUND;
            SendMessage.chatToPlayer(p, Lang.NO_ITEM_FOUND.get(p));
        }
        Bukkit.getPluginManager().callEvent((Event)new PostTransactionEvent(items, prices, p, removed, Transaction.Type.SELL_ALL_COMMAND, result));
    }

    private Integer sellShulker(Map<ShopItem, Sold> CACHE, Map<Integer, SmartStack> toRemove, Player p, ItemStack box, String perm, int slot) {
        BlockStateMeta bm;
        HashMap<ShopItem, Sold> bCACHE = new HashMap<ShopItem, Sold>();
        HashMap<Integer, ItemStack> items = new HashMap<Integer, ItemStack>();
        int removed = 0;
        if (box.getItemMeta() instanceof BlockStateMeta && (bm = (BlockStateMeta)box.getItemMeta()).getBlockState() instanceof ShulkerBox) {
            Inventory inv = ((ShulkerBox)bm.getBlockState()).getInventory();
            if (inv.isEmpty()) {
                return null;
            }
            for (int i = 0; i < 27; ++i) {
                ShopItem shopItem;
                ItemStack is = inv.getItem(i);
                if (is == null || is.getType().equals((Object)Material.AIR) || (shopItem = this.plugin.createItem.matchShopItem(is, p, perm)) == null || !(shopItem.getSellPrice() >= 0.0)) continue;
                int qty = is.getAmount();
                if (shopItem.isMaxSell(bCACHE.getOrDefault(shopItem, new Sold()).getAmount() + CACHE.getOrDefault(shopItem, new Sold()).getAmount() + qty)) {
                    if (bCACHE.getOrDefault(shopItem, new Sold()).getAmount() + CACHE.getOrDefault(shopItem, new Sold()).getAmount() >= shopItem.getMaxSell()) continue;
                    qty = shopItem.getMaxSell() - (bCACHE.getOrDefault(shopItem, new Sold()).getAmount() + CACHE.getOrDefault(shopItem, new Sold()).getAmount());
                }
                Sold sold2 = bCACHE.computeIfAbsent(shopItem, k -> new Sold());
                sold2.addValues(qty, shopItem.getSellPrice(p, is, qty));
                removed += qty;
                ItemStack stack = new ItemStack(is);
                stack.setAmount(is.getAmount() - qty);
                items.put(i, stack);
                sold2.addIndex(i);
            }
            for (Map.Entry<?, ?> sold3 : new HashSet<>(bCACHE.entrySet())) {
                if (!((ShopItem)sold3.getKey()).isMinSell(((Sold)sold3.getValue()).getAmount())) continue;
                bCACHE.remove(sold3.getKey());
                removed -= ((Sold)sold3.getValue()).getAmount();
                ((Sold)sold3.getValue()).getIndexes().forEach(items::remove);
            }
            if (!bCACHE.isEmpty()) {
                toRemove.put(slot, new SmartStack.Shulker(box, items));
                bCACHE.forEach((item, sold) -> CACHE.merge((ShopItem)item, (Sold)sold, Sold::addValue));
                return removed;
            }
        }
        return null;
    }

    private void removeItems(Inventory inv, Map<Integer, SmartStack> items) {
        items.forEach((i, stack) -> {
            if (stack instanceof SmartStack.Shulker) {
                this.emptyShulker((SmartStack.Shulker)stack);
            } else if (stack.getItem().getAmount() != 0) {
                inv.setItem(i.intValue(), stack.getItem());
            } else {
                inv.clear(i.intValue());
            }
        });
    }

    private void emptyShulker(SmartStack.Shulker shulker) {
        ItemStack sBox = shulker.getItem();
        BlockStateMeta sbm = (BlockStateMeta)sBox.getItemMeta();
        ShulkerBox s = (ShulkerBox)sbm.getBlockState();
        shulker.getItems().forEach((i, item) -> {
            if (item.getAmount() != 0) {
                s.getInventory().setItem(i.intValue(), item);
            } else {
                s.getInventory().clear(i.intValue());
            }
        });
        sbm.setBlockState((BlockState)s);
        sBox.setItemMeta((ItemMeta)sbm);
    }

    private Map<EcoType, Double> getPrices(Map<EcoType, Double> prices, Map<ShopItem, Sold> CACHE) {
        for (Map.Entry<ShopItem, Sold> entry : CACHE.entrySet()) {
            prices.merge(entry.getKey().getEcoType(), entry.getValue().getPrice(), Double::sum);
        }
        return prices;
    }

    private Map<ShopItem, Integer> getItems(Map<ShopItem, Integer> items, Map<ShopItem, Sold> CACHE) {
        for (Map.Entry<ShopItem, Sold> entry : CACHE.entrySet()) {
            items.merge(entry.getKey(), entry.getValue().getAmount(), Integer::sum);
        }
        return items;
    }

    private Map<EcoType, Double> getPrices(Map<ShopItem, Sold> CACHE) {
        HashMap<EcoType, Double> prices = new HashMap<EcoType, Double>();
        for (Map.Entry<ShopItem, Sold> entry : CACHE.entrySet()) {
            prices.merge(entry.getKey().getEcoType(), entry.getValue().getPrice(), Double::sum);
        }
        return prices;
    }

    private Map<ShopItem, Integer> getItems(Map<ShopItem, Sold> CACHE) {
        HashMap<ShopItem, Integer> items = new HashMap<ShopItem, Integer>();
        for (Map.Entry<ShopItem, Sold> entry : CACHE.entrySet()) {
            items.merge(entry.getKey(), entry.getValue().getAmount(), Integer::sum);
        }
        return items;
    }

    private double callPreTransactionEvent(ShopItem shopItem, Player player, int quantity, double sellPrice) {
        PreTransactionEvent preTransactionEvent = new PreTransactionEvent(shopItem, player, quantity, sellPrice, Transaction.Type.SELL_ALL_COMMAND);
        Bukkit.getPluginManager().callEvent((Event)preTransactionEvent);
        sellPrice = preTransactionEvent.getPrice();
        return preTransactionEvent.isCancelled() ? -1.0 : sellPrice;
    }

    private Map<EcoType, Double> callPreTransactionEvent(Map<ShopItem, Integer> items, Map<EcoType, Double> prices, Player player, int amount) {
        PreTransactionEvent preTransactionEvent = new PreTransactionEvent(items, prices, player, amount, Transaction.Type.SELL_ALL_COMMAND);
        Bukkit.getPluginManager().callEvent((Event)preTransactionEvent);
        return preTransactionEvent.isCancelled() ? null : preTransactionEvent.getPrices();
    }

    private String getCommandUsages(Player p) {
        StringBuilder builder = new StringBuilder();
        if (PermissionsCache.hasPermission(p, "DynamicShopGUIPlus.sellall")) {
            builder.append(Lang.SELLALL_INVENTORY_COMMAND_USAGE.get(p).getLegacy()).append("\n");
        }
        if (PermissionsCache.hasPermission(p, "DynamicShopGUIPlus.sellallhand")) {
            builder.append(Lang.SELLALL_HAND_COMMAND_USAGE.get(p).getLegacy()).append("\n");
        }
        if (PermissionsCache.hasPermission(p, "DynamicShopGUIPlus.sellallitem")) {
            builder.append(Lang.SELLALL_ITEM_COMMAND_USAGE.get(p).getLegacy()).append("\n");
        }
        return builder.toString();
    }

    @Override
    public List<String> tabComplete(CommandSender commandSender, String s, String[] args) {
        if (!(PermissionsCache.hasPermission(commandSender, "DynamicShopGUIPlus.sellall") && PermissionsCache.hasPermission(commandSender, "DynamicShopGUIPlus.sellallhand") && PermissionsCache.hasPermission(commandSender, "DynamicShopGUIPlus.sellallitem"))) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            ArrayList<String> tabCompletions = new ArrayList<String>(this.plugin.getSupportedMatNames());
            tabCompletions.add("hand");
            tabCompletions.add("inventory");
            if (!args[0].isEmpty()) {
                ArrayList<String> completions = new ArrayList<String>();
                StringUtil.copyPartialMatches((String)args[0], tabCompletions, completions);
                Collections.sort(completions);
                return completions;
            }
            return tabCompletions;
        }
        if (args.length == 2) {
            if (!args[1].isEmpty()) {
                ArrayList<String> completions = new ArrayList<String>();
                StringUtil.copyPartialMatches((String)args[1], Arrays.asList("8", "16", "32", "64", "128", "256", "512", "1024"), completions);
                Collections.sort(completions);
                return completions;
            }
            return Arrays.asList("8", "16", "32", "64", "128", "256", "512", "1024");
        }
        return Collections.emptyList();
    }
}

