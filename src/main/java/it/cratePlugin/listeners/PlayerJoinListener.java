package it.cratePlugin.listeners;

import it.cratePlugin.CratePlugin;
import it.cratePlugin.utils.Utils;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final CratePlugin plugin;

    public PlayerJoinListener(CratePlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        // Notify if daily is available
        if (plugin.getDailyLoginManager().canClaimDaily(player.getUniqueId())) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.sendMessage(Utils.color("&6✦ Il tuo &ereward giornaliero &6è disponibile! &7Usa &e/cratedaily"));
                }
            }, 60L);
        }
    }
}
