package com.zemnitskiy.aggregatehub;

import com.zemnitskiy.aggregatehub.config.DatabaseListConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@EnableConfigurationProperties(DatabaseListConfig.class)
public class AggregateHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(AggregateHubApplication.class, args);
    }

}
