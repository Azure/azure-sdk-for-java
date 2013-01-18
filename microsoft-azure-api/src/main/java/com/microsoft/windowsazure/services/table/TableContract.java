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
package com.microsoft.windowsazure.services.table;

import com.microsoft.windowsazure.services.core.FilterableService;
import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.table.models.BatchOperations;
import com.microsoft.windowsazure.services.table.models.BatchResult;
import com.microsoft.windowsazure.services.table.models.DeleteEntityOptions;
import com.microsoft.windowsazure.services.table.models.Entity;
import com.microsoft.windowsazure.services.table.models.GetEntityResult;
import com.microsoft.windowsazure.services.table.models.GetServicePropertiesResult;
import com.microsoft.windowsazure.services.table.models.GetTableResult;
import com.microsoft.windowsazure.services.table.models.InsertEntityResult;
import com.microsoft.windowsazure.services.table.models.QueryEntitiesOptions;
import com.microsoft.windowsazure.services.table.models.QueryEntitiesResult;
import com.microsoft.windowsazure.services.table.models.QueryTablesOptions;
import com.microsoft.windowsazure.services.table.models.QueryTablesResult;
import com.microsoft.windowsazure.services.table.models.ServiceProperties;
import com.microsoft.windowsazure.services.table.models.TableServiceOptions;
import com.microsoft.windowsazure.services.table.models.UpdateEntityResult;

/**
 * Defines the methods available on the Windows Azure Table storage service. Construct an object instance implementing
 * <code>TableContract</code> with one of the static <em>create</em> methods on {@link TableService}. These methods
 * associate a <code>Configuration</code> with the implementation, so the methods on the instance of
 * <code>TableContract</code> all work with a particular storage account.
 */
public interface TableContract extends FilterableService<TableContract> {
    /**
     * Gets the properties of a storage account’s Table service, including Windows Azure Storage Analytics.
     * 
     * @return
     *         A {@link GetServicePropertiesResult} reference to the Table service properties.
     * 
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    GetServicePropertiesResult getServiceProperties() throws ServiceException;

    /**
     * Gets the properties of a storage account’s Table service, including Windows Azure Storage Analytics, using the
     * specified options.
     * <p>
     * Use the {@link TableServiceOptions options} parameter to specify options for the request.
     * 
     * @param options
     *            A {@link TableServiceOptions} instance containing options for the request.
     * @return
     *         A {@link GetServicePropertiesResult} reference to the Table service properties.
     * 
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    GetServicePropertiesResult getServiceProperties(TableServiceOptions options) throws ServiceException;

    /**
     * Sets the properties of a storage account’s Table service, including Windows Azure Storage Analytics.
     * 
     * @param serviceProperties
     *            A {@link ServiceProperties} instance containing the Table service properties to set.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    void setServiceProperties(ServiceProperties serviceProperties) throws ServiceException;

    /**
     * Sets the properties of a storage account’s Table service, including Windows Azure Storage Analytics, using the
     * specified options.
     * <p>
     * Use the {@link TableServiceOptions options} parameter to specify options for the request.
     * 
     * @param serviceProperties
     *            A {@link ServiceProperties} instance containing the Table service properties to set.
     * @param options
     *            A {@link TableServiceOptions} instance containing options for the request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    void setServiceProperties(ServiceProperties serviceProperties, TableServiceOptions options) throws ServiceException;

    /**
     * Creates a table with the specified name in the storage account.
     * <p>
     * Table names must be unique within a storage account, and must conform to these rules:
     * <ul>
     * <li>Table names may contain only alphanumeric characters.</li>
     * <li>Table names cannot begin with a numeric character.</li>
     * <li>Table names are case-insensitive.</li>
     * <li>Table names must be from 3 to 63 characters long.</li>
     * </ul>
     * <p>
     * These rules are also described by the regular expression "^[A-Za-z][A-Za-z0-9]{2,62}$".
     * <p>
     * Table names preserve the case with which they were created, but are case-insensitive when used.
     * 
     * @param table
     *            A {@link String} containing the name of the table to create.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    void createTable(String table) throws ServiceException;

    /**
     * Creates a table with the specified name in the storage account, using the specified options.
     * <p>
     * Use the {@link TableServiceOptions options} parameter to specify options for the request.
     * <p>
     * Table names must be unique within a storage account, and must conform to these rules:
     * <ul>
     * <li>Table names may contain only alphanumeric characters.</li>
     * <li>Table names cannot begin with a numeric character.</li>
     * <li>Table names are case-insensitive.</li>
     * <li>Table names must be from 3 to 63 characters long.</li>
     * </ul>
     * <p>
     * These rules are also described by the regular expression "^[A-Za-z][A-Za-z0-9]{2,62}$".
     * <p>
     * Table names preserve the case with which they were created, but are case-insensitive when used.
     * 
     * @param table
     *            A {@link String} containing the name of the table to create.
     * @param options
     *            A {@link TableServiceOptions} instance containing options for the request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    void createTable(String table, TableServiceOptions options) throws ServiceException;

    /**
     * Deletes the specified table and any data it contains from the storage account.
     * <p>
     * When a table is successfully deleted, it is immediately marked for deletion and is no longer accessible to
     * clients. The table is later removed from the Table service during garbage collection.
     * <p>
     * Note that deleting a table is likely to take at least 40 seconds to complete. If an operation is attempted
     * against the table while it is being deleted, the service returns status code 409 (Conflict), with additional
     * error information indicating that the table is being deleted. This causes a {@link ServiceException} to be thrown
     * in the context of the client request.
     * 
     * @param table
     *            A {@link String} containing the name of the table to delete.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    void deleteTable(String table) throws ServiceException;

    /**
     * Deletes the specified table and any data it contains from the storage account, using the specified options.
     * <p>
     * Use the {@link TableServiceOptions options} parameter to specify options for the request.
     * <p>
     * When a table is successfully deleted, it is immediately marked for deletion and is no longer accessible to
     * clients. The table is later removed from the Table service during garbage collection.
     * <p>
     * Note that deleting a table is likely to take at least 40 seconds to complete. If an operation is attempted
     * against the table while it is being deleted, the service returns status code 409 (Conflict), with additional
     * error information indicating that the table is being deleted. This causes a {@link ServiceException} to be thrown
     * in the context of the client request.
     * 
     * @param table
     *            A {@link String} containing the name of the table to delete.
     * @param options
     *            A {@link TableServiceOptions} instance containing options for the request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    void deleteTable(String table, TableServiceOptions options) throws ServiceException;

    /**
     * Gets the specified table entry from the list of tables in the storage account.
     * 
     * @param table
     *            A {@link String} containing the name of the table to retrieve.
     * @return
     *         A {@link GetTableResult} instance containing the table entry returned.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    GetTableResult getTable(String table) throws ServiceException;

    /**
     * Gets the specified table entry from the list of tables in the storage account, using the specified options.
     * <p>
     * Use the {@link TableServiceOptions options} parameter to specify options for the request.
     * 
     * @param table
     *            A {@link String} containing the name of the table to retrieve.
     * @param options
     *            A {@link TableServiceOptions} instance containing options for the request.
     * @return
     *         A {@link GetTableResult} instance containing the table entry returned.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    GetTableResult getTable(String table, TableServiceOptions options) throws ServiceException;

    /**
     * Gets a list of tables in the storage account.
     * 
     * @return
     *         A {@link QueryTablesResult} instance containing the list of table entries returned.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    QueryTablesResult queryTables() throws ServiceException;

    /**
     * Gets a list of tables in the storage account, using the specified options.
     * <p>
     * Use the {@link QueryTablesOptions options} parameter to specify options for the request, such as a filter to
     * limit results to tables with certain properties, the next table name continuation token to use to resume the
     * query tables request from, and a prefix string to match table names with.
     * 
     * @param options
     *            A {@link QueryTablesOptions} instance containing options for the request.
     * @return
     *         A {@link QueryTablesResult} instance containing the list of table entries returned.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    QueryTablesResult queryTables(QueryTablesOptions options) throws ServiceException;

    /**
     * Inserts an entity into a table.
     * 
     * @param table
     *            A {@link String} containing the name of the table to insert the entity into.
     * @param entity
     *            An {@link Entity} instance containing the entity data to insert in the table.
     * @return
     *         An {@link InsertEntityResult} containing the entity inserted in the table.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    InsertEntityResult insertEntity(String table, Entity entity) throws ServiceException;

    /**
     * Inserts an entity into a table, using the specified options.
     * 
     * @param table
     *            A {@link String} containing the name of the table to insert the entity into.
     * @param entity
     *            An {@link Entity} instance containing the entity data to insert in the table.
     * @param options
     *            A {@link TableServiceOptions} instance containing options for the request.
     * @return
     *         An {@link InsertEntityResult} containing the entity inserted in the table.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    InsertEntityResult insertEntity(String table, Entity entity, TableServiceOptions options) throws ServiceException;

    /**
     * Updates an entity in a table. The entity data is completely replaced with the data in the <em>entity</em>
     * parameter.
     * 
     * @param table
     *            A {@link String} containing the name of the table that contains the entity to update.
     * @param entity
     *            An {@link Entity} instance containing the entity to update in the table.
     * @return
     *         An {@link UpdateEntityResult} containing the ETag of the updated entity.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    UpdateEntityResult updateEntity(String table, Entity entity) throws ServiceException;

    /**
     * Updates an entity in a table, using the specified options. The entity data is completely replaced with the data
     * in the <em>entity</em> parameter.
     * 
     * @param table
     *            A {@link String} containing the name of the table that contains the entity to update.
     * @param entity
     *            An {@link Entity} instance containing the entity to update in the table.
     * @param options
     *            A {@link TableServiceOptions} instance containing options for the request.
     * @return
     *         An {@link UpdateEntityResult} containing the ETag of the updated entity.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    UpdateEntityResult updateEntity(String table, Entity entity, TableServiceOptions options) throws ServiceException;

    /**
     * Merges entity data into an existing entity in a table. Property values in the existing entity are overwritten
     * with matching properties in the <em>entity</em> parameter. Properties in the <em>entity</em> parameter that are
     * not present in the existing entity are added to it.
     * 
     * @param table
     *            A {@link String} containing the name of the table that contains the entity to merge.
     * @param entity
     *            An {@link Entity} instance containing the entity data to merge into the existing entity.
     * @return
     *         An {@link UpdateEntityResult} containing the ETag of the modified entity.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    UpdateEntityResult mergeEntity(String table, Entity entity) throws ServiceException;

    /**
     * Merges entity data into an existing entity in a table, using the specified options. Property values in the
     * existing entity are overwritten with matching properties in the <em>entity</em> parameter. Properties in the
     * <em>entity</em> parameter that are not present in the existing entity are added to it.
     * 
     * @param table
     *            A {@link String} containing the name of the table that contains the entity to merge.
     * @param entity
     *            An {@link Entity} instance containing the entity data to merge into the existing entity.
     * @param options
     *            A {@link TableServiceOptions} instance containing options for the request.
     * @return
     *         An {@link UpdateEntityResult} containing the ETag of the modified entity.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    UpdateEntityResult mergeEntity(String table, Entity entity, TableServiceOptions options) throws ServiceException;

    /**
     * Inserts or replaces an entity in a table. If the table does not contain an entity with a matching primary key, it
     * is inserted. Otherwise, the entity data is completely replaced with the data in the <em>entity</em> parameter.
     * 
     * @param table
     *            A {@link String} containing the name of the table that contains the entity to insert or replace.
     * @param entity
     *            An {@link Entity} instance containing the entity data to insert or replace in the table.
     * @return
     *         An {@link UpdateEntityResult} containing the ETag of the modified entity.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    UpdateEntityResult insertOrReplaceEntity(String table, Entity entity) throws ServiceException;

    /**
     * Inserts or replaces an entity in a table, using the specified options. If the table does not contain an entity
     * with a matching primary key, it is inserted. Otherwise, the entity data is completely replaced with the data in
     * the <em>entity</em> parameter.
     * 
     * @param table
     *            A {@link String} containing the name of the table that contains the entity to insert or replace.
     * @param entity
     *            An {@link Entity} instance containing the entity data to insert or replace in the table.
     * @param options
     *            A {@link TableServiceOptions} instance containing options for the request.
     * @return
     *         An {@link UpdateEntityResult} containing the ETag of the modified entity.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    UpdateEntityResult insertOrReplaceEntity(String table, Entity entity, TableServiceOptions options)
            throws ServiceException;

    /**
     * Inserts or merges an entity in a table. If the table does not contain an entity with a matching primary key, it
     * is inserted. Otherwise, property values in the existing entity are overwritten with matching properties in the
     * <em>entity</em> parameter. Properties in the <em>entity</em> parameter that are not present in the existing
     * entity are added to it.
     * 
     * @param table
     *            A {@link String} containing the name of the table that contains the entity to insert or merge.
     * @param entity
     *            An {@link Entity} instance containing the entity data to insert or merge in the table.
     * @return
     *         An {@link UpdateEntityResult} containing the ETag of the modified entity.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    UpdateEntityResult insertOrMergeEntity(String table, Entity entity) throws ServiceException;

    /**
     * Inserts or merges an entity in a table, using the specified options. If the table does not contain an entity with
     * a matching primary key, it is inserted. Otherwise, property values in the existing entity are overwritten with
     * matching properties in the <em>entity</em> parameter. Properties in the <em>entity</em> parameter that are not
     * present in the existing entity are added to it.
     * 
     * @param table
     *            A {@link String} containing the name of the table that contains the entity to insert or merge.
     * @param entity
     *            An {@link Entity} instance containing the entity data to insert or merge in the table.
     * @param options
     *            A {@link TableServiceOptions} instance containing options for the request.
     * @return
     *         An {@link UpdateEntityResult} containing the ETag of the modified entity.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    UpdateEntityResult insertOrMergeEntity(String table, Entity entity, TableServiceOptions options)
            throws ServiceException;

    /**
     * Deletes an entity from a table.
     * 
     * @param table
     *            A {@link String} containing the name of the table to delete the entity from.
     * @param partitionKey
     *            A {@link String} containing the partition key of the entity to delete.
     * @param rowKey
     *            A {@link String} containing the row key of the entity to delete.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    void deleteEntity(String table, String partitionKey, String rowKey) throws ServiceException;

    /**
     * Deletes an entity from a table, using the specified options.
     * <p>
     * Use the {@link DeleteEntityOptions options} parameter to specify an ETag value that must match to delete the
     * entity.
     * 
     * @param table
     *            A {@link String} containing the name of the table to delete the entity from.
     * @param partitionKey
     *            A {@link String} containing the partition key of the entity to delete.
     * @param rowKey
     *            A {@link String} containing the row key of the entity to delete.
     * @param options
     *            A {@link DeleteEntityOptions} instance containing the ETag to match with the entity to delete.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    void deleteEntity(String table, String partitionKey, String rowKey, DeleteEntityOptions options)
            throws ServiceException;

    /**
     * Gets the specified entity.
     * 
     * @param table
     *            A {@link String} containing the name of the table to get the entity from.
     * @param partitionKey
     *            A {@link String} containing the partition key of the entity to get.
     * @param rowKey
     *            A {@link String} containing the row key of the entity to get.
     * @return
     *         A {@link GetEntityResult} instance containing the entity data returned in the server response.
     * @throws ServiceException
     */
    GetEntityResult getEntity(String table, String partitionKey, String rowKey) throws ServiceException;

    /**
     * Gets the specified entity, using the specified options.
     * 
     * @param table
     *            A {@link String} containing the name of the table to get the entity from.
     * @param partitionKey
     *            A {@link String} containing the partition key of the entity to get.
     * @param rowKey
     *            A {@link String} containing the row key of the entity to get.
     * @param options
     *            A {@link TableServiceOptions} instance containing options for the request.
     * @return
     *         A {@link GetEntityResult} instance containing the entity data returned in the server response.
     * @throws ServiceException
     */
    GetEntityResult getEntity(String table, String partitionKey, String rowKey, TableServiceOptions options)
            throws ServiceException;

    /**
     * Lists the entities in a table.
     * 
     * @param table
     *            A {@link String} containing the name of the table to retrieve the list of entities from.
     * @return
     *         A {@link QueryEntitiesResult} instance containing the server response to the batch request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    QueryEntitiesResult queryEntities(String table) throws ServiceException;

    /**
     * Lists the entities in a table that match the specified options.
     * <p>
     * Use the {@link QueryEntitiesOptions options} parameter to specify the next partition key and next row key
     * continuation tokens to use to resume the query entities request from, a collection of the property names to
     * include in the entities returned in the server response, a filter to limit results to entities with certain
     * property values, and a top count to limit the response to that number of the first matching results.
     * 
     * @param table
     *            A {@link String} containing the name of the table to retrieve the list of entities from.
     * @param options
     *            A {@link QueryEntitiesOptions} instance containing options for the request.
     * @return
     *         A {@link QueryEntitiesResult} instance containing the server response to the batch request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    QueryEntitiesResult queryEntities(String table, QueryEntitiesOptions options) throws ServiceException;

    /**
     * Submits multiple entity operations in the same table and partition group as a single transaction. Multiple insert
     * entity, update entity, merge entity, delete entity, insert or replace entity, and insert or merge entity
     * operations are supported within a single transaction.
     * 
     * @param operations
     *            A {@link BatchOperations} instance containing the list of operations to send as a single transaction.
     * @return
     *         A {@link BatchResult} instance containing the server response to the batch request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    BatchResult batch(BatchOperations operations) throws ServiceException;

    /**
     * Submits multiple entity operations in the same table and partition group as a single transaction, using the
     * specified options. Multiple insert entity, update entity, merge entity, delete entity, insert or replace entity,
     * and insert or merge entity operations are supported within a single transaction.
     * <p>
     * Use the {@link TableServiceOptions options} parameter to specify options for the request.
     * 
     * @param operations
     *            A {@link BatchOperations} instance containing the list of operations to send as a single transaction.
     * @param options
     *            A {@link TableServiceOptions} instance containing options for the request.
     * @return
     *         A {@link BatchResult} instance containing the server response to the batch request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    BatchResult batch(BatchOperations operations, TableServiceOptions options) throws ServiceException;
}
