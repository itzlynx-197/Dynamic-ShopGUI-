

package com.siliconstudios.dynamicshopguiplus.commands.base;

import java.util.List;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.util.PermissionsCache;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class PluginCommand
extends BukkitCommand {
    public final DynamicShopGUIPlus plugin;
    private final List<String> disabledWorlds;

    public PluginCommand(DynamicShopGUIPlus plugin, String commandName, String description, String usageMessage, String permission, List<String> disabledWorlds) {
        super(commandName);
        this.description = description;
        this.usageMessage = usageMessage;
        if (permission != null) {
            this.setPermission(permission);
        }
        this.plugin = plugin;
        this.disabledWorlds = disabledWorlds;
    }

    public abstract boolean execute(@NotNull CommandSender var1, @NotNull String var2, @NotNull String[] var3);

    public abstract List<String> tabComplete(CommandSender var1, String var2, String[] var3);

    public boolean canUse(Player player) {
        return this.hasAccessInWorld(player) && this.isAllowedGamemode(player);
    }

    public boolean hasAccessInWorld(Player player) {
        if (!this.disabledWorlds.contains(player.getWorld().getName())) {
            return true;
        }
        SendMessage.chatToPlayer(player, Lang.COMMAND_DISABLED_IN_WORLD.get());
        return false;
    }

    private boolean isAllowedGamemode(Player player) {
        if (this.plugin.bannedGamemodes.contains(player.getGameMode()) && !PermissionsCache.hasPermission(player, "DynamicShopGUIPlus.bypassgamemode")) {
            SendMessage.chatToPlayer(player, Lang.CANNOT_USE_COMMAND_BANNED_GAMEMODE.get().replace("%gamemode%", player.getGameMode().name().toLowerCase()));
            return false;
        }
        return true;
    }
}

