

package com.siliconstudios.dynamicshopguiplus.files.config;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import com.siliconstudios.dynamicshopguiplus.files.ConfigManager;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.ConfigLoadException;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.ConfigSaveException;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class Config
extends YamlConfiguration {
    private final Path path;
    public Configuration defaults;

    public Config(File file) throws ConfigLoadException {
        this.path = file.toPath();
        this.loadConfig(file);
    }

    private void loadConfig(File file) throws ConfigLoadException {
        try {
            this.load(file);
        }
        catch (IOException e) {
            throw new ConfigLoadException("A unknown IOE Exception occurred while loading " + file.getName() + "\n" + this.stackTraceToString(e));
        }
        catch (InvalidConfigurationException e) {
            throw new ConfigLoadException("Invalid Yaml syntax while loading " + file.getName() + " config, use a online Yaml parser to validate the config and fix any errors.\n" + this.stackTraceToString((Exception)((Object)e)));
        }
    }

    private String stackTraceToString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    public void reload() throws ConfigLoadException {
        this.loadConfig(this.path.toFile());
    }

    public void save() throws ConfigSaveException {
        try {
            this.save(this.path.toFile());
        }
        catch (IOException e) {
            throw new ConfigSaveException(Lang.COULD_NOT_SAVE_CONFIG.get().replace("%fileName%", this.path.getFileName().toString()) + "\n" + this.stackTraceToString(e));
        }
    }

    public void saveConfig() {
        try {
            this.save();
            this.reload();
        }
        catch (ConfigLoadException | ConfigSaveException e) {
            SendMessage.errorMessage(e.getMessage());
            ConfigManager.badYaml = this.path.getFileName().toString().split("\\.")[0];
        }
    }

    public void setDef(@NotNull Configuration defaults) {
        this.defaults = defaults;
    }

    public Configuration getDef() {
        return this.defaults;
    }

    public Path getFilePath() {
        return this.path;
    }
}

