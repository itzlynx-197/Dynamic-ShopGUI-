

package com.siliconstudios.dynamicshopguiplus.providers;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.objects.EnchantCost;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.ConfigLoadException;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.EnchantCostLoadException;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

public class EnchantCostProvider {
    private final DynamicShopGUIPlus plugin;
    private final Map<String, EnchantCost> costs = new HashMap<String, EnchantCost>();

    public EnchantCostProvider(DynamicShopGUIPlus plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        this.costs.clear();
        File file = new File(this.plugin.getDataFolder(), "enchant-costs.yml");
        if (!file.exists()) {
            this.plugin.saveResource("enchant-costs.yml", false);
        }
        try {
            YamlConfiguration config = DynamicShopGUIPlus.getInstance().loadConfiguration(file);
            ConfigurationSection enchants = config.getConfigurationSection("enchants");
            if (enchants == null) {
                return;
            }
            for (String key : enchants.getKeys(false)) {
                try {
                    EnchantCost cost = EnchantCost.load(enchants.getConfigurationSection(key));
                    this.costs.put(key.toLowerCase(Locale.ENGLISH), cost);
                }
                catch (EnchantCostLoadException e) {
                    SendMessage.warnMessage("Failed to load enchantment cost from enchant-costs.yml: " + e.getMessage());
                }
            }
            SendMessage.infoMessage(Lang.ENCHANTMENT_COSTS_LOADED.get().replace("%amount%", String.valueOf(this.costs.size())));
        }
        catch (ConfigLoadException e) {
            SendMessage.errorMessage(Lang.ERROR_LOADING_ENCHANTMENT_COSTS.get());
            e.printStackTrace();
        }
    }

    public boolean isEmpty() {
        return this.costs.isEmpty();
    }

    public double getEnchantAddition(ItemStack item, ItemStack itemToGive, double basePrice) {
        if (this.costs.isEmpty()) {
            return 0.0;
        }
        Map<String, Integer> baseLevels = this.getEnchantLevels(itemToGive);
        double addition = 0.0;
        for (Map.Entry<String, Integer> entry : this.getEnchantLevels(item).entrySet()) {
            EnchantCost cost = this.costs.get(entry.getKey());
            if (cost == null) continue;
            addition += cost.getCost(basePrice, entry.getValue(), baseLevels.getOrDefault(entry.getKey(), 0));
        }
        return addition;
    }

    private Map<String, Integer> getEnchantLevels(ItemStack item) {
        HashMap<String, Integer> levels = new HashMap<String, Integer>();
        if (!item.getEnchantments().isEmpty() || item.getType() == Material.ENCHANTED_BOOK) {
            Map<Enchantment,Integer> enchantments = item.getType() == Material.ENCHANTED_BOOK ? ((EnchantmentStorageMeta)item.getItemMeta()).getStoredEnchants() : item.getEnchantments();
            for (Map.Entry<Enchantment,Integer> enchant : enchantments.entrySet()) {
                levels.put(((Enchantment)enchant.getKey()).getKey().getKey(), (Integer)enchant.getValue());
            }
        }
        return levels;
    }
}

