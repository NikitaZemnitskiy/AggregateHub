package com.zemnitskiy.aggregatehub.strategy.mysql;

import com.zemnitskiy.aggregatehub.config.DatabaseListConfig.DatabaseConfig;
import com.zemnitskiy.aggregatehub.strategy.DatabaseStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * MySQL-specific implementation of {@link DatabaseStrategy} for configuring DataSource
 * and JPA properties tailored to MySQL databases.
 */
@Component("mysql")
public class MySqlStrategy implements DatabaseStrategy {

    @Value("${database-strategies.mysql.driver}")
    private String driver;

    @Value("${database-strategies.mysql.dialect}")
    private String dialect;

    private static final Logger logger = LoggerFactory.getLogger(MySqlStrategy.class);

    /**
     * Creates a {@link DataSource} instance for MySQL using the provided {@link DatabaseConfig}.
     *
     * @param config the database configuration containing connection details
     * @return a configured {@link DataSource}
     */
    @Override
    public DataSource createDataSource(DatabaseConfig config) {
        logger.info("Creating MySQL DataSource with URL: {}", config.url());
        return DataSourceBuilder.create()
                .driverClassName(driver)
                .url(config.url())
                .username(config.user())
                .password(config.password())
                .build();
    }

    /**
     * Provides JPA properties specific to MySQL, including dialect, schema management,
     * and SQL formatting options.
     *
     * @return a {@link Properties} instance with MySQL-specific JPA properties
     */
    @Override
    public Properties getJpaProperties() {
        Properties jpaProperties = new Properties();
        jpaProperties.put(HIBERNATE_DIALECT, dialect);
        jpaProperties.put(HIBERNATE_HBM2DDL_AUTO, "update");
        jpaProperties.put(HIBERNATE_SHOW_SQL, "true");
        jpaProperties.put(HIBERNATE_FORMAT_SQL, "true");

        logger.debug("Configured JPA properties for MySQL: {}", jpaProperties);
        return jpaProperties;
    }
}