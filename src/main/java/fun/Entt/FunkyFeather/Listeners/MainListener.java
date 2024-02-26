package fun.Entt.FunkyFeather.Listeners;

import fun.Entt.FunkyFeather.Config.MCM;
import fun.Entt.FunkyFeather.FunkyFeather;
import fun.Entt.FunkyFeather.Utils.InventoryHandler;
import fun.Entt.FunkyFeather.Utils.MSGU;

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

            if (config.getBoolean("Config.respawn-zone.set-respawn-enabled")) {
                String path = "Config.respawn-zone.zone";
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
        String showName = config.getString("Config.Feather.Name");

        if (feather != null) {
            int amount = feather.getAmount();
            if (amount > 1) {
                feather.setAmount(amount - 1);
                player.sendMessage(MSGU.color(FunkyFeather.prefix + "&2You used an " + showName +  "!"));
            } else {
                player.getInventory().remove(feather);
                player.sendMessage(MSGU.color(FunkyFeather.prefix + "&aYou used your last " + showName +  "!"));
            }
        } else {
            player.sendMessage(MSGU.color(FunkyFeather.prefix + "&cYou don't have any " + showName +  "!"));
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