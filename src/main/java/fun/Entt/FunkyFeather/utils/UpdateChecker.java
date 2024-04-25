package fun.Entt.FunkyFeather.utils;

import fun.Entt.FunkyFeather.FunkyFeather;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

public class UpdateChecker {
    private FunkyFeather plugin;
    private int resourceId;

    public UpdateChecker(FunkyFeather plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    public String getLatestVersion() throws IOException {
        URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + resourceId);
        InputStream inputStream = url.openConnection().getInputStream();
        Scanner scanner = new Scanner(inputStream);
        if (scanner.hasNext()) {
            return scanner.next();
        }
        return null;
    }

    public boolean isUpdateAvailable() throws IOException {
        String currentVersion = plugin.getDescription().getVersion();
        String latestVersion = getLatestVersion();
        return latestVersion != null && !latestVersion.equals(currentVersion);
    }
}