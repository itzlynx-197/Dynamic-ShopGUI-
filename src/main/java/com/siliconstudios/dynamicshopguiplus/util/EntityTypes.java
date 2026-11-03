

package com.siliconstudios.dynamicshopguiplus.util;

import java.util.Locale;
import java.util.Optional;
import com.siliconstudios.dynamicshopguiplus.util.ServerInfo;
import org.bukkit.entity.EntityType;

public class EntityTypes {
    public static Optional<EntityType> matchEntityType(String name) {
        name = name.replace(" ", "_").toUpperCase(Locale.ENGLISH);
        if (ServerInfo.supportsComponents()) {
            switch (name) {
                case "MUSHROOM_COW": {
                    name = "MOOSHROOM";
                    break;
                }
                case "SNOWMAN": {
                    name = "SNOW_GOLEM";
                }
            }
        }
        try {
            return Optional.of(EntityType.valueOf((String)name));
        }
        catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}

