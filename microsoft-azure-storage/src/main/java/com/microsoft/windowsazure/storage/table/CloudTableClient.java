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

package com.microsoft.windowsazure.storage.table;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.microsoft.windowsazure.storage.DoesServiceRequest;
import com.microsoft.windowsazure.storage.OperationContext;
import com.microsoft.windowsazure.storage.ResultContinuation;
import com.microsoft.windowsazure.storage.ResultContinuationType;
import com.microsoft.windowsazure.storage.ResultSegment;
import com.microsoft.windowsazure.storage.ServiceClient;
import com.microsoft.windowsazure.storage.ServiceProperties;
import com.microsoft.windowsazure.storage.ServiceStats;
import com.microsoft.windowsazure.storage.StorageCredentials;
import com.microsoft.windowsazure.storage.StorageException;
import com.microsoft.windowsazure.storage.StorageUri;
import com.microsoft.windowsazure.storage.core.ExecutionEngine;
import com.microsoft.windowsazure.storage.core.LazySegmentedIterable;
import com.microsoft.windowsazure.storage.core.SR;
import com.microsoft.windowsazure.storage.core.SegmentedStorageRequest;
import com.microsoft.windowsazure.storage.core.StorageRequest;
import com.microsoft.windowsazure.storage.core.Utility;
import com.microsoft.windowsazure.storage.queue.CloudQueue;

/**
 * Provides a service client for accessing the Windows Azure Table service.
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
 * href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179360.aspx">Addressing Table Service Resources</a>.
 * <p>
 * The credentials can be a combination of the storage account name and a key, or a shared access signature. For more
 * information, see the MSDN topic <a
 * href="http://msdn.microsoft.com/en-us/library/windowsazure/hh225339.aspx">Authenticating Access to Your Storage
 * Account</a>.
 * 
 */
public final class CloudTableClient extends ServiceClient {
    /**
     * The {@link TablePayloadFormat} that is used for any table accessed with this <code>CloudTableClient</code>
     * object.
     * 
     * Default is Json.
     */
    private TablePayloadFormat payloadFormat = TablePayloadFormat.Json;

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
     * Initializes an instance of the {@link CloudTableClient} class using a Table service endpoint.
     * <p>
     * A {@link CloudTableClient} initialized with this constructor must have storage account credentials added before
     * it can be used to access the Windows Azure storage service.
     * 
     * @param baseUri
     *            A <code>java.net.URI</code> that represents the Table service endpoint used to initialize the
     *            client.
     */
    public CloudTableClient(final URI baseUri) {
        this(new StorageUri(baseUri));
    }

    /**
     * Initializes an instance of the {@link CloudTableClient} class using a Table service endpoint.
     * <p>
     * A {@link CloudTableClient} initialized with this constructor must have storage account credentials added before
     * it can be used to access the Windows Azure storage service.
     * 
     * @param baseUri
     *            A <code>StorageUri</code> that represents the Table service endpoint used to initialize the
     *            client.
     */
    public CloudTableClient(final StorageUri baseUri) {
        this(baseUri, null);
        this.setTimeoutInMs(TableConstants.TABLE_DEFAULT_TIMEOUT_IN_MS);
    }

    /**
     * Initializes an instance of the {@link CloudTableClient} class using a Table service endpoint and
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
     * Initializes an instance of the {@link CloudTableClient} class using a Table service endpoint and
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
        this.setTimeoutInMs(TableConstants.TABLE_DEFAULT_TIMEOUT_IN_MS);
    }

    /**
     * Gets a {@link CloudTable} object that represents the storage service
     * queue for the specified address.
     * 
     * @param tableAddress
     *            A <code>String</code> that represents the name of the table,
     *            or the absolute URI to the queue.
     * 
     * @return A {@link CloudQueue} object that represents a reference to the
     *         table.
     * 
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    public CloudTable getTableReference(final String tableAddress) throws URISyntaxException, StorageException {
        Utility.assertNotNullOrEmpty("tableAddress", tableAddress);
        return new CloudTable(tableAddress, this);
    }

    /**
     * Executes the specified batch operation on a table as an atomic operation. A batch operation may contain up to 100
     * individual table operations, with the requirement that each operation entity must have same partition key. Only
     * one retrieve operation is allowed per batch. Note that the total payload of a batch operation is limited to 4MB.
     * <p>
     * This method invokes an <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd894038.aspx">Entity Group
     * Transaction</a> on the REST API to execute the specified batch operation on the table as an atomic unit, using
     * the Table service endpoint and storage account credentials of this instance.
     * 
     * @param tableName
     *            A <code>String</code> containing the name of the table to execute the operations on.
     * @param batch
     *            The {@link TableBatchOperation} object representing the operations to execute on the table.
     * 
     * @return
     *         A <code>java.util.ArrayList</code> of {@link TableResult} that contains the results, in order, of
     *         each {@link TableOperation} in the {@link TableBatchOperation} on the named table.
     * 
     * @throws StorageException
     *             if an error occurs accessing the storage service, or the operation fails.
     */
    @DoesServiceRequest
    public ArrayList<TableResult> execute(final String tableName, final TableBatchOperation batch)
            throws StorageException {
        return this.execute(tableName, batch, null /* options */, null /* opContext */);
    }

    /**
     * Executes the specified batch operation on a table as an atomic operation, using the specified
     * {@link TableRequestOptions} and {@link OperationContext}. A batch operation may contain up to 100 individual
     * table operations, with the requirement that each operation entity must have same partition key. Only one retrieve
     * operation is allowed per batch. Note that the total payload of a batch operation is limited to 4MB.
     * <p>
     * This method invokes an <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd894038.aspx">Entity Group
     * Transaction</a> on the REST API to execute the specified batch operation on the table as an atomic unit, using
     * the Table service endpoint and storage account credentials of this instance.
     * 
     * Use the {@link TableRequestOptions} to override execution options such as the timeout or retry policy for the
     * operation.
     * 
     * @param tableName
     *            A <code>String</code> containing the name of the table to execute the operations on.
     * @param batch
     *            The {@link TableBatchOperation} object representing the operations to execute on the table.
     * @param options
     *            A {@link TableRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudTableClient}.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation. Specify <code>null</code> to
     *            safely ignore operation context.
     * 
     * @return
     *         A <code>java.util.ArrayList</code> of {@link TableResult} that contains the results, in order, of
     *         each {@link TableOperation} in the {@link TableBatchOperation} on the named table.
     * 
     * @throws StorageException
     *             if an error occurs accessing the storage service, or the operation fails.
     */
    @DoesServiceRequest
    public ArrayList<TableResult> execute(final String tableName, final TableBatchOperation batch,
            TableRequestOptions options, OperationContext opContext) throws StorageException {
        Utility.assertNotNull("batch", batch);
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = TableRequestOptions.applyDefaults(options, this);
        return batch.execute(this, tableName, options, opContext);
    }

    /**
     * Executes the operation on a table.
     * <p>
     * This method will invoke the <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179423.aspx">Table
     * Service REST API</a> to execute the specified operation on the table, using the Table service endpoint and
     * storage account credentials of this instance.
     * 
     * @param tableName
     *            A <code>String</code> containing the name of the table to execute the operation on.
     * @param operation
     *            The {@link TableOperation} object representing the operation to execute on the table.
     * 
     * @return
     *         A {@link TableResult} containing the result of executing the {@link TableOperation} on the table.
     * 
     * @throws StorageException
     *             if an error occurs accessing the storage service, or the operation fails.
     */
    @DoesServiceRequest
    public TableResult execute(final String tableName, final TableOperation operation) throws StorageException {
        return this.execute(tableName, operation, null /* options */, null /* opContext */);
    }

    /**
     * Executes the operation on a table, using the specified {@link TableRequestOptions} and {@link OperationContext}.
     * <p>
     * This method will invoke the <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179423.aspx">Table
     * Service REST API</a> to execute the specified operation on the table, using the Table service endpoint and
     * storage account credentials of this instance.
     * 
     * Use the {@link TableRequestOptions} to override execution options such as the timeout or retry policy for the
     * operation.
     * 
     * @param tableName
     *            A <code>String</code> containing the name of the table to execute the operation on.
     * @param operation
     *            The {@link TableOperation} object representing the operation to execute on the table.
     * @param options
     *            A {@link TableRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudTableClient}.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation. Specify <code>null</code> to
     *            safely ignore operation context.
     * 
     * @return
     *         A {@link TableResult} containing the result of executing the {@link TableOperation} on the table.
     * 
     * @throws StorageException
     *             if an error occurs accessing the storage service, or the operation fails.
     */
    @DoesServiceRequest
    public TableResult execute(final String tableName, final TableOperation operation,
            final TableRequestOptions options, final OperationContext opContext) throws StorageException {
        Utility.assertNotNull("operation", operation);
        return operation.execute(this, tableName, options, opContext);
    }

    /**
     * Executes a query, applying the specified {@link EntityResolver} to the result.
     * <p>
     * This method will invoke a <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179421.aspx">Query
     * Entities</a> operation on the <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179423.aspx">Table
     * Service REST API</a> to query the table, using the Table service endpoint and storage account credentials of this
     * instance.
     * 
     * @param query
     *            A {@link TableQuery} instance specifying the table to query and the query parameters to use.
     * @param resolver
     *            An {@link EntityResolver} instance which creates a projection of the table query result entities into
     *            the specified type <code>R</code>.
     * 
     * @return
     *         A collection implementing the <code>Iterable</code> interface containing the projection into type
     *         <code>R</code> of the results of executing the query.
     * @throws StorageException
     */
    @DoesServiceRequest
    public <R> Iterable<R> execute(final TableQuery<?> query, final EntityResolver<R> resolver) throws StorageException {
        return this.execute(query, resolver, null /* options */, null /* opContext */);
    }

    /**
     * Executes a query, applying the specified {@link EntityResolver} to the result, using the
     * specified {@link TableRequestOptions} and {@link OperationContext}.
     * <p>
     * This method will invoke a <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179421.aspx">Query
     * Entities</a> operation on the <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179423.aspx">Table
     * Service REST API</a> to query the table, using the Table service endpoint and storage account credentials of this
     * instance.
     * 
     * Use the {@link TableRequestOptions} to override execution options such as the timeout or retry policy for the
     * operation.
     * 
     * @param query
     *            A {@link TableQuery} instance specifying the table to query and the query parameters to use.
     * @param resolver
     *            An {@link EntityResolver} instance which creates a projection of the table query result entities into
     *            the specified type <code>R</code>.
     * @param options
     *            A {@link TableRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudTableClient}.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation. Specify <code>null</code> to
     *            safely ignore operation context.
     * 
     * @return
     *         A collection implementing the <code>Iterable</code> interface containing the projection into type
     *         <code>R</code> of the results of executing the query.
     * @throws StorageException
     */
    @DoesServiceRequest
    @SuppressWarnings("unchecked")
    public <R> Iterable<R> execute(final TableQuery<?> query, final EntityResolver<R> resolver,
            final TableRequestOptions options, final OperationContext opContext) throws StorageException {
        Utility.assertNotNull("query", query);
        Utility.assertNotNull(SR.QUERY_REQUIRES_VALID_CLASSTYPE_OR_RESOLVER, resolver);
        return (Iterable<R>) this.generateIteratorForQuery(query, resolver, options, opContext);
    }

    /**
     * Executes a query.
     * <p>
     * This method will invoke a <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179421.aspx">Query
     * Entities</a> operation on the <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179423.aspx">Table
     * Service REST API</a> to query the table, using the Table service endpoint and storage account credentials of this
     * instance.
     * 
     * @param query
     *            A {@link TableQuery} instance specifying the table to query and the query parameters to use,
     *            specialized for a type T implementing {@link TableEntity}.
     * 
     * @return
     *         A collection implementing the <code>Iterable</code> interface specialized for type T of the results of
     *         executing the query.
     * @throws StorageException
     */
    @DoesServiceRequest
    public <T extends TableEntity> Iterable<T> execute(final TableQuery<T> query) throws StorageException {
        return this.execute(query, null /* options */, null /* opContext */);
    }

    /**
     * Executes a query, using the specified {@link TableRequestOptions} and {@link OperationContext}.
     * <p>
     * This method will invoke a <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179421.aspx">Query
     * Entities</a> operation on the <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179423.aspx">Table
     * Service REST API</a> to query the table, using the Table service endpoint and storage account credentials of this
     * instance.
     * 
     * Use the {@link TableRequestOptions} to override execution options such as the timeout or retry policy for the
     * operation.
     * 
     * @param query
     *            A {@link TableQuery} instance specifying the table to query and the query parameters to use,
     *            specialized for a type T implementing {@link TableEntity}.
     * @param options
     *            A {@link TableRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudTableClient}.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation. Specify <code>null</code> to
     *            safely ignore operation context.
     * 
     * @return
     *         A collection implementing the <code>Iterable</code> interface specialized for type T of the results of
     *         executing the query.
     * @throws StorageException
     */
    @SuppressWarnings("unchecked")
    @DoesServiceRequest
    public <T extends TableEntity> Iterable<T> execute(final TableQuery<T> query, final TableRequestOptions options,
            final OperationContext opContext) throws StorageException {
        Utility.assertNotNull("query", query);
        return (Iterable<T>) this.generateIteratorForQuery(query, null, options, opContext);
    }

    /**
     * Executes a query in segmented mode with the specified {@link ResultContinuation} continuation token,
     * applying the {@link EntityResolver} to the result.
     * Executing a query with <code>executeSegmented</code> allows the query to be resumed after returning partial
     * results, using information returned by the server in the {@link ResultSegment} object.
     * <p>
     * This method will invoke a <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179421.aspx">Query
     * Entities</a> operation on the <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179423.aspx">Table
     * Service REST API</a> to query the table, using the Table service endpoint and storage account credentials of this
     * instance.
     * 
     * @param query
     *            A {@link TableQuery} instance specifying the table to query and the query parameters to use.
     * @param resolver
     *            An {@link EntityResolver} instance which creates a projection of the table query result entities into
     *            the specified type <code>R</code>.
     * @param continuationToken
     *            A {@link ResultContinuation} object representing a continuation token from the server when the
     *            operation returns a partial result. Specify <code>null</code> on the initial call. Call the
     *            {@link ResultSegment#getContinuationToken()} method on the result to obtain the
     *            {@link ResultContinuation} object to use in the next call to resume the query.
     * 
     * @return
     *         A {@link ResultSegment} containing the projection into type <code>R</code> of the results of executing
     *         the query.
     * 
     * @throws IOException
     *             if an IO error occurred during the operation.
     * @throws URISyntaxException
     *             if the URI generated for the query is invalid.
     * @throws StorageException
     *             if a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public <R> ResultSegment<R> executeSegmented(final TableQuery<?> query, final EntityResolver<R> resolver,
            final ResultContinuation continuationToken) throws IOException, URISyntaxException, StorageException {
        return this.executeSegmented(query, resolver, continuationToken, null /* options */, null /* opContext */);
    }

    /**
     * Executes a query in segmented mode with the specified {@link ResultContinuation} continuation token,
     * using the specified {@link TableRequestOptions} and {@link OperationContext}, applying the {@link EntityResolver}
     * to the result.
     * Executing a query with <code>executeSegmented</code> allows the query to be resumed after returning partial
     * results, using information returned by the server in the {@link ResultSegment} object.
     * <p>
     * This method will invoke a <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179421.aspx">Query
     * Entities</a> operation on the <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179423.aspx">Table
     * Service REST API</a> to query the table, using the Table service endpoint and storage account credentials of this
     * instance.
     * 
     * Use the {@link TableRequestOptions} to override execution options such as the timeout or retry policy for the
     * operation.
     * 
     * @param query
     *            A {@link TableQuery} instance specifying the table to query and the query parameters to use.
     * @param resolver
     *            An {@link EntityResolver} instance which creates a projection of the table query result entities into
     *            the specified type <code>R</code>.
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
     *         A {@link ResultSegment} containing the projection into type <code>R</code> of the results of executing
     *         the query.
     * 
     * @throws IOException
     *             if an IO error occurred during the operation.
     * @throws URISyntaxException
     *             if the URI generated for the query is invalid.
     * @throws StorageException
     *             if a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    @SuppressWarnings("unchecked")
    public <R> ResultSegment<R> executeSegmented(final TableQuery<?> query, final EntityResolver<R> resolver,
            final ResultContinuation continuationToken, final TableRequestOptions options,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {
        Utility.assertNotNull(SR.QUERY_REQUIRES_VALID_CLASSTYPE_OR_RESOLVER, resolver);
        return (ResultSegment<R>) this
                .executeQuerySegmentedImpl(query, resolver, continuationToken, options, opContext);
    }

    /**
     * Executes a query in segmented mode with a {@link ResultContinuation} continuation token.
     * Executing a query with <code>executeSegmented</code> allows the query to be resumed after returning partial
     * results, using information returned by the server in the {@link ResultSegment} object.
     * <p>
     * This method will invoke a <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179421.aspx">Query
     * Entities</a> operation on the <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179423.aspx">Table
     * Service REST API</a> to query the table, using the Table service endpoint and storage account credentials of this
     * instance.
     * 
     * @param query
     *            A {@link TableQuery} instance specifying the table to query and the query parameters to use,
     *            specialized for a type T implementing {@link TableEntity}.
     * @param continuationToken
     *            A {@link ResultContinuation} object representing a continuation token from the server when the
     *            operation returns a partial result. Specify <code>null</code> on the initial call. Call the
     *            {@link ResultSegment#getContinuationToken()} method on the result to obtain the
     *            {@link ResultContinuation} object to use in the next call to resume the query.
     * 
     * @return
     *         A {@link ResultSegment} specialized for type T of the results of executing the query.
     * 
     * @throws IOException
     *             if an IO error occurred during the operation.
     * @throws URISyntaxException
     *             if the URI generated for the query is invalid.
     * @throws StorageException
     *             if a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public <T extends TableEntity> ResultSegment<T> executeSegmented(final TableQuery<T> query,
            final ResultContinuation continuationToken) throws IOException, URISyntaxException, StorageException {
        return this.executeSegmented(query, continuationToken, null /* options */, null /* opContext */);
    }

    /**
     * Executes a query in segmented mode with a {@link ResultContinuation} continuation token,
     * using the specified {@link TableRequestOptions} and {@link OperationContext}.
     * Executing a query with <code>executeSegmented</code> allows the query to be resumed after returning partial
     * results, using information returned by the server in the {@link ResultSegment} object.
     * <p>
     * This method will invoke a <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179421.aspx">Query
     * Entities</a> operation on the <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179423.aspx">Table
     * Service REST API</a> to query the table, using the Table service endpoint and storage account credentials of this
     * instance.
     * 
     * Use the {@link TableRequestOptions} to override execution options such as the timeout or retry policy for the
     * operation.
     * 
     * @param query
     *            A {@link TableQuery} instance specifying the table to query and the query parameters to use,
     *            specialized for a type T implementing {@link TableEntity}.
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
     *         A {@link ResultSegment} specialized for type T of the results of executing the query.
     * 
     * @throws IOException
     *             if an IO error occurred during the operation.
     * @throws URISyntaxException
     *             if the URI generated for the query is invalid.
     * @throws StorageException
     *             if a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    @SuppressWarnings("unchecked")
    public <T extends TableEntity> ResultSegment<T> executeSegmented(final TableQuery<T> query,
            final ResultContinuation continuationToken, final TableRequestOptions options,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {
        Utility.assertNotNull("query", query);
        return (ResultSegment<T>) this.executeQuerySegmentedImpl(query, null, continuationToken, options, opContext);
    }

    /**
     * Lists the table names in the storage account.
     * <p>
     * This method invokes the <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179405.aspx">Query
     * Tables</a> REST API to list the table names, using the Table service endpoint and storage account credentials of
     * this instance.
     * 
     * @return
     *         An <code>Iterable</code> collection of the table names in the storage account retrieved lazily.
     * @throws StorageException
     */
    @DoesServiceRequest
    public Iterable<String> listTables() throws StorageException {
        return this.listTables(null);
    }

    /**
     * Lists the table names in the storage account that match the specified prefix.
     * <p>
     * This method invokes the <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179405.aspx">Query
     * Tables</a> REST API to list the table names that match the prefix, using the Table service endpoint and storage
     * account credentials of this instance.
     * 
     * @param prefix
     *            A <code>String</code> containing the prefix to match on table names to return.
     * 
     * @return
     *         An <code>Iterable</code> collection of the table names in the storage account retrieved lazily that match
     *         the specified
     *         prefix.
     * @throws StorageException
     */
    @DoesServiceRequest
    public Iterable<String> listTables(final String prefix) throws StorageException {
        return this.listTables(prefix, null /* options */, null /* opContext */);
    }

    /**
     * Lists the table names in the storage account that match the specified prefix, using the specified
     * {@link TableRequestOptions} and {@link OperationContext}.
     * <p>
     * This method invokes the <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179405.aspx">Query
     * Tables</a> REST API to list the table names that match the prefix, using the Table service endpoint and storage
     * account credentials of this instance.
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
     * @throws StorageException
     */
    @DoesServiceRequest
    public Iterable<String> listTables(final String prefix, final TableRequestOptions options,
            final OperationContext opContext) throws StorageException {
        return this.execute(this.generateListTablesQuery(prefix), this.tableNameResolver, options, opContext);
    }

    /**
     * Lists the table names in the storage account in segmented mode. This method allows listing of tables to be
     * resumed after returning a partial set of results, using information returned by the server in the
     * {@link ResultSegment} object.
     * <p>
     * This method invokes the <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179405.aspx">Query
     * Tables</a> REST API to list the table names, using the Table service endpoint and storage account credentials of
     * this instance.
     * 
     * @return
     *         A {@link ResultSegment} of <code>String</code> objects containing table names in the storage account.
     * 
     * @throws IOException
     *             if an IO error occurred during the operation.
     * @throws URISyntaxException
     *             if the URI generated for the operation is invalid.
     * @throws StorageException
     *             if a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public ResultSegment<String> listTablesSegmented() throws IOException, URISyntaxException, StorageException {
        return this.listTablesSegmented(null);
    }

    /**
     * Lists the table names in the storage account that match the specified prefix in segmented mode. This method
     * allows listing of tables to be resumed after returning a partial set of results, using information returned by
     * the server in the {@link ResultSegment} object.
     * <p>
     * This method invokes the <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179405.aspx">Query
     * Tables</a> REST API to list the table names that match the prefix, using the Table service endpoint and storage
     * account credentials of this instance.
     * 
     * @param prefix
     *            A <code>String</code> containing the prefix to match on table names to return.
     * 
     * @return
     *         A {@link ResultSegment} of <code>String</code> objects containing table names matching the prefix in the
     *         storage account.
     * 
     * @throws IOException
     *             if an IO error occurred during the operation.
     * @throws URISyntaxException
     *             if the URI generated for the operation is invalid.
     * @throws StorageException
     *             if a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public ResultSegment<String> listTablesSegmented(final String prefix) throws IOException, URISyntaxException,
            StorageException {
        return this.listTablesSegmented(prefix, null /* maxResults */, null /* continuationToken */,
                null /* options */, null /* opContext */);
    }

    /**
     * Lists up to the specified maximum of the table names in the storage account that match the specified prefix in a
     * resumable mode with the specified {@link ResultContinuation} continuation token, using the specified
     * {@link TableRequestOptions} and {@link OperationContext}. This method allows listing of tables to be resumed
     * after returning a page of results, using information returned by the server in the {@link ResultSegment} object.
     * <p>
     * This method invokes the <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179405.aspx">Query
     * Tables</a> REST API to list the table names that match the prefix, using the Table service endpoint and storage
     * account credentials of this instance.
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
     * @throws IOException
     *             if an IO error occurred during the operation.
     * @throws URISyntaxException
     *             if the URI generated for the operation is invalid.
     * @throws StorageException
     *             if a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public ResultSegment<String> listTablesSegmented(final String prefix, final Integer maxResults,
            final ResultContinuation continuationToken, final TableRequestOptions options,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {
        return this.executeSegmented(this.generateListTablesQuery(prefix).take(maxResults), this.tableNameResolver,
                continuationToken, options, opContext);
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
        TableQuery<TableServiceEntity> listQuery = TableQuery.<TableServiceEntity> from(
                TableConstants.TABLES_SERVICE_TABLES_NAME, TableServiceEntity.class);

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
    private <T extends TableEntity, R> ResultSegment<?> executeQuerySegmentedImpl(final TableQuery<T> queryToExecute,
            final EntityResolver<R> resolver, final ResultContinuation continuationToken, TableRequestOptions options,
            OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = TableRequestOptions.applyDefaults(options, this);

        Utility.assertContinuationType(continuationToken, ResultContinuationType.TABLE);

        SegmentedStorageRequest segmentedRequest = new SegmentedStorageRequest();
        segmentedRequest.setToken(continuationToken);

        return ExecutionEngine.executeWithRetry(this, queryToExecute,
                this.executeQuerySegmentedWithResolverCoreImpl(queryToExecute, resolver, options, segmentedRequest),
                options.getRetryPolicyFactory(), opContext);
    }

    private <T extends TableEntity, R> StorageRequest<CloudTableClient, TableQuery<T>, ResultSegment<T>> executeQuerySegmentedCoreImpl(
            final TableQuery<T> queryToExecute, final EntityResolver<R> resolver, final TableRequestOptions options,
            final SegmentedStorageRequest segmentedRequest) throws StorageException {

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
                        queryToExecute.getSourceTableName(), null/* identity */, options.getTimeoutIntervalInMs(),
                        queryToExecute.generateQueryBuilder(), segmentedRequest.getToken(), options, context);
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
                    throw TableServiceException.generateTableServiceException(true, this.getResult(), null, this
                            .getConnection().getErrorStream(), options.getTablePayloadFormat());
                }

                return null;
            }

            @SuppressWarnings("unchecked")
            @Override
            public ResultSegment<T> postProcessResponse(HttpURLConnection connection, TableQuery<T> queryRef,
                    CloudTableClient client, OperationContext context, ResultSegment<T> storageObject) throws Exception {
                ODataPayload<T> clazzResponse = null;

                InputStream inStream = connection.getInputStream();

                clazzResponse = (ODataPayload<T>) TableParser.parseQueryResponse(inStream, options,
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
        };

        return getRequest;
    }

    private <T extends TableEntity, R> StorageRequest<CloudTableClient, TableQuery<T>, ResultSegment<R>> executeQuerySegmentedWithResolverCoreImpl(
            final TableQuery<T> queryToExecute, final EntityResolver<R> resolver, final TableRequestOptions options,
            final SegmentedStorageRequest segmentedRequest) throws StorageException {

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
                        queryToExecute.getSourceTableName(), null/* identity */, options.getTimeoutIntervalInMs(),
                        queryToExecute.generateQueryBuilder(), segmentedRequest.getToken(), options, context);
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
                    throw TableServiceException.generateTableServiceException(true, this.getResult(), null, this
                            .getConnection().getErrorStream(), options.getTablePayloadFormat());
                }

                return null;
            }

            @SuppressWarnings("unchecked")
            @Override
            public ResultSegment<R> postProcessResponse(HttpURLConnection connection, TableQuery<T> queryRef,
                    CloudTableClient client, OperationContext context, ResultSegment<R> storageObject) throws Exception {
                ODataPayload<R> resolvedResponse = null;

                InputStream inStream = connection.getInputStream();

                resolvedResponse = (ODataPayload<R>) TableParser.parseQueryResponse(inStream, options,
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
        };

        return getRequest;
    }

    protected final StorageUri getTransformedEndPoint(final OperationContext opContext) throws URISyntaxException,
            StorageException {
        if (this.getCredentials().doCredentialsNeedTransformUri()) {
            if (this.getEndpoint().isAbsolute()) {
                return this.getCredentials().transformUri(this.getStorageUri(), opContext);
            }
            else {
                final StorageException ex = Utility.generateNewUnexpectedStorageException(null);
                ex.getExtendedErrorInformation().setErrorMessage(SR.TABLE_OBJECT_RELATIVE_URIS_NOT_SUPPORTED);
                throw ex;
            }
        }
        else {
            return this.getStorageUri();
        }
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
     * @throws StorageException
     */
    private <T extends TableEntity, R> Iterable<?> generateIteratorForQuery(final TableQuery<T> queryRef,
            final EntityResolver<R> resolver, TableRequestOptions options, OperationContext opContext)
            throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = TableRequestOptions.applyDefaults(options, this);

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
     * Queries the service to get the service statistics
     * 
     * @return ServiceStats for the given storage service
     * @throws StorageException
     */
    @DoesServiceRequest
    public ServiceStats getServiceStats() throws StorageException {
        return this.getServiceStats(null /* options */, null /* opContext */);
    }

    /**
     * Queries the service to get the service statistics
     * 
     * @param options
     *            A {@link TableRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudTableClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @return ServiceStats for the given storage service
     * @throws StorageException
     */
    @DoesServiceRequest
    public ServiceStats getServiceStats(TableRequestOptions options, OperationContext opContext)
            throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = TableRequestOptions.applyDefaults(options, this);

        return ExecutionEngine.executeWithRetry(this, null, this.getServiceStatsImpl(options, true),
                this.getRetryPolicyFactory(), opContext);
    }

    /**
     * Retrieves the current ServiceProperties for the given storage service. This includes Metrics and Logging
     * Configurations.
     * 
     * @return the ServiceProperties object representing the current configuration of the service.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final ServiceProperties downloadServiceProperties() throws StorageException {
        return this.downloadServiceProperties(null /* options */, null /* opContext */);
    }

    /**
     * Retrieves the current ServiceProperties for the given storage service. This includes Metrics and Logging
     * Configurations.
     * 
     * @param options
     *            A {@link TableRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudTableClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @return the ServiceProperties object representing the current configuration of the service.
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
        options = TableRequestOptions.applyDefaults(options, this);

        return ExecutionEngine.executeWithRetry(this, null, this.downloadServicePropertiesImpl(options, true),
                options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Uploads a new configuration to the storage service. This includes Metrics and Logging Configuration.
     * 
     * @param properties
     *            The ServiceProperties to upload.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void uploadServiceProperties(final ServiceProperties properties) throws StorageException {
        this.uploadServiceProperties(properties, null /* options */, null /* opContext */);
    }

    /**
     * Uploads a new configuration to the storage service. This includes Metrics and Logging Configuration.
     * 
     * @param properties
     *            The ServiceProperties to upload.
     * @param options
     *            A {@link TableRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudTableClient}).
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
        options = TableRequestOptions.applyDefaults(options, this);

        Utility.assertNotNull("properties", properties);

        ExecutionEngine.executeWithRetry(this, null,
                this.uploadServicePropertiesImpl(properties, options, opContext, true),
                options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Gets the {@link TablePayloadFormat} that is used for any table accessed with this <code>CloudTableClient</code>
     * object.
     * 
     * @return
     *         The {@link TablePayloadFormat} used by this <code>CloudTableClient</code>
     */
    public TablePayloadFormat getTablePayloadFormat() {
        return this.payloadFormat;
    }

    /**
     * Sets the {@link TablePayloadFormat} that is used for any table accessed with this <code>CloudTableClient</code>
     * object.
     * 
     * @param payloadFormat
     *            The TablePayloadFormat to use.
     */
    public void setTablePayloadFormat(TablePayloadFormat payloadFormat) {
        this.payloadFormat = payloadFormat;
    }
}
