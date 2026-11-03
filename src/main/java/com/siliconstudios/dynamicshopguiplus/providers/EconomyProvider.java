

package com.siliconstudios.dynamicshopguiplus.providers;

import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.util.EcoType;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.EconomyLoadException;
import org.bukkit.OfflinePlayer;

public interface EconomyProvider {
    public void setup(DynamicShopGUIPlus var1) throws EconomyLoadException;

    public double getBalance(OfflinePlayer var1);

    public void depositBalance(OfflinePlayer var1, double var2);

    public void withdrawBalance(OfflinePlayer var1, double var2);

    public EcoType getType();

    public String getPlural();

    public String getSingular();

    public String getFriendly();

    public boolean isDecimal();

    public double round(double var1);

    public String formatPrice(double var1);
}

