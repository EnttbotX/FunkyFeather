package x.Entt.FunkyFeather.Utils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.entity.Player;

import x.Entt.FunkyFeather.FF;

public class PAPI extends PlaceholderExpansion {
    private FF plugin;

    public PAPI(FF plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean register() {
        return super.register();
    }

    @Override
    public String getIdentifier() {
        return "FunkyFeather";
    }

    @Override
    public String getAuthor() {
        return "Enttbot";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {

        if (player == null) {
            return "";
        }

        if (identifier.equals("ff_name")) {
            return plugin.getFH().getConfig().getString("Feather.Name");
        }

        if (identifier.equals("ff_player")) {
            return player.getName();
        }

        return null;
    }
}