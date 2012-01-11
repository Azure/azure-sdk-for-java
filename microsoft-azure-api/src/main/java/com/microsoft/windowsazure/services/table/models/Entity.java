package com.microsoft.windowsazure.services.table.models;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Entity {
    private String etag;
    private Map<String, Property> properties = new HashMap<String, Property>();

    public String getEtag() {
        return etag;
    }

    public Entity setEtag(String etag) {
        this.etag = etag;
        return this;
    }

    public String getPartitionKey() {
        Property p = getProperty("PartitionKey");
        return p == null ? null : (String) p.getValue();
    }

    public Entity setPartitionKey(String partitionKey) {
        setProperty("PartitionKey", null, partitionKey);
        return this;
    }

    public String getRowKey() {
        Property p = getProperty("RowKey");
        return p == null ? null : (String) p.getValue();
    }

    public Entity setRowKey(String rowKey) {
        setProperty("RowKey", null, rowKey);
        return this;
    }

    public Date getTimestamp() {
        Property p = getProperty("Timestamp");
        return p == null ? null : (Date) p.getValue();
    }

    public Entity setTimestamp(Date timestamp) {
        setProperty("Timestamp", null, timestamp);
        return this;
    }

    public Map<String, Property> getProperties() {
        return properties;
    }

    public Entity setProperties(Map<String, Property> properties) {
        this.properties = properties;
        return this;
    }

    public Property getProperty(String name) {
        return properties.get(name);
    }

    public Entity setProperty(String name, Property property) {
        this.properties.put(name, property);
        return this;
    }

    public Entity setProperty(String name, String edmType, Object value) {
        setProperty(name, new Property().setEdmType(edmType).setValue(value));
        return this;
    }

    public Object getPropertyValue(String name) {
        Property p = getProperty(name);
        return p == null ? null : p.getValue();
    }
}
