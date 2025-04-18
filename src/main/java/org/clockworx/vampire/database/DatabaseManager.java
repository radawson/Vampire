package org.clockworx.vampire.database;

import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.clockworx.vampire.entity.VampirePlayer;
import org.clockworx.vampire.config.VampireConfig;
import org.clockworx.vampire.config.LanguageConfig;
import org.clockworx.vampire.entity.BloodOffer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.logging.Level;
import java.util.concurrent.CompletableFuture;
import java.util.List;

/**
 * Interface for database operations related to vampire players.
 * All database operations should go through this interface, which will delegate
 * to the appropriate backend implementation (MySQL, SQLite, YAML, etc.).
 */
public interface DatabaseManager {
    
    /**
     * Initializes the database connection and creates necessary tables.
     * 
     * @return A CompletableFuture that completes when initialization is done
     */
    CompletableFuture<Void> initialize();
    
    /**
     * Shuts down the database connection.
     * 
     * @return A CompletableFuture that completes when shutdown is done
     */
    CompletableFuture<Void> shutdown();
    
    /**
     * Gets a player's vampire data from the database.
     * 
     * @param uuid The player's UUID
     * @return A CompletableFuture that completes with the player's vampire data, or null if not found
     */
    CompletableFuture<VampirePlayer> getPlayer(UUID uuid);
    
    /**
     * Saves a player's vampire data to the database.
     * 
     * @param player The player's vampire data
     * @return A CompletableFuture that completes when the save is done
     */
    CompletableFuture<Void> savePlayer(VampirePlayer player);
    
    /**
     * Deletes a player's vampire data from the database.
     * 
     * @param uuid The player's UUID
     * @return A CompletableFuture that completes when the deletion is done
     */
    CompletableFuture<Void> deletePlayer(UUID uuid);
    
    /**
     * Checks if a player is a vampire.
     * 
     * @param uuid The player's UUID
     * @return A CompletableFuture that completes with true if the player is a vampire, false otherwise
     */
    CompletableFuture<Boolean> isVampire(UUID uuid);
    
    /**
     * Checks if a player is infected.
     * 
     * @param uuid The player's UUID
     * @return A CompletableFuture that completes with true if the player is infected, false otherwise
     */
    CompletableFuture<Boolean> isInfected(UUID uuid);
    
    /**
     * Gets a player's blood level.
     * 
     * @param uuid The player's UUID
     * @return A CompletableFuture that completes with the player's blood level, or 0.0 if not found
     */
    CompletableFuture<Double> getBloodLevel(UUID uuid);
    
    /**
     * Sets a player's blood level.
     * 
     * @param uuid The player's UUID
     * @param blood The new blood level
     * @return A CompletableFuture that completes when the update is done
     */
    CompletableFuture<Void> setBloodLevel(UUID uuid, double blood);
    
    /**
     * Gets a player's infection level.
     * 
     * @param uuid The player's UUID
     * @return A CompletableFuture that completes with the player's infection level, or 0.0 if not found
     */
    CompletableFuture<Double> getInfectionLevel(UUID uuid);
    
    /**
     * Sets a player's infection level.
     * 
     * @param uuid The player's UUID
     * @param infection The new infection level
     * @return A CompletableFuture that completes when the update is done
     */
    CompletableFuture<Void> setInfectionLevel(UUID uuid, double infection);
    
    /**
     * Gets a player's infection reason.
     * 
     * @param uuid The player's UUID
     * @return A CompletableFuture that completes with the player's infection reason, or null if not found
     */
    CompletableFuture<String> getInfectionReason(UUID uuid);
    
    /**
     * Sets a player's infection reason.
     * 
     * @param uuid The player's UUID
     * @param reason The new infection reason
     * @return A CompletableFuture that completes when the update is done
     */
    CompletableFuture<Void> setInfectionReason(UUID uuid, String reason);
    
    /**
     * Gets a player's infection time.
     * 
     * @param uuid The player's UUID
     * @return A CompletableFuture that completes with the player's infection time, or 0 if not found
     */
    CompletableFuture<Long> getInfectionTime(UUID uuid);
    
    /**
     * Sets a player's infection time.
     * 
     * @param uuid The player's UUID
     * @param time The new infection time
     * @return A CompletableFuture that completes when the update is done
     */
    CompletableFuture<Void> setInfectionTime(UUID uuid, long time);
    
    /**
     * Gets a player's last shriek time.
     * 
     * @param uuid The player's UUID
     * @return A CompletableFuture that completes with the player's last shriek time, or 0 if not found
     */
    CompletableFuture<Long> getLastShriekTime(UUID uuid);
    
    /**
     * Sets a player's last shriek time.
     * 
     * @param uuid The player's UUID
     * @param time The new last shriek time
     * @return A CompletableFuture that completes when the update is done
     */
    CompletableFuture<Void> setLastShriekTime(UUID uuid, long time);
    
    /**
     * Gets a player's last blood trade time.
     * 
     * @param uuid The player's UUID
     * @return A CompletableFuture that completes with the player's last blood trade time, or 0 if not found
     */
    CompletableFuture<Long> getLastBloodTradeTime(UUID uuid);
    
    /**
     * Sets a player's last blood trade time.
     * 
     * @param uuid The player's UUID
     * @param time The new last blood trade time
     * @return A CompletableFuture that completes when the update is done
     */
    CompletableFuture<Void> setLastBloodTradeTime(UUID uuid, long time);
    
    /**
     * Gets a player's last blood trade partner.
     * 
     * @param uuid The player's UUID
     * @return A CompletableFuture that completes with the player's last blood trade partner, or null if not found
     */
    CompletableFuture<UUID> getLastBloodTradePartner(UUID uuid);
    
    /**
     * Sets a player's last blood trade partner.
     * 
     * @param uuid The player's UUID
     * @param partner The new last blood trade partner
     * @return A CompletableFuture that completes when the update is done
     */
    CompletableFuture<Void> setLastBloodTradePartner(UUID uuid, UUID partner);
    
    /**
     * Gets a player's last blood trade amount.
     * 
     * @param uuid The player's UUID
     * @return A CompletableFuture that completes with the player's last blood trade amount, or 0.0 if not found
     */
    CompletableFuture<Double> getLastBloodTradeAmount(UUID uuid);
    
    /**
     * Sets a player's last blood trade amount.
     * 
     * @param uuid The player's UUID
     * @param amount The new last blood trade amount
     * @return A CompletableFuture that completes when the update is done
     */
    CompletableFuture<Void> setLastBloodTradeAmount(UUID uuid, double amount);
    
    /**
     * Gets a player's last blood trade type.
     * 
     * @param uuid The player's UUID
     * @return A CompletableFuture that completes with the player's last blood trade type, or null if not found
     */
    CompletableFuture<String> getLastBloodTradeType(UUID uuid);
    
    /**
     * Sets a player's last blood trade type.
     * 
     * @param uuid The player's UUID
     * @param type The new last blood trade type
     * @return A CompletableFuture that completes when the update is done
     */
    CompletableFuture<Void> setLastBloodTradeType(UUID uuid, String type);
    
    /**
     * Creates a new blood offer.
     * 
     * @param senderUuid The UUID of the player offering blood
     * @param targetUuid The UUID of the player being offered blood
     * @param amount The amount of blood being offered
     * @return A CompletableFuture that completes with the created blood offer
     */
    CompletableFuture<BloodOffer> createBloodOffer(UUID senderUuid, UUID targetUuid, double amount);
    
    /**
     * Gets a pending blood offer for a player.
     * 
     * @param playerUuid The player's UUID
     * @return A CompletableFuture that completes with the blood offer, or null if none exists
     */
    CompletableFuture<BloodOffer> getBloodOffer(UUID playerUuid);
    
    /**
     * Accepts a blood offer.
     * 
     * @param playerUuid The UUID of the player accepting the offer
     * @return A CompletableFuture that completes with true if the offer was accepted successfully, false otherwise
     */
    CompletableFuture<Boolean> acceptBloodOffer(UUID playerUuid);
    
    /**
     * Rejects a blood offer.
     * 
     * @param playerUuid The UUID of the player rejecting the offer
     * @return A CompletableFuture that completes when the offer is rejected
     */
    CompletableFuture<Boolean> rejectBloodOffer(UUID playerUuid);
    
    /**
     * Gets all pending blood offers.
     * 
     * @return A CompletableFuture that completes with a list of all pending offers
     */
    CompletableFuture<List<BloodOffer>> getAllBloodOffers();
    
    /**
     * Cleans up expired blood offers.
     * 
     * @param timeoutMillis The timeout in milliseconds
     * @return A CompletableFuture that completes when the cleanup is done
     */
    CompletableFuture<Boolean> cleanupExpiredOffers(long timeoutMillis);
    
    /**
     * Adds an infection to a player.
     * 
     * @param uuid The player's UUID
     * @param amount The amount of infection to add
     * @param reason The reason for the infection
     * @return A CompletableFuture that completes when the update is done
     */
    CompletableFuture<Boolean> addInfection(UUID uuid, double amount, String reason);
    
    /**
     * Sets a player's vampire status.
     * 
     * @param uuid The player's UUID
     * @param isVampire The new vampire status
     * @return A CompletableFuture that completes when the update is done
     */
    CompletableFuture<Boolean> setVampire(UUID uuid, boolean isVampire);
    
    /**
     * Gets all vampires.
     * 
     * @return A CompletableFuture that completes with a list of all vampires
     */
    CompletableFuture<List<VampirePlayer>> getAllVampires();
    
    /**
     * Saves a configuration value to the database.
     * 
     * @param key The configuration key
     * @param value The configuration value
     * @return A CompletableFuture that completes when the save is done
     */
    CompletableFuture<Boolean> saveConfig(String key, String value);
    
    /**
     * Gets a configuration value from the database.
     * 
     * @param key The configuration key
     * @return A CompletableFuture that completes with the configuration value, or null if not found
     */
    CompletableFuture<String> getConfig(String key);
    
    /**
     * Saves a language value to the database.
     * 
     * @param key The language key
     * @param value The language value
     * @return A CompletableFuture that completes when the save is done
     */
    CompletableFuture<Boolean> saveLanguage(String key, String value);
    
    /**
     * Gets a language value from the database.
     * 
     * @param key The language key
     * @return A CompletableFuture that completes with the language value, or null if not found
     */
    CompletableFuture<String> getLanguage(String key);
    
    /**
     * Uses blood from a player.
     * 
     * @param uuid The player's UUID
     * @param amount The amount of blood to use
     * @return A CompletableFuture that completes with true if successful, false otherwise
     */
    CompletableFuture<Boolean> useBlood(UUID uuid, double amount);
    
    /**
     * Loads the configuration from the database.
     * 
     * @return A CompletableFuture that completes with the loaded configuration
     */
    CompletableFuture<VampireConfig> loadConfig();
    
    /**
     * Saves the configuration to the database.
     * 
     * @param config The configuration to save
     * @return A CompletableFuture that completes when the save is done
     */
    CompletableFuture<Boolean> saveConfig(VampireConfig config);
    
    /**
     * Loads the language configuration from the database.
     * 
     * @return A CompletableFuture that completes with the loaded language configuration
     */
    CompletableFuture<LanguageConfig> loadLanguage();
    
    /**
     * Saves the language configuration to the database.
     * 
     * @param language The language configuration to save
     * @return A CompletableFuture that completes when the save is done
     */
    CompletableFuture<Boolean> saveLanguage(LanguageConfig language);
} 