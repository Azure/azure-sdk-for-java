// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import java.util.Map;

public class TableEntity {
    Map<String, Object> properties;

    TableEntity() {
    }

    /**
     * creates a new TableEntity
     *
     * @param rowKey rowKey
     * @param partitionKey partitionKey
     * @param properties map of properties of the entity
     */
    TableEntity(String rowKey, String partitionKey, Map<String, Object> properties) {
        this.properties = properties;
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
}
