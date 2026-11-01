package com.silicon_studios.dynamic_shopgui_plus.features.nbothooks;

import com.silicon_studios.dynamic_shopgui_plus.DynamicShopGUIPlus;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class NbtHookManager {
    public interface ItemResolver {
        String getName();
        boolean isAvailable();
        ItemStack resolve(String id);
        String getId(ItemStack item);
    }

    private final DynamicShopGUIPlus plugin;
    private final List<ItemResolver> resolvers = new ArrayList<>();

    public NbtHookManager(DynamicShopGUIPlus plugin) {
        this.plugin = plugin;
        resolvers.add(new ItemsAdderResolver());
        resolvers.add(new OraxenResolver());
        resolvers.add(new NexoResolver());
    }

    public void register(ItemResolver r) { resolvers.add(r); }

    public ItemStack resolve(String id) {
        if (id == null) return null;
        for (ItemResolver r : resolvers) {
            if (!r.isAvailable()) continue;
            try {
                ItemStack it = r.resolve(id);
                if (it != null) return it;
            } catch (Throwable ignored) {}
        }
        return null;
    }

    public String getId(ItemStack item) {
        if (item == null) return null;
        for (ItemResolver r : resolvers) {
            if (!r.isAvailable()) continue;
            try {
                String id = r.getId(item);
                if (id != null) return r.getName() + ":" + id;
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private static class ItemsAdderResolver implements ItemResolver {
        public String getName() { return "itemsadder"; }
        public boolean isAvailable() { return org.bukkit.Bukkit.getPluginManager().getPlugin("ItemsAdder") != null; }
        public ItemStack resolve(String id) {
            try {
                Class<?> c = Class.forName("dev.lone.itemsadder.api.CustomStack");
                Object cs = c.getMethod("getInstance", String.class).invoke(null, id.replace("itemsadder:", ""));
                if (cs == null) return null;
                return (ItemStack) c.getMethod("getItemStack").invoke(cs);
            } catch (Throwable t) { return null; }
        }
        public String getId(ItemStack item) {
            try {
                Class<?> c = Class.forName("dev.lone.itemsadder.api.CustomStack");
                Object cs = c.getMethod("byItemStack", ItemStack.class).invoke(null, item);
                if (cs == null) return null;
                return String.valueOf(c.getMethod("getNamespacedID").invoke(cs));
            } catch (Throwable t) { return null; }
        }
    }

    private static class OraxenResolver implements ItemResolver {
        public String getName() { return "oraxen"; }
        public boolean isAvailable() { return org.bukkit.Bukkit.getPluginManager().getPlugin("Oraxen") != null; }
        public ItemStack resolve(String id) {
            try {
                Class<?> c = Class.forName("io.th0rgal.oraxen.api.OraxenItems");
                Object it = c.getMethod("getItemById", String.class).invoke(null, id.replace("oraxen:", ""));
                if (it == null) return null;
                return (ItemStack) it.getClass().getMethod("build").invoke(it);
            } catch (Throwable t) { return null; }
        }
        public String getId(ItemStack item) {
            try {
                Class<?> c = Class.forName("io.th0rgal.oraxen.api.OraxenItems");
                Object id = c.getMethod("getIdByItem", ItemStack.class).invoke(null, item);
                return id == null ? null : String.valueOf(id);
            } catch (Throwable t) { return null; }
        }
    }

    private static class NexoResolver implements ItemResolver {
        public String getName() { return "nexo"; }
        public boolean isAvailable() { return org.bukkit.Bukkit.getPluginManager().getPlugin("Nexo") != null; }
        public ItemStack resolve(String id) {
            try {
                Class<?> c = Class.forName("com.nexomc.nexo.api.NexoItems");
                Object it = c.getMethod("itemFromId", String.class).invoke(null, id.replace("nexo:", ""));
                return (ItemStack) it;
            } catch (Throwable t) { return null; }
        }
        public String getId(ItemStack item) {
            try {
                Class<?> c = Class.forName("com.nexomc.nexo.api.NexoItems");
                Object id = c.getMethod("idFromItem", ItemStack.class).invoke(null, item);
                return id == null ? null : String.valueOf(id);
            } catch (Throwable t) { return null; }
        }
    }
}