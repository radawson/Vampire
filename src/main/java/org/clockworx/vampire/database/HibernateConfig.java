package org.clockworx.vampire.database;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

import java.util.Properties;

public class HibernateConfig {
    private static SessionFactory sessionFactory;

    public static void initialize(String dbType, String dbUrl, String dbUser, String dbPassword) {
        // Configure Hibernate
        Configuration configuration = new Configuration();
        Properties settings = new Properties();
        
        // Common settings
        settings.put(Environment.SHOW_SQL, "true");
        settings.put(Environment.HBM2DDL_AUTO, "update");
        settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
        settings.put(Environment.TRANSACTION_COORDINATOR_STRATEGY, "jdbc");
        settings.put(Environment.JDBC_TIME_ZONE, "UTC");
        
        // Database-specific settings
        if ("mysql".equalsIgnoreCase(dbType)) {
            settings.put(Environment.DIALECT, "org.hibernate.dialect.MySQLDialect");
            settings.put(Environment.CONNECTION_PROVIDER, "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
            settings.put("hibernate.hikari.jdbcUrl", dbUrl);
            settings.put("hibernate.hikari.username", dbUser);
            settings.put("hibernate.hikari.password", dbPassword);
            settings.put("hibernate.hikari.maximumPoolSize", "10");
            settings.put("hibernate.hikari.minimumIdle", "5");
            settings.put("hibernate.hikari.idleTimeout", "300000"); // 5 minutes
            settings.put("hibernate.hikari.connectionTimeout", "10000"); // 10 seconds
            settings.put("hibernate.hikari.autoCommit", "true");
        } else {
            settings.put(Environment.DIALECT, "org.sqlite.hibernate.dialect.SQLiteDialect");
            settings.put("javax.persistence.jdbc.driver", "org.sqlite.JDBC");
            settings.put("javax.persistence.jdbc.url", dbUrl);
        }
        
        configuration.setProperties(settings);
        
        // Add entity classes
        configuration.addAnnotatedClass(org.clockworx.vampire.entity.VampirePlayerEntity.class);
        configuration.addAnnotatedClass(org.clockworx.vampire.entity.BloodOfferEntity.class);
        
        // Build session factory
        sessionFactory = configuration.buildSessionFactory();
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            throw new IllegalStateException("Hibernate has not been initialized. Call initialize() first.");
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
            sessionFactory = null;
        }
    }
} 