

package com.siliconstudios.dynamicshopguiplus.commands;

import java.util.Collections;
import java.util.List;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.commands.base.PluginCommand;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;

public class Sreload
extends PluginCommand {
    public Sreload(DynamicShopGUIPlus plugin, List<String> disabledWorlds) {
        super(plugin, "sreload", "Reloads the plugin", "/sreload", "DynamicShopGUIPlus.reload", disabledWorlds);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            if (this.hasAccessInWorld(player)) {
                long start = System.currentTimeMillis();
                this.plugin.getConfigManager().reload();
                Lang.CONFIGS_RELOADED.reload();
                this.plugin.getMetaUtils().update();
                this.plugin.getConfigManager().validate();
                if (this.plugin.badYMLParse == null) {
                    if (!this.plugin.getEcoHandler().relead()) {
                        SendMessage.chatToPlayer(player, Lang.ERROR_OCCURRED_WHILE_RELOADING.get(player));
                        return true;
                    }
                    this.plugin.updateClickMappings();
                    this.plugin.startupReload.setupPluginVersion();
                    this.plugin.getSpawnerManager().init();
                    if (this.plugin.getBedrockInventorys() != null) {
                        this.plugin.getBedrockInventorys().reload();
                    }
                    this.plugin.startupReload.checkDebugMode();
                    this.plugin.startupReload.loadInventoryTitles();
                    this.plugin.startupReload.loadItems();
                    this.plugin.reloadModifiers();
                    this.plugin.reloadPlayerData();
                    this.plugin.startupReload.updateAvailable();
                    SendMessage.infoMessage((Object)player, Lang.DONE.get(player).replace("%millis%", String.valueOf(System.currentTimeMillis() - start)));
                } else {
                    SendMessage.errorMessage((Object)player, Lang.ERROR_OCCURRED_WHILE_RELOADING.get(player));
                    SendMessage.errorMessage("Skipping all settings and item loading because the configuration files could not be loaded...");
                }
            }
        } else if (sender instanceof ConsoleCommandSender || sender instanceof BlockCommandSender || sender instanceof RemoteConsoleCommandSender) {
            long start = System.currentTimeMillis();
            this.plugin.getConfigManager().reload();
            Lang.CONFIGS_RELOADED.reload();
            this.plugin.getMetaUtils().update();
            this.plugin.getConfigManager().validate();
            if (this.plugin.badYMLParse == null) {
                if (!this.plugin.getEcoHandler().relead()) {
                    return true;
                }
                this.plugin.updateClickMappings();
                this.plugin.startupReload.setupPluginVersion();
                this.plugin.getSpawnerManager().init();
                if (this.plugin.getBedrockInventorys() != null) {
                    this.plugin.getBedrockInventorys().reload();
                }
                this.plugin.startupReload.checkDebugMode();
                this.plugin.startupReload.loadInventoryTitles();
                this.plugin.startupReload.loadItems();
                this.plugin.reloadModifiers();
                this.plugin.reloadPlayerData();
                this.plugin.startupReload.updateAvailable();
                SendMessage.infoMessage(Lang.DONE.get().replace("%millis%", String.valueOf(System.currentTimeMillis() - start)));
            } else {
                SendMessage.errorMessage("Skipping all settings and item loading because the configuration files could not be loaded...");
            }
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender commandSender, String s, String[] args) {
        return Collections.emptyList();
    }
}

