

package com.siliconstudios.dynamicshopguiplus.providers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.events.stands.StandListener;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.files.StandStorage;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.objects.stands.ChunkLoc;
import com.siliconstudios.dynamicshopguiplus.objects.stands.Stand;
import com.siliconstudios.dynamicshopguiplus.objects.stands.StandLoc;
import com.siliconstudios.dynamicshopguiplus.objects.stands.StandSettings;
import com.siliconstudios.dynamicshopguiplus.objects.stands.StandType;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.StandLoadException;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.StandUnloadException;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class StandProvider {
    private final DynamicShopGUIPlus plugin;
    private final StandStorage storage;
    private final Set<Stand> stands;
    private final Map<UUID, Stand> loaded = new HashMap<UUID, Stand>();
    private final Set<StandLoc> spawnLocations = new HashSet<StandLoc>();

    public StandProvider(DynamicShopGUIPlus plugin) {
        this.plugin = plugin;
        this.storage = new StandStorage(plugin);
        this.stands = this.storage.getStoredStands();
        this.plugin.getServer().getPluginManager().registerEvents((Listener)new StandListener(this.plugin, this), (Plugin)this.plugin);
        SendMessage.infoMessage(Lang.LOADED_SHOP_STAND_MODULE.get().replace("%count%", String.valueOf(this.stands.size())));
        this.plugin.runTaskLater(this::loadSpawnRegions, 20L);
    }

    public Stand getStand(UUID uuid) {
        return this.loaded.get(uuid);
    }

    public Stand getStand(StandLoc loc) {
        return this.stands.stream().filter(e -> e.getLoc().equals(loc)).findFirst().orElse(null);
    }

    public Stand getStand(int id) {
        return this.stands.stream().filter(e -> e.getId() == id).findFirst().orElse(null);
    }

    public Set<Stand> getStands() {
        return this.stands;
    }

    public void createStand(Block block, String item, StandType type) throws StandLoadException {
        StandLoc loc = new StandLoc(block);
        Stand stand = new Stand(this.stands.size() + 1, loc, item, type, new StandSettings());
        UUID uuid = stand.load();
        this.stands.add(stand);
        this.loaded.put(uuid, stand);
    }

    public void loadStands(ChunkLoc chunkLoc) {
        Set<Stand> stands = this.stands.stream().filter(e -> chunkLoc.contains(e.getLoc())).collect(Collectors.toSet());
        this.loadStands(stands);
    }

    public boolean reloadStand(Stand stand) {
        try {
            stand.unload();
            this.loaded.remove(stand.getStand());
        }
        catch (StandUnloadException e) {
            SendMessage.errorMessage(Lang.SHOP_STAND_UNLOAD_FAILED.get().replace("%id%", String.valueOf(stand.getId())).replace("%reason%", e.getMessage()));
            return false;
        }
        this.plugin.runTaskLater(() -> {
            try {
                UUID uuid = stand.load();
                this.loaded.put(uuid, stand);
            }
            catch (StandLoadException e) {
                SendMessage.errorMessage(Lang.SHOP_STAND_LOAD_FAILED.get().replace("%id%", String.valueOf(stand.getId())).replace("%reason%", e.getMessage()));
            }
        }, 2L);
        return true;
    }

    private void loadStands(Set<Stand> stands) {
        for (Stand stand : stands) {
            try {
                UUID uuid = stand.load();
                this.loaded.put(uuid, stand);
            }
            catch (StandLoadException e) {
                SendMessage.errorMessage(Lang.SHOP_STAND_LOAD_FAILED.get().replace("%id%", String.valueOf(stand.getId())).replace("%reason%", e.getMessage()));
            }
        }
    }

    public void checkAndUnload(ChunkLoc chunkLoc) {
        Set<Stand> stands = this.loaded.values().stream().filter(e -> chunkLoc.contains(e.getLoc())).collect(Collectors.toSet());
        this.unloadStands(stands);
    }

    private void unloadStands(Set<Stand> stands) {
        for (Stand stand : stands) {
            try {
                stand.unload();
            }
            catch (StandUnloadException e) {
                SendMessage.errorMessage(Lang.SHOP_STAND_UNLOAD_FAILED.get().replace("%id%", String.valueOf(stand.getId())).replace("%reason%", e.getMessage()));
            }
        }
    }

    private void loadSpawnRegions() {
        for (World world : Bukkit.getWorlds()) {
            this.spawnLocations.add(new StandLoc(world.getSpawnLocation()));
            for (Chunk chunk : world.getLoadedChunks()) {
                this.loadStands(new ChunkLoc(chunk));
            }
        }
    }

    public void destroy(Stand stand) {
        try {
            stand.unload();
            stand.getLoc().toBukkit().getBlock().setType(Material.AIR);
            this.stands.remove(stand);
            this.loaded.remove(stand.getStand());
            SendMessage.infoMessage(Lang.REMOVED_SHOP_STAND.get().replace("%location%", stand.getLoc().toString()));
        }
        catch (StandUnloadException e) {
            SendMessage.errorMessage(Lang.SHOP_STAND_UNLOAD_FAILED.get().replace("%id%", String.valueOf(stand.getId())).replace("%reason%", e.getMessage()));
            SendMessage.errorMessage("Forcing removal...");
            this.unloadInvalid(stand, stand.getLoc().toBukkit());
        }
    }

    private void unloadInvalid(Stand stand, Location loc) {
        loc.add(0.5, 0.5, 0.5);
        Collection<Entity> entitys = loc.getWorld().getNearbyEntities(loc, 1.0, 1.0, 1.0);
        for (Entity e : entitys) {
            if (!(e instanceof Item) || !this.matchesLoc(e.getLocation(), loc)) continue;
            e.remove();
        }
        loc.subtract(0.0, 0.5, 0.0);
        entitys = loc.getWorld().getNearbyEntities(loc, 1.0, 1.0, 1.0);
        for (Entity e : entitys) {
            if (!(e instanceof ArmorStand) || !e.getLocation().equals((Object)loc)) continue;
            e.remove();
        }
        this.stands.remove(stand);
        this.loaded.remove(stand.getStand());
    }

    private boolean matchesLoc(Location loc1, Location loc2) {
        return loc1.getX() == loc2.getX() && loc1.getY() == loc2.getY() && loc1.getZ() == loc2.getZ();
    }

    public void reload() {
        this.unloadStands(new HashSet<Stand>(this.loaded.values()));
        if (!this.loaded.isEmpty()) {
            this.loaded.clear();
        }
        this.loadSpawnRegions();
        SendMessage.infoMessage(Lang.LOADED_SHOP_STAND_MODULE.get().replace("%count%", String.valueOf(this.stands.size())));
    }

    public void stop() {
        for (Stand stand : this.loaded.values()) {
            try {
                stand.unload();
            }
            catch (StandUnloadException e) {
                SendMessage.errorMessage(Lang.SHOP_STAND_UNLOAD_FAILED.get().replace("%id%", String.valueOf(stand.getId())).replace("%reason%", e.getMessage()));
            }
        }
        this.storage.save(this.stands);
    }
}

