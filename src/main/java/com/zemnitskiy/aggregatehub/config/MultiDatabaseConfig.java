package com.zemnitskiy.aggregatehub.config;

import com.zemnitskiy.aggregatehub.config.DatabaseListConfig.DatabaseConfig;
import com.zemnitskiy.aggregatehub.repository.DatabaseStrategy;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for managing multiple databases with specific strategies.
 * <p>
 * This class initializes DataSource and EntityManagerFactory instances for each database
 * defined in {@link DatabaseListConfig}. It uses the provided {@link DatabaseStrategy} implementations
 * to configure each database according to its specified strategy.
 * </p>
 */
@Configuration
public class MultiDatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(MultiDatabaseConfig.class);

    private final DatabaseListConfig databaseListConfig;
    private final Map<String, DatabaseStrategy> databaseStrategies;
    private final Map<String, DataSource> dataSourceMap = new HashMap<>();
    private final Map<String, EntityManagerFactory> entityManagerFactoryMap = new HashMap<>();

    /**
     * Constructor for MultiDatabaseConfig.
     *
     * @param databaseListConfig Configuration for the list of databases.
     * @param databaseStrategies Map of available database strategies, keyed by strategy type.
     */
    public MultiDatabaseConfig(DatabaseListConfig databaseListConfig, Map<String, DatabaseStrategy> databaseStrategies) {
        this.databaseListConfig = databaseListConfig;
        this.databaseStrategies = databaseStrategies;
    }

    /**
     * Initializes database configurations by creating DataSource and EntityManagerFactory instances
     * for each defined database.
     */
    @PostConstruct
    public void init() {
        logger.info("Initializing MultiDatabaseConfig with available strategies: {}", databaseStrategies.keySet());

        for (DatabaseConfig dbConfig : databaseListConfig.getDataSources()) {
            System.out.println(databaseListConfig);
            String type = dbConfig.getStrategy().toLowerCase();
            DatabaseStrategy strategy = databaseStrategies.get(type);

            if (strategy == null) {
                logger.error("No strategy found for database type: {}", type);
                throw new IllegalArgumentException("No strategy found for database type: " + type);
            }

            logger.info("Configuring database: {} of type: {}", dbConfig.getName(), type);

            // Create and store DataSource
            DataSource dataSource = strategy.createDataSource(dbConfig);
            dataSourceMap.put(dbConfig.getName(), dataSource);
            logger.info("DataSource created for {}", dbConfig.getName());

            // Prepare entity mapping
            Map<String, String> mapping = dbConfig.getMapping() != null ? new HashMap<>(dbConfig.getMapping()) : new HashMap<>();
            mapping.put("table", dbConfig.getTable() != null && !dbConfig.getTable().isEmpty() ? dbConfig.getTable() : "users");

            // Create and store EntityManagerFactory
            LocalContainerEntityManagerFactoryBean emfBean = strategy.createEntityManagerFactory(
                    dataSource, dbConfig.getName() + "PU", mapping, mapping.get("table"));
            emfBean.afterPropertiesSet();

            EntityManagerFactory emf = emfBean.getObject();
            entityManagerFactoryMap.put(dbConfig.getName(), emf);
            logger.info("Configured EntityManagerFactory for: {}", dbConfig.getName());
        }
    }

    /**
     * Provides an unmodifiable map of configured DataSources.
     *
     * @return Map of DataSource instances keyed by database name.
     */
    @Bean
    public Map<String, DataSource> dataSources() {
        return Collections.unmodifiableMap(dataSourceMap);
    }

    /**
     * Provides an unmodifiable map of configured EntityManagerFactories.
     *
     * @return Map of EntityManagerFactory instances keyed by database name.
     */
    @Bean
    public Map<String, EntityManagerFactory> entityManagerFactories() {
        return Collections.unmodifiableMap(entityManagerFactoryMap);
    }
}