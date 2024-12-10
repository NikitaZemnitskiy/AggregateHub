package com.zemnitskiy.aggregatehub.repository;

import com.zemnitskiy.aggregatehub.config.DatabaseListConfig;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.Map;

public interface DatabaseStrategy {
    DataSource createDataSource(DatabaseListConfig.DatabaseConfig config);
    LocalContainerEntityManagerFactoryBean createEntityManagerFactory(DataSource dataSource, String persistenceUnitName, Map<String, String> properties, String tableName);
}
