package it.cratePlugin.managers;

import it.cratePlugin.CratePlugin;
import it.cratePlugin.models.CrateType;
import it.cratePlugin.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ShopManager implements Listener {

    private final CratePlugin plugin;
    private final Map<UUID, Inventory> openShops = new HashMap<>();

    private static final int[] KEY_SLOTS = {10, 12, 14, 16, 28, 30};
    private static final CrateType[] CRATE_ORDER = {
            CrateType.COMUNE, CrateType.NON_COMUNE, CrateType.RARO,
            CrateType.EPICO, CrateType.LEGGENDARIO, CrateType.MITICO
    };

    public ShopManager(CratePlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openShop(Player player) {
        Inventory gui = plugin.getServer().createInventory(null, 45,
                Utils.color("&6✦ &lShop Chiavi Crate &r&6✦"));

        // Glass border
        ItemStack border = buildItem(Material.BLACK_STAINED_GLASS_PANE, "&8 ");
        for (int i = 0; i < 45; i++) gui.setItem(i, border);

        // Keys
        for (int i = 0; i < CRATE_ORDER.length; i++) {
            CrateType type = CRATE_ORDER[i];
            ItemStack keyItem = buildShopKey(type);
            gui.setItem(KEY_SLOTS[i], keyItem);
        }

        // Info item
        ItemStack info = buildItem(Material.BOOK, "&eInfo");
        ItemMeta meta = info.getItemMeta();
        meta.lore(List.of(
                Utils.color("&7Clicca su una chiave"),
                Utils.color("&7per acquistarla con"),
                Utils.color("&6livelli di esperienza&7.")
        ));
        info.setItemMeta(meta);
        gui.setItem(22, info);

        player.openInventory(gui);
        openShops.put(player.getUniqueId(), gui);
    }

    private ItemStack buildShopKey(CrateType type) {
        ItemStack key = plugin.getKeyManager().createKey(type);
        ItemMeta meta = key.getItemMeta();
        int price = plugin.getConfig().getInt("crates." + type.getConfigKey() + ".shop-price", 10);
        String displayName = plugin.getConfig().getString(
                "crates." + type.getConfigKey() + ".display-name", type.name());

        List<Component> lore = new ArrayList<>(meta.lore() != null ? meta.lore() : List.of());
        lore.add(Utils.color("&8---"));
        lore.add(Utils.color("&6Prezzo: &e" + price + " livelli di EXP"));
        lore.add(Utils.color("&7Clicca per acquistare"));
        meta.lore(lore);
        key.setItemMeta(meta);
        return key;
    }

    @EventHandler
    public void onShopClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!openShops.containsKey(player.getUniqueId())) return;
        if (!event.getInventory().equals(openShops.get(player.getUniqueId()))) return;

        event.setCancelled(true);
        int slot = event.getRawSlot();

        for (int i = 0; i < KEY_SLOTS.length; i++) {
            if (slot == KEY_SLOTS[i]) {
                CrateType type = CRATE_ORDER[i];
                buyKey(player, type);
                return;
            }
        }
    }

    private void buyKey(Player player, CrateType type) {
        int price = plugin.getConfig().getInt("crates." + type.getConfigKey() + ".shop-price", 10);
        if (player.getLevel() < price) {
            player.sendMessage(Utils.color("&cNon hai abbastanza livelli! Ti servono &e"
                    + price + " livelli&c."));
            return;
        }
        player.setLevel(player.getLevel() - price);
        plugin.getKeyManager().giveKey(player, type, 1);
        String keyName = plugin.getConfig().getString(
                "crates." + type.getConfigKey() + ".key-name", type.name());
        player.sendMessage(Utils.color("&aHai acquistato: &r" + keyName + " &aper &e"
                + price + " livelli&a!"));
    }

    private ItemStack buildItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Utils.color(name));
        item.setItemMeta(meta);
        return item;
    }
}
