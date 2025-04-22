package org.clockworx.vampire.database;

import org.clockworx.vampire.VampirePlugin;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.clockworx.vampire.util.VampireMessages;

import java.util.Properties;
import java.util.logging.Level;

/**
 * Configures and initializes Hibernate ORM for database connectivity.
 * Handles both MySQL and SQLite connection setup with appropriate dialects and connection pooling.
 */
public class HibernateConfig {
    private static SessionFactory sessionFactory;

    /**
     * Initializes Hibernate with the specified database configuration.
     * 
     * @param plugin The VampirePlugin instance
     * @param dbType The database type ("mysql" or "sqlite")
     * @param dbUrl The JDBC URL for the database connection
     * @param dbUser The database username (for MySQL)
     * @param dbPassword The database password (for MySQL)
     */
    public static void initialize(VampirePlugin plugin, String dbType, String dbUrl, String dbUser, String dbPassword) {
        try {
            plugin.getLogger().log(Level.INFO, "HibernateConfig initializing with dbType: '" + dbType + "'");
            
            // Configure Hibernate
            Configuration configuration = new Configuration();
            Properties settings = new Properties();
            
            // Common settings
            settings.put(Environment.SHOW_SQL, "false"); // Set to false for production
            settings.put(Environment.HBM2DDL_AUTO, "update");
            settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
            
            // Apply Table Prefix
            String tablePrefix = plugin.getVampireConfig().getDatabaseTablePrefix();
            settings.put(Environment.PHYSICAL_NAMING_STRATEGY, new PrefixPhysicalNamingStrategy(tablePrefix));
            plugin.getLogger().log(Level.INFO, "Applied table prefix: '" + tablePrefix + "'");
            
            // Database-specific settings
            if ("mysql".equalsIgnoreCase(dbType)) {
                plugin.getLogger().log(Level.INFO, "Configuring Hibernate for MySQL...");
                settings.put(Environment.CONNECTION_PROVIDER, "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
                
                // Hikari Specific Properties (Primary)
                settings.put("hibernate.hikari.jdbcUrl", dbUrl);
                settings.put("hibernate.hikari.username", dbUser);
                settings.put("hibernate.hikari.password", dbPassword);
                settings.put("hibernate.hikari.maximumPoolSize", "10");
                settings.put("hibernate.hikari.minimumIdle", "5");
                settings.put("hibernate.hikari.idleTimeout", "300000"); // 5 minutes
                settings.put("hibernate.hikari.connectionTimeout", "10000"); // 10 seconds
                settings.put("hibernate.hikari.autoCommit", "true");
    
            } else if ("sqlite".equalsIgnoreCase(dbType)) {
                plugin.getLogger().log(Level.INFO, "Configuring Hibernate for SQLite...");
                settings.put(Environment.DIALECT, "org.hibernate.community.dialect.SQLiteDialect");
                settings.put("jakarta.persistence.jdbc.driver", "org.sqlite.JDBC");
                settings.put("jakarta.persistence.jdbc.url", dbUrl);
            } else {
                plugin.getLogger().log(Level.SEVERE, "Unsupported database type configured: '" + dbType + "'. Cannot initialize Hibernate.");
                throw new IllegalArgumentException("Unsupported database type: " + dbType);
            }
            
            configuration.setProperties(settings);
            
            // Add entity classes
            configuration.addAnnotatedClass(org.clockworx.vampire.entity.VampirePlayerEntity.class);
            configuration.addAnnotatedClass(org.clockworx.vampire.entity.BloodOfferEntity.class);
            
            // Build session factory
            sessionFactory = configuration.buildSessionFactory();
        } catch (Throwable ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize Hibernate: " + ex.getMessage(), ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Gets the initialized SessionFactory instance.
     * 
     * @return The SessionFactory instance
     * @throws IllegalStateException if initialize() has not been called
     */
    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            throw new IllegalStateException("Hibernate has not been initialized. Call initialize() first.");
        }
        return sessionFactory;
    }

    /**
     * Shuts down Hibernate by closing the SessionFactory if it exists.
     */
    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
            sessionFactory = null;
        }
    }
} 