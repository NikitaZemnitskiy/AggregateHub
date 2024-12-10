package com.zemnitskiy.aggregatehub.repository.postgres;

import com.zemnitskiy.aggregatehub.config.DatabaseListConfig.DatabaseConfig;
import com.zemnitskiy.aggregatehub.repository.DatabaseStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(PostgresStrategy.class);

    public static final String DIALECT = "org.hibernate.dialect.PostgreSQLDialect";
    public static final String DRIVER = "org.postgresql.Driver";

    /**
     * Creates a {@link DataSource} instance for PostgreSQL using the provided {@link DatabaseConfig}.
     *
     * @param config the database configuration containing connection details
     * @return a configured {@link DataSource}
     */
    @Override
    public DataSource createDataSource(DatabaseConfig config) {
        logger.info("Creating PostgreSQL DataSource with URL: {}", config.getUrl());
        return DataSourceBuilder.create()
                .driverClassName(DRIVER)
                .url(config.getUrl())
                .username(config.getUser())
                .password(config.getPassword())
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
        jpaProperties.put(HIBERNATE_DIALECT, DIALECT);
        jpaProperties.put(HIBERNATE_HBM2DDL_AUTO, "update");
        jpaProperties.put(HIBERNATE_SHOW_SQL, "true");
        jpaProperties.put(HIBERNATE_FORMAT_SQL, "true");

        logger.debug("Configured JPA properties for PostgreSQL: {}", jpaProperties);
        return jpaProperties;
    }
}