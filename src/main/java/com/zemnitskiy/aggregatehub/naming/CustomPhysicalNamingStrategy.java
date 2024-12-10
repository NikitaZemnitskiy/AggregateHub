package com.zemnitskiy.aggregatehub.naming;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import java.util.Map;
import java.util.HashMap;

public class CustomPhysicalNamingStrategy extends PhysicalNamingStrategyStandardImpl {

    private final Map<String, String> columnMapping;

    public CustomPhysicalNamingStrategy(String mappingString) {
        this.columnMapping = parseMappingString(mappingString);
    }

    private Map<String, String> parseMappingString(String mappingString) {
        Map<String, String> map = new HashMap<>();
        //  mappingString: "{id=MySql2id, username=Mysql2Username, name=Mysql2Name, surname=Mysql2Surname}"
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
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
        if (name == null) {
            return null;
        }

        String mappedName = columnMapping.get(name.getText());
        if (mappedName != null) {
            return Identifier.toIdentifier(mappedName);
        }

        return super.toPhysicalColumnName(name, context);
    }
}