package com.silicon_studios.dynamic_shopgui_plus.features.guieditor;

import com.silicon_studios.dynamic_shopgui_plus.DynamicShopGUIPlus;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class EditorListener implements Listener {
    private final DynamicShopGUIPlus plugin;

    public EditorListener(DynamicShopGUIPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        EditorMenu.handleClick(plugin, e);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        EditorMenu.handleClose(e);
    }
}