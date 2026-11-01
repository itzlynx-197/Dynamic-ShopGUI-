package com.silicon_studios.dynamic_shopgui_plus.features.commanditems;

import com.silicon_studios.dynamic_shopgui_plus.DynamicShopGUIPlus;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DelayedCommandScheduler {
    private final DynamicShopGUIPlus plugin;
    private final Map<String, BukkitRunnable> tasks = new ConcurrentHashMap<>();

    public DelayedCommandScheduler(DynamicShopGUIPlus plugin) {
        this.plugin = plugin;
    }

    public void schedule(UUID player, String command, long delayTicks, long repeatTicks, int repetitions, boolean asConsole) {
        String key = player + ":" + command + ":" + System.nanoTime();
        BukkitRunnable task = new BukkitRunnable() {
            int remaining = repetitions;
            @Override
            public void run() {
                Player p = Bukkit.getPlayer(player);
                String cmd = command.replace("%player%", p != null ? p.getName() : player.toString());
                if (asConsole) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                } else if (p != null) {
                    p.performCommand(cmd);
                }
                if (repeatTicks <= 0) {
                    cancel();
                    tasks.remove(key);
                    return;
                }
                if (repetitions > 0 && --remaining <= 0) {
                    cancel();
                    tasks.remove(key);
                }
            }
        };
        if (repeatTicks > 0) {
            task.runTaskTimer(plugin, delayTicks, repeatTicks);
        } else {
            task.runTaskLater(plugin, delayTicks);
        }
        tasks.put(key, task);
    }

    public void cancelAll() {
        for (BukkitRunnable t : tasks.values()) t.cancel();
        tasks.clear();
    }
}