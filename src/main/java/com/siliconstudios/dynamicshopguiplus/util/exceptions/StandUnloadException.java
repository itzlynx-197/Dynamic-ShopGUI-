

package com.siliconstudios.dynamicshopguiplus.util.exceptions;

import com.siliconstudios.dynamicshopguiplus.files.Translatable;
import org.bukkit.ChatColor;

public class StandUnloadException
extends Exception {
    public StandUnloadException(String reason) {
        super(reason);
    }

    public StandUnloadException(Translatable reason) {
        super(ChatColor.stripColor((String)reason.getLegacy()));
    }
}

