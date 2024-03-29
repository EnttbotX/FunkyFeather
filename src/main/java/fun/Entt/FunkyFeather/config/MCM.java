package fun.Entt.FunkyFeather.config;

import fun.Entt.FunkyFeather.FunkyFeather;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.List;

public class MCM {

    private CC configFile;
    private FunkyFeather plugin;
    private String name;
    private List<String> lore;
    private String cmsg;
    private List<String> respawnZone;
    private boolean setRespawnEnabled;
    private boolean vaultIntEnabled;
    private int giveCost;
    private int useCost;

    public MCM(FunkyFeather plugin) {
        this.plugin = plugin;
        configFile = new CC("config.yml", null, plugin);
        configFile.registerConfig();
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = configFile.getConfig();
        setRespawnEnabled = config.getBoolean("Config.respawn-zone.enabled");
        vaultIntEnabled = config.getBoolean("Config.Vault.enabled");
        name = config.getString("Config.Feather.Name");
        lore = Collections.unmodifiableList(config.getStringList("Config.Feather.Lore"));
        cmsg = config.getString("Config.Feather.Consume-Message");
        respawnZone = Collections.unmodifiableList(config.getStringList("Config.respawn-zone.zone"));
        giveCost = config.getInt("Config.Vault.give-cost");
        useCost = config.getInt("Config.Vault.use-cost");
    }

    public FileConfiguration getConfig() {
        return configFile.getConfig();
    }

    public void saveConfig() {
        configFile.saveConfig();
    }

    public void reloadConfig() {
        configFile.reloadConfig();
        loadConfig();
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