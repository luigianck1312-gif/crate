package it.cratePlugin.commands;

import it.cratePlugin.CratePlugin;
import it.cratePlugin.utils.Utils;
import org.bukkit.command.*;

public class CrateReloadCommand implements CommandExecutor {
    private final CratePlugin plugin;
    public CrateReloadCommand(CratePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("crateplugin.admin")) {
            sender.sendMessage(Utils.colorStr("&cPermesso negato."));
            return true;
        }
        plugin.reloadConfig();
        sender.sendMessage(Utils.colorStr("&aCratePlugin ricaricato con successo!"));
        return true;
    }
}
