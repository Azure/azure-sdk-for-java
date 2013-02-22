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

package com.microsoft.windowsazure.services.table.client;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.text.ParseException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.microsoft.windowsazure.services.core.storage.Constants;
import com.microsoft.windowsazure.services.core.storage.OperationContext;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.ExecutionEngine;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.StorageOperation;

/**
 * A class which represents a single table operation.
 * <p>
 * Use the static factory methods to construct {@link TableOperation} instances for operations on tables that insert,
 * update, merge, delete, replace or retrieve table entities. To execute a {@link TableOperation} instance, call the
 * <code>execute</code> method on a {@link CloudTableClient} instance. A {@link TableOperation} may be executed directly
 * or as part of a {@link TableBatchOperation}. If a {@link TableOperation} returns an entity result, it is stored in
 * the corresponding {@link TableResult} returned by the <code>execute</code> method.
 * 
 */
public class TableOperation {
    /**
     * A static factory method returning a {@link TableOperation} instance to delete the specified entity from Windows
     * Azure storage. To execute this {@link TableOperation} on a given table, call the
     * {@link CloudTableClient#execute(String, TableOperation)} method on a {@link CloudTableClient} instance with the
     * table name and the {@link TableOperation} as arguments.
     * 
     * @param entity
     *            The object instance implementing {@link TableEntity} to associate with the operation.
     * @return
     *         A new {@link TableOperation} instance to insert the table entity.
     */
    public static TableOperation delete(final TableEntity entity) {
        Utility.assertNotNull("Entity", entity);
        Utility.assertNotNullOrEmpty("Entity Etag", entity.getEtag());
        return new TableOperation(entity, TableOperationType.DELETE);
    }

    /**
     * A static factory method returning a {@link TableOperation} instance to insert the specified entity into Windows
     * Azure storage. To execute this {@link TableOperation} on a given table, call the
     * {@link CloudTableClient#execute(String, TableOperation)} method on a {@link CloudTableClient} instance with the
     * table name and the {@link TableOperation} as arguments.
     * 
     * @param entity
     *            The object instance implementing {@link TableEntity} to associate with the operation.
     * @return
     *         A new {@link TableOperation} instance to insert the table entity.
     */
    public static TableOperation insert(final TableEntity entity) {
        Utility.assertNotNull("Entity", entity);
        return new TableOperation(entity, TableOperationType.INSERT);
    }

    /**
     * A static factory method returning a {@link TableOperation} instance to merge the specified entity into Windows
     * Azure storage, or insert it if it does not exist. To execute this {@link TableOperation} on a given table, call
     * the {@link CloudTableClient#execute(String, TableOperation)} method on a {@link CloudTableClient} instance with
     * the table name and the {@link TableOperation} as arguments.
     * 
     * @param entity
     *            The object instance implementing {@link TableEntity} to associate with the operation.
     * @return
     *         A new {@link TableOperation} instance for inserting or merging the table entity.
     */
    public static TableOperation insertOrMerge(final TableEntity entity) {
        Utility.assertNotNull("Entity", entity);
        return new TableOperation(entity, TableOperationType.INSERT_OR_MERGE);
    }

    /**
     * A static factory method returning a {@link TableOperation} instance to replace the specified entity in Windows
     * Azure storage, or insert it if it does not exist. To execute this {@link TableOperation} on a given table, call
     * the {@link CloudTableClient#execute(String, TableOperation)} method on a {@link CloudTableClient} instance with
     * the table name and the {@link TableOperation} as arguments.
     * 
     * @param entity
     *            The object instance implementing {@link TableEntity} to associate with the operation.
     * @return
     *         A new {@link TableOperation} instance for inserting or replacing the table entity.
     */
    public static TableOperation insertOrReplace(final TableEntity entity) {
        Utility.assertNotNull("Entity", entity);
        return new TableOperation(entity, TableOperationType.INSERT_OR_REPLACE);
    }

    /**
     * A static factory method returning a {@link TableOperation} instance to merge the specified table entity into
     * Windows Azure storage. To execute this {@link TableOperation} on a given table, call the
     * {@link CloudTableClient#execute(String, TableOperation)} method on a {@link CloudTableClient} instance with the
     * table name and the {@link TableOperation} as arguments.
     * 
     * @param entity
     *            The object instance implementing {@link TableEntity} to associate with the operation.
     * @return
     *         A new {@link TableOperation} instance for merging the table entity.
     */
    public static TableOperation merge(final TableEntity entity) {
        Utility.assertNotNull("Entity", entity);
        Utility.assertNotNullOrEmpty("Entity Etag", entity.getEtag());
        return new TableOperation(entity, TableOperationType.MERGE);
    }

    /**
     * A static factory method returning a {@link TableOperation} instance to retrieve the specified table entity and
     * return it as the specified type. To execute this {@link TableOperation} on a given table, call the
     * {@link CloudTableClient#execute(String, TableOperation)} method on a {@link CloudTableClient} instance with the
     * table name and the {@link TableOperation} as arguments.
     * 
     * @param partitionKey
     *            A <code>String</code> containing the PartitionKey value for the entity to retrieve.
     * @param rowKey
     *            A <code>String</code> containing the RowKey value for the entity to retrieve.
     * @param clazzType
     *            The class type of the table entity object to retrieve.
     * @return
     *         A new {@link TableOperation} instance for retrieving the table entity.
     */
    public static TableOperation retrieve(final String partitionKey, final String rowKey,
            final Class<? extends TableEntity> clazzType) {
        final QueryTableOperation retOp = new QueryTableOperation(partitionKey, rowKey);
        retOp.setClazzType(clazzType);
        return retOp;
    }

    /**
     * A static factory method returning a {@link TableOperation} instance to retrieve the specified table entity and
     * return a projection of it using the specified resolver. To execute this {@link TableOperation} on a given table,
     * call the {@link CloudTableClient#execute(String, TableOperation)} method on a {@link CloudTableClient} instance
     * with the table name and the {@link TableOperation} as arguments.
     * 
     * @param partitionKey
     *            A <code>String</code> containing the PartitionKey value for the entity to retrieve.
     * @param rowKey
     *            A <code>String</code> containing the RowKey value for the entity to retrieve.
     * @param resolver
     *            The implementation of {@link EntityResolver} to use to project the result entity as type T.
     * @return
     *         A new {@link TableOperation} instance for retrieving the table entity.
     */
    public static TableOperation retrieve(final String partitionKey, final String rowKey,
            final EntityResolver<?> resolver) {
        final QueryTableOperation retOp = new QueryTableOperation(partitionKey, rowKey);
        retOp.setResolver(resolver);
        return retOp;
    }

    /**
     * A static factory method returning a {@link TableOperation} instance to replace the specified table entity. To
     * execute this {@link TableOperation} on a given table, call the
     * {@link CloudTableClient#execute(String, TableOperation)} method on a {@link CloudTableClient} instance with the
     * table name and the {@link TableOperation} as arguments.
     * 
     * @param entity
     *            The object instance implementing {@link TableEntity} to associate with the operation.
     * @return
     *         A new {@link TableOperation} instance for replacing the table entity.
     */
    public static TableOperation replace(final TableEntity entity) {
        Utility.assertNotNullOrEmpty("Entity Etag", entity.getEtag());
        return new TableOperation(entity, TableOperationType.REPLACE);
    }

    /**
     * The table entity instance associated with the operation.
     */
    TableEntity entity;

    /**
     * The {@link TableOperationType} enumeration value for the operation type.
     */
    TableOperationType opType = null;

    /**
     * Nullary Default Constructor.
     */
    protected TableOperation() {
        // empty ctor
    }

    /**
     * Reserved for internal use. Constructs a {@link TableOperation} with the specified table entity and operation
     * type.
     * 
     * @param entity
     *            The object instance implementing {@link TableEntity} to associate with the operation.
     * @param opType
     *            The {@link TableOperationType} enumeration value for the operation type.
     */
    protected TableOperation(final TableEntity entity, final TableOperationType opType) {
        this.entity = entity;
        this.opType = opType;
    }

    /**
     * Reserved for internal use. Performs a delete operation on the specified table, using the specified
     * {@link TableRequestOptions} and {@link OperationContext}.
     * <p>
     * This method will invoke the <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd135727.aspx">Delete
     * Entity</a> REST API to execute this table operation, using the Table service endpoint and storage account
     * credentials in the {@link CloudTableClient} object.
     * 
     * @param client
     *            A {@link CloudTableClient} instance specifying the Table service endpoint, storage account
     *            credentials, and any additional query parameters.
     * @param tableName
     *            A <code>String</code> containing the name of the table.
     * @param options
     *            A {@link TableRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation.
     * 
     * @return
     *         A {@link TableResult} containing the results of executing the operation.
     * 
     * @throws StorageException
     *             if an error occurs in the storage operation.
     */
    private TableResult performDelete(final CloudTableClient client, final String tableName,
            final TableRequestOptions options, final OperationContext opContext) throws StorageException {
        final boolean isTableEntry = TableConstants.TABLES_SERVICE_TABLES_NAME.equals(tableName);
        final String tableIdentity = isTableEntry ? this.getEntity().writeEntity(opContext)
                .get(TableConstants.TABLE_NAME).getValueAsString() : null;

        if (!isTableEntry) {
            Utility.assertNotNullOrEmpty("Delete requires a valid ETag", this.getEntity().getEtag());
            Utility.assertNotNullOrEmpty("Delete requires a valid PartitionKey", this.getEntity().getPartitionKey());
            Utility.assertNotNullOrEmpty("Delete requires a valid RowKey", this.getEntity().getRowKey());
        }

        final StorageOperation<CloudTableClient, TableOperation, TableResult> impl = new StorageOperation<CloudTableClient, TableOperation, TableResult>(
                options) {
            @Override
            public TableResult execute(final CloudTableClient client, final TableOperation operation,
                    final OperationContext opContext) throws Exception {

                final HttpURLConnection request = TableRequest.delete(client.getTransformedEndPoint(opContext),
                        tableName, generateRequestIdentity(isTableEntry, tableIdentity, false), operation.getEntity()
                                .getEtag(), options.getTimeoutIntervalInMs(), null, options, opContext);

                client.getCredentials().signRequestLite(request, -1L, opContext);

                this.setResult(ExecutionEngine.processRequest(request, opContext));

                if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND
                        || this.getResult().getStatusCode() == HttpURLConnection.HTTP_CONFLICT) {
                    throw TableServiceException.generateTableServiceException(false, this.getResult(), operation,
                            request.getErrorStream());
                }

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                    throw TableServiceException.generateTableServiceException(true, this.getResult(), operation,
                            request.getErrorStream());
                }

                return operation.parseResponse(null, this.getResult().getStatusCode(), null, opContext);
            }
        };

        return ExecutionEngine.executeWithRetry(client, this, impl, options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Reserved for internal use. Performs an insert operation on the specified table, using the specified
     * {@link TableRequestOptions} and {@link OperationContext}.
     * <p>
     * This method will invoke the Insert Entity REST API to execute this table operation, using the Table service
     * endpoint and storage account credentials in the {@link CloudTableClient} object.
     * 
     * @param client
     *            A {@link CloudTableClient} instance specifying the Table service endpoint, storage account
     *            credentials, and any additional query parameters.
     * @param tableName
     *            A <code>String</code> containing the name of the table.
     * @param options
     *            A {@link TableRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation.
     * 
     * @return
     *         A {@link TableResult} containing the results of executing the operation.
     * 
     * @throws StorageException
     *             if an error occurs in the storage operation.
     */
    private TableResult performInsert(final CloudTableClient client, final String tableName,
            final TableRequestOptions options, final OperationContext opContext) throws StorageException {
        final boolean isTableEntry = TableConstants.TABLES_SERVICE_TABLES_NAME.equals(tableName);
        final String tableIdentity = isTableEntry ? this.getEntity().writeEntity(opContext)
                .get(TableConstants.TABLE_NAME).getValueAsString() : null;

        // Upserts need row key and partition key
        if (!isTableEntry && this.opType != TableOperationType.INSERT) {
            Utility.assertNotNullOrEmpty("Upserts require a valid PartitionKey", this.getEntity().getPartitionKey());
            Utility.assertNotNullOrEmpty("Upserts require a valid RowKey", this.getEntity().getRowKey());
        }

        final StorageOperation<CloudTableClient, TableOperation, TableResult> impl = new StorageOperation<CloudTableClient, TableOperation, TableResult>(
                options) {
            @Override
            public TableResult execute(final CloudTableClient client, final TableOperation operation,
                    final OperationContext opContext) throws Exception {
                final HttpURLConnection request = TableRequest.insert(client.getTransformedEndPoint(opContext),
                        tableName, generateRequestIdentity(isTableEntry, tableIdentity, false),
                        operation.opType != TableOperationType.INSERT ? operation.getEntity().getEtag() : null,
                        operation.opType.getUpdateType(), options.getTimeoutIntervalInMs(), null, options, opContext);

                client.getCredentials().signRequestLite(request, -1L, opContext);

                AtomPubParser.writeSingleEntityToStream(operation.getEntity(), isTableEntry, request.getOutputStream(),
                        opContext);

                this.setResult(ExecutionEngine.processRequest(request, opContext));
                if (operation.opType == TableOperationType.INSERT) {
                    if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_CONFLICT) {
                        throw TableServiceException.generateTableServiceException(false, this.getResult(), operation,
                                request.getErrorStream());
                    }

                    if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_CREATED) {
                        throw TableServiceException.generateTableServiceException(true, this.getResult(), operation,
                                request.getErrorStream());
                    }

                    InputStream inStream = request.getInputStream();
                    TableResult res = null;

                    try {
                        final XMLStreamReader xmlr = Utility.createXMLStreamReaderFromStream(inStream);
                        res = operation.parseResponse(xmlr, this.getResult().getStatusCode(), null, opContext);
                    }
                    finally {
                        inStream.close();
                    }

                    return res;
                }
                else {
                    if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_NO_CONTENT) {
                        return operation.parseResponse(null, this.getResult().getStatusCode(),
                                request.getHeaderField(TableConstants.HeaderConstants.ETAG), opContext);
                    }
                    else {
                        throw TableServiceException.generateTableServiceException(true, this.getResult(), operation,
                                request.getErrorStream());
                    }
                }
            }
        };

        return ExecutionEngine.executeWithRetry(client, this, impl, options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Reserved for internal use. Perform a merge operation on the specified table, using the specified
     * {@link TableRequestOptions} and {@link OperationContext}.
     * <p>
     * This method will invoke the Merge Entity REST API to execute this table operation, using the Table service
     * endpoint and storage account credentials in the {@link CloudTableClient} object.
     * 
     * @param client
     *            A {@link CloudTableClient} instance specifying the Table service endpoint, storage account
     *            credentials, and any additional query parameters.
     * @param tableName
     *            A <code>String</code> containing the name of the table.
     * @param options
     *            A {@link TableRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation.
     * 
     * @return
     *         A {@link TableResult} containing the results of executing the operation.
     * 
     * @throws StorageException
     *             if an error occurs in the storage operation.
     */
    private TableResult performMerge(final CloudTableClient client, final String tableName,
            final TableRequestOptions options, final OperationContext opContext) throws StorageException {
        Utility.assertNotNullOrEmpty("Merge requires a valid ETag", this.getEntity().getEtag());
        Utility.assertNotNullOrEmpty("Merge requires a valid PartitionKey", this.getEntity().getPartitionKey());
        Utility.assertNotNullOrEmpty("Merge requires a valid RowKey", this.getEntity().getRowKey());

        final StorageOperation<CloudTableClient, TableOperation, TableResult> impl = new StorageOperation<CloudTableClient, TableOperation, TableResult>(
                options) {
            @Override
            public TableResult execute(final CloudTableClient client, final TableOperation operation,
                    final OperationContext opContext) throws Exception {

                final HttpURLConnection request = TableRequest.merge(client.getTransformedEndPoint(opContext),
                        tableName, generateRequestIdentity(false, null, false), operation.getEntity().getEtag(),
                        options.getTimeoutIntervalInMs(), null, options, opContext);

                client.getCredentials().signRequestLite(request, -1L, opContext);

                AtomPubParser.writeSingleEntityToStream(operation.getEntity(), false, request.getOutputStream(),
                        opContext);

                this.setResult(ExecutionEngine.processRequest(request, opContext));

                if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND
                        || this.getResult().getStatusCode() == HttpURLConnection.HTTP_CONFLICT) {
                    throw TableServiceException.generateTableServiceException(false, this.getResult(), operation,
                            request.getErrorStream());
                }

                if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_NO_CONTENT) {
                    return operation.parseResponse(null, this.getResult().getStatusCode(),
                            request.getHeaderField(TableConstants.HeaderConstants.ETAG), opContext);
                }
                else {
                    throw TableServiceException.generateTableServiceException(true, this.getResult(), operation,
                            request.getErrorStream());
                }
            }
        };

        return ExecutionEngine.executeWithRetry(client, this, impl, options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Reserved for internal use. Perform an update operation on the specified table, using the specified
     * {@link TableRequestOptions} and {@link OperationContext}.
     * <p>
     * This method will invoke the Storage Service REST API to execute this table operation, using the Table service
     * endpoint and storage account credentials in the {@link CloudTableClient} object.
     * 
     * @param client
     *            A {@link CloudTableClient} instance specifying the Table service endpoint, storage account
     *            credentials, and any additional query parameters.
     * @param tableName
     *            A <code>String</code> containing the name of the table.
     * @param options
     *            A {@link TableRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation.
     * 
     * @return
     *         A {@link TableResult} containing the results of executing the operation.
     * 
     * @throws StorageException
     *             if an error occurs in the storage operation.
     */
    private TableResult performUpdate(final CloudTableClient client, final String tableName,
            final TableRequestOptions options, final OperationContext opContext) throws StorageException {
        Utility.assertNotNullOrEmpty("Update requires a valid ETag", this.getEntity().getEtag());
        Utility.assertNotNullOrEmpty("Update requires a valid PartitionKey", this.getEntity().getPartitionKey());
        Utility.assertNotNullOrEmpty("Update requires a valid RowKey", this.getEntity().getRowKey());
        final StorageOperation<CloudTableClient, TableOperation, TableResult> impl = new StorageOperation<CloudTableClient, TableOperation, TableResult>(
                options) {
            @Override
            public TableResult execute(final CloudTableClient client, final TableOperation operation,
                    final OperationContext opContext) throws Exception {

                final HttpURLConnection request = TableRequest.update(client.getTransformedEndPoint(opContext),
                        tableName, generateRequestIdentity(false, null, false), operation.getEntity().getEtag(),
                        options.getTimeoutIntervalInMs(), null, options, opContext);

                client.getCredentials().signRequestLite(request, -1L, opContext);

                AtomPubParser.writeSingleEntityToStream(operation.getEntity(), false, request.getOutputStream(),
                        opContext);

                this.setResult(ExecutionEngine.processRequest(request, opContext));

                if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND
                        || this.getResult().getStatusCode() == HttpURLConnection.HTTP_CONFLICT) {
                    throw TableServiceException.generateTableServiceException(false, this.getResult(), operation,
                            request.getErrorStream());
                }

                if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_NO_CONTENT) {
                    return operation.parseResponse(null, this.getResult().getStatusCode(),
                            request.getHeaderField(TableConstants.HeaderConstants.ETAG), opContext);
                }
                else {
                    throw TableServiceException.generateTableServiceException(true, this.getResult(), operation,
                            request.getErrorStream());
                }
            }
        };

        return ExecutionEngine.executeWithRetry(client, this, impl, options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Reserved for internal use. Execute this table operation on the specified table, using the specified
     * {@link TableRequestOptions} and {@link OperationContext}.
     * <p>
     * This method will invoke the Storage Service REST API to execute this table operation, using the Table service
     * endpoint and storage account credentials in the {@link CloudTableClient} object.
     * 
     * @param client
     *            A {@link CloudTableClient} instance specifying the Table service endpoint, storage account
     *            credentials, and any additional query parameters.
     * @param tableName
     *            A <code>String</code> containing the name of the table.
     * @param options
     *            A {@link TableRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation.
     * 
     * @return
     *         A {@link TableResult} containing the results of executing the operation.
     * 
     * @throws StorageException
     *             if an error occurs in the storage operation.
     */
    protected TableResult execute(final CloudTableClient client, final String tableName, TableRequestOptions options,
            OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new TableRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(client);
        Utility.assertNotNullOrEmpty("TableName", tableName);

        if (this.getOperationType() == TableOperationType.INSERT
                || this.getOperationType() == TableOperationType.INSERT_OR_MERGE
                || this.getOperationType() == TableOperationType.INSERT_OR_REPLACE) {
            return this.performInsert(client, tableName, options, opContext);
        }
        else if (this.getOperationType() == TableOperationType.DELETE) {
            return this.performDelete(client, tableName, options, opContext);
        }
        else if (this.getOperationType() == TableOperationType.MERGE) {
            return this.performMerge(client, tableName, options, opContext);
        }
        else if (this.getOperationType() == TableOperationType.REPLACE) {
            return this.performUpdate(client, tableName, options, opContext);
        }
        else if (this.getOperationType() == TableOperationType.RETRIEVE) {
            return ((QueryTableOperation) this).performRetrieve(client, tableName, options, opContext);
        }
        else {
            throw new IllegalArgumentException("Unknown table operation");
        }
    }

    /**
     * Reserved for internal use. Generates the request identity, consisting of the specified entry name, or the
     * PartitionKey and RowKey pair from the operation, to identify the operation target.
     * 
     * @param isSingleIndexEntry
     *            Pass <code>true</code> to use the specified <code>entryName</code> parameter, or <code>false</code> to
     *            use PartitionKey and RowKey values from the operation as the request identity.
     * @param entryName
     *            The entry name to use as the request identity if the <code>isSingleIndexEntry</code> parameter is
     *            <code>true</code>.
     * @param encodeKeys
     *            Pass <code>true</code> to url encode the partition & row keys
     * @return
     *         A <code>String</code> containing the formatted request identity string.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    protected String generateRequestIdentity(boolean isSingleIndexEntry, final String entryName, boolean encodeKeys)
            throws StorageException {
        if (isSingleIndexEntry) {
            return String.format("'%s'", entryName);
        }

        if (this.opType == TableOperationType.INSERT) {
            return Constants.EMPTY_STRING;
        }
        else {
            String pk = null;
            String rk = null;

            if (this.opType == TableOperationType.RETRIEVE) {
                final QueryTableOperation qOp = (QueryTableOperation) this;
                pk = qOp.getPartitionKey();
                rk = qOp.getRowKey();
            }
            else {
                pk = this.getEntity().getPartitionKey();
                rk = this.getEntity().getRowKey();
            }

            return String.format("%s='%s',%s='%s'", TableConstants.PARTITION_KEY, encodeKeys ? Utility.safeEncode(pk)
                    : pk, TableConstants.ROW_KEY, encodeKeys ? Utility.safeEncode(rk) : rk);
        }
    }

    /**
     * Reserved for internal use. Generates the request identity string for the specified table. The request identity
     * string combines the table name with the PartitionKey and RowKey from the operation to identify specific table
     * entities. This request identity is already UrlEncoded.
     * 
     * @param tableName
     *            A <code>String</code> containing the name of the table.
     * @return
     *         A <code>String</code> containing the formatted request identity string for the specified table.
     * @throws StorageException
     */
    protected String generateRequestIdentityWithTable(final String tableName) throws StorageException {
        return String.format("/%s(%s)", tableName, generateRequestIdentity(false, null, true));
    }

    /**
     * Reserved for internal use. Gets the table entity associated with this operation.
     * 
     * @return
     *         The {@link TableEntity} instance associated with this operation.
     */
    protected synchronized final TableEntity getEntity() {
        return this.entity;
    }

    /**
     * Reserved for internal use. Gets the operation type for this operation.
     * 
     * @return the opType
     *         The {@link TableOperationType} instance associated with this operation.
     */
    protected synchronized final TableOperationType getOperationType() {
        return this.opType;
    }

    /**
     * Reserved for internal use. Parses the table operation response into a {@link TableResult} to return.
     * 
     * @param xmlr
     *            An <code>XMLStreamReader</code> containing the response to an insert operation.
     * @param httpStatusCode
     *            The HTTP status code returned from the operation request.
     * @param etagFromHeader
     *            The <code>String</code> containing the Etag returned with the operation response.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation.
     * 
     * @return
     *         The {@link TableResult} representing the result of the operation.
     * 
     * @throws XMLStreamException
     *             if an error occurs accessing the <code>XMLStreamReader</code>.
     * @throws ParseException
     *             if an error occurs in parsing the response.
     * @throws InstantiationException
     *             if an error occurs in object construction.
     * @throws IllegalAccessException
     *             if an error occurs in reflection on an object type.
     * @throws StorageException
     *             if an error occurs in the storage operation.
     */
    protected TableResult parseResponse(final XMLStreamReader xmlr, final int httpStatusCode,
            final String etagFromHeader, final OperationContext opContext) throws XMLStreamException, ParseException,
            InstantiationException, IllegalAccessException, StorageException {
        TableResult resObj = null;
        if (this.opType == TableOperationType.INSERT) {
            // Sending null for class type and resolver will ignore parsing the return payload.
            resObj = AtomPubParser.parseSingleOpResponse(xmlr, httpStatusCode, null, null, opContext);
            resObj.updateResultObject(this.getEntity());
        }
        else {
            resObj = new TableResult(httpStatusCode);
            resObj.setResult(this.getEntity());

            if (this.opType != TableOperationType.DELETE && etagFromHeader != null) {
                resObj.setEtag(etagFromHeader);
                this.getEntity().setEtag(etagFromHeader);
            }
        }

        return resObj;
    }

    /**
     * Reserved for internal use. Sets the {@link TableEntity} instance for the table operation.
     * 
     * @param entity
     *            The {@link TableEntity} instance to set.
     */
    protected synchronized final void setEntity(final TableEntity entity) {
        this.entity = entity;
    }
}
