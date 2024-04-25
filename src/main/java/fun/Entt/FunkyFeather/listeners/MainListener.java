package fun.Entt.FunkyFeather.listeners;

import fun.Entt.FunkyFeather.config.MCM;
import fun.Entt.FunkyFeather.FunkyFeather;
import fun.Entt.FunkyFeather.utils.InventoryHandler;
import fun.Entt.FunkyFeather.utils.MSGU;

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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

import static fun.Entt.FunkyFeather.FunkyFeather.econ;

public class MainListener implements Listener {
    private final boolean hasMethod;
    private final FunkyFeather plugin;

    public MainListener(FunkyFeather plugin) {
        this.plugin = plugin;
        this.hasMethod = this.hasMethod("setKeepInventory");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (player.hasPermission("ff.keep") && hasFF(player)) {
            if (!this.hasMethod) {
                InventoryHandler.getInstance().saveInventoryAndArmor(player);
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
        MCM mcm = plugin.getMCM();
        FileConfiguration config = mcm.getConfig();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isFunkyFeather(item, config)) {
                return true;
            }
        }
        return false;
    }

    private boolean isFunkyFeather(ItemStack item, FileConfiguration config) {
        if (item.getType() == Material.FEATHER && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = MSGU.color(config.getString("Config.Feather.Name"));
            return item.getItemMeta().getDisplayName().equals(displayName);
        }
        return false;
    }

    private boolean hasMethod(String string) {
        boolean hasMethod = false;
        Method[] methods = PlayerDeathEvent.class.getMethods();
        for (Method m : methods) {
            if (m.getName().equals(string)) {
                hasMethod = true;
                break;
            }
        }
        return hasMethod;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("ff.keep") && hasFF(player)) {
            MCM mcm = plugin.getMCM();
            FileConfiguration config = mcm.getConfig();

            if (config.getBoolean("respawn-zone.set-respawn-enabled")) {
                String path = "respawn-zone.zone";
                if (config.contains(path)) {
                    double x = config.getDouble(path + ".x");
                    double y = config.getDouble(path + ".y");
                    double z = config.getDouble(path + ".z");
                    float yaw = (float) config.getDouble(path + ".yaw");
                    float pitch = (float) config.getDouble(path + ".pitch");
                    World world = plugin.getServer().getWorld(config.getString(path + ".world"));

                    Location l = new Location(world, x, y, z, yaw, pitch);
                    plugin.getServer().getScheduler().runTask(plugin, () -> player.teleport(l));
                }
            }

            useFeather(player);
        }

        InventoryHandler ih = InventoryHandler.getInstance();
        if (ih.hasInventorySaved(player) && ih.hasArmorSaved(player)) {
            player.getInventory().setContents(ih.loadInventory(player));
            player.getInventory().setArmorContents(ih.loadArmor(player));
            ih.removeInventoryAndArmor(player);
        }
    }

    private void useFeather(Player player) {
        MCM mcm = plugin.getMCM();
        FileConfiguration config = mcm.getConfig();
        ItemStack feather = findFunkyFeather(player);
        String showName = config.getString("Feather.Name");
        int UseCost = mcm.getUseCost();

        if (feather != null) {
            if (mcm.isVaultIntEnabled()) {
                if (UseCost > 0) {
                    if (econ.getBalance(player) >= UseCost) {
                        int amount = feather.getAmount();
                        if (amount > 0) {
                            feather.setAmount(amount - 1);
                            econ.withdrawPlayer(player, UseCost);
                            List<String> messages = config.getStringList("Messages.use");
                            String messageFormat = String.join(" ", messages);
                            player.sendMessage(MSGU.color(messageFormat.replace("%ff_name%", Objects.requireNonNull(config.getString("Feather.Name"))).replace("%player%", player.getName())));
                        } else {
                            player.getInventory().remove(feather);
                            econ.withdrawPlayer(player, UseCost);
                            List<String> messages = config.getStringList("Messages.last_feather_used");
                            String messageFormat = String.join(" ", messages);
                            player.sendMessage(MSGU.color(messageFormat.replace("%ff_name%", Objects.requireNonNull(config.getString("Feather.Name"))).replace("%player%", player.getName())));
                        }
                    } else {
                        List<String> messages = config.getStringList("Messages.no_money");
                        String messageFormat = String.join(" ", messages);
                        player.sendMessage(MSGU.color(messageFormat.replace("%ff_name%", Objects.requireNonNull(config.getString("Feather.Name"))).replace("%player%", player.getName())));
                    }
                } else {
                    int amount = feather.getAmount();
                    if (amount > 0) {
                        feather.setAmount(amount - 1);
                        econ.withdrawPlayer(player, UseCost);
                        List<String> messages = config.getStringList("Messages.use");
                        String messageFormat = String.join(" ", messages);
                        player.sendMessage(MSGU.color(messageFormat.replace("%ff_name%", Objects.requireNonNull(config.getString("Feather.Name"))).replace("%player%", player.getName())));
                    } else {
                        player.getInventory().remove(feather);
                        econ.withdrawPlayer(player, UseCost);
                        List<String> messages = config.getStringList("Messages.last_feather_used");
                        String messageFormat = String.join(" ", messages);
                        player.sendMessage(MSGU.color(messageFormat.replace("%ff_name%", Objects.requireNonNull(config.getString("Feather.Name"))).replace("%player%", player.getName())));
                    }
                }
            } else {
                int amount = feather.getAmount();
                if (amount > 0) {
                    feather.setAmount(amount - 1);
                    List<String> messages = config.getStringList("Messages.use");
                    String messageFormat = String.join(" ", messages);
                    player.sendMessage(MSGU.color(messageFormat.replace("%ff_name%", Objects.requireNonNull(config.getString("Feather.Name"))).replace("%player%", player.getName())));
                } else {
                    player.getInventory().remove(feather);
                    List<String> messages = config.getStringList("Messages.last_feather_used");
                    String messageFormat = String.join(" ", messages);
                    player.sendMessage(MSGU.color(messageFormat.replace("%ff_name%", Objects.requireNonNull(config.getString("Feather.Name"))).replace("%player%", player.getName())));
                }
            }
        } else {
            List<String> messages = config.getStringList("Messages.no_feathers");
            String messageFormat = String.join(" ", messages);
            player.sendMessage(MSGU.color(messageFormat.replace("%ff_name%", Objects.requireNonNull(config.getString("Feather.Name"))).replace("%player%", player.getName())));
        }
    }

    private ItemStack findFunkyFeather(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isFunkyFeather(item, plugin.getMCM().getConfig())) {
                return item;
            }
        }
        return null;
    }
}