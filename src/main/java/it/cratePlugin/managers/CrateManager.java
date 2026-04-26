package it.cratePlugin.managers;

import it.cratePlugin.CratePlugin;
import it.cratePlugin.models.CrateReward;
import it.cratePlugin.models.CrateType;
import it.cratePlugin.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CrateManager {

    private final CratePlugin plugin;
    private final Map<Location, CrateType> placedCrates = new HashMap<>();
    private final Set<UUID> spinningPlayers = new HashSet<>();

    public static final String CRATE_PDC_KEY = "crate_block_type";

    public CrateManager(CratePlugin plugin) {
        this.plugin = plugin;
    }

    // ============ CRATE PLACEMENT ============
    public void placeCrate(Location loc, CrateType type) {
        placedCrates.put(loc, type);
        Block block = loc.getBlock();
        block.setType(Material.CHEST);
    }

    public CrateType getCrateAtLocation(Location loc) {
        return placedCrates.get(loc);
    }

    public boolean isCrateLocation(Location loc) {
        return placedCrates.containsKey(loc);
    }

    // ============ SPIN ============
    public void spinCrate(Player player, CrateType type) {
        if (spinningPlayers.contains(player.getUniqueId())) {
            player.sendMessage(Utils.color("&cSei già in attesa di un'apertura!"));
            return;
        }

        if (!plugin.getKeyManager().hasKey(player, type)) {
            ConfigurationSection section = plugin.getConfig()
                    .getConfigurationSection("crates." + type.getConfigKey());
            String name = section != null ? section.getString("display-name", type.name()) : type.name();
            player.sendMessage(Utils.color("&cNon hai una chiave per la &r" + name + "&c!"));
            return;
        }

        // Consume key
        plugin.getKeyManager().consumeKey(player, type);

        spinningPlayers.add(player.getUniqueId());

        List<CrateReward> rewards = getRewards(type);
        openAnimationGUI(player, type, rewards);
    }

    // ============ ANIMATION GUI ============
    private void openAnimationGUI(Player player, CrateType type, List<CrateReward> rewards) {
        int spinTime = plugin.getConfig().getInt("animation.spin-time", 40);
        int spinDelay = plugin.getConfig().getInt("animation.spin-delay", 2);

        Inventory gui = plugin.getServer().createInventory(null, 27,
                Utils.color(plugin.getConfig().getString("crates." + type.getConfigKey() + ".display-name",
                        "&6Crate") + " &8- &eApertura..."));

        // Fill with glass panes
        ItemStack pane = buildItem(Material.GRAY_STAINED_GLASS_PANE, "&8 ");
        for (int i = 0; i < 27; i++) gui.setItem(i, pane);

        // Highlight slot 13 (center)
        ItemStack highlight = buildItem(Material.YELLOW_STAINED_GLASS_PANE, "&eSlot Vincitore");
        gui.setItem(13, highlight);

        player.openInventory(gui);

        // Rolling slots (top row: 0-8 visible scroll)
        int[] scrollSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8};

        new BukkitRunnable() {
            int tick = 0;
            final int maxTicks = spinTime;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    spinningPlayers.remove(player.getUniqueId());
                    cancel();
                    return;
                }

                if (tick >= maxTicks) {
                    // Pick winner
                    CrateReward winner = pickWeightedReward(rewards);
                    gui.setItem(13, buildRewardItem(winner));

                    // Update title
                    player.sendMessage(Utils.color("&6➤ &aHai vinto: &r" + getRewardName(winner)));

                    // Sound & firework
                    String soundStr = plugin.getConfig().getString("animation.sound-win", "ENTITY_PLAYER_LEVELUP");
                    try { player.playSound(player.getLocation(), Sound.valueOf(soundStr), 1f, 1f); }
                    catch (Exception ignored) {}

                    if (plugin.getConfig().getBoolean("animation.firework-on-win", true)) {
                        Utils.spawnWinFirework(player);
                    }

                    // Give reward
                    giveReward(player, winner);
                    spinningPlayers.remove(player.getUniqueId());

                    // Schedule close
                    plugin.getServer().getScheduler().runTaskLater(plugin,
                            () -> { if (player.isOnline()) player.closeInventory(); }, 60L);
                    cancel();
                    return;
                }

                // Scroll animation
                Collections.shuffle(rewards);
                for (int i = 0; i < scrollSlots.length; i++) {
                    CrateReward r = rewards.get(i % rewards.size());
                    gui.setItem(scrollSlots[i], buildRewardItem(r));
                }

                // Sound tick
                String soundStr = plugin.getConfig().getString("animation.sound-spin", "BLOCK_NOTE_BLOCK_PLING");
                try {
                    float pitch = 0.5f + (tick / (float) maxTicks);
                    player.playSound(player.getLocation(), Sound.valueOf(soundStr), 0.5f, pitch);
                } catch (Exception ignored) {}

                tick += spinDelay;
            }
        }.runTaskTimer(plugin, 0L, spinDelay);
    }

    // ============ REWARDS ============
    public List<CrateReward> getRewards(CrateType type) {
        List<CrateReward> list = new ArrayList<>();
        ConfigurationSection section = plugin.getConfig()
                .getConfigurationSection("crates." + type.getConfigKey() + ".rewards");
        if (section == null) return list;

        for (String key : section.getKeys(false)) {
            ConfigurationSection r = section.getConfigurationSection(key);
            if (r == null) continue;
            String rewardType = r.getString("type", "ITEM");
            int weight = r.getInt("weight", 10);

            if ("COMMAND".equalsIgnoreCase(rewardType)) {
                String cmd = r.getString("command", "");
                String displayName = r.getString("display-name", "&eRicompensa");
                list.add(new CrateReward(cmd, displayName, weight));
            } else if ("EXPERIENCE".equalsIgnoreCase(rewardType)) {
                int amount = r.getInt("amount", 10);
                list.add(new CrateReward(CrateReward.RewardType.EXPERIENCE, null,
                        "&aEsperienza", amount, weight, new HashMap<>()));
            } else {
                Material mat = Material.matchMaterial(r.getString("item", "STONE"));
                if (mat == null) mat = Material.STONE;
                String name = r.getString("name", null);
                int amount = r.getInt("amount", 1);
                Map<Enchantment, Integer> enchants = new HashMap<>();
                ConfigurationSection enc = r.getConfigurationSection("enchants");
                if (enc != null) {
                    for (String enchKey : enc.getKeys(false)) {
                        Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchKey.toLowerCase()));
                        if (enchantment != null) enchants.put(enchantment, enc.getInt(enchKey));
                    }
                }
                list.add(new CrateReward(CrateReward.RewardType.ITEM, mat, name, amount, weight, enchants));
            }
        }
        return list;
    }

    private CrateReward pickWeightedReward(List<CrateReward> rewards) {
        int totalWeight = rewards.stream().mapToInt(CrateReward::getWeight).sum();
        int roll = new Random().nextInt(totalWeight);
        int cumulative = 0;
        for (CrateReward r : rewards) {
            cumulative += r.getWeight();
            if (roll < cumulative) return r;
        }
        return rewards.get(0);
    }

    private void giveReward(Player player, CrateReward reward) {
        switch (reward.getType()) {
            case ITEM -> {
                ItemStack item = buildRewardItem(reward);
                player.getInventory().addItem(item).forEach((slot, leftover) ->
                        player.getWorld().dropItemNaturally(player.getLocation(), leftover));
            }
            case EXPERIENCE -> player.giveExp(reward.getAmount());
            case COMMAND -> {
                String cmd = reward.getCommand().replace("%player%", player.getName());
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd);
            }
        }
    }

    private ItemStack buildRewardItem(CrateReward reward) {
        if (reward.getType() == CrateReward.RewardType.COMMAND) {
            return buildItem(Material.PAPER, reward.getCommandDisplayName());
        }
        if (reward.getType() == CrateReward.RewardType.EXPERIENCE) {
            return buildItem(Material.EXPERIENCE_BOTTLE, "&aEsperienza x" + reward.getAmount());
        }
        Material mat = reward.getItem();
        if (mat == null) mat = Material.STONE;
        ItemStack item = new ItemStack(mat, Math.max(1, reward.getAmount()));
        ItemMeta meta = item.getItemMeta();
        if (reward.getItemName() != null) meta.displayName(Utils.color(reward.getItemName()));
        if (reward.getEnchants() != null) {
            reward.getEnchants().forEach((ench, lvl) -> meta.addEnchant(ench, lvl, true));
        }
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Utils.color(name));
        item.setItemMeta(meta);
        return item;
    }

    private String getRewardName(CrateReward reward) {
        if (reward.getType() == CrateReward.RewardType.COMMAND) return reward.getCommandDisplayName();
        if (reward.getType() == CrateReward.RewardType.EXPERIENCE) return "&aEsperienza x" + reward.getAmount();
        return reward.getItemName() != null ? reward.getItemName() : reward.getItem().name();
    }

    public boolean isSpinning(UUID uuid) { return spinningPlayers.contains(uuid); }
    public Map<Location, CrateType> getPlacedCrates() { return placedCrates; }
}
