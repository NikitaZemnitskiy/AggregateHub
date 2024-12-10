package com.zemnitskiy.aggregatehub.config;

import com.zemnitskiy.aggregatehub.repository.DatabaseStrategy;
import com.zemnitskiy.aggregatehub.config.DatabaseListConfig.DatabaseConfig;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.*;

@Configuration
public class MultiDatabaseConfig {

    @Autowired
    private DatabaseListConfig databaseListConfig;

    @Autowired
    private Map<String, DatabaseStrategy> databaseStrategies;

    private Map<String, DataSource> dataSourceMap = new HashMap<>();
    private Map<String, EntityManagerFactory> entityManagerFactoryMap = new HashMap<>();

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

            // Создаём DataSource
            DataSource dataSource = strategy.createDataSource(dbConfig);
            dataSourceMap.put(dbConfig.getName(), dataSource);
            System.out.println("DataSource created for " + dbConfig.getName());


            Map<String, String> mapping = dbConfig.getMapping();

            LocalContainerEntityManagerFactoryBean emfBean = strategy.createEntityManagerFactory(dataSource, dbConfig.getName() + "PU", mapping);
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