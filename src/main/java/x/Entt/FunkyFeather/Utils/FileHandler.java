package x.Entt.FunkyFeather.Utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import x.Entt.FunkyFeather.FF;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

public class FileHandler {
    private final FF plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private final File configFile;
    private final File messagesFile;

    private static final String RESPAWN_ENABLED_PATH = "Config.respawn-zone.enabled";
    private static final String VAULT_ENABLED_PATH = "Config.Vault.enabled";
    private static final String FEATHER_NAME_PATH = "Config.Feather.Name";
    private static final String FEATHER_LORE_PATH = "Config.Feather.Lore";
    private static final String RESPAWN_ZONE_PATH = "Config.respawn-zone.zone";
    private static final String VAULT_GIVE_COST_PATH = "Config.Vault.give-cost";
    private static final String VAULT_USE_COST_PATH = "Config.Vault.use-cost";

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
        reloadMessages();
        loadConfigValues();
    }

    private void loadConfigValues() {
        setRespawnEnabled = config.getBoolean(RESPAWN_ENABLED_PATH, false);
        vaultIntEnabled = config.getBoolean(VAULT_ENABLED_PATH, false);
        name = config.getString(FEATHER_NAME_PATH, MSG.color("&5FunkyFeather"));
        lore = config.getStringList(FEATHER_LORE_PATH);
        respawnZone = config.getStringList(RESPAWN_ZONE_PATH);
        giveCost = config.getInt(VAULT_GIVE_COST_PATH, 0);
        useCost = config.getInt(VAULT_USE_COST_PATH, 0);
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Couldn't save config.yml!", e);
        }
    }

    public void reloadConfig() {
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(configFile);
        loadConfigValues();
    }

    public void reloadMessages() {
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

    public Optional<String> getName() {
        return Optional.ofNullable(name);
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

    public boolean isVaultIntEnabled() {return vaultIntEnabled;}

    public int getGiveCost() {
        return giveCost;
    }

    public int getUseCost() {
        return useCost;
    }
}