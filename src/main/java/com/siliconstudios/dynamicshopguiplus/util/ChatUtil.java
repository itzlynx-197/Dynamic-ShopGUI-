

package com.siliconstudios.dynamicshopguiplus.util;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.util.AdventureUtil;
import com.siliconstudios.dynamicshopguiplus.util.Gradient;
import com.siliconstudios.dynamicshopguiplus.util.ServerInfo;
import org.bukkit.ChatColor;

public class ChatUtil {
    private static final Pattern rgbPattern = Pattern.compile("#[a-fA-F0-9]{6}");
    private static final Pattern startGradientPattern = Pattern.compile("<gradient:#[a-fA-F0-9]{6}>");
    private static final Pattern endGradientPattern = Pattern.compile("</gradient:#[a-fA-F0-9]{6}>");
    private static final AdventureUtil adventureUtils;
    private static final boolean supportsRGB;

    public static AdventureUtil getAdventureUtils() {
        return adventureUtils;
    }

    public static String getLastColor(String input) {
        String result = "";
        int length = input.length();
        for (int index = length - 1; index > -1; --index) {
            char section = input.charAt(index);
            if ((section == '\u00a7' || section == '&') && index < length - 1) {
                char c = input.charAt(index + 1);
                ChatColor color = ChatColor.getByChar((char)c);
                if (color == null) continue;
                if (color.isColor() || color.equals((Object)ChatColor.RESET)) {
                    result = color.toString() + result;
                    break;
                }
                if (!color.isFormat()) continue;
                result = color.toString() + result;
                continue;
            }
            if (section != '#' || index >= length - 6 || !supportsRGB || !ServerInfo.supportsSpigot()) continue;
            String rgb = input.substring(index, index + 6);
            try {
                Color.decode(rgb);
                return section + rgb;
            }
            catch (NumberFormatException numberFormatException) {
                
            }
        }
        return result;
    }

    private static String formatLegacy(String msg) {
        if (supportsRGB) {
            Gradient gradient = ChatUtil.findGradient(msg);
            if (gradient != null) {
                msg = gradient.get();
            }
            Matcher matcher = rgbPattern.matcher(msg);
            while (matcher.find()) {
                String hex = msg.substring(matcher.start(), matcher.end());
                net.md_5.bungee.api.ChatColor rgb = net.md_5.bungee.api.ChatColor.of((String)hex);
                if (matcher.start() != 0 && matcher.end() != msg.length()) {
                    if (msg.charAt(matcher.start() - 1) == '&') {
                        hex = "&" + hex;
                    }
                    if (msg.charAt(matcher.start() - 1) == '{' && msg.charAt(matcher.end()) == '}') {
                        hex = "{" + hex + "}";
                    }
                }
                msg = msg.replace(hex, String.valueOf(rgb));
                matcher = rgbPattern.matcher(msg);
            }
        }
        return ChatColor.translateAlternateColorCodes((char)'&', (String)msg).replace("\\n", "\n");
    }

    private static Gradient findGradient(String value) {
        Matcher matcher = startGradientPattern.matcher(value);
        if (matcher.find()) {
            String g1 = value.substring(matcher.start(), matcher.end());
            matcher = endGradientPattern.matcher(value);
            if (matcher.find()) {
                String g2 = value.substring(matcher.start(), matcher.end());
                return new Gradient(g1, g2, value);
            }
            return null;
        }
        return null;
    }

    private static String formatVanilla(String msg) {
        if (supportsRGB) {
            Matcher matcher = rgbPattern.matcher(msg);
            while (matcher.find()) {
                String hex = msg.substring(matcher.start(), matcher.end());
                net.md_5.bungee.api.ChatColor rgb = net.md_5.bungee.api.ChatColor.of((String)hex);
                if (matcher.start() != 0 && matcher.end() != msg.length()) {
                    if (msg.charAt(matcher.start() - 1) == '&') {
                        hex = "&" + hex;
                    }
                    if (msg.charAt(matcher.start() - 1) == '{' && msg.charAt(matcher.end()) == '}') {
                        hex = "{" + hex + "}";
                    }
                }
                msg = msg.replace(hex, String.valueOf(rgb));
                matcher = rgbPattern.matcher(msg);
            }
        }
        return ChatColor.translateAlternateColorCodes((char)'&', (String)msg).replace("\\n", "\n");
    }

    public static boolean hasRGB(String s) {
        if (ServerInfo.getVersion().olderOrEqualAs(ServerInfo.Version.v1_15_R1)) {
            return false;
        }
        if (adventureUtils != null) {
            s = adventureUtils.formatMiniToLegacy(s);
        }
        String stripped = ChatUtil.formatLegacy(s).replace("\u00a7x", "#").replaceAll("\u00a7", "");
        return rgbPattern.matcher(stripped).find();
    }

    public static String formatColors(String msg) {
        if (adventureUtils != null) {
            msg = adventureUtils.formatMiniToLegacy(msg);
        }
        return ChatUtil.formatLegacy(msg);
    }

    public static String formatColorsRaw(String s) {
        if (DynamicShopGUIPlus.getInstance().paperMeta) {
            return adventureUtils.formatLegacyToMini(s);
        }
        return ChatUtil.formatColors(s);
    }

    public static String stripColor(String s) {
        return adventureUtils != null ? adventureUtils.stripColor(s) : ChatColor.stripColor((String)s);
    }

    public static String getLastColors(String s) {
        return adventureUtils != null ? AdventureUtil.getLastColor(s) : ChatUtil.getLastColor(s);
    }

    public static String getGsonComponent(String s) {
        return adventureUtils.getGsonComponent(s);
    }

    private static boolean isMiniLibAvailable() {
        try {
            Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
            return true;
        }
        catch (ClassNotFoundException e) {
            return false;
        }
    }

    static {
        supportsRGB = ServerInfo.getVersion().newerOrEqualAs(ServerInfo.Version.v1_16_R1);
        adventureUtils = ServerInfo.getVersion().newerOrEqualAs(ServerInfo.Version.v1_19_R1) ? new AdventureUtil() : null;
    }
}

