package com.zemnitskiy.aggregatehub.naming;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import java.util.Map;
import java.util.HashMap;

public class CustomPhysicalNamingStrategy extends PhysicalNamingStrategyStandardImpl {

    private final Map<String, String> tableMapping;
    private final Map<String, String> columnMapping;

    public CustomPhysicalNamingStrategy(String tableMappingString, String columnMappingString) {
        this.tableMapping = parseMappingString(tableMappingString);
        this.columnMapping = parseMappingString(columnMappingString);
    }

    private Map<String, String> parseMappingString(String mappingString) {
        Map<String, String> map = new HashMap<>();
        if (mappingString == null || mappingString.isEmpty()) {
            return map;
        }

        mappingString = mappingString.trim();
        if (mappingString.startsWith("{") && mappingString.endsWith("}")) {
            mappingString = mappingString.substring(1, mappingString.length() - 1);
        }

        String[] entries = mappingString.split(", ");
        for (String entry : entries) {
            String[] kv = entry.split("=");
            if (kv.length == 2) {
                map.put(kv[0].trim(), kv[1].trim());
            }
        }
        return map;
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        if (name == null) {
            return null;
        }

        String logicalName = name.getText();
        String mappedName = tableMapping.get(logicalName);
        if (mappedName != null) {
            System.out.println("Mapping table '" + logicalName + "' to '" + mappedName + "'");
            return Identifier.toIdentifier(mappedName);
        }

        return super.toPhysicalTableName(name, context);
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
        if (name == null) {
            return null;
        }

        String logicalName = name.getText();
        String mappedName = columnMapping.get(logicalName);
        if (mappedName != null) {
            System.out.println("Mapping column '" + logicalName + "' to '" + mappedName + "'");
            return Identifier.toIdentifier(mappedName);
        }

        return super.toPhysicalColumnName(name, context);
    }
}