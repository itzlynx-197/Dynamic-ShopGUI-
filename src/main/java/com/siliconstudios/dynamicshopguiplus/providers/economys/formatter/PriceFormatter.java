

package com.siliconstudios.dynamicshopguiplus.providers.economys.formatter;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.providers.EconomyProvider;
import com.siliconstudios.dynamicshopguiplus.util.EconomyHandler;
import org.bukkit.configuration.ConfigurationSection;

public class PriceFormatter {
    public static Format getFormatter() {
        Locale locale = Locale.forLanguageTag(ConfigManager.getConfig().getString("locale", "en-US"));
        NumberFormat format = PriceFormatter.getCurrencyFormat(locale, ConfigManager.getConfig().getString("currency-format", "#,##0.00"), "currency-format");
        List<String> magnitudes = PriceFormatter.getMagnitudes((String)null);
        return magnitudes.isEmpty() ? new LongFormat(locale, format) : new ShortFormat(locale, format, magnitudes.toArray(new String[0]));
    }

    public static Format getFormatter(String path) {
        if (!(ConfigManager.getConfig().contains(path + ".format") || ConfigManager.getConfig().contains(path + ".locale") || ConfigManager.getConfig().contains(path + ".abbreviations"))) {
            return EconomyHandler.getDefaultFormatter();
        }
        Locale locale = Locale.forLanguageTag(PriceFormatter.getLocale(path));
        NumberFormat format = PriceFormatter.getCurrencyFormat(locale, PriceFormatter.getFormat(path), path + ".format");
        List<String> magnitudes = PriceFormatter.getMagnitudes(path);
        return magnitudes.isEmpty() ? new LongFormat(locale, format) : new ShortFormat(locale, format, magnitudes.toArray(new String[0]));
    }

    private static String getFormat(String path) {
        String defFormat = ConfigManager.getConfig().getString("currency-format", "#,##0.00");
        return ConfigManager.getConfig().getString(path + ".format", defFormat);
    }

    private static String getLocale(String path) {
        String defLocale = ConfigManager.getConfig().getString("locale", "en-US");
        return ConfigManager.getConfig().getString(path + ".locale", defLocale);
    }

    private static List<String> getMagnitudes(@Nullable String path) {
        if (path != null && ConfigManager.getConfig().contains(path + ".abbreviations.enabled")) {
            if (ConfigManager.getConfig().getBoolean(path + ".abbreviations.enabled")) {
                return ConfigManager.getConfig().getStringList(path + ".abbreviations.formats");
            }
            return new ArrayList<String>();
        }
        if (ConfigManager.getConfig().getBoolean("abbreviations.enabled")) {
            return ConfigManager.getConfig().getStringList("abbreviations.formats");
        }
        return new ArrayList<String>();
    }

    private static List<String> getMagnitudes(ConfigurationSection section) {
        ArrayList<String> formats = new ArrayList<String>();
        for (String magnitude : section.getKeys(false)) {
            formats.add(section.getString(magnitude));
        }
        return formats;
    }

    private static NumberFormat getCurrencyFormat(Locale loc, String format, String path) {
        DecimalFormat currencyFormat;
        try {
            currencyFormat = new DecimalFormat(format, DecimalFormatSymbols.getInstance(loc));
        }
        catch (IllegalArgumentException e) {
            SendMessage.warnMessage("Could not load currency format of '" + format + "' at " + path + ", with reason: " + e.getMessage());
            currencyFormat = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(loc));
        }
        currencyFormat.setRoundingMode(RoundingMode.FLOOR);
        return currencyFormat;
    }

    public static final class LongFormat
    implements Format {
        private final int decimalPoints;
        private final Locale locale;
        private final NumberFormat format;

        public LongFormat(Locale locale, NumberFormat format) {
            this.locale = locale;
            this.format = format;
            this.decimalPoints = format.getMaximumFractionDigits();
        }

        @Override
        public String format(EconomyProvider provider, double amount) {
            if (!provider.isDecimal()) {
                return (amount == 1.0 ? provider.getSingular() : provider.getPlural()).replace("%price%", String.format(this.locale, "%,.0f", (double)Math.round(amount)));
            }
            return (amount == 1.0 ? provider.getSingular() : provider.getPlural()).replace("%price%", this.format.format(amount));
        }

        @Override
        public double round(double value) {
            long factor = (long)Math.pow(10.0, this.decimalPoints);
            long tmp = Math.round(value *= (double)factor);
            return (double)tmp / (double)factor;
        }
    }

    public static final class ShortFormat
    implements Format {
        private final int decimalPoints;
        private final Locale locale;
        private final NumberFormat format;
        private final String[] magnitudes;

        public ShortFormat(Locale locale, NumberFormat format, String[] magnitudes) {
            this.locale = locale;
            this.format = format;
            this.magnitudes = magnitudes;
            this.decimalPoints = format.getMaximumFractionDigits();
        }

        @Override
        public String format(EconomyProvider provider, double amount) {
            if (amount >= 1000.0) {
                return provider.getPlural().replace("%price%", this.formatShort(amount));
            }
            if (!provider.isDecimal()) {
                return (amount == 1.0 ? provider.getSingular() : provider.getPlural()).replace("%price%", String.format(this.locale, "%,.0f", (double)Math.round(amount)));
            }
            return (amount == 1.0 ? provider.getSingular() : provider.getPlural()).replace("%price%", this.format.format(amount));
        }

        @Override
        public double round(double value) {
            long factor = (long)Math.pow(10.0, this.decimalPoints);
            long tmp = Math.round(value *= (double)factor);
            return (double)tmp / (double)factor;
        }

        private String formatShort(double amount) {
            int i = 0;
            while (true) {
                if ((amount /= 1000.0) < 1000.0) {
                    if (amount == (double)((int)amount)) {
                        return (int)amount + this.magnitudes[i];
                    }
                    return String.format(this.locale, "%,.1f", amount) + this.magnitudes[i];
                }
                ++i;
            }
        }
    }

    public static interface Format {
        public String format(EconomyProvider var1, double var2);

        public double round(double var1);
    }
}

