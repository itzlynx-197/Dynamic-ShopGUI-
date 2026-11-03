

package com.siliconstudios.dynamicshopguiplus.files.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.files.config.Config;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.ConfigLoadException;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.ConfigSaveException;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.yaml.snakeyaml.Yaml;

public class CommentedConfig
extends Config {
    private final Set<String> specialKeys = new HashSet<String>(Arrays.asList("display-lore-layout", "visual-transaction-limits", "disable-back", "click-mappings"));
    private final Map<String, String> comments;
    private boolean reload = false;

    public CommentedConfig(DynamicShopGUIPlus plugin, File file, InputStream def) throws ConfigLoadException {
        super(file);
        Map<String, String> comments = new HashMap<String, String>();
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(def, StandardCharsets.UTF_8));
            comments = this.getComments(input.lines().collect(Collectors.toList()));
            input.close();
        }
        catch (FileNotFoundException input) {
        }
        catch (IOException e) {
            throw new ConfigLoadException("A unknown IOE Exception occurred while loading " + file.getName() + "\n" + this.stackTraceToString(e));
        }
        this.comments = comments;
        this.setDef((Configuration)plugin.loadConfiguration(new BufferedReader(new InputStreamReader(plugin.getResource(file.getName()), StandardCharsets.UTF_8)), file.getName()));
    }

    private Set<String> getKeys() throws InvalidConfigurationException {
        LinkedHashSet<String> results = new LinkedHashSet<String>();
        Set old = this.getKeys(false);
        old.removeAll(results);
        for (String def : this.defaults.getKeys(false)) {
            results.add(def);
            Object value = this.get(def);
            if (value instanceof MemorySection) {
                for (String deepKey2 : (this.specialKeys.contains(def) ? this.getDef() : this).getConfigurationSection(def).getKeys(true)) {
                    results.add(def + "." + deepKey2);
                }
                continue;
            }
            if (value != null) continue;
            ConfigurationSection section = this.getDef().getConfigurationSection(def);
            if (section != null) {
                section.getKeys(true).forEach(deepKey -> results.add(def + "." + deepKey));
            }
            this.reload = true;
        }
        results.addAll(old);
        return results;
    }

    @Override
    public void save() throws ConfigSaveException {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(this.getFilePath(), new OpenOption[0]), StandardCharsets.UTF_8));
            Yaml yaml = new Yaml();
            for (String key : this.getKeys()) {
                Object obj;
                String[] keys = key.split("\\.");
                String actualKey = keys[keys.length - 1];
                String comment = this.comments.get(key);
                StringBuilder prefixBuilder = new StringBuilder();
                int indents = keys.length - 1;
                this.appendPrefixSpaces(prefixBuilder, indents);
                String prefixSpaces = prefixBuilder.toString();
                if (comment != null) {
                    writer.write(comment);
                }
                if ((obj = this.get(key, this.getDef().get(key))) instanceof ConfigurationSerializable) {
                    writer.write(prefixSpaces + actualKey + ": " + yaml.dump((Object)((ConfigurationSerializable)obj).serialize()));
                    continue;
                }
                if (obj instanceof String || obj instanceof Character) {
                    if (obj instanceof String && ((String)obj).contains("\n")) {
                        writer.write(prefixSpaces + actualKey + ": \"" + ((String)obj).replace("\n", "\\n") + "\"\n");
                        continue;
                    }
                    String raw = yaml.dump(obj);
                    if (!raw.startsWith("|")) {
                        writer.write(prefixSpaces + actualKey + ": " + raw);
                        continue;
                    }
                    String[] lines = raw.substring(0, raw.length() - 1).split("\n");
                    writer.write(prefixSpaces + actualKey + ": " + lines[0] + "\n");
                    for (int i = 1; i < lines.length; ++i) {
                        writer.write(prefixSpaces + "  " + lines[i] + "\n");
                    }
                    continue;
                }
                if (obj instanceof List) {
                    writer.write(this.getListAsString((List)obj, actualKey, prefixSpaces));
                    continue;
                }
                if (obj instanceof MemorySection) {
                    writer.write(prefixSpaces + actualKey + ":\n");
                    continue;
                }
                writer.write(prefixSpaces + actualKey + ": " + yaml.dump(obj));
            }
            String danglingComments = this.comments.get(null);
            if (danglingComments != null) {
                writer.write(danglingComments);
            }
            writer.close();
            if (this.reload) {
                this.reload = false;
                this.reload();
            }
        }
        catch (IOException | ConfigLoadException | InvalidConfigurationException e) {
            throw new ConfigSaveException(Lang.COULD_NOT_SAVE_CONFIG.get().replace("%fileName%", this.getFilePath().getFileName().toString()) + "\n" + this.stackTraceToString((Exception)e));
        }
    }

    private String stackTraceToString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    private Map<String, String> getComments(List<String> lines) {
        HashMap<String, String> comments = new HashMap<String, String>();
        StringBuilder builder = new StringBuilder();
        StringBuilder keyBuilder = new StringBuilder();
        int lastLineIndentCount = 0;
        for (String line : lines) {
            if (line != null && line.trim().startsWith("-")) continue;
            if (line == null || line.trim().equals("") || line.trim().startsWith("#")) {
                builder.append(line).append("\n");
                continue;
            }
            lastLineIndentCount = this.setFullKey(keyBuilder, line, lastLineIndentCount);
            if (keyBuilder.length() <= 0) continue;
            comments.put(keyBuilder.toString(), builder.toString());
            builder.setLength(0);
        }
        if (builder.length() > 0) {
            comments.put(null, builder.toString());
        }
        return comments;
    }

    private String getListAsString(List list, String actualKey, String prefixSpaces) {
        StringBuilder builder = new StringBuilder(prefixSpaces).append(actualKey).append(":");
        if (list.isEmpty()) {
            builder.append(" []\n");
            return builder.toString();
        }
        builder.append("\n");
        for (int i = 0; i < list.size(); ++i) {
            Object o = list.get(i);
            if (o instanceof String || o instanceof Character) {
                String s = o.toString();
                if (s.contains("\"") || s.contains("'")) {
                    s = s.replace("'", "''");
                }
                builder.append(prefixSpaces).append("- '").append(s).append("'");
            } else if (o instanceof List) {
                builder.append(prefixSpaces).append("- ").append(new Yaml().dump(o));
            } else {
                builder.append(prefixSpaces).append("- ").append(o);
            }
            if (i == list.size()) continue;
            builder.append("\n");
        }
        return builder.toString();
    }

    private int setFullKey(StringBuilder keyBuilder, String configLine, int lastLineIndentCount) {
        int currentIndents = this.countIndents(configLine);
        String key = configLine.trim().split(":")[0];
        if (keyBuilder.length() == 0) {
            keyBuilder.append(key);
        } else if (currentIndents == lastLineIndentCount) {
            this.removeLastKey(keyBuilder);
            if (keyBuilder.length() > 0) {
                keyBuilder.append(".");
            }
            keyBuilder.append(key);
        } else if (currentIndents > lastLineIndentCount) {
            keyBuilder.append(".").append(key);
        } else {
            int difference = lastLineIndentCount - currentIndents;
            for (int i = 0; i < difference + 1; ++i) {
                this.removeLastKey(keyBuilder);
            }
            if (keyBuilder.length() > 0) {
                keyBuilder.append(".");
            }
            keyBuilder.append(key);
        }
        return currentIndents;
    }

    private int countIndents(String s) {
        int spaces = 0;
        for (char c : s.toCharArray()) {
            if (c != ' ') break;
            ++spaces;
        }
        return spaces / 2;
    }

    private void removeLastKey(StringBuilder keyBuilder) {
        String temp = keyBuilder.toString();
        String[] keys = temp.split("\\.");
        if (keys.length == 1) {
            keyBuilder.setLength(0);
            return;
        }
        temp = temp.substring(0, temp.length() - keys[keys.length - 1].length() - 1);
        keyBuilder.setLength(temp.length());
    }

    private String getPrefixSpaces(int indents) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < indents; ++i) {
            builder.append("  ");
        }
        return builder.toString();
    }

    private void appendPrefixSpaces(StringBuilder builder, int indents) {
        builder.append(this.getPrefixSpaces(indents));
    }

    private final class ConfigurationDefaults {
        public Configuration defaults;

        public ConfigurationDefaults(Configuration defaults) {
            this.defaults = defaults;
        }
    }
}

