package it.cratePlugin.managers;

import it.cratePlugin.CratePlugin;
import it.cratePlugin.models.PlayerQuest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataManager {

    private final CratePlugin plugin;
    private File dataFile;
    private FileConfiguration data;

    // Cache
    // uuid -> (crateType -> keyCount)
    private final Map<UUID, Map<String, Integer>> playerKeys = new HashMap<>();
    // uuid -> lastDailyTimestamp
    private final Map<UUID, Long> lastDaily = new HashMap<>();
    // uuid -> streak
    private final Map<UUID, Integer> loginStreak = new HashMap<>();
    // uuid -> (questId -> PlayerQuest)
    private final Map<UUID, Map<String, PlayerQuest>> playerQuests = new HashMap<>();

    public DataManager(CratePlugin plugin) {
        this.plugin = plugin;
        dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
        loadAll();
    }

    // ============ KEYS ============
    public int getKeys(UUID uuid, String crateType) {
        return playerKeys.getOrDefault(uuid, new HashMap<>()).getOrDefault(crateType, 0);
    }

    public void addKeys(UUID uuid, String crateType, int amount) {
        playerKeys.computeIfAbsent(uuid, k -> new HashMap<>())
                .merge(crateType, amount, Integer::sum);
        saveKeys(uuid);
    }

    public boolean removeKey(UUID uuid, String crateType) {
        int current = getKeys(uuid, crateType);
        if (current <= 0) return false;
        playerKeys.get(uuid).put(crateType, current - 1);
        saveKeys(uuid);
        return true;
    }

    // ============ DAILY ============
    public long getLastDaily(UUID uuid) {
        return lastDaily.getOrDefault(uuid, 0L);
    }

    public int getLoginStreak(UUID uuid) {
        return loginStreak.getOrDefault(uuid, 0);
    }

    public void setDailyData(UUID uuid, long timestamp, int streak) {
        lastDaily.put(uuid, timestamp);
        loginStreak.put(uuid, streak);
        data.set("players." + uuid + ".lastDaily", timestamp);
        data.set("players." + uuid + ".streak", streak);
        save();
    }

    // ============ QUESTS ============
    public Map<String, PlayerQuest> getPlayerQuests(UUID uuid) {
        return playerQuests.computeIfAbsent(uuid, k -> new HashMap<>());
    }

    public PlayerQuest getOrCreateQuest(UUID uuid, String questId) {
        Map<String, PlayerQuest> quests = getPlayerQuests(uuid);
        return quests.computeIfAbsent(questId, id -> new PlayerQuest(id));
    }

    public void saveQuest(UUID uuid, PlayerQuest quest) {
        getPlayerQuests(uuid).put(quest.getQuestId(), quest);
        String path = "players." + uuid + ".quests." + quest.getQuestId();
        data.set(path + ".progress", quest.getProgress());
        data.set(path + ".completed", quest.isCompleted());
        data.set(path + ".rewardClaimed", quest.isRewardClaimed());
        save();
    }

    // ============ PERSISTENCE ============
    private void loadAll() {
        if (!data.contains("players")) return;
        for (String uuidStr : data.getConfigurationSection("players").getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            String base = "players." + uuidStr;

            // Keys
            if (data.contains(base + ".keys")) {
                Map<String, Integer> keys = new HashMap<>();
                for (String type : data.getConfigurationSection(base + ".keys").getKeys(false)) {
                    keys.put(type, data.getInt(base + ".keys." + type, 0));
                }
                playerKeys.put(uuid, keys);
            }

            // Daily
            lastDaily.put(uuid, data.getLong(base + ".lastDaily", 0L));
            loginStreak.put(uuid, data.getInt(base + ".streak", 0));

            // Quests
            if (data.contains(base + ".quests")) {
                Map<String, PlayerQuest> quests = new HashMap<>();
                for (String questId : data.getConfigurationSection(base + ".quests").getKeys(false)) {
                    int progress = data.getInt(base + ".quests." + questId + ".progress", 0);
                    boolean completed = data.getBoolean(base + ".quests." + questId + ".completed", false);
                    boolean claimed = data.getBoolean(base + ".quests." + questId + ".rewardClaimed", false);
                    quests.put(questId, new PlayerQuest(questId, progress, completed, claimed));
                }
                playerQuests.put(uuid, quests);
            }
        }
    }

    private void saveKeys(UUID uuid) {
        Map<String, Integer> keys = playerKeys.getOrDefault(uuid, new HashMap<>());
        for (Map.Entry<String, Integer> e : keys.entrySet()) {
            data.set("players." + uuid + ".keys." + e.getKey(), e.getValue());
        }
        save();
    }

    public void saveAll() {
        for (UUID uuid : playerKeys.keySet()) saveKeys(uuid);
        save();
    }

    private void save() {
        try { data.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }
}
