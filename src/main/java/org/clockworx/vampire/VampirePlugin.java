package org.clockworx.vampire;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import io.papermc.paper.plugin.PaperPlugin;
import org.clockworx.vampire.cmd.VampireCommand;
import org.clockworx.vampire.config.VampireConfig;
import org.clockworx.vampire.database.DatabaseManager;
import org.clockworx.vampire.database.HibernateDatabaseManager;
import org.clockworx.vampire.manager.AltarManager;
import org.clockworx.vampire.manager.BloodManager;
import org.clockworx.vampire.manager.VampireManager;
import org.clockworx.vampire.task.BloodRegenerationTask;
import org.clockworx.vampire.task.DaylightTask;
import org.clockworx.vampire.task.VampireTask;

import java.util.logging.Level;

/**
 * Main plugin class for the Vampire plugin.
 * This class serves as the entry point and central manager for the plugin.
 */
public final class VampirePlugin extends PaperPlugin {
    
    private VampireConfig config;
    private DatabaseManager databaseManager;
    private VampireManager vampireManager;
    private BloodManager bloodManager;
    private AltarManager altarManager;
    private VampireCommand vampireCommand;

    @Override
    public void onEnable() {
        // Initialize configs
        initializeConfigs();

        // Initialize database
        initializeDatabase();

        // Initialize managers
        initializeManagers();

        // Register commands
        registerCommands();

        // Start tasks
        startTasks();

        getLogger().info("Vampire plugin enabled!");
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
    private void initializeConfigs() {
        config = new VampireConfig(this);
        config.load();
    }

    /**
     * Initialize database
     */
    private void initializeDatabase() {
        databaseManager = new HibernateDatabaseManager(this);
        databaseManager.initialize().join();
    }

    /**
     * Initialize managers
     */
    private void initializeManagers() {
        vampireManager = new VampireManager(this);
        bloodManager = new BloodManager(this);
        altarManager = new AltarManager(this);
    }

    /**
     * Register commands
     */
    private void registerCommands() {
        vampireCommand = new VampireCommand(this);
        getCommand("vampire").setExecutor(vampireCommand);
        getCommand("vampire").setTabCompleter(vampireCommand);
    }

    /**
     * Start tasks
     */
    private void startTasks() {
        new BloodRegenerationTask(this).runTaskTimer(this, 20L, 20L);
        new DaylightTask(this).runTaskTimer(this, 20L, 20L);
        new VampireTask(this).runTaskTimer(this, 20L, 20L);
    }

    /**
     * Debug message
     */
    public void debug(String message) {
        if (config.isDebug()) {
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

    public DatabaseManager getDatabaseManager() {
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

    public VampireCommand getVampireCommand() {
        return vampireCommand;
    }
} 