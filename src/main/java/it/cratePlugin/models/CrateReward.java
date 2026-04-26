package it.cratePlugin.models;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.Map;

public class CrateReward {

    public enum RewardType { ITEM, EXPERIENCE, COMMAND }

    private final RewardType type;
    private final Material item;
    private final String itemName;
    private final int amount;
    private final int weight;
    private final Map<Enchantment, Integer> enchants;
    private final String command;
    private final String commandDisplayName;

    // Item/XP reward
    public CrateReward(RewardType type, Material item, String itemName,
                       int amount, int weight, Map<Enchantment, Integer> enchants) {
        this.type = type;
        this.item = item;
        this.itemName = itemName;
        this.amount = amount;
        this.weight = weight;
        this.enchants = enchants;
        this.command = null;
        this.commandDisplayName = null;
    }

    // Command reward
    public CrateReward(String command, String displayName, int weight) {
        this.type = RewardType.COMMAND;
        this.item = null;
        this.itemName = null;
        this.amount = 0;
        this.weight = weight;
        this.enchants = null;
        this.command = command;
        this.commandDisplayName = displayName;
    }

    public RewardType getType()              { return type; }
    public Material getItem()               { return item; }
    public String getItemName()             { return itemName; }
    public int getAmount()                  { return amount; }
    public int getWeight()                  { return weight; }
    public Map<Enchantment, Integer> getEnchants() { return enchants; }
    public String getCommand()              { return command; }
    public String getCommandDisplayName()   { return commandDisplayName; }
}
