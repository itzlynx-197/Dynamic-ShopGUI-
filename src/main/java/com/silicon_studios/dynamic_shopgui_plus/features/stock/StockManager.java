package com.silicon_studios.dynamic_shopgui_plus.features.stock;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StockManager {
    private final Map<String, Map<UUID, Integer>> playerUsage = new ConcurrentHashMap<>();
    private final Map<String, Integer> globalUsage = new ConcurrentHashMap<>();
    private final Map<String, Long> windowStart = new ConcurrentHashMap<>();

    public boolean canTransact(String itemPath, Player player, int amount, StockLimit limit) {
        if (limit == null) return true;
        long now = System.currentTimeMillis();
        long ws = windowStart.getOrDefault(itemPath, now);
        if (limit.getWindowTicks() > 0 && (now - ws) > limit.getWindowTicks() * 50L) {
            playerUsage.remove(itemPath);
            globalUsage.remove(itemPath);
            windowStart.put(itemPath, now);
        } else {
            windowStart.putIfAbsent(itemPath, now);
        }
        int used;
        if (limit.getMode() == StockLimit.Mode.PLAYER) {
            used = playerUsage.getOrDefault(itemPath, new ConcurrentHashMap<>()).getOrDefault(player.getUniqueId(), 0);
        } else {
            used = globalUsage.getOrDefault(itemPath, 0);
        }
        return (used + amount) <= limit.getMaxAmount();
    }

    public void record(String itemPath, Player player, int amount, StockLimit limit) {
        if (limit == null) return;
        if (limit.getMode() == StockLimit.Mode.PLAYER) {
            playerUsage.computeIfAbsent(itemPath, k -> new ConcurrentHashMap<>()).merge(player.getUniqueId(), amount, Integer::sum);
        } else {
            globalUsage.merge(itemPath, amount, Integer::sum);
        }
    }
}