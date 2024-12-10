package com.zemnitskiy.aggregatehub.config;

import com.zemnitskiy.aggregatehub.config.DatabaseListConfig.DatabaseConfig;
import com.zemnitskiy.aggregatehub.repository.DatabaseStrategy;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MultiDatabaseConfig {

    private final DatabaseListConfig databaseListConfig;

    private final Map<String, DatabaseStrategy> databaseStrategies;

    private final Map<String, DataSource> dataSourceMap = new HashMap<>();
    private final Map<String, EntityManagerFactory> entityManagerFactoryMap = new HashMap<>();

    public MultiDatabaseConfig(DatabaseListConfig databaseListConfig, Map<String, DatabaseStrategy> databaseStrategies) {
        this.databaseListConfig = databaseListConfig;
        this.databaseStrategies = databaseStrategies;
    }

    @PostConstruct
    public void init() {
        System.out.println("Available Database Strategies: " + databaseStrategies.keySet());

        for (DatabaseConfig dbConfig : databaseListConfig.getDataSources()) {
            String type = dbConfig.getStrategy().toLowerCase();
            DatabaseStrategy strategy = databaseStrategies.get(type);
            if (strategy == null) {
                throw new IllegalArgumentException("No strategy found for database type: " + type);
            }

            System.out.println("Configuring database: " + dbConfig.getName() + " of type: " + type);

            DataSource dataSource = strategy.createDataSource(dbConfig);
            dataSourceMap.put(dbConfig.getName(), dataSource);
            System.out.println("DataSource created for " + dbConfig.getName());

            Map<String, String> mapping = dbConfig.getMapping();
            if (mapping == null) {
                mapping = new HashMap<>();
            }
            String tableName = dbConfig.getTable();

            if (tableName != null && !tableName.isEmpty()) {
                mapping.put("table", tableName);
            } else {
                mapping.put("table", "users");
            }

            LocalContainerEntityManagerFactoryBean emfBean = strategy.createEntityManagerFactory(dataSource, dbConfig.getName() + "PU", mapping, tableName);
            emfBean.setPackagesToScan("com.zemnitskiy.aggregatehub.model");
            emfBean.afterPropertiesSet();
            EntityManagerFactory emf = emfBean.getObject();
            entityManagerFactoryMap.put(dbConfig.getName(), emf);
            System.out.println("EntityManagerFactory created for " + dbConfig.getName());
            System.out.println("Configured EntityManagerFactory for: " + dbConfig.getName());
        }
    }

    @Bean
    public Map<String, DataSource> dataSources() {
        return Collections.unmodifiableMap(dataSourceMap);
    }

    @Bean
    public Map<String, EntityManagerFactory> entityManagerFactories() {
        return Collections.unmodifiableMap(entityManagerFactoryMap);
    }
}