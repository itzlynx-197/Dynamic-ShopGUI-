

package com.siliconstudios.dynamicshopguiplus.events;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.providers.UserManager;
import com.siliconstudios.dynamicshopguiplus.util.PermissionsCache;
import com.siliconstudios.dynamicshopguiplus.util.SkullUtil;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinEvent
implements Listener {
    private final DynamicShopGUIPlus plugin;
    private final boolean joinMessage;
    private static final Set<UUID> toLoad = new HashSet<UUID>();

    public JoinEvent(DynamicShopGUIPlus plugin, boolean joinMessage) {
        this.plugin = plugin;
        this.joinMessage = joinMessage;
    }

    public static boolean isToLoad(UUID uuid) {
        return toLoad.contains(uuid);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (this.plugin.updateChecker != null && PermissionsCache.hasPermission(player, "DynamicShopGUIPlus.notifyUpdate")) {
            this.checkForUpdates(player);
        }
        if (this.joinMessage) {
            this.sendJoinMessage(player);
        }
        SkullUtil.updateTexture(e.getPlayer());
        toLoad.add(player.getUniqueId());
        this.plugin.runTaskLater(() -> {
            if (toLoad.contains(player.getUniqueId())) {
                UserManager.getUser(player);
            }
        }, 5L);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        toLoad.remove(e.getPlayer().getUniqueId());
    }

    private void checkForUpdates(Player p) {
        if (this.plugin.updateChecker.updateAvailable) {
            this.plugin.runTaskLater(() -> {
                SendMessage.infoMessage((Object)p, Lang.UPDATE_AVAILABLE.get(p).replace("%plugin_version%", this.plugin.updateChecker.CURRENT_VERSION.getVer()).replace("%latest_version%", this.plugin.updateChecker.LATEST_VERSION.getVer()));
            }, 5L);
        }
    }

    private void sendJoinMessage(Player p) {
        OfflinePlayer offlinePlayer = this.plugin.getServer().getOfflinePlayer(p.getUniqueId());
        if (offlinePlayer.hasPlayedBefore()) {
            SendMessage.chatToPlayer(p, Lang.JOIN_MESSAGE.get(p).replace("%balance%", this.plugin.formatPrice(null, this.plugin.getEcoHandler().getDefaultProvider().getBalance((OfflinePlayer)p))));
        }
    }
}

