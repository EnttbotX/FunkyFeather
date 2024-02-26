package fun.Entt.FunkyFeather.Commands;

import fun.Entt.FunkyFeather.Config.MCM;
import fun.Entt.FunkyFeather.FunkyFeather;
import fun.Entt.FunkyFeather.Utils.MSGU;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MainCMD implements CommandExecutor {
    private final FunkyFeather plugin;

    public MainCMD(FunkyFeather plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }

        Player player = (Player) sender;
        MCM mcm = plugin.getMCM();
        FileConfiguration config = mcm.getConfig();

        if (!player.hasPermission("ff.commands")) {
            player.sendMessage(MSGU.color(FunkyFeather.prefix + "&cYou don't have permissions to use this command."));
            return true;
        }

        if (args.length < 1) {
            help(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.getMCM().reloadConfig();
            sender.sendMessage(MSGU.color(FunkyFeather.prefix + "&2Config reloaded!"));
        } else if (args[0].equalsIgnoreCase("setrespawn")) {
            if (!mcm.isSetRespawnEnabled()) {
                player.sendMessage(MSGU.color(FunkyFeather.prefix + "&cThe respawn system is disabled in config."));
                return true;
            }

            String path = "Config.respawn-zone.zone";
            Location l = player.getLocation();

            double x = l.getX();
            double y = l.getY();
            double z = l.getZ();
            float yaw = l.getYaw();
            float pitch = l.getPitch();
            String world = l.getWorld().getName();

            config.set(path + ".x", x);
            config.set(path + ".y", y);
            config.set(path + ".z", z);
            config.set(path + ".yaw", yaw);
            config.set(path + ".pitch", pitch);
            config.set(path + ".world", world);
            mcm.saveConfig();

            player.sendMessage(MSGU.color(FunkyFeather.prefix + "&aRespawn location set!"));

        } else if (args[0].equalsIgnoreCase("give")) {
            giveFeather(sender);
        } else {
            help(sender);
        }
        return true;
    }

    public void giveFeather(CommandSender sender) {
        MCM mcm = plugin.getMCM();
        Player player = (Player) sender;
        FileConfiguration config = mcm.getConfig();

        ItemStack stack = new ItemStack(Material.FEATHER, 1);
        ItemMeta meta = stack.getItemMeta();

        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        String displayName = MSGU.color(config.getString("Config.Feather.Name"));
        meta.setDisplayName(displayName);

        List<String> lore = new ArrayList<>();
        for (String line : config.getStringList("Config.Feather.Lore")) {
            lore.add(MSGU.color(line));
        }
        meta.setLore(lore);
        stack.setItemMeta(meta);

        String showName = config.getString("Config.Feather.Name");

        player.getInventory().addItem(stack);
        player.sendMessage(MSGU.color(FunkyFeather.prefix + "&2You received a " + showName +  "!"));
    }

    private void help(CommandSender sender) {
        sender.sendMessage(MSGU.color("&e&l---------- " + FunkyFeather.prefix + "&e&l----------"));
        sender.sendMessage(MSGU.color("&2&LSETRESPAWN: sets the respawn for the players."));
        sender.sendMessage(MSGU.color("&2&LRELOAD: reload config and data folder."));
        sender.sendMessage(MSGU.color("&2&LGIVE: give a feather for you."));
        sender.sendMessage(MSGU.color("&e&l-----------------------------------"));
    }
}