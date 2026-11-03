

package com.siliconstudios.dynamicshopguiplus.files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.objects.stands.Stand;
import com.siliconstudios.dynamicshopguiplus.objects.stands.StandLoc;
import com.siliconstudios.dynamicshopguiplus.objects.stands.StandSettings;
import com.siliconstudios.dynamicshopguiplus.objects.stands.StandType;
import com.siliconstudios.dynamicshopguiplus.util.JsonUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class StandStorage {
    private final File file;

    public StandStorage(DynamicShopGUIPlus plugin) {
        this.file = new File(plugin.getDataFolder() + File.separator + "stands.json");
        try {
            if (!this.file.getParentFile().exists()) {
                this.file.getParentFile().mkdirs();
            }
            if (!this.file.exists()) {
                this.file.createNewFile();
            }
            if (this.file.length() == 0L) {
                FileOutputStream outputStream = new FileOutputStream(this.file);
                ((OutputStream)outputStream).write("[]".getBytes());
                ((OutputStream)outputStream).close();
            }
        }
        catch (IOException e) {
            SendMessage.warnMessage(Lang.ERROR_CREATING_STANDS_STORAGE.get());
            e.printStackTrace();
        }
    }

    public Set<Stand> getStoredStands() {
        HashSet<Stand> stands = new HashSet<Stand>();
        try (FileReader reader = new FileReader(this.file);){
            for (Object obj : JsonUtil.parseArray(reader)) {
                try {
                    JSONObject stand = (JSONObject)obj;
                    int id = JsonUtil.getLong(stand, "stand_id").intValue();
                    StandLoc loc = new StandLoc(JsonUtil.getString(stand, "location"), id);
                    StandType type = StandType.fromName(JsonUtil.getString(stand, "type"));
                    String item = JsonUtil.getString(stand, "item");
                    StandSettings settings = new StandSettings(JsonUtil.getString(stand, "settings"));
                    stands.add(new Stand(id, loc, item, type, settings));
                }
                catch (Exception e) {
                    SendMessage.errorMessage(Lang.ERROR_LOADING_SHOP_STAND.get().replace("%stand%", obj.toString()).replace("%reason%", e.getMessage()));
                }
            }
        }
        catch (Exception e) {
            SendMessage.warnMessage(Lang.ERROR_LOADING_SHOP_STANDS.get());
            e.printStackTrace();
        }
        return stands;
    }

    public void save(Set<Stand> stands) {
        this.saveToFile(stands);
    }

    private void saveToFile(Set<Stand> stands) {
        JSONArray array = this.toJSON(stands);
        if (array == null) {
            return;
        }
        SendMessage.logDebugMessage(Lang.SAVING_SHOP_STANDS.get().replace("%count%", String.valueOf(stands.size())));
        try (FileWriter file = new FileWriter(this.file);){
            file.write(array.toJSONString());
            file.flush();
        }
        catch (IOException e) {
            SendMessage.warnMessage(Lang.ERROR_SAVING_SHOP_STANDS.get());
            e.printStackTrace();
        }
    }

    private JSONArray toJSON(Set<Stand> stands) {
        if (stands.isEmpty()) {
            return null;
        }
        JSONArray array = new JSONArray();
        for (Stand stand : stands) {
            array.add((Object)stand.serialize());
        }
        return array;
    }
}

