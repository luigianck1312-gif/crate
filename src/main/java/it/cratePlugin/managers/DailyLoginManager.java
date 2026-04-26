package it.cratePlugin.managers;

import it.cratePlugin.CratePlugin;
import it.cratePlugin.models.CrateType;
import it.cratePlugin.utils.Utils;
import org.bukkit.entity.Player;

import java.util.Calendar;
import java.util.UUID;

public class DailyLoginManager {

    private final CratePlugin plugin;

    public DailyLoginManager(CratePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean canClaimDaily(UUID uuid) {
        long last = plugin.getDataManager().getLastDaily(uuid);
        if (last == 0) return true;
        Calendar lastCal = Calendar.getInstance();
        lastCal.setTimeInMillis(last);
        Calendar now = Calendar.getInstance();
        // Different day?
        return lastCal.get(Calendar.DAY_OF_YEAR) != now.get(Calendar.DAY_OF_YEAR)
                || lastCal.get(Calendar.YEAR) != now.get(Calendar.YEAR);
    }

    public long getTimeUntilNextDaily(UUID uuid) {
        long last = plugin.getDataManager().getLastDaily(uuid);
        if (last == 0) return 0;
        Calendar nextCal = Calendar.getInstance();
        nextCal.setTimeInMillis(last);
        nextCal.add(Calendar.DAY_OF_YEAR, 1);
        nextCal.set(Calendar.HOUR_OF_DAY, 0);
        nextCal.set(Calendar.MINUTE, 0);
        nextCal.set(Calendar.SECOND, 0);
        nextCal.set(Calendar.MILLISECOND, 0);
        return Math.max(0, nextCal.getTimeInMillis() - System.currentTimeMillis());
    }

    public void claimDaily(Player player) {
        UUID uuid = player.getUniqueId();

        if (!canClaimDaily(uuid)) {
            long remaining = getTimeUntilNextDaily(uuid);
            long hours = remaining / 3600000;
            long minutes = (remaining % 3600000) / 60000;
            player.sendMessage(Utils.color("&cHai già riscattato il daily oggi! Torna tra &e"
                    + hours + "h " + minutes + "m&c."));
            return;
        }

        // Check if streak is broken (missed a day)
        int streak = plugin.getDataManager().getLoginStreak(uuid);
        long last = plugin.getDataManager().getLastDaily(uuid);

        if (last > 0) {
            Calendar lastCal = Calendar.getInstance();
            lastCal.setTimeInMillis(last);
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DAY_OF_YEAR, -1);
            // If last claim was not yesterday → reset streak
            boolean wasYesterday = lastCal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)
                    && lastCal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR);
            if (!wasYesterday) streak = 0;
        }

        streak++;

        // Find best matching reward (closest milestone ≤ streak)
        int bestMilestone = 1;
        for (String key : plugin.getConfig().getConfigurationSection("daily-login.streak-rewards").getKeys(false)) {
            int milestone = Integer.parseInt(key);
            if (milestone <= streak && milestone > bestMilestone) bestMilestone = milestone;
        }
        // Default to day 1
        if (streak == 1) bestMilestone = 1;

        String rewardPath = "daily-login.streak-rewards." + bestMilestone;
        String keyType = plugin.getConfig().getString(rewardPath + ".key-type", "comune");
        int keyAmount = plugin.getConfig().getInt(rewardPath + ".key-amount", 1);

        CrateType type = CrateType.fromString(keyType);
        if (type == null) type = CrateType.COMUNE;

        plugin.getKeyManager().giveKey(player, type, keyAmount);
        plugin.getDataManager().setDailyData(uuid, System.currentTimeMillis(), streak);

        String typeName = plugin.getConfig().getString("crates." + type.getConfigKey() + ".key-name", keyType);

        player.sendMessage(Utils.color("&6✦ Daily Login - Giorno &e#" + streak + "&6 ✦"));
        player.sendMessage(Utils.color("&aHai ricevuto &e" + keyAmount + "x " + typeName + "&a!"));
        if (streak % 7 == 0) {
            player.sendMessage(Utils.color("&d&l⭐ Streak settimanale completata! ⭐"));
        }
    }
}
