

package com.siliconstudios.dynamicshopguiplus.providers.priceModifiers.seasons;

import java.util.Objects;
import com.siliconstudios.dynamicshopguiplus.providers.priceModifiers.PriceModifier;
import org.bukkit.entity.Player;

public class SeasonModifier
extends PriceModifier {
    private final int modifier;
    private final String world;

    public SeasonModifier(String world, int modifier) {
        this.world = world;
        this.modifier = modifier;
    }

    public String getWorld() {
        return this.world;
    }

    @Override
    public double modify(Player player, double original) {
        if (!player.getWorld().getName().equals(this.world)) {
            return original;
        }
        return original + original / 100.0 * (double)this.modifier;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object != null && this.getClass() == object.getClass()) {
            SeasonModifier modifier = (SeasonModifier)object;
            return this.modifier == modifier.modifier && this.world.equals(modifier.world);
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(this.modifier, this.world);
    }
}

