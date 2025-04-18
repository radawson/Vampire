package org.clockworx.vampire.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.util.Properties;

public class HibernateConfig {
    private static SessionFactory sessionFactory;
    private static DataSource dataSource;

    public static void initialize(String dbType, String dbUrl, String dbUser, String dbPassword) {
        // Create data source based on database type
        if ("mysql".equalsIgnoreCase(dbType)) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dbUrl);
            config.setUsername(dbUser);
            config.setPassword(dbPassword);
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(5);
            config.setIdleTimeout(300000); // 5 minutes
            config.setConnectionTimeout(10000); // 10 seconds
            config.setAutoCommit(true);
            dataSource = new HikariDataSource(config);
        } else {
            SQLiteDataSource sqliteDs = new SQLiteDataSource();
            sqliteDs.setUrl(dbUrl);
            dataSource = sqliteDs;
        }

        // Configure Hibernate
        Configuration configuration = new Configuration();
        Properties settings = new Properties();
        
        // Common settings
        settings.put(Environment.DATASOURCE, dataSource);
        settings.put(Environment.SHOW_SQL, "true");
        settings.put(Environment.HBM2DDL_AUTO, "update");
        settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
        settings.put(Environment.TRANSACTION_COORDINATOR_STRATEGY, "jdbc");
        settings.put(Environment.JDBC_TIME_ZONE, "UTC");
        
        // Database-specific settings
        if ("mysql".equalsIgnoreCase(dbType)) {
            settings.put(Environment.DIALECT, "org.hibernate.dialect.MySQLDialect");
            settings.put(Environment.CONNECTION_PROVIDER, "com.zaxxer.hikari.hibernate.HikariConnectionProvider");
        } else {
            settings.put(Environment.DIALECT, "org.sqlite.hibernate.dialect.SQLiteDialect");
            settings.put(Environment.CONNECTION_PROVIDER, "org.hibernate.connection.C3P0ConnectionProvider");
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

    public static DataSource getDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("Database has not been initialized. Call initialize() first.");
        }
        return dataSource;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
        }
    }
} 