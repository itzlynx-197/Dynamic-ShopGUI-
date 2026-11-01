package com.silicon_studios.dynamic_shopgui_plus.util;

import com.silicon_studios.dynamic_shopgui_plus.DynamicShopGUIPlus;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public class Scheduler {
    private final DynamicShopGUIPlus plugin;

    public Scheduler(DynamicShopGUIPlus plugin) {
        this.plugin = plugin;
    }

    public void runAsync(Runnable task) {
        if (isFolia()) {
            plugin.getServer().getGlobalRegionScheduler().run(plugin, (t) -> task.run());
        } else {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    public void runLater(Runnable task, long delayTicks) {
        if (isFolia()) {
            plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, (t) -> task.run(), delayTicks);
        } else {
            plugin.getServer().getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    public void runTimer(Runnable task, long delayTicks, long periodTicks) {
        if (isFolia()) {
            plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, (t) -> task.run(), delayTicks, periodTicks);
        } else {
            plugin.getServer().getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
        }
    }

    public void runSync(Runnable task) {
        if (isFolia()) {
            plugin.getServer().getGlobalRegionScheduler().run(plugin, (t) -> task.run());
        } else {
            plugin.getServer().getScheduler().runTask(plugin, task);
        }
    }

    private boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}