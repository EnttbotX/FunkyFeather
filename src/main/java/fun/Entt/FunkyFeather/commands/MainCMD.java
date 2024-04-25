package fun.Entt.FunkyFeather.commands;

import fun.Entt.FunkyFeather.config.MCM;
import fun.Entt.FunkyFeather.FunkyFeather;
import fun.Entt.FunkyFeather.utils.MSGU;

import javafx.scene.transform.Translate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static fun.Entt.FunkyFeather.FunkyFeather.econ;

public class MainCMD implements CommandExecutor, TabCompleter {
    private final FunkyFeather plugin;

    public MainCMD(FunkyFeather plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            if (args[0].equalsIgnoreCase("reload")) {
                plugin.getMCM().reloadConfig();
            } else {
                sender.sendMessage("&cThis command can only be executed by a player.");
                sender.sendMessage("&cConsole commands: &f/ff reload");
                return true;
            }
        }

        Player player = (Player) sender;
        MCM mcm = plugin.getMCM();
        FileConfiguration config = mcm.getConfig();

        if (args.length < 1) {
            help(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("ff.reload")) {
                player.sendMessage(MSGU.color(FunkyFeather.prefix + "&cYou don't have permissions to use this command."));
                return true;
            }

            registerConfig();
            plugin.getMCM().reloadConfig();
            sender.sendMessage(MSGU.color(FunkyFeather.prefix + "&2Config reloaded!"));
        } else if (args[0].equalsIgnoreCase("setrespawn")) {
            if (!mcm.isSetRespawnEnabled()) {
                player.sendMessage(MSGU.color(FunkyFeather.prefix + "&cThe respawn system is disabled in config."));
                return true;
            }

            if (!player.hasPermission("ff.setrespawn")) {
                player.sendMessage(MSGU.color(FunkyFeather.prefix + "&cYou don't have permissions to use this command."));
                return true;
            }

            String path = "respawn-zone.zone";
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
            giveFeather(sender, args);
            registerConfig();
        } else {
            help(sender);
            registerConfig();
        }
        return true;
    }

    public void registerConfig() {
        MCM mcm = plugin.getMCM();
        FileConfiguration config = mcm.getConfig();
        File configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            config.options().copyDefaults(true);
        }
    }

    public void giveFeather(CommandSender sender, String[] args) {
        registerConfig();
        MCM mcm = plugin.getMCM();
        Player player = (Player) sender;
        FileConfiguration config = mcm.getConfig();

        int GiveCost = mcm.getGiveCost();

        if (!player.hasPermission("ff.give")) {
            player.sendMessage(MSGU.color(FunkyFeather.prefix + "&cYou don't have permissions to use this command."));
            return;
        }

        Material material = Material.valueOf(config.getString("Feather.Material"));

        ItemStack stack = new ItemStack(material, 1);
        ItemMeta meta = stack.getItemMeta();

        if (config.getBoolean("Feather.Enchanted")) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        String displayName = MSGU.color(config.getString("Feather.Name"));
        meta.setDisplayName(displayName);

        List<String> lore = new ArrayList<>();
        for (String line : config.getStringList("Feather.Lore")) {
            lore.add(MSGU.color(line));
        }
        meta.setLore(lore);
        stack.setItemMeta(meta);

        if (mcm.isVaultIntEnabled()) {
            if (GiveCost > 0) {
                if (args.length >= 2) {
                    Player targetPlayer = Bukkit.getPlayer(args[1]);
                    if (targetPlayer == null || !targetPlayer.isOnline()) {
                        player.sendMessage(MSGU.color(FunkyFeather.prefix + "&cPlayer not found or not online."));
                        return;
                    }

                    if (args.length == 3 && args[2].equalsIgnoreCase("-wc")) {
                        player.getInventory().addItem(stack);
                        List<String> messages = config.getStringList("Messages.give");
                        String messageFormat = String.join(" ", messages);
                        player.sendMessage(MSGU.color(messageFormat.replace("%ff_name%", Objects.requireNonNull(config.getString("Feather.Name"))).replace("%player%", player.getName())));
                        player.sendMessage(MSGU.color("&7[WITH OUT COST]"));
                    } else {
                        if (econ.getBalance(player) >= GiveCost) {
                            econ.withdrawPlayer(player, GiveCost);
                            targetPlayer.getInventory().addItem(stack);
                            List<String> messages = config.getStringList("Messages.gave_to_player");
                            String messageFormat = String.join(" ", messages);
                            player.sendMessage(MSGU.color(messageFormat.replace("%ff_name%", Objects.requireNonNull(config.getString("Feather.Name"))).replace("%player%", player.getName())));
                            List<String> oMessages = config.getStringList("Messages.give");
                            String oMessageFormat = String.join(" ", oMessages);
                            player.sendMessage(MSGU.color(oMessageFormat.replace("%ff_name%", Objects.requireNonNull(config.getString("Feather.Name"))).replace("%player%", player.getName())));
                        } else {
                            List<String> messages = config.getStringList("Messages.no_money");
                            String messageFormat = String.join(" ", messages);
                            player.sendMessage(MSGU.color(messageFormat.replace("%ff_name%", Objects.requireNonNull(config.getString("Feather.Name"))).replace("%player%", player.getName())));
                        }
                    }
                } else {
                    if (econ.getBalance(player) >= GiveCost) {
                        econ.withdrawPlayer(player, GiveCost);
                        player.getInventory().addItem(stack);
                        List<String> messages = config.getStringList("Messages.give");
                        String messageFormat = String.join(" ", messages);
                        player.sendMessage(MSGU.color(messageFormat.replace("%ff_name%", Objects.requireNonNull(config.getString("Feather.Name"))).replace("%player%", player.getName())));
                    } else {
                        List<String> messages = config.getStringList("Messages.no_money");
                        String messageFormat = String.join(" ", messages);
                        player.sendMessage(MSGU.color(messageFormat.replace("%ff_name%", Objects.requireNonNull(config.getString("Feather.Name"))).replace("%player%", player.getName())));
                    }
                }
            } else {
                player.getInventory().addItem(stack);
                List<String> messages = config.getStringList("Messages.give");
                String messageFormat = String.join(" ", messages);
                player.sendMessage(MSGU.color(messageFormat.replace("%ff_name%", Objects.requireNonNull(config.getString("Feather.Name"))).replace("%player%", player.getName())));
            }
        } else {
            player.getInventory().addItem(stack);
            List<String> messages = config.getStringList("Messages.give");
            String messageFormat = String.join(" ", messages);
            player.sendMessage(MSGU.color(messageFormat.replace("%ff_name%", Objects.requireNonNull(config.getString("Feather.Name"))).replace("%player%", player.getName())));
        }
    }

    private void help(CommandSender sender) {
        sender.sendMessage(MSGU.color("&e&l---------- " + FunkyFeather.prefix + "&e&l----------"));
        sender.sendMessage(MSGU.color("&2&lSETRESPAWN: sets the respawn for players."));
        sender.sendMessage(MSGU.color("&2&lRELOAD: reload the ff."));
        sender.sendMessage(MSGU.color("&2&lGIVE: give a feather to you."));
        sender.sendMessage(MSGU.color("&e&l-----------------------------------"));
        sender.sendMessage(MSGU.color(" "));
        sender.sendMessage(MSGU.color("&c&lYOU CAN USE '-wc' to have no cost in /ff give"));
        sender.sendMessage(MSGU.color("&c&lUSE: /ff give (player) -wc"));
        sender.sendMessage(MSGU.color(" "));
        sender.sendMessage(MSGU.color("&c&lYOU CAN USE: /ff give"));
        sender.sendMessage(MSGU.color("&cto get a feather."));
        sender.sendMessage(MSGU.color(" "));
        sender.sendMessage(MSGU.color("&e&l---------- " + FunkyFeather.prefix + "&e&l----------"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String arg0 = args[0];

            String[] subcommands = new String[]{"reload", "setrespawn", "give"};

            for (String subcommand : subcommands) {
                if (subcommand.startsWith(arg0)) {
                    completions.add(subcommand);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            String arg1 = args[1];

            if (!arg1.startsWith("-")) {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    completions.add(player.getName());
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            String arg2 = args[2];

            if ("-wc".startsWith(arg2) && !args[1].startsWith("-") && args[2].isEmpty()) {
                completions.add("-wc");
            }
        }

        return completions;
    }
}