

package com.siliconstudios.dynamicshopguiplus.util;

import java.util.Objects;
import java.util.UUID;
import org.bukkit.entity.Player;

public class PermissionHolder {
    final UUID uuid;
    final String world;
    final String permission;

    public PermissionHolder(Player player, String permission) {
        this.uuid = player.getUniqueId();
        this.world = player.getWorld().getName();
        this.permission = permission;
    }

    public UUID getOwner() {
        return this.uuid;
    }

    public String getWorld() {
        return this.world;
    }

    public String getPermission() {
        return this.permission;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object != null && this.getClass() == object.getClass()) {
            PermissionHolder holder = (PermissionHolder)object;
            return this.uuid.equals(holder.getOwner()) && this.permission.equals(holder.getPermission()) && this.world.equals(holder.getWorld());
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(this.uuid, this.permission);
    }
}

