package it.cratePlugin;

import it.cratePlugin.commands.*;
import it.cratePlugin.listeners.*;
import it.cratePlugin.managers.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class CratePlugin extends JavaPlugin {

    private static CratePlugin instance;
    private CrateManager crateManager;
    private KeyManager keyManager;
    private DailyLoginManager dailyLoginManager;
    private QuestManager questManager;
    private ShopManager shopManager;
    private DataManager dataManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Init managers
        this.dataManager        = new DataManager(this);
        this.crateManager       = new CrateManager(this);
        this.keyManager         = new KeyManager(this);
        this.dailyLoginManager  = new DailyLoginManager(this);
        this.questManager       = new QuestManager(this);
        this.shopManager        = new ShopManager(this);

        // Register commands
        registerCommands();

        // Register listeners
        registerListeners();

        getLogger().info("CratePlugin abilitato con successo!");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) dataManager.saveAll();
        getLogger().info("CratePlugin disabilitato.");
    }

    private void registerCommands() {
        getCommand("crate").setExecutor(new CrateCommand(this));
        getCommand("cratekey").setExecutor(new CrateKeyCommand(this));
        getCommand("cratedaily").setExecutor(new CrateDailyCommand(this));
        getCommand("cratequest").setExecutor(new CrateQuestCommand(this));
        getCommand("crateshop").setExecutor(new CrateShopCommand(this));
        getCommand("cratereload").setExecutor(new CrateReloadCommand(this));
        getCommand("crateplace").setExecutor(new CratePlaceCommand(this));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new CrateInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new QuestProgressListener(this), this);
    }

    public static CratePlugin getInstance() { return instance; }
    public CrateManager getCrateManager()           { return crateManager; }
    public KeyManager getKeyManager()               { return keyManager; }
    public DailyLoginManager getDailyLoginManager() { return dailyLoginManager; }
    public QuestManager getQuestManager()           { return questManager; }
    public ShopManager getShopManager()             { return shopManager; }
    public DataManager getDataManager()             { return dataManager; }
}
