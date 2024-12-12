package com.zemnitskiy.aggregatehub.strategy.postgres;

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
 * PostgreSQL-specific implementation of {@link DatabaseStrategy} for configuring DataSource
 * and JPA properties tailored to PostgreSQL databases.
 */
@Component("postgres")
public class PostgresStrategy implements DatabaseStrategy {

    @Value("${database-strategies.postgres.driver}")
    private String driver;

    @Value("${database-strategies.postgres.dialect}")
    private String dialect;

    private static final Logger logger = LoggerFactory.getLogger(PostgresStrategy.class);

    /**
     * Creates a {@link DataSource} instance for PostgreSQL using the provided {@link DatabaseConfig}.
     *
     * @param config the database configuration containing connection details
     * @return a configured {@link DataSource}
     */
    @Override
    public DataSource createDataSource(DatabaseConfig config) {
        logger.info("Creating PostgreSQL DataSource with URL: {}", config.url());
        return DataSourceBuilder.create()
                .driverClassName(driver)
                .url(config.url())
                .username(config.user())
                .password(config.password())
                .build();
    }

    /**
     * Provides JPA properties specific to PostgreSQL, including dialect, schema management,
     * and SQL formatting options.
     *
     * @return a {@link Properties} instance with PostgreSQL-specific JPA properties
     */
    @Override
    public Properties getJpaProperties() {
        Properties jpaProperties = new Properties();
        jpaProperties.put(HIBERNATE_DIALECT, dialect);
        jpaProperties.put(HIBERNATE_HBM2DDL_AUTO, "update");
        jpaProperties.put(HIBERNATE_SHOW_SQL, "true");
        jpaProperties.put(HIBERNATE_FORMAT_SQL, "true");

        logger.debug("Configured JPA properties for PostgreSQL: {}", jpaProperties);
        return jpaProperties;
    }
}