package com.zemnitskiy.aggregatehub.repository.mysql;

import com.zemnitskiy.aggregatehub.config.DatabaseListConfig.DatabaseConfig;
import com.zemnitskiy.aggregatehub.repository.DatabaseStrategy;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Properties;

@Component("mysql")
public class MySqlStrategy implements DatabaseStrategy {

    public static final String DIALECT = "org.hibernate.dialect.MySQL8Dialect";
    public static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    @Override
    public DataSource createDataSource(DatabaseConfig config) {
        return DataSourceBuilder.create()
                .driverClassName(DRIVER)
                .url(config.getUrl())
                .username(config.getUser())
                .password(config.getPassword())
                .build();
    }

    @Override
    public Properties getJpaProperties() {
        Properties jpaProperties = new Properties();
        jpaProperties.put(HIBERNATE_DIALECT, DIALECT);
        jpaProperties.put(HIBERNATE_HBM2DDL_AUTO, "update");
        jpaProperties.put(HIBERNATE_SHOW_SQL, "true");
        jpaProperties.put(HIBERNATE_FORMAT_SQL, "true");
        return jpaProperties;
    }
}