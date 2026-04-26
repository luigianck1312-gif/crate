package it.cratePlugin.managers;

import it.cratePlugin.CratePlugin;
import it.cratePlugin.models.CrateType;
import it.cratePlugin.models.PlayerQuest;
import it.cratePlugin.utils.Utils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public class QuestManager {

    private final CratePlugin plugin;

    public enum QuestType {
        KILL_MOBS, MINE_BLOCKS, KILL_PLAYERS, FISH, CRAFT_ITEMS, KILL_BOSS, REACH_LEVEL
    }

    public QuestManager(CratePlugin plugin) {
        this.plugin = plugin;
    }

    public void addProgress(Player player, QuestType eventType, int amount) {
        UUID uuid = player.getUniqueId();
        ConfigurationSection questsSection = plugin.getConfig().getConfigurationSection("quests");
        if (questsSection == null) return;

        for (String questId : questsSection.getKeys(false)) {
            ConfigurationSection q = questsSection.getConfigurationSection(questId);
            if (q == null) continue;
            String typeStr = q.getString("type", "");
            QuestType qType;
            try { qType = QuestType.valueOf(typeStr); } catch (Exception e) { continue; }
            if (qType != eventType) continue;

            PlayerQuest pq = plugin.getDataManager().getOrCreateQuest(uuid, questId);
            if (pq.isCompleted()) continue;

            int target = q.getInt("target", 1);
            pq.addProgress(amount);

            if (pq.getProgress() >= target) {
                pq.setCompleted(true);
                player.sendMessage(Utils.color("&6✦ Quest completata: &e"
                        + q.getString("display-name", questId) + " &6✦"));
                player.sendMessage(Utils.color("&aUsa &e/cratequest complete " + questId
                        + " &aper riscattare la ricompensa!"));
            }

            plugin.getDataManager().saveQuest(uuid, pq);
        }
    }

    public void claimQuestReward(Player player, String questId) {
        ConfigurationSection q = plugin.getConfig().getConfigurationSection("quests." + questId);
        if (q == null) {
            player.sendMessage(Utils.color("&cQuest non trovata: &e" + questId));
            return;
        }

        PlayerQuest pq = plugin.getDataManager().getOrCreateQuest(player.getUniqueId(), questId);
        if (!pq.isCompleted()) {
            player.sendMessage(Utils.color("&cNon hai completato questa quest ancora!"));
            return;
        }
        if (pq.isRewardClaimed()) {
            player.sendMessage(Utils.color("&cHai già riscattato questa ricompensa!"));
            return;
        }

        String keyType = q.getString("reward-key", "comune");
        int keyAmount = q.getInt("reward-amount", 1);
        CrateType type = CrateType.fromString(keyType);
        if (type == null) type = CrateType.COMUNE;

        plugin.getKeyManager().giveKey(player, type, keyAmount);
        pq.setRewardClaimed(true);
        plugin.getDataManager().saveQuest(player.getUniqueId(), pq);

        String keyName = plugin.getConfig().getString("crates." + type.getConfigKey() + ".key-name", keyType);
        player.sendMessage(Utils.color("&aRicompensa riscattata: &e" + keyAmount + "x " + keyName + "&a!"));
    }

    public void listQuests(Player player) {
        ConfigurationSection questsSection = plugin.getConfig().getConfigurationSection("quests");
        if (questsSection == null) {
            player.sendMessage(Utils.color("&cNessuna quest configurata."));
            return;
        }

        player.sendMessage(Utils.color("&6&m               &r &6Quest &r&6&m               "));
        UUID uuid = player.getUniqueId();

        for (String questId : questsSection.getKeys(false)) {
            ConfigurationSection q = questsSection.getConfigurationSection(questId);
            PlayerQuest pq = plugin.getDataManager().getOrCreateQuest(uuid, questId);
            int target = q.getInt("target", 1);
            int progress = Math.min(pq.getProgress(), target);

            String status;
            if (pq.isRewardClaimed()) status = "&8[✔ Riscattata]";
            else if (pq.isCompleted()) status = "&a[✔ Completata]";
            else status = "&e[" + progress + "/" + target + "]";

            String rewardKey = q.getString("reward-key", "comune");
            int rewardAmt = q.getInt("reward-amount", 1);
            String keyName = plugin.getConfig().getString("crates." + rewardKey + ".key-name", rewardKey);

            player.sendMessage(Utils.color("&e" + q.getString("display-name", questId)
                    + " &8- " + status));
            player.sendMessage(Utils.color("  &7" + q.getString("description", "")
                    + " &8| Ricompensa: &r" + rewardAmt + "x " + keyName));
        }
        player.sendMessage(Utils.color("&6&m                                           "));
    }

    public Set<String> getQuestIds() {
        ConfigurationSection s = plugin.getConfig().getConfigurationSection("quests");
        return s != null ? s.getKeys(false) : Set.of();
    }
}
