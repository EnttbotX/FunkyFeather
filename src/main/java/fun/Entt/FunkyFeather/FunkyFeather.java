package fun.Entt.FunkyFeather;

import fun.Entt.FunkyFeather.Commands.MainCMD;
import fun.Entt.FunkyFeather.Config.MCM;
import fun.Entt.FunkyFeather.Listeners.MainListener;
import fun.Entt.FunkyFeather.Utils.MSGU;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class FunkyFeather extends JavaPlugin {
    public static String prefix = "&e&l[Funky Feather] ";
    private final String version = getDescription().getVersion();
    private MCM mcm;

    @Override
    public void onEnable() {
        try {
            saveDefaultConfig();
            mcm = new MCM(this);
            Bukkit.getConsoleSender().sendMessage
                    (MSGU.color(prefix + "&av" + version + " &2Enabled!"));

            registerCommands();
            registerEvents();
            registerConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage
                (MSGU.color(prefix + "&av" + version + "&cDisabled"));
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
    public MCM getMCM() {
        return mcm;
    }
}
