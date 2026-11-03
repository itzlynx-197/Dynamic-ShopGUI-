

package com.siliconstudios.dynamicshopguiplus.objects.stands;

import com.siliconstudios.dynamicshopguiplus.objects.stands.StandLoc;
import org.bukkit.Chunk;

public class ChunkLoc {
    public final String world;
    public final int x1;
    public final int z1;

    public ChunkLoc(Chunk chunk) {
        this.world = chunk.getWorld().getName();
        this.x1 = chunk.getX();
        this.z1 = chunk.getZ();
    }

    public boolean contains(StandLoc loc) {
        return this.world.equals(loc.world) && this.x1 == loc.x >> 4 && this.z1 == loc.z >> 4;
    }
}

