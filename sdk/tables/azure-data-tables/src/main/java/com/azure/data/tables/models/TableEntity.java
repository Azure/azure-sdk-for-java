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
 * An entity within a table.
 *
 * A {@code TableEntity} can be used directly when interacting with the Tables service, with methods on the
 * {@link com.azure.data.tables.TableClient} and {@link com.azure.data.tables.TableAsyncClient} classes that accept and
 * return {@code TableEntity} instances. After creating an instance, call the {@link #addProperty(String, Object)} or
 * {@link #setProperties(Map)} methods to add properties to the entity. When retrieving an entity from the service, call
 * the {@link #getProperty(String)} or {@link #getProperties()} methods to access the entity's properties.
 */
@Fluent
public final class TableEntity {
    private final ClientLogger logger = new ClientLogger(TableEntity.class);
    private final Map<String, Object> properties;
    private final String partitionKey;
    private final String rowKey;

    static {
        // This is used by classes in different packages to get access to private and package-private methods.
        ModelHelper.setEntityCreator(TableEntity::new);
    }

    /**
     * Construct a new {@code TableEntity}.
     *
     * @param partitionKey The partition key of the entity.
     * @param rowKey The row key of the entity.
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

        this.partitionKey = partitionKey;
        this.rowKey = rowKey;
    }

    private TableEntity() {
        this.properties = new HashMap<>();
        this.partitionKey = null;
        this.rowKey = null;
    }

    /**
     * Gets a single property from the entity's properties map.
     *
     * Only properties that have been added by calling {@link #addProperty(String, Object)} or
     * {@link #setProperties(Map)} will be returned from this method.
     *
     * @param key Key for the property.
     * @return Value of the property.
     * @throws NullPointerException if {@code key} is null.
     */
    public Object getProperty(String key) {
        return properties.get(key);
    }

    /**
     * Gets the map of the entity's properties.
     *
     * Only properties that have been added by calling {@link #addProperty(String, Object)} or
     * {@link #setProperties(Map)} will be returned from this method.
     *
     * @return A map of all properties representing this entity, including system properties.
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Adds a single property to the entity's properties map.
     *
     * @param key Key for the property.
     * @param value Value of the property.
     *
     * @return The updated {@link TableEntity}.
     * @throws NullPointerException if {@code key} is null.
     */
    public TableEntity addProperty(String key, Object value) {
        validateProperty(key, value);
        properties.put(key, value);

        return this;
    }

    /**
     * Sets the contents of the provided map to the entity's properties map.
     *
     * @param properties The map of properties to set.
     *
     * @return The updated {@link TableEntity}.
     * @throws NullPointerException if {@code properties} is null.
     */
    public TableEntity setProperties(Map<String, Object> properties) {
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            validateProperty(entry.getKey(), entry.getValue());
        }

        this.properties.clear();

        if (this.partitionKey != null) {
            this.properties.put(TablesConstants.PARTITION_KEY, this.partitionKey);
        }

        if (this.rowKey != null) {
            this.properties.put(TablesConstants.ROW_KEY, this.rowKey);
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
     * Gets the entity's row key.
     *
     * @return The entity's row key.
     */
    public String getRowKey() {
        return (String) properties.get(TablesConstants.ROW_KEY);
    }

    /**
     * Gets the entity's partition key.
     *
     * @return The entity's partition key.
     */
    public String getPartitionKey() {
        return (String) properties.get(TablesConstants.PARTITION_KEY);
    }

    /**
     * Gets the entity's timestamp.
     *
     * The timestamp is automatically populated by the service. New {@code TableEntity} instances will not have a
     * timestamp, but a timestamp will be present on any {@code TableEntity} returned from the service.
     *
     * @return The entity's timestamp.
     */
    public OffsetDateTime getTimestamp() {
        return (OffsetDateTime) properties.get(TablesConstants.TIMESTAMP_KEY);
    }

    /**
     * Gets the entity's eTag.
     *
     * The eTag is automatically populated by the service. New {@code TableEntity} instances will not have an eTag, but
     * an eTag will be present on any {@code TableEntity} returned from the service.
     *
     * @return The entity's eTag.
     */
    public String getETag() {
        return (String) properties.get(TablesConstants.ODATA_ETAG_KEY);
    }

    /**
     * returns the type of this entity
     *
     * @return type
     */
    String getOdataType() {
        return (String) properties.get(TablesConstants.ODATA_TYPE_KEY);
    }

    /**
     * returns the ID of this entity
     *
     * @return ID
     */
    String getOdataId() {
        return (String) properties.get(TablesConstants.ODATA_ID_KEY);
    }

    /**
     * returns the edit link of this entity
     *
     * @return edit link
     */
    String getOdataEditLink() {
        return (String) properties.get(TablesConstants.ODATA_EDIT_LINK_KEY);
    }
}
