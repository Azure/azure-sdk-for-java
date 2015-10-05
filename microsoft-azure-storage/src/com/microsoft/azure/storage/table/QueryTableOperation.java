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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import com.fasterxml.jackson.core.JsonParseException;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageExtendedErrorInformation;
import com.microsoft.azure.storage.core.ExecutionEngine;
import com.microsoft.azure.storage.core.RequestLocationMode;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.StorageRequest;
import com.microsoft.azure.storage.core.Utility;

/**
 * A class that extends {@link TableOperation} to implement a query to retrieve a single table entity. To execute a
 * {@link QueryTableOperation} instance, call the <code>execute</code> method on a {@link CloudTableClient} instance.
 * This operation can be executed directly or as part of a {@link TableBatchOperation}. If the
 * {@link QueryTableOperation} returns an entity result, it is stored in the corresponding {@link TableResult} returned
 * by the <code>execute</code> method.
 */
public class QueryTableOperation extends TableOperation {
    private EntityResolver<?> resolver;

    private Class<? extends TableEntity> clazzType;

    private String partitionKey;

    private String rowKey;

    private boolean isPrimaryOnlyRetrieve = false;

    /**
     * Default constructor.
     */
    protected QueryTableOperation() {
        super(null, TableOperationType.RETRIEVE);
    }

    /**
     * Constructs a {@link QueryTableOperation} instance to retrieve a single table entity with the specified partition
     * key and row key.
     * 
     * @param partitionKey
     *            A <code>String</code> containing the PartitionKey value for the entity.
     * @param rowKey
     *            A <code>String</code> containing the RowKey value for the entity.
     */
    QueryTableOperation(final String partitionKey, final String rowKey) {
        super(null, TableOperationType.RETRIEVE);
        Utility.assertNotNull("partitionKey", partitionKey);
        this.partitionKey = partitionKey;
        this.rowKey = rowKey;
    }

    /**
     * Gets the PartitionKey value for the entity to retrieve.
     * 
     * @return
     *         A <code>String</code> containing the PartitionKey value for the entity.
     */
    public String getPartitionKey() {
        return this.partitionKey;
    }

    /**
     * Gets the resolver to project the entity retrieved as a particular type.
     * 
     * @return
     *         The {@link EntityResolver} instance.
     */
    public EntityResolver<?> getResolver() {
        return this.resolver;
    }

    /**
     * Gets the RowKey value for the entity to retrieve.
     * 
     * @return
     *         A <code>String</code> containing the RowKey value for the entity.
     */
    public String getRowKey() {
        return this.rowKey;
    }

    /**
     * Reserved for internal use. Gets the class type of the entity returned by the query.
     * 
     * @return
     *         The <code>java.lang.Class</code> implementing {@link TableEntity} that represents the entity type for the
     *         query.
     */
    protected Class<? extends TableEntity> getClazzType() {
        return this.clazzType;
    }

    /**
     * Reserved for internal use. Parses the table operation response into a {@link TableResult} to return.
     * 
     * @param inStream
     *            An <code>InputStream</code> containing the response to a query operation.
     * @param httpStatusCode
     *            The HTTP status code returned from the operation request.
     * @param etagFromHeader
     *            The <code>String</code> containing the Etag returned with the operation response.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation.
     * @return
     *         The {@link TableResult} representing the result of the query operation.
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
    @Override
    protected TableResult parseResponse(final InputStream inStream, final int httpStatusCode, String etagFromHeader,
            final OperationContext opContext, final TableRequestOptions options) throws InstantiationException,
            IllegalAccessException, StorageException, JsonParseException, IOException {
        TableResult res = TableDeserializer.parseSingleOpResponse(inStream, options, httpStatusCode,
                this.getClazzType(), this.getResolver(), opContext);
        res.setEtag(etagFromHeader);
        return res;
    }

    /**
     * Reserved for internal use. Performs a retrieve operation on the specified table, using the specified
     * {@link TableRequestOptions} and {@link OperationContext}.
     * <p>
     * This method will invoke the Storage Service REST API to execute this table operation, using the Table service
     * endpoint and storage account credentials in the {@link CloudTableClient} object.
     * 
     * @param client
     *            A {@link CloudTableClient} instance specifying the Table service endpoint and storage account
     *            credentials to use.
     * @param tableName
     *            A <code>String</code> containing the name of the table to query.
     * @param options
     *            A {@link TableRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation.
     * 
     * @return
     *         A {@link TableResult} containing the results of executing the query operation.
     * 
     * @throws StorageException
     *             if an error occurs in the storage operation.
     */
    protected TableResult performRetrieve(final CloudTableClient client, final String tableName,
            final TableRequestOptions options, final OperationContext opContext) throws StorageException {

        return ExecutionEngine.executeWithRetry(client, this, this.retrieveImpl(client, tableName, options),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudTableClient, QueryTableOperation, TableResult> retrieveImpl(
            final CloudTableClient client, final String tableName, final TableRequestOptions options) {
        final boolean isTableEntry = TableConstants.TABLES_SERVICE_TABLES_NAME.equals(tableName);
        if (this.getClazzType() != null) {
            Utility.checkNullaryCtor(this.getClazzType());
        }
        else {
            Utility.assertNotNull(SR.QUERY_REQUIRES_VALID_CLASSTYPE_OR_RESOLVER, this.getResolver());
        }

        final StorageRequest<CloudTableClient, QueryTableOperation, TableResult> getRequest = new StorageRequest<CloudTableClient, QueryTableOperation, TableResult>(
                options, client.getStorageUri()) {

            @Override
            public void setRequestLocationMode() {
                this.setRequestLocationMode(isPrimaryOnlyRetrieve() ? RequestLocationMode.PRIMARY_ONLY
                        : RequestLocationMode.PRIMARY_OR_SECONDARY);
            }

            @Override
            public HttpURLConnection buildRequest(CloudTableClient client, QueryTableOperation operation,
                    OperationContext context) throws Exception {
                return TableRequest
                        .query(client.getTransformedEndPoint(context).getUri(this.getCurrentLocation()), options,
                                null/* Query Builder */, context, tableName,
                                generateRequestIdentity(isTableEntry, operation.getPartitionKey()), null/* Continuation Token */);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudTableClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signTableRequest(connection, client, -1L, context);
            }

            @Override
            public TableResult preProcessResponse(QueryTableOperation operation, CloudTableClient client,
                    OperationContext context) throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK
                        && this.getResult().getStatusCode() != HttpURLConnection.HTTP_NOT_FOUND) {
                    this.setNonExceptionedRetryableFailure(true);
                }
                return null;
            }

            @Override
            public TableResult postProcessResponse(HttpURLConnection connection, QueryTableOperation operation,
                    CloudTableClient client, OperationContext context, TableResult storageObject) throws Exception {
                if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                    // Empty result
                    return new TableResult(this.getResult().getStatusCode());
                }

                // Parse response for updates
                InputStream inStream = connection.getInputStream();
                TableResult res = parseResponse(inStream, this.getResult().getStatusCode(), this.getConnection()
                        .getHeaderField(TableConstants.HeaderConstants.ETAG), context, options);

                return res;
            }

            @Override
            public StorageExtendedErrorInformation parseErrorDetails() {
                return TableStorageErrorDeserializer.parseErrorDetails(this);
            }
        };

        return getRequest;
    }

    /**
     * Reserved for internal use. Sets the class type of the entity returned by the query.
     * 
     * @param clazzType
     *            The <code>java.lang.Class</code> implementing {@link TableEntity} that represents the entity type for
     *            the query.
     */
    protected void setClazzType(final Class<? extends TableEntity> clazzType) {
        Utility.assertNotNull("clazzType", clazzType);
        Utility.checkNullaryCtor(clazzType);
        this.clazzType = clazzType;
    }

    /**
     * Reserved for internal use. Sets the PartitionKey value for the entity to retrieve.
     * 
     * @param partitionKey
     *            A <code>String</code> containing the PartitionKey value for the entity.
     */
    protected void setPartitionKey(final String partitionKey) {
        this.partitionKey = partitionKey;
    }

    /**
     * Reserved for internal use. Sets the resolver to project the entity retrieved as a particular type.
     * 
     * @param resolver
     *            The {@link EntityResolver} instance to use.
     */
    protected void setResolver(final EntityResolver<?> resolver) {
        Utility.assertNotNull(SR.QUERY_REQUIRES_VALID_CLASSTYPE_OR_RESOLVER, resolver);
        this.resolver = resolver;
    }

    /**
     * Reserved for internal use. Sets the RowKey value for the entity to retrieve.
     * 
     * @param rowKey
     *            A <code>String</code> containing the RowKey value for the entity.
     */
    protected void setRowKey(final String rowKey) {
        this.rowKey = rowKey;
    }

    /**
     * @return the isPrimaryOnlyRetrieve
     */
    protected final boolean isPrimaryOnlyRetrieve() {
        return this.isPrimaryOnlyRetrieve;
    }

    /**
     * @param isPrimaryOnlyRetrieve
     *            the isPrimaryOnlyRetrieve to set
     */
    protected void setPrimaryOnlyRetrieve(boolean isPrimaryOnlyRetrieve) {
        this.isPrimaryOnlyRetrieve = isPrimaryOnlyRetrieve;
    }
}
