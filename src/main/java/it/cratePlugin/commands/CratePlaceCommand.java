package it.cratePlugin.commands;

import it.cratePlugin.CratePlugin;
import it.cratePlugin.models.CrateType;
import it.cratePlugin.utils.Utils;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class CratePlaceCommand implements CommandExecutor {
    private final CratePlugin plugin;
    public CratePlaceCommand(CratePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Solo giocatori."); return true; }
        if (!sender.hasPermission("crateplugin.admin")) {
            player.sendMessage(Utils.color("&cPermesso negato."));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(Utils.color("&cUso: /crateplace <tipo>"));
            return true;
        }

        CrateType type = CrateType.fromString(args[0]);
        if (type == null) {
            player.sendMessage(Utils.color("&cTipo non valido."));
            return true;
        }

        plugin.getCrateManager().placeCrate(player.getLocation().getBlock().getLocation(), type);
        String name = plugin.getConfig().getString("crates." + type.getConfigKey() + ".display-name", type.name());
        player.sendMessage(Utils.color("&aCrate &r" + name + " &apiazzata con successo!"));
        return true;
    }
}
