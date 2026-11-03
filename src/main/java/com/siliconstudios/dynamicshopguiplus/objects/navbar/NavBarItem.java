

package com.siliconstudios.dynamicshopguiplus.objects.navbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.files.Translatable;
import com.siliconstudios.dynamicshopguiplus.objects.navbar.NavItem;
import com.siliconstudios.dynamicshopguiplus.providers.UserManager;
import com.siliconstudios.dynamicshopguiplus.util.EcoType;
import com.siliconstudios.dynamicshopguiplus.util.LocalizedItemStack;
import com.siliconstudios.dynamicshopguiplus.util.Replacement;
import com.siliconstudios.dynamicshopguiplus.util.XMaterial;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.ItemLoadException;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class NavBarItem
implements NavItem {
    private LocalizedItemStack navBarItem;
    private String action = null;
    private boolean staticItem = true;
    private boolean onlyGeyser;
    private boolean disabled;
    private Paginated paginatedItem;
    private Map<Locale, List<Integer>> placeholders;
    private String bedrockTexture;

    @Override
    public String getAction() {
        return this.action;
    }

    @Override
    public String getAction(int page, int allPages) {
        return this.paginatedItem != null && (allPages == 1 || this.action.equals("PAGE_BACK") && page == 1 || this.action.equals("PAGE_NEXT") && page == allPages) ? this.paginatedItem.getA() : this.action;
    }

    @Override
    public boolean isDisabled() {
        return this.disabled;
    }

    @Override
    public boolean isOnlyGeyser() {
        return this.onlyGeyser;
    }

    @Override
    public boolean isStatic(int page, int allPages) {
        return this.staticItem && (this.paginatedItem == null || allPages == 1 || page != 1 && page != 2 && page != allPages - 1 && page != allPages);
    }

    @Override
    public String getBedrockTexture() {
        return this.bedrockTexture;
    }

    @Override
    public String getBedrockTexture(int page, int allpages) {
        if (this.paginatedItem != null && (allpages == 1 || this.action.equals("PAGE_BACK") && page == 1 || this.action.equals("PAGE_NEXT") && page == allpages)) {
            return this.paginatedItem.getBedrockTexture();
        }
        return this.bedrockTexture;
    }

    public NavBarItem(ItemStack item, String action) {
        this.navBarItem = new LocalizedItemStack(item);
        this.action = action;
    }

    public NavBarItem(ConfigurationSection config, ItemStack fillItem) throws ItemLoadException {
        LocalizedItemStack item = DynamicShopGUIPlus.getInstance().createItem.createFillItem(null, config);
        if (item == null || item.getItem().getType().equals((Object)Material.AIR)) {
            this.disabled = true;
            return;
        }
        this.placeholders = this.getPlaceholders(item);
        if (this.placeholders.values().stream().anyMatch(l -> !l.isEmpty())) {
            this.staticItem = false;
        }
        if (config.getBoolean("paginated.enabled")) {
            ConfigurationSection section = config.getConfigurationSection("paginated.orElse");
            this.paginatedItem = section != null && !section.getKeys(false).isEmpty() ? new Paginated(section) : new Paginated(fillItem);
            this.staticItem = false;
        }
        this.action = this.getAction(config, fillItem);
        this.navBarItem = item;
        this.bedrockTexture = config.getString("bedrock-texture");
    }

    private Translatable replaceTranslations(String s) {
        return Lang.fromConfig(s.replace("%translations-previous-page%", Lang.PREVIOUS_PAGE.get().toString()).replace("%translations-current-page%", Lang.CURRENT_PAGE.get().getLegacy().contains("%page%") ? Lang.CURRENT_PAGE.get().toString() : Lang.CURRENT_PAGE.get().builder().append(" %page%/%pages%").build().toString()).replace("%translations-next-page%", Lang.NEXT_PAGE.get().toString()).replace("%translations-money%", Lang.MONEY.get().toString()).replace("%translations-cancel%", Lang.CANCEL.get().toString()).replace("%translations-back%", Lang.BACK.get().toString()));
    }

    private String getAction(ConfigurationSection section, ItemStack fillItem) {
        if (!section.contains("action")) {
            return "NONE";
        }
        switch (section.getString("action").toUpperCase(Locale.ENGLISH)) {
            case "TOGGLE_MODE": {
                this.onlyGeyser = section.getBoolean("onlyEnableForGeyserPlayers", true);
            }
            case "PAGE_BACK": 
            case "PAGE_NEXT": {
                if (this.paginatedItem == null && section.getBoolean("paginated.enabled", true)) {
                    this.paginatedItem = new Paginated(fillItem);
                    this.staticItem = false;
                }
            }
            case "CLOSE": 
            case "BACK": {
                return section.getString("action").toUpperCase(Locale.ENGLISH);
            }
        }
        return "NONE";
    }

    @Override
    public ItemStack getItem(Player p, EcoType type, int page, int allpages) {
        if (this.staticItem) {
            return this.navBarItem == null ? null : this.navBarItem.getItem(p);
        }
        if (this.paginatedItem != null && (allpages == 1 || this.action.equals("PAGE_BACK") && page == 1 || this.action.equals("PAGE_NEXT") && page == allpages)) {
            return this.paginatedItem.getItem(p, type, page, allpages);
        }
        ItemStack item = this.navBarItem.getItem(p);
        if (!item.hasItemMeta()) {
            return item;
        }
        ItemMeta meta = item.getItemMeta();
        List<Integer> placeholders = NavBarItem.getPlaceholdersFor(this.placeholders, p);
        if (placeholders.contains(-1)) {
            DynamicShopGUIPlus.getInstance().getMetaUtils().setRawDisplayName(meta, this.replacePlaceHolders(p, DynamicShopGUIPlus.getInstance().getMetaUtils().getRawDisplayName(meta), type, page, allpages));
        }
        if (meta.hasLore()) {
            List<String> lore = DynamicShopGUIPlus.getInstance().getMetaUtils().getRawLore(meta);
            for (Integer i : placeholders) {
                if (i == -1) continue;
                lore.set(i, this.replacePlaceHolders(p, lore.get(i), type, page, allpages));
            }
            DynamicShopGUIPlus.getInstance().getMetaUtils().setRawLore(meta, lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public ItemStack getItem(Player p, EcoType type) {
        if (this.onlyGeyser && !UserManager.getUser(p).isBedrock()) {
            return this.paginatedItem != null ? this.paginatedItem.getItem(p, type, 0, 0) : new ItemStack(Material.AIR);
        }
        if (this.staticItem) {
            return this.navBarItem == null ? null : this.navBarItem.getItem(p);
        }
        ItemStack item = this.navBarItem.getItem(p);
        if (!item.hasItemMeta()) {
            return item;
        }
        ItemMeta meta = item.getItemMeta();
        List<Integer> placeholders = NavBarItem.getPlaceholdersFor(this.placeholders, p);
        if (placeholders.contains(-1)) {
            DynamicShopGUIPlus.getInstance().getMetaUtils().setRawDisplayName(meta, this.replacePlaceHolders(p, DynamicShopGUIPlus.getInstance().getMetaUtils().getRawDisplayName(meta), type, 0, 0));
        }
        if (meta.hasLore()) {
            List<String> lore = DynamicShopGUIPlus.getInstance().getMetaUtils().getRawLore(meta);
            for (Integer i : placeholders) {
                if (i == -1) continue;
                lore.set(i, this.replacePlaceHolders(p, lore.get(i), type, 0, 0));
            }
            DynamicShopGUIPlus.getInstance().getMetaUtils().setRawLore(meta, lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    private boolean hasPlaceholder(String s) {
        return s.contains("%player_name%") || s.contains("player_displayname") || s.contains("%player_level%") || s.contains("%player_balance%") || s.contains("%page%") || s.contains("%pages%");
    }

    private Map<Locale, List<Integer>> getPlaceholders(LocalizedItemStack item) {
        HashMap<Locale, List<Integer>> placeholders = new HashMap<Locale, List<Integer>>();
        for (Map.Entry<Locale, ItemStack> entry : item.getVariants().entrySet()) {
            placeholders.put(entry.getKey(), this.getPlaceholders(entry.getValue()));
        }
        return placeholders;
    }

    private List<Integer> getPlaceholders(ItemStack item) {
        String displayName;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName() && !item.getItemMeta().hasLore()) {
            return Collections.EMPTY_LIST;
        }
        ItemMeta meta = item.getItemMeta();
        ArrayList<Integer> placeholders = new ArrayList<Integer>();
        if (meta.hasDisplayName() && this.hasPlaceholder(displayName = DynamicShopGUIPlus.getInstance().getMetaUtils().getRawDisplayName(meta))) {
            placeholders.add(-1);
        }
        if (meta.hasLore()) {
            int i = 0;
            for (String s : DynamicShopGUIPlus.getInstance().getMetaUtils().getRawLore(meta)) {
                if (this.hasPlaceholder(s)) {
                    placeholders.add(i);
                }
                ++i;
            }
        }
        return placeholders;
    }

    private static List<Integer> getPlaceholdersFor(Map<Locale, List<Integer>> placeholders, Player p) {
        if (placeholders == null) {
            return Collections.EMPTY_LIST;
        }
        List<Integer> list = placeholders.get(UserManager.getUser(p).getLocale());
        return list != null ? list : placeholders.getOrDefault(Lang.getDefaultLocale(), Collections.EMPTY_LIST);
    }

    private Replacement[] getReplacements(Player p, EcoType type, int page, int pages) {
        Replacement[] replacementArray = new Replacement[6];
        replacementArray[0] = new Replacement("%player_name%", () -> ((Player)p).getName());
        replacementArray[1] = new Replacement("%player_displayname%", () -> DynamicShopGUIPlus.getInstance().getDisplayName(p));
        replacementArray[2] = new Replacement("%player_level%", () -> String.valueOf(p.getLevel()));
        replacementArray[3] = new Replacement("%player_balance%", () -> DynamicShopGUIPlus.getInstance().formatPrice(type, DynamicShopGUIPlus.getInstance().getEcoHandler().getEcon(type).getBalance((OfflinePlayer)p)));
        replacementArray[4] = new Replacement("%page%", () -> String.valueOf(page));
        replacementArray[5] = new Replacement("%pages%", () -> String.valueOf(pages));
        return replacementArray;
    }

    private String replacePlaceHolders(Player p, String s, EcoType type, int page, int pages) {
        for (Replacement replacement : this.getReplacements(p, type, page, pages)) {
            if (!s.contains(replacement.key)) continue;
            s = s.replace(replacement.key, replacement.value.get());
        }
        return s;
    }

    public class Paginated {
        private LocalizedItemStack orElse;
        private String action = null;
        private String skullOwner;
        private boolean staticItem = true;
        private boolean disabled = false;
        private Map<Locale, List<Integer>> placeholders;
        private String bedrockTexture;

        public Paginated(ItemStack fillItem) {
            if (fillItem == null) {
                this.disabled = true;
                this.orElse = new LocalizedItemStack(new ItemStack(Material.AIR));
            } else {
                this.orElse = new LocalizedItemStack(fillItem);
            }
            this.action = "NONE";
        }

        public Paginated(ConfigurationSection config) throws ItemLoadException {
            LocalizedItemStack item = DynamicShopGUIPlus.getInstance().createItem.createFillItem(null, config);
            if (item == null || item.getItem().getType().equals((Object)Material.AIR)) {
                this.disabled = true;
                return;
            }
            this.placeholders = NavBarItem.this.getPlaceholders(item);
            if (this.placeholders.values().stream().anyMatch(l -> !l.isEmpty())) {
                this.staticItem = false;
            }
            this.action = NavBarItem.this.getAction(config, null);
            this.orElse = item;
            this.bedrockTexture = config.getString("bedrock-texture");
        }

        public boolean isDisabled() {
            return this.disabled;
        }

        public String getA() {
            return this.action;
        }

        public boolean isStatic() {
            return this.staticItem;
        }

        public String getBedrockTexture() {
            return this.bedrockTexture;
        }

        public ItemStack getItem(Player p, EcoType type, int page, int allpages) {
            if (this.staticItem) {
                return this.orElse == null ? null : this.orElse.getItem(p);
            }
            ItemStack item = this.orElse.getItem(p);
            if (!item.hasItemMeta()) {
                return item;
            }
            if (item.getType().equals((Object)XMaterial.PLAYER_HEAD.parseMaterial())) {
                SkullMeta skullMeta = (SkullMeta)item.getItemMeta();
                skullMeta.setOwner(this.skullOwner == null ? p.getName() : this.skullOwner);
                item.setItemMeta((ItemMeta)skullMeta);
            }
            ItemMeta meta = item.getItemMeta();
            List<Integer> placeholders = NavBarItem.getPlaceholdersFor(this.placeholders, p);
            if (placeholders.contains(-1)) {
                DynamicShopGUIPlus.getInstance().getMetaUtils().setRawDisplayName(meta, NavBarItem.this.replacePlaceHolders(p, DynamicShopGUIPlus.getInstance().getMetaUtils().getRawDisplayName(meta), type, page, allpages));
            }
            if (meta.hasLore()) {
                List<String> lore = DynamicShopGUIPlus.getInstance().getMetaUtils().getRawLore(meta);
                for (Integer i : placeholders) {
                    if (i == -1) continue;
                    lore.set(i, NavBarItem.this.replacePlaceHolders(p, lore.get(i), type, page, allpages));
                }
                DynamicShopGUIPlus.getInstance().getMetaUtils().setRawLore(meta, lore);
            }
            item.setItemMeta(meta);
            return item;
        }
    }
}

