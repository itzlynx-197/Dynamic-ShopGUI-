

package com.siliconstudios.dynamicshopguiplus.objects;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.files.Translatable;
import com.siliconstudios.dynamicshopguiplus.providers.UserManager;
import org.bukkit.entity.Player;

public class GUITitle {
    private final Map<Locale, Title> titles = new HashMap<Locale, Title>();

    public GUITitle(Lang title, String bedrockKey) {
        for (Locale locale : Lang.getLocales()) {
            Translatable resolved = title.get(locale);
            Translatable bedrock = Lang.getOrNull(bedrockKey, locale);
            this.titles.put(locale, new Title(resolved, bedrock == null ? resolved : bedrock));
        }
    }

    public Translatable getTitle(Player p) {
        Title title = this.titles.getOrDefault(UserManager.getUser(p).getLocale(), this.titles.get(Lang.getDefaultLocale()));
        if (UserManager.getUser(p).isBedrock()) {
            return title.bedrockTitle;
        }
        return title.title;
    }

    private static final class Title {
        private final Translatable title;
        private final Translatable bedrockTitle;

        private Title(Translatable title, Translatable bedrockTitle) {
            this.title = title;
            this.bedrockTitle = bedrockTitle;
        }
    }
}

