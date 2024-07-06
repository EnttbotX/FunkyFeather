package x.Entt.FunkyFeather.Utils;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InvHandler {
    private static final InvHandler instance = new InvHandler();
    private final Map<Player, ItemStack[]> inventories = new HashMap<>();
    private final Map<Player, ItemStack[]> armors = new HashMap<>();

    private InvHandler() {
    }

    public static InvHandler getInstance() {
        return instance;
    }

    public void saveInventoryAndArmor(Player player) {
        ItemStack[] inventory = player.getInventory().getContents();
        ItemStack[] armor = player.getInventory().getArmorContents();
        inventories.put(player, inventory);
        armors.put(player, armor);
    }

    public ItemStack[] loadInventory(Player player) {
        return inventories.get(player);
    }

    public ItemStack[] loadArmor(Player player) {
        return armors.get(player);
    }

    public void removeInventoryAndArmor(Player player) {
        inventories.remove(player);
        armors.remove(player);
    }

    public boolean hasInventorySaved(Player player) {
        return inventories.containsKey(player);
    }

    public boolean hasArmorSaved(Player player) {
        return armors.containsKey(player);
    }
}