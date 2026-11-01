package com.silicon_studios.dynamic_shopgui_plus.features.dynamicpricing;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicPricingManager {
    public static class Config {
        public boolean enabled = false;
        public double sensitivity = 0.001;
        public double minMultiplier = 0.5;
        public double maxMultiplier = 2.0;
        public double decayAmount = 0.05;
    }

    private final Config config = new Config();
    private final Map<String, Double> netFlow = new ConcurrentHashMap<>();
    private final Map<String, Double> multipliers = new ConcurrentHashMap<>();

    public Config getConfig() { return config; }

    public void onBuy(String itemPath, int amount) {
        if (!config.enabled) return;
        netFlow.merge(itemPath, -(double) amount, Double::sum);
        recompute(itemPath);
    }

    public void onSell(String itemPath, int amount) {
        if (!config.enabled) return;
        netFlow.merge(itemPath, (double) amount, Double::sum);
        recompute(itemPath);
    }

    private void recompute(String itemPath) {
        double flow = netFlow.getOrDefault(itemPath, 0.0);
        double mult = 1.0 + flow * config.sensitivity;
        mult = Math.max(config.minMultiplier, Math.min(config.maxMultiplier, mult));
        multipliers.put(itemPath, mult);
    }

    public double getMultiplier(String itemPath) {
        return multipliers.getOrDefault(itemPath, 1.0);
    }

    public double adjustBuy(String itemPath, double base) {
        return base * getMultiplier(itemPath);
    }

    public double adjustSell(String itemPath, double base) {
        return base * getMultiplier(itemPath);
    }

    public void decay() {
        if (!config.enabled) return;
        for (String k : multipliers.keySet()) {
            double m = multipliers.get(k);
            if (m > 1.0) m = Math.max(1.0, m - config.decayAmount);
            else if (m < 1.0) m = Math.min(1.0, m + config.decayAmount);
            multipliers.put(k, m);
        }
    }
}