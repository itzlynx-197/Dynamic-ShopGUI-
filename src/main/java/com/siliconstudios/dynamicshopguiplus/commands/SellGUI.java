

package com.siliconstudios.dynamicshopguiplus.commands;

import java.util.Collections;
import java.util.List;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.commands.base.PluginCommands;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class SellGUI
extends PluginCommands {
    public SellGUI(DynamicShopGUIPlus plugin, List<String> aliases, List<String> disabledWorlds) {
        super(plugin, aliases.get(0), "Opens the sell GUI", "/" + aliases.remove(0), "DynamicShopGUIPlus.sellgui", aliases, disabledWorlds);
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (this.plugin.badYMLParse != null) {
            SendMessage.warnMessage((Object)sender, "This command cannot be executed now, please fix the configuration formatting first!");
            return true;
        }
        if (sender instanceof Player) {
            Player p = (Player)sender;
            if (!this.canUse(p)) {
                return true;
            }
            new com.siliconstudios.dynamicshopguiplus.objects.SellGUI().open(p);
        } else if (sender instanceof ConsoleCommandSender || sender instanceof BlockCommandSender) {
            Player p = this.plugin.getServer().getPlayer(args[0]);
            if (p != null) {
                new com.siliconstudios.dynamicshopguiplus.objects.SellGUI().open(p);
            } else {
                SendMessage.warnMessage((Object)sender, Lang.REAL_PLAYER.get());
            }
        }
        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender commandSender, String s, String[] args) {
        return Collections.emptyList();
    }
}

