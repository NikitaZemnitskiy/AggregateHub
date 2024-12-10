package com.zemnitskiy.aggregatehub.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "aggregate-hub")
public class DatabaseListConfig {
    private List<DatabaseConfig> dataSources;

    @Data
    public static class DatabaseConfig {
        private String name;
        private String strategy;
        private String url;
        private String user;
        private String table    ;
        private String password;
        private Map<String, String> mapping;
    }
}