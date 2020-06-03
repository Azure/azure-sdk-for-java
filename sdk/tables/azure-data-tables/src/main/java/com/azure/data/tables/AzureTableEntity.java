package com.azure.data.tables;

/**
 * Represents an entity in azure table.
 */
public class AzureTableEntity {
    private final String key;
    private final Object value;

    AzureTableEntity(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public String getKey() {
        return key;
    }
}
