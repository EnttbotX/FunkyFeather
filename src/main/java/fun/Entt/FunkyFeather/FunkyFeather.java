package fun.Entt.FunkyFeather;

import fun.Entt.FunkyFeather.commands.MainCMD;
import fun.Entt.FunkyFeather.config.MCM;
import fun.Entt.FunkyFeather.listeners.MainListener;
import fun.Entt.FunkyFeather.utils.MSGU;
import fun.Entt.FunkyFeather.utils.UpdateChecker;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class FunkyFeather extends JavaPlugin {
    public static String prefix = "&e&l[Funky Feather] ";
    private final String version = getDescription().getVersion();
    public static Economy econ = null;
    private UpdateChecker updateChecker;
    private int resourceId = 115289;
    private MCM mcm;

    @Override
    public void onEnable() {
        other.bStats.MetricsLite metrics = new other.bStats.MetricsLite(this);

        try {
            saveDefaultConfig();
            mcm = new MCM(this);
            Bukkit.getConsoleSender().sendMessage
                    (MSGU.color(prefix + "&av" + version + " &2Enabled!"));

            registerCommands();
            registerEvents();
            registerConfig();
            updateCheck();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mcm.isVaultIntEnabled()) {
            if (!setupEconomy()) {
                this.getLogger().severe("Vault integration disabled for FunkyFeather");
                mcm.getConfig().set("Config.Vault.enabled", false);
            }
        }
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage
                (MSGU.color(prefix + "&av" + version + " &cDisabled"));
    }

    public void registerCommands() {
        this.getCommand("funkyfeather").setExecutor(new MainCMD(this));
    }

    public void registerEvents() {
        getServer().getPluginManager().registerEvents(new MainListener(this), this);
    }

    public void registerConfig() {
        FileConfiguration config = getConfig();
        File configFile = new File(getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            config.options().copyDefaults(true);
        }
    }

    public void updateCheck() {
        updateChecker = new UpdateChecker(this, resourceId);

        try {
            if (updateChecker.isUpdateAvailable()) {
                getLogger().info(MSGU.color(prefix + "&cThere is an new update of the plugin"));
            } else {
                getLogger().info(MSGU.color(prefix + "&2Plugin updated"));
            }
        } catch (Exception e) {
            getLogger().warning(MSGU.color(prefix + "&4&lError searching newer versions: " + e.getMessage()));
        }
    }

    public MCM getMCM() {
        return mcm;
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            econ = economyProvider.getProvider();
        } else {
            Bukkit.getConsoleSender().sendMessage(MSGU.color("&cEconomyProvider is null"));
        }

        return (econ != null);
    }
}
