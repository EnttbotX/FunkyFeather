package x.Entt.FunkyFeather.Events;

import me.clip.placeholderapi.PlaceholderAPI;

import x.Entt.FunkyFeather.FF;
import x.Entt.FunkyFeather.Utils.FileHandler;
import x.Entt.FunkyFeather.Utils.InvHandler;
import x.Entt.FunkyFeather.Utils.MSG;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Events implements Listener {
    private final FF plugin;
    private final Economy econ;

    public Events(FF plugin) {
        this.plugin = plugin;
        this.econ = FF.econ;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (player.hasPermission("ff.keep") && hasFunkyFeather(player)) {
            handlePlayerDeath(event, player);
        }

        useFunkyFeather(player);
    }

    private void handlePlayerDeath(PlayerDeathEvent event, Player player) {
        if (player.hasPermission("ff.keep") && hasFunkyFeather(player)) {
            event.setKeepInventory(true);
            event.setKeepLevel(true);
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        FileHandler fh = plugin.getFH();
        Player player = event.getPlayer();
        FileConfiguration config = fh.getConfig();

        if (player.hasPermission("ff.keep") && hasFunkyFeather(player)) {
            if (fh.isSetRespawnEnabled()) {
                String path = "respawn-zone.zone";
                if (config.contains(path)) {
                    World world = plugin.getServer().getWorld(Objects.requireNonNull(config.getString(path + ".world")));
                    if (world != null) {
                        Location location = new Location(
                                world,
                                config.getDouble(path + ".x"),
                                config.getDouble(path + ".y"),
                                config.getDouble(path + ".z"),
                                (float) config.getDouble(path + ".yaw"),
                                (float) config.getDouble(path + ".pitch")
                        );

                        player.teleport(location);
                    }
                }
            }
        }

        restorePlayerInventory(player);
    }

    private void restorePlayerInventory(Player player) {
        InvHandler invHandler = InvHandler.getInstance();
        if (invHandler.hasInventorySaved(player) && invHandler.hasArmorSaved(player)) {
            player.getInventory().setContents(invHandler.loadInventory(player));
            player.getInventory().setArmorContents(invHandler.loadArmor(player));
            invHandler.removeInventoryAndArmor(player);
        }
    }

    private boolean hasFunkyFeather(Player player) {
        FileHandler fh = plugin.getFH();
        return Arrays.stream(player.getInventory().getContents())
                .filter(Objects::nonNull)
                .anyMatch(item -> isFunkyFeather(item, fh.getConfig()));
    }

    private boolean isFunkyFeather(ItemStack item, FileConfiguration config) {
        return item.getType() == Material.FEATHER &&
                item.hasItemMeta() &&
                Objects.requireNonNull(item.getItemMeta()).hasDisplayName() &&
                MSG.color(config.getString("Feather.Name"))
                        .equals(item.getItemMeta().getDisplayName()) &&
                Objects.equals(item.getItemMeta().getLore(),
                        config.getStringList(MSG.color("Feather.Lore")));
    }

    private void useFunkyFeather(Player player) {
        ItemStack feather = findFunkyFeather(player);
        if (feather != null) {
            int useCost = plugin.getFH().getUseCost();
            if (plugin.getFH().isVaultIntEnabled() && useCost > 0) {
                if (econ.getBalance(player) >= useCost) {
                    processFeatherUsage(player, feather, useCost);
                } else {
                    sendPlayerMessage(player, "Messages.no_money");
                }
            } else {
                processFeatherUsage(player, feather, useCost);
            }
        } else {
            sendPlayerMessage(player, "Messages.no_feathers");
        }
    }

    private void processFeatherUsage(Player player, ItemStack feather, int useCost) {
        int amount = feather.getAmount();
        if (amount > 0) {
            feather.setAmount(amount - 1);
            if (useCost > 0) {
                econ.withdrawPlayer(player, useCost);
            }
            sendPlayerMessage(player, "use");
        } else {
            player.sendMessage(MSG.color(plugin.getFH().getMessages().getString("Messages.last_feather_used")));
        }
    }

    private ItemStack findFunkyFeather(Player player) {
        return Arrays.stream(player.getInventory().getContents())
                .filter(Objects::nonNull)
                .filter(item -> isFunkyFeather(item, plugin.getFH().getConfig()))
                .findFirst().orElse(null);
    }

    private void sendPlayerMessage(Player player, String path) {
        FileConfiguration config = plugin.getFH().getConfig();
        List<String> messages = config.getStringList(path);

        String formattedMessage = String.join("\n", messages);

        formattedMessage = formattedMessage.replace("%ff_name%", Objects.requireNonNull(config.getString("Feather.Name")));

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            formattedMessage = PlaceholderAPI.setPlaceholders(player, formattedMessage);
        }

        player.sendMessage(MSG.color(formattedMessage));
    }
}