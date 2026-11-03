

package com.siliconstudios.dynamicshopguiplus.methodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.objects.User;
import com.siliconstudios.dynamicshopguiplus.util.ItemLayout;
import com.siliconstudios.dynamicshopguiplus.util.PermissionsCache;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class CalculateAmount {
    private static DynamicShopGUIPlus plugin;

    public CalculateAmount(DynamicShopGUIPlus plugin) {
        CalculateAmount.plugin = plugin;
    }

    public List<String> loadItemsInOrder(ItemLayout layout, String section, String page, Integer size, int defSize) {
        LinkedList<String> softItems = new LinkedList<String>();
        String path = "pages." + page + ".items";
        for (String itemLoc : ConfigManager.getShop(section).getConfigurationSection(path).getKeys(false)) {
            if (ConfigManager.getShop(section).getBoolean(path + "." + itemLoc + ".hidden")) continue;
            try {
                String s = ConfigManager.getShop(section).getString(path + "." + itemLoc + ".slot");
                ArrayList<Integer> slots = this.getSlots(s);
                if (slots != null && slots.stream().allMatch(i -> i >= 0)) {
                    for (int pos : slots) {
                        while (layout.containsKey(pos)) {
                            ++pos;
                        }
                        layout.set(pos, page + ".items." + itemLoc);
                    }
                    continue;
                }
                softItems.add(page + ".items." + itemLoc);
            }
            catch (NumberFormatException e) {
                SendMessage.warnMessage("Invalid slot index for format '" + ConfigManager.getShop(section).getString(path + "." + itemLoc + ".slot") + "' for item '" + section + "." + itemLoc + "'");
            }
        }
        for (String loc : softItems) {
            layout.add(loc);
        }
        return layout.build(size, defSize);
    }

    public ArrayList<Integer> getSlots(String slots) throws NumberFormatException {
        if (slots == null) {
            return null;
        }
        try {
            return new ArrayList<Integer>(Collections.singletonList(Integer.parseInt(slots) - 1));
        }
        catch (NumberFormatException e) {
            ArrayList<Integer> pos = new ArrayList<Integer>();
            slots = StringUtils.deleteWhitespace((String)slots);
            for (String index : slots.split(",")) {
                String[] params = index.split("-");
                if (params.length == 2) {
                    for (int i = Integer.parseInt(params[0]) - 1; i < Integer.parseInt(params[1]); ++i) {
                        pos.add(i);
                    }
                    continue;
                }
                if (params.length != 1) continue;
                pos.add(Integer.parseInt(params[0]) - 1);
            }
            if (!pos.isEmpty()) {
                return pos;
            }
            throw new NumberFormatException();
        }
    }

    public static double calculateDiscount(Player player, String section, double price) {
        double discount = 0.0;
        for (String group : plugin.getDiscounts(section).keySet()) {
            if (!PermissionsCache.hasPermission(player, "DynamicShopGUIPlus.discounts." + group)) continue;
            double i = plugin.getDiscounts(section).get(group);
            if (discount != 0.0 && !(discount < i)) continue;
            discount = i;
        }
        return price - price / 100.0 * discount;
    }

    public static double calculateMultiplier(Player player, String section, double price) {
        double multiplier = 0.0;
        for (String group : plugin.getMultipliers(section).keySet()) {
            if (!PermissionsCache.hasPermission(player, "DynamicShopGUIPlus.sell-multipliers." + group)) continue;
            double i = plugin.getMultipliers(section).get(group);
            if (multiplier != 0.0 && !(multiplier < i)) continue;
            multiplier = i;
        }
        return price + price / 100.0 * multiplier;
    }

    public static List<String> splitLongString(String string) {
        ArrayList<String> list = new ArrayList<String>();
        String[] words = string.split(" ");
        String[] colorlessWords = ChatColor.stripColor((String)string).split(" ");
        int length = 0;
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < colorlessWords.length; ++i) {
            String word = colorlessWords[i];
            if ((length += word.length() + 1) >= 46 || word.contains("\n")) {
                String l = line.toString();
                list.add(l);
                length = word.length();
                line = new StringBuilder();
                if (word.contains("\n")) continue;
                line.append(ChatColor.getLastColors((String)l)).append(words[i]);
                continue;
            }
            if (line.length() != 0) {
                line.append(" ");
            }
            line.append(words[i]);
        }
        list.add(line.toString());
        return list;
    }

    public static List<String> getMultipleItemLore(String ... lore) {
        ArrayList<String> lines = new ArrayList<String>();
        for (String line : lore) {
            if (line.contains("\n")) {
                lines.addAll(Arrays.asList(line.split("\n")));
                continue;
            }
            lines.add(line);
        }
        return lines;
    }

    public Map<Integer, String> getMainMenuItemSlots() {
        int maxSize = CalculateAmount.plugin.mainMenuSize;
        if (CalculateAmount.plugin.navBar.isEnableMainNav()) {
            maxSize -= 9;
        }
        ArrayList<Integer> cache = new ArrayList<Integer>();
        ArrayList<String> notSet = new ArrayList<String>();
        HashMap<Integer, String> itemSlots = new HashMap<Integer, String>();
        for (String section : plugin.getShopSections()) {
            ArrayList<Integer> slots;
            if (ConfigManager.getSection(section).getBoolean("hidden") || ConfigManager.getSection(section).getBoolean("sub-section")) continue;
            String value = ConfigManager.getSection(section).getString("slot");
            if (value == null) {
                notSet.add(section);
                continue;
            }
            try {
                slots = this.getSlots(value);
            }
            catch (NumberFormatException e) {
                notSet.add(section);
                continue;
            }
            ArrayList<Integer> valid = new ArrayList<Integer>();
            for (Integer slot : slots) {
                if (slot < 0 || slot >= maxSize) {
                    if (slots.size() == 1 || valid.isEmpty()) {
                        SendMessage.errorMessage(Lang.CANNOT_DISPLAY_SHOPSECTION.get().replace("%shopsection%", section).replace("%maxInvSize%", String.valueOf(maxSize)));
                        notSet.add(section);
                        continue;
                    }
                    SendMessage.errorMessage("Slot '%slot%' for shop %section% out of bounds for 0-%maxInvSize%, skipping slot...".replace("%section%", section).replace("%slot%", String.valueOf(slot)).replace("%maxInvSize%", String.valueOf(maxSize)));
                    continue;
                }
                if (cache.contains(slot)) {
                    if (slots.size() == 1 || valid.isEmpty()) {
                        notSet.add(section);
                        continue;
                    }
                    SendMessage.errorMessage("Slot '%slot%' for shop %section% is already used, skipping slot...".replace("%section%", section).replace("%slot%", String.valueOf(slot)));
                    continue;
                }
                cache.add(slot);
                itemSlots.put(slot, section);
                valid.add(slot);
            }
        }
        for (String section : notSet) {
            int place = 0;
            while (cache.contains(place)) {
                ++place;
            }
            if (place >= maxSize) {
                SendMessage.warnMessage(Lang.CANNOT_DISPLAY_SHOPSECTION.get().replace("%shopsection%", section).replace("%maxInvSize%", String.valueOf(maxSize)));
                continue;
            }
            itemSlots.put(place, section);
            cache.add(place);
            SendMessage.warnMessage("The place of shop " + section + " in the main shop-inventory has automatically been changed to '" + place + "' because it was already used or could not be found.");
        }
        return itemSlots;
    }

    public static int getInvSlotsDef(User user, int def) {
        return CalculateAmount.plugin.resizeGUI && user.isBedrock() ? 54 : def;
    }

    public static int getInvSlots(User user, int keys) {
        return CalculateAmount.plugin.resizeGUI && user.isBedrock() ? 54 : CalculateAmount.getSlots(keys);
    }

    public static int getInvSlots(int size) {
        int[] arr = new int[]{1, 2, 3, 4, 5, 6};
        if (size != 1 && size != 2 && size != 3 && size != 4 && size != 5 && size != 6) {
            int idx = 0;
            int dist = Math.abs(arr[0] - size);
            for (int i = 1; i < arr.length; ++i) {
                int cdist = Math.abs(arr[i] - size);
                if (cdist >= dist) continue;
                idx = i;
                dist = cdist;
            }
            size = arr[idx];
        }
        return size * 9;
    }

    public static Integer getSlots(Integer keys) {
        int slots = keys == 9 ? 18 : (keys < 9 && keys > 0 ? 18 : (keys == 18 ? 27 : (keys < 18 && keys > 9 ? 27 : (keys == 27 ? 36 : (keys < 27 && keys > 18 ? 36 : (keys == 36 ? 45 : (keys < 36 && keys > 27 ? 45 : (keys == 45 ? 54 : (keys < 45 && keys > 36 ? 54 : 54)))))))));
        return slots;
    }
}

