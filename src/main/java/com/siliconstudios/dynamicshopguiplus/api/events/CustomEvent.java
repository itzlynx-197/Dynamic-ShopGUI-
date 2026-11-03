

package com.siliconstudios.dynamicshopguiplus.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CustomEvent
extends Event {
    private static final HandlerList handlerList = new HandlerList();

    @NotNull
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}

