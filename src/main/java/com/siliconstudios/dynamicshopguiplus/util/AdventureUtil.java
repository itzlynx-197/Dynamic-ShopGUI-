

package com.siliconstudios.dynamicshopguiplus.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.siliconstudios.dynamicshopguiplus.files.Translatable;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.util.ServerInfo;
import net.kyori.adventure.platform.bukkit.MinecraftComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextFormat;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.CharacterAndFormat;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class AdventureUtil {
    private static final Pattern tagPattern;
    private static final Pattern rgbSerializer;
    private static final String BASE = "\u00a7";
    private static final String RESET = "\u00a7r";
    private static final GsonComponentSerializer gsonSerializer;
    private static final LegacyComponentSerializer legacyDeserializer;
    private static final LegacyComponentSerializer legacySerializer;
    private static final LegacyComponentSerializer bedrockLegacySerializer;
    private static final String BEDROCK_COLORS;
    private static final String BEDROCK_DECORATIONS;
    private static final MiniMessage miniMessage;

    public String formatMiniToLegacy(String msg) {
        return legacyDeserializer.serialize(this.formatMini(msg));
    }

    public String formatLegacyToMini(String msg) {
        return ((String)miniMessage.serialize((Component)legacySerializer.deserialize(rgbSerializer.matcher(msg.replace('\u00a7', '&')).replaceAll("<$1>")))).replace("\\<", "<");
    }

    public Component formatMini(String msg) {
        return miniMessage.deserialize(msg.replace('\u00a7', '&'));
    }

    public Component formatLegacyAndMini(String msg) {
        return this.formatMini(this.formatLegacyToMini(msg));
    }

    public String getGsonComponent(String s) {
        return (String)gsonSerializer.serialize(this.formatMini(this.formatLegacyToMini(s)));
    }

    public static Component fromGson(String gson) {
        return gsonSerializer.deserialize(gson);
    }

    public static String toLegacy(Component comp) {
        return legacyDeserializer.serialize(comp);
    }

    public static String toBedrockLegacy(Component comp) {
        String legacy = bedrockLegacySerializer.serialize(comp);
        int legacyLength = legacy.length();
        StringBuilder finalLegacy = new StringBuilder(legacyLength);
        int appliedFormatting = 0;
        boolean lastFormatReset = false;
        for (int i = 0; i < legacyLength; ++i) {
            char legacyChar = legacy.charAt(i);
            if (legacyChar != '\u00a7' || i == legacyLength - 1) {
                finalLegacy.append(legacyChar);
                lastFormatReset = false;
                if (legacyChar != '\n' || appliedFormatting == 0) continue;
                AdventureUtil.applyBedrockFormattingFlags(appliedFormatting, finalLegacy);
                continue;
            }
            char next = legacy.charAt(++i);
            if (lastFormatReset && next == 'r') continue;
            if (!lastFormatReset && BEDROCK_COLORS.indexOf(next) != -1 && appliedFormatting != 0) {
                finalLegacy.append(RESET);
            }
            finalLegacy.append(BASE).append(next);
            lastFormatReset = next == 'r';
            appliedFormatting = AdventureUtil.setBedrockFormattingFlag(appliedFormatting, next);
        }
        return finalLegacy.toString();
    }

    private static int setBedrockFormattingFlag(int flags, char format) {
        if (format == 'r') {
            return 0;
        }
        int index = BEDROCK_COLORS.indexOf(format);
        if (index == -1) {
            index = BEDROCK_DECORATIONS.indexOf(format);
            if (index == -1) {
                return flags;
            }
            return flags | 1 << BEDROCK_COLORS.length() + index;
        }
        return 1 << index;
    }

    private static void applyBedrockFormattingFlags(int flags, StringBuilder builder) {
        int i;
        int colorCount = BEDROCK_COLORS.length();
        for (i = 0; i < colorCount; ++i) {
            if ((flags >> i & 1) != 1) continue;
            builder.append(BASE).append(BEDROCK_COLORS.charAt(i));
        }
        for (i = 0; i < BEDROCK_DECORATIONS.length(); ++i) {
            if ((flags >> i + colorCount & 1) != 1) continue;
            builder.append(BASE).append(BEDROCK_DECORATIONS.charAt(i));
        }
    }

    public static String serialize(Component component) {
        return ((String)miniMessage.serialize(component)).replace("\\<", "<");
    }

    public static String asGson(Component comp) {
        return (String)gsonSerializer.serialize(comp);
    }

    public static Object asVanilla(Translatable s) {
        try {
            return ServerInfo.invokeStaticLegacyMethod("asVanilla", "io.papermc.paper.adventure.PaperAdventure", new Class[]{Component.class}, s.get());
        }
        catch (Exception e) {
            return MinecraftComponentSerializer.get().serialize((Component)s.get());
        }
    }

    public String stripColor(String s) {
        return tagPattern.matcher(s).replaceAll("");
    }

    public static String getLastColor(String miniFormat) {
        int openBracket;
        int pos = miniFormat.length();
        LinkedHashMap<String, Boolean> formattingState = new LinkedHashMap<String, Boolean>();
        String color = null;
        boolean colorClosed = false;
        while (pos > 0 && (openBracket = miniFormat.lastIndexOf(60, pos - 1)) != -1) {
            block12: {
                String tag;
                block10: {
                    String tagName;
                    block11: {
                        int closeBracket = miniFormat.indexOf(62, openBracket);
                        if (closeBracket == -1) {
                            pos = openBracket;
                            continue;
                        }
                        tag = miniFormat.substring(openBracket + 1, closeBracket);
                        if (!tag.startsWith("/")) break block10;
                        tagName = tag.substring(1);
                        if (!tagName.isEmpty() && !Color.isValid(tagName) && !tag.substring(1).matches("#[0-9a-fA-F]{6}")) break block11;
                        colorClosed = true;
                        break block12;
                    }
                    if (!Format.isValid(tagName)) break block12;
                    formattingState.put(tagName.toLowerCase(), false);
                    break block12;
                }
                if (color == null && Format.isValid(tag)) {
                    String formatName = tag.toLowerCase();
                    if (!formattingState.containsKey(formatName)) {
                        formattingState.put(formatName, true);
                    }
                } else if (tag.startsWith("#") && tag.matches("#[0-9a-fA-F]{6}") || Color.isValid(tag)) {
                    color = tag;
                    break;
                }
            }
            pos = openBracket;
        }
        if (color == null) {
            return "";
        }
        if (colorClosed) {
            return "";
        }
        StringBuilder result = new StringBuilder("<").append(color).append(">");
        ArrayList<String> formats = new ArrayList<String>();
        for (Map.Entry entry : formattingState.entrySet()) {
            if (!((Boolean)entry.getValue()).booleanValue()) continue;
            formats.add((String)entry.getKey());
        }
        Collections.reverse(formats);
        for (String format : formats) {
            result.append("<").append(format).append(">");
        }
        return result.toString();
    }

    static {
        LegacyComponentSerializer bedrockSerializer;
        rgbSerializer = Pattern.compile("(?<![:/<&{])(#([0-9a-fA-F]{6}))");
        gsonSerializer = GsonComponentSerializer.gson();
        legacyDeserializer = LegacyComponentSerializer.legacySection();
        LegacyComponentSerializer.Builder legacyBuilder = null;
        try {
            legacyBuilder = (LegacyComponentSerializer.Builder)ServerInfo.invokeLegacyVersionMethod("toBuilder", LegacyComponentSerializer.legacyAmpersand(), new Object[0]);
            legacyBuilder = legacyBuilder.flattener(ComponentFlattener.basic());
        }
        catch (Exception e) {
            SendMessage.errorMessage("Failed to initialize Adventure Utils");
            e.printStackTrace();
        }
        if (ServerInfo.getVersion().newerOrEqualAs(ServerInfo.Version.v1_16_R1)) {
            legacyBuilder.hexColors();
            legacyBuilder.useUnusualXRepeatedCharacterHexFormat();
        }
        legacySerializer = legacyBuilder.build();
        ArrayList<CharacterAndFormat> bedrockFormats = new ArrayList<CharacterAndFormat>(CharacterAndFormat.defaults());
        bedrockFormats.remove(CharacterAndFormat.STRIKETHROUGH);
        bedrockFormats.remove(CharacterAndFormat.UNDERLINED);
        bedrockFormats.add(CharacterAndFormat.characterAndFormat((char)'g', (TextFormat)TextColor.color((int)221, (int)214, (int)5)));
        bedrockFormats.add(CharacterAndFormat.characterAndFormat((char)'h', (TextFormat)TextColor.color((int)227, (int)212, (int)209)));
        bedrockFormats.add(CharacterAndFormat.characterAndFormat((char)'i', (TextFormat)TextColor.color((int)206, (int)202, (int)202)));
        bedrockFormats.add(CharacterAndFormat.characterAndFormat((char)'j', (TextFormat)TextColor.color((int)68, (int)58, (int)59)));
        bedrockFormats.add(CharacterAndFormat.characterAndFormat((char)'m', (TextFormat)TextColor.color((int)151, (int)22, (int)7)));
        bedrockFormats.add(CharacterAndFormat.characterAndFormat((char)'n', (TextFormat)TextColor.color((int)180, (int)104, (int)77)));
        bedrockFormats.add(CharacterAndFormat.characterAndFormat((char)'p', (TextFormat)TextColor.color((int)222, (int)177, (int)45)));
        bedrockFormats.add(CharacterAndFormat.characterAndFormat((char)'q', (TextFormat)TextColor.color((int)17, (int)160, (int)54)));
        bedrockFormats.add(CharacterAndFormat.characterAndFormat((char)'s', (TextFormat)TextColor.color((int)44, (int)186, (int)168)));
        bedrockFormats.add(CharacterAndFormat.characterAndFormat((char)'t', (TextFormat)TextColor.color((int)33, (int)73, (int)123)));
        bedrockFormats.add(CharacterAndFormat.characterAndFormat((char)'u', (TextFormat)TextColor.color((int)154, (int)92, (int)198)));
        bedrockFormats.add(CharacterAndFormat.characterAndFormat((char)'v', (TextFormat)TextColor.color((int)235, (int)114, (int)20)));
        try {
            bedrockSerializer = LegacyComponentSerializer.builder().character('\u00a7').formats(bedrockFormats).build();
        }
        catch (Exception e) {
            SendMessage.errorMessage("Failed to initialize Bedrock Adventure Utils, falling back to standard legacy colors");
            e.printStackTrace();
            bedrockSerializer = legacyDeserializer;
        }
        bedrockLegacySerializer = bedrockSerializer;
        StringBuilder colorChars = new StringBuilder();
        StringBuilder decorationChars = new StringBuilder();
        for (CharacterAndFormat format : bedrockFormats) {
            if (format.format() instanceof TextColor) {
                colorChars.append(format.character());
            }
            if (!(format.format() instanceof TextDecoration)) continue;
            decorationChars.append(format.character());
        }
        BEDROCK_COLORS = colorChars.toString();
        BEDROCK_DECORATIONS = decorationChars.toString();
        MiniMessage.Builder miniBuilder = MiniMessage.builder();
        miniBuilder.tags(TagResolver.standard());
        miniBuilder.strict(false);
        miniMessage = miniBuilder.build();
        HashSet<String> tags = new HashSet<String>();
        tags.addAll(Color.names());
        tags.addAll(Format.names());
        tags.addAll(Special.names());
        tagPattern = Pattern.compile("</?!?(?:(" + String.join((CharSequence)"|", tags) + ")(?::[^>]+)?|#[0-9a-fA-F]{6})>");
    }

    private static enum Color {
        BLACK("black"),
        DARK_BLUE("dark_blue"),
        DARK_GREEN("dark_green"),
        DARK_AQUA("dark_aqua"),
        DARK_RED("dark_red"),
        DARK_PURPLE("dark_purple"),
        GOLD("gold"),
        GRAY("gray"),
        DARK_GRAY("dark_gray"),
        BLUE("blue"),
        GREEN("green"),
        AQUA("aqua"),
        RED("red"),
        LIGHT_PURPLE("light_purple"),
        YELLOW("yellow"),
        WHITE("white");

        private final String name;

        private Color(String name) {
            this.name = name;
        }

        public static List<String> names() {
            return Arrays.stream(Color.values()).map(Color::getName).collect(Collectors.toList());
        }

        public String getName() {
            return this.name;
        }

        private static boolean isValid(String color) {
            for (Color c : Color.values()) {
                if (!c.name.equals(color.toLowerCase(Locale.ENGLISH))) continue;
                return true;
            }
            return false;
        }
    }

    private static enum Format {
        BOLD("bold", "b"),
        ITALIC("italic", "i", "em"),
        UNDERLINED("underlined", "u"),
        STRIKETHROUGH("strikethrough", "st"),
        OBFUSCATED("obfuscated", "obf");

        private final String[] names;

        private Format(String ... names) {
            this.names = names;
        }

        public static List<String> names() {
            return Arrays.stream(Format.values()).flatMap(s -> Arrays.stream(s.getNames())).collect(Collectors.toList());
        }

        public String[] getNames() {
            return this.names;
        }

        private static boolean isValid(String s) {
            s = s.toLowerCase(Locale.ROOT);
            for (Format f : Format.values()) {
                for (String name : f.names) {
                    if (!name.equals(s)) continue;
                    return true;
                }
            }
            return false;
        }
    }

    private static enum Special {
        COLOR("color", "colour", "c"),
        HOVER("hover"),
        CLICK("click"),
        KEY("key"),
        TRANSLATABLE("lang", "translatable", "tr"),
        TRANSLATABLE_FALLBACK("lang_or", "translate_or", "tr_or"),
        INSERTION("insertion"),
        FONT("font"),
        GRADIENT("gradient"),
        RAINBOW("rainbow"),
        TRANSITION("transition"),
        RESET("reset"),
        NEWLINE("newLine", "br"),
        SELECTOR("selector", "sel"),
        SCORE("score"),
        NBT("nbt", "data");

        private final String[] names;

        private Special(String ... names) {
            this.names = names;
        }

        public static List<String> names() {
            return Arrays.stream(Special.values()).flatMap(s -> Arrays.stream(s.getNames())).collect(Collectors.toList());
        }

        public String[] getNames() {
            return this.names;
        }

        private static boolean isValid(String s) {
            s = s.toLowerCase(Locale.ROOT);
            for (Special f : Special.values()) {
                for (String name : f.names) {
                    if (!name.equals(s)) continue;
                    return true;
                }
            }
            return false;
        }
    }
}

