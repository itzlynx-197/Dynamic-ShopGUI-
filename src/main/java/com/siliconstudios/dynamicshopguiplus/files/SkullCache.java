

package com.siliconstudios.dynamicshopguiplus.files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class SkullCache {
    private final File file;
    private JSONObject skullCache;

    public SkullCache(DynamicShopGUIPlus plugin) {
        this.file = new File(plugin.getDataFolder() + File.separator + "cache" + File.separator + "skullcache.json");
        try {
            if (!this.file.getParentFile().exists()) {
                this.file.getParentFile().mkdirs();
            }
            if (!this.file.exists()) {
                this.file.createNewFile();
            }
            if (this.file.length() == 0L) {
                FileOutputStream outputStream = new FileOutputStream(this.file);
                ((OutputStream)outputStream).write("{}".getBytes());
                ((OutputStream)outputStream).close();
            }
        }
        catch (IOException e) {
            SendMessage.warnMessage("Failed to create 'skullcache.json' cache file");
            e.printStackTrace();
        }
        try (FileReader reader = new FileReader(this.file);){
            this.skullCache = (JSONObject)new JSONParser().parse((Reader)reader);
        }
        catch (Exception e) {
            SendMessage.warnMessage("Failed to load skullcache from 'skullcache.json'");
            e.printStackTrace();
        }
    }

    public String getSkull(String name) {
        return (String)this.skullCache.get((Object)name);
    }

    public PlayerTexture getTexture(String name) {
        Object object = this.skullCache.get((Object)name);
        if (object == null) {
            return null;
        }
        return new PlayerTexture((String)object);
    }

    public String cacheSkull(String name, String texture) {
        this.skullCache.put((Object)name, (Object)texture);
        return texture;
    }

    public void write() {
        try (FileWriter file = new FileWriter(this.file);){
            file.write(this.skullCache.toJSONString());
            file.flush();
        }
        catch (IOException e) {
            SendMessage.warnMessage("Failed to save skull cache to 'skullcache.json'");
            e.printStackTrace();
        }
    }

    public static class PlayerTexture {
        private final String texture;

        public PlayerTexture(String texture) {
            this.texture = texture;
        }

        public String getTexture() {
            return this.texture;
        }
    }
}

