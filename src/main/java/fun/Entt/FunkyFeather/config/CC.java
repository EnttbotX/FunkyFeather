package fun.Entt.FunkyFeather.config;

import fun.Entt.FunkyFeather.FunkyFeather;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class CC {
    private FunkyFeather plugin;
    private String fileName;
    private FileConfiguration fileConfiguration = null;
    private File file = null;
    private String folderName;

    public CC(String fileName, String folderName, FunkyFeather plugin) {
        this.fileName = fileName;
        this.folderName = folderName;
        this.plugin = plugin;
    }

    public void registerConfig() {
        file = new File(getFilePath());

        if (!file.exists()) {
            plugin.saveResource(getResourcePath(), false);
        }

        fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }

    public void saveConfig() {
        try {
            fileConfiguration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        if (fileConfiguration == null) {
            reloadConfig();
        }
        return fileConfiguration;
    }

    public void reloadConfig() {
        file = new File(getFilePath());
        fileConfiguration = YamlConfiguration.loadConfiguration(file);

        if (file != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(file);
            fileConfiguration.setDefaults(defConfig);
        }
    }

    private String getResourcePath() {
        if (folderName != null) {
            return folderName + File.separator + fileName;
        } else {
            return fileName;
        }
    }

    private String getFilePath() {
        if (folderName != null) {
            return plugin.getDataFolder() + File.separator + folderName + File.separator + fileName;
        } else {
            return plugin.getDataFolder() + File.separator + fileName;
        }
    }
}