package it.cratePlugin.listeners;

import it.cratePlugin.CratePlugin;
import it.cratePlugin.models.CrateType;
import it.cratePlugin.utils.Utils;
import org.bukkit.Material;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class CrateInteractListener implements Listener {

    private final CratePlugin plugin;

    public CrateInteractListener(CratePlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.CHEST) return;

        var loc = event.getClickedBlock().getLocation();
        if (!plugin.getCrateManager().isCrateLocation(loc)) return;

        event.setCancelled(true);
        var player = event.getPlayer();
        CrateType type = plugin.getCrateManager().getCrateAtLocation(loc);
        if (type == null) return;

        // Check if player has the key
        if (!plugin.getKeyManager().hasKey(player, type)) {
            String keyName = plugin.getConfig().getString(
                    "crates." + type.getConfigKey() + ".key-name", type.name());
            String crateName = plugin.getConfig().getString(
                    "crates." + type.getConfigKey() + ".display-name", type.name());
            player.sendMessage(Utils.color("&cTi serve una &r" + keyName + " &cper aprire questa &r" + crateName + "&c!"));
            player.sendMessage(Utils.color("&7Ottienila dalle &equest&7, dal &edaily login&7 o dallo &e/crateshop&7."));
            return;
        }

        plugin.getCrateManager().spinCrate(player, type);
    }
}
