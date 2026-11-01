package com.silicon_studios.dynamic_shopgui_plus.listeners;

import com.silicon_studios.dynamic_shopgui_plus.DynamicShopGUIPlus;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class ProxyMessageListener implements PluginMessageListener {
    @Override
    public void onPluginMessageReceived(String channel, org.bukkit.entity.Player player, byte[] message) {
        if (!channel.equals("dynamicshopgui:command")) return;
        String cmd = new String(message, java.nio.charset.StandardCharsets.UTF_8);
        DynamicShopGUIPlus.getInstance().getServer().dispatchCommand(player, cmd);
    }
}