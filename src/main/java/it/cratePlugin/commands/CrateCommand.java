package it.cratePlugin.commands;

import it.cratePlugin.CratePlugin;
import it.cratePlugin.models.CrateType;
import it.cratePlugin.utils.Utils;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class CrateCommand implements CommandExecutor {

    private final CratePlugin plugin;

    public CrateCommand(CratePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Solo i giocatori possono usare questo comando.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(Utils.color("&cUso: /crate <tipo>"));
            player.sendMessage(Utils.color("&7Tipi: &fcomune&7, &anon_comune&7, &braro&7, &5epico&7, &6leggendario&7, &cmitico"));
            return true;
        }

        CrateType type = CrateType.fromString(args[0]);
        if (type == null) {
            player.sendMessage(Utils.color("&cTipo di crate non valido: &e" + args[0]));
            return true;
        }

        plugin.getCrateManager().spinCrate(player, type);
        return true;
    }
}
