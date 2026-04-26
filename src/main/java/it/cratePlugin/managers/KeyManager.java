package it.cratePlugin.managers;

import it.cratePlugin.CratePlugin;
import it.cratePlugin.models.CrateType;
import it.cratePlugin.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class KeyManager {

    private final CratePlugin plugin;
    public static final String PDC_KEY = "crate_key_type";

    public KeyManager(CratePlugin plugin) {
        this.plugin = plugin;
    }

    public ItemStack createKey(CrateType type) {
        ConfigurationSection section = plugin.getConfig()
                .getConfigurationSection("crates." + type.getConfigKey());
        if (section == null) return new ItemStack(Material.TRIPWIRE_HOOK);

        String keyName = section.getString("key-name", "&7Chiave");
        List<String> lore = section.getStringList("key-lore");

        ItemStack key = new ItemStack(Material.TRIPWIRE_HOOK);
        ItemMeta meta = key.getItemMeta();
        meta.displayName(Utils.color(keyName));

        List<Component> loreComponents = new ArrayList<>();
        for (String line : lore) loreComponents.add(Utils.color(line));
        meta.lore(loreComponents);

        // Tag PersistentData per identificare il tipo di crate
        NamespacedKey nk = new NamespacedKey(plugin, PDC_KEY);
        meta.getPersistentDataContainer().set(nk, PersistentDataType.STRING, type.getConfigKey());

        key.setItemMeta(meta);
        return key;
    }

    public CrateType getKeyType(ItemStack item) {
        if (item == null || item.getType() != Material.TRIPWIRE_HOOK) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        NamespacedKey nk = new NamespacedKey(plugin, PDC_KEY);
        String val = meta.getPersistentDataContainer().get(nk, PersistentDataType.STRING);
        if (val == null) return null;
        return CrateType.fromString(val);
    }

    public void giveKey(Player player, CrateType type, int amount) {
        ItemStack key = createKey(type);
        key.setAmount(Math.min(amount, 64));
        // Se amount > 64 dai più stack
        int remaining = amount;
        while (remaining > 0) {
            int stackSize = Math.min(remaining, 64);
            ItemStack stack = createKey(type);
            stack.setAmount(stackSize);
            player.getInventory().addItem(stack).forEach((slot, leftover) ->
                    player.getWorld().dropItemNaturally(player.getLocation(), leftover));
            remaining -= stackSize;
        }
        // Also store in data
        plugin.getDataManager().addKeys(player.getUniqueId(), type.getConfigKey(), amount);
    }

    public boolean hasKey(Player player, CrateType type) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            CrateType t = getKeyType(item);
            if (t == type) return true;
        }
        return false;
    }

    public boolean consumeKey(Player player, CrateType type) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null) continue;
            CrateType t = getKeyType(item);
            if (t == type) {
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    player.getInventory().setItem(i, null);
                }
                plugin.getDataManager().removeKey(player.getUniqueId(), type.getConfigKey());
                return true;
            }
        }
        return false;
    }
}
