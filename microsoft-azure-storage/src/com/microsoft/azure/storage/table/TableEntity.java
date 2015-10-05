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

import java.util.Date;
import java.util.HashMap;

import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;

/**
 * An interface required for table entity types. The {@link TableEntity} interface declares getter and setter methods
 * for the common entity properties, and <code>writeEntity</code> and <code>readEntity</code> methods for serialization
 * and deserialization of all entity properties using a property map. Create classes implementing {@link TableEntity} to
 * customize property storage, retrieval, serialization and deserialization, and to provide additional custom logic for
 * a table entity.
 * <p>
 * The Storage client library includes two implementations of {@link TableEntity} that provide for simple property
 * access and serialization:
 * <p>
 * {@link DynamicTableEntity} implements {@link TableEntity} and provides a simple property map to store and retrieve
 * properties. Use a {@link DynamicTableEntity} for simple access to entity properties when only a subset of properties
 * are returned (for example, by a select clause in a query), or for when your query can return multiple entity types
 * with different properties. You can also use this type to perform bulk table updates of heterogeneous entities without
 * losing property information.
 * <p>
 * {@link TableServiceEntity} is an implementation of {@link TableEntity} that uses reflection-based serialization and
 * deserialization behavior in its <code>writeEntity</code> and <code>readEntity</code> methods.
 * {@link TableServiceEntity}-derived classes with methods that follow a convention for types and naming are serialized
 * and deserialized automatically.
 * <p>
 * Any class that implements {@link TableEntity} can take advantage of the automatic reflection-based serialization and
 * deserialization behavior in {@link TableServiceEntity} by invoking the static methods
 * <code>TableServiceEntity.readEntityWithReflection</code> in <code>readEntity</code> and
 * <code>TableServiceEntity.writeEntityWithReflection</code> in <code>writeEntity</code>. The class must provide methods
 * that follow the type and naming convention to be serialized and deserialized automatically. When both a getter method
 * and setter method are found for a given property name and data type, then the appropriate method is invoked
 * automatically to serialize or deserialize the data. The reflection code looks for getter and setter methods in pairs
 * of the form
 * <p>
 * <code>public <em>type</em> get<em>PropertyName</em>() { ... }</code>
 * <p>
 * and
 * <p>
 * <code>public void set<em>PropertyName</em>(<em>type</em> parameter) { ... }</code>
 * <p>
 * where <em>PropertyName</em> is a property name for the table entity, and <em>type</em> is a Java type compatible with
 * the EDM data type of the property. See the table in the class description for {@link TableServiceEntity} for a map of
 * property types to their Java equivalents. The {@link StoreAs} annotation may be applied with a <code>name</code>
 * attribute to specify a property name for reflection on getter and setter methods that do not follow the property name
 * convention. Method names and the <code>name</code> attribute of {@link StoreAs} annotations are case sensitive for
 * matching property names with reflection. Use the {@link Ignore} annotation to prevent methods from being used by
 * reflection for automatic serialization and deserialization. Note that the names "PartitionKey", "RowKey",
 * "Timestamp", and "Etag" are reserved and will be ignored if set with the {@link StoreAs} annotation in a subclass
 * that uses the reflection methods.
 * <p>
 * 
 * @see TableServiceEntity
 * @see DynamicTableEntity
 */
public interface TableEntity {

    /**
     * Gets the ETag value to verify for the entity. This value is used to determine if the table entity has changed 
     * since it was last read from Microsoft Azure storage. The client cannot update this value on the service.
     * 
     * @return
     *         A <code>String</code> which represents the ETag for the entity.
     */
    public String getEtag();

    /**
     * Gets the PartitionKey value for the entity.
     * 
     * @return
     *         A <code>String</code> which represents the PartitionKey value for the entity.
     */
    public String getPartitionKey();

    /**
     * Gets the RowKey value for the entity.
     * 
     * @return
     *         A <code>String</code> which represents the RowKey value for the entity.
     */
    public String getRowKey();

    /**
     * Gets the Timestamp for the entity. The server manages the value of Timestamp, which cannot be modified. 
     * 
     * @return
     *         A <code>java.util.Date</code> object which represents the Timestamp value for the entity.
     */
    public Date getTimestamp();

    /**
     * Populates an instance of the object implementing {@link TableEntity} using the specified properties parameter,
     * which represents a map of <code>String</code> property names to {@link EntityProperty} data typed values.
     * 
     * @param properties
     *            The <code>java.util.HashMap</code> of <code>String</code> to {@link EntityProperty} data typed values
     *            to use to populate the table entity instance.
     * @param opContext
     *            An {@link OperationContext} object used to track the execution of the operation.
     * 
     * @throws StorageException
     *             If an error occurs during the operation.
     */
    public void readEntity(HashMap<String, EntityProperty> properties, OperationContext opContext)
            throws StorageException;

    /**
     * Sets the ETag value to verify for the entity. This value is used to determine if the table entity has changed 
     * since it was last read from Microsoft Azure storage. The client cannot update this value on the service.
     * 
     * @param etag
     *            A <code>String</code> which specifies the ETag to set for the entity.
     */
    public void setEtag(String etag);

    /**
     * Sets the PartitionKey value for the entity.
     * 
     * @param partitionKey
     *            A <code>String</code> which specifies the PartitionKey value to set for the entity.
     */
    public void setPartitionKey(String partitionKey);

    /**
     * Sets the RowKey value for the entity.
     * 
     * @param rowKey
     *            A <code>String</code> which specifies the RowKey value to set for the entity.
     */
    public void setRowKey(String rowKey);

    /**
     * Sets the Timestamp value for the entity. Note that timestamp is a read-only property on the service and should
     * not be set by the user.
     * 
     * @param timeStamp
     *            A <code>java.util.Date</code> which specifies the Timestamp value to set for the entity.
     */
    public void setTimestamp(Date timeStamp);

    /**
     * Returns a map of <code>String</code> property names to {@link EntityProperty} data typed values
     * that represents the serialized content of the table entity instance.
     * 
     * @param opContext
     *            An {@link OperationContext} object used to track the execution of the operation.
     * 
     * @return
     *         A <code>java.util.HashMap</code> of <code>String</code> property names to {@link EntityProperty} data
     *         typed values representing the properties of the table entity.
     * 
     * @throws StorageException
     *             If an error occurs during the operation.
     */
    public HashMap<String, EntityProperty> writeEntity(OperationContext opContext) throws StorageException;
}
