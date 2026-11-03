

package com.siliconstudios.dynamicshopguiplus.events;

import java.util.Locale;
import com.siliconstudios.dynamicshopguiplus.events.JoinEvent;
import com.siliconstudios.dynamicshopguiplus.objects.User;
import com.siliconstudios.dynamicshopguiplus.providers.UserManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLocaleChangeEvent;

public class LocaleChangeEvent
implements Listener {
    @EventHandler
    public void onLocaleChange(PlayerLocaleChangeEvent e) {
        try {
            if (JoinEvent.isToLoad(e.getPlayer().getUniqueId())) {
                return;
            }
        }
        catch (NullPointerException ignored) {
            return;
        }
        User user = UserManager.getUser(e.getPlayer());
        if (user != null) {
            Locale locale = UserManager.getLocale(e.getPlayer(), e.getLocale());
            user.setLocale(locale);
        } else {
            UserManager.loadUser(e.getPlayer());
        }
    }
}

