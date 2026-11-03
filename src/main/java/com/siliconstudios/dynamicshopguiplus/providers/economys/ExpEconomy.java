

package com.siliconstudios.dynamicshopguiplus.providers.economys;

import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.providers.EconomyProvider;
import com.siliconstudios.dynamicshopguiplus.providers.economys.formatter.PriceFormatter;
import com.siliconstudios.dynamicshopguiplus.util.ChatUtil;
import com.siliconstudios.dynamicshopguiplus.util.EcoType;
import com.siliconstudios.dynamicshopguiplus.util.EconomyType;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.EconomyLoadException;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class ExpEconomy
implements EconomyProvider {
    private String friendly;
    private String singular;
    private String plural;
    private final boolean decimal = false;
    private PriceFormatter.Format priceFormat;

    @Override
    public void setup(DynamicShopGUIPlus plugin) throws EconomyLoadException {
        String path = ConfigManager.getPathIgnoreCase("currency-formats.exp");
        this.formatSingular(path);
        this.formatPlural(path);
        this.friendly = ChatUtil.formatColors(ConfigManager.getConfig().getString(path + ".friendly", Lang.XP_CURRENCY_NAME_PLURAL.get().getLegacy()).replace("%translations-exp-currency-name-plural%", Lang.XP_CURRENCY_NAME_PLURAL.get().getLegacy()));
        this.priceFormat = PriceFormatter.getFormatter(path);
        SendMessage.infoMessage("Successfully enabled EXP points economy");
    }

    @Override
    public double getBalance(OfflinePlayer p) {
        return this.getTotalExperience((Player)p);
    }

    @Override
    public void depositBalance(OfflinePlayer p, double amount) {
        this.setTotalExperience((Player)p, this.getTotalExperience((Player)p) + (int)Math.round(amount));
    }

    @Override
    public void withdrawBalance(OfflinePlayer p, double amount) {
        this.setTotalExperience((Player)p, this.getTotalExperience((Player)p) - (int)Math.round(amount));
    }

    @Override
    public EcoType getType() {
        return new EcoType(EconomyType.EXP);
    }

    @Override
    public String getPlural() {
        return this.plural;
    }

    @Override
    public String getSingular() {
        return this.singular;
    }

    @Override
    public String getFriendly() {
        return this.friendly;
    }

    @Override
    public boolean isDecimal() {
        return this.decimal;
    }

    @Override
    public double round(double value) {
        return this.priceFormat.round(value);
    }

    @Override
    public String formatPrice(double price) {
        return this.priceFormat.format(this, price);
    }

    private void formatSingular(String path) {
        String singular = ChatUtil.formatColors(ConfigManager.getConfig().getString(path + ".singular", Lang.XP_CURRENCY_NAME_SINGULAR.get().getLegacy()).replace("%translations-exp-currency-name-singular%", Lang.XP_CURRENCY_NAME_SINGULAR.get().getLegacy()));
        this.singular = !singular.isEmpty() ? (this.isSymbol(singular) ? Lang.SYMBOL_PRICING_FORMAT.get().getLegacy().replace("%symbol%", singular) : Lang.NAMED_PRICING_FORMAT.get().getLegacy().replace("%currency-format%", singular)) : Lang.CURRENCYSYMBOL.get().getLegacy();
    }

    private void formatPlural(String path) {
        String plural = ChatUtil.formatColors(ConfigManager.getConfig().getString(path + ".plural", Lang.XP_CURRENCY_NAME_PLURAL.get().getLegacy()).replace("%translations-exp-currency-name-plural%", Lang.XP_CURRENCY_NAME_PLURAL.get().getLegacy()));
        this.plural = !plural.isEmpty() ? (this.isSymbol(plural) ? Lang.SYMBOL_PRICING_FORMAT.get().getLegacy().replace("%symbol%", plural) : Lang.NAMED_PRICING_FORMAT.get().getLegacy().replace("%currency-format%", plural)) : Lang.CURRENCYSYMBOL.get().getLegacy();
    }

    private boolean isSymbol(String format) {
        return format.chars().count() == 1L;
    }

    private void setTotalExperience(Player player, int exp) {
        player.setExp(0.0f);
        player.setLevel(0);
        player.setTotalExperience(0);
        int amount = exp;
        while (amount > 0) {
            int expToLevel = this.getExpAtLevel(player);
            if ((amount -= expToLevel) >= 0) {
                player.giveExp(expToLevel);
                continue;
            }
            player.giveExp(amount += expToLevel);
            amount = 0;
        }
    }

    private int getExpAtLevel(Player player) {
        return this.getExpAtLevel(player.getLevel());
    }

    private int getExpAtLevel(int level) {
        if (level <= 15) {
            return 2 * level + 7;
        }
        if (level >= 16 && level <= 30) {
            return 5 * level - 38;
        }
        return 9 * level - 158;
    }

    private int getExpToLevel(int level) {
        int exp = 0;
        for (int currentLevel = 0; currentLevel < level; ++currentLevel) {
            exp += this.getExpAtLevel(currentLevel);
        }
        if (exp < 0) {
            exp = Integer.MAX_VALUE;
        }
        return exp;
    }

    private int getTotalExperience(Player player) {
        int exp = Math.round((float)this.getExpAtLevel(player) * player.getExp());
        int currentLevel = player.getLevel();
        while (currentLevel > 0) {
            exp += this.getExpAtLevel(--currentLevel);
        }
        if (exp < 0) {
            exp = Integer.MAX_VALUE;
        }
        return exp;
    }

    private int getExpUntilNextLevel(Player player) {
        int exp = Math.round((float)this.getExpAtLevel(player) * player.getExp());
        int nextLevel = player.getLevel();
        return this.getExpAtLevel(nextLevel) - exp;
    }
}

