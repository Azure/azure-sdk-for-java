// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.tables.implementation.TablesConstants;
import com.azure.data.tables.implementation.ModelHelper;

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
    private final String partitionKey;
    private final String rowKey;
    private final Map<String, Object> properties;

    private final OffsetDateTime timestamp;
    private final String eTag;
    private final String odataType;
    private final String odataId;
    private final String odataEditLink;

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
        this.partitionKey = partitionKey;

        if (rowKey == null || rowKey.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(
                "'%s' is an empty value.", TablesConstants.ROW_KEY)));
        }
        this.rowKey = rowKey;

        this.timestamp = null;
        this.eTag = null;
        this.odataEditLink = null;
        this.odataId = null;
        this.odataType = null;

        this.properties = new HashMap<>();
        properties.put(TablesConstants.PARTITION_KEY, partitionKey);
        properties.put(TablesConstants.ROW_KEY, rowKey);
    }

    TableEntity(Map<String, Object> properties) {
        Object partitionKey =  properties.get(TablesConstants.PARTITION_KEY);
        if (partitionKey != null && (!(partitionKey instanceof String) || ((String) partitionKey).isEmpty())) {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(
                "'%s' is an empty value or is of the wrong type.", TablesConstants.PARTITION_KEY)));
        }
        this.partitionKey = (String) partitionKey;

        Object rowKey = properties.get(TablesConstants.ROW_KEY);
        if (rowKey != null && (!(rowKey instanceof String) || ((String) rowKey).isEmpty())) {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(
                "'%s' is an empty value or is of the wrong type.", TablesConstants.ROW_KEY)));
        }
        this.rowKey = (String) rowKey;

        Object timestamp = properties.get(TablesConstants.TIMESTAMP_KEY);
        if (timestamp != null && !(timestamp instanceof OffsetDateTime)) {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(
                "'%s' value is of the wrong type.", TablesConstants.TIMESTAMP_KEY)));
        }
        this.timestamp = (OffsetDateTime) timestamp;

        Object eTag = properties.get(TablesConstants.ODATA_ETAG_KEY);
        if (eTag != null && !(eTag instanceof String)) {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(
                "'%s' value is of the wrong type.", TablesConstants.ODATA_ETAG_KEY)));
        }
        this.eTag = (String) eTag;

        Object odataEditLink = properties.get(TablesConstants.ODATA_EDIT_LINK_KEY);
        if (odataEditLink != null && !(odataEditLink instanceof String)) {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(
                "'%s' value is of the wrong type.", TablesConstants.ODATA_EDIT_LINK_KEY)));
        }
        this.odataEditLink = (String) odataEditLink;

        Object odataId = properties.get(TablesConstants.ODATA_ID_KEY);
        if (odataId != null && !(odataId instanceof String)) {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(
                "'%s' value is of the wrong type.", TablesConstants.ODATA_ID_KEY)));
        }
        this.odataId = (String) odataId;

        Object odataType = properties.get(TablesConstants.ODATA_TYPE_KEY);
        if (odataType != null && !(odataType instanceof String)) {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(
                "'%s' value is of the wrong type.", TablesConstants.ODATA_TYPE_KEY)));
        }
        this.odataType = (String) odataType;

        this.properties = properties;
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
     * @return The updated {@link TableEntity} object.
     * @throws NullPointerException if {@code key} is null.
     */
    public TableEntity addProperty(String key, Object value) {
        Objects.requireNonNull(key, "'key' cannot be null.");

        if (TablesConstants.PARTITION_KEY.equals(key)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException(TablesConstants.PARTITION_KEY + " cannot be set after object creation."));
        } else if (TablesConstants.ROW_KEY.equals(key)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException(TablesConstants.ROW_KEY + " cannot be set after object creation."));
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
     * gets the Timestamp
     *
     * @return the Timestamp for the entity
     */
    public OffsetDateTime getTimestamp() {
        return timestamp;
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
     * returns the type of this entity
     *
     * @return type
     */
    String getOdataType() {
        return odataType;
    }

    /**
     * returns the ID of this entity
     *
     * @return ID
     */
    String getOdataId() {
        return odataId;
    }

    /**
     * returns the edit link of this entity
     *
     * @return edit link
     */
    String getOdataEditLink() {
        return odataEditLink;
    }
}
