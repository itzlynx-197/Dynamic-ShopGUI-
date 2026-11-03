

package com.siliconstudios.dynamicshopguiplus.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import org.bukkit.Bukkit;

public class ServerInfo {
    private static ServerType type;
    private static Version version;
    private static boolean components;
    private static final boolean NEW_VERSION_SCHEMA;

    public static boolean supportsSpigot() {
        return type != ServerType.BUKKIT;
    }

    public static boolean supportsPaper() {
        return type != ServerType.BUKKIT && type != ServerType.SPIGOT;
    }

    public static boolean usesMojangMappings() {
        try {
            return version.newerOrEqualAs(Version.v1_21_R1) && Class.forName("net.minecraft.server.MinecraftServer").getDeclaredField("LOGGER") != null;
        }
        catch (ClassNotFoundException e) {
            return false;
        }
        catch (NoSuchFieldException | NullPointerException e) {
            return false;
        }
    }

    public static boolean supportsPaperNativeComponents() {
        try {
            Field c = Class.forName(Bukkit.getServer().getClass().getPackage().getName() + ".inventory.CraftMetaItem").getDeclaredField("displayName");
            c.setAccessible(true);
            return c.getType() == Class.forName("net.minecraft.network.chat.Component");
        }
        catch (ClassNotFoundException | NoSuchFieldException e) {
            return false;
        }
    }

    public static boolean supportsMiniMessageBukkit() {
        try {
            Class.forName("net.kyori.adventure.platform.bukkit.BukkitAudiences");
            return true;
        }
        catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isPaper() {
        return type == ServerType.PAPER;
    }

    public static boolean isFolia() {
        return type == ServerType.FOLIA;
    }

    public static boolean supportsComponents() {
        return components;
    }

    public static Version getVersion() {
        return version;
    }

    public static boolean isNMS() {
        return version.getSimple() > Version.v1_16_R3.getSimple();
    }

    public static Object invokeModernVersionMethod(String method, Object ... args) throws Exception {
        Class[] classes = new Class[args.length];
        for (int i = 0; i < args.length; ++i) {
            classes[i] = args[i].getClass();
        }
        return ServerInfo.invokeModernVersionMethod(method, classes, args);
    }

    public static Object invokeModernVersionMethod(String method, Class<?>[] classes, Object ... args) throws Exception {
        Class<?> clazz = DynamicShopGUIPlus.getInstance().versionHandler.getClass();
        Method c = clazz.getDeclaredMethod(method, classes);
        c.setAccessible(true);
        return c.invoke((Object)DynamicShopGUIPlus.getInstance().versionHandler, args);
    }

    public static Object invokeLegacyVersionMethod(String method, Object obj, Object ... args) throws Exception {
        Class[] classes = new Class[args.length];
        for (int i = 0; i < args.length; ++i) {
            classes[i] = args[i].getClass();
        }
        Method c = obj.getClass().getDeclaredMethod(method, classes);
        c.setAccessible(true);
        return c.invoke(obj, args);
    }

    public static Object invokeStaticLegacyMethod(String method, String clazzName, Class<?>[] classes, Object ... args) throws Exception {
        return ServerInfo.invokeStaticLegacyMethod(method, Class.forName(clazzName), classes, args);
    }

    public static Object invokeStaticLegacyMethod(String method, Class<?> clazz, Class<?>[] classes, Object ... args) throws Exception {
        Method c = clazz.getDeclaredMethod(method, classes);
        c.setAccessible(true);
        return c.invoke(null, args);
    }

    public static boolean isClassPresent(String clazz) {
        try {
            Class.forName(clazz);
            return true;
        }
        catch (ClassNotFoundException e) {
            return false;
        }
    }

    static {
        NEW_VERSION_SCHEMA = Bukkit.getBukkitVersion().split("-")[0].split("\\.")[0].length() == 2;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            type = ServerType.FOLIA;
        }
        catch (ClassNotFoundException e) {
            try {
                Class.forName("com.destroystokyo.paper.PaperConfig");
                type = ServerType.PAPER;
            }
            catch (ClassNotFoundException e1) {
                try {
                    Class.forName("org.spigotmc.SpigotConfig");
                    type = ServerType.SPIGOT;
                }
                catch (ClassNotFoundException e2) {
                    type = ServerType.BUKKIT;
                }
            }
        }
        try {
            version = Version.valueOf(Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]);
        }
        catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            try {
                Method c = Bukkit.class.getDeclaredMethod("getMinecraftVersion", new Class[0]);
                c.setAccessible(true);
                version = Version.getFromSimple((String)c.invoke(null, new Object[0]));
            }
            catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException err) {
                err.printStackTrace();
                version = Version.getFromSimple(Bukkit.getBukkitVersion().split("-")[0].split("\\.build")[0].split("\\.local")[0]);
            }
        }
        components = version != null && version.newerOrEqualAs(Version.v1_20_R4);
    }

    private static enum ServerType {
        BUKKIT,
        SPIGOT,
        PAPER,
        FOLIA;

    }

    public static enum Version {
        v26_3("26.3", "26.3.1", "26.3.2"),
        v26_2("26.2", "26.2.1", "26.2.2"),
        v26_1("26.1", "26.1.1", "26.1.2"),
        v1_21_R7("1.21.11"),
        v1_21_R6("1.21.9", "1.21.10"),
        v1_21_R5("1.21.6", "1.21.7", "1.21.8"),
        v1_21_R4("1.21.5"),
        v1_21_R3("1.21.4"),
        v1_21_R2("1.21.2", "1.21.3"),
        v1_21_R1("1.21", "1.21.1"),
        v1_20_R4("1.20.5", "1.20.6"),
        v1_20_R3("1.20.3", "1.20.4"),
        v1_20_R2("1.20.2"),
        v1_20_R1("1.20", "1.20.1"),
        v1_19_R3("1.19.4"),
        v1_19_R2("1.19.3"),
        v1_19_R1("1.19", "1.19.1", "1.19.2"),
        v1_18_R2("1.18.2"),
        v1_18_R1("1.18", "1.18.1"),
        v1_17_R1("1.17", "1.17.1"),
        v1_16_R3("1.16.4", "1.16.5"),
        v1_16_R2("1.16.2", "1.16.3"),
        v1_16_R1("1.16", "1.16.1"),
        v1_15_R1("1.15", "1.15.1", "1.15.2"),
        v1_14_R1("1.14", "1.14.1", "1.14.2", "1.14.3", "1.14.4"),
        v1_13_R2("1.13.1", "1.13.2"),
        v1_13_R1("1.13"),
        v1_12_R1("1.12", "1.12.1", "1.12.2"),
        v1_11_R1("1.11", "1.11.1", "1.11.2"),
        v1_10_R1("1.10", "1.10.1", "1.10.2"),
        v1_9_R2("1.9.3", "1.9.4"),
        v1_9_R1("1.9", "1.9.1", "1.9.2"),
        v1_8_R3("1.8.4", "1.8.5", "1.8.6", "1.8.7", "1.8.8"),
        v1_8_R2("1.8.1", "1.8.2", "1.8.3"),
        v1_8_R1("1.8"),
        UNKOWN_VERSION("0.0");

        private final String[] versions;

        private Version(String ... versions) {
            this.versions = versions;
        }

        public static Version getFromSimple(String version) {
            for (Version ver : Version.values()) {
                if (!Arrays.asList(ver.versions).contains(version)) continue;
                return ver;
            }
            return UNKOWN_VERSION;
        }

        public int getMajor() {
            if (this == UNKOWN_VERSION) {
                return 0;
            }
            if (NEW_VERSION_SCHEMA) {
                return Integer.parseInt(this.name().split("_", 2)[0].replace("v", ""));
            }
            String major = this.name().split("_")[1];
            if (major.length() == 1) {
                major = "0" + major;
            }
            return Integer.parseInt(major);
        }

        public int getVersion() {
            if (this == UNKOWN_VERSION) {
                return 0;
            }
            String first = this.name().split("_", 2)[0].replace("v", "");
            String major = this.name().split("_")[1];
            return Integer.parseInt(first + major);
        }

        public int getSimple() {
            if (this == UNKOWN_VERSION) {
                return 0;
            }
            String first = this.name().split("_", 2)[0].replace("v", "");
            String major = this.name().split("_")[1];
            if (major.length() == 1) {
                major = "0" + major;
            }
            if (NEW_VERSION_SCHEMA) {
                return Integer.parseInt(first + major);
            }
            String patch = String.valueOf(this.name().charAt(this.name().length() - 1));
            return Integer.parseInt(first + major + patch);
        }

        public boolean newerOrEqualAs(Version version) {
            return this.getSimple() >= version.getSimple();
        }

        public boolean olderOrEqualAs(Version version) {
            return this.getSimple() <= version.getSimple();
        }
    }
}

