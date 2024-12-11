package com.zemnitskiy.aggregatehub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;


@ConfigurationProperties(prefix = "aggregate-hub")
public record DatabaseListConfig(List<DatabaseConfig> dataSources) {
    public record DatabaseConfig(String name, String strategy, String url, String user, String table, String password,
                                 Map<String, String> mapping) {
    }
}