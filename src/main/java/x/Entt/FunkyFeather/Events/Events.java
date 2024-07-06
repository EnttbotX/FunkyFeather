package x.Entt.FunkyFeather.Events;

import x.Entt.FunkyFeather.FF;
import x.Entt.FunkyFeather.Utils.FileHandler;
import x.Entt.FunkyFeather.Utils.InvHandler;
import x.Entt.FunkyFeather.Utils.MSG;
import x.Entt.FunkyFeather.Utils.UpdateLogger;

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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

import static x.Entt.FunkyFeather.FF.prefix;

public class Events implements Listener {
    private final boolean hasMethod;
    private final FF plugin;
    private final Economy econ;
    private UpdateLogger updateChecker;
    private final int resourceId = 115289;

    public Events(FF plugin) {
        this.plugin = plugin;
        this.hasMethod = hasMethod("setKeepInventory");
        this.econ = FF.econ;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (player.hasPermission("ff.keep") && hasFF(player)) {
            if (!hasMethod) {
                InvHandler.getInstance().saveInventoryAndArmor(player);
                event.getDrops().clear();
            } else {
                event.setKeepInventory(true);
                event.getDrops().clear();
                event.setKeepLevel(true);
                event.setDroppedExp(0);
            }
        }
    }

    private boolean hasFF(Player player) {
        FileHandler fh = plugin.getFH();
        FileConfiguration config = fh.getConfig();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isFunkyFeather(item, config)) {
                return true;
            }
        }
        return false;
    }

    private boolean isFunkyFeather(ItemStack item, FileConfiguration config) {
        if (item.getType() == Material.FEATHER && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = MSG.color(config.getString("Feather.Name"));
            return item.getItemMeta().getDisplayName().equals(displayName);
        }
        return false;
    }

    private boolean hasMethod(String methodName) {
        for (Method method : PlayerDeathEvent.class.getMethods()) {
            if (method.getName().equals(methodName)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("ff.keep") && hasFF(player)) {
            FileHandler fh = plugin.getFH();
            FileConfiguration config = fh.getConfig();

            if (config.getBoolean("respawn-zone.set-respawn-enabled")) {
                String path = "respawn-zone.zone";
                if (config.contains(path)) {
                    double x = config.getDouble(path + ".x");
                    double y = config.getDouble(path + ".y");
                    double z = config.getDouble(path + ".z");
                    float yaw = (float) config.getDouble(path + ".yaw");
                    float pitch = (float) config.getDouble(path + ".pitch");
                    World world = plugin.getServer().getWorld(Objects.requireNonNull(config.getString(path + ".world")));

                    Location location = new Location(world, x, y, z, yaw, pitch);
                    plugin.getServer().getScheduler().runTask(plugin, () -> player.teleport(location));
                }
            }

            useFeather(player);
        }

        InvHandler ih = InvHandler.getInstance();
        if (ih.hasInventorySaved(player) && ih.hasArmorSaved(player)) {
            player.getInventory().setContents(ih.loadInventory(player));
            player.getInventory().setArmorContents(ih.loadArmor(player));
            ih.removeInventoryAndArmor(player);
        }
    }

    private void useFeather(Player player) {
        FileHandler fh = plugin.getFH();
        FileConfiguration config = fh.getConfig();
        ItemStack feather = findFunkyFeather(player);
        String showName = config.getString("Feather.Name");
        int useCost = fh.getUseCost();

        if (feather != null) {
            if (fh.isVaultIntEnabled()) {
                if (useCost > 0) {
                    if (econ.getBalance(player) >= useCost) {
                        handleFeatherUsage(player, feather, useCost);
                    } else {
                        sendMessage(player, "Messages.no_money");
                    }
                } else {
                    handleFeatherUsage(player, feather, useCost);
                }
            } else {
                handleFeatherUsage(player, feather, 0);
            }
        } else {
            sendMessage(player, "Messages.no_feathers");
        }
    }

    private void handleFeatherUsage(Player player, ItemStack feather, int useCost) {
        FileHandler fh = plugin.getFH();
        int amount = feather.getAmount();
        if (amount > 0) {
            feather.setAmount(amount - 1);
            if (useCost > 0) {
                econ.withdrawPlayer(player, useCost);
            }
            sendMessage(player, "Messages.use");
        } else {
            player.sendMessage(MSG.color(prefix + fh.getMessages().getString("last_feather_used")));
        }
    }

    private void sendMessage(Player player, String path) {
        FileConfiguration config = plugin.getFH().getConfig();
        List<String> messages = config.getStringList(path);
        String messageFormat = String.join("\n", messages);
        player.sendMessage(MSG.color(messageFormat.replace("%ff_name%", Objects.requireNonNull(config.getString("Feather.Name"))).replace("%player%", player.getName())));
    }

    private ItemStack findFunkyFeather(Player player) {
        FileHandler fh = plugin.getFH();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isFunkyFeather(item, fh.getConfig())) {
                return item;
            }
        }
        return null;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                updateChecker = new UpdateLogger(plugin, resourceId);
                if (updateChecker.isUpdateAvailable()) {
                    player.sendMessage(MSG.color(prefix + "&cThere is a new update of the plugin"));
                }
            } catch (Exception e) {
                player.sendMessage(MSG.color(prefix + "&4&lError searching new versions: " + e.getMessage()));
            }
        }, 20 * 20L);
    }
}