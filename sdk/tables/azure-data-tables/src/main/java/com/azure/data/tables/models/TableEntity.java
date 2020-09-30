// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.tables.implementation.ModelHelper;
import com.azure.data.tables.implementation.TablesConstants;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * table entity class
 */
@Fluent
public class TableEntity {
    private final ClientLogger logger = new ClientLogger(TableEntity.class);
    private final Map<String, Object> properties;

    static {
        // This is used by classes in different packages to get access to private and package-private methods.
        ModelHelper.setEntityCreator(TableEntity::new);
    }

    /**
     * Create a new instance.
     *
     * @param partitionKey the partition key
     * @param rowKey the row key
     */
    public TableEntity(String partitionKey, String rowKey) {
        if (partitionKey == null || partitionKey.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(
                "'%s' is an empty value.", TablesConstants.PARTITION_KEY)));
        }

        if (rowKey == null || rowKey.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(
                "'%s' is an empty value.", TablesConstants.ROW_KEY)));
        }

        this.properties = new HashMap<>();
        properties.put(TablesConstants.PARTITION_KEY, partitionKey);
        properties.put(TablesConstants.ROW_KEY, rowKey);
    }

    private TableEntity() {
        this.properties = new HashMap<>();
    }

    /**
     * Gets a single property from the properties map
     *
     * @param key Key for the property.
     * @return Value of the property.
     */
    public final Object getProperty(String key) {
        return properties.get(key);
    }

    /**
     * Gets the map of properties
     *
     * @return map of properties representing this entity
     */
    public final Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Adds a property to the entity.
     *
     * @param key Key for the property.
     * @param value Value of the property.
     *
     * @return The updated {@link TableEntity} object.
     * @throws NullPointerException if {@code key} is null.
     */
    public final TableEntity addProperty(String key, Object value) {
        validateProperty(key, value);
        properties.put(key, value);
        return this;
    }

    /**
     * Adds properties to the entity.
     *
     * @param properties The map of properties to add.
     *
     * @return The updated {@link TableEntity} object.
     */
    public final TableEntity addProperties(Map<String, Object> properties) {
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            validateProperty(entry.getKey(), entry.getValue());
        }
        this.properties.putAll(properties);
        return this;
    }

    private void validateProperty(String key, Object value) {
        Objects.requireNonNull(key, "'key' cannot be null.");

        if ((TablesConstants.PARTITION_KEY.equals(key) || TablesConstants.ROW_KEY.equals(key)) && value != null
            && (!(value instanceof String) || ((String) value).isEmpty())) {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(
                "'%s' must be a non-empty String.", key)));
        }

        if (TablesConstants.TIMESTAMP_KEY.equals(key) && value != null && !(value instanceof OffsetDateTime)) {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(
                "'%s' must be an OffsetDateTime.", key)));
        }

        if ((TablesConstants.ODATA_ETAG_KEY.equals(key) || TablesConstants.ODATA_EDIT_LINK_KEY.equals(key)
            || TablesConstants.ODATA_ID_KEY.equals(key) || TablesConstants.ODATA_TYPE_KEY.equals(key)) && value != null
            && !(value instanceof String)) {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(
                "'%s' must be a String.", key)));
        }
    }

    /**
     * gets the row key
     *
     * @return the row key for the given entity
     */
    public final String getRowKey() {
        return (String) properties.get(TablesConstants.ROW_KEY);
    }

    /**
     * gets the partition key
     *
     * @return the partition key for the given entity
     */
    public final String getPartitionKey() {
        return (String) properties.get(TablesConstants.PARTITION_KEY);
    }

    /**
     * gets the Timestamp
     *
     * @return the Timestamp for the entity
     */
    public final OffsetDateTime getTimestamp() {
        return (OffsetDateTime) properties.get(TablesConstants.TIMESTAMP_KEY);
    }

    /**
     * gets the etag
     *
     * @return the etag for the entity
     */
    public final String getETag() {
        return (String) properties.get(TablesConstants.ODATA_ETAG_KEY);
    }

    /**
     * returns the type of this entity
     *
     * @return type
     */
    final String getOdataType() {
        return (String) properties.get(TablesConstants.ODATA_TYPE_KEY);
    }

    /**
     * returns the ID of this entity
     *
     * @return ID
     */
    final String getOdataId() {
        return (String) properties.get(TablesConstants.ODATA_ID_KEY);
    }

    /**
     * returns the edit link of this entity
     *
     * @return edit link
     */
    final String getOdataEditLink() {
        return (String) properties.get(TablesConstants.ODATA_EDIT_LINK_KEY);
    }
}
