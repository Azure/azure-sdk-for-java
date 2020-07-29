// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.tables.implementation.EntityHelper;
import com.azure.data.tables.implementation.TableConstants;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.azure.data.tables.implementation.TableConstants.PARTITION_KEY;
import static com.azure.data.tables.implementation.TableConstants.ROW_KEY;

/**
 * table entity class
 */
@Fluent
public class Entity {
    private final ClientLogger logger = new ClientLogger(Entity.class);
    private final String partitionKey;
    private final String rowKey;
    private final Map<String, Object> properties = new HashMap<>();

    private String eTag;

    static {
        // This is used by classes in different packages to get access to private and package-private methods.
        EntityHelper.setEntityAccessor((entity, name) -> entity.setETag(name));
    }

    /**
     * Create a new instance.
     *
     * @param partitionKey the partition key
     * @param rowKey the row key
     */
    public Entity(String partitionKey, String rowKey) {
        this.rowKey = Objects.requireNonNull(rowKey, "'rowKey' cannot be null.");
        this.partitionKey = Objects.requireNonNull(partitionKey, "'partitionKey' cannot be null.");
        Objects.requireNonNull(properties, "'properties' cannot be null.");

        properties.put(PARTITION_KEY, partitionKey);
        properties.put(TableConstants.ROW_KEY, rowKey);
    }

    /**
     * Gets the map of properties
     *
     * @return map of properties representing this entity
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Adds a property to the entity.
     *
     * @param key Key to for the property.
     * @param value Value of the property.
     *
     * @return The updated {@link Entity} object.
     * @throws NullPointerException if {@code key} is null.
     */
    public Entity addProperty(String key, Object value) {
        Objects.requireNonNull(key, "'key' cannot be null.");

        if (PARTITION_KEY.equals(key)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException(PARTITION_KEY + " cannot be set after object creation."));
        } else if (ROW_KEY.equals(key)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException(ROW_KEY + " cannot be set after object creation."));
        }

        properties.put(key, value);
        return this;
    }

    /**
     * gets the row key
     *
     * @return the row key for the given entity
     */
    public String getRowKey() {
        return rowKey;
    }

    /**
     * gets the partition key
     *
     * @return the partition key for the given entity
     */
    public String getPartitionKey() {
        return partitionKey;
    }

    /**
     * gets the etag
     *
     * @return the etag for the entity
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Sets the ETag on the Entity.
     *
     * @param eTag ETag to set.
     */
    void setETag(String eTag) {
        this.eTag = eTag;
    }
}
