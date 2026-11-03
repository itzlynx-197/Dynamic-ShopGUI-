

package com.siliconstudios.dynamicshopguiplus.files;

import java.util.List;

public interface Translatable {
    public static final char BLACK = '0';
    public static final char DARK_BLUE = '1';
    public static final char DARK_GREEN = '2';
    public static final char DARK_AQUA = '3';
    public static final char DARK_RED = '4';
    public static final char DARK_PURPLE = '5';
    public static final char GOLD = '6';
    public static final char GRAY = '7';
    public static final char DARK_GRAY = '8';
    public static final char BLUE = '9';
    public static final char GREEN = 'a';
    public static final char AQUA = 'b';
    public static final char RED = 'c';
    public static final char LIGHT_PURPLE = 'd';
    public static final char YELLOW = 'e';
    public static final char WHITE = 'f';

    public Translatable replace(String var1, String var2);

    public Translatable replace(String var1, Translatable var2);

    public String getLegacy();

    public String getBedrockLegacy();

    public String toString();

    public Object get();

    public List<Translatable> getLines();

    public Builder builder();

    public static interface Builder {
        public Builder append(Translatable var1);

        public Builder append(String var1);

        public Builder color(char var1);

        public Translatable build();
    }
}

