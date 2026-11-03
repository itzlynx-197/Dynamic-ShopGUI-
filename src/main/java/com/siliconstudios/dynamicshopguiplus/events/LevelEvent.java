

package com.siliconstudios.dynamicshopguiplus.events;

import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.files.Translatable;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;

public class LevelEvent
implements Listener {
    @EventHandler
    public void onPlayerLevelChange(PlayerLevelChangeEvent e) {
        Player player = e.getPlayer();
        double levelmoney5 = ConfigManager.getConfig().getDouble("leveleventmoney5/10/15");
        double levelmoney30 = ConfigManager.getConfig().getDouble("leveleventmoney20/25/30");
        Translatable message5 = Lang.LEVEL_EVENT_MESSAGE.get(player).replace("%amount%", DynamicShopGUIPlus.getInstance().formatPrice(null, levelmoney5));
        Translatable message30 = Lang.LEVEL_EVENT_MESSAGE.get(player).replace("%amount%", DynamicShopGUIPlus.getInstance().formatPrice(null, levelmoney30));
        if (ConfigManager.getConfig().getBoolean("enable-levelevent")) {
            if (player.getLevel() == 5) {
                DynamicShopGUIPlus.getInstance().getEcoHandler().getDefaultProvider().depositBalance((OfflinePlayer)player, levelmoney5);
                SendMessage.chatToPlayer(player, message5);
            } else if (player.getLevel() == 10) {
                DynamicShopGUIPlus.getInstance().getEcoHandler().getDefaultProvider().depositBalance((OfflinePlayer)player, levelmoney5);
                SendMessage.chatToPlayer(player, message5);
            } else if (player.getLevel() == 15) {
                DynamicShopGUIPlus.getInstance().getEcoHandler().getDefaultProvider().depositBalance((OfflinePlayer)player, levelmoney5);
                SendMessage.chatToPlayer(player, message5);
            } else if (player.getLevel() == 20) {
                DynamicShopGUIPlus.getInstance().getEcoHandler().getDefaultProvider().depositBalance((OfflinePlayer)player, levelmoney30);
                SendMessage.chatToPlayer(player, message30);
            } else if (player.getLevel() == 25) {
                DynamicShopGUIPlus.getInstance().getEcoHandler().getDefaultProvider().depositBalance((OfflinePlayer)player, levelmoney30);
                SendMessage.chatToPlayer(player, message30);
            } else if (player.getLevel() == 30) {
                DynamicShopGUIPlus.getInstance().getEcoHandler().getDefaultProvider().depositBalance((OfflinePlayer)player, levelmoney30);
                SendMessage.chatToPlayer(player, message30);
            }
        }
    }
}

