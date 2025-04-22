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
import org.clockworx.vampire.util.ResourceUtil;

import java.util.logging.Level;

/**
 * Main plugin class for the Vampire plugin.
 * This class serves as the entry point and central manager for the plugin.
 */
public final class VampirePlugin extends JavaPlugin {

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
        SunUtil.init(this);
        ResourceUtil.init(this);
        // VampireMessages.init(this); // Moved AFTER initializeConfigs

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
        // Read the "version" key (which is injected by Gradle from project version)
        // Use a default that won't match the current version if the key is missing.
        String loadedConfigVersionStr = config.getConfig().getString("version", "0.0.0"); 
        
        // Compare the loaded version string from the config with the plugin's version
        if (!getPluginMeta().getVersion().equals(loadedConfigVersionStr)) {
            getLogger().log(Level.WARNING, "*********************************************************************");
            getLogger().log(Level.WARNING, "Your config.yml version does not match the plugin version!");
            getLogger().log(Level.WARNING, "Config Version: " + loadedConfigVersionStr + ", Plugin Version: " + getPluginMeta().getVersion());
            getLogger().log(Level.WARNING, "Please backup your current config.yml, delete it, and let the plugin generate a new one.");
            getLogger().log(Level.WARNING, "Then, manually merge your old settings into the new file.");
            getLogger().log(Level.WARNING, "Using a mismatched config may cause errors or unexpected behavior.");
            getLogger().log(Level.WARNING, "*********************************************************************");
            // You could still add a separate check for EXPECTED_CONFIG_STRUCTURE_VERSION if needed
            // int structureVersion = config.getConfig().getInt("config-structure-version", 0); // Example key
            // if (structureVersion < EXPECTED_CONFIG_STRUCTURE_VERSION) { ... }
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