package fun.Entt.FunkyFeather.Utils;

import org.bukkit.ChatColor;

public class MSGU {
    public static String color(String message) {
        if (message == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
