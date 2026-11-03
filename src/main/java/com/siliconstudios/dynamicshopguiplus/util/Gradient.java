

package com.siliconstudios.dynamicshopguiplus.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import org.bukkit.ChatColor;

public class Gradient {
    private final String format;
    private final String original;
    private final String start;
    private final String end;
    private final List<net.md_5.bungee.api.ChatColor> colors = new ArrayList<net.md_5.bungee.api.ChatColor>();
    private final Pattern rbgPattern = Pattern.compile("#[a-fA-F0-9]{6}");

    public Gradient(String startGradient, String endGradient, String original) {
        Color c2;
        int start = original.indexOf(startGradient);
        int end = original.indexOf(endGradient) + endGradient.length();
        this.start = original.substring(0, start);
        this.end = original.substring(end);
        this.format = this.getFormat(this.start);
        this.original = original.substring(start, end).replace(startGradient, "").replace(endGradient, "");
        Color c1 = this.getColor(startGradient);
        if (c1 == null) {
            c1 = Color.decode("#D4AF37");
        }
        if ((c2 = this.getColor(endGradient)) == null) {
            c2 = Color.decode("#FADA6F");
        }
        for (int i = 0; i < this.original.length(); ++i) {
            float ratio = (float)i / (float)this.original.length();
            int r = (int)((float)c2.getRed() * ratio + (float)c1.getRed() * (1.0f - ratio));
            int g = (int)((float)c2.getGreen() * ratio + (float)c1.getGreen() * (1.0f - ratio));
            int b = (int)((float)c2.getBlue() * ratio + (float)c1.getBlue() * (1.0f - ratio));
            this.colors.add(net.md_5.bungee.api.ChatColor.of((Color)new Color(r, g, b)));
        }
    }

    private Color getColor(String gradientPattern) {
        Matcher m1 = this.rbgPattern.matcher(gradientPattern);
        if (m1.find()) {
            try {
                return Color.decode(gradientPattern.substring(m1.start(), m1.end()));
            }
            catch (NumberFormatException e) {
                SendMessage.warnMessage("Invalid rgb format for '" + gradientPattern + "', using default gradient colors...");
            }
        } else {
            SendMessage.warnMessage("Invalid rgb format for '" + gradientPattern + "', using default gradient colors...");
        }
        return null;
    }

    public net.md_5.bungee.api.ChatColor getColor(int i) {
        return this.colors.get(i);
    }

    public String get() {
        int i = 0;
        StringBuilder builder = new StringBuilder();
        builder.append(this.start);
        for (char c : this.original.toCharArray()) {
            builder.append(this.getColor(i)).append(this.format).append(c);
            ++i;
        }
        builder.append(this.end);
        return builder.toString();
    }

    private String getFormat(String input) {
        String result = "";
        int length = input.length();
        for (int index = length - 1; index > -1; --index) {
            char c;
            ChatColor color;
            char section = input.charAt(index);
            if (section != '\u00a7' && section != '&' || index >= length - 1 || (color = ChatColor.getByChar((char)(c = input.charAt(index + 1)))) == null || !color.isFormat()) continue;
            result = color.toString() + result;
            if (color.isColor() || color.equals((Object)ChatColor.RESET)) break;
        }
        return result;
    }
}

