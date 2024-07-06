package x.Entt.FunkyFeather.Events;

import me.clip.placeholderapi.PlaceholderAPI;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import x.Entt.FunkyFeather.FF;
import x.Entt.FunkyFeather.Utils.MSG;

import java.util.List;

public class ActionsHandler implements Listener {
    private final FF plugin;

    public ActionsHandler(FF plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        executeActions(player, "die");
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        executeActions(player, "respawn");
    }

    private void executeActions(Player player, String eventType) {
        FileConfiguration config = plugin.getFH().getConfig();
        List<String> actions = config.getStringList("Actions." + eventType + ".do");

        for (String action : actions) {
            if (action.startsWith("command:")) {
                String command = action.replace("command:", "").trim();
                command = PlaceholderAPI.setPlaceholders(player, command);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            } else if (action.startsWith("msg:")) {
                String message = action.replace("msg:", "").trim();
                message = PlaceholderAPI.setPlaceholders(player, message);
                player.sendMessage(MSG.color(message));
            } else if (action.startsWith("c_msg:")) {
                String message = action.replace("c_msg:", "").trim();
                message = PlaceholderAPI.setPlaceholders(player, message);
                Bukkit.broadcastMessage(MSG.color(message));
            }
        }
    }
}