package x.Entt.FunkyFeather.CMDs;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import x.Entt.FunkyFeather.Utils.FileHandler;
import x.Entt.FunkyFeather.FF;
import x.Entt.FunkyFeather.Utils.MSG;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static x.Entt.FunkyFeather.FF.econ;
import static x.Entt.FunkyFeather.FF.prefix;

public class CMDs implements CommandExecutor, TabCompleter {
    private final FF plugin;
    private final FileHandler fh;

    public CMDs(FF plugin) {
        this.plugin = plugin;
        this.fh = plugin.getFH();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MSG.color(prefix + fh.getMessages().getString("for-players")));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0 || args.length > 2) {
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "feather":
                giveFeather(player, args);
                return true;

            case "setrespawn":
                setRespawn(player, true);
                return true;

            case "help":
                sendHelp(player);
                return true;

            case "reload":
                reloadConfig(player);
                return true;

            default:
                return false;
        }
    }

    private void giveFeather(Player player, String[] args) {
        FileConfiguration config = fh.getConfig();
        FileConfiguration msg = fh.getMessages();

        String materialName = config.getString("Feather.Material");
        if (materialName == null) {
            player.sendMessage(MSG.color(prefix + "The Feather.Material config is missing or invalid."));
            return;
        }

        ItemStack feather;
        try {
            feather = new ItemStack(Material.valueOf(materialName.toUpperCase()));
        } catch (IllegalArgumentException e) {
            player.sendMessage(MSG.color(prefix + "Invalid material specified for the feather."));
            return;
        }

        ItemMeta meta = feather.getItemMeta();

        if (meta != null) {
            String featherName = config.getString("Feather.Name");
            if (featherName != null) {
                meta.setDisplayName(MSG.color(featherName));
            }

            if (!config.getBoolean("Feather.Enchant.glint", true)) { // Default to true if the path is not found
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            List<String> enchantments = config.getStringList("Feather.Enchant.enchants");
            for (String enchant : enchantments) {
                String[] parts = enchant.split(", ");
                if (parts.length == 2) {
                    try {
                        Enchantment enchantment = Enchantment.getByName(parts[0].toUpperCase());
                        int level = Integer.parseInt(parts[1]);
                        if (enchantment != null) {
                            meta.addEnchant(enchantment, level, true);
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }

            List<String> lore = new ArrayList<>();
            for (String line : config.getStringList("Feather.Lore")) {
                lore.add(MSG.color(line));
            }
            meta.setLore(lore);

            feather.setItemMeta(meta);
        }

        player.getInventory().addItem(feather);
        player.sendMessage(MSG.color(prefix + msg.getString("give_feather")));

        String mode = config.getString("Feather.Enchant.effects.mode");
        if (mode != null) {
            mode = mode.toUpperCase();
            for (String effect : config.getStringList("Feather.Enchant.effects.effects")) {
                String[] parts = effect.split(" ");
                if (parts.length == 3) {
                    try {
                        PotionEffectType effectType = PotionEffectType.getByName(parts[0].toUpperCase());
                        int duration = Integer.parseInt(parts[1]);
                        int amplifier = Integer.parseInt(parts[2]);
                        if (effectType != null) {
                            if (mode.equals("IN_HAND") && player.getInventory().getItemInMainHand().isSimilar(feather)) {
                                player.addPotionEffect(new PotionEffect(effectType, duration, amplifier));
                            } else if (mode.equals("INVENTORY") && player.getInventory().contains(feather)) {
                                player.addPotionEffect(new PotionEffect(effectType, duration, amplifier));
                            }
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            player.sendMessage(MSG.color(prefix + "The Feather.Enchant.effects.mode config is missing or invalid."));
        }

        if (fh.isVaultIntEnabled()) {
            if (args.length == 3) {
                if (!args[2].equalsIgnoreCase("-w")) {
                    int cost = fh.getGiveCost();
                    econ.withdrawPlayer(player, cost);
                } else {
                    player.sendMessage(MSG.color(prefix + fh.getMessages().getString("give-no-costs")));
                }
            }
        }
    }

    private void setRespawn(Player player, boolean enabled) {
        FileConfiguration config = fh.getConfig();
        FileConfiguration msg = fh.getMessages();

        if (fh.isSetRespawnEnabled()) {
            Location loc = player.getLocation();
            config.set("respawn-zone.set-respawn-enabled", true);
            config.set("respawn-zone.zone.x", loc.getX());
            config.set("respawn-zone.zone.y", loc.getY());
            config.set("respawn-zone.zone.z", loc.getZ());
            config.set("respawn-zone.zone.yaw", loc.getYaw());
            config.set("respawn-zone.zone.pitch", loc.getPitch());
            config.set("respawn-zone.zone.world", loc.getWorld().getName());
            fh.saveConfig();


            if (config.getString("respawn-zone.zone") != null) {
                player.sendMessage(MSG.color(prefix + msg.getString("replace_respawn")));
            } else {
                player.sendMessage(MSG.color(prefix + msg.getString("set_respawn")));
            }
        } else {
            player.sendMessage(MSG.color(prefix + msg.getString("respawn-disabled")));
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(MSG.color("&e<------------ " + prefix + "&e------------>"));
        player.sendMessage(MSG.color("&2&lFEATHER: &freceive the Funky Feather &7&l[use -w for get free]"));
        player.sendMessage(MSG.color("&2&lHELP: &fget help anout the plugin"));
        player.sendMessage(MSG.color("&2&lRELOAD: &freload the plugin and its files"));
        player.sendMessage(MSG.color("&2&lSETRESPAWN: &fset or change the respawn"));
        player.sendMessage(MSG.color("&e<------------ " + prefix + "&e------------>"));
    }

    private void reloadConfig(Player player) {
        fh.reloadConfig();
        player.sendMessage(MSG.color(prefix + fh.getConfig().getString("Messages.reload")));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> commands = new ArrayList<>();
        if (args.length == 1) {
            commands.add("feather");
            commands.add("setrespawn");
            commands.add("help");
            commands.add("reload");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("feather")) {
            commands.add("-w");
        }
        return commands;
    }
}