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
import java.util.logging.Level;

public class ActionsHandler implements Listener {
    private final FF plugin;

    private static final String COMMAND_PREFIX = "command:";
    private static final String MESSAGE_PREFIX = "msg:";
    private static final String BROADCAST_MESSAGE_PREFIX = "c_msg:";

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

    /**
     * Executes the configured actions for a specific event type.
     *
     * @param player the player involved in the event
     * @param eventType the type of event ("die" or "respawn")
     */
    private void executeActions(Player player, String eventType) {
        FileConfiguration config = plugin.getFH().getConfig();
        List<String> actions = config.getStringList("Actions." + eventType + ".do");

        for (String action : actions) {
            if (action.startsWith(COMMAND_PREFIX)) {
                executeCommand(player, action);
            } else if (action.startsWith(MESSAGE_PREFIX)) {
                sendMessageToPlayer(player, action);
            } else if (action.startsWith(BROADCAST_MESSAGE_PREFIX)) {
                broadcastMessage(player, action);
            }
        }
    }

    /**
     * Executes a console command.
     *
     * @param player the player who triggered the event
     * @param action the command action string
     */
    private void executeCommand(Player player, String action) {
        try {
            String command = action.replace(COMMAND_PREFIX, "").trim();
            command = PlaceholderAPI.setPlaceholders(player, command);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error executing command action for player " + player.getName(), e);
        }
    }

    /**
     * Sends a message to the player.
     *
     * @param player the player to send the message to
     * @param action the message action string
     */
    private void sendMessageToPlayer(Player player, String action) {
        String message = action.replace(MESSAGE_PREFIX, "").trim();
        message = PlaceholderAPI.setPlaceholders(player, message);
        player.sendMessage(MSG.color(message));
    }

    /**
     * Broadcasts a message to all players.
     *
     * @param player the player who triggered the event
     * @param action the broadcast message action string
     */
    private void broadcastMessage(Player player, String action) {
        String message = action.replace(BROADCAST_MESSAGE_PREFIX, "").trim();
        message = PlaceholderAPI.setPlaceholders(player, message);
        Bukkit.broadcastMessage(MSG.color(message));
    }
}