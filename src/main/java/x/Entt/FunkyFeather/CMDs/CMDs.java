package x.Entt.FunkyFeather.CMDs;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import x.Entt.FunkyFeather.FF;
import x.Entt.FunkyFeather.Utils.MSG;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static x.Entt.FunkyFeather.FF.econ;
import static x.Entt.FunkyFeather.FF.prefix;

public class CMDs implements CommandExecutor, TabCompleter {
    private final FF plugin;

    public CMDs(FF plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            help(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "setrespawn":
                return handleSetRespawn(sender);
            case "reload":
                return handleReload(sender);
            case "give":
                return handleGiveFeather(sender, args);
            case "help":
            default:
                help(sender);
                return true;
        }
    }

    private void help(CommandSender sender) {
        sender.sendMessage(MSG.color("&2------------------"));
        sender.sendMessage(MSG.color("&aAvailable Commands:"));
        sender.sendMessage(MSG.color("&a/ff setrespawn &7- Sets the respawn point at your current location."));
        sender.sendMessage(MSG.color("&a/ff reload &7- Reloads the plugin's configuration and messages."));
        sender.sendMessage(MSG.color("&a/ff give [player] [-w] &7- Gives a feather to a player."));
        sender.sendMessage(MSG.color("&2------------------"));
    }

    private boolean handleSetRespawn(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MSG.color(plugin.getFH().getMessages().getString("Messages.for-players")));
            return true;
        }

        if (!plugin.getFH().isSetRespawnEnabled()) {
            sender.sendMessage(MSG.color(plugin.getFH().getMessages().getString("Messages.respawn-disabled")));
            return true;
        }

        Player player = (Player) sender;
        Location location = player.getLocation();

        plugin.getFH().getConfig().set("respawn-zone.world", location.getWorld().getName());
        plugin.getFH().getConfig().set("respawn-zone.x", location.getX());
        plugin.getFH().getConfig().set("respawn-zone.y", location.getY());
        plugin.getFH().getConfig().set("respawn-zone.z", location.getZ());
        plugin.getFH().getConfig().set("respawn-zone.yaw", location.getYaw());
        plugin.getFH().getConfig().set("respawn-zone.pitch", location.getPitch());

        plugin.getFH().saveConfig();

        sender.sendMessage(MSG.color(plugin.getFH().getMessages().getString("Messages.set-respawn")));
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        plugin.getFH().reloadConfig();
        plugin.getFH().reloadMessages();
        sender.sendMessage(MSG.color(prefix + plugin.getFH().getMessages().getString("reload")));
        return true;
    }

    private boolean handleGiveFeather(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MSG.color(prefix + plugin.getFH().getMessages().getString("Messages.for-players", "&cThis command is only for players.")));
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            sender.sendMessage(MSG.color(prefix + plugin.getFH().getMessages().getString("Messages.usage-give", "&c/ff give [player] [-w]")));
            return true;
        }

        Player targetPlayer = plugin.getServer().getPlayer(args[1]);

        if (targetPlayer == null) {
            sender.sendMessage(MSG.color(prefix + plugin.getFH().getMessages().getString("Messages.player-not-found", "&cPlayer not found.")));
            return true;
        }

        boolean free = args.length > 2 && "-w".equals(args[2]);

        if (free && !player.hasPermission("ff.withoutcost")) {
            player.sendMessage(MSG.color(prefix + plugin.getFH().getMessages().getString("Messages.no_permission", "&cYou don't have permission to use this command.")));
            return true;
        }

        if (!free) {
            if (econ == null) {
                player.sendMessage(MSG.color(prefix + "&cEconomy system is not available. Cannot process payment."));
                return true;
            }

            double giveCost = plugin.getFH().getConfig().getDouble("Vault.give-cost", 0);

            if (econ.has(player, giveCost)) {
                sender.sendMessage(MSG.color(prefix + plugin.getFH().getMessages().getString("Messages.no-money", "&cYou don't have enough money to give a feather.")));
                return true;
            }

            econ.withdrawPlayer(player, giveCost);
            player.sendMessage(MSG.color(prefix + plugin.getFH().getMessages().getString("Messages.give", "&aYou have given a feather and paid the cost.")));
        } else {
            player.sendMessage(MSG.color(prefix + plugin.getFH().getMessages().getString("Messages.give-no-cost", "&aYou have given a feather for free.")));
        }

        ItemStack feather = createFunkyFeather(targetPlayer);
        targetPlayer.getInventory().addItem(feather);
        return true;
    }

    private ItemStack createFunkyFeather(Player targetPlayer) {
        ItemStack feather = new ItemStack(Material.FEATHER);
        ItemMeta meta = feather.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(MSG.color(plugin.getFH().getConfig().getString("Feather.Name")));

            List<String> lore = plugin.getFH().getConfig().getStringList("Feather.Lore").stream()
                    .map(MSG::color)
                    .collect(Collectors.toList());
            meta.setLore(lore);

            plugin.getFH().getConfig().getStringList("Feather.Enchant.enchants").forEach(enchantStr -> {
                String[] enchantParts = enchantStr.split(" : ");
                if (enchantParts.length == 2) {
                    Enchantment enchantment = Enchantment.getByName(enchantParts[0].toUpperCase());
                    try {
                        int level = Integer.parseInt(enchantParts[1]);
                        if (enchantment != null) {
                            meta.addEnchant(enchantment, level, true);
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            });

            if (plugin.getFH().getConfig().getBoolean("Feather.Enchant.hide-enchants")) {
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            feather.setItemMeta(meta);
        }

        if ("IN_HAND".equalsIgnoreCase(plugin.getFH().getConfig().getString("Feather.Enchant.effects.mode"))) {
            plugin.getFH().getConfig().getStringList("Feather.Enchant.effects.effects").forEach(effectStr -> {
                String[] effectParts = effectStr.split(" ");
                if (effectParts.length == 3) {
                    PotionEffectType effectType = PotionEffectType.getByName(effectParts[0].toUpperCase());
                    try {
                        int duration = Integer.parseInt(effectParts[1]) * 20;
                        int amplifier = Integer.parseInt(effectParts[2]);
                        if (effectType != null) {
                            targetPlayer.addPotionEffect(new PotionEffect(effectType, duration, amplifier));
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            });
        }

        return feather;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(List.of("give", "reload", "help", "setrespawn"));
        } else if (args.length == 2 && "give".equalsIgnoreCase(args[0])) {
            completions.addAll(plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList()));
        } else if (args.length == 3 && "give".equalsIgnoreCase(args[0]) && sender.hasPermission("ff.withoutcost")) {
            completions.add("-w");
        }

        return completions;
    }
}