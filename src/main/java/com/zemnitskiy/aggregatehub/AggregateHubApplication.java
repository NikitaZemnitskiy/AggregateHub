package com.zemnitskiy.aggregatehub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class AggregateHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(AggregateHubApplication.class, args);
    }

}
