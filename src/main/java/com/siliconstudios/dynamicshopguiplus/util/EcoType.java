

package com.siliconstudios.dynamicshopguiplus.util;

import java.util.Objects;
import javax.annotation.Nullable;
import com.siliconstudios.dynamicshopguiplus.util.EconomyType;

public class EcoType {
    private final EconomyType type;
    private final String currency;

    public EcoType(EconomyType type) {
        this.type = type;
        this.currency = null;
    }

    public EcoType(EconomyType type, String currency) {
        this.type = type;
        this.currency = currency;
    }

    public EconomyType getType() {
        return this.type;
    }

    @Nullable
    public String getCurrency() {
        return this.currency;
    }

    public String toString() {
        return this.currency == null ? this.type.getName() : this.type.getName() + ":" + this.currency;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object != null && this.getClass() == object.getClass()) {
            EcoType holder = (EcoType)object;
            return this.type.equals((Object)holder.type) && Objects.equals(this.currency, holder.currency);
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.type, this.currency});
    }
}

