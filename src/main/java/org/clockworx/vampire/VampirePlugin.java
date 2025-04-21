package org.clockworx.vampire;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.Plugin;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.clockworx.vampire.cmd.VampireCommand;
import org.clockworx.vampire.config.LanguageConfig;
import org.clockworx.vampire.config.VampireConfig;
import org.clockworx.vampire.database.DatabaseManager;
import org.clockworx.vampire.database.HibernateDatabaseManager;
import org.clockworx.vampire.manager.AltarManager;
import org.clockworx.vampire.manager.BloodManager;
import org.clockworx.vampire.manager.VampireManager;
import org.clockworx.vampire.manager.ItemManager;
import org.clockworx.vampire.task.BloodRegenerationTask;
import org.clockworx.vampire.task.VampireTask;
import org.clockworx.vampire.util.VampireMessages;
import org.clockworx.vampire.util.FxUtil;
import org.clockworx.vampire.util.SunUtil;

import java.util.logging.Level;

/**
 * Main plugin class for the Vampire plugin.
 * This class serves as the entry point and central manager for the plugin.
 */
public final class VampirePlugin extends JavaPlugin {
    
   // Define the expected config version
    private static final int CURRENT_CONFIG_VERSION = 1; 

    private static VampirePlugin plugin; // Declare static plugin instance
    private VampireConfig config;
    private LanguageConfig languageConfig;
    private HibernateDatabaseManager databaseManager;
    private VampireManager vampireManager;
    private BloodManager bloodManager;
    private AltarManager altarManager;
    private ItemManager itemManager;
    private VampireCommand vampireCommand;

    @Override
    public void onEnable() {
        plugin = this; // Assign in onEnable

        // --- Initialize Utilities ---
        FxUtil.init(this); 
        // VampireMessages.init(this); // Moved AFTER initializeConfigs
        SunUtil.init(this);

        // --- Configuration ---
        if (!initializeConfigs()) {
            VampireMessages.init(this); // Initialize Messages AFTER configs are loaded
            initializeDatabase();
            initializeManagers();
            registerCommands();
            startTasks();
            
            getLogger().info("Vampire plugin enabled!");
        }
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.shutdown();
        }

        if (vampireManager != null) {
            vampireManager.shutdown();
        }

        getLogger().info("Vampire plugin disabled!");
    }

    /**
     * Initialize configurations
     */
    private boolean initializeConfigs() {
        config = new VampireConfig(this);
        // Config loading happens within its constructor now

        // Load LanguageConfig immediately after main config
        languageConfig = new LanguageConfig(this);
        languageConfig.loadLanguage(config.getLanguage()); 
        // Now Messages can be initialized safely AFTER this method finishes
        // VampireMessages.init(this); // This call remains in onEnable, but must happen AFTER initializeConfigs

        // --- Config Version Check ---
        int loadedConfigVersion = config.getConfig().getInt("config-version", 0); // Read version, default to 0 if missing
        if (loadedConfigVersion < CURRENT_CONFIG_VERSION) {
            getLogger().log(Level.WARNING, "*********************************************************************");
            getLogger().log(Level.WARNING, "Your config.yml is outdated (Version: " + loadedConfigVersion + ", Expected: " + CURRENT_CONFIG_VERSION + ")!");
            getLogger().log(Level.WARNING, "Please backup your current config.yml, delete it, and let the plugin generate a new one.");
            getLogger().log(Level.WARNING, "You can then manually merge your old settings into the new file.");
            getLogger().log(Level.WARNING, "Some features might not work correctly until the config is updated.");
            getLogger().log(Level.WARNING, "*********************************************************************");
            // Optionally, you could disable the plugin here if the config is too old
            // getServer().getPluginManager().disablePlugin(this);
            // return true; // Indicate failure
        }

        getLogger().info("Configurations initialized!");
        return false;
    }

    /**
     * Initialize database
     */
    private void initializeDatabase() {
        databaseManager = new HibernateDatabaseManager(this);
        databaseManager.initialize().join();

        getLogger().info("Database initialized!");
    }

    /**
     * Initialize managers
     */
    private void initializeManagers() {
        vampireManager = new VampireManager(this);
        bloodManager = new BloodManager(this);
        altarManager = new AltarManager(this);
        itemManager = new ItemManager(this);

        getLogger().info("Managers initialized!");
    }

    /**
     * Register commands
     */
    private void registerCommands() {
        vampireCommand = new VampireCommand(this);
        if (getCommand("vampire") != null) {
            getCommand("vampire").setExecutor(vampireCommand);
            getCommand("vampire").setTabCompleter(vampireCommand);
            getLogger().info("Registered 'vampire' command.");
        } else {
            getLogger().warning("Could not find 'vampire' command registration in plugin.yml!");
        }
    }

    /**
     * Start tasks
     */
    private void startTasks() {
        new BloodRegenerationTask(this).runTaskTimer(this, 20L, 20L);
        new VampireTask(this).start();
        
        getLogger().info("Tasks initialized!");
    }

    /**
     * Debug message
     */
    public void debug(String message) {
        if (config != null && config.isDebug()) {
            getLogger().info("[DEBUG] " + message);
        }
    }

    /**
     * Error message
     */
    public void error(String message, Throwable t) {
        getLogger().log(Level.SEVERE, message, t);
    }

    // Getters
    public VampireConfig getVampireConfig() {
        return config;
    }

    public LanguageConfig getLanguageConfig() {
        return languageConfig;
    }

    public HibernateDatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public VampireManager getVampireManager() {
        return vampireManager;
    }

    public BloodManager getBloodManager() {
        return bloodManager;
    }

    public AltarManager getAltarManager() {
        return altarManager;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public VampireCommand getVampireCommand() {
        return vampireCommand;
    }
} 