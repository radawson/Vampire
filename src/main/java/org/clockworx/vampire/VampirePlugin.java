package org.clockworx.vampire;

import org.bukkit.plugin.java.JavaPlugin;
import org.clockworx.vampire.altar.AltarManager;
import org.clockworx.vampire.cmd.VampireCommand;
import org.clockworx.vampire.config.LanguageConfig;
import org.clockworx.vampire.config.VampireConfig;
import org.clockworx.vampire.database.DatabaseManager;
import org.clockworx.vampire.database.HibernateDatabaseManager;
import org.clockworx.vampire.entity.VampirePlayer;
import org.clockworx.vampire.listener.VampireListener;
import org.clockworx.vampire.task.VampireTask;
import org.clockworx.vampire.util.BloodFlaskUtil;
import org.clockworx.vampire.util.HolyWaterUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Main plugin class for the Vampire plugin.
 * This class serves as the entry point and central manager for the plugin.
 */
public class VampirePlugin extends JavaPlugin {
    
    private static VampirePlugin instance;
    private VampireConfig config;
    private LanguageConfig language;
    private DatabaseManager databaseManager;
    private VampireTask task;
    private VampireListener listener;
    private BloodFlaskUtil bloodFlaskUtil;
    private HolyWaterUtil holyWaterUtil;
    private VampireCommand vampireCommand;
    private AltarManager altarManager;
    
    // Cache of online vampire players
    private final Map<UUID, VampirePlayer> playerCache = new HashMap<>();
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config if it doesn't exist
        saveDefaultConfig();
        
        // Initialize components
        initializeConfigs();
        initializeDatabase();
        initializeUtils();
        initializeCommands();
        initializeListeners();
        initializeTasks();
        initializeAltars();
        
        getLogger().info("Vampire plugin has been enabled!");
    }
    
    @Override
    public void onDisable() {
        // Shutdown components
        if (task != null) {
            task.shutdown();
        }
        
        if (databaseManager != null) {
            databaseManager.shutdown().join();
        }

        
        // Clear player cache
        playerCache.clear();
        
        getLogger().info("Vampire plugin has been disabled!");
    }
    
    /**
     * Initialize configuration files
     */
    private void initializeConfigs() {
        config = new VampireConfig(this);
        language = new LanguageConfig(this);
    }
    
    /**
     * Initialize database connection
     */
    private void initializeDatabase() {
        databaseManager = new HibernateDatabaseManager(this);
        databaseManager.initialize().join();
    }
    
    /**
     * Initialize utility classes
     */
    private void initializeUtils() {
        bloodFlaskUtil = new BloodFlaskUtil(this);
        holyWaterUtil = new HolyWaterUtil(this);
    }
    
    /**
     * Initialize commands
     */
    private void initializeCommands() {
        vampireCommand = new VampireCommand(this);
        getCommand("vampire").setExecutor(vampireCommand);
        getCommand("vampire").setTabCompleter(vampireCommand);
    }
    
    /**
     * Initialize event listeners
     */
    private void initializeListeners() {
        listener = new VampireListener(this);
        getServer().getPluginManager().registerEvents(listener, this);
    }
    
    /**
     * Initialize tick-based tasks
     */
    private void initializeTasks() {
        task = new VampireTask(this);
        task.start();
    }
    
    /**
     * Initialize altar manager
     */
    private void initializeAltars() {
        altarManager = new AltarManager();
    }
    
    /**
     * Get a player's vampire data, loading it from the database if necessary
     * 
     * @param uuid The player's UUID
     * @return A CompletableFuture that will complete with the player's vampire data
     */
    public CompletableFuture<VampirePlayer> getVampirePlayer(UUID uuid) {
        // Check cache first
        if (playerCache.containsKey(uuid)) {
            return CompletableFuture.completedFuture(playerCache.get(uuid));
        }
        
        // Load from database
        return databaseManager.getPlayer(uuid).thenApply(player -> {
            if (player != null) {
                playerCache.put(uuid, player);
            }
            return player;
        });
    }
    
    /**
     * Save a player's vampire data to the database
     * 
     * @param player The player to save
     * @return A CompletableFuture that will complete when the save is done
     */
    public CompletableFuture<Void> saveVampirePlayer(VampirePlayer player) {
        return databaseManager.savePlayer(player);
    }
    
    /**
     * Remove a player from the cache
     * 
     * @param uuid The player's UUID
     */
    public void removeFromCache(UUID uuid) {
        playerCache.remove(uuid);
    }
    
    /**
     * Get the plugin instance
     * 
     * @return The plugin instance
     */
    public static VampirePlugin getInstance() {
        return instance;
    }
    
    /**
     * Get the plugin configuration
     * 
     * @return The plugin configuration
     */
    public VampireConfig getVampireConfig() {
        return config;
    }
    
    /**
     * Get the language configuration
     * 
     * @return The language configuration
     */
    public LanguageConfig getLanguageConfig() {
        return language;
    }
    
    /**
     * Get the database manager
     * 
     * @return The database manager
     */
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    /**
     * Get the blood flask utility
     * 
     * @return The blood flask utility
     */
    public BloodFlaskUtil getBloodFlaskUtil() {
        return bloodFlaskUtil;
    }
    
    /**
     * Get the holy water utility
     * 
     * @return The holy water utility
     */
    public HolyWaterUtil getHolyWaterUtil() {
        return holyWaterUtil;
    }
    
    /**
     * Get the altar manager
     * @return The altar manager
     */
    public AltarManager getAltarManager() {
        return altarManager;
    }
    
    /**
     * Log a debug message
     * 
     * @param message The message to log
     */
    public void debug(String message) {
        if (config.isDebugEnabled()) {
            getLogger().info("[DEBUG] " + message);
        }
    }
    
    /**
     * Log an error message
     * 
     * @param message The message to log
     * @param throwable The throwable to log
     */
    public void error(String message, Throwable throwable) {
        getLogger().log(Level.SEVERE, message, throwable);
    }
} 