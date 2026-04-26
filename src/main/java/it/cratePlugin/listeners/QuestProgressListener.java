package it.cratePlugin.listeners;

import it.cratePlugin.CratePlugin;
import it.cratePlugin.managers.QuestManager;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;

public class QuestProgressListener implements Listener {

    private final CratePlugin plugin;

    public QuestProgressListener(CratePlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        Player killer = event.getEntity().getKiller();

        if (event.getEntity() instanceof Player) {
            plugin.getQuestManager().addProgress(killer, QuestManager.QuestType.KILL_PLAYERS, 1);
        } else {
            EntityType type = event.getEntityType();
            if (type == EntityType.ENDER_DRAGON || type == EntityType.WITHER) {
                plugin.getQuestManager().addProgress(killer, QuestManager.QuestType.KILL_BOSS, 1);
            } else {
                plugin.getQuestManager().addProgress(killer, QuestManager.QuestType.KILL_MOBS, 1);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        plugin.getQuestManager().addProgress(event.getPlayer(), QuestManager.QuestType.MINE_BLOCKS, 1);
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            plugin.getQuestManager().addProgress(event.getPlayer(), QuestManager.QuestType.FISH, 1);
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        plugin.getQuestManager().addProgress(player, QuestManager.QuestType.CRAFT_ITEMS, 1);
    }

    @EventHandler
    public void onLevelChange(PlayerLevelChangeEvent event) {
        plugin.getQuestManager().addProgress(event.getPlayer(), QuestManager.QuestType.REACH_LEVEL,
                event.getNewLevel() - event.getOldLevel());
    }
}
