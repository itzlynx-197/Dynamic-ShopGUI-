

package com.siliconstudios.dynamicshopguiplus.util.exceptions;

import com.siliconstudios.dynamicshopguiplus.files.Translatable;
import org.bukkit.ChatColor;

public class ItemLoadException
extends Exception {
    public ItemLoadException(String reason) {
        super(reason);
    }

    public ItemLoadException(Translatable reason) {
        super(ChatColor.stripColor((String)reason.getLegacy()));
    }
}

