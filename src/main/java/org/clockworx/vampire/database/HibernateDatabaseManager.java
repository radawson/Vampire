package org.clockworx.vampire.database;

import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;
import org.clockworx.vampire.entity.VampirePlayerEntity;
import org.clockworx.vampire.entity.BloodOffer;
import org.clockworx.vampire.entity.BloodOfferEntity;
import org.clockworx.vampire.config.VampireConfig;
import org.clockworx.vampire.config.LanguageConfig;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hibernate.query.MutationQuery;

import java.util.UUID;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class HibernateDatabaseManager implements DatabaseManager {
    private final VampirePlugin plugin;

    public HibernateDatabaseManager(VampirePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Void> initialize() {
        // Initialization is now effectively handled by Flyway (creating/migrating schema)
        // and Hibernate's lazy SessionFactory initialization.
        // We just need to confirm the DatabaseManager instance is ready.
        plugin.getLogger().log(Level.INFO, "HibernateDatabaseManager instance created. Schema managed by Flyway.");
        return CompletableFuture.completedFuture(null); // Indicate immediate completion
    }

    @Override
    public CompletableFuture<Void> shutdown() {
        // The actual SessionFactory shutdown is handled centrally in VampirePlugin.onDisable
        // by calling HibernateConfig.shutdown(). This method might not be strictly needed
        // in the interface anymore, but we keep it for consistency for now.
        plugin.getLogger().log(Level.INFO, "HibernateDatabaseManager shutdown called (actual SessionFactory shutdown managed elsewhere).");
        return CompletableFuture.completedFuture(null);
    }

    // Helper method to execute transactional code safely
    private <T> CompletableFuture<T> executeTransaction(TransactionFunction<T> function) {
        return CompletableFuture.supplyAsync(() -> {
            Transaction tx = null;
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                tx = session.beginTransaction();
                T result = function.apply(session);
                tx.commit();
                return result;
            } catch (Exception e) {
                if (tx != null && tx.isActive()) {
                    try {
                        tx.rollback();
                    } catch (Exception rbEx) {
                        plugin.getLogger().log(Level.SEVERE, "Transaction rollback failed", rbEx);
                    }
                }
                // Log the original error
                plugin.getLogger().log(Level.SEVERE, "Database transaction failed", e);
                // Rethrow as a RuntimeException to fail the CompletableFuture
                throw new RuntimeException("Database transaction failed", e);
            }
        });
    }

    // Helper functional interface for transactions
    @FunctionalInterface
    private interface TransactionFunction<T> {
        T apply(Session session) throws Exception; // Allow checked exceptions
    }

    // Simplified execute function for operations returning Void
    private CompletableFuture<Void> executeTransactionVoid(VoidTransactionFunction function) {
        return CompletableFuture.runAsync(() -> {
            Transaction tx = null;
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                tx = session.beginTransaction();
                function.apply(session);
                tx.commit();
            } catch (Exception e) {
                if (tx != null && tx.isActive()) {
                    try {
                        tx.rollback();
                    } catch (Exception rbEx) {
                        plugin.getLogger().log(Level.SEVERE, "Transaction rollback failed", rbEx);
                    }
                }
                // Log the original error
                plugin.getLogger().log(Level.SEVERE, "Database transaction failed", e);
                // Rethrow as a RuntimeException to fail the CompletableFuture
                throw new RuntimeException("Database transaction failed", e);
            }
        });
    }

    // Helper functional interface for void transactions
    @FunctionalInterface
    private interface VoidTransactionFunction {
        void apply(Session session) throws Exception; // Allow checked exceptions
    }

    @Override
    public CompletableFuture<VampirePlayer> getPlayer(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            // Use try-with-resources for session management, no transaction needed for read
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                return entity != null ? convertToVampirePlayer(entity) : null;
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to get player " + uuid, e);
                throw new RuntimeException("Failed to get player", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> savePlayer(VampirePlayer player) {
        return executeTransactionVoid(session -> {
            VampirePlayerEntity entity = convertToEntity(player);
            // Use merge for both insert and update
            session.merge(entity);
        });
    }

    @Override
    public CompletableFuture<Void> deletePlayer(UUID uuid) {
        return executeTransactionVoid(session -> {
            VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
            if (entity != null) {
                session.remove(entity);
            }
        });
    }

    // Read-only methods don't need the full transaction helper unless complex query
    @Override
    public CompletableFuture<Boolean> isVampire(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                // Use getReference for potential performance gain if only checking existence/simple field
                // VampirePlayerEntity entity = session.getReference(VampirePlayerEntity.class, uuid);
                // However, get is safer if the entity might not exist
                VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                return entity != null && entity.isVampire();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to check vampire status for " + uuid, e);
                throw new RuntimeException("Failed to check vampire status", e);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> isInfected(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                // Check infection level > 0
                return entity != null && entity.getInfectionLevel() > 0.0;
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to check infection status for " + uuid, e);
                throw new RuntimeException("Failed to check infection status", e);
            }
        });
    }

    @Override
    public CompletableFuture<Double> getBloodLevel(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                return entity != null ? entity.getBloodLevel() : 0.0;
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to get blood level for " + uuid, e);
                throw new RuntimeException("Failed to get blood level", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> setBloodLevel(UUID uuid, double blood) {
        return executeTransactionVoid(session -> {
            VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
            if (entity != null) {
                entity.setBloodLevel(blood);
                session.merge(entity); // Persist changes
            } else {
                // Log a warning or debug message if player not found?
                plugin.debug("Attempted to set blood level for non-existent player: " + uuid);
            }
        });
    }

    @Override
    public CompletableFuture<Double> getInfectionLevel(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                return entity != null ? entity.getInfectionLevel() : 0.0;
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to get infection level for " + uuid, e);
                throw new RuntimeException("Failed to get infection level", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> setInfectionLevel(UUID uuid, double infection) {
        return executeTransactionVoid(session -> {
            VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
            if (entity != null) {
                entity.setInfectionLevel(infection);
                session.merge(entity);
            } else {
                plugin.debug("Attempted to set infection level for non-existent player: " + uuid);
            }
        });
    }

    @Override
    public CompletableFuture<String> getInfectionReason(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                return entity != null ? entity.getInfectionReason() : null;
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to get infection reason for " + uuid, e);
                throw new RuntimeException("Failed to get infection reason", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> setInfectionReason(UUID uuid, String reason) {
        return executeTransactionVoid(session -> {
            VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
            if (entity != null) {
                entity.setInfectionReason(reason);
                session.merge(entity);
            } else {
                plugin.debug("Attempted to set infection reason for non-existent player: " + uuid);
            }
        });
    }

    @Override
    public CompletableFuture<Long> getInfectionTime(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                return entity != null ? entity.getInfectionTime() : 0L;
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to get infection time for " + uuid, e);
                throw new RuntimeException("Failed to get infection time", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> setInfectionTime(UUID uuid, long time) {
        return executeTransactionVoid(session -> {
            VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
            if (entity != null) {
                entity.setInfectionTime(time);
                session.merge(entity);
            } else {
                plugin.debug("Attempted to set infection time for non-existent player: " + uuid);
            }
        });
    }

    // --- Getters for specific fields (Example: Last Shriek Time) ---
    // These follow the same pattern as getBloodLevel, getInfectionLevel etc.

    @Override
    public CompletableFuture<Long> getLastShriekTime(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                return entity != null ? entity.getLastShriekTime() : 0L;
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to get last shriek time for " + uuid, e);
                throw new RuntimeException("Failed to get last shriek time", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> setLastShriekTime(UUID uuid, long time) {
        return executeTransactionVoid(session -> {
            VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
            if (entity != null) {
                entity.setLastShriekTime(time);
                session.merge(entity);
            } else {
                plugin.debug("Attempted to set last shriek time for non-existent player: " + uuid);
            }
        });
    }

    @Override
    public CompletableFuture<Long> getLastBloodTradeTime(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                return entity != null ? entity.getLastBloodTradeTime() : 0L;
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to get last blood trade time for " + uuid, e);
                throw new RuntimeException("Failed to get last blood trade time", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> setLastBloodTradeTime(UUID uuid, long time) {
        return executeTransactionVoid(session -> {
            VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
            if (entity != null) {
                entity.setLastBloodTradeTime(time);
                session.merge(entity);
            } else {
                plugin.debug("Attempted to set last blood trade time for non-existent player: " + uuid);
            }
        });
    }

    @Override
    public CompletableFuture<UUID> getLastBloodTradePartner(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                return entity != null ? entity.getLastBloodTradePartner() : null;
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to get last blood trade partner for " + uuid, e);
                throw new RuntimeException("Failed to get last blood trade partner", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> setLastBloodTradePartner(UUID uuid, UUID partner) {
        return executeTransactionVoid(session -> {
            VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
            if (entity != null) {
                entity.setLastBloodTradePartner(partner);
                session.merge(entity);
            } else {
                plugin.debug("Attempted to set last blood trade partner for non-existent player: " + uuid);
            }
        });
    }

    @Override
    public CompletableFuture<Double> getLastBloodTradeAmount(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                return entity != null ? entity.getLastBloodTradeAmount() : 0.0;
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to get last blood trade amount for " + uuid, e);
                throw new RuntimeException("Failed to get last blood trade amount", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> setLastBloodTradeAmount(UUID uuid, double amount) {
        return executeTransactionVoid(session -> {
            VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
            if (entity != null) {
                entity.setLastBloodTradeAmount(amount);
                session.merge(entity);
            } else {
                plugin.debug("Attempted to set last blood trade amount for non-existent player: " + uuid);
            }
        });
    }

    @Override
    public CompletableFuture<String> getLastBloodTradeType(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                return entity != null ? entity.getLastBloodTradeType() : null;
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to get last blood trade type for " + uuid, e);
                throw new RuntimeException("Failed to get last blood trade type", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> setLastBloodTradeType(UUID uuid, String type) {
        return executeTransactionVoid(session -> {
            VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
            if (entity != null) {
                entity.setLastBloodTradeType(type);
                session.merge(entity);
            } else {
                plugin.debug("Attempted to set last blood trade type for non-existent player: " + uuid);
            }
        });
    }

    // --- Blood Offer Methods ---

    @Override
    public CompletableFuture<BloodOffer> createBloodOffer(UUID senderUuid, UUID targetUuid, double amount) {
        return executeTransaction(session -> {
            BloodOfferEntity entity = new BloodOfferEntity(senderUuid, targetUuid, amount);
            // Persist ensures it gets an ID if using auto-increment
            session.persist(entity);
            return convertToBloodOffer(entity);
        });
    }

    @Override
    public CompletableFuture<BloodOffer> getBloodOffer(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                // Query for active offers targeting the player
                Query<BloodOfferEntity> query = session.createQuery(
                    "FROM BloodOfferEntity WHERE targetUuid = :uuid AND accepted = false AND rejected = false",
                    BloodOfferEntity.class);
                query.setParameter("uuid", playerUuid);
                query.setMaxResults(1); // Only expect one active offer per target
                BloodOfferEntity entity = query.uniqueResult(); // Use uniqueResult
                return entity != null ? convertToBloodOffer(entity) : null;
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to get blood offer for " + playerUuid, e);
                throw new RuntimeException("Failed to get blood offer", e);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> acceptBloodOffer(UUID playerUuid) {
        return executeTransaction(session -> {
            // Find the active offer for the player
            Query<BloodOfferEntity> query = session.createQuery(
                "FROM BloodOfferEntity WHERE targetUuid = :uuid AND accepted = false AND rejected = false",
                BloodOfferEntity.class);
            query.setParameter("uuid", playerUuid);
            query.setMaxResults(1);
            BloodOfferEntity entity = query.uniqueResult();

            if (entity != null) {
                entity.setAccepted(true);
                session.merge(entity); // Update the entity
                return true; // Offer found and accepted
            }
            return false; // No active offer found
        });
    }

    @Override
    public CompletableFuture<Boolean> rejectBloodOffer(UUID playerUuid) {
        return executeTransaction(session -> {
            // Find the active offer for the player
            Query<BloodOfferEntity> query = session.createQuery(
                "FROM BloodOfferEntity WHERE targetUuid = :uuid AND accepted = false AND rejected = false", // Ensure it's not already accepted
                BloodOfferEntity.class);
            query.setParameter("uuid", playerUuid);
            query.setMaxResults(1);
            BloodOfferEntity entity = query.uniqueResult();

            if (entity != null) {
                entity.setRejected(true);
                session.merge(entity);
                return true; // Offer found and rejected
            }
            return false; // No active offer found
        });
    }

    @Override
    public CompletableFuture<List<BloodOffer>> getAllBloodOffers() {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                Query<BloodOfferEntity> query = session.createQuery(
                    "FROM BloodOfferEntity WHERE accepted = false AND rejected = false", // Only active offers
                    BloodOfferEntity.class);
                List<BloodOfferEntity> entities = query.list();
                // Convert list of entities to list of domain objects
                return entities.stream()
                    .map(this::convertToBloodOffer) // Use the existing conversion method
                    .collect(Collectors.toList());
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to get all blood offers", e);
                throw new RuntimeException("Failed to get all blood offers", e);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> cleanupExpiredOffers(long timeoutMillis) {
        return executeTransaction(session -> {
            long cutoff = System.currentTimeMillis() - timeoutMillis;
            // Use HQL for delete query for efficiency
            MutationQuery deleteQuery = session.createMutationQuery(
                "DELETE FROM BloodOfferEntity WHERE timestamp < :cutoff AND accepted = false AND rejected = false");
            deleteQuery.setParameter("cutoff", cutoff);
            int deletedCount = deleteQuery.executeUpdate();
            plugin.debug("Cleaned up " + deletedCount + " expired blood offers.");
            return true; // Indicate success (even if 0 deleted)
        });
    }

    // --- Other Methods ---

    @Override
    public CompletableFuture<Boolean> addInfection(UUID uuid, double amount, String reason) {
        return executeTransaction(session -> {
            // Retrieve the entity first
            VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);

            if (entity != null) {
                // Check if already vampire, don't apply infection
                if (entity.isVampire()) {
                    plugin.debug("Attempted to infect player who is already a vampire: " + uuid);
                    return false; // Indicate infection was not added
                }

                double currentInfection = entity.getInfectionLevel();
                // Clamp new infection level between 0 and 1
                double newInfection = Math.max(0.0, Math.min(1.0, currentInfection + amount));
                entity.setInfectionLevel(newInfection);
                entity.setInfectionReason(reason);
                // Only update time if infection actually increased?
                if (newInfection > currentInfection) {
                    entity.setInfectionTime(System.currentTimeMillis());
                }
                session.merge(entity);
                return true; // Infection added/updated
            }
            return false; // Player not found
        });
    }

    @Override
    public CompletableFuture<Boolean> setVampire(UUID uuid, boolean isVampire) {
        return executeTransaction(session -> {
            VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);

            if (entity != null) {
                boolean changed = entity.isVampire() != isVampire;
                entity.setVampire(isVampire);
                // If turning into a vampire, clear infection and set time
                if (isVampire) {
                    entity.setInfectionLevel(0.0);
                    entity.setInfectionReason(null); // Clear reason
                    entity.setInfectionTime(System.currentTimeMillis()); // Time of becoming vampire
                    // Set maker ID if applicable (might need another parameter or logic)
                } else {
                    // Optionally handle turning human (clear maker? reset level?)
                    // entity.setMakerId(null);
                    // entity.setVampireLevel(0);
                }
                session.merge(entity);
                return changed; // Return true if status was actually changed
            } else if (isVampire) {
                // Handle case where trying to set non-existent player as vampire
                // Maybe create a new record?
                plugin.getLogger().warning("Attempted to set non-existent player as vampire: " + uuid + ". Record not created.");
                return false;
            }
            return false; // Player not found and not setting to vampire
        });
    }

    @Override
    public CompletableFuture<List<VampirePlayer>> getAllVampires() {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                Query<VampirePlayerEntity> query = session.createQuery(
                    "FROM VampirePlayerEntity WHERE isVampire = true",
                    VampirePlayerEntity.class);
                List<VampirePlayerEntity> entities = query.list();
                return entities.stream()
                    .map(this::convertToVampirePlayer)
                    .collect(Collectors.toList());
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to get all vampires", e);
                throw new RuntimeException("Failed to get all vampires", e);
            }
        });
    }

    // --- Config/Language Methods (Marked as not implemented) ---
    // These methods interact with configuration files, not the primary database entities.
    // They shouldn't be part of the DatabaseManager interface if config is file-based.
    // If you needed DB-backed config, you'd implement them with a separate ConfigEntity.

    @Override
    public CompletableFuture<Boolean> saveConfig(String key, String value) {
        plugin.getLogger().warning("saveConfig(key, value) called on HibernateDatabaseManager - This method is not implemented for file-based config.");
        return CompletableFuture.completedFuture(false); // Indicate not supported
    }

    @Override
    public CompletableFuture<String> getConfig(String key) {
        plugin.getLogger().warning("getConfig(key) called on HibernateDatabaseManager - This method is not implemented for file-based config.");
        return CompletableFuture.completedFuture(null); // Indicate not supported
    }

    @Override
    public CompletableFuture<Boolean> saveLanguage(String key, String value) {
        plugin.getLogger().warning("saveLanguage(key, value) called on HibernateDatabaseManager - This method is not implemented for file-based config.");
        return CompletableFuture.completedFuture(false); // Indicate not supported
    }

    @Override
    public CompletableFuture<String> getLanguage(String key) {
        plugin.getLogger().warning("getLanguage(key) called on HibernateDatabaseManager - This method is not implemented for file-based config.");
        return CompletableFuture.completedFuture(null); // Indicate not supported
    }

    @Override
    public CompletableFuture<Boolean> useBlood(UUID uuid, double amount) {
        return executeTransaction(session -> {
            VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
            if (entity != null && entity.getBloodLevel() >= amount) {
                entity.setBloodLevel(entity.getBloodLevel() - amount);
                session.merge(entity);
                return true; // Blood used successfully
            }
            // Return false if player not found or insufficient blood
            return false;
        });
    }

    @Override
    public CompletableFuture<VampireConfig> loadConfig() {
        plugin.getLogger().warning("loadConfig() called on HibernateDatabaseManager - This method is not implemented for file-based config.");
        return CompletableFuture.completedFuture(null); // Indicate not supported
    }

    @Override
    public CompletableFuture<Boolean> saveConfig(VampireConfig config) {
        plugin.getLogger().warning("saveConfig(VampireConfig) called on HibernateDatabaseManager - This method is not implemented for file-based config.");
        return CompletableFuture.completedFuture(false); // Indicate not supported
    }

    @Override
    public CompletableFuture<LanguageConfig> loadLanguage() {
        plugin.getLogger().warning("loadLanguage() called on HibernateDatabaseManager - This method is not implemented for file-based config.");
        return CompletableFuture.completedFuture(null); // Indicate not supported
    }

    @Override
    public CompletableFuture<Boolean> saveLanguage(LanguageConfig language) {
        plugin.getLogger().warning("saveLanguage(LanguageConfig) called on HibernateDatabaseManager - This method is not implemented for file-based config.");
        return CompletableFuture.completedFuture(false); // Indicate not supported
    }

    // --- Helper Conversion Methods ---
    private VampirePlayer convertToVampirePlayer(VampirePlayerEntity entity) {
        if (entity == null) return null;
        VampirePlayer player = new VampirePlayer(entity.getUuid(), entity.getName());
        // Use internal setters for loading state directly from DB entity
        player.setVampireInternal(entity.isVampire());
        player.setBloodInternal(entity.getBloodLevel());
        player.setInfectionLevelInternal(entity.getInfectionLevel());
        player.setInfectionReason(entity.getInfectionReason());
        player.setInfectionTime(entity.getInfectionTime());
        player.setLastShriekTime(entity.getLastShriekTime());
        player.setLastBloodTradeTime(entity.getLastBloodTradeTime());
        player.setLastBloodTradePartner(entity.getLastBloodTradePartner());
        player.setLastBloodTradeAmount(entity.getLastBloodTradeAmount());
        player.setLastBloodTradeType(entity.getLastBloodTradeType());
        player.setVampireLevel(entity.getVampireLevel());
        player.setMakerId(entity.getMakerId());
        // Make sure to load ALL relevant fields from the entity
        // Example: player.setSomeOtherField(entity.getSomeOtherField());
        return player;
    }

    private VampirePlayerEntity convertToEntity(VampirePlayer player) {
        if (player == null) return null;
        // Consider fetching existing entity first if updates are common to avoid detaching
        VampirePlayerEntity entity = new VampirePlayerEntity(player.getUuid(), player.getName());
        entity.setVampire(player.isVampire());
        entity.setBloodLevel(player.getBlood());
        entity.setInfectionLevel(player.getInfectionLevel());
        entity.setInfectionReason(player.getInfectionReason());
        entity.setInfectionTime(player.getInfectionTime());
        entity.setLastShriekTime(player.getLastShriekTime());
        entity.setLastBloodTradeTime(player.getLastBloodTradeTime());
        entity.setLastBloodTradePartner(player.getLastBloodTradePartner());
        entity.setLastBloodTradeAmount(player.getLastBloodTradeAmount());
        entity.setLastBloodTradeType(player.getLastBloodTradeType());
        entity.setVampireLevel(player.getVampireLevel());
        entity.setMakerId(player.getMakerId());
        // Make sure to save ALL relevant fields to the entity
        // Example: entity.setSomeOtherField(player.getSomeOtherField());
        return entity;
    }

    private BloodOffer convertToBloodOffer(BloodOfferEntity entity) {
        if (entity == null) return null;
        // Fix: Use the correct constructor (without timestamp, as it's set internally)
        // Ensure the timestamp is being transferred correctly if needed elsewhere, but constructor takes 3 args
        BloodOffer offer = new BloodOffer(entity.getSenderUuid(), entity.getTargetUuid(), entity.getAmount());
        // We might need to manually set the timestamp in the domain object if it's read from the DB entity
        // offer.setTimestamp(entity.getTimestamp()); // Add a setter if needed
        if (entity.isAccepted()) {
            offer.setAccepted();
        }
        if (entity.isRejected()) {
            offer.setRejected();
        }
        // Assign the database ID if needed in the domain object
        // offer.setId(entity.getId()); // If BloodOffer has an ID field
        return offer;
    }
} 