

package com.siliconstudios.dynamicshopguiplus.objects.stands;

import java.util.Objects;

public final class StandSettings {
    private boolean hologram = true;

    public StandSettings(String settings) {
        if (settings == null) {
            return;
        }
        String[] parts = settings.split("\\|");
        this.hologram = parts[0].equals("1");
    }

    public StandSettings() {
    }

    public boolean isHologram() {
        return this.hologram;
    }

    public void toggleHologram() {
        this.hologram = !this.hologram;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.hologram ? 1 : 0).append("|");
        return builder.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        StandSettings that = (StandSettings)o;
        return this.hologram == that.hologram;
    }

    public int hashCode() {
        return Objects.hashCode(this.hologram);
    }
}

