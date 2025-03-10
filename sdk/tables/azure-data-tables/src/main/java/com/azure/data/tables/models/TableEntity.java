// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.tables.implementation.TableEntityAccessHelper;
import com.azure.data.tables.implementation.TablesConstants;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <h2>Overview</h2>
 * An entity within a table.
 * <p>
 * A {@code TableEntity} can be used directly when interacting with the Tables service, with methods on the
 * {@link com.azure.data.tables.TableClient} and {@link com.azure.data.tables.TableAsyncClient} classes that accept and
 * return {@code TableEntity} instances. After creating an instance, call the {@link #addProperty(String, Object)} or
 * {@link #setProperties(Map)} methods to add properties to the entity. When retrieving an entity from the service, call
 * the {@link #getProperty(String)} or {@link #getProperties()} methods to access the entity's properties.
 *
 * <h3><strong>Usage Code Samples</strong></h3>
 *
 * <p>The following samples provide examples of common operations preformed on a TableEntity. The samples use a subset of acceptable
 * property values. For an exhaustive list, see <a href="https://docs.microsoft.com/rest/api/storageservices/understanding-the-table-service-data-model#property-types">the service documentation</a>.
 * </p>
 *
 * <strong>Create a TableEntity</strong>
 *
 * <p>The following sample shows the creation of a table entity.</p>
 *
 *  <!-- src_embed com.azure.data.tables.models.TableEntity.create#string-string -->
 * <pre>
 * TableEntity entity = new TableEntity&#40;&quot;partitionKey&quot;, &quot;rowKey&quot;&#41;;
 * </pre>
 *  <!-- end com.azure.data.tables.models.TableEntity.create#string-string -->
 *
 * <strong>Add properties to a TableEntity</strong>
 *
 * <p>The following sample shows the addition of properties to a table entity.</p>
 *
 * <!-- src_embed com.azure.data.tables.models.TableEntity.create#string-string -->
 * <pre>
 * TableEntity entity = new TableEntity&#40;&quot;partitionKey&quot;, &quot;rowKey&quot;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.models.TableEntity.create#string-string -->
 *
 * <strong>Set properties from a TableEntity</strong>
 *
 * <p>The following sample shows the setting of a table entity's properties.</p>
 *
 * <!-- src_embed com.azure.data.tables.models.TableEntity.setProperties#map -->
 * <pre>
 * Map&lt;String, Object&gt; properties = new HashMap&lt;&gt;&#40;&#41;;
 * properties.put&#40;&quot;String&quot;, &quot;StringValue&quot;&#41;;
 * properties.put&#40;&quot;Integer&quot;, 100&#41;;
 * properties.put&#40;&quot;Boolean&quot;, true&#41;;
 * TableEntity entity = new TableEntity&#40;&quot;partitionKey&quot;, &quot;rowKey&quot;&#41;
 *     .setProperties&#40;properties&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.models.TableEntity.setProperties#map -->
 *
 * <strong>Get a property from a TableEntity</strong>
 *
 * <p>The following sample shows the retrieval of a property from a table entity.</p>
 *
 * <!-- src_embed com.azure.data.tables.models.TableEntity.getProperty#string -->
 * <pre>
 * TableEntity entity = new TableEntity&#40;&quot;partitionKey&quot;, &quot;rowKey&quot;&#41;
 *     .addProperty&#40;&quot;String&quot;, &quot;StringValue&quot;&#41;
 *     .addProperty&#40;&quot;Integer&quot;, 100&#41;
 *     .addProperty&#40;&quot;Boolean&quot;, true&#41;;
 *
 * String stringValue = &#40;String&#41; entity.getProperty&#40;&quot;String&quot;&#41;;
 * int integerValue = &#40;int&#41; entity.getProperty&#40;&quot;Integer&quot;&#41;;
 * boolean booleanValue = &#40;boolean&#41; entity.getProperty&#40;&quot;Boolean&quot;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.models.TableEntity.getProperty#string -->
 *
 * <strong>Get properties from a TableEntity</strong>
 *
 * <p>The following sample shows the retrieval of all properties from a table entity.</p>
 *
 * <!-- src_embed com.azure.data.tables.models.TableEntity.getProperties -->
 * <pre>
 * TableEntity entity = new TableEntity&#40;&quot;partitionKey&quot;, &quot;rowKey&quot;&#41;
 *     .addProperty&#40;&quot;String&quot;, &quot;StringValue&quot;&#41;
 *     .addProperty&#40;&quot;Integer&quot;, 100&#41;
 *     .addProperty&#40;&quot;Boolean&quot;, true&#41;;
 *
 * Map&lt;String, Object&gt; properties = entity.getProperties&#40;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.models.TableEntity.getProperties -->
 *
 */
@Fluent
public final class TableEntity {
    private final ClientLogger logger = new ClientLogger(TableEntity.class);
    private final Map<String, Object> properties;
    private final String partitionKey;
    private final String rowKey;

    static {
        // This is used by classes in different packages to get access to private and package-private methods.
        TableEntityAccessHelper.setTableEntityCreator(TableEntity::new);
    }

    /**
     * Construct a new {@code TableEntity}.
     *
     * @param partitionKey The partition key of the entity.
     * @param rowKey The row key of the entity.
     */
    public TableEntity(String partitionKey, String rowKey) {
        if (null == partitionKey) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException(String.format("'%s' is an null value.", TablesConstants.PARTITION_KEY)));
        }

        if (null == rowKey) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException(String.format("'%s' is an null value.", TablesConstants.ROW_KEY)));
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
     * <p>
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
     * <p>
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

        if (TablesConstants.TIMESTAMP_KEY.equals(key) && value != null && !(value instanceof OffsetDateTime)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException(String.format("'%s' must be an OffsetDateTime.", key)));
        }

        if ((TablesConstants.ODATA_ETAG_KEY.equals(key)
            || TablesConstants.ODATA_EDIT_LINK_KEY.equals(key)
            || TablesConstants.ODATA_ID_KEY.equals(key)
            || TablesConstants.ODATA_TYPE_KEY.equals(key)) && value != null && !(value instanceof String)) {

            throw logger
                .logExceptionAsError(new IllegalArgumentException(String.format("'%s' must be a String.", key)));
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
     * <p>
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
     * <p>
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
