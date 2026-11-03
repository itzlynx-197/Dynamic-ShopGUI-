

package com.siliconstudios.dynamicshopguiplus.objects.stands;

import java.util.Objects;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.util.XMaterial;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.StandLoadException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class StandLoc {
    public final String world;
    public final int x;
    public final double y;
    public final int z;

    public StandLoc(String loc, int standID) throws StandLoadException {
        String[] parts = loc.split(":");
        if (Bukkit.getWorld((String)parts[0]) == null) {
            throw new StandLoadException(Lang.INVALID_SHOP_STAND_WORLD.get().replace("%world%", parts[0]).replace("%id%", String.valueOf(standID)));
        }
        this.world = parts[0];
        try {
            this.x = Integer.parseInt(parts[1]);
            this.y = Double.parseDouble(parts[2]);
            this.z = Integer.parseInt(parts[3]);
        }
        catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            throw new StandLoadException(Lang.INVALID_SHOP_STAND_FORMAT.get().replace("%pos_x%", String.valueOf(parts[1])).replace("%pos_y%", String.valueOf(parts[2])).replace("%pos_z%", String.valueOf(parts[3])).replace("%id%", String.valueOf(standID)));
        }
    }

    public StandLoc(Block block) {
        this.world = block.getLocation().getWorld().getName();
        this.x = block.getLocation().getBlockX();
        this.y = XMaterial.getBlockY(block);
        this.z = block.getLocation().getBlockZ();
    }

    public StandLoc(Location location) {
        this.world = location.getWorld().getName();
        this.x = location.getBlockX();
        this.y = location.getY();
        this.z = location.getBlockZ();
    }

    public Location toBukkit() {
        return new Location(Bukkit.getWorld((String)this.world), (double)this.x, this.y, (double)this.z);
    }

    public String getSimple() {
        return this.world + ":" + this.x + ":" + this.y + ":" + this.z;
    }

    public String toString() {
        return Lang.SHOP_STAND_LOCATION_FORMAT.get().getLegacy().replace("%world%", this.world).replace("%pos_x%", String.valueOf(this.x)).replace("%pos_y%", String.valueOf(this.y)).replace("%pos_z%", String.valueOf(this.z));
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof StandLoc) {
            StandLoc loc = (StandLoc)object;
            return this.world.equals(loc.world) && this.x == loc.x && this.y == loc.y && this.z == loc.z;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(this.world, this.x, this.y, this.z);
    }
}

