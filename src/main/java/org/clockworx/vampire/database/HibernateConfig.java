package org.clockworx.vampire.database;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.clockworx.vampire.VampirePlugin;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

/**
 * Configures and initializes Hibernate ORM for database connectivity.
 * Handles both MySQL and SQLite connection setup with appropriate dialects and connection pooling.
 * SessionFactory is initialized lazily on first request.
 */
public class HibernateConfig {
    private static volatile SessionFactory sessionFactory; // volatile for thread safety
    private static volatile boolean initialized = false; // volatile for thread safety
    private static final Object initLock = new Object(); // Lock object for synchronization

    /**
     * Initializes Hibernate with the specified database configuration.
     * This method is called internally and synchronized to ensure it only runs once.
     *
     * @param plugin The VampirePlugin instance
     */
    private static void initializeInternal(VampirePlugin plugin) {
        // Double-checked locking pattern to avoid unnecessary synchronization
        if (initialized) {
            return;
        }
        synchronized (initLock) {
            if (initialized) {
                return; // Check again inside synchronized block
            }

            try {
                plugin.getLogger().log(Level.INFO, "Initializing Hibernate SessionFactory...");

                // Get database configuration from VampireConfig
                String dbType = plugin.getVampireConfig().getDatabaseType();
                String dbUrl = plugin.getVampireConfig().getDatabaseUrl();
                String dbUser = plugin.getVampireConfig().getDatabaseUser();
                String dbPassword = plugin.getVampireConfig().getDatabasePassword();
                String tablePrefix = plugin.getVampireConfig().getDatabaseTablePrefix();

                // Configure Hibernate
                Configuration configuration = new Configuration();
                Properties settings = new Properties();

                // Common settings
                settings.put(Environment.SHOW_SQL, "false");
                settings.put(Environment.HBM2DDL_AUTO, "none");
                settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");

                // Apply Table Prefix
                settings.put(Environment.PHYSICAL_NAMING_STRATEGY, new PrefixPhysicalNamingStrategy(tablePrefix));
                plugin.getLogger().log(Level.INFO, "Applying Hibernate table prefix: '" + tablePrefix + "'");

                // Configure Hibernate Logging
                Level sqlLogLevel = plugin.getVampireConfig().isDebug() ? Level.FINE : Level.INFO;
                Logger.getLogger("org.hibernate.SQL").setLevel(sqlLogLevel);
                Logger.getLogger("org.hibernate.orm.jdbc.bind").setLevel(sqlLogLevel);
                plugin.getLogger().log(Level.INFO, "Redirected Hibernate SQL logging to logger at level: " + sqlLogLevel.getName());

                // Database-specific settings
                if ("mysql".equalsIgnoreCase(dbType)) {
                    plugin.getLogger().log(Level.INFO, "Configuring Hibernate for MySQL...");
                    settings.put(Environment.DIALECT, "org.hibernate.dialect.MySQLDialect");
                    settings.put(Environment.CONNECTION_PROVIDER, "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");

                    // Hikari Specific Properties
                    settings.put("hibernate.hikari.jdbcUrl", dbUrl);
                    settings.put("hibernate.hikari.username", dbUser);
                    settings.put("hibernate.hikari.password", dbPassword);
                    settings.put("hibernate.hikari.driverClassName", "com.mysql.cj.jdbc.Driver");
                    settings.put("hibernate.hikari.maximumPoolSize", "10");
                    settings.put("hibernate.hikari.minimumIdle", "5");
                    settings.put("hibernate.hikari.idleTimeout", "300000");
                    settings.put("hibernate.hikari.connectionTimeout", "10000");
                    settings.put("hibernate.hikari.autoCommit", "true");

                } else if ("sqlite".equalsIgnoreCase(dbType)) {
                    plugin.getLogger().log(Level.INFO, "Configuring Hibernate for SQLite...");
                    settings.put(Environment.DIALECT, "org.hibernate.community.dialect.SQLiteDialect");
                    settings.put("jakarta.persistence.jdbc.url", dbUrl);
                    settings.put("hibernate.connection.autocommit", "true");

                } else if ("postgres".equalsIgnoreCase(dbType) || "postgresql".equalsIgnoreCase(dbType)) {
                    plugin.getLogger().log(Level.INFO, "Configuring Hibernate for PostgreSQL...");
                    settings.put(Environment.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
                    settings.put(Environment.CONNECTION_PROVIDER, "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
                    
                    // Hikari Specific Properties for PostgreSQL
                    settings.put("hibernate.hikari.jdbcUrl", dbUrl);
                    settings.put("hibernate.hikari.username", dbUser);
                    settings.put("hibernate.hikari.password", dbPassword);
                    settings.put("hibernate.hikari.driverClassName", "org.postgresql.Driver");
                    settings.put("hibernate.hikari.maximumPoolSize", "10");
                    settings.put("hibernate.hikari.minimumIdle", "5");
                    settings.put("hibernate.hikari.idleTimeout", "300000");
                    settings.put("hibernate.hikari.connectionTimeout", "10000");
                    settings.put("hibernate.hikari.autoCommit", "true");

                } else {
                    plugin.getLogger().log(Level.SEVERE, "Unsupported database type configured for Hibernate: '" + dbType + "'");
                    throw new IllegalArgumentException("Unsupported database type: " + dbType);
                }

                configuration.setProperties(settings);

                // Add entity classes
                configuration.addAnnotatedClass(org.clockworx.vampire.entity.VampirePlayerEntity.class);
                configuration.addAnnotatedClass(org.clockworx.vampire.entity.BloodOfferEntity.class);

                // Build session factory
                sessionFactory = configuration.buildSessionFactory();
                initialized = true;
                plugin.getLogger().log(Level.INFO, "Hibernate SessionFactory built successfully.");

            } catch (Throwable ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to initialize Hibernate SessionFactory: " + ex.getMessage(), ex);
                throw new ExceptionInInitializerError(ex);
            }
        }
    }

    /**
     * Gets the initialized SessionFactory instance.
     * Initializes the SessionFactory on the first call if it hasn't been already.
     * Ensures thread-safe lazy initialization.
     *
     * @return The SessionFactory instance
     * @throws IllegalStateException if the plugin instance isn't available or initialization fails
     */
    public static SessionFactory getSessionFactory() {
        if (!initialized) {
            VampirePlugin pluginInstance = VampirePlugin.getPlugin(VampirePlugin.class);
            if (pluginInstance == null) {
                 throw new IllegalStateException("Cannot initialize Hibernate: VampirePlugin instance not available.");
            }
            initializeInternal(pluginInstance);
        }
        if (sessionFactory == null) {
             throw new IllegalStateException("Hibernate SessionFactory could not be initialized. Check previous logs for errors.");
         }
        return sessionFactory;
    }

    /**
     * Shuts down Hibernate by closing the SessionFactory if it exists and is open.
     * Resets the initialized state.
     */
    public static void shutdown() {
        synchronized (initLock) {
            if (sessionFactory != null && !sessionFactory.isClosed()) {
                VampirePlugin pluginInstance = VampirePlugin.getPlugin(VampirePlugin.class);
                if (pluginInstance != null) {
                     pluginInstance.getLogger().log(Level.INFO, "Shutting down Hibernate SessionFactory...");
                }
                try {
                    sessionFactory.close();
                } catch (Exception e) {
                     if (pluginInstance != null) {
                          pluginInstance.getLogger().log(Level.SEVERE, "Error closing Hibernate SessionFactory", e);
                     }
                }
            }
            sessionFactory = null;
            initialized = false;
        }
    }
} 