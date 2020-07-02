// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import java.util.Map;

/**
 * table entity class
 */
public class TableEntity {
    private Map<String, Object> properties;

    /**
     * creates a new TableEntity
     *
     * @param properties map of properties of the entity
     */
    public TableEntity(Map<String, Object> properties) {
    }

    /**
     * returns a map of properties
     *
     * @return map of properties of thsi entity
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * adds a new property to this entity's property map
     *
     * @param key the key of the property
     * @param value the value of the property
     */
    public void addProperty(String key, Object value) {

    }

    /**
     * set the properties
     *
     * @param properties properties to set to this entity
     */
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
