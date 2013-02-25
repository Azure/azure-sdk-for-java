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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.xml.stream.XMLStreamException;

import com.microsoft.windowsazure.services.core.storage.DoesServiceRequest;
import com.microsoft.windowsazure.services.core.storage.OperationContext;
import com.microsoft.windowsazure.services.core.storage.ResultContinuation;
import com.microsoft.windowsazure.services.core.storage.ResultContinuationType;
import com.microsoft.windowsazure.services.core.storage.ResultSegment;
import com.microsoft.windowsazure.services.core.storage.ServiceClient;
import com.microsoft.windowsazure.services.core.storage.StorageCredentials;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.ExecutionEngine;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.LazySegmentedIterable;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.SegmentedStorageOperation;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.StorageOperation;
import com.microsoft.windowsazure.services.queue.client.CloudQueue;

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
        return this.execute(tableName, batch, null, null);
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

        if (options == null) {
            options = new TableRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this);
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
        return this.execute(tableName, operation, null, null);
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
     */
    @DoesServiceRequest
    public <R> Iterable<R> execute(final TableQuery<?> query, final EntityResolver<R> resolver) {
        return this.execute(query, resolver, null, null);
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
     */
    @DoesServiceRequest
    @SuppressWarnings("unchecked")
    public <R> Iterable<R> execute(final TableQuery<?> query, final EntityResolver<R> resolver,
            final TableRequestOptions options, final OperationContext opContext) {
        Utility.assertNotNull("query", query);
        Utility.assertNotNull("Query requires a valid class type or resolver.", resolver);
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
     */
    @DoesServiceRequest
    public <T extends TableEntity> Iterable<T> execute(final TableQuery<T> query) {
        return this.execute(query, null, null);
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
     */
    @SuppressWarnings("unchecked")
    @DoesServiceRequest
    public <T extends TableEntity> Iterable<T> execute(final TableQuery<T> query, final TableRequestOptions options,
            final OperationContext opContext) {
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
        return this.executeSegmented(query, resolver, continuationToken, null, null);
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
        Utility.assertNotNull("Query requires a valid class type or resolver.", resolver);
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
        return this.executeSegmented(query, continuationToken, null, null);
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
     *         An <code>Iterable</code> collection of the table names in the storage account.
     */
    @DoesServiceRequest
    public Iterable<String> listTables() {
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
     *         An <code>Iterable</code> collection of the table names in the storage account that match the specified
     *         prefix.
     */
    @DoesServiceRequest
    public Iterable<String> listTables(final String prefix) {
        return this.listTables(prefix, null, null);
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
     *         An <code>Iterable</code> collection of the table names in the storage account that match the specified
     *         prefix.
     */
    @DoesServiceRequest
    public Iterable<String> listTables(final String prefix, final TableRequestOptions options,
            final OperationContext opContext) {
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
        return this.listTablesSegmented(prefix, null, null, null, null);
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
     * Reserved for internal use. Implements the REST API call at the core of a segmented table query
     * operation.
     * 
     * @param queryToExecute
     *            The {@link TableQuery} to execute.
     * @param resolver
     *            An {@link EntityResolver} instance to use to project the result entity as an instance of type
     *            <code>R</code>. Pass <code>null</code> to return the results as the table entity type.
     * @param continuationToken
     *            The {@link ResultContinuation} to pass with the operation to resume a query, if any. Pass
     *            <code>null</code> for an initial query.
     * @param taskReference
     *            A reference to the {@link StorageOperation} implementing the segmented operation.
     * @param options
     *            A {@link TableRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation.
     * @return
     *         A {@link ResultSegment} containing a collection of the query results specialized for the
     *         {@link TableEntity} or {@link EntityResolver} type returned by the query.
     * @throws StorageException
     *             if a Storage service error occurs.
     * @throws IOException
     *             if an IO error occurs.
     * @throws URISyntaxException
     *             if the URI generated for the query is invalid.
     * @throws XMLStreamException
     *             if an error occurs accessing the <code>XMLStreamReader</code>.
     * @throws ParseException
     *             if an error occurs in parsing the response.
     * @throws InstantiationException
     *             if an error occurs in object construction.
     * @throws IllegalAccessException
     *             if an error occurs in reflection on an object type.
     * @throws InvalidKeyException
     *             if the key for an entity is invalid.
     */
    @SuppressWarnings("unchecked")
    protected <T extends TableEntity, R> ResultSegment<?> executeQuerySegmentedCore(final TableQuery<T> queryToExecute,
            final EntityResolver<R> resolver, final ResultContinuation continuationToken,
            final StorageOperation<?, ?, ?> taskReference, final TableRequestOptions options,
            final OperationContext opContext) throws StorageException, IOException, URISyntaxException,
            XMLStreamException, ParseException, InstantiationException, IllegalAccessException, InvalidKeyException {
        if (resolver == null) {
            Utility.assertNotNull("Query requires a valid class type or resolver.", queryToExecute.getClazzType());
        }

        final HttpURLConnection queryRequest = TableRequest.query(this.getTransformedEndPoint(opContext),
                queryToExecute.getSourceTableName(), null/* identity */, options.getTimeoutIntervalInMs(),
                queryToExecute.generateQueryBuilder(), continuationToken, options, opContext);

        this.getCredentials().signRequestLite(queryRequest, -1L, opContext);

        ExecutionEngine.processRequest(queryRequest, opContext, taskReference.getResult());

        if (taskReference.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
            throw TableServiceException.generateTableServiceException(true, taskReference.getResult(), null,
                    queryRequest.getErrorStream());
        }

        ODataPayload<T> clazzResponse = null;
        ODataPayload<R> resolvedResponse = null;

        InputStream inStream = queryRequest.getInputStream();

        try {
            if (resolver == null) {
                clazzResponse = (ODataPayload<T>) AtomPubParser.parseResponse(inStream, queryToExecute.getClazzType(),
                        null, opContext);
            }
            else {
                resolvedResponse = (ODataPayload<R>) AtomPubParser.parseResponse(inStream,
                        queryToExecute.getClazzType(), resolver, opContext);
            }
        }
        finally {
            inStream.close();
        }

        final ResultContinuation nextToken = TableResponse.getTableContinuationFromResponse(queryRequest);

        if (resolver == null) {
            return new ResultSegment<T>(clazzResponse.results,
                    queryToExecute.getTakeCount() == null ? clazzResponse.results.size()
                            : queryToExecute.getTakeCount(), nextToken);
        }
        else {
            return new ResultSegment<R>(resolvedResponse.results,
                    queryToExecute.getTakeCount() == null ? resolvedResponse.results.size()
                            : queryToExecute.getTakeCount(), nextToken);
        }
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

        if (options == null) {
            options = new TableRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this);

        Utility.assertContinuationType(continuationToken, ResultContinuationType.TABLE);

        final StorageOperation<CloudTableClient, TableQuery<T>, ResultSegment<?>> impl = new StorageOperation<CloudTableClient, TableQuery<T>, ResultSegment<?>>(
                options) {
            @Override
            public ResultSegment<?> execute(final CloudTableClient client, final TableQuery<T> queryRef,
                    final OperationContext opContext) throws Exception {

                return CloudTableClient.this.executeQuerySegmentedCore(queryRef, resolver, continuationToken, this,
                        (TableRequestOptions) this.getRequestOptions(), opContext);
            }
        };
        return ExecutionEngine.executeWithRetry(this, queryToExecute, impl, options.getRetryPolicyFactory(), opContext);
    }

    protected final URI getTransformedEndPoint(final OperationContext opContext) throws URISyntaxException,
            StorageException {
        if (this.getCredentials().doCredentialsNeedTransformUri()) {
            if (this.getEndpoint().isAbsolute()) {
                return this.getCredentials().transformUri(this.getEndpoint(), opContext);
            }
            else {
                final StorageException ex = Utility.generateNewUnexpectedStorageException(null);
                ex.getExtendedErrorInformation().setErrorMessage("Table Object relative URIs not supported.");
                throw ex;
            }
        }
        else {
            return this.getEndpoint();
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
     */
    protected <T extends TableEntity, R> Iterable<?> generateIteratorForQuery(final TableQuery<T> queryRef,
            final EntityResolver<R> resolver, TableRequestOptions options, OperationContext opContext) {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new TableRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this);

        if (resolver == null) {
            final SegmentedStorageOperation<CloudTableClient, TableQuery<T>, ResultSegment<T>> impl = new SegmentedStorageOperation<CloudTableClient, TableQuery<T>, ResultSegment<T>>(
                    options, null) {
                @Override
                public ResultSegment<T> execute(final CloudTableClient client, final TableQuery<T> queryToExecute,
                        final OperationContext opContext) throws Exception {

                    @SuppressWarnings("unchecked")
                    final ResultSegment<T> result = (ResultSegment<T>) CloudTableClient.this.executeQuerySegmentedCore(
                            queryToExecute, null, this.getToken(), this,
                            (TableRequestOptions) this.getRequestOptions(), opContext);

                    // Note, setting the token on the SegmentedStorageOperation is
                    // key, this is how the iterator will share the token across executions
                    if (result != null) {
                        this.setToken(result.getContinuationToken());
                    }

                    return result;
                }
            };

            return new LazySegmentedIterable<CloudTableClient, TableQuery<T>, T>(impl, this, queryRef,
                    options.getRetryPolicyFactory(), opContext);
        }
        else {
            final SegmentedStorageOperation<CloudTableClient, TableQuery<T>, ResultSegment<R>> impl = new SegmentedStorageOperation<CloudTableClient, TableQuery<T>, ResultSegment<R>>(
                    options, null) {
                @Override
                public ResultSegment<R> execute(final CloudTableClient client, final TableQuery<T> queryToExecute,
                        final OperationContext opContext) throws Exception {

                    @SuppressWarnings("unchecked")
                    final ResultSegment<R> result = (ResultSegment<R>) CloudTableClient.this.executeQuerySegmentedCore(
                            queryToExecute, resolver, this.getToken(), this,
                            (TableRequestOptions) this.getRequestOptions(), opContext);

                    // Note, setting the token on the SegmentedStorageOperation is
                    // key, this is how the iterator will share the token across executions
                    if (result != null) {
                        this.setToken(result.getContinuationToken());
                    }

                    return result;
                }
            };
            return new LazySegmentedIterable<CloudTableClient, TableQuery<T>, R>(impl, this, queryRef,
                    options.getRetryPolicyFactory(), opContext);
        }
    }
}
