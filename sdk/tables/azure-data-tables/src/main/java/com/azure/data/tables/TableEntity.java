// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.Fluent;
import java.util.Map;

/**
 * table entity class
 */
@Fluent
public class TableEntity {
    private final Map<String, Object> properties;
    //tableName
    //etag

    /**
     * creates a new TableEntity
     *
     * @param properties map of properties of the entity
     */
    public TableEntity(Map<String, Object> properties) {
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
     * @return the updated entity
     */
    public TableEntity addProperty(String key, Object value) {
        return this;
    }
}
