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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;

import com.microsoft.azure.storage.DoesServiceRequest;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.ResultContinuation;
import com.microsoft.azure.storage.ResultContinuationType;
import com.microsoft.azure.storage.ResultSegment;
import com.microsoft.azure.storage.ServiceClient;
import com.microsoft.azure.storage.ServiceProperties;
import com.microsoft.azure.storage.ServiceStats;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsAnonymous;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageExtendedErrorInformation;
import com.microsoft.azure.storage.StorageUri;
import com.microsoft.azure.storage.core.ExecutionEngine;
import com.microsoft.azure.storage.core.LazySegmentedIterable;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.SegmentedStorageRequest;
import com.microsoft.azure.storage.core.StorageRequest;
import com.microsoft.azure.storage.core.Utility;

/**
 * Provides a service client for accessing the Microsoft Azure Table service.
 * <p>
 * The {@link CloudTableClient} class encapsulates the base URI for the Table service endpoint and the credentials for
 * accessing the storage account, and provides methods to create, delete, list, and query tables, as well as methods to
 * execute operations and queries on table entities. These methods invoke Storage Service REST API operations to make
 * the requests and obtain the results that are returned.
 * <p>
 * A Table service endpoint is the base URI for Table service resources, including the DNS name of the storage account:
 * <br>
 * <code>&nbsp&nbsp&nbsp&nbsphttp://<em>myaccount</em>.table.core.windows.net</code><br>
 * For more information, see the MSDN topic <a
 * href="http://msdn.microsoft.com/en-us/library/azure/dd179360.aspx">Addressing Table Service Resources</a>.
 * <p>
 * The credentials can be a combination of the storage account name and a key, or a shared access signature. For more
 * information, see the MSDN topic <a href="http://msdn.microsoft.com/en-us/library/azure/hh225339.aspx">Authenticating
 * Access to Your Storage Account</a>.
 * 
 */
public final class CloudTableClient extends ServiceClient {
    /**
     * Holds the default request option values associated with this Service Client.
     */
    private TableRequestOptions defaultRequestOptions = new TableRequestOptions();

    /**
     * Reserved for internal use. An {@link EntityResolver} that projects table entity data as a <code>String</code>
     * containing the table name.
     */
    private final EntityResolver<String> tableNameResolver = new EntityResolver<String>() {
        @Override
        public String resolve(String partitionKey, String rowKey, Date timeStamp,
                HashMap<String, EntityProperty> properties, String etag) {
            return properties.get(TableConstants.TABLE_NAME).getValueAsString();
        }
    };

    /**
     * Initializes an instance of the <code>CloudTableClient</code> class using a Table service endpoint and
     * storage account credentials.
     * 
     * @param baseUri
     *            A <code>java.net.URI</code> object that represents the Table service endpoint used to initialize the
     *            client.
     * @param credentials
     *            A {@link StorageCredentials} object that represents the storage account credentials for access.
     */
    public CloudTableClient(final URI baseUri, StorageCredentials credentials) {
        this(new StorageUri(baseUri), credentials);
    }

    /**
     * Initializes an instance of the <code>CloudTableClient</code>class using a Table service endpoint and
     * storage account credentials.
     * 
     * @param baseUri
     *            A {@link StorageUri} object that represents the Table service endpoint used to initialize the
     *            client.
     * @param credentials
     *            A {@link StorageCredentials} object that represents the storage account credentials for access.
     */
    public CloudTableClient(final StorageUri baseUri, StorageCredentials credentials) {
        super(baseUri, credentials);
        if (credentials == null || credentials.getClass().equals(StorageCredentialsAnonymous.class)) {
            throw new IllegalArgumentException(SR.STORAGE_CREDENTIALS_NULL_OR_ANONYMOUS);
        }
        TableRequestOptions.applyDefaults(this.defaultRequestOptions);
    }

    /**
     * Gets a {@link CloudTable} object with the specified name.
     * 
     * @param tableName
     *            A <code>String</code> which represents the name of the table, which must adhere to table naming rules.
     *            The table name should not include any path separator characters (/).
     *            Table names are case insensitive, must be unique within an account and must be between 3-63 characters
     *            long. Table names must start with an cannot begin with a numeric character and may only contain
     *            alphanumeric characters. Some table names are reserved, including "table".
     * 
     * @return A reference to a {@link CloudTable} object.
     * 
     * @throws URISyntaxException
     *             If the resource URI constructed based on the tableName is invalid.
     * @throws StorageException
     *             If a storage service error occurred.
     * @see <a href="http://msdn.microsoft.com/en-us/library/azure/dd179338.aspx">Understanding the Table Service Data
     *      Model</a>
     */
    public CloudTable getTableReference(final String tableName) throws URISyntaxException, StorageException {
        return new CloudTable(tableName, this);
    }

    /**
     * Lists the table names in the storage account.
     * <p>
     * This method invokes the <a href="http://msdn.microsoft.com/en-us/library/azure/dd179405.aspx">Query Tables</a>
     * REST API to list the table names, using the Table service endpoint and storage account credentials of this
     * instance.
     * 
     * @return An <code>Iterable</code> collection of the table names in the storage account retrieved lazily.
     */
    @DoesServiceRequest
    public Iterable<String> listTables() {
        return this.listTables(null);
    }

    /**
     * Lists the table names in the storage account that match the specified prefix.
     * <p>
     * This method invokes the <a href="http://msdn.microsoft.com/en-us/library/azure/dd179405.aspx">Query Tables</a>
     * REST API to list the table names that match the prefix, using the Table service endpoint and storage account
     * credentials of this instance.
     * 
     * @param prefix
     *            A <code>String</code> containing the prefix to match on table names to return.
     * 
     * @return
     *         An <code>Iterable</code> collection of the table names in the storage account retrieved lazily that match
     *         the specified prefix.
     */
    @DoesServiceRequest
    public Iterable<String> listTables(final String prefix) {
        return this.listTables(prefix, null /* options */, null /* opContext */);
    }

    /**
     * Lists the table names in the storage account that match the specified prefix, using the specified
     * {@link TableRequestOptions} and {@link OperationContext}.
     * <p>
     * This method invokes the <a href="http://msdn.microsoft.com/en-us/library/azure/dd179405.aspx">Query Tables</a>
     * REST API to list the table names that match the prefix, using the Table service endpoint and storage account
     * credentials of this instance.
     * 
     * Use the {@link TableRequestOptions} to override execution options such as the timeout or retry policy for the
     * operation.
     * 
     * @param prefix
     *            A <code>String</code> containing the prefix to match on table names to return.
     * @param options
     *            A {@link TableRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudTableClient}.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation. Specify <code>null</code> to
     *            safely ignore operation context.
     * 
     * @return
     *         An <code>Iterable</code> collection of the table names in the storage account retrieved lazily that match
     *         the specified
     *         prefix.
     */
    @SuppressWarnings("unchecked")
    @DoesServiceRequest
    public Iterable<String> listTables(final String prefix, final TableRequestOptions options,
            final OperationContext opContext) {
        return (Iterable<String>) this.generateIteratorForQuery(this.generateListTablesQuery(prefix),
                this.tableNameResolver, options, opContext);
    }

    /**
     * Lists the table names in the storage account in segmented mode. This method allows listing of tables to be
     * resumed after returning a partial set of results, using information returned by the server in the
     * {@link ResultSegment} object.
     * <p>
     * This method invokes the <a href="http://msdn.microsoft.com/en-us/library/azure/dd179405.aspx">Query Tables</a>
     * REST API to list the table names, using the Table service endpoint and storage account credentials of this
     * instance.
     * 
     * @return
     *         A {@link ResultSegment} of <code>String</code> objects containing table names in the storage account.
     * 
     * @throws StorageException
     *             if a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public ResultSegment<String> listTablesSegmented() throws StorageException {
        return this.listTablesSegmented(null);
    }

    /**
     * Lists the table names in the storage account that match the specified prefix in segmented mode. This method
     * allows listing of tables to be resumed after returning a partial set of results, using information returned by
     * the server in the {@link ResultSegment} object.
     * <p>
     * This method invokes the <a href="http://msdn.microsoft.com/en-us/library/azure/dd179405.aspx">Query Tables</a>
     * REST API to list the table names that match the prefix, using the Table service endpoint and storage account
     * credentials of this instance.
     * 
     * @param prefix
     *            A <code>String</code> containing the prefix to match on table names to return.
     * 
     * @return
     *         A {@link ResultSegment} of <code>String</code> objects containing table names matching the prefix in the
     *         storage account.
     * 
     * @throws StorageException
     *             if a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public ResultSegment<String> listTablesSegmented(final String prefix) throws StorageException {
        return this.listTablesSegmented(prefix, null /* maxResults */, null /* continuationToken */,
                null /* options */, null /* opContext */);
    }

    /**
     * Lists up to the specified maximum of the table names in the storage account that match the specified prefix in a
     * resumable mode with the specified {@link ResultContinuation} continuation token, using the specified
     * {@link TableRequestOptions} and {@link OperationContext}. This method allows listing of tables to be resumed
     * after returning a page of results, using information returned by the server in the {@link ResultSegment} object.
     * <p>
     * This method invokes the <a href="http://msdn.microsoft.com/en-us/library/azure/dd179405.aspx">Query Tables</a>
     * REST API to list the table names that match the prefix, using the Table service endpoint and storage account
     * credentials of this instance.
     * 
     * Use the {@link TableRequestOptions} to override execution options such as the timeout or retry policy for the
     * operation.
     * 
     * @param prefix
     *            A <code>String</code> containing the prefix to match on table names to return.
     * @param maxResults
     *            The maximum number of table names to return in the {@link ResultSegment}. If this parameter is null,
     *            the query will list up to the maximum 1,000 results.
     * @param continuationToken
     *            A {@link ResultContinuation} object representing a continuation token from the server when the
     *            operation returns a partial result. Specify <code>null</code> on the initial call. Call the
     *            {@link ResultSegment#getContinuationToken()} method on the result to obtain the
     *            {@link ResultContinuation} object to use in the next call to resume the query.
     * @param options
     *            A {@link TableRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudTableClient}.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation. Specify <code>null</code> to
     *            safely ignore operation context.
     * 
     * @return
     *         A {@link ResultSegment} of <code>String</code> objects containing table names in the storage account.
     * 
     * @throws StorageException
     *             if a storage service error occurred during the operation.
     */
    @SuppressWarnings("unchecked")
    @DoesServiceRequest
    public ResultSegment<String> listTablesSegmented(final String prefix, final Integer maxResults,
            final ResultContinuation continuationToken, final TableRequestOptions options,
            final OperationContext opContext) throws StorageException {
        if (null != maxResults) {
            Utility.assertGreaterThanOrEqual("maxResults", maxResults, 1);
        }
        
        return (ResultSegment<String>) this.executeQuerySegmentedImpl(
                this.generateListTablesQuery(prefix).take(maxResults), this.tableNameResolver, continuationToken,
                options, opContext);
    }

    /**
     * Reserved for internal use. Generates a query to list table names with the given prefix.
     * 
     * @param prefix
     *            A <code>String</code> containing the prefix to match on table names to return.
     * @return
     *         A {@link TableQuery} instance for listing table names with the specified prefix.
     */
    private TableQuery<TableServiceEntity> generateListTablesQuery(final String prefix) {
        TableQuery<TableServiceEntity> listQuery = TableQuery.<TableServiceEntity> from(TableServiceEntity.class);
        listQuery.setSourceTableName(TableConstants.TABLES_SERVICE_TABLES_NAME);

        if (!Utility.isNullOrEmpty(prefix)) {
            // Append Max char to end '{' is 1 + 'z' in AsciiTable > uppperBound = prefix + '{'
            final String prefixFilter = String.format("(%s ge '%s') and (%s lt '%s{')", TableConstants.TABLE_NAME,
                    prefix, TableConstants.TABLE_NAME, prefix);

            listQuery = listQuery.where(prefixFilter);
        }

        return listQuery;
    }

    /**
     * Reserved for internal use. Executes a segmented query operation using the specified retry and timeout policies.
     * 
     * @param queryToExecute
     *            The {@link TableQuery} to execute.
     * @param resolver
     *            An {@link EntityResolver} instance which creates a projection of the table query result entities into
     *            the specified type <code>R</code>. Pass <code>null</code> to return the results as the table entity
     *            type.
     * @param continuationToken
     *            The {@link ResultContinuation} to pass with the operation to resume a query, if any. Pass
     *            <code>null</code> for an initial query.
     * @param options
     *            A {@link TableRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudTableClient}.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation. Specify <code>null</code> to
     *            safely ignore operation context.
     * @return
     *         A {@link ResultSegment} containing a collection of the query results specialized for the
     *         {@link TableEntity} or {@link EntityResolver} type returned by the query.
     * @throws StorageException
     *             if a Storage service error occurs.
     */
    protected <T extends TableEntity, R> ResultSegment<?> executeQuerySegmentedImpl(final TableQuery<T> queryToExecute,
            final EntityResolver<R> resolver, final ResultContinuation continuationToken, TableRequestOptions options,
            OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = TableRequestOptions.populateAndApplyDefaults(options, this);

        Utility.assertContinuationType(continuationToken, ResultContinuationType.TABLE);

        SegmentedStorageRequest segmentedRequest = new SegmentedStorageRequest();
        segmentedRequest.setToken(continuationToken);

        return ExecutionEngine.executeWithRetry(this, queryToExecute,
                this.executeQuerySegmentedWithResolverCoreImpl(queryToExecute, resolver, options, segmentedRequest),
                options.getRetryPolicyFactory(), opContext);
    }

    private <T extends TableEntity, R> StorageRequest<CloudTableClient, TableQuery<T>, ResultSegment<T>> executeQuerySegmentedCoreImpl(
            final TableQuery<T> queryToExecute, final EntityResolver<R> resolver, final TableRequestOptions options,
            final SegmentedStorageRequest segmentedRequest) {

        if (resolver == null) {
            Utility.assertNotNull(SR.QUERY_REQUIRES_VALID_CLASSTYPE_OR_RESOLVER, queryToExecute.getClazzType());
        }

        final StorageRequest<CloudTableClient, TableQuery<T>, ResultSegment<T>> getRequest = new StorageRequest<CloudTableClient, TableQuery<T>, ResultSegment<T>>(
                options, this.getStorageUri()) {

            @Override
            public void setRequestLocationMode() {
                this.setRequestLocationMode(Utility.getListingLocationMode(segmentedRequest.getToken()));
            }

            @Override
            public HttpURLConnection buildRequest(CloudTableClient client, TableQuery<T> queryRef,
                    OperationContext context) throws Exception {
                return TableRequest.query(client.getTransformedEndPoint(context).getUri(this.getCurrentLocation()),
                        options, queryToExecute.generateQueryBuilder(), context, queryToExecute.getSourceTableName(),
                        null/* identity */, segmentedRequest.getToken());
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudTableClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signTableRequest(connection, client, -1L, context);
            }

            @Override
            public ResultSegment<T> preProcessResponse(TableQuery<T> queryRef, CloudTableClient client,
                    OperationContext context) throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    throw TableServiceException.generateTableServiceException(this.getResult(), null, 
                            this.getConnection().getErrorStream(), options.getTablePayloadFormat());
                }

                return null;
            }

            @SuppressWarnings("unchecked")
            @Override
            public ResultSegment<T> postProcessResponse(HttpURLConnection connection, TableQuery<T> queryRef,
                    CloudTableClient client, OperationContext context, ResultSegment<T> storageObject) throws Exception {
                ODataPayload<T> clazzResponse = null;

                InputStream inStream = connection.getInputStream();

                clazzResponse = (ODataPayload<T>) TableDeserializer.parseQueryResponse(inStream, options,
                        queryToExecute.getClazzType(), null, context);

                final ResultContinuation nextToken = TableResponse.getTableContinuationFromResponse(connection);
                if (nextToken != null) {
                    nextToken.setTargetLocation(this.getResult().getTargetLocation());
                }

                // Note, setting the token on the SegmentedStorageRequest is
                // key, this is how the iterator will share the token across executions
                segmentedRequest.setToken(nextToken);

                return new ResultSegment<T>(clazzResponse.results,
                        queryToExecute.getTakeCount() == null ? clazzResponse.results.size()
                                : queryToExecute.getTakeCount(), nextToken);
            }

            @Override
            public StorageExtendedErrorInformation parseErrorDetails() {
                return TableStorageErrorDeserializer.parseErrorDetails(this);
            }
        };

        return getRequest;
    }

    private <T extends TableEntity, R> StorageRequest<CloudTableClient, TableQuery<T>, ResultSegment<R>> executeQuerySegmentedWithResolverCoreImpl(
            final TableQuery<T> queryToExecute, final EntityResolver<R> resolver, final TableRequestOptions options,
            final SegmentedStorageRequest segmentedRequest) {

        if (resolver == null) {
            Utility.assertNotNull(SR.QUERY_REQUIRES_VALID_CLASSTYPE_OR_RESOLVER, queryToExecute.getClazzType());
        }

        final StorageRequest<CloudTableClient, TableQuery<T>, ResultSegment<R>> getRequest = new StorageRequest<CloudTableClient, TableQuery<T>, ResultSegment<R>>(
                options, this.getStorageUri()) {

            @Override
            public void setRequestLocationMode() {
                this.setRequestLocationMode(Utility.getListingLocationMode(segmentedRequest.getToken()));
            }

            @Override
            public HttpURLConnection buildRequest(CloudTableClient client, TableQuery<T> queryRef,
                    OperationContext context) throws Exception {
                return TableRequest.query(client.getTransformedEndPoint(context).getUri(this.getCurrentLocation()),
                        options, queryToExecute.generateQueryBuilder(), context, queryToExecute.getSourceTableName(),
                        null/* identity */, segmentedRequest.getToken());
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudTableClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signTableRequest(connection, client, -1L, context);
            }

            @Override
            public ResultSegment<R> preProcessResponse(TableQuery<T> queryRef, CloudTableClient client,
                    OperationContext context) throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    throw TableServiceException.generateTableServiceException(this.getResult(), null, 
                            this.getConnection().getErrorStream(), options.getTablePayloadFormat());
                }

                return null;
            }

            @SuppressWarnings("unchecked")
            @Override
            public ResultSegment<R> postProcessResponse(HttpURLConnection connection, TableQuery<T> queryRef,
                    CloudTableClient client, OperationContext context, ResultSegment<R> storageObject) throws Exception {
                ODataPayload<R> resolvedResponse = null;

                InputStream inStream = connection.getInputStream();

                resolvedResponse = (ODataPayload<R>) TableDeserializer.parseQueryResponse(inStream, options,
                        queryToExecute.getClazzType(), resolver, context);

                final ResultContinuation nextToken = TableResponse.getTableContinuationFromResponse(connection);
                if (nextToken != null) {
                    nextToken.setTargetLocation(this.getResult().getTargetLocation());
                }

                // Note, setting the token on the SegmentedStorageRequest is
                // key, this is how the iterator will share the token across executions
                segmentedRequest.setToken(nextToken);

                return new ResultSegment<R>(resolvedResponse.results,
                        queryToExecute.getTakeCount() == null ? resolvedResponse.results.size()
                                : queryToExecute.getTakeCount(), nextToken);

            }

            @Override
            public StorageExtendedErrorInformation parseErrorDetails() {
                return TableStorageErrorDeserializer.parseErrorDetails(this);
            }
        };

        return getRequest;
    }

    protected final StorageUri getTransformedEndPoint(final OperationContext opContext) throws URISyntaxException,
            StorageException {
        return this.getCredentials().transformUri(this.getStorageUri(), opContext);
    }

    /**
     * Reserved for internal use. Generates an iterator for a segmented query operation.
     * 
     * @param queryRef
     *            The {@link TableQuery} to execute.
     * @param resolver
     *            An {@link EntityResolver} instance which creates a projection of the table query result entities into
     *            the specified type <code>R</code>. Pass <code>null</code> to return the results as the table entity
     *            type.
     * @param options
     *            A {@link TableRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudTableClient}.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation. Specify <code>null</code> to
     *            safely ignore operation context.
     * @return
     *         An instance of <code>Iterable</code> specialized for the {@link TableEntity} or {@link EntityResolver}
     *         type returned by the query.
     */
    protected <T extends TableEntity, R> Iterable<?> generateIteratorForQuery(final TableQuery<T> queryRef,
            final EntityResolver<R> resolver, TableRequestOptions options, OperationContext opContext) {

        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = TableRequestOptions.populateAndApplyDefaults(options, this);

        SegmentedStorageRequest segmentedRequest = new SegmentedStorageRequest();

        if (resolver == null) {
            return new LazySegmentedIterable<CloudTableClient, TableQuery<T>, T>(this.executeQuerySegmentedCoreImpl(
                    queryRef, resolver, options, segmentedRequest), this, queryRef, options.getRetryPolicyFactory(),
                    opContext);
        }
        else {
            return new LazySegmentedIterable<CloudTableClient, TableQuery<T>, R>(
                    this.executeQuerySegmentedWithResolverCoreImpl(queryRef, resolver, options, segmentedRequest),
                    this, queryRef, options.getRetryPolicyFactory(), opContext);
        }
    }

    /**
     * Queries the service for the {@link ServiceStats}.
     * 
     * @return {@link ServiceStats} for the given storage service
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public ServiceStats getServiceStats() throws StorageException {
        return this.getServiceStats(null /* options */, null /* opContext */);
    }

    /**
     * Queries the given storage service for the {@link ServiceStats}.
     * 
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @return {@link ServiceStats} for the given storage service
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public ServiceStats getServiceStats(TableRequestOptions options, OperationContext opContext)
            throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = TableRequestOptions.populateAndApplyDefaults(options, this);

        return ExecutionEngine.executeWithRetry(this, null, this.getServiceStatsImpl(options, true),
                options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Retrieves the current {@link ServiceProperties} for the given storage service. This includes Logging,
     * HourMetrics, MinuteMetrics and CORS configurations.
     * 
     * @return the {@link ServiceProperties} object representing the current configuration of the service.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final ServiceProperties downloadServiceProperties() throws StorageException {
        return this.downloadServiceProperties(null /* options */, null /* opContext */);
    }

    /**
     * Retrieves the current {@link ServiceProperties} for the given storage service. This includes Logging,
     * HourMetrics, MinuteMetrics and CORS configurations.
     * 
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return the {@link ServiceProperties} object representing the current configuration of the service.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final ServiceProperties downloadServiceProperties(TableRequestOptions options, OperationContext opContext)
            throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = TableRequestOptions.populateAndApplyDefaults(options, this);

        return ExecutionEngine.executeWithRetry(this, null, this.downloadServicePropertiesImpl(options, true),
                options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Uploads a new {@link ServiceProperties} configuration to the given storage service. This includes Logging,
     * HourMetrics, MinuteMetrics and CORS configurations.
     * 
     * @param properties
     *            The {@link ServiceProperties} to upload.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void uploadServiceProperties(final ServiceProperties properties) throws StorageException {
        this.uploadServiceProperties(properties, null /* options */, null /* opContext */);
    }

    /**
     * Uploads a new {@link ServiceProperties} configuration to the given storage service. This includes Logging,
     * HourMetrics, MinuteMetrics and CORS configurations.
     * 
     * @param properties
     *            The {@link ServiceProperties} to upload.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void uploadServiceProperties(final ServiceProperties properties, TableRequestOptions options,
            OperationContext opContext) throws StorageException {
        if (!Utility.isNullOrEmpty(properties.getDefaultServiceVersion())) {
            throw new IllegalArgumentException(SR.DEFAULT_SERVICE_VERSION_ONLY_SET_FOR_BLOB_SERVICE);
        }

        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = TableRequestOptions.populateAndApplyDefaults(options, this);

        Utility.assertNotNull("properties", properties);

        ExecutionEngine.executeWithRetry(this, null,
                this.uploadServicePropertiesImpl(properties, options, opContext, true),
                options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Gets the {@link TableRequestOptions} that is used for requests associated with this <code>CloudTableClient</code>
     * 
     * @return
     *         The {@link TableRequestOptions} object containing the values used by this <code>CloudTableClient</code>
     */
    @Override
    public TableRequestOptions getDefaultRequestOptions() {
        return this.defaultRequestOptions;
    }

    /**
     * Sets the {@link TableRequestOptions} that is used for any table accessed with this <code>CloudTableClient</code>
     * object.
     * 
     * @param defaultRequestOptions
     *            The TableRequestOptions to use.
     */
    public void setDefaultRequestOptions(TableRequestOptions defaultRequestOptions) {
        Utility.assertNotNull("defaultRequestOptions", defaultRequestOptions);
        this.defaultRequestOptions = defaultRequestOptions;
    }

    /**
     * Indicates whether path-style URIs are used.
     * 
     * @return <code>true</code> if path-style URIs are used; otherwise <code>false</code>.
     */
    @Override
    protected boolean isUsePathStyleUris() {
        return super.isUsePathStyleUris();
    }
}