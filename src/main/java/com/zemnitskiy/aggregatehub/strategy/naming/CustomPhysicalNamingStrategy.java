package com.zemnitskiy.aggregatehub.strategy.naming;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Custom implementation of {@link PhysicalNamingStrategyStandardImpl} that allows
 * custom mapping of table and column names based on predefined mappings.
 */
public class CustomPhysicalNamingStrategy extends PhysicalNamingStrategyStandardImpl {

    private static final Logger logger = LoggerFactory.getLogger(CustomPhysicalNamingStrategy.class);

    private final Map<String, String> tableMapping;
    private final Map<String, String> columnMapping;


    public CustomPhysicalNamingStrategy(Map<String, String> tableMapping, Map<String, String> columnMapping) {
        this.tableMapping = tableMapping;
        this.columnMapping = columnMapping;
    }

    /**
     * Maps the logical table name to its physical name using the provided table mappings.
     *
     * @param name    the logical name of the table
     * @param context the JDBC environment
     * @return the mapped physical table name, or the default name if no mapping is found
     */
    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        if (name == null) {
            return null;
        }

        String logicalName = name.getText();
        String mappedName = tableMapping.get(logicalName);
        if (mappedName != null) {
            logger.debug("Mapping table '{}' to '{}'", logicalName, mappedName);
            return Identifier.toIdentifier(mappedName);
        }

        return super.toPhysicalTableName(name, context);
    }

    /**
     * Maps the logical column name to its physical name using the provided column mappings.
     *
     * @param name    the logical name of the column
     * @param context the JDBC environment
     * @return the mapped physical column name, or the default name if no mapping is found
     */
    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
        if (name == null) {
            return null;
        }

        String logicalName = name.getText();
        String mappedName = columnMapping.get(logicalName);
        if (mappedName != null) {
            logger.debug("Mapping column '{}' to '{}'", logicalName, mappedName);
            return Identifier.toIdentifier(mappedName);
        }

        return super.toPhysicalColumnName(name, context);
    }
}