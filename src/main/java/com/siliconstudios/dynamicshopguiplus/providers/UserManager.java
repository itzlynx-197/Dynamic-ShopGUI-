

package com.siliconstudios.dynamicshopguiplus.providers;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.objects.User;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import protocolsupport.api.ProtocolSupportAPI;
import protocolsupport.api.ProtocolVersion;

public class UserManager {
    private final DynamicShopGUIPlus plugin;
    private static final ArrayList<Integer> versions = new ArrayList();
    private static ViaAPI vv;
    private static FloodgateApi fAPI;
    private static String prefix;
    private static boolean ps;
    private static final HashMap<UUID, User> USER_CACHE;

    public UserManager(DynamicShopGUIPlus plugin) {
        this.plugin = plugin;
    }

    public void init() {
        if (this.plugin.getServer().getPluginManager().getPlugin("ViaVersion") != null) {
            vv = Via.getAPI();
            this.plugin.runTaskLater(() -> versions.addAll((Collection<Integer>)vv.getServerVersion().supportedVersions()), 5L);
        }
        if (this.plugin.getServer().getPluginManager().getPlugin("ProtocolSupport") != null) {
            versions.addAll(Arrays.stream(ProtocolVersion.getAllSupported()).map(ProtocolVersion::getId).collect(Collectors.toList()));
            ps = true;
        }
        if (this.plugin.getServer().getPluginManager().isPluginEnabled("floodgate")) {
            fAPI = FloodgateApi.getInstance();
            prefix = this.getPrefix();
        }
    }

    private String getPrefix() {
        File conf = new File(DynamicShopGUIPlus.getInstance().getDataFolder().getParent() + "/floodgate/config.yml");
        if (!conf.exists()) {
            SendMessage.logDebugMessage("Failed to find player prefix for floodgate players, using default of '.'...");
            return ".";
        }
        YamlConfiguration config = DynamicShopGUIPlus.getInstance().loadConfiguration(conf, "floodgate config.yml");
        if (config == null) {
            return ".";
        }
        return config.getString("username-prefix", ".");
    }

    public static User getUser(Player p) {
        User user = USER_CACHE.get(p.getUniqueId());
        if (user == null) {
            USER_CACHE.put(p.getUniqueId(), new User(p));
        }
        return user;
    }

    public static void loadUser(Player p) {
        USER_CACHE.put(p.getUniqueId(), new User(p));
    }

    public static boolean isPrPlayer(Player p) {
        if (vv != null) {
            return !versions.contains(vv.getPlayerVersion(p.getUniqueId()));
        }
        if (false) {
            return false;
        }
        return false;
    }

    public static boolean isFloodgatePlayer(Player p) {
        if (fAPI != null) {
            return fAPI.isFloodgatePlayer(p.getUniqueId());
        }
        return p.getName().startsWith(prefix);
    }

    public static Locale getLocale(Player p, boolean bedrock) {
        return UserManager.getLocale(p, bedrock ? UserManager.getBedrockLocale(p) : p.getLocale());
    }

    public static Locale getLocale(Player p, String s) {
        Locale locale = Locale.forLanguageTag(s.replace("_", "-"));
        if (locale.getLanguage().isEmpty()) {
            SendMessage.warnMessage("Failed to parse locale of player " + p.getName() + " for '" + s + "', using server default...");
            return Lang.getDefaultLocale();
        }
        Locale matchedCountry = null;
        for (Locale supported : Lang.getLocales()) {
            if (supported.equals(locale)) {
                return locale;
            }
            if (!supported.getCountry().isEmpty() || !supported.getLanguage().equals(locale.getLanguage())) continue;
            matchedCountry = supported;
        }
        if (matchedCountry != null) {
            return matchedCountry;
        }
        SendMessage.logDebugMessage("Locale of player " + p.getName() + " for '" + locale + "' is not supported by plugin, using plugin default...");
        return Lang.getDefaultLocale();
    }

    public static String getBedrockLocale(Player p) {
        if (fAPI != null) {
            return p.getLocale();
        }
        return p.getLocale();
    }

    public void reloadPlayerData() {
        this.plugin.getServer().getOnlinePlayers().forEach(UserManager::loadUser);
    }

    static {
        prefix = ".";
        ps = false;
        USER_CACHE = new HashMap();
    }
}

