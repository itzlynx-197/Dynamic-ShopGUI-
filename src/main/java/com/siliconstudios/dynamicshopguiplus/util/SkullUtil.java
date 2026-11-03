

package com.siliconstudios.dynamicshopguiplus.util;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.SkullCache;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.util.JsonUtil;
import com.siliconstudios.dynamicshopguiplus.util.ServerInfo;
import com.siliconstudios.dynamicshopguiplus.util.XMaterial;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class SkullUtil {
    private static final String VALUE_PROPERTY = "{\"textures\":{\"SKIN\":";
    private static DynamicShopGUIPlus plugin;
    private static SkullCache SKULL_CACHE;
    private static Field VALUE;
    private static Field MODERN_PROFILE;
    private static Method PROFILE_SETTER;
    private static Field PROFILE;
    private static Field VAL;
    private static Field SIG;
    private static Field propertyMap;
    private static Method createResolved;
    private static boolean rateLimit;
    private static final Function<GameProfile, PropertyMap> propertiesMethod;
    private static final Function<GameProfile, String> nameMethod;
    private static Class<?> profileClass;

    public static PropertyMap getProperties(GameProfile profile) {
        return propertiesMethod.apply(profile);
    }

    public static String getName(GameProfile profile) {
        return nameMethod.apply(profile);
    }

    public SkullUtil(DynamicShopGUIPlus plugin) {
        SKULL_CACHE = new SkullCache(plugin);
        SkullUtil.plugin = plugin;
    }

    public void saveCache() {
        SKULL_CACHE.write();
    }

    private static String getPost(String url) throws Exception {
        URL link = new URL(url);
        BufferedReader in = new BufferedReader(new InputStreamReader(link.openStream()));
        StringBuilder builder = new StringBuilder();
        for (String s : in.lines().collect(Collectors.toList())) {
            builder.append(s);
        }
        in.close();
        return builder.toString().replaceAll(" ", "");
    }

    private static void cache(Consumer<String> onComplete, ItemStack item, String name) {
        if (name.equals("CS-CoreLib")) {
            return;
        }
        try {
            String result = SkullUtil.getPost("https://api.minecraftservices.com/minecraft/profile/lookup/name/" + name);
            String uuid = (String)((JSONObject)new JSONParser().parse(result)).get((Object)"id");
            String s = SkullUtil.getPost("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
            JSONObject property = (JSONObject)((JSONArray)((JSONObject)new JSONParser().parse(s)).get((Object)"properties")).get(0);
            onComplete.accept(SkullUtil.getTextureURL(JsonUtil.getString(property, "value")));
        }
        catch (FileNotFoundException e) {
            onComplete.accept(" ");
        }
        catch (SocketException e) {
            SendMessage.logDebugMessage("Socket exception occurred while trying to retrieve skull texture for player '" + name + "', trying again in 5 seconds...");
            plugin.runTaskLaterAsync(() -> SkullUtil.cache(onComplete, item, name), 100L);
        }
        catch (UnknownHostException e) {
        }
        catch (IOException e) {
            if (e.getMessage().contains("429")) {
                if (!rateLimit) {
                    SendMessage.logDebugMessage("Mojang started to rate limit our connections while trying to retrieve a skull texture, trying again in 60 seconds...");
                    rateLimit = true;
                }
                plugin.runTaskLaterAsync(() -> {
                    SkullUtil.cache(onComplete, item, name);
                    rateLimit = false;
                }, 1200L);
            } else {
                SendMessage.warnMessage("Failed to retrieve skull texture for player '" + name + "'");
                e.printStackTrace();
            }
        }
        catch (Exception e) {
            SendMessage.warnMessage("Failed to retrieve skull texture for player '" + name + "'");
            e.printStackTrace();
        }
    }

    public static String getPlayerSkullTexture(Object data) {
        try {
            String v = new String(Base64.getDecoder().decode((String)VALUE.get(data)));
            JsonObject json = ((JsonObject)new Gson().fromJson(v, JsonObject.class)).get("textures").getAsJsonObject();
            json.get("SKIN").getAsJsonObject().remove("metadata");
            return Base64.getEncoder().encodeToString(("{\"textures\":" + json + "}").getBytes());
        }
        catch (Exception e) {
            SendMessage.errorMessage("Failed to get skull texture from item");
            e.printStackTrace();
            return null;
        }
    }

    public static String getSkullTexture(SkullMeta meta) {
        try {
            return SkullUtil.getSkullTexture(SkullUtil.getProfile(meta));
        }
        catch (Exception e) {
            SendMessage.errorMessage("Failed to get skull texture from item");
            e.printStackTrace();
            return null;
        }
    }

    private static GameProfile getProfile(SkullMeta meta) throws Exception {
        Object profile = PROFILE.get(meta);
        return (GameProfile)(profileClass == GameProfile.class ? profile : MODERN_PROFILE.get(profile));
    }

    private static String getSkullTexture(GameProfile profile) throws Exception {
        try {
            if (profile != null) {
                Object propertyMap = null;
                try {
                    propertyMap = SkullUtil.getProperties(profile);
                } catch (Throwable ignored) {}
                if (propertyMap != null) {
                    Object textures = null;
                    try {
                        textures = propertyMap.getClass().getMethod("get", Object.class).invoke(propertyMap, "textures");
                    } catch (Throwable ignored) {}
                    if (textures != null) {
                        try {
                            for (Object property : (Iterable<?>) textures) {
                                String value = SkullUtil.getValue(property);
                                if (value.isEmpty()) continue;
                                return value;
                            }
                        } catch (Throwable ignored) {}
                    }
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    public static String getTextureURL(String raw) {
        String json = new String(Base64.getDecoder().decode(raw));
        try {
            JSONObject data = (JSONObject)new JSONParser().parse(json);
            String url = ((JSONObject)data.get((Object)"textures")).get((Object)"SKIN").toString().replace("\\", "").replace("https://", "http://");
            return SkullUtil.encodeTexture(url);
        }
        catch (ParseException e) {
            return "";
        }
        catch (Exception e) {
            SendMessage.errorMessage("Failed to get skull texture from item: " + json);
            e.printStackTrace();
            return null;
        }
    }

    private static String encodeTexture(String url) {
        return Base64.getEncoder().encodeToString((VALUE_PROPERTY + url + "}}").getBytes());
    }

    public static boolean isSimilarSkull(ItemStack item1, ItemStack item2) {
        return SkullUtil.getSkullTexture((SkullMeta)item1.getItemMeta()).equals(SkullUtil.getSkullTexture((SkullMeta)item2.getItemMeta()));
    }

    public static void setSkullTexture(ItemStack stack, SkullMeta meta, String name, boolean async) {
        String texture = SKULL_CACHE.getSkull(name);
        if (texture == null) {
            Consumer<String> onComplete = s -> {
                SKULL_CACHE.cacheSkull(name, (String)s);
                plugin.runTaskLater(() -> {
                    SkullMeta sm = (SkullMeta)stack.getItemMeta();
                    SkullUtil.setSkullTexture(sm, name, s);
                    stack.setItemMeta((ItemMeta)sm);
                }, 1L);
            };
            if (async) {
                plugin.runTaskLaterAsync(() -> SkullUtil.cache(onComplete, stack, name), 1L);
            } else {
                SkullUtil.cache(onComplete, stack, name);
            }
        } else if (meta == null) {
            SkullMeta sm = (SkullMeta)stack.getItemMeta();
            SkullUtil.setSkullTexture(sm, name, texture);
            stack.setItemMeta((ItemMeta)sm);
        } else {
            SkullUtil.setSkullTexture(meta, name, texture);
        }
    }

    public static void setSkullTexture(Consumer<GameProfile> onComplete, ItemStack stack, SkullMeta meta, String name) {
        String texture = SKULL_CACHE.getSkull(name);
        if (texture == null) {
            plugin.runTaskLaterAsync(() -> SkullUtil.cache(s -> {
                SKULL_CACHE.cacheSkull(name, (String)s);
                onComplete.accept(SkullUtil.getProfile(name, s));
            }, stack, name), 1L);
        } else if (meta == null) {
            SkullMeta sm = (SkullMeta)stack.getItemMeta();
            SkullUtil.setSkullTexture(sm, name, texture);
            stack.setItemMeta((ItemMeta)sm);
        } else {
            SkullUtil.setSkullTexture(meta, name, texture);
        }
    }

    private static void setSkullTexture(SkullMeta meta, String name, String texture) {
        SkullUtil.applySkullProfile(SkullUtil.getProfile(name, texture), meta);
    }

    private static GameProfile getProfile(String name, String texture) {
        GameProfile profile = SkullUtil.createNewProfile(UUID.nameUUIDFromBytes(texture.getBytes(StandardCharsets.UTF_8)), name);
        Object propertyMap = null;
        try {
            propertyMap = SkullUtil.getProperties(profile);
        } catch (Throwable ignored) {}
        if (propertyMap != null) {
            try {
                propertyMap.getClass().getMethod("put", Object.class, Object.class).invoke(propertyMap, "textures", new com.mojang.authlib.properties.Property("textures", texture));
            } catch (Throwable ignored) {}
        }
        return profile;
    }

    public static void applySkullProfile(Object profile, SkullMeta sm) {
        try {
            if (PROFILE_SETTER != null) {
                PROFILE_SETTER.invoke((Object)sm, profileClass == GameProfile.class ? profile : SkullUtil.getModernProfile((GameProfile)profile));
            } else {
                PROFILE.set(sm, profile);
            }
        }
        catch (ExceptionInInitializerError | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            SendMessage.logDebugMessage("Failed to create skull texture");
            e.printStackTrace();
        }
    }

    public static void updateTexture(Player p) {
        String texture = null;
        try {
            Method m = p.getClass().getDeclaredMethod(SkullUtil.plugin.version > 117 ? "getPlayerProfile" : "getProfile", new Class[0]);
            m.setAccessible(true);
            if (SkullUtil.plugin.version > 117) {
                String url = ((PlayerProfile)m.invoke((Object)p, new Object[0])).getTextures().getSkin().toString();
                if (url == null) {
                    return;
                }
                texture = SkullUtil.encodeTexture("{\"url\":\"" + url + "\"}");
            } else {
                texture = SkullUtil.getSkullTexture((GameProfile)m.invoke((Object)p, new Object[0]));
                if (texture == null) {
                    return;
                }
                texture = SkullUtil.getTextureURL(texture);
            }
        }
        catch (Exception m) {
            
        }
        if (texture == null) {
            return;
        }
        SkullCache.PlayerTexture cached = SKULL_CACHE.getTexture(p.getName());
        if (cached == null) {
            SKULL_CACHE.cacheSkull(p.getName(), texture);
            return;
        }
        try {
            if (!texture.equalsIgnoreCase(cached.getTexture())) {
                SendMessage.logDebugMessage("Detected skin change of " + p.getName() + ", updating cached skin...");
                SKULL_CACHE.cacheSkull(p.getName(), texture);
            }
        }
        catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
            
        }
    }

    private static String getValue(Object prop) {
        try {
            return (String)VAL.get(prop);
        }
        catch (IllegalAccessException e) {
            SendMessage.warnMessage("A error occurred while reading skull");
            e.printStackTrace();
            return "";
        }
    }

    private static GameProfile createNewProfile(UUID uuid, String name) {
        return new GameProfile(uuid, name);
    }

    private static Object getModernProfile(GameProfile profile) {
        try {
            if (ServerInfo.getVersion().newerOrEqualAs(ServerInfo.Version.v1_21_R6)) {
                return createResolved.invoke(null, profile);
            }
            return Class.forName("net.minecraft.world.item.component.ResolvableProfile").getDeclaredConstructor(GameProfile.class).newInstance(profile);
        }
        catch (Exception e) {
            SendMessage.errorMessage("Failed to create modern player profile instance");
            throw new RuntimeException(e);
        }
    }

    private static Function<GameProfile, PropertyMap> getPropertiesMethod() {
        try {
            Method method = GameProfile.class.getMethod("getProperties", new Class[0]);
            return profile -> {
                try {
                    return (PropertyMap) method.invoke(profile, new Object[0]);
                } catch (Exception e) {
                    return null;
                }
            };
        } catch (Throwable ignored) {
            return profile -> null;
        }
    }

    private static Function<GameProfile, String> getNameMethod() {
        try {
            Method method = GameProfile.class.getMethod("getName", new Class[0]);
            return profile -> {
                try {
                    return (String) method.invoke(profile, new Object[0]);
                } catch (Exception e) {
                    return null;
                }
            };
        } catch (Throwable ignored) {
            return profile -> null;
        }
    }

    static {
        MODERN_PROFILE = null;
        PROFILE_SETTER = null;
        rateLimit = false;
        propertiesMethod = SkullUtil.getPropertiesMethod();
        nameMethod = SkullUtil.getNameMethod();
        SkullMeta meta = (SkullMeta)new ItemStack(XMaterial.PLAYER_HEAD.parseMaterial()).getItemMeta();
        try {
            if (meta.getClass().getDeclaredField("profile").getType() == GameProfile.class) {
                throw new NoSuchFieldException();
            }
            profileClass = Class.forName("net.minecraft.world.item.component.ResolvableProfile");
            for (Field field : profileClass.getDeclaredFields()) {
                if (!field.getType().equals(GameProfile.class)) continue;
                MODERN_PROFILE = field;
                field.setAccessible(true);
                break;
            }
        }
        catch (ClassNotFoundException | LinkageError | NoSuchFieldException | SecurityException e) {
            profileClass = GameProfile.class;
        }
        try {
            PROFILE_SETTER = meta.getClass().getDeclaredMethod("setProfile", profileClass);
            PROFILE_SETTER.setAccessible(true);
        }
        catch (NoSuchMethodException | SecurityException e) {
            
        }
        try {
            createResolved = Class.forName("net.minecraft.world.item.component.ResolvableProfile").getDeclaredMethod(ServerInfo.usesMojangMappings() ? "createResolved" : "a", GameProfile.class);
            createResolved.setAccessible(true);
        }
        catch (NoSuchMethodException e) {
            try {
                createResolved = Class.forName("net.minecraft.world.item.component.ResolvableProfile").getDeclaredMethod("a", GameProfile.class);
                createResolved.setAccessible(true);
            }
            catch (ClassNotFoundException | NoSuchMethodException reflectiveOperationException) {}
        }
        catch (ClassNotFoundException e) {
            
        }
        try {
            PROFILE = meta.getClass().getDeclaredField("profile");
            PROFILE.setAccessible(true);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            ex.printStackTrace();
        }
        Property property = new Property("", "");
        try {
            VAL = property.getClass().getDeclaredField("value");
            VAL.setAccessible(true);
            SIG = property.getClass().getDeclaredField("signature");
            SIG.setAccessible(true);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            ex.printStackTrace();
        }
        try {
            Class<?> map = Class.forName("com.mojang.authlib.properties.PropertyMap");
            propertyMap = map.getDeclaredField("properties");
            propertyMap.setAccessible(true);
            Class<?> c = Class.forName("org.geysermc.floodgate.skin.SkinDataImpl");
            VALUE = c.getDeclaredField("value");
            VALUE.setAccessible(true);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            ex.printStackTrace();
        }
        catch (ClassNotFoundException classNotFoundException) {
            
        }
    }
}

