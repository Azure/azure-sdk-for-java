package com.azure.data.tables;

import java.util.Map;

public class TableEntity {
    Map<String, Object> properties;

    public TableEntity() {

    }

    public TableEntity(String table, String row, String partition, Map<String, Object> properties) {
        this.properties = properties;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void addProperty(String key, Object value) {

    }
}
