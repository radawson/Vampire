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

import java.util.UUID;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class HibernateDatabaseManager implements DatabaseManager {
    private final VampirePlugin plugin;

    public HibernateDatabaseManager(VampirePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Void> initialize() {
        return CompletableFuture.runAsync(() -> {
            String dbType = plugin.getConfig().getString("database.type", "sqlite");
            String dbUrl = plugin.getConfig().getString("database.url", "jdbc:sqlite:plugins/Vampire/database.db");
            String dbUser = plugin.getConfig().getString("database.user", "");
            String dbPassword = plugin.getConfig().getString("database.password", "");
            
            HibernateConfig.initialize(dbType, dbUrl, dbUser, dbPassword);
        });
    }

    @Override
    public CompletableFuture<Void> shutdown() {
        return CompletableFuture.runAsync(HibernateConfig::shutdown);
    }

    @Override
    public CompletableFuture<VampirePlayer> getPlayer(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                return entity != null ? convertToVampirePlayer(entity) : null;
            }
        });
    }

    @Override
    public CompletableFuture<Void> savePlayer(VampirePlayer player) {
        return CompletableFuture.runAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                try {
                    VampirePlayerEntity entity = convertToEntity(player);
                    session.merge(entity);
                    tx.commit();
                } catch (Exception e) {
                    tx.rollback();
                    throw e;
                }
            }
        });
    }

    @Override
    public CompletableFuture<Void> deletePlayer(UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                try {
                    VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                    if (entity != null) {
                        session.remove(entity);
                    }
                    tx.commit();
                } catch (Exception e) {
                    tx.rollback();
                    throw e;
                }
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> isVampire(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                return entity != null && entity.isVampire();
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> isInfected(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                return entity != null && entity.getInfectionLevel() > 0;
            }
        });
    }

    @Override
    public CompletableFuture<Double> getBloodLevel(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                return entity != null ? entity.getBloodLevel() : 0.0;
            }
        });
    }

    @Override
    public CompletableFuture<Void> setBloodLevel(UUID uuid, double blood) {
        return CompletableFuture.runAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                try {
                    VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                    if (entity != null) {
                        entity.setBloodLevel(blood);
                        session.merge(entity);
                    }
                    tx.commit();
                } catch (Exception e) {
                    tx.rollback();
                    throw e;
                }
            }
        });
    }

    @Override
    public CompletableFuture<Double> getInfectionLevel(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                return entity != null ? entity.getInfectionLevel() : 0.0;
            }
        });
    }

    @Override
    public CompletableFuture<Void> setInfectionLevel(UUID uuid, double infection) {
        return CompletableFuture.runAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                try {
                    VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                    if (entity != null) {
                        entity.setInfectionLevel(infection);
                        session.merge(entity);
                    }
                    tx.commit();
                } catch (Exception e) {
                    tx.rollback();
                    throw e;
                }
            }
        });
    }

    @Override
    public CompletableFuture<String> getInfectionReason(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                return entity != null ? entity.getInfectionReason() : null;
            }
        });
    }

    @Override
    public CompletableFuture<Void> setInfectionReason(UUID uuid, String reason) {
        return CompletableFuture.runAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                try {
                    VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                    if (entity != null) {
                        entity.setInfectionReason(reason);
                        session.merge(entity);
                    }
                    tx.commit();
                } catch (Exception e) {
                    tx.rollback();
                    throw e;
                }
            }
        });
    }

    @Override
    public CompletableFuture<Long> getInfectionTime(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                return entity != null ? entity.getInfectionTime() : 0L;
            }
        });
    }

    @Override
    public CompletableFuture<Void> setInfectionTime(UUID uuid, long time) {
        return CompletableFuture.runAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                try {
                    VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                    if (entity != null) {
                        entity.setInfectionTime(time);
                        session.merge(entity);
                    }
                    tx.commit();
                } catch (Exception e) {
                    tx.rollback();
                    throw e;
                }
            }
        });
    }

    @Override
    public CompletableFuture<Long> getLastShriekTime(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                return entity != null ? entity.getLastShriekTime() : 0L;
            }
        });
    }

    @Override
    public CompletableFuture<Void> setLastShriekTime(UUID uuid, long time) {
        return CompletableFuture.runAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                try {
                    VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                    if (entity != null) {
                        entity.setLastShriekTime(time);
                        session.merge(entity);
                    }
                    tx.commit();
                } catch (Exception e) {
                    tx.rollback();
                    throw e;
                }
            }
        });
    }

    @Override
    public CompletableFuture<Long> getLastBloodTradeTime(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                return entity != null ? entity.getLastBloodTradeTime() : 0L;
            }
        });
    }

    @Override
    public CompletableFuture<Void> setLastBloodTradeTime(UUID uuid, long time) {
        return CompletableFuture.runAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                try {
                    VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                    if (entity != null) {
                        entity.setLastBloodTradeTime(time);
                        session.merge(entity);
                    }
                    tx.commit();
                } catch (Exception e) {
                    tx.rollback();
                    throw e;
                }
            }
        });
    }

    @Override
    public CompletableFuture<UUID> getLastBloodTradePartner(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                return entity != null ? entity.getLastBloodTradePartner() : null;
            }
        });
    }

    @Override
    public CompletableFuture<Void> setLastBloodTradePartner(UUID uuid, UUID partner) {
        return CompletableFuture.runAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                try {
                    VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                    if (entity != null) {
                        entity.setLastBloodTradePartner(partner);
                        session.merge(entity);
                    }
                    tx.commit();
                } catch (Exception e) {
                    tx.rollback();
                    throw e;
                }
            }
        });
    }

    @Override
    public CompletableFuture<Double> getLastBloodTradeAmount(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                return entity != null ? entity.getLastBloodTradeAmount() : 0.0;
            }
        });
    }

    @Override
    public CompletableFuture<Void> setLastBloodTradeAmount(UUID uuid, double amount) {
        return CompletableFuture.runAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                try {
                    VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                    if (entity != null) {
                        entity.setLastBloodTradeAmount(amount);
                        session.merge(entity);
                    }
                    tx.commit();
                } catch (Exception e) {
                    tx.rollback();
                    throw e;
                }
            }
        });
    }

    @Override
    public CompletableFuture<String> getLastBloodTradeType(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                return entity != null ? entity.getLastBloodTradeType() : null;
            }
        });
    }

    @Override
    public CompletableFuture<Void> setLastBloodTradeType(UUID uuid, String type) {
        return CompletableFuture.runAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                try {
                    VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                    if (entity != null) {
                        entity.setLastBloodTradeType(type);
                        session.merge(entity);
                    }
                    tx.commit();
                } catch (Exception e) {
                    tx.rollback();
                    throw e;
                }
            }
        });
    }

    @Override
    public CompletableFuture<BloodOffer> createBloodOffer(UUID senderUuid, UUID targetUuid, double amount) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                try {
                    BloodOfferEntity entity = new BloodOfferEntity(senderUuid, targetUuid, amount);
                    session.persist(entity);
                    tx.commit();
                    return convertToBloodOffer(entity);
                } catch (Exception e) {
                    tx.rollback();
                    throw e;
                }
            }
        });
    }

    @Override
    public CompletableFuture<BloodOffer> getBloodOffer(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                Query<BloodOfferEntity> query = session.createQuery(
                    "FROM BloodOfferEntity WHERE targetUuid = :uuid AND accepted = false AND rejected = false",
                    BloodOfferEntity.class);
                query.setParameter("uuid", playerUuid);
                BloodOfferEntity entity = query.uniqueResult();
                return entity != null ? convertToBloodOffer(entity) : null;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> acceptBloodOffer(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                try {
                    Query<BloodOfferEntity> query = session.createQuery(
                        "FROM BloodOfferEntity WHERE targetUuid = :uuid AND accepted = false AND rejected = false",
                        BloodOfferEntity.class);
                    query.setParameter("uuid", playerUuid);
                    BloodOfferEntity entity = query.uniqueResult();
                    if (entity != null) {
                        entity.setAccepted(true);
                        session.merge(entity);
                        tx.commit();
                        return true;
                    }
                    tx.commit();
                    return false;
                } catch (Exception e) {
                    tx.rollback();
                    throw e;
                }
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> rejectBloodOffer(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                try {
                    Query<BloodOfferEntity> query = session.createQuery(
                        "FROM BloodOfferEntity WHERE targetUuid = :uuid AND rejected = false",
                        BloodOfferEntity.class);
                    query.setParameter("uuid", playerUuid);
                    BloodOfferEntity entity = query.uniqueResult();
                    
                    if (entity != null) {
                        entity.setRejected(true);
                        session.merge(entity);
                        tx.commit();
                        return true;
                    }
                    tx.commit();
                    return false;
                } catch (Exception e) {
                    tx.rollback();
                    throw e;
                }
            }
        });
    }

    @Override
    public CompletableFuture<List<BloodOffer>> getAllBloodOffers() {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                Query<BloodOfferEntity> query = session.createQuery(
                    "FROM BloodOfferEntity WHERE accepted = false AND rejected = false",
                    BloodOfferEntity.class);
                List<BloodOfferEntity> entities = query.list();
                return entities.stream()
                    .map(this::convertToBloodOffer)
                    .collect(Collectors.toList());
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> cleanupExpiredOffers(long timeoutMillis) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                try {
                    long cutoff = System.currentTimeMillis() - timeoutMillis;
                    Query<BloodOfferEntity> query = session.createQuery(
                        "FROM BloodOfferEntity WHERE timestamp < :cutoff",
                        BloodOfferEntity.class);
                    query.setParameter("cutoff", cutoff);
                    List<BloodOfferEntity> entities = query.list();
                    for (BloodOfferEntity entity : entities) {
                        session.remove(entity);
                    }
                    tx.commit();
                    return true;
                } catch (Exception e) {
                    tx.rollback();
                    throw e;
                }
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> addInfection(UUID uuid, double amount, String reason) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                try {
                    Query<VampirePlayerEntity> query = session.createQuery(
                        "FROM VampirePlayerEntity WHERE uuid = :uuid",
                        VampirePlayerEntity.class);
                    query.setParameter("uuid", uuid);
                    VampirePlayerEntity entity = query.uniqueResult();
                    
                    if (entity != null) {
                        double currentInfection = entity.getInfectionLevel();
                        entity.setInfectionLevel(currentInfection + amount);
                        entity.setInfectionReason(reason);
                        entity.setInfectionTime(System.currentTimeMillis());
                        session.merge(entity);
                        tx.commit();
                        return true;
                    }
                    tx.commit();
                    return false;
                } catch (Exception e) {
                    tx.rollback();
                    throw e;
                }
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> setVampire(UUID uuid, boolean isVampire) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                try {
                    Query<VampirePlayerEntity> query = session.createQuery(
                        "FROM VampirePlayerEntity WHERE uuid = :uuid",
                        VampirePlayerEntity.class);
                    query.setParameter("uuid", uuid);
                    VampirePlayerEntity entity = query.uniqueResult();
                    
                    if (entity != null) {
                        entity.setVampire(isVampire);
                        if (isVampire) {
                            entity.setInfectionTime(System.currentTimeMillis());
                        }
                        session.merge(entity);
                        tx.commit();
                        return true;
                    }
                    tx.commit();
                    return false;
                } catch (Exception e) {
                    tx.rollback();
                    throw e;
                }
            }
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
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> saveConfig(String key, String value) {
        // Not implemented as we're using file-based config
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<String> getConfig(String key) {
        // Not implemented as we're using file-based config
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Boolean> saveLanguage(String key, String value) {
        // Not implemented as we're using file-based config
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<String> getLanguage(String key) {
        // Not implemented as we're using file-based config
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Boolean> useBlood(UUID uuid, double amount) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                try {
                    VampirePlayerEntity entity = session.get(VampirePlayerEntity.class, uuid);
                    if (entity != null && entity.getBloodLevel() >= amount) {
                        entity.setBloodLevel(entity.getBloodLevel() - amount);
                        session.merge(entity);
                        tx.commit();
                        return true;
                    }
                    tx.commit();
                    return false;
                } catch (Exception e) {
                    tx.rollback();
                    throw e;
                }
            }
        });
    }

    @Override
    public CompletableFuture<VampireConfig> loadConfig() {
        // Not implemented as we're using file-based config
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Boolean> saveConfig(VampireConfig config) {
        // Not implemented as we're using file-based config
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<LanguageConfig> loadLanguage() {
        // Not implemented as we're using file-based config
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Boolean> saveLanguage(LanguageConfig language) {
        // Not implemented as we're using file-based config
        return CompletableFuture.completedFuture(null);
    }

    // Helper methods for converting between entities and domain objects
    private VampirePlayer convertToVampirePlayer(VampirePlayerEntity entity) {
        VampirePlayer player = new VampirePlayer(entity.getUuid(), entity.getName());
        player.setVampire(entity.isVampire());
        player.setBlood(entity.getBloodLevel());
        player.setInfectionLevel(entity.getInfectionLevel());
        player.setInfectionReason(entity.getInfectionReason());
        player.setInfectionTime(entity.getInfectionTime());
        player.setLastShriekTime(entity.getLastShriekTime());
        player.setLastBloodTradeTime(entity.getLastBloodTradeTime());
        player.setLastBloodTradePartner(entity.getLastBloodTradePartner());
        player.setLastBloodTradeAmount(entity.getLastBloodTradeAmount());
        player.setLastBloodTradeType(entity.getLastBloodTradeType());
        return player;
    }

    private VampirePlayerEntity convertToEntity(VampirePlayer player) {
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
        return entity;
    }

    private BloodOffer convertToBloodOffer(BloodOfferEntity entity) {
        BloodOffer offer = new BloodOffer(entity.getSenderUuid(), entity.getTargetUuid(), entity.getAmount());
        if (entity.isAccepted()) {
            offer.setAccepted();
        }
        if (entity.isRejected()) {
            offer.setRejected();
        }
        return offer;
    }
} 