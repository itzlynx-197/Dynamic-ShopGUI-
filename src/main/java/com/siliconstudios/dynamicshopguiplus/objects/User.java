

package com.siliconstudios.dynamicshopguiplus.objects;

import java.util.Locale;
import java.util.UUID;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.events.MenuHandler;
import com.siliconstudios.dynamicshopguiplus.providers.UserManager;
import org.bukkit.entity.Player;

public class User {
    private final UUID uuid;
    private final boolean bedrock;
    private final boolean pr;
    private Locale locale;
    private boolean openNewGUI;

    public User(Player player) {
        this.uuid = player.getUniqueId();
        this.pr = UserManager.isPrPlayer(player);
        this.bedrock = UserManager.isFloodgatePlayer(player);
        this.locale = UserManager.getLocale(player, this.bedrock);
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public boolean isBedrock() {
        return this.bedrock;
    }

    public boolean isPr() {
        return this.pr;
    }

    public void setOpenNewGUI(boolean openNewGUI) {
        this.openNewGUI = openNewGUI;
        if (MenuHandler.backOnClose && openNewGUI) {
            DynamicShopGUIPlus.getInstance().runTaskLater(() -> this.setOpenNewGUI(false), 1L);
        }
    }

    public boolean isOpenNewGUI() {
        return this.openNewGUI;
    }
}

