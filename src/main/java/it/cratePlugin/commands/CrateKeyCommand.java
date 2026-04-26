package it.cratePlugin.commands;

import it.cratePlugin.CratePlugin;
import it.cratePlugin.models.CrateType;
import it.cratePlugin.utils.Utils;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class CrateKeyCommand implements CommandExecutor {

    private final CratePlugin plugin;

    public CrateKeyCommand(CratePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("crateplugin.admin")) {
            sender.sendMessage(Utils.colorStr("&cPermesso negato."));
            return true;
        }

        // /cratekey give <player> <tipo> [quantità]
        // /cratekey remove <player> <tipo>
        // /cratekey check <player> <tipo>
        if (args.length < 3) {
            sender.sendMessage(Utils.colorStr("&cUso: /cratekey <give|remove|check> <player> <tipo> [quantità]"));
            return true;
        }

        String subCmd = args[0].toLowerCase();
        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(Utils.colorStr("&cGiocatore non trovato: &e" + args[1]));
            return true;
        }

        CrateType type = CrateType.fromString(args[2]);
        if (type == null) {
            sender.sendMessage(Utils.colorStr("&cTipo non valido: &e" + args[2]));
            return true;
        }

        switch (subCmd) {
            case "give" -> {
                int amount = args.length >= 4 ? parseIntSafe(args[3], 1) : 1;
                plugin.getKeyManager().giveKey(target, type, amount);
                String keyName = plugin.getConfig().getString(
                        "crates." + type.getConfigKey() + ".key-name", type.name());
                sender.sendMessage(Utils.colorStr("&aData &e" + amount + "x " + keyName
                        + " &aa &e" + target.getName() + "&a."));
                target.sendMessage(Utils.colorStr("&aHai ricevuto &e" + amount + "x " + keyName + "&a da un admin!"));
            }
            case "remove" -> {
                boolean removed = plugin.getKeyManager().consumeKey(target, type);
                if (removed) sender.sendMessage(Utils.colorStr("&aChiave rimossa da &e" + target.getName() + "&a."));
                else sender.sendMessage(Utils.colorStr("&c" + target.getName() + " non ha quella chiave."));
            }
            case "check" -> {
                int keys = plugin.getDataManager().getKeys(target.getUniqueId(), type.getConfigKey());
                String keyName = plugin.getConfig().getString(
                        "crates." + type.getConfigKey() + ".key-name", type.name());
                sender.sendMessage(Utils.colorStr("&e" + target.getName() + " &7ha &e"
                        + keys + "x " + keyName + "&7."));
            }
            default -> sender.sendMessage(Utils.colorStr("&cSottomando sconosciuto."));
        }
        return true;
    }

    private int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; }
    }
}
