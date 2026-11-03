

package com.siliconstudios.dynamicshopguiplus.objects;

import com.siliconstudios.dynamicshopguiplus.util.exceptions.EnchantCostLoadException;
import org.bukkit.configuration.ConfigurationSection;

public class EnchantCost {
    private final double cost;
    private final double percent;
    private final double additionalCost;
    private final double levelMultiplier;
    private final double levelCost;
    private final boolean percentBased;
    private final boolean staticLevelCost;

    private EnchantCost(double cost, double percent, double additionalCost, double levelMultiplier, double levelCost, boolean percentBased, boolean staticLevelCost) {
        this.cost = cost;
        this.percent = percent;
        this.additionalCost = additionalCost;
        this.levelMultiplier = levelMultiplier;
        this.levelCost = levelCost;
        this.percentBased = percentBased;
        this.staticLevelCost = staticLevelCost;
    }

    public static EnchantCost load(ConfigurationSection section) throws EnchantCostLoadException {
        if (section == null) {
            return null;
        }
        boolean usePercent = section.contains("percent");
        if (!usePercent && !section.contains("cost")) {
            throw new EnchantCostLoadException("Failed to load cost, needs either the 'cost' or 'percent' option");
        }
        double percent = section.getDouble("percent", -1.0);
        double cost = section.getDouble("cost", -1.0);
        if (usePercent ? percent < 0.0 : cost < 0.0) {
            throw new EnchantCostLoadException("Cost/percent for enchantment needs to be a positive number");
        }
        double additionalCost = Math.max(0.0, section.getDouble("additional-cost-percent", 0.0));
        boolean staticLevelCost = section.contains("level-cost");
        double levelCost = Math.max(0.0, section.getDouble("level-cost", 0.0));
        double levelMultiplier = Math.max(0.0, section.getDouble("level-multiplier", 1.0));
        return new EnchantCost(cost, percent, additionalCost, levelMultiplier, levelCost, usePercent, staticLevelCost);
    }

    public double getCost(double basePrice, int level) {
        double base = this.percentBased ? basePrice * (this.percent / 100.0) : this.cost + (this.additionalCost > 0.0 ? basePrice * (this.additionalCost / 100.0) : 0.0);
        if (level <= 1) {
            return base;
        }
        if (this.staticLevelCost) {
            return base + this.levelCost * (double)(level - 1);
        }
        return base * (1.0 + (double)(level - 1) * this.levelMultiplier);
    }

    public double getCost(double basePrice, int level, int baseLevel) {
        if (baseLevel >= level) {
            return 0.0;
        }
        double cost = this.getCost(basePrice, level);
        if (baseLevel >= 1) {
            cost -= this.getCost(basePrice, baseLevel);
        }
        return Math.max(0.0, cost);
    }
}

