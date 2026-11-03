

package com.siliconstudios.dynamicshopguiplus.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class FireworkUtil {
    public static ItemStack addEffect(ItemStack item, ConfigurationSection config) {
        FireworkMeta meta = (FireworkMeta)item.getItemMeta();
        if (config.getInt("duration", 3) != 0) {
            meta.setPower(config.getInt("duration", 3));
        }
        if (config.contains("flicker") || config.contains("trail") || config.contains("shape") || config.contains("fade-colors")) {
            if (!config.getStringList("colors").isEmpty()) {
                FireworkEffect.Builder builder = FireworkEffect.builder();
                List<FireworkColor> colors = FireworkColor.matchColorsNames(config.getStringList("colors"));
                if (!colors.isEmpty()) {
                    colors.forEach(fireworkColor -> builder.withColor(fireworkColor.parseColor()));
                } else {
                    builder.withColor(FireworkColor.WHITE.parseColor());
                }
                if (!config.getStringList("fade-colors").isEmpty()) {
                    FireworkColor.matchColorsNames(config.getStringList("fade-colors")).forEach(fireworkColor -> builder.withFade(fireworkColor.parseColor()));
                }
                if (config.getBoolean("flicker", false)) {
                    builder.withFlicker();
                }
                if (config.getBoolean("trail", false)) {
                    builder.trail(true);
                }
                if (config.getString("shape") != null) {
                    try {
                        builder.with(FireworkEffect.Type.valueOf((String)config.getString("shape").toUpperCase(Locale.ENGLISH)));
                    }
                    catch (IllegalArgumentException ignored) {
                        SendMessage.errorMessage("Could not add the firework shape '" + config.getString("shape") + "'");
                    }
                }
                meta.addEffect(builder.build());
            } else {
                SendMessage.errorMessage("To apply a flicker, trail or firework type you need to give the firework at least one color using the 'colors' option");
            }
        }
        item.setItemMeta((ItemMeta)meta);
        return item;
    }

    public static Map<String, Object> serialize(FireworkMeta meta) {
        HashMap<String, Object> keys = new HashMap<String, Object>();
        keys.put("duration", meta.getPower());
        if (!meta.hasEffects()) {
            return keys;
        }
        FireworkEffect effect = (FireworkEffect)meta.getEffects().get(0);
        keys.put("colors", effect.getColors().stream().map(c -> FireworkColor.matchColor(c.asRGB()).name()).collect(Collectors.toList()));
        keys.put("fade-colors", effect.getFadeColors().stream().map(c -> FireworkColor.matchColor(c.asRGB()).name()).collect(Collectors.toList()));
        keys.put("shape", effect.getType().name());
        keys.put("trail", effect.hasTrail());
        keys.put("flicker", effect.hasFlicker());
        return keys;
    }

    public static enum FireworkColor {
        WHITE(1, 0xF0F0F0),
        ORANGE(2, 15435844),
        MAGENTA(3, 12801229),
        LIGHT_BLUE(4, 6719955),
        YELLOW(5, 14602026),
        LIME(6, 4312372),
        PINK(7, 14188952),
        GRAY(8, 0x434343),
        LIGHT_GRAY(9, 0xABABAB),
        CYAN(10, 2651799),
        PURPLE(11, 8073150),
        BLUE(12, 2437522),
        BROWN(13, 5320730),
        GREEN(14, 3887386),
        RED(15, 11743532),
        BLACK(16, 0x1E1B1B);

        int id;
        int rgb;

        private FireworkColor(int id, int rgb) {
            this.id = id;
            this.rgb = rgb;
        }

        public static FireworkColor matchColor(String name) {
            for (FireworkColor color : FireworkColor.values()) {
                if (!color.name().equalsIgnoreCase(name)) continue;
                return color;
            }
            return null;
        }

        public static List<FireworkColor> matchColorsNames(List<String> list) {
            ArrayList<FireworkColor> colors = new ArrayList<FireworkColor>();
            for (String c : list) {
                for (FireworkColor color : FireworkColor.values()) {
                    if (!color.name().equalsIgnoreCase(c)) continue;
                    colors.add(color);
                }
            }
            return colors;
        }

        public static List<FireworkColor> matchColorsIds(String s) {
            ArrayList<FireworkColor> colors = new ArrayList<FireworkColor>();
            for (String i : s.split(",")) {
                try {
                    for (FireworkColor color : FireworkColor.values()) {
                        if (color.id != Integer.parseInt(i)) continue;
                        colors.add(color);
                    }
                }
                catch (NumberFormatException numberFormatException) {
                    
                }
            }
            return colors;
        }

        public static FireworkColor getFromID(int id) {
            for (FireworkColor color : FireworkColor.values()) {
                if (color.id != id) continue;
                return color;
            }
            return null;
        }

        public static FireworkColor matchColor(int rgb) {
            for (FireworkColor color : FireworkColor.values()) {
                if (color.rgb != rgb) continue;
                return color;
            }
            return null;
        }

        public Color parseColor() {
            return Color.fromRGB((int)this.rgb);
        }
    }
}

