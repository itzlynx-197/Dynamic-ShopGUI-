

package com.siliconstudios.dynamicshopguiplus.objects.mappings;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.objects.mappings.ClickAction;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.ClickType;

public class ClickMappings {
    private final Map<ClickType, ClickAction> mappings = new HashMap<ClickType, ClickAction>();

    public ClickMappings(String section) {
        HashMap<ClickAction, ClickType> settings = new HashMap<ClickAction, ClickType>();
        ClickAction[] actions = new ClickAction[]{ClickAction.BUY, ClickAction.SELL, ClickAction.SELL_ALL};
        ClickType[] types = new ClickType[]{ClickType.LEFT, ClickType.SHIFT_LEFT, ClickType.RIGHT, ClickType.SHIFT_RIGHT, ClickType.UNKNOWN};
        ConfigurationSection mappings = (section != null ? ConfigManager.getSection(section) : ConfigManager.getConfig()).getConfigurationSection("click-mappings");
        for (String string : mappings.getKeys(false)) {
            ClickAction action = null;
            ClickType click = null;
            try {
                action = ClickAction.valueOf(string);
            }
            catch (IllegalArgumentException e) {
                SendMessage.warnMessage(Lang.INVALID_CLICK_ACTION.get().replace("%click-action%", string).replace("%click-mapping%", string + ": " + mappings.getString(string)));
                if (section != null) {
                    SendMessage.errorSections(section, "click-mappings");
                }
                SendMessage.errorItemConfig("click-mappings");
            }
            try {
                String s = mappings.getString(string, "NONE");
                if (s.equals("NONE")) {
                    click = ClickType.UNKNOWN;
                } else {
                    ClickType finalClick = click = ClickType.valueOf((String)mappings.getString(string, "null"));
                    if (Arrays.stream(types).noneMatch(t -> t == finalClick)) {
                        throw new IllegalArgumentException();
                    }
                }
            }
            catch (IllegalArgumentException e) {
                click = null;
                SendMessage.warnMessage(Lang.INVALID_CLICK_TYPE.get().replace("%click-type%", mappings.getString(string, "null")).replace("%click-mapping%", string + ": " + mappings.getString(string)));
                if (section != null) {
                    SendMessage.errorSections(section, "click-mappings");
                }
                SendMessage.errorItemConfig("click-mappings");
            }
            ConfigurationSection def = ConfigManager.getConfig().getDef().getConfigurationSection("click-mappings");
            if (click == null && action != null) {
                click = ClickType.valueOf((String)ConfigManager.getConfig().getDef().getString("click-mappings." + (Object)((Object)action)));
            }
            if (action == null && click != null) {
                for (String k : def.getKeys(false)) {
                    if (!def.getString(k).equals(click.name())) continue;
                    action = ClickAction.valueOf(k);
                }
            }
            settings.put(action, click);
        }
        if (!settings.containsKey((Object)ClickAction.BUY)) {
            settings.put(ClickAction.BUY, Arrays.stream(types).filter(t -> !settings.containsValue(t)).findFirst().orElse(ClickType.UNKNOWN));
        }
        if (!settings.containsKey((Object)ClickAction.SELL)) {
            settings.put(ClickAction.SELL, Arrays.stream(types).filter(t -> !settings.containsValue(t)).findFirst().orElse(ClickType.UNKNOWN));
        }
        if (!settings.containsKey((Object)ClickAction.SELL_ALL)) {
            settings.put(ClickAction.SELL_ALL, Arrays.stream(types).filter(t -> !settings.containsValue(t)).findFirst().orElse(ClickType.UNKNOWN));
        }
        for (Map.Entry<?, ?> entry : new HashSet<>(settings.entrySet())) {
            if (entry.getValue() != ClickType.UNKNOWN) continue;
            settings.remove(entry.getKey());
        }
        settings.forEach((key, value) -> this.mappings.put((ClickType)value, (ClickAction)((Object)((Object)key))));
    }

    public ClickAction getAction(ClickType clickType) {
        return this.mappings.get(clickType);
    }
}

