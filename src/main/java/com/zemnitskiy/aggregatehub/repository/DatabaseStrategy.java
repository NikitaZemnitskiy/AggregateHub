package com.zemnitskiy.aggregatehub.repository;

import com.zemnitskiy.aggregatehub.config.DatabaseListConfig;
import com.zemnitskiy.aggregatehub.naming.CustomPhysicalNamingStrategy;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

public interface DatabaseStrategy {
    String HIBERNATE_DIALECT = "hibernate.dialect";
    String HIBERNATE_HBM2DDL_AUTO = "hibernate.hbm2ddl.auto";
    String HIBERNATE_SHOW_SQL = "hibernate.show_sql";
    String HIBERNATE_FORMAT_SQL = "hibernate.format_sql";

    String HBM2DDL_AUTO_UPDATE = "update";
    String SHOW_SQL_TRUE = "true";
    String FORMAT_SQL_TRUE = "true";

    DataSource createDataSource(DatabaseListConfig.DatabaseConfig config);
    default LocalContainerEntityManagerFactoryBean createEntityManagerFactory(DataSource dataSource, String persistenceUnitName, Map<String, String> mapping, String tableName) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.zemnitskiy.aggregatehub.model");
        emf.setPersistenceUnitName(persistenceUnitName);

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(true);
        emf.setJpaVendorAdapter(vendorAdapter);

        Properties jpaProperties = getJpaProperties();
        configureNamingStrategy(jpaProperties, mapping, tableName);
        emf.setJpaProperties(jpaProperties);

        return emf;
    }

    default void configureNamingStrategy(Properties jpaProperties, Map<String, String> mapping, String tableName) {
        String tableMappingString = tableName == null ? null : "users=" + tableName;
        String columnMappingString = mapping.toString();

        CustomPhysicalNamingStrategy namingStrategy = new CustomPhysicalNamingStrategy(tableMappingString, columnMappingString);
        jpaProperties.put("hibernate.physical_naming_strategy", namingStrategy);
    }

    Properties getJpaProperties();
}
