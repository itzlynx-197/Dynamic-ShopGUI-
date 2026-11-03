package com.siliconstudios.dynamicshopguiplus.commands.editshop.subcommands.transactionLogs;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.commands.editshop.SubCommad;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.util.PermissionsCache;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Export
extends SubCommad {
    private final DynamicShopGUIPlus plugin;

    public Export(DynamicShopGUIPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "export";
    }

    @Override
    public String getDescription() {
        return "Create a local Transaction Log file of the currently logged transaction";
    }

    @Override
    public String getSyntax() {
        return "/editshop logs export";
    }

    @Override
    public boolean hasPermission(CommandSender source) {
        return PermissionsCache.hasPermission(source, "DynamicShopGUIPlus.eshop.logs." + this.getName());
    }

    @Override
    public void perform(Object logger, String[] args) {
        SendMessage.infoMessage(logger, ChatColor.GREEN + "Exporting transaction data, this can take some time depending on the size of the transactions database...");
        this.plugin.runTaskAsync(() -> this.retrieveAndCreate(logger));
    }

    public void retrieveAndCreate(Object logger) {
        long start = System.currentTimeMillis();
        String exported = this.plugin.getTransactionLog().exportLogs();
        try {
            new Gson().fromJson(exported, JsonObject.class);
        }
        catch (JsonSyntaxException e) {
            SendMessage.errorMessage(logger, "A unknown error occurred while exporting logs data");
            e.printStackTrace();
            return;
        }
        File out = this.saveToFile(exported);
        this.plugin.runTask(() -> {
            SendMessage.infoMessage(logger, "Took " + (System.currentTimeMillis() - start) + "ms to create logs file");
            SendMessage.infoMessage(logger, ChatColor.GREEN + "Saved it here: " + ChatColor.WHITE + out.getAbsolutePath());
        });
    }

    private File saveToFile(String data) {
        File out = new File(this.plugin.getDataFolder(), "logs.json");
        try {
            if (!out.exists()) {
                out.createNewFile();
            }
        }
        catch (IOException e) {
            System.out.println("Failed to create logs.json");
        }
        try (FileWriter file = new FileWriter(out)) {
            file.write(data);
            file.flush();
        }
        catch (IOException e) {
            System.err.println("Failed to save logs to 'logs.json'");
            e.printStackTrace();
        }
        return out;
    }

    @Override
    public List<String> getTabCompletion(String[] args) {
        return null;
    }
}
