package org.clockworx.vampire.database;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * A Hibernate PhysicalNamingStrategy that adds a configured prefix to all table names.
 * Used to avoid naming collisions in shared databases.
 */
public class PrefixPhysicalNamingStrategy extends PhysicalNamingStrategyStandardImpl {

    private final String tablePrefix;

    /**
     * Creates a new naming strategy with the specified prefix.
     * @param tablePrefix The prefix to add to table names (e.g., "vampire_"). Can be empty.
     */
    public PrefixPhysicalNamingStrategy(String tablePrefix) {
        this.tablePrefix = (tablePrefix != null && !tablePrefix.isEmpty()) ? tablePrefix : "";
    }

    @Override
    public Identifier toPhysicalTableName(Identifier logicalName, JdbcEnvironment context) {
        if (logicalName == null) {
            return null;
        }
        // Apply prefix only if it's not empty
        String prefixedName = tablePrefix.isEmpty() ? logicalName.getText() : tablePrefix + logicalName.getText();
        // Return the identifier, respecting case sensitivity settings from standard strategy
        return super.toPhysicalTableName(Identifier.toIdentifier(prefixedName), context);
    }
} 