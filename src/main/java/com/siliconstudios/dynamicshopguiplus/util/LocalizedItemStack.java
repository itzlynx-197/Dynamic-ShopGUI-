

package com.siliconstudios.dynamicshopguiplus.util;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.providers.UserManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class LocalizedItemStack {
    private final ItemStack single;
    private final Map<Locale, ItemStack> variants;

    public LocalizedItemStack(ItemStack item) {
        this.single = item;
        this.variants = null;
    }

    public LocalizedItemStack(Map<Locale, ItemStack> variants) {
        this.single = null;
        this.variants = variants;
    }

    public ItemStack getItem(Player p) {
        if (this.single != null) {
            return this.single;
        }
        ItemStack item = this.variants.get(UserManager.getUser(p).getLocale());
        return (item != null ? item : this.variants.get(Lang.getDefaultLocale())).clone();
    }

    public ItemStack getItem() {
        if (this.single != null) {
            return this.single.clone();
        }
        return this.variants.get(Lang.getDefaultLocale()).clone();
    }

    public Map<Locale, ItemStack> getVariants() {
        return this.single != null ? Collections.singletonMap(Lang.getDefaultLocale(), this.single) : this.variants;
    }
}

