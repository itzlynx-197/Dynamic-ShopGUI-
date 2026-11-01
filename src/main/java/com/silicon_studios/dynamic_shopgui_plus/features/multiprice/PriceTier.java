package com.silicon_studios.dynamic_shopgui_plus.features.multiprice;

import org.bukkit.entity.Player;

public class PriceTier {
    private final String permission;
    private final String condition;
    private final Double buy;
    private final Double sell;
    private final int priority;

    public PriceTier(String permission, String condition, Double buy, Double sell, int priority) {
        this.permission = permission;
        this.condition = condition;
        this.buy = buy;
        this.sell = sell;
        this.priority = priority;
    }

    public String getPermission() { return permission; }
    public String getCondition() { return condition; }
    public Double getBuy() { return buy; }
    public Double getSell() { return sell; }
    public int getPriority() { return priority; }

    public boolean matches(Player player) {
        if (permission != null && !permission.isEmpty() && !player.hasPermission(permission)) {
            return false;
        }
        if (condition != null && !condition.isEmpty()) {
            if (condition.startsWith("level")) {
                int lvl = player.getLevel();
                String op = condition.replaceAll("[^<>=!]", "");
                int val = Integer.parseInt(condition.replaceAll("[^0-9]", ""));
                switch (op) {
                    case ">=": if (!(lvl >= val)) return false; break;
                    case ">": if (!(lvl > val)) return false; break;
                    case "<=": if (!(lvl <= val)) return false; break;
                    case "<": if (!(lvl < val)) return false; break;
                    case "==": case "=": if (!(lvl == val)) return false; break;
                    default: return false;
                }
            } else if (condition.startsWith("world:")) {
                if (!player.getWorld().getName().equalsIgnoreCase(condition.substring(6).trim())) return false;
            } else if (condition.startsWith("group:")) {
                String group = condition.substring(6).trim();
                if (com.silicon_studios.dynamic_shopgui_plus.DynamicShopGUIPlus.getInstance().getEconomyHandler() != null) {
                }
                if (!player.hasPermission("group." + group)) return false;
            }
        }
        return true;
    }
}