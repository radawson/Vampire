package org.clockworx.vampire;

import java.util.logging.Level;
import java.util.Map;

import org.bukkit.plugin.java.JavaPlugin;
import org.clockworx.vampire.cmd.VampireCommand;
import org.clockworx.vampire.config.LanguageConfig;
import org.clockworx.vampire.config.VampireConfig;
import org.clockworx.vampire.database.HibernateDatabaseManager;
import org.clockworx.vampire.level.LevelManager;
import org.clockworx.vampire.manager.AltarManager;
import org.clockworx.vampire.manager.BloodManager;
import org.clockworx.vampire.manager.ItemManager;
import org.clockworx.vampire.manager.VampireManager;
import org.clockworx.vampire.task.BloodRegenerationTask;
import org.clockworx.vampire.task.VampireTask;
import org.clockworx.vampire.util.FxUtil;
import org.clockworx.vampire.util.ResourceUtil;
import org.clockworx.vampire.util.SunUtil;
import org.clockworx.vampire.util.VampireMessages;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.FluentConfiguration;

/**
 * Main plugin class for the Vampire plugin.
 * This class serves as the entry point and central manager for the plugin.
 */
public final class VampirePlugin extends JavaPlugin {

    private static VampirePlugin plugin;
    private VampireConfig config;
    private LanguageConfig languageConfig;
    private HibernateDatabaseManager databaseManager;
    private VampireManager vampireManager;
    private BloodManager bloodManager;
    private AltarManager altarManager;
    private ItemManager itemManager;
    private VampireCommand vampireCommand;
    private LevelManager levelManager;
    private VampireTask vampireTask;
    private BloodRegenerationTask saveTask;

    @Override
    public void onEnable() {
        plugin = this; // Assign in onEnable

        // --- Initialize Utilities ---
        FxUtil.init(this);
        SunUtil.init(this);
        ResourceUtil.init(this);

        // --- Configuration ---
        // Load configurations first
        if (!initializeConfigs()) { // Method now returns false on success, true on failure
            getLogger().severe("Failed to initialize configurations. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize Messages AFTER configs are loaded
        VampireMessages.init(this);

        // --- Database Migrations ---
        // Run Flyway migrations BEFORE initializing Hibernate/DatabaseManager
        if (!runDatabaseMigrations()) {
            getLogger().severe("Database migration failed. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // --- Initialize Level Manager EARLY ---
        // Needs to be ready before other managers or tasks that depend on it
        levelManager = new LevelManager(this);
        levelManager.loadLevels();

        // --- Initialize Database Abstraction Layer ---
        // This now only creates the manager instance; Hibernate session factory
        // will be initialized lazily on first use via HibernateConfig.getSessionFactory()
        initializeDatabaseManager();

        // --- Initialize Core Components ---
        initializeManagers(); // VampireManager is initialized here
        registerCommands();
        registerListeners();
        startTasks(); // BloodRegenerationTask is started here

        getLogger().info("Vampire plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        // Save data on disable
        if (vampireManager != null) {
            // vampireManager.saveAllVampires(); // Method doesn't seem to exist
            // Attempting shutdown instead, assuming it handles saving
            try {
                 vampireManager.shutdown();
            } catch (Exception e) {
                 getLogger().log(Level.SEVERE, "Error during VampireManager shutdown in onDisable", e);
            }
        }
        if (databaseManager != null) {
            // databaseManager.close();
            try {
                databaseManager.shutdown(); // Try shutdown instead of close
            } catch (Exception e) {
                 getLogger().log(Level.SEVERE, "Error during DatabaseManager shutdown in onDisable", e);
            }
        }

        // Cancel tasks (Handled by plugin instance directly)
        if (vampireTask != null) {
            vampireTask.cancel();
        }
        if (saveTask != null) {
            // Assuming saveTask is a BukkitRunnable
            if (saveTask instanceof org.bukkit.scheduler.BukkitRunnable) {
                ((org.bukkit.scheduler.BukkitRunnable) saveTask).cancel();
            }
        }

        getLogger().info("Vampire plugin disabled!");
    }

    /**
     * Initialize configurations.
     * @return true if initialization succeeds, false otherwise.
     */
    private boolean initializeConfigs() {
        try {
            config = new VampireConfig(this);
            // Config loading happens within its constructor now

            // Load LanguageConfig immediately after main config
            languageConfig = new LanguageConfig(this);
            languageConfig.loadLanguage(config.getLanguage());

            // --- Config Version Check ---
            String loadedConfigVersionStr = config.getConfig().getString("version", "0.0.0");

            if (!getPluginMeta().getVersion().equals(loadedConfigVersionStr)) {
                getLogger().log(Level.WARNING, "*********************************************************************");
                getLogger().log(Level.WARNING, "Your config.yml version does not match the plugin version!");
                getLogger().log(Level.WARNING, "Config Version: " + loadedConfigVersionStr + ", Plugin Version: " + getPluginMeta().getVersion());
                getLogger().log(Level.WARNING, "Please backup your current config.yml, delete it, and let the plugin generate a new one.");
                getLogger().log(Level.WARNING, "Then, manually merge your old settings into the new file.");
                getLogger().log(Level.WARNING, "Using a mismatched config may cause errors or unexpected behavior.");
                getLogger().log(Level.WARNING, "*********************************************************************");
            }
             getLogger().info("Configurations initialized!");
             return true; // Indicate success
        } catch (Exception e) {
             getLogger().log(Level.SEVERE, "Error initializing configurations", e);
             return false; // Indicate failure
        }
    }

    /**
     * Executes database migrations using Flyway.
     * @return true if migrations were successful, false otherwise.
     */
    private boolean runDatabaseMigrations() {
        getLogger().info("Starting database migration check...");
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // Set context class loader for Flyway to find drivers/resources
            Thread.currentThread().setContextClassLoader(getClassLoader());

            // Get database details from loaded config
            String dbType = config.getDatabaseType();
            String dbUrl = config.getDatabaseUrl();
            String dbUser = config.getDatabaseUser();
            String dbPassword = config.getDatabasePassword();
            String tablePrefix = config.getDatabaseTablePrefix(); // Get the prefix

            // Load the appropriate JDBC driver explicitly
            // This ensures it's loaded by the correct classloader
            try {
                if ("mysql".equalsIgnoreCase(dbType)) {
                    Class.forName("com.mysql.cj.jdbc.Driver", true, getClassLoader());
                 } else if ("sqlite".equalsIgnoreCase(dbType)) {
                    Class.forName("org.sqlite.JDBC", true, getClassLoader());
                 } else if ("postgres".equalsIgnoreCase(dbType) || "postgresql".equalsIgnoreCase(dbType)) {
                     Class.forName("org.postgresql.Driver", true, getClassLoader());
                 }
                 // Add other database drivers here if needed
            } catch (ClassNotFoundException e) {
                getLogger().log(Level.SEVERE, "Could not find JDBC driver for database type: " + dbType, e);
                return false;
            }

            FluentConfiguration flywayConfig = Flyway.configure(getClassLoader()) // Pass classloader
                .dataSource(dbUrl, dbUser, dbPassword)
                .locations("classpath:db/migration") // Point to migration scripts in resources
                .encoding("UTF-8")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .placeholders(Map.of("tablePrefix", tablePrefix));

             // Set the schema history table name with the prefix
             // Flyway's default table is flyway_schema_history
             String historyTableName = tablePrefix.isEmpty() ? "flyway_schema_history" : tablePrefix + "flyway_schema_history";
             flywayConfig.table(historyTableName);
             getLogger().info("Using Flyway history table: " + historyTableName);

            Flyway flyway = flywayConfig.load();

            // Run migrations
            flyway.migrate();

            getLogger().info("Database migration check completed successfully.");
            return true; // Success
        } catch (FlywayException e) {
            getLogger().log(Level.SEVERE, "Database migration failed!", e);
            // Log specific migration error details if available
            if (e.getCause() != null) {
                 getLogger().log(Level.SEVERE, "Cause: " + e.getCause().getMessage(), e.getCause());
            }
            return false; // Failure
        } catch (Exception e) { // Catch other potential errors during setup
            getLogger().log(Level.SEVERE, "An unexpected error occurred during database migration setup!", e);
            return false; // Failure
        } finally {
             // Restore original class loader
             Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }


    /**
     * Initialize database manager instance.
     * Note: This no longer initializes the Hibernate SessionFactory.
     */
    private void initializeDatabaseManager() {
        databaseManager = new HibernateDatabaseManager(this);
        // databaseManager.initialize().join(); // REMOVED - Hibernate initializes lazily now
        getLogger().info("DatabaseManager initialized (Hibernate SessionFactory will load on first use).");
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
     * Register event listeners
     */
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new org.clockworx.vampire.listener.VampireListener(this, vampireManager), this);
        getLogger().info("Registered event listeners.");
    }

    /**
     * Start tasks
     */
    private void startTasks() {
        vampireTask = new VampireTask(this);
        saveTask = new BloodRegenerationTask(this);
        vampireTask.start();
        saveTask.runTaskTimer(this, 20L, 20L);

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

    /**
     * Reloads the plugin's configuration and language files.
     */
    public void reload() {
        try {
            // Reload main config
            config.loadConfig();
            // Reload language files
            VampireMessages.reloadMessages();
            // Reload level config
            if (levelManager != null) { 
                 levelManager.reloadLevels();
            }
            // Reload altars (might depend on config changes)
            if (altarManager != null) { 
                 // altarManager.loadAltars(); // Method doesn't seem to exist for reloading
                 // TODO: Determine correct way to reload AltarManager if needed
                 getLogger().warning("Altar reloading not implemented in reload() yet."); // Add a warning
            }
            // Reload vampire data (optional, might not be needed on config reload)
            // vampireManager.reloadVampires();
            getLogger().info("Configuration and language files reloaded.");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to reload plugin configuration", e);
        }
    }

    // Getters
    public VampireConfig getVampireConfig() {
        return config;
    }

    public LanguageConfig getLanguageConfig() {
        return languageConfig;
    }

    // Getter remains the same type, but initialization timing changed
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

    public LevelManager getLevelManager() {
        return levelManager;
    }

    /**
     * Static getter for the plugin instance.
     * Useful for accessing the plugin from static contexts, but use with caution.
     * Consider dependency injection where possible.
     * @return The singleton instance of VampirePlugin.
     */
    public static VampirePlugin getInstance() {
        return plugin;
    }
} 