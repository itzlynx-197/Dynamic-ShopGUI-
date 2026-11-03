

package com.siliconstudios.dynamicshopguiplus.util;

import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;

public final class BackButton {
    public static boolean DIRECT_CONSOLE;
    public static boolean DIRECT;
    public static boolean SUB_SECTIONS;

    public static void init() {
        DIRECT = ConfigManager.getConfig().getBoolean("disable-back.direct-shop-command");
        DIRECT_CONSOLE = ConfigManager.getConfig().getBoolean("disable-back.direct-shop-command-console");
        SUB_SECTIONS = ConfigManager.getConfig().getBoolean("disable-back.sub-sections");
    }
}

