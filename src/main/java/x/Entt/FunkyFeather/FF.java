package x.Entt.FunkyFeather;

import x.Entt.FunkyFeather.CMDs.CMDs;
import x.Entt.FunkyFeather.Events.ActionsHandler;
import x.Entt.FunkyFeather.Utils.*;
import x.Entt.FunkyFeather.Events.Events;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public class FF extends JavaPlugin {
    public static String prefix;
    private final String version = getDescription().getVersion();
    public static Economy econ = null;
    private UpdateLogger updateChecker;
    private FileHandler fh;
    private int resourceId = 115289;
    private int bstats = 21442;

    @Override
    public void onEnable() {
        try {
            updateCheck();

            saveDefaultConfig();
            this.fh = new FileHandler(this);

            prefix = fh.getMessages().getString("prefix");

            Bukkit.getConsoleSender().sendMessage
                    (MSG.color(prefix + "&av" + version + " &2Enabled!"));

            registerCommands();
            registerMetrics();
            registerEvents();
            registerFiles();
            registerPAPI();

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (fh.isVaultIntEnabled()) {
            FileConfiguration config = fh.getConfig();
            if (!setupEconomy()) {
                this.getLogger().severe(MSG.color(prefix + "&cVault is null, disabling in &5FunkyFeather &dv" + version));
                config.set("Vault.enabled", false);
                fh.saveConfig();
            }
        }
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage
                (MSG.color(prefix + "&av" + version + " &cDisabled"));
    }

    public void registerCommands() {
        Objects.requireNonNull(this.getCommand("funkyfeather")).setExecutor(new CMDs(this));
    }

    public void registerEvents() {
        getServer().getPluginManager().registerEvents(new Events(this), this);
        getServer().getPluginManager().registerEvents(new ActionsHandler(this), this);
    }

    public void registerFiles() {
        FileConfiguration config = getConfig();
        File configFile = new File(getDataFolder(), "config.yml");
        FileConfiguration msg = fh.getMessages();
        File msgs = new File(getDataFolder(), "msg.yml");

        if (!configFile.exists()) {
            config.options().copyDefaults(true);
        }
        if (!msgs.exists()) {
            msg.options().copyDefaults();
        }
    }

    public void registerPAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().warning(MSG.color(prefix + "&cCould not find PlaceholderAPI!"));
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PAPI(this).register();
        }
    }

    public void registerMetrics() {
        Metrics metrics = new Metrics(this, bstats);

        metrics.addCustomChart(new Metrics.SimplePie("vault_enabled", () -> {
            if (fh.isVaultIntEnabled()) {
                return String.valueOf(true);
            } else {
                return String.valueOf(false);
            }
        }));
    }

    public void updateCheck() {
        updateChecker = new UpdateLogger(this, resourceId);

        try {
            if (updateChecker.isUpdateAvailable()) {
                getLogger().info(MSG.color(prefix + "&cThere is an new update of the plugin"));
            } else {
                getLogger().info(MSG.color(prefix + "&2Plugin updated"));
            }
        } catch (Exception e) {
            getLogger().warning(MSG.color(prefix + "&4&lError searching newer versions: " + e.getMessage()));
        }
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            econ = economyProvider.getProvider();
        } else {
            Bukkit.getConsoleSender().sendMessage(MSG.color(prefix + "&cEconomyProvider is null"));
        }

        return (econ != null);
    }

    public FileHandler getFH() {
        return fh;
    }
}
