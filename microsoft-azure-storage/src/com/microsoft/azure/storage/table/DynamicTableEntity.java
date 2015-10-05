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

import java.util.HashMap;

import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;

/**
 * A {@link TableEntity} type which allows callers direct access to the property map of the entity. This class extends
 * {@link TableServiceEntity} to eliminate the use of reflection for serialization and deserialization.
 * 
 */
public class DynamicTableEntity extends TableServiceEntity {
    private HashMap<String, EntityProperty> properties = new HashMap<String, EntityProperty>();

    /**
     * Nullary default constructor.
     */
    public DynamicTableEntity() {
        // Empty constructor.
    }

    /**
     * Initializes a new instance of the {@link DynamicTableEntity} class with the specified partition key and row key.
     * 
     * @param partitionKey
     *            A <code>String</code> which represents the partition key of the {@link DynamicTableEntity} to be initialized.
     * @param rowKey
     *            A <code>String</code> which represents the row key of the {@link DynamicTableEntity} to be initialized.
     */
    public DynamicTableEntity(String partitionKey, String rowKey) {
        super(partitionKey, rowKey);
    }

    /**
     * Constructs a {@link DynamicTableEntity} instance using the specified property map.
     * 
     * @param properties
     *            A <code>java.util.HashMap</code> containing a map of <code>String</code> property names to
     *            {@link EntityProperty} data typed values to store in the new {@link DynamicTableEntity}.
     */
    public DynamicTableEntity(final HashMap<String, EntityProperty> properties) {
        this.setProperties(properties);
    }

    /**
     * Initializes a new instance of the {@link DynamicTableEntity} class with the specified partition key and row key.
     * 
     * @param partitionKey
     *            A <code>String</code> which represents the partition key of the {@link DynamicTableEntity} to be initialized.
     * @param rowKey
     *            A <code>String</code> which represents the row key of the {@link DynamicTableEntity} to be initialized.
     * @param properties
     *            A <code>java.util.HashMap</code> containing a map of <code>String</code> property names to
     *            {@link EntityProperty} data typed values to store in the new {@link DynamicTableEntity}.
     */
    public DynamicTableEntity(String partitionKey, String rowKey, final HashMap<String, EntityProperty> properties) {
        this(partitionKey, rowKey, null /* etag */, properties);
    }

    /**
     * Initializes a new instance of the {@link DynamicTableEntity} class with the specified partition key and row key.
     * 
     * @param partitionKey
     *            A <code>String</code> which represents the partition key of the {@link DynamicTableEntity} to be initialized.
     * @param rowKey
     *            A <code>String</code> which represents the row key of the {@link DynamicTableEntity} to be initialized.
     * @param etag
     *            The ETag of the {@link DynamicTableEntity} to be initialized. This value is used to determine if the table 
     *            entity has changed since it was last read from Microsoft Azure storage. The client cannot update this value 
     *            on the service.
     * @param properties
     *            A <code>java.util.HashMap</code> containing a map of <code>String</code> property names to
     *            {@link EntityProperty} data typed values to store in the new {@link DynamicTableEntity}.
     */
    public DynamicTableEntity(String partitionKey, String rowKey, String etag,
            final HashMap<String, EntityProperty> properties) {
        super(partitionKey, rowKey);

        this.etag = etag;
        this.setProperties(properties);
    }

    /**
     * Gets the property map for this {@link DynamicTableEntity} instance.
     *  
     * @return
     *         A <code>java.util.HashMap</code> containing the map of <code>String</code> property names to
     *         {@link EntityProperty} data typed values for this {@link DynamicTableEntity} instance.
     */
    public HashMap<String, EntityProperty> getProperties() {
        return this.properties;
    }

    /**
     * Populates this {@link DynamicTableEntity} instance using the specified map of property names to
     * {@link EntityProperty} data typed values.
     * 
     * @param properties
     *            The <code>java.util.HashMap</code> of <code>String</code> property names to {@link EntityProperty}
     *            data typed values to store in this {@link DynamicTableEntity} instance.
     * @param opContext
     *            An {@link OperationContext} object used to track the execution of the operation.
     */
    @Override
    public void readEntity(final HashMap<String, EntityProperty> properties, final OperationContext opContext) {
        this.setProperties(properties);
    }

    /**
     * Sets the property map for this {@link DynamicTableEntity} instance.
     * 
     * @param properties
     *            A <code>java.util.HashMap</code> containing the map of <code>String</code> property names to
     *            {@link EntityProperty} data typed values to set in this {@link DynamicTableEntity} instance.
     */
    public void setProperties(final HashMap<String, EntityProperty> properties) {
        this.properties = properties;
    }

    /**
     * Returns the map of property names to {@link EntityProperty} data values from this {@link DynamicTableEntity}
     * instance.
     * 
     * @param opContext
     *            An {@link OperationContext} object used to track the execution of the operation.
     * 
     * @return
     *         A <code>java.util.HashMap</code> containing the map of <code>String</code> property names to
     *         {@link EntityProperty} data typed values stored in this {@link DynamicTableEntity} instance.
     *         
     * @throws StorageException
     *             if a Storage service error occurs.
     */
    @Override
    public HashMap<String, EntityProperty> writeEntity(final OperationContext opContext) throws StorageException {
        return this.getProperties();
    }
}
