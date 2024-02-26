package fun.Entt.FunkyFeather.Config;

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

    public MCM(FunkyFeather plugin) {
        this.plugin = plugin;
        configFile = new CC("config.yml", null, plugin);
        configFile.registerConfig();
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = configFile.getConfig();
        setRespawnEnabled = config.getBoolean("Config.respawn-zone.set-respawn-enabled");
        name = config.getString("Config.Feather.Name");
        lore = Collections.unmodifiableList(config.getStringList("Config.Feather.Lore"));
        cmsg = config.getString("Config.Feather.Consume-Message");
        respawnZone = Collections.unmodifiableList(config.getStringList("Config.respawn-zone.zone"));
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

    public List<String> getLore() {
        return lore;
    }

    public String getCMSG() {
        return cmsg;
    }

    public boolean isSetRespawnEnabled() {
        return setRespawnEnabled;
    }

    public String getName() {
        return name;
    }
}