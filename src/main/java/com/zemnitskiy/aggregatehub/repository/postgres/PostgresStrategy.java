package com.zemnitskiy.aggregatehub.repository.postgres;

import com.zemnitskiy.aggregatehub.config.DatabaseListConfig.DatabaseConfig;
import com.zemnitskiy.aggregatehub.naming.CustomPhysicalNamingStrategy;
import com.zemnitskiy.aggregatehub.repository.DatabaseStrategy;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

@Component("postgres")
public class PostgresStrategy implements DatabaseStrategy {

    @Override
    public DataSource createDataSource(DatabaseConfig config) {
        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url(config.getUrl())
                .username(config.getUser())
                .password(config.getPassword())
                .build();
    }

    @Override
    public LocalContainerEntityManagerFactoryBean createEntityManagerFactory(DataSource dataSource, String persistenceUnitName, Map<String, String> mapping, String tableName) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.zemnitskiy.aggregatehub.model");
        emf.setPersistenceUnitName(persistenceUnitName);

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(true);
        emf.setJpaVendorAdapter(vendorAdapter);

        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        jpaProperties.put("hibernate.hbm2ddl.auto", "update");
        jpaProperties.put("hibernate.show_sql", "true");
        jpaProperties.put("hibernate.format_sql", "true");

        String tableMappingString = tableName == null ? null : "users=" + tableName;
        String columnMappingString = mapping.toString();

        CustomPhysicalNamingStrategy namingStrategy = new CustomPhysicalNamingStrategy(tableMappingString, columnMappingString);
        jpaProperties.put("hibernate.physical_naming_strategy", namingStrategy);

        emf.setJpaProperties(jpaProperties);

        return emf;
    }
}