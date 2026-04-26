package it.cratePlugin.commands;

import it.cratePlugin.CratePlugin;
import it.cratePlugin.utils.Utils;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class CrateQuestCommand implements CommandExecutor {
    private final CratePlugin plugin;
    public CrateQuestCommand(CratePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Solo giocatori."); return true; }

        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            plugin.getQuestManager().listQuests(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("complete")) {
            if (args.length < 2) {
                player.sendMessage(Utils.color("&cUso: /cratequest complete <quest_id>"));
                return true;
            }
            plugin.getQuestManager().claimQuestReward(player, args[1]);
            return true;
        }

        // Admin: force complete
        if (args[0].equalsIgnoreCase("forcecomplete") && sender.hasPermission("crateplugin.admin")) {
            if (args.length < 3) {
                player.sendMessage(Utils.color("&cUso: /cratequest forcecomplete <quest_id> <player>"));
                return true;
            }
            Player target = plugin.getServer().getPlayer(args[2]);
            if (target == null) { player.sendMessage(Utils.color("&cGiocatore non trovato.")); return true; }
            var pq = plugin.getDataManager().getOrCreateQuest(target.getUniqueId(), args[1]);
            pq.setCompleted(true);
            pq.setProgress(plugin.getConfig().getInt("quests." + args[1] + ".target", 1));
            plugin.getDataManager().saveQuest(target.getUniqueId(), pq);
            player.sendMessage(Utils.color("&aQuest &e" + args[1] + " &acompletata per &e" + target.getName() + "&a."));
            return true;
        }

        player.sendMessage(Utils.color("&cUso: /cratequest <list|complete <id>>"));
        return true;
    }
}
