

package com.siliconstudios.dynamicshopguiplus.objects.layouts;

import java.util.ArrayList;
import com.siliconstudios.dynamicshopguiplus.util.JsonUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public class SimpleLayout {
    public String title;
    public String authMethod;
    public String description;
    public ArrayList<String> tags;
    public ArrayList<String> files;
    public String plVer;
    public String mcVer;
    public boolean requiresAuthorization;
    public long creation;
    public int downloads;

    public SimpleLayout(String s) throws ParseException {
        JSONObject json = JsonUtil.parseJson(s);
        this.title = JsonUtil.getString(json, "title");
        this.authMethod = JsonUtil.getString(json, "authMethod");
        this.description = JsonUtil.getString(json, "description");
        this.tags = JsonUtil.getArray(json, "tags");
        this.files = JsonUtil.getArray(json, "files");
        this.plVer = JsonUtil.getString(json, "plVer");
        this.mcVer = JsonUtil.getString(json, "mcVer");
        this.requiresAuthorization = (Boolean)json.get((Object)"requiresAuthorization");
        this.creation = (Long)json.get((Object)"creation");
        this.downloads = ((Long)json.get((Object)"downloads")).intValue();
    }
}

