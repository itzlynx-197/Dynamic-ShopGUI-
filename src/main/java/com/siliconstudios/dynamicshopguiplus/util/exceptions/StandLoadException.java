

package com.siliconstudios.dynamicshopguiplus.util.exceptions;

import com.siliconstudios.dynamicshopguiplus.files.Translatable;
import org.bukkit.ChatColor;

public class StandLoadException
extends Exception {
    public StandLoadException(String reason) {
        super(reason);
    }

    public StandLoadException(Translatable reason) {
        super(ChatColor.stripColor((String)reason.getLegacy()));
    }
}

