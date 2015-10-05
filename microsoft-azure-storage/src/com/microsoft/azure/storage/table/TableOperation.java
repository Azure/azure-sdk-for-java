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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import com.fasterxml.jackson.core.JsonParseException;
import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageExtendedErrorInformation;
import com.microsoft.azure.storage.core.ExecutionEngine;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.StorageRequest;
import com.microsoft.azure.storage.core.Utility;

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
     * A static factory method returning a {@link TableOperation} instance to delete the specified entity from Microsoft
     * Azure storage. To execute this {@link TableOperation} on a given table, call the
     * {@link CloudTable#execute(TableOperation)} method on a {@link CloudTableClient} instance with the
     * 
     * @param entity
     *            The object instance implementing {@link TableEntity} to associate with the operation.
     * @return
     *         A new {@link TableOperation} instance to insert the table entity.
     */
    public static TableOperation delete(final TableEntity entity) {
        Utility.assertNotNull("entity", entity);
        Utility.assertNotNullOrEmpty("entity etag", entity.getEtag());
        return new TableOperation(entity, TableOperationType.DELETE);
    }

    /**
     * A static factory method returning a {@link TableOperation} instance to insert the specified entity into
     * Microsoft Azure storage. To execute this {@link TableOperation} on a given table, call the
     * {@link CloudTable#execute(TableOperation)} method on a {@link CloudTableClient} instance with the

     * 
     * @param entity
     *            The object instance implementing {@link TableEntity} to associate with the operation.
     * @return
     *         A new {@link TableOperation} instance to insert the table entity.
     */
    public static TableOperation insert(final TableEntity entity) {
        return insert(entity, false);
    }

    /**
     * A static factory method returning a {@link TableOperation} instance to insert the specified entity into
     * Microsoft Azure storage. To execute this {@link TableOperation} on a given table, call the
     * {@link CloudTable#execute(TableOperation)} method on a {@link CloudTableClient} instance with the
     * 
     * @param entity
     *            The object instance implementing {@link TableEntity} to associate with the operation.
     * @param echoContent
     *            The boolean representing whether the message payload should be returned in the response.
     * @return
     *         A new {@link TableOperation} instance to insert the table entity.
     */
    public static TableOperation insert(final TableEntity entity, boolean echoContent) {
        Utility.assertNotNull("entity", entity);
        return new TableOperation(entity, TableOperationType.INSERT, echoContent);
    }

    /**
     * A static factory method returning a {@link TableOperation} instance to merge the specified entity into
     * Microsoft Azure storage, or insert it if it does not exist. To execute this {@link TableOperation} on a given
     * table, call
     * the {@link CloudTable#execute(TableOperation)} method on a {@link CloudTableClient} instance with
     * the table name and the {@link TableOperation} as arguments.
     * 
     * @param entity
     *            The object instance implementing {@link TableEntity} to associate with the operation.
     * @return
     *         A new {@link TableOperation} instance for inserting or merging the table entity.
     */
    public static TableOperation insertOrMerge(final TableEntity entity) {
        Utility.assertNotNull("entity", entity);
        return new TableOperation(entity, TableOperationType.INSERT_OR_MERGE);
    }

    /**
     * A static factory method returning a {@link TableOperation} instance to replace the specified entity in
     * Microsoft Azure storage, or insert it if it does not exist. To execute this {@link TableOperation} on a given
     * table, call
     * the {@link CloudTable#execute(TableOperation)} method on a {@link CloudTableClient} instance with
     * the table name and the {@link TableOperation} as arguments.
     * 
     * @param entity
     *            The object instance implementing {@link TableEntity} to associate with the operation.
     * @return
     *         A new {@link TableOperation} instance for inserting or replacing the table entity.
     */
    public static TableOperation insertOrReplace(final TableEntity entity) {
        Utility.assertNotNull("entity", entity);
        return new TableOperation(entity, TableOperationType.INSERT_OR_REPLACE);
    }

    /**
     * A static factory method returning a {@link TableOperation} instance to merge the specified table entity into
     * Microsoft Azure storage. To execute this {@link TableOperation} on a given table, call the
     * {@link CloudTable#execute(TableOperation)} method on a {@link CloudTableClient} instance with the
     * 
     * @param entity
     *            The object instance implementing {@link TableEntity} to associate with the operation.
     * @return
     *         A new {@link TableOperation} instance for merging the table entity.
     */
    public static TableOperation merge(final TableEntity entity) {
        Utility.assertNotNull("entity", entity);
        Utility.assertNotNullOrEmpty("entity etag", entity.getEtag());
        return new TableOperation(entity, TableOperationType.MERGE);
    }

    /**
     * A static factory method returning a {@link TableOperation} instance to retrieve the specified table entity and
     * return it as the specified type. To execute this {@link TableOperation} on a given table, call the
     * {@link CloudTable#execute(TableOperation)} method on a {@link CloudTableClient} instance with the
     * 
     * @param partitionKey
     *            A <code>String</code> which specifies the PartitionKey value for the entity to retrieve.
     * @param rowKey
     *            A <code>String</code> which specifies the RowKey value for the entity to retrieve.
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
     * call the {@link CloudTable#execute(TableOperation)} method on a {@link CloudTableClient} instance
     * with the table name and the {@link TableOperation} as arguments.
     * 
     * @param partitionKey
     *            A <code>String</code> which specifies the PartitionKey value for the entity to retrieve.
     * @param rowKey
     *            A <code>String</code> which specifies the RowKey value for the entity to retrieve.
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
     * {@link CloudTable#execute(TableOperation)} method.
     * 
     * @param entity
     *            The object instance implementing {@link TableEntity} to associate with the operation.
     * @return
     *         A new {@link TableOperation} instance for replacing the table entity.
     */
    public static TableOperation replace(final TableEntity entity) {
        Utility.assertNotNullOrEmpty("entity etag", entity.getEtag());
        return new TableOperation(entity, TableOperationType.REPLACE);
    }

    /**
     * The table entity instance associated with the operation.
     */
    private TableEntity entity;

    /**
     * The {@link TableOperationType} enumeration value for the operation type.
     */
    private TableOperationType opType = null;

    /**
     * The value that represents whether the message payload should be returned in the response.
     */
    private boolean echoContent;

    /**
     * Nullary Default Constructor.
     */
    protected TableOperation() {
        // Empty constructor.
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
        this(entity, opType, false);
    }

    /**
     * Reserved for internal use. Constructs a {@link TableOperation} with the specified table entity and operation
     * type.
     * 
     * @param entity
     *            The object instance implementing {@link TableEntity} to associate with the operation.
     * @param opType
     *            The {@link TableOperationType} enumeration value for the operation type.
     * @param echoContent
     *            The boolean representing whether the message payload should be returned in the response.
     */
    protected TableOperation(final TableEntity entity, final TableOperationType opType, final boolean echoContent) {
        this.entity = entity;
        this.opType = opType;
        this.echoContent = echoContent;
    }

    /**
     * Reserved for internal use. Performs a delete operation on the specified table, using the specified
     * {@link TableRequestOptions} and {@link OperationContext}.
     * <p>
     * This method will invoke the <a href="http://msdn.microsoft.com/en-us/library/azure/dd135727.aspx">Delete
     * Entity</a> REST API to execute this table operation, using the Table service endpoint and storage account
     * credentials in the {@link CloudTableClient} object.
     * 
     * @param client
     *            A {@link CloudTableClient} instance specifying the Table service endpoint, storage account
     *            credentials, and any additional query parameters.
     * @param tableName
     *            A <code>String</code> which specifies the name of the table.
     * @param options
     *            A {@link TableRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation.
     * 
     * @return
     *         A {@link TableResult} which represents the results of executing the operation.
     * 
     * @throws StorageException
     *             if an error occurs in the storage operation.
     */
    private TableResult performDelete(final CloudTableClient client, final String tableName,
            final TableRequestOptions options, final OperationContext opContext) throws StorageException {

        return ExecutionEngine.executeWithRetry(client, this, this.deleteImpl(client, tableName, options, opContext),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudTableClient, TableOperation, TableResult> deleteImpl(final CloudTableClient client,
            final String tableName, final TableRequestOptions options, final OperationContext opContext)
            throws StorageException {
        final boolean isTableEntry = TableConstants.TABLES_SERVICE_TABLES_NAME.equals(tableName);
        final String tableIdentity = isTableEntry ? this.getEntity().writeEntity(opContext)
                .get(TableConstants.TABLE_NAME).getValueAsString() : null;

        if (!isTableEntry) {
            Utility.assertNotNullOrEmpty(SR.ETAG_INVALID_FOR_DELETE, this.getEntity().getEtag());
            Utility.assertNotNull(SR.PARTITIONKEY_MISSING_FOR_DELETE, this.getEntity().getPartitionKey());
            Utility.assertNotNull(SR.ROWKEY_MISSING_FOR_DELETE, this.getEntity().getRowKey());
        }

        final StorageRequest<CloudTableClient, TableOperation, TableResult> deleteRequest = new StorageRequest<CloudTableClient, TableOperation, TableResult>(
                options, client.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudTableClient client, TableOperation operation,
                    OperationContext context) throws Exception {
                return TableRequest.delete(client.getTransformedEndPoint(context).getUri(this.getCurrentLocation()),
                        options, null, context, tableName, generateRequestIdentity(isTableEntry, tableIdentity),
                        operation.getEntity().getEtag());
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudTableClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signTableRequest(connection, client, -1L, context);
            }

            @Override
            public TableResult preProcessResponse(TableOperation operation, CloudTableClient client,
                    OperationContext context) throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                    throw TableServiceException.generateTableServiceException(this.getResult(), operation, 
                            this.getConnection().getErrorStream(), options.getTablePayloadFormat());
                }

                return operation.parseResponse(null, this.getResult().getStatusCode(), null, opContext, options);
            }

            @Override
            public StorageExtendedErrorInformation parseErrorDetails() {
                return TableStorageErrorDeserializer.parseErrorDetails(this);
            }
        };

        return deleteRequest;
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
     *            A <code>String</code> which specifies the name of the table.
     * @param options
     *            A {@link TableRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation.
     * 
     * @return
     *         A {@link TableResult} which represents the results of executing the operation.
     * 
     * @throws StorageException
     *             if an error occurs in the storage operation.
     */
    private TableResult performInsert(final CloudTableClient client, final String tableName,
            final TableRequestOptions options, final OperationContext opContext) throws StorageException {

        return ExecutionEngine.executeWithRetry(client, this, this.insertImpl(client, tableName, options, opContext),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudTableClient, TableOperation, TableResult> insertImpl(final CloudTableClient client,
            final String tableName, final TableRequestOptions options, final OperationContext opContext)
            throws StorageException {
        final boolean isTableEntry = TableConstants.TABLES_SERVICE_TABLES_NAME.equals(tableName);
        final String tableIdentity = isTableEntry ? this.getEntity().writeEntity(opContext)
                .get(TableConstants.TABLE_NAME).getValueAsString() : null;

        // Inserts need row key and partition key
        if (!isTableEntry) {
            Utility.assertNotNull(SR.PARTITIONKEY_MISSING_FOR_INSERT, this.getEntity().getPartitionKey());
            Utility.assertNotNull(SR.ROWKEY_MISSING_FOR_INSERT, this.getEntity().getRowKey());
        }

        ByteArrayOutputStream entityStream = new ByteArrayOutputStream();
        try {
            TableEntitySerializer.writeSingleEntityToStream(entityStream, options.getTablePayloadFormat(), this.entity,
                    isTableEntry, opContext);
            // We need to buffer once and use it for all retries instead of serializing the entity every single time. 
            // In the future, this could also be used to calculate transactional MD5 for table operations.
            final byte[] entityBytes = entityStream.toByteArray();

            final StorageRequest<CloudTableClient, TableOperation, TableResult> putRequest = new StorageRequest<CloudTableClient, TableOperation, TableResult>(
                    options, client.getStorageUri()) {

                @Override
                public HttpURLConnection buildRequest(CloudTableClient client, TableOperation operation,
                        OperationContext context) throws Exception {
                    this.setSendStream(new ByteArrayInputStream(entityBytes));
                    this.setLength((long) entityBytes.length);
                    return TableRequest.insert(
                            client.getTransformedEndPoint(opContext).getUri(this.getCurrentLocation()), options, null,
                            opContext, tableName, generateRequestIdentity(isTableEntry, tableIdentity),
                            operation.opType != TableOperationType.INSERT ? operation.getEntity().getEtag() : null,
                            operation.getEchoContent(), operation.opType.getUpdateType());
                }

                @Override
                public void signRequest(HttpURLConnection connection, CloudTableClient client, OperationContext context)
                        throws Exception {
                    StorageRequest.signTableRequest(connection, client, -1L, context);
                }

                @Override
                public TableResult preProcessResponse(TableOperation operation, CloudTableClient client,
                        OperationContext context) throws Exception {
                    if (operation.opType == TableOperationType.INSERT) {
                        if (operation.getEchoContent()
                                && this.getResult().getStatusCode() == HttpURLConnection.HTTP_CREATED) {
                            // Insert should receive created if echo content is on
                            return new TableResult();
                        }
                        else if (!operation.getEchoContent()
                                && this.getResult().getStatusCode() == HttpURLConnection.HTTP_NO_CONTENT) {
                            // Insert should receive no content if echo content is off
                            return operation.parseResponse(null, this.getResult().getStatusCode(), this.getConnection()
                                    .getHeaderField(TableConstants.HeaderConstants.ETAG), opContext, options);
                        }
                    }
                    else if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_NO_CONTENT) {
                        // InsertOrMerge and InsertOrReplace should always receive no content
                        return operation.parseResponse(null, this.getResult().getStatusCode(), this.getConnection()
                                .getHeaderField(TableConstants.HeaderConstants.ETAG), opContext, options);
                    }

                    throw TableServiceException.generateTableServiceException(this.getResult(), operation, 
                            this.getConnection().getErrorStream(), options.getTablePayloadFormat());
                }

                @Override
                public TableResult postProcessResponse(HttpURLConnection connection, TableOperation operation,
                        CloudTableClient client, OperationContext context, TableResult result) throws Exception {
                    if (operation.opType == TableOperationType.INSERT && operation.getEchoContent()) {
                        result = operation.parseResponse(this.getConnection().getInputStream(), this.getResult()
                                .getStatusCode(),
                                this.getConnection().getHeaderField(TableConstants.HeaderConstants.ETAG), opContext,
                                options);
                    }
                    return result;
                }

                @Override
                public StorageExtendedErrorInformation parseErrorDetails() {
                    return TableStorageErrorDeserializer.parseErrorDetails(this);
                }
            };

            return putRequest;
        }
        catch (IOException e) {
            // The request was not even made. There was an error while trying to read the entity. Just throw.
            StorageException translatedException = StorageException.translateClientException(e);
            throw translatedException;
        }

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
     *            A <code>String</code> which specifies the name of the table.
     * @param options
     *            A {@link TableRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation.
     * 
     * @return
     *         A {@link TableResult} which represents the results of executing the operation.
     * 
     * @throws StorageException
     *             if an error occurs in the storage operation.
     * @throws IOException
     */
    private TableResult performMerge(final CloudTableClient client, final String tableName,
            final TableRequestOptions options, final OperationContext opContext) throws StorageException {

        return ExecutionEngine.executeWithRetry(client, this, this.mergeImpl(client, tableName, options, opContext),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudTableClient, TableOperation, TableResult> mergeImpl(final CloudTableClient client,
            final String tableName, final TableRequestOptions options, final OperationContext opContext)
            throws StorageException {
        Utility.assertNotNullOrEmpty(SR.ETAG_INVALID_FOR_MERGE, this.getEntity().getEtag());
        Utility.assertNotNull(SR.PARTITIONKEY_MISSING_FOR_MERGE, this.getEntity().getPartitionKey());
        Utility.assertNotNull(SR.ROWKEY_MISSING_FOR_MERGE, this.getEntity().getRowKey());

        ByteArrayOutputStream entityStream = new ByteArrayOutputStream();
        try {
            TableEntitySerializer.writeSingleEntityToStream(entityStream, options.getTablePayloadFormat(),
                    this.getEntity(), false, opContext);
            // We need to buffer once and use it for all retries instead of serializing the entity every single time. 
            // In the future, this could also be used to calculate transactional MD5 for table operations.
            final byte[] entityBytes = entityStream.toByteArray();

            final StorageRequest<CloudTableClient, TableOperation, TableResult> putRequest = new StorageRequest<CloudTableClient, TableOperation, TableResult>(
                    options, client.getStorageUri()) {

                @Override
                public HttpURLConnection buildRequest(CloudTableClient client, TableOperation operation,
                        OperationContext context) throws Exception {
                    this.setSendStream(new ByteArrayInputStream(entityBytes));
                    this.setLength((long) entityBytes.length);
                    return TableRequest.merge(client.getTransformedEndPoint(opContext)
                            .getUri(this.getCurrentLocation()), options, null, opContext, tableName,
                            generateRequestIdentity(false, null), operation.getEntity().getEtag());
                }

                @Override
                public void signRequest(HttpURLConnection connection, CloudTableClient client, OperationContext context)
                        throws Exception {
                    StorageRequest.signTableRequest(connection, client, -1L, context);
                }

                @Override
                public TableResult preProcessResponse(TableOperation operation, CloudTableClient client,
                        OperationContext context) throws Exception {
                    if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                        throw TableServiceException.generateTableServiceException(this.getResult(), operation, 
                                this.getConnection().getErrorStream(), options.getTablePayloadFormat());
                    }

                    return operation.parseResponse(null, this.getResult().getStatusCode(), this.getConnection()
                            .getHeaderField(TableConstants.HeaderConstants.ETAG), opContext, options);
                }

                @Override
                public StorageExtendedErrorInformation parseErrorDetails() {
                    return TableStorageErrorDeserializer.parseErrorDetails(this);
                }
            };

            return putRequest;
        }
        catch (IOException e) {
            // The request was not even made. There was an error while trying to read the entity. Just throw.
            StorageException translatedException = StorageException.translateClientException(e);
            throw translatedException;
        }
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
     *            A <code>String</code> which specifies the name of the table.
     * @param options
     *            A {@link TableRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation.
     * 
     * @return
     *         A {@link TableResult} which represents the results of executing the operation.
     * 
     * @throws StorageException
     *             if an error occurs in the storage operation.
     */
    private TableResult performUpdate(final CloudTableClient client, final String tableName,
            final TableRequestOptions options, final OperationContext opContext) throws StorageException {

        return ExecutionEngine.executeWithRetry(client, this, this.updateImpl(client, tableName, options, opContext),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudTableClient, TableOperation, TableResult> updateImpl(final CloudTableClient client,
            final String tableName, final TableRequestOptions options, final OperationContext opContext)
            throws StorageException {
        Utility.assertNotNullOrEmpty(SR.ETAG_INVALID_FOR_UPDATE, this.getEntity().getEtag());
        Utility.assertNotNull(SR.PARTITIONKEY_MISSING_FOR_UPDATE, this.getEntity().getPartitionKey());
        Utility.assertNotNull(SR.ROWKEY_MISSING_FOR_UPDATE, this.getEntity().getRowKey());

        ByteArrayOutputStream entityStream = new ByteArrayOutputStream();
        try {
            TableEntitySerializer.writeSingleEntityToStream(entityStream, options.getTablePayloadFormat(),
                    this.getEntity(), false, opContext);
            // We need to buffer once and use it for all retries instead of serializing the entity every single time. 
            // In the future, this could also be used to calculate transactional MD5 for table operations.
            final byte[] entityBytes = entityStream.toByteArray();

            final StorageRequest<CloudTableClient, TableOperation, TableResult> putRequest = new StorageRequest<CloudTableClient, TableOperation, TableResult>(
                    options, client.getStorageUri()) {

                @Override
                public HttpURLConnection buildRequest(CloudTableClient client, TableOperation operation,
                        OperationContext context) throws Exception {
                    this.setSendStream(new ByteArrayInputStream(entityBytes));
                    this.setLength((long) entityBytes.length);
                    return TableRequest.update(
                            client.getTransformedEndPoint(context).getUri(this.getCurrentLocation()), options, null,
                            context, tableName, generateRequestIdentity(false, null), operation.getEntity()
                                    .getEtag());
                }

                @Override
                public void signRequest(HttpURLConnection connection, CloudTableClient client, OperationContext context)
                        throws Exception {
                    StorageRequest.signTableRequest(connection, client, -1L, context);
                }

                @Override
                public TableResult preProcessResponse(TableOperation operation, CloudTableClient client,
                        OperationContext context) throws Exception {
                    if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                        throw TableServiceException.generateTableServiceException(this.getResult(), operation, 
                                this.getConnection().getErrorStream(), options.getTablePayloadFormat());
                    }

                    return operation.parseResponse(null, this.getResult().getStatusCode(), this.getConnection()
                            .getHeaderField(TableConstants.HeaderConstants.ETAG), opContext, options);
                }

                @Override
                public StorageExtendedErrorInformation parseErrorDetails() {
                    return TableStorageErrorDeserializer.parseErrorDetails(this);
                }
            };

            return putRequest;
        }
        catch (IOException e) {
            // The request was not even made. There was an error while trying to read the entity. Just throw.
            StorageException translatedException = StorageException.translateClientException(e);
            throw translatedException;
        }
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
     *            A <code>String</code> which specifies the name of the table.
     * @param options
     *            A {@link TableRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation.
     * 
     * @return
     *         A {@link TableResult} which represents the results of executing the operation.
     * 
     * @throws StorageException
     *             if an error occurs in the storage operation.
     */
    protected TableResult execute(final CloudTableClient client, final String tableName, TableRequestOptions options,
            OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = TableRequestOptions.populateAndApplyDefaults(options, client);
        Utility.assertNotNullOrEmpty(TableConstants.TABLE_NAME, tableName);

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
            throw new IllegalArgumentException(SR.UNKNOWN_TABLE_OPERATION);
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
     * @return
     *         A <code>String</code> which represents the formatted request identity string.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    protected String generateRequestIdentity(boolean isSingleIndexEntry, final String entryName)
            throws StorageException {
        if (isSingleIndexEntry) {
            return String.format("'%s'", entryName.replace("'", "''"));
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

            return String.format("%s='%s',%s='%s'", 
                    TableConstants.PARTITION_KEY, pk.replace("'", "''"), 
                    TableConstants.ROW_KEY, rk.replace("'", "''"));
        }
    }

    /**
     * Reserved for internal use. Generates the request identity string for the specified table. The request identity
     * string combines the table name with the PartitionKey and RowKey from the operation to identify specific table
     * entities. This request identity is already UrlEncoded.
     * 
     * @param tableName
     *            A <code>String</code> which specifies the name of the table.
     * @return
     *         A <code>String</code> which represents the formatted request identity string for the specified table.
     * @throws StorageException
     */
    protected String generateRequestIdentityWithTable(final String tableName) throws StorageException {
        return String.format("%s(%s)", tableName, generateRequestIdentity(false, null));
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
     * @param inStream
     *            An <code>InputStream</code> which specifies the response to an insert operation.
     * @param httpStatusCode
     *            An <code>int</code> which represents the HTTP status code returned from the operation request.
     * @param etagFromHeader
     *            The <code>String</code> which specifies the Etag returned with the operation response.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation.
     * 
     * @return
     *         The {@link TableResult} representing the result of the operation.
     * 
     * @throws InstantiationException
     *             if an error occurs in object construction.
     * @throws IllegalAccessException
     *             if an error occurs in reflection on an object type.
     * @throws StorageException
     *             if an error occurs in the storage operation.
     * @throws IOException
     *             if an error occurs while accessing the {@link InputStream} with Json.
     * @throws JsonParseException
     *             if an error occurs while parsing the Json, if Json is used.
     */
    protected TableResult parseResponse(final InputStream inStream, final int httpStatusCode, String etagFromHeader,
            final OperationContext opContext, final TableRequestOptions options) throws InstantiationException,
            IllegalAccessException, StorageException, JsonParseException, IOException {
        TableResult resObj;

        if (this.opType == TableOperationType.INSERT && this.echoContent) {
            // Sending null for class type and resolver will ignore parsing the return payload.
            resObj = TableDeserializer.parseSingleOpResponse(inStream, options, httpStatusCode, null /* clazzType */,
                    null /*resolver */, opContext);
            resObj.setEtag(etagFromHeader);
            resObj.updateResultObject(this.getEntity());
        }
        else {
            resObj = new TableResult(httpStatusCode);
            resObj.setResult(this.getEntity());

            if (this.opType != TableOperationType.DELETE && etagFromHeader != null) {
                resObj.setEtag(etagFromHeader);
                resObj.updateResultObject(this.getEntity());
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

    /**
     * Gets the boolean representing whether the message payload should be returned in the response.
     * 
     * @return <code>true</code> if the message payload should be returned in the response; otherwise <code>false</code>
     */
    protected boolean getEchoContent() {
        return this.echoContent;
    }

    /**
     * Sets the boolean representing whether the message payload should be returned in the response.
     * 
     * @param echoContent
     *            <code>true</code> if the message payload should be returned in the response; otherwise
     *            <code>false</code>.
     */
    protected void setEchoContent(boolean echoContent) {
        this.echoContent = echoContent;
    }

}
