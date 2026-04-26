package it.cratePlugin.commands;

import it.cratePlugin.CratePlugin;
import it.cratePlugin.utils.Utils;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class CrateDailyCommand implements CommandExecutor {
    private final CratePlugin plugin;
    public CrateDailyCommand(CratePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Solo i giocatori.");
            return true;
        }
        plugin.getDailyLoginManager().claimDaily(player);
        return true;
    }
}
