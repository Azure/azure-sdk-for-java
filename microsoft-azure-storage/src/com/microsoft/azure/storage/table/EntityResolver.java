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

import com.microsoft.azure.storage.StorageException;

/**
 * An interface to perform client side projection on a retrieved entity. An {@link EntityResolver} instance must
 * implement a <code>resolve</code> method projecting the entity data represented by the parameters passed in as a new
 * instance of the type specified by the type parameter.
 * <p>
 * This interface is useful for converting directly from table entity data to a client object type without requiring a
 * separate table entity class type that deserializes every property individually. For example, a client can perform a
 * client side projection of a <em>Customer</em> entity by simply returning the <code>String</code> for the
 * <em>CustomerName</em> property of each entity. The result of this projection will be a collection of
 * <code>String</code>s containing each customer name.
 * 
 * @param <T>
 *            The type of the object that the resolver produces.
 */
public interface EntityResolver<T> {
    /**
     * Returns a reference to a new object instance of type <code>T</code> containing a projection of the specified
     * table entity data.
     * 
     * @param partitionKey
     *            A <code>String</code> containing the PartitionKey value for the entity.
     * @param rowKey
     *            A <code>String</code> containing the RowKey value for the entity.
     * @param timeStamp
     *            A <code>java.util.Date</code> containing the Timestamp value for the entity.
     * @param properties
     *            The <code>java.util.HashMap</code> of <code>String</code> property names to {@link EntityProperty}
     *            data type and value pairs representing the table entity data.
     * @param etag
     *            A <code>String</code> containing the Etag for the entity.
     * @return
     *         A reference to an object instance of type <code>T</code> constructed as a projection of the table entity
     *         parameters.
     * @throws StorageException
     *             if an error occurs during the operation.
     */
    T resolve(String partitionKey, String rowKey, Date timeStamp, HashMap<String, EntityProperty> properties,
            String etag) throws StorageException;
}
