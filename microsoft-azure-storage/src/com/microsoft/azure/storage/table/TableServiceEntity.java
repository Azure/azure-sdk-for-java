/**
 * Copyright Microsoft Corporation
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

package com.microsoft.azure.storage.table;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageErrorCodeStrings;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.core.SR;

/**
 * The {@link TableServiceEntity} class represents the base object type for a table entity in the Storage service.
 * {@link TableServiceEntity} provides a base implementation for the {@link TableEntity} interface that provides
 * <code>readEntity</code> and <code>writeEntity</code> methods that by default serialize and deserialize all properties
 * via reflection. A table entity class may extend this class and override the <code>readEntity</code> and
 * <code>writeEntity</code> methods to provide customized or more performant serialization logic.
 * <p>
 * The use of reflection allows subclasses of {@link TableServiceEntity} to be serialized and deserialized without
 * having to implement the serialization code themselves. When both a getter method and setter method are found for a
 * given property name and data type, then the appropriate method is invoked automatically to serialize or deserialize
 * the data. To take advantage of the automatic serialization code, your table entity classes should provide getter and
 * setter methods for each property in the corresponding table entity in Microsoft Azure table storage. The reflection
 * code looks for getter and setter methods in pairs of the form
 * <p>
 * <code>public <em>type</em> get<em>PropertyName</em>() { ... }</code>
 * <p>
 * and
 * <p>
 * <code>public void set<em>PropertyName</em>(<em>type</em> parameter) { ... }</code>
 * <p>
 * where <em>PropertyName</em> is a property name for the table entity, and <em>type</em> is a Java type compatible with
 * the EDM data type of the property. See the table below for a map of property types to their Java equivalents. The
 * {@link StoreAs} annotation may be applied with a <code>name</code> attribute to specify a property name for
 * reflection on getter and setter methods that do not follow the property name convention. Method names and the
 * <code>name</code> attribute of {@link StoreAs} annotations are case sensitive for matching property names with
 * reflection. Use the {@link Ignore} annotation to prevent methods from being used by reflection for automatic
 * serialization and deserialization. Note that the names "PartitionKey", "RowKey", "Timestamp", and "Etag" are reserved
 * and will be ignored if set with the {@link StoreAs} annotation in a subclass.
 * <p>
 * The following table shows the supported property data types in Microsoft Azure storage and the corresponding Java
 * types when deserialized.
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
 * <td>{@link EdmType#DATE_TIME}</td>
 * <td><code>java.util.Date</code></td>
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
 * See the MSDN topic <a href="http://msdn.microsoft.com//library/azure/dd179338.aspx">Understanding the Table Service
 * Data Model</a> for an overview of tables, entities, and properties as used in the Microsoft Azure Storage service.
 * <p>
 * For an overview of the available EDM primitive data types and names, see the
 * 
 * <a href="http://www.odata.org/developers/protocols/overview#AbstractTypeSystem">Primitive Data Types</a> section of
 * the <a href="http://www.odata.org/developers/protocols/overview">OData Protocol Overview</a>.
 * <p>
 * 
 * @see EdmType
 */
public class TableServiceEntity implements TableEntity {
    /**
     * Deserializes the table entity property map into the specified object instance using reflection.
     * <p>
     * This static method takes an object instance that represents a table entity type and uses reflection on its class
     * type to find methods to deserialize the data from the property map into the instance.
     * <p>
     * Each property name and data type in the properties map is compared with the methods in the class type for a pair
     * of getter and setter methods to use for serialization and deserialization. The class is scanned for methods with
     * names that match the property name with "get" and "set" prepended, or with the {@link StoreAs} annotation set
     * with the property name. The methods must have return types or parameter data types that match the data type of
     * the corresponding {@link EntityProperty} value. If such a pair is found, the data is copied into the instance
     * object by invoking the setter method on the instance. Properties that do not match a method pair by name and data
     * type are not copied.
     * 
     * @param instance
     *            An <code>Object</code> reference to an instance of a class implementing {@link TableEntity} to
     *            deserialize the table entity
     *            data into.
     * @param properties
     *            A <code>java.util.HashMap</code> object which maps <code>String</code> property names to
     *            {@link EntityProperty} objects containing typed data
     *            values to deserialize into the instance parameter object.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation.
     * 
     * @throws IllegalArgumentException
     *             if the table entity response received is invalid or improperly formatted.
     * @throws IllegalAccessException
     *             if the table entity threw an exception during deserialization.
     * @throws InvocationTargetException
     *             if a method invoked on the instance parameter threw an exception during deserialization.
     */
    public static void readEntityWithReflection(final Object instance,
            final HashMap<String, EntityProperty> properties, final OperationContext opContext)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        final HashMap<String, PropertyPair> props = PropertyPair.generatePropertyPairs(instance.getClass());

        for (final Entry<String, EntityProperty> p : properties.entrySet()) {
            if (props.containsKey(p.getKey())) {
                props.get(p.getKey()).consumeEntityProperty(p.getValue(), instance);
            }
        }
    }

    /**
     * Serializes the property data from a table entity instance into a property map using reflection.
     * <p>
     * This static method takes an object instance that represents a table entity type and uses reflection on its class
     * type to find methods to serialize the data from the instance into the property map.
     * <p>
     * Each property name and data type in the properties map is compared with the methods in the class type for a pair
     * of getter and setter methods to use for serialization and deserialization. The class is scanned for methods with
     * names that match the property name with "get" and "set" prepended, or with the {@link StoreAs} annotation set
     * with the property name. The methods must have return types or parameter data types that match the data type of
     * the corresponding {@link EntityProperty} value. If such a pair is found, the data is copied from the instance
     * object by invoking the getter method on the instance. Properties that do not have a method pair with matching
     * name and data type are not copied.
     * 
     * @param instance
     *            An <code>Object</code> reference to an instance of a class implementing {@link TableEntity} to
     *            serialize the table entity
     *            data from.
     * @return
     *         A <code>java.util.HashMap</code> object which maps <code>String</code> property names to
     *         {@link EntityProperty} objects containing typed data values serialized from the instance parameter
     *         object.
     * 
     * @throws IllegalArgumentException
     *             if the table entity is invalid or improperly formatted.
     * @throws IllegalAccessException
     *             if the table entity threw an exception during serialization.
     * @throws InvocationTargetException
     *             if a method invoked on the instance parameter threw an exception during serialization.
     */
    public static HashMap<String, EntityProperty> writeEntityWithReflection(final Object instance)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        final HashMap<String, PropertyPair> props = PropertyPair.generatePropertyPairs(instance.getClass());

        final HashMap<String, EntityProperty> retVal = new HashMap<String, EntityProperty>();
        for (final Entry<String, PropertyPair> p : props.entrySet()) {
            retVal.put(p.getValue().effectiveName, p.getValue().generateEntityProperty(instance));
        }

        return retVal;
    }

    /**
     * The default to multiply the number of CPU's by to get the number of threads to allow in the
     * <code>ReflectedEntityCache</code>
     */
    private static final int DEFAULT_CONCURRENCY_MULTIPLIER = 4;

    /**
     * The default load factor for the <code>ReflectedEntityCache</code>
     */
    private static final float DEFAULT_LOAD_FACTOR = (float) .75;

    /**
     * The default initial capacity for the <code>ReflectedEntityCache</code>
     */
    private static final int DEFAULT_INITIAL_CAPACITY = 31;

    /**
     * The reflected entity cache stores known entity types and their respective reflected entity dictionaries. Rather
     * than using reflection on a known entity type, the values from the dictionary are used instead.
     */
    private static boolean disableReflectedEntityCache = false;

    /**
     * Reserved for internal use. The value of the partition key in the entity.
     */
    protected String partitionKey = null;

    /**
     * Reserved for internal use. The value of the row key in the entity.
     */
    protected String rowKey = null;

    /**
     * Reserved for internal use. The value of the ETag for the entity.
     */
    protected String etag = null;

    /**
     * Reserved for internal use. The value of the Timestamp in the entity.
     */
    protected Date timeStamp = new Date();

    /**
     * Initializes an empty {@link TableServiceEntity} instance.
     */
    public TableServiceEntity() {
        // Empty ctor
    }

    /**
     * Initializes a new instance of the {@link TableServiceEntity} class with the specified partition key and row key.
     * 
     * @param partitionKey
     *            A <code>String</code> which represents the partition key of the {@link TableServiceEntity} to be
     *            initialized.
     * @param rowKey
     *            A <code>String</code> which represents the row key of the {@link TableServiceEntity} to be
     *            initialized.
     */
    public TableServiceEntity(String partitionKey, String rowKey) {
        this.partitionKey = partitionKey;
        this.rowKey = rowKey;
    }

    /**
     * Gets the ETag value to verify for the entity. This value is used to determine if the table entity has changed 
     * since it was last read from Microsoft Azure storage. The client cannot update this value on the service.
     * 
     * @return
     *         A <code>String</code> containing the ETag for the entity.
     */
    @Override
    public String getEtag() {
        return this.etag;
    }

    /**
     * Gets the PartitionKey value for the entity.
     * 
     * @return
     *         A <code>String</code> containing the PartitionKey value for the entity.
     */
    @Override
    public String getPartitionKey() {
        return this.partitionKey;
    }

    /**
     * Gets the RowKey value for the entity.
     * 
     * @return
     *         A <code>String</code> containing the RowKey value for the entity.
     */
    @Override
    public String getRowKey() {
        return this.rowKey;
    }

    /**
     * Gets the Timestamp for the entity. The server manages the value of Timestamp, which cannot be modified. 
     * 
     * @return
     *         A <code>java.util.Date</code> object which represents the Timestamp value for the entity.
     */
    @Override
    public Date getTimestamp() {
        return this.timeStamp;
    }

    /**
     * Gets a value indicating whether or not the reflected entity cache is disabled. For most scenarios, disabling
     * the reflected entity cache is not recommended due to its effect on performance.
     * 
     * The reflected entity cache stores known entity types and their respective reflected entity dictionaries. Rather
     * than using reflection on a known entity type, the values from the dictionary are used instead.
     * 
     * @return
     *         <code>true</code> if the reflected entity cache is disabled; otherwise, <code>false</code>.
     */
    public static boolean isReflectedEntityCacheDisabled() {
        return TableServiceEntity.disableReflectedEntityCache;
    }

    /**
     * Sets a boolean representing whether or not the reflected entity cache is disabled. For most scenarios, disabling
     * the reflected entity cache is not recommended due to its effect on performance.
     * 
     * The reflected entity cache stores known entity types and their respective reflected entity dictionaries. Rather
     * than using reflection on a known entity type, the values from the dictionary are used instead.
     * 
     * @param disableReflectedEntityCache
     *            <code>true</code> to disable the reflected entity cache; otherwise, <code>false</code>.
     */
    public static void setReflectedEntityCacheDisabled(boolean disableReflectedEntityCache) {
        if (TableServiceEntity.reflectedEntityCache != null && disableReflectedEntityCache) {
            TableServiceEntity.reflectedEntityCache.clear();
        }

        TableServiceEntity.disableReflectedEntityCache = disableReflectedEntityCache;
    }

    /**
     * Populates this table entity instance using the map of property names to {@link EntityProperty} data typed values.
     * <p>
     * This method invokes {@link TableServiceEntity#readEntityWithReflection} to populate the table entity instance the
     * method is called on using reflection. Table entity classes that extend {@link TableServiceEntity} can take
     * advantage of this behavior by implementing getter and setter methods for the particular properties of the table
     * entity in Microsoft Azure storage the class represents.
     * <p>
     * Override this method in classes that extend {@link TableServiceEntity} to invoke custom serialization code.
     * 
     * @param properties
     *            The <code>java.util.HashMap</code> of <code>String</code> property names to {@link EntityProperty}
     *            data values to deserialize and store in this table entity instance.
     * @param opContext
     *            An {@link OperationContext} object used to track the execution of the operation.
     * @throws StorageException
     *             if an error occurs during the deserialization.
     */
    @Override
    public void readEntity(final HashMap<String, EntityProperty> properties, final OperationContext opContext)
            throws StorageException {
        try {
            readEntityWithReflection(this, properties, opContext);
        }
        catch (IllegalArgumentException e) {
            throw new StorageException(StorageErrorCodeStrings.INVALID_DOCUMENT, SR.RESPONSE_RECEIVED_IS_INVALID,
                    Constants.HeaderConstants.HTTP_UNUSED_306, null, e);
        }
        catch (IllegalAccessException e) {
            throw new StorageException(StorageErrorCodeStrings.INVALID_DOCUMENT,
                    SR.EXCEPTION_THROWN_DURING_DESERIALIZATION, Constants.HeaderConstants.HTTP_UNUSED_306, null, e);
        }
        catch (InvocationTargetException e) {
            throw new StorageException(StorageErrorCodeStrings.INTERNAL_ERROR,
                    SR.EXCEPTION_THROWN_DURING_DESERIALIZATION, Constants.HeaderConstants.HTTP_UNUSED_306, null, e);
        }
    }

    /**
     * Sets the ETag value to verify for the entity. This value is used to determine if the table entity has changed 
     * since it was last read from Microsoft Azure storage. The client cannot update this value on the service.
     * 
     * @param etag
     *            A <code>String</code> containing the ETag for the entity.
     */
    @Override
    public void setEtag(final String etag) {
        this.etag = etag;
    }

    /**
     * Sets the PartitionKey value for the entity.
     * 
     * @param partitionKey
     *            A <code>String</code> containing the PartitionKey value for the entity.
     */
    @Override
    public void setPartitionKey(final String partitionKey) {
        this.partitionKey = partitionKey;
    }

    /**
     * Sets the RowKey value for the entity.
     * 
     * @param rowKey
     *            A <code>String</code> containing the RowKey value for the entity.
     */
    @Override
    public void setRowKey(final String rowKey) {
        this.rowKey = rowKey;
    }

    /**
     * Sets the <code>timeStamp</code> value for the entity. Note that the timestamp property is a read-only property,
     * set by the service only.
     * 
     * @param timeStamp
     *            A <code>java.util.Date</code> containing the <code>timeStamp</code> value for the entity.
     */
    @Override
    public void setTimestamp(final Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     * Returns a map of property names to {@link EntityProperty} data typed values created by serializing this table
     * entity instance.
     * <p>
     * This method invokes {@link #writeEntityWithReflection} to serialize the table entity instance the method is
     * called on using reflection. Table entity classes that extend {@link TableServiceEntity} can take advantage of
     * this behavior by implementing getter and setter methods for the particular properties of the table entity in
     * Microsoft Azure storage the class represents. Note that the property names "PartitionKey", "RowKey", and
     * "Timestamp" are reserved and will be ignored if set on other methods with the {@link StoreAs} annotation.
     * <p>
     * Override this method in classes that extend {@link TableServiceEntity} to invoke custom serialization code.
     * 
     * @param opContext
     *            An {@link OperationContext} object used to track the execution of the operation.
     * @return
     *         A <code>java.util.HashMap</code> of <code>String</code> property names to {@link EntityProperty} data
     *         typed values representing the properties serialized from this table entity instance.
     * @throws StorageException
     *             if an error occurs during the serialization.
     */
    @Override
    public HashMap<String, EntityProperty> writeEntity(final OperationContext opContext) throws StorageException {
        try {
            return writeEntityWithReflection(this);
        }
        catch (final IllegalAccessException e) {
            throw new StorageException(StorageErrorCodeStrings.INTERNAL_ERROR,
                    SR.ATTEMPTED_TO_SERIALIZE_INACCESSIBLE_PROPERTY, Constants.HeaderConstants.HTTP_UNUSED_306, null, e);
        }
        catch (final InvocationTargetException e) {
            throw new StorageException(StorageErrorCodeStrings.INTERNAL_ERROR,
                    SR.EXCEPTION_THROWN_DURING_SERIALIZATION, Constants.HeaderConstants.HTTP_UNUSED_306, null, e);
        }
    }

    /**
     * The reflected entity cache caches known entity types and their respective reflected entity dictionaries when
     * entities are deserialized and the payload does not include JSON metadata.
     */
    private static ConcurrentHashMap<Class<?>, HashMap<String, PropertyPair>> reflectedEntityCache = initialize();

    private static ConcurrentHashMap<Class<?>, HashMap<String, PropertyPair>> initialize() {
        Runtime runtime = Runtime.getRuntime();
        int numberOfProcessors = runtime.availableProcessors();
        return new ConcurrentHashMap<Class<?>, HashMap<String, PropertyPair>>(DEFAULT_INITIAL_CAPACITY,
                DEFAULT_LOAD_FACTOR, numberOfProcessors * DEFAULT_CONCURRENCY_MULTIPLIER);
    }

    /**
     * The reflected entity cache caches known entity types and their respective reflected entity dictionaries when
     * entities are deserialized and the payload does not include JSON metadata.
     * 
     * @return
     *         The <code>ConcurrentHashMap<Class<?>, HashMap<String, PropertyPair>></code> representing the known entity
     *         types and their reflected entity dictionaries
     */
    protected static ConcurrentHashMap<Class<?>, HashMap<String, PropertyPair>> getReflectedEntityCache() {
        return TableServiceEntity.reflectedEntityCache;
    }
}