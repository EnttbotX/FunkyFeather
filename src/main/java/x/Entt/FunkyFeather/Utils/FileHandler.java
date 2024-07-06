package x.Entt.FunkyFeather.Utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import x.Entt.FunkyFeather.FF;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FileHandler {

    private final FF plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private final File configFile;
    private final File messagesFile;

    private String name;
    private List<String> lore;
    private List<String> respawnZone;
    private boolean setRespawnEnabled;
    private boolean vaultIntEnabled;
    private int giveCost;
    private int useCost;

    public FileHandler(FF plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        this.messagesFile = new File(plugin.getDataFolder(), "msg.yml");

        reloadConfig();
        reloadMSGs();
        loadConfigValues();
    }

    public void loadConfigValues() {
        // Load config.yml values
        setRespawnEnabled = config.getBoolean("Config.respawn-zone.enabled");
        vaultIntEnabled = config.getBoolean("Config.Vault.enabled");
        name = config.getString("Config.Feather.Name");
        lore = config.getStringList("Config.Feather.Lore");
        respawnZone = config.getStringList("Config.respawn-zone.zone");
        giveCost = config.getInt("Config.Vault.give-cost");
        useCost = config.getInt("Config.Vault.use-cost");
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Couldn't save config.yml!");
        }
    }

    public void reloadConfig() {
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void reloadMSGs() {
        if (!messagesFile.exists()) {
            plugin.saveResource("msg.yml", false);
        }
        this.messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public List<String> getRespawnZone() {
        return respawnZone;
    }

    public boolean isSetRespawnEnabled() {
        return setRespawnEnabled;
    }

    public boolean isVaultIntEnabled() {
        return vaultIntEnabled;
    }

    public int getGiveCost() {
        return giveCost;
    }

    public int getUseCost() {
        return useCost;
    }
}