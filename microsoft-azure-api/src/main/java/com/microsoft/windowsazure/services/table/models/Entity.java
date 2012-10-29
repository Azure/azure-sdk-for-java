/**
 * Copyright 2012 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.table.models;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an element of structured storage in a table. Tables store data as a collection of uniquely identifiable
 * entities. Entities are similar to rows in a database. An entity has a primary key and a set of properties. A property
 * is a name, typed-value pair, similar to a database column.
 * <p>
 * The Table service does not enforce any schema for tables, so two entities in the same table may have different sets
 * of properties. Developers may choose to enforce a schema on the client side. A table may contain any number of
 * entities.
 * <p>
 * An entity always has the following system properties:
 * <ul>
 * <li><strong>PartitionKey</strong> property</li>
 * <li><strong>RowKey</strong> property</li>
 * <li><strong>Timestamp</strong> property</li>
 * </ul>
 * These system properties are automatically included for every entity in a table. The names of these properties are
 * reserved and cannot be changed. The developer is responsible for inserting and updating the values of
 * <strong>PartitionKey</strong> and <strong>RowKey</strong>. The server manages the value of
 * <strong>Timestamp</strong>, which cannot be modified.
 * <p>
 * An entity can have up to 255 properties, including the three system properties. Therefore, the user may include up to
 * 252 custom properties, in addition to the three system properties. The combined size of all data in an entity's
 * properties cannot exceed 1 MB.
 * <p>
 * The following table shows the supported property data types in Windows Azure storage and the corresponding Java types
 * when deserialized.
 * <TABLE BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0">
 * <TR BGCOLOR="#EEEEFF" CLASS="TableSubHeadingColor">
 * <th>Storage Type</th>
 * <th>EdmType Value</th>
 * <th>Java Type</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td><strong>Edm.Binary</strong></td>
 * <td>{@link EdmType#BINARY}</td>
 * <td><code>byte[], Byte[]</code></td>
 * <td>An array of bytes up to 64 KB in size.</td>
 * </tr>
 * <tr>
 * <td><strong>Edm.Boolean</strong></td>
 * <td>{@link EdmType#BOOLEAN}</td>
 * <td><code>boolean, Boolean</code></td>
 * <td>A Boolean value.</td>
 * </tr>
 * <tr>
 * <td><strong>Edm.DateTime</strong></td>
 * <td>{@link EdmType#DATETIME}</td>
 * <td><code>Date</code></td>
 * <td>A 64-bit value expressed as Coordinated Universal Time (UTC). The supported range begins from 12:00 midnight,
 * January 1, 1601 A.D. (C.E.), UTC. The range ends at December 31, 9999.</td>
 * </tr>
 * <tr>
 * <td><strong>Edm.Double</strong></td>
 * <td>{@link EdmType#DOUBLE}</td>
 * <td><code>double, Double</code></td>
 * <td>A 64-bit double-precision floating point value.</td>
 * </tr>
 * <tr>
 * <td><strong>Edm.Guid</strong></td>
 * <td>{@link EdmType#GUID}</td>
 * <td><code>UUID</code></td>
 * <td>A 128-bit globally unique identifier.</td>
 * </tr>
 * <tr>
 * <td><strong>Edm.Int32</strong></td>
 * <td>{@link EdmType#INT32}</td>
 * <td><code>int, Integer</code></td>
 * <td>A 32-bit integer value.</td>
 * </tr>
 * <tr>
 * <td><strong>Edm.Int64</strong></td>
 * <td>{@link EdmType#INT64}</td>
 * <td><code>long, Long</code></td>
 * <td>A 64-bit integer value.</td>
 * </tr>
 * <tr>
 * <td><strong>Edm.String</strong></td>
 * <td>{@link EdmType#STRING}</td>
 * <td><code>String</code></td>
 * <td>A UTF-16-encoded value. String values may be up to 64 KB in size.</td>
 * </tr>
 * </table>
 * <p>
 * See the MSDN topic <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179338.aspx">Understanding the
 * Table Service Data Model</a> for an overview of tables, entities, and properties as used in the Windows Azure Storage
 * service.
 * <p>
 * For an overview of the available EDM primitive data types and names, see the <a
 * href="http://www.odata.org/developers/protocols/overview#AbstractTypeSystem">Primitive Data Types</a> section of the
 * <a href="http://www.odata.org/developers/protocols/overview">OData Protocol Overview</a>.
 * <p>
 */
public class Entity {
    private String etag;
    private Map<String, Property> properties = new HashMap<String, Property>();

    /**
     * Gets the ETag value for the entity. This value is used to determine if the table entity has changed since it was
     * last read from Windows Azure storage. When modifying an entity, the ETag value may be set to force the operation
     * to fail if the ETag does not match the ETag on the server.
     * 
     * @return
     *         A <code>String</code> containing the ETag for the entity.
     */
    public String getEtag() {
        return etag;
    }

    /**
     * Sets the ETag value for the entity. This value is used to determine if the table entity has changed since it was
     * last read from Windows Azure storage. When modifying an entity, the ETag value may be set to force the operation
     * to fail if the ETag does not match the ETag on the server. Set the <em>etag</em>parameter to <code>null</code> to
     * force an unconditional operation.
     * 
     * @param etag
     *            A <code>String</code> containing the ETag for the entity.
     */
    public Entity setEtag(String etag) {
        this.etag = etag;
        return this;
    }

    /**
     * Gets the <strong>PartitionKey</strong> value for the entity.
     * 
     * @return
     *         A <code>String</code> containing the <strong>PartitionKey</strong> value for the entity.
     */
    public String getPartitionKey() {
        Property p = getProperty("PartitionKey");
        return p == null ? null : (String) p.getValue();
    }

    /**
     * Sets the <strong>PartitionKey</strong> value for the entity.
     * <p>
     * Tables are partitioned to support load balancing across storage nodes. A table's entities are organized by
     * partition. A partition is a consecutive range of entities possessing the same partition key value. The partition
     * key is a unique identifier for the partition within a given table, specified by the <strong>PartitionKey</strong>
     * property. The partition key forms the first part of an entity's primary key. The partition key may be a string
     * value up to 1 KB in size.
     * <p>
     * You must include the <strong>PartitionKey</strong> property in every insert, update, and delete operation.
     * 
     * @param partitionKey
     *            A <code>String</code> containing the <strong>PartitionKey</strong> value for the entity.
     * @return
     *         A reference to this {@link Entity} instance.
     */
    public Entity setPartitionKey(String partitionKey) {
        setProperty("PartitionKey", null, partitionKey);
        return this;
    }

    /**
     * Gets the <strong>RowKey</strong> value for the entity.
     * 
     * @return
     *         A <code>String</code> containing the <strong>RowKey</strong> value for the entity.
     */
    public String getRowKey() {
        Property p = getProperty("RowKey");
        return p == null ? null : (String) p.getValue();
    }

    /**
     * Sets the <strong>RowKey</strong> value for the entity.
     * <p>
     * The second part of the primary key is the row key, specified by the <strong>RowKey</strong> property. The row key
     * is a unique identifier for an entity within a given partition. Together the <strong>PartitionKey</strong> and
     * <strong>RowKey</strong> uniquely identify every entity within a table.
     * <p>
     * The row key is a string value that may be up to 1 KB in size.
     * <p>
     * You must include the <strong>RowKey</strong> property in every insert, update, and delete operation.
     * 
     * @param rowKey
     *            A <code>String</code> containing the <strong>RowKey</strong> value for the entity.
     * @return
     *         A reference to this {@link Entity} instance.
     */
    public Entity setRowKey(String rowKey) {
        setProperty("RowKey", null, rowKey);
        return this;
    }

    /**
     * Gets the <strong>Timestamp</strong> value for the entity.
     * 
     * @return
     *         A {@link Date} containing the <strong>Timestamp</strong> value for the entity.
     */
    public Date getTimestamp() {
        Property p = getProperty("Timestamp");
        return p == null ? null : (Date) p.getValue();
    }

    /**
     * Sets the <strong>Timestamp</strong> value for the entity.
     * <p>
     * The <strong>Timestamp</strong> property is a value that is maintained on the server side to record the time an
     * entity was last modified. The Table service uses the <strong>Timestamp</strong> property internally to provide
     * optimistic concurrency. You should treat this property as opaque: It should not be read, nor set on insert or
     * update operations (the value will be ignored).
     * 
     * @param timestamp
     *            A {@link Date} containing the <strong>Timestamp</strong> value for the entity.
     * @return
     *         A reference to this {@link Entity} instance.
     */
    public Entity setTimestamp(Date timestamp) {
        setProperty("Timestamp", null, timestamp);
        return this;
    }

    /**
     * Gets the properties collection of name and typed-data pairs in the entity.
     * 
     * @return
     *         The {@link java.util.HashMap} collection of <code>String</code> property names to {@link Property}
     *         typed-data value pairs in the entity.
     */
    public Map<String, Property> getProperties() {
        return properties;
    }

    /**
     * Sets the properties collection of name and typed-data pairs in the entity.
     * 
     * @param properties
     *            The {@link java.util.HashMap} collection of <code>String</code> property names to {@link Property}
     *            typed-data value pairs to set in the entity.
     * @return
     *         A reference to this {@link Entity} instance.
     */
    public Entity setProperties(Map<String, Property> properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Gets the named property value from the properties collection in the entity.
     * 
     * @param name
     *            A {@link String} containing the name of the entity property to return.
     * @return
     *         The {@link Property} instance associated with the <em>name</em> parameter value in the entity,
     *         or <code>null</code> if the name is not in the properties collection.
     */
    public Property getProperty(String name) {
        return properties.get(name);
    }

    /**
     * Sets the property associated with the specified name in the properties collection in the entity.
     * 
     * @param name
     *            The {@link String} containing the name to associate with the property value.
     * @param property
     *            The {@link Property} instance to associate with the <em>name</em> parameter value in the entity
     * @return
     *         A reference to this {@link Entity} instance.
     */
    public Entity setProperty(String name, Property property) {
        this.properties.put(name, property);
        return this;
    }

    /**
     * Sets the property data type and value associated with the specified name in the properties collection in the
     * entity.
     * 
     * @param name
     *            The {@link String} containing the name to associate with the property value.
     * @param edmType
     *            A {@link String} containing the EDM data type to associate with the property value. This must be one
     *            of the supported EDM types, defined as string constants in the {@link EdmType} class.
     * @param value
     *            An {@link Object} containing the data value of the property, serializable as the associated EDM data
     *            type of the property.
     * @return
     *         A reference to this {@link Entity} instance.
     */
    public Entity setProperty(String name, String edmType, Object value) {
        setProperty(name, new Property().setEdmType(edmType).setValue(value));
        return this;
    }

    /**
     * Gets the named property value from the properties collection in the entity cast an an {@link Object}.
     * 
     * @param name
     *            A {@link String} containing the name of the entity property to return.
     * @return
     *         The value associated with the <em>name</em> parameter in the properties collection in the entity,
     *         cast as an {@link Object}, or <code>null</code> if the name is not in the properties collection.
     */
    public Object getPropertyValue(String name) {
        Property p = getProperty(name);
        return p == null ? null : p.getValue();
    }
}
