package com.zemnitskiy.aggregatehub.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DatabaseListConfig.class)
public class DatabasesAutoConfig {
}