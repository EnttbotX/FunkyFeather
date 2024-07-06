package x.Entt.FunkyFeather.Utils;

import net.md_5.bungee.api.ChatColor;

public class MSG {
    public static String color(String message) {
        if (message == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
