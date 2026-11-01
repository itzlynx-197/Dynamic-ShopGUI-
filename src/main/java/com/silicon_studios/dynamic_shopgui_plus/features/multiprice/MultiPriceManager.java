package com.silicon_studios.dynamic_shopgui_plus.features.multiprice;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiPriceManager {
    public List<PriceTier> loadTiers(ConfigurationSection itemSection) {
        List<PriceTier> tiers = new ArrayList<>();
        if (itemSection == null || !itemSection.contains("prices")) return tiers;
        Object raw = itemSection.get("prices");
        if (!(raw instanceof List)) return tiers;
        int priority = 0;
        for (Object o : (List<?>) raw) {
            if (!(o instanceof ConfigurationSection)) continue;
            ConfigurationSection s = (ConfigurationSection) o;
            String perm = s.getString("permission");
            String cond = s.getString("condition");
            Double buy = s.contains("buy") ? s.getDouble("buy") : null;
            Double sell = s.contains("sell") ? s.getDouble("sell") : null;
            int prio = s.getInt("priority", priority);
            tiers.add(new PriceTier(perm, cond, buy, sell, prio));
            priority++;
        }
        tiers.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
        return tiers;
    }

    public double resolveBuy(List<PriceTier> tiers, org.bukkit.entity.Player player, double baseBuy) {
        if (tiers == null || tiers.isEmpty() || player == null) return baseBuy;
        for (PriceTier t : tiers) {
            if (t.matches(player) && t.getBuy() != null) return t.getBuy();
        }
        return baseBuy;
    }

    public double resolveSell(List<PriceTier> tiers, org.bukkit.entity.Player player, double baseSell) {
        if (tiers == null || tiers.isEmpty() || player == null) return baseSell;
        for (PriceTier t : tiers) {
            if (t.matches(player) && t.getSell() != null) return t.getSell();
        }
        return baseSell;
    }
}