

package com.siliconstudios.dynamicshopguiplus.objects.layouts;

import java.util.HashMap;
import java.util.List;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.objects.inventorys.AuthRequest;
import org.bukkit.Bukkit;
import org.json.simple.JSONObject;

public class SimpleCard {
    private AuthRequest.AuthMethod authMethod;
    private long token;
    private String title;
    private String description;
    private List<String> tags;

    public void setAuthMethod(AuthRequest.AuthMethod authMethod) {
        this.authMethod = authMethod;
    }

    public void setToken(long token) {
        this.token = token;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public List<String> getTags() {
        return this.tags;
    }

    public AuthRequest.AuthMethod getAuthMethod() {
        return this.authMethod;
    }

    public String toString() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("authMethod", this.authMethod.name());
        map.put("token", this.token);
        map.put("title", this.title);
        map.put("description", this.description);
        map.put("tags", this.tags);
        map.put("plVer", DynamicShopGUIPlus.getInstance().getDescription().getVersion());
        map.put("mcVer", Bukkit.getBukkitVersion().split("-")[0]);
        return new JSONObject(map).toString();
    }
}

