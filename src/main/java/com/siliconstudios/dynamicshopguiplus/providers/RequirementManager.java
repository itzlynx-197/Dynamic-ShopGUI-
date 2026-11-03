

package com.siliconstudios.dynamicshopguiplus.providers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.providers.UserManager;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.ItemRequirement;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.RequirementType;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.Requirements;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.items.TimeRequirement;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.items.quest.QuestManager;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.items.quest.QuestRequirement;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.items.region.RegionManager;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.items.region.RegionRequirement;
import com.siliconstudios.dynamicshopguiplus.util.ChatUtil;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.ItemRequirementLoadException;
import org.bukkit.entity.Player;

public class RequirementManager {
    private final DynamicShopGUIPlus plugin;
    private QuestManager questManager;
    private RegionManager regionManager;

    public RequirementManager(DynamicShopGUIPlus plugin) {
        this.plugin = plugin;
    }

    public Requirements load(String section, String item) {
        Requirements requirements = new Requirements();
        for (String s : ConfigManager.getShop(section).getStringList(item + ".requirements")) {
            RequirementType type;
            try {
                type = RequirementType.valueOf(s.split("::")[0]);
            }
            catch (IllegalArgumentException e) {
                SendMessage.warnMessage(Lang.INVALID_ITEM_REQUIREMENT_TYPE.get().replace("%type%", s.split("::")[0]));
                SendMessage.errorShops(section, item);
                continue;
            }
            try {
                String requirement = s.split("::")[1];
                List lore = ConfigManager.getConfig().getStringList(type.getLorePath());
                requirements.add(this.createRequirement(section + "." + item, type, requirement, lore));
            }
            catch (ItemRequirementLoadException e) {
                SendMessage.warnMessage(Lang.INVALID_ITEM_REQUIREMENT.get().replace("%requirement%", s) + ": " + e.getMessage());
                SendMessage.errorShops(section, item);
            }
        }
        return requirements.isEmpty() ? null : requirements;
    }

    private ItemRequirement createRequirement(String path, RequirementType type, String value, List<String> lore) throws ItemRequirementLoadException {
        switch (type) {
            case TIME: {
                return new TimeRequirement(value, lore);
            }
            case QUEST: {
                if (this.questManager == null) {
                    this.questManager = new QuestManager(this.plugin);
                }
                QuestRequirement questRequirement = new QuestRequirement(this.questManager.getProvider(), value, lore);
                return this.questManager.addRequirement(questRequirement, path);
            }
            case REGION: {
                if (this.regionManager == null) {
                    this.regionManager = new RegionManager(this.plugin);
                }
                RegionRequirement regionRequirement = new RegionRequirement(this.regionManager.getProvider(), value, lore);
                return this.regionManager.addRequirement(regionRequirement, path);
            }
        }
        return null;
    }

    public static Object getFormatted(RequirementType type, long requirement) {
        switch (type) {
            case TIME: {
                return requirement;
            }
        }
        return requirement;
    }

    public static RequirementLore getLore(List<String> list, String def, String ... formatted) {
        return DynamicShopGUIPlus.getInstance().paperMeta ? new CompRequirementLore(list, def, formatted) : new BukkitRequirementLore(list, def, formatted);
    }

    private static String translateLocal(String s, String ... formatted) {
        if (formatted.length == 1) {
            if (s.contains("%requirement%")) {
                s = s.replace("%requirement%", formatted[0]);
            }
        } else if (formatted.length == 2) {
            if (s.contains("%minTime%")) {
                s = s.replace("%minTime%", formatted[0]);
            }
            if (s.contains("%maxTime%")) {
                s = s.replace("%maxTime%", formatted[1]);
            }
        }
        return s;
    }

    private static LocalizedLore getLocalized(Map<Locale, LocalizedLore> lore, Player p) {
        LocalizedLore localized = lore.get(UserManager.getUser(p).getLocale());
        return localized != null ? localized : lore.get(Lang.getDefaultLocale());
    }

    private static List<String> translate(List<String> list, Locale locale) {
        if (list.isEmpty()) {
            return list;
        }
        ArrayList<String> lore = new ArrayList<String>();
        for (String s : list) {
            if (s.contains("%translations-")) {
                try {
                    Lang placeholder = Lang.valueOf(s.split("%translations-")[1].split("%")[0].toUpperCase(Locale.ENGLISH).replace("-", "_"));
                    s = s.replace("%translations-" + placeholder.getKey() + "%", placeholder.getRaw(locale).replace("\\n", "\n"));
                }
                catch (IllegalArgumentException illegalArgumentException) {
                    
                }
            }
            for (String e : s.split("\n")) {
                lore.add(DynamicShopGUIPlus.getInstance().paperMeta ? ChatUtil.getAdventureUtils().formatLegacyToMini(e) : ChatUtil.formatColors(e));
            }
        }
        return lore;
    }

    private static String translate(String s, Locale locale) {
        if (s == null) {
            return "";
        }
        if (s.contains("%translations-")) {
            try {
                Lang placeholder = Lang.valueOf(s.split("%translations-")[1].split("%")[0].toUpperCase(Locale.ENGLISH).replace("-", "_"));
                s = s.replace("%translations-" + placeholder.getKey() + "%", placeholder.getRaw(locale));
            }
            catch (IllegalArgumentException illegalArgumentException) {
                
            }
        }
        return DynamicShopGUIPlus.getInstance().paperMeta ? ChatUtil.getAdventureUtils().formatLegacyToMini(s) : ChatUtil.formatColors(s);
    }

    private static boolean isEmpty(List<String> list) {
        return list.stream().allMatch(String::isEmpty);
    }

    private static final class LocalizedLore {
        private final Map<Integer, String> placeholders = new HashMap<Integer, String>();
        private String[] lore;
        private String[] fastLore;

        private LocalizedLore(String[] lore, String[] fastLore) {
            this.lore = lore;
            this.fastLore = fastLore;
            int i = 0;
            for (String s : fastLore) {
                if (Arrays.stream(new String[]{"%requirement%", "%progress%"}).anyMatch(s::contains)) {
                    this.placeholders.put(i, s);
                }
                ++i;
            }
        }
    }

    private static final class CompRequirementLore
    implements RequirementLore {
        private final LocalizedLore lore;
        private final Map<Locale, LocalizedLore> localizedLore;
        private boolean replaced = false;

        public CompRequirementLore(List<String> list, String def, String ... formatted) {
            String[] lore = list != null ? (!RequirementManager.isEmpty(list) ? RequirementManager.translate(list, Lang.getDefaultLocale()).stream().map(s -> RequirementManager.translateLocal(s, formatted)).collect(Collectors.toList()).toArray(new String[list.size()]) : new String[]{}) : RequirementManager.translate(RequirementManager.translateLocal(def, formatted), Lang.getDefaultLocale()).split("\n");
            String[] compLore = (String[])Arrays.stream(lore).map(ChatUtil::getGsonComponent).toArray(String[]::new);
            this.lore = !DynamicShopGUIPlus.getInstance().perPlayerLanguages ? new LocalizedLore(lore, compLore) : null;
            this.localizedLore = DynamicShopGUIPlus.getInstance().perPlayerLanguages ? this.getLocalizedLore(list, def, formatted) : null;
        }

        private Map<Locale, LocalizedLore> getLocalizedLore(List<String> list, String def, String ... formatted) {
            HashMap<Locale, LocalizedLore> localizedLore = new HashMap<Locale, LocalizedLore>();
            for (Locale locale : Lang.getLocales()) {
                String[] lore = list != null ? (!RequirementManager.isEmpty(list) ? (String[])RequirementManager.translate(list, locale).stream().map(s -> RequirementManager.translateLocal(s, formatted)).toArray(String[]::new) : new String[]{}) : RequirementManager.translate(RequirementManager.translateLocal(def, formatted), locale).split("\n");
                String[] compLore = (String[])Arrays.stream(lore).map(ChatUtil::getGsonComponent).toArray(String[]::new);
                localizedLore.put(locale, new LocalizedLore(lore, compLore));
            }
            return localizedLore;
        }

        @Override
        public boolean isReplaced() {
            return this.replaced;
        }

        @Override
        public void replace(String target, String replacement) {
            if (this.lore != null) {
                for (int i = 0; i < this.lore.lore.length; ++i) {
                    if (!this.lore.lore[i].contains(target)) continue;
                    ((LocalizedLore)this.lore).lore[i] = this.lore.lore[i].replace(target, replacement);
                    ((LocalizedLore)this.lore).fastLore[i] = this.lore.fastLore[i].replace(target, replacement);
                    this.replaced = true;
                }
            } else {
                for (LocalizedLore localized : this.localizedLore.values()) {
                    for (int i = 0; i < localized.lore.length; ++i) {
                        if (!localized.lore[i].contains(target)) continue;
                        ((LocalizedLore)localized).lore[i] = localized.lore[i].replace(target, replacement);
                        ((LocalizedLore)localized).fastLore[i] = localized.fastLore[i].replace(target, replacement);
                        this.replaced = true;
                    }
                }
            }
        }

        @Override
        public String[] get(Player p, boolean fast, boolean pr, String ... replacements) {
            LocalizedLore localized = this.localizedLore != null ? RequirementManager.getLocalized(this.localizedLore, p) : this.lore;
            String[] clone = (String[])(fast && !pr ? localized.fastLore : localized.lore).clone();
            for (Map.Entry ph : localized.placeholders.entrySet()) {
                for (int i = 0; i < replacements.length; i += 2) {
                    if (!((String)ph.getValue()).contains(replacements[i])) continue;
                    clone[((Integer)ph.getKey()).intValue()] = clone[(Integer)ph.getKey()].replace(replacements[i], replacements[i + 1]);
                }
            }
            return clone;
        }

        @Override
        public String[] get(Player p, boolean nbt, boolean pr) {
            LocalizedLore localized = this.localizedLore != null ? RequirementManager.getLocalized(this.localizedLore, p) : this.lore;
            return nbt ? localized.fastLore : localized.lore;
        }
    }

    private static final class BukkitRequirementLore
    implements RequirementLore {
        private final LocalizedLore lore;
        private final Map<Locale, LocalizedLore> localizedLore;
        private boolean replaced = false;

        public BukkitRequirementLore(List<String> list, String def, String ... formatted) {
            String[] lore = list != null ? (!RequirementManager.isEmpty(list) ? RequirementManager.translate(list, Lang.getDefaultLocale()).stream().map(s -> RequirementManager.translateLocal(s, formatted)).collect(Collectors.toList()).toArray(new String[list.size()]) : new String[]{}) : RequirementManager.translate(RequirementManager.translateLocal(def, formatted), Lang.getDefaultLocale()).split("\n");
            String[] fastLore = (String[])Arrays.stream(lore).map(s -> DynamicShopGUIPlus.getInstance().versionHandler.toNBT((String)s)).toArray(String[]::new);
            this.lore = !DynamicShopGUIPlus.getInstance().perPlayerLanguages ? new LocalizedLore(lore, fastLore) : null;
            this.localizedLore = DynamicShopGUIPlus.getInstance().perPlayerLanguages ? this.getLocalizedLore(list, def, formatted) : null;
        }

        private Map<Locale, LocalizedLore> getLocalizedLore(List<String> list, String def, String ... formatted) {
            HashMap<Locale, LocalizedLore> localizedLore = new HashMap<Locale, LocalizedLore>();
            for (Locale locale : Lang.getLocales()) {
                String[] lore = list != null ? (!RequirementManager.isEmpty(list) ? (String[])RequirementManager.translate(list, locale).stream().map(s -> RequirementManager.translateLocal(s, formatted)).toArray(String[]::new) : new String[]{}) : RequirementManager.translate(RequirementManager.translateLocal(def, formatted), locale).split("\n");
                String[] fastLore = (String[])Arrays.stream(lore).map(s -> DynamicShopGUIPlus.getInstance().versionHandler.toNBT((String)s)).toArray(String[]::new);
                localizedLore.put(locale, new LocalizedLore(lore, fastLore));
            }
            return localizedLore;
        }

        @Override
        public boolean isReplaced() {
            return this.replaced;
        }

        @Override
        public void replace(String target, String replacement) {
            if (this.lore != null) {
                for (int i = 0; i < this.lore.lore.length; ++i) {
                    if (!this.lore.lore[i].contains(target)) continue;
                    ((LocalizedLore)this.lore).lore[i] = this.lore.lore[i].replace(target, replacement);
                    ((LocalizedLore)this.lore).fastLore[i] = this.lore.fastLore[i].replace(target, replacement);
                    this.replaced = true;
                }
            } else {
                for (LocalizedLore localized : this.localizedLore.values()) {
                    for (int i = 0; i < localized.lore.length; ++i) {
                        if (!localized.lore[i].contains(target)) continue;
                        ((LocalizedLore)localized).lore[i] = localized.lore[i].replace(target, replacement);
                        ((LocalizedLore)localized).fastLore[i] = localized.fastLore[i].replace(target, replacement);
                        this.replaced = true;
                    }
                }
            }
        }

        @Override
        public String[] get(Player p, boolean fast, boolean pr, String ... replacements) {
            LocalizedLore localized = this.localizedLore != null ? RequirementManager.getLocalized(this.localizedLore, p) : this.lore;
            String[] clone = (String[])(fast && !pr ? localized.fastLore : localized.lore).clone();
            for (Map.Entry ph : localized.placeholders.entrySet()) {
                for (int i = 0; i < replacements.length; i += 2) {
                    if (!((String)ph.getValue()).contains(replacements[i])) continue;
                    clone[((Integer)ph.getKey()).intValue()] = ((String)ph.getValue()).replace(replacements[i], replacements[i + 1]);
                }
            }
            return clone;
        }

        @Override
        public String[] get(Player p, boolean nbt, boolean pr) {
            LocalizedLore localized = this.localizedLore != null ? RequirementManager.getLocalized(this.localizedLore, p) : this.lore;
            return nbt && !pr ? localized.fastLore : localized.lore;
        }
    }

    public static interface RequirementLore {
        public boolean isReplaced();

        public void replace(String var1, String var2);

        public String[] get(Player var1, boolean var2, boolean var3, String ... var4);

        public String[] get(Player var1, boolean var2, boolean var3);
    }
}

