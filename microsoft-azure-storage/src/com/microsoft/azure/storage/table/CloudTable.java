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
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.xml.stream.XMLStreamException;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.DoesServiceRequest;
import com.microsoft.azure.storage.IPRange;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.ResultContinuation;
import com.microsoft.azure.storage.ResultSegment;
import com.microsoft.azure.storage.SharedAccessPolicyHandler;
import com.microsoft.azure.storage.SharedAccessPolicySerializer;
import com.microsoft.azure.storage.SharedAccessProtocols;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsSharedAccessSignature;
import com.microsoft.azure.storage.StorageErrorCodeStrings;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageExtendedErrorInformation;
import com.microsoft.azure.storage.StorageUri;
import com.microsoft.azure.storage.core.ExecutionEngine;
import com.microsoft.azure.storage.core.PathUtility;
import com.microsoft.azure.storage.core.RequestLocationMode;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.SharedAccessSignatureHelper;
import com.microsoft.azure.storage.core.StorageCredentialsHelper;
import com.microsoft.azure.storage.core.StorageRequest;
import com.microsoft.azure.storage.core.UriQueryBuilder;
import com.microsoft.azure.storage.core.Utility;

/**
 * Represents a table in the Microsoft Azure Table service.
 */
public final class CloudTable {
    /**
     * The name of the table.
     */
    private String name;

    /**
     * Holds the list of URIs for all locations.
     */
    private StorageUri storageUri;

    /**
     * A reference to the associated service client.
     */
    private CloudTableClient tableServiceClient;

    /**
     * Gets the name of the table.
     *
     * @return A <code>String</code> object that represents the name of the table.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the table service client associated with this queue.
     *
     * @return A {@link CloudTableClient} object that represents the service client associated with this table.
     */
    public CloudTableClient getServiceClient() {
        return this.tableServiceClient;
    }

    /**
     * Returns the list of URIs for all locations.
     *
     * @return A {@link StorageUri} that represents the list of URIs for all locations..
     */
    public final StorageUri getStorageUri() {
        return this.storageUri;
    }

    /**
     * Gets the absolute URI for this table.
     *
     * @return A <code>java.net.URI</code> object that represents the URI for this table.
     */
    public URI getUri() {
        return this.storageUri.getPrimaryUri();
    }

    /**
     * Creates an instance of the <code>CloudTable</code> class using the specified table URI. The table URI must
     * include a SAS token.
     *
     * @param uri
     *            A <code>java.net.URI</code> object that represents the absolute URI of the table.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudTable(final URI uri) throws StorageException {
        this(new StorageUri(uri, null));
    }

    /**
     * Creates an instance of the <code>CloudTable</code> class using the specified table URI. The table URI must
     * include a SAS token.
     *
     * @param uri
     *            A {@link StorageUri} object that represents the absolute URI of the table.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudTable(final StorageUri uri) throws StorageException {
        this(uri, (StorageCredentials)null);
    }
    
    /**
     * Creates an instance of the <code>CloudTable</code> class using the specified table URI and credentials.
     *
     * @param uri
     *            A <code>java.net.URI</code> object that represents the absolute URI of the table.
     * @param credentials
     *            A {@link StorageCredentials} object used to authenticate access.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudTable(final URI uri, final StorageCredentials credentials) throws StorageException {
        this(new StorageUri(uri, null), credentials);
    }

    /**
     * Creates an instance of the <code>CloudTable</code> class using the specified table StorageUri and credentials.
     *
     * @param uri
     *            A {@link StorageUri} object that represents the absolute StorageUri of the table.
     * @param credentials
     *            A {@link StorageCredentials} object used to authenticate access.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudTable(final StorageUri uri, final StorageCredentials credentials) throws StorageException {
        this.parseQueryAndVerify(uri, credentials);
    }

    /**
     * Creates an instance of the <code>CloudTable</code> class using the specified name and client.
     *
     * @param tableName
     *            A <code>String</code> which represents the name of the table, which must adhere to table naming rules.
     *            The table name should not include any path separator characters (/).
     *            Table names are case insensitive, must be unique within an account and must be between 3-63 characters
     *            long. Table names must start with an cannot begin with a numeric character and may only contain
     *            alphanumeric characters. Some table names are reserved, including "table".
     * @param client
     *            A {@link CloudTableClient} object that represents the associated service client, and that specifies
     *            the endpoint for the Table service.
     *
     * @throws URISyntaxException
     *             If the resource URI constructed based on the tableName is invalid.
     * @throws StorageException
     *             If a storage service error occurred.
     * @see <a href="http://msdn.microsoft.com/library/azure/dd179338.aspx">Understanding the Table Service Data
     *      Model</a>
     */
    protected CloudTable(final String tableName, final CloudTableClient client) throws URISyntaxException,
    StorageException {
        Utility.assertNotNull("client", client);
        Utility.assertNotNull("tableName", tableName);

        this.storageUri = PathUtility.appendPathToUri(client.getStorageUri(), tableName);
        this.name = tableName;
        this.tableServiceClient = client;
    }

    /**
     * Creates the table in the storage service with default request options.
     * <p>
     * This method invokes the <a href="http://msdn.microsoft.com/en-us/library/azure/dd135729.aspx">Create Table</a>
     * REST API to create the specified table, using the Table service endpoint and storage account credentials of this
     * instance.
     *
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public void create() throws StorageException {
        this.create(null /* options */, null /* opContext */);
    }

    /**
     * Creates the table in the storage service, using the specified {@link TableRequestOptions} and
     * {@link OperationContext}.
     * <p>
     * This method invokes the <a href="http://msdn.microsoft.com/en-us/library/azure/dd135729.aspx">Create Table</a>
     * REST API to create the specified table, using the Table service endpoint and storage account credentials of this
     * instance.
     *
     * Use the {@link TableRequestOptions} to override execution options such as the timeout or retry policy for the
     * operation.
     *
     * @param options
     *            A {@link TableRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudTableClient}.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation. Specify <code>null</code> to
     *            safely ignore operation context.
     *
     * @throws StorageException
     *             If an error occurs accessing the storage service, or because the table cannot be
     *             created, or already exists.
     */
    @DoesServiceRequest
    public void create(TableRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = TableRequestOptions.populateAndApplyDefaults(options, this.tableServiceClient);

        Utility.assertNotNullOrEmpty("tableName", this.name);

        final DynamicTableEntity tableEntry = new DynamicTableEntity();
        tableEntry.getProperties().put(TableConstants.TABLE_NAME, new EntityProperty(this.name));

        TableOperation.insert(tableEntry).execute(this.tableServiceClient, TableConstants.TABLES_SERVICE_TABLES_NAME,
                options, opContext);
    }

    /**
     * Creates the table in the storage service using default request options if it does not already exist.
     *
     * @return <code>true</code> if the table is created in the storage service; otherwise <code>false</code>.
     *
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public boolean createIfNotExists() throws StorageException {
        return this.createIfNotExists(null /* options */, null /* opContext */);
    }

    /**
     * Creates the table in the storage service with the specified request options and operation context, if it does not
     * already exist.
     *
     * @param options
     *            A {@link TableRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudTableClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     *
     * @return <code>true</code> if the table did not already exist and was created; otherwise <code>false</code> .
     *
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public boolean createIfNotExists(TableRequestOptions options, OperationContext opContext) throws StorageException {
        options = TableRequestOptions.populateAndApplyDefaults(options, this.tableServiceClient);

        boolean exists = this.exists(true, options, opContext);
        if (exists) {
            return false;
        }
        else {
            try {
                this.create(options, opContext);
                return true;
            }
            catch (StorageException e) {
                if (e.getHttpStatusCode() == HttpURLConnection.HTTP_CONFLICT
                        && StorageErrorCodeStrings.TABLE_ALREADY_EXISTS.equals(e.getErrorCode())) {
                    return false;
                }
                else {
                    throw e;
                }
            }
        }
    }

    /**
     * Deletes the table from the storage service.
     *
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public void delete() throws StorageException {
        this.delete(null /* options */, null /* opContext */);
    }

    /**
     * Deletes the table from the storage service, using the specified request options and operation context.
     *
     * @param options
     *            A {@link TableRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudTableClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     *
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public void delete(TableRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = TableRequestOptions.populateAndApplyDefaults(options, this.tableServiceClient);

        Utility.assertNotNullOrEmpty("tableName", this.name);
        final DynamicTableEntity tableEntry = new DynamicTableEntity();
        tableEntry.getProperties().put(TableConstants.TABLE_NAME, new EntityProperty(this.name));

        TableOperation deleteOp = new TableOperation(tableEntry, TableOperationType.DELETE);
        deleteOp.execute(this.tableServiceClient, TableConstants.TABLES_SERVICE_TABLES_NAME, options, opContext);
    }

    /**
     * Deletes the table from the storage service, if it exists.
     *
     * @return <code>true</code> if the table existed in the storage service and has been deleted; otherwise
     *         <code>false</code>.
     *
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public boolean deleteIfExists() throws StorageException {
        return this.deleteIfExists(null /* options */, null /* opContext */);
    }

    /**
     * Deletes the table from the storage service using the specified request options and operation context, if it
     * exists.
     *
     * @param options
     *            A {@link TableRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudTableClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     *
     * @return A value of <code>true</code> if the table existed in the storage service and has been deleted, otherwise
     *         <code>false</code>.
     *
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public boolean deleteIfExists(TableRequestOptions options, OperationContext opContext) throws StorageException {
        options = TableRequestOptions.populateAndApplyDefaults(options, this.tableServiceClient);

        if (this.exists(true, options, opContext)) {
            try {
                this.delete(options, opContext);
            }
            catch (StorageException ex) {
                if (ex.getHttpStatusCode() == HttpURLConnection.HTTP_NOT_FOUND
                        && StorageErrorCodeStrings.RESOURCE_NOT_FOUND.equals(ex.getErrorCode())) {
                    return false;
                }
                else {
                    throw ex;
                }
            }
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Executes the specified batch operation on a table as an atomic operation. A batch operation may contain up to 100
     * individual table operations, with the requirement that each operation entity must have same partition key. Only
     * one retrieve operation is allowed per batch. Note that the total payload of a batch operation is limited to 4MB.
     * <p>
     * This method invokes an <a href="http://msdn.microsoft.com/en-us/library/azure/dd894038.aspx">Entity Group
     * Transaction</a> on the REST API to execute the specified batch operation on the table as an atomic unit, using
     * the Table service endpoint and storage account credentials of this instance.
     *
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
    public ArrayList<TableResult> execute(final TableBatchOperation batch) throws StorageException {
        return this.execute(batch, null /* options */, null /* opContext */);
    }

    /**
     * Executes the specified batch operation on a table as an atomic operation, using the specified
     * {@link TableRequestOptions} and {@link OperationContext}. A batch operation may contain up to 100 individual
     * table operations, with the requirement that each operation entity must have same partition key. Only one retrieve
     * operation is allowed per batch. Note that the total payload of a batch operation is limited to 4MB.
     * <p>
     * This method invokes an <a href="http://msdn.microsoft.com/en-us/library/azure/dd894038.aspx">Entity Group
     * Transaction</a> on the REST API to execute the specified batch operation on the table as an atomic unit, using
     * the Table service endpoint and storage account credentials of this instance.
     *
     * Use the {@link TableRequestOptions} to override execution options such as the timeout or retry policy for the
     * operation.
     *
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
    public ArrayList<TableResult> execute(final TableBatchOperation batch, TableRequestOptions options,
            OperationContext opContext) throws StorageException {
        Utility.assertNotNull("batch", batch);
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = TableRequestOptions.populateAndApplyDefaults(options, this.getServiceClient());
        return batch.execute(this.getServiceClient(), this.getName(), options, opContext);
    }

    /**
     * Executes the operation on a table.
     * <p>
     * This method will invoke the <a href="http://msdn.microsoft.com/en-us/library/azure/dd179423.aspx">Table Service
     * REST API</a> to execute the specified operation on the table, using the Table service endpoint and storage
     * account credentials of this instance.
     *
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
    public TableResult execute(final TableOperation operation) throws StorageException {
        return this.execute(operation, null /* options */, null /* opContext */);
    }

    /**
     * Executes the operation on a table, using the specified {@link TableRequestOptions} and {@link OperationContext}.
     * <p>
     * This method will invoke the <a href="http://msdn.microsoft.com/en-us/library/azure/dd179423.aspx">Table Service
     * REST API</a> to execute the specified operation on the table, using the Table service endpoint and storage
     * account credentials of this instance.
     *
     * Use the {@link TableRequestOptions} to override execution options such as the timeout or retry policy for the
     * operation.
     *
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
    public TableResult execute(final TableOperation operation, final TableRequestOptions options,
            final OperationContext opContext) throws StorageException {
        Utility.assertNotNull("operation", operation);
        return operation.execute(this.getServiceClient(), this.getName(), options, opContext);
    }

    /**
     * Executes a query, applying the specified {@link EntityResolver} to the result.
     * <p>
     * This method will invoke a <a href="http://msdn.microsoft.com/en-us/library/azure/dd179421.aspx">Query
     * Entities</a> operation on the <a href="http://msdn.microsoft.com/en-us/library/azure/dd179423.aspx">Table Service
     * REST API</a> to query the table, using the Table service endpoint and storage account credentials of this
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
        return this.execute(query, resolver, null /* options */, null /* opContext */);
    }

    /**
     * Executes a query, applying the specified {@link EntityResolver} to the result, using the
     * specified {@link TableRequestOptions} and {@link OperationContext}.
     * <p>
     * This method will invoke a <a href="http://msdn.microsoft.com/en-us/library/azure/dd179421.aspx">Query
     * Entities</a> operation on the <a href="http://msdn.microsoft.com/en-us/library/azure/dd179423.aspx">Table Service
     * REST API</a> to query the table, using the Table service endpoint and storage account credentials of this
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
    @SuppressWarnings({ "unchecked" })
    public <R> Iterable<R> execute(final TableQuery<?> query, final EntityResolver<R> resolver,
            final TableRequestOptions options, final OperationContext opContext) {
        Utility.assertNotNull("query", query);
        Utility.assertNotNull(SR.QUERY_REQUIRES_VALID_CLASSTYPE_OR_RESOLVER, resolver);
        query.setSourceTableName(this.getName());
        return (Iterable<R>) this.getServiceClient().generateIteratorForQuery(query, resolver, options, opContext);
    }

    /**
     * Executes a query.
     * <p>
     * This method will invoke a <a href="http://msdn.microsoft.com/en-us/library/azure/dd179421.aspx">Query
     * Entities</a> operation on the <a href="http://msdn.microsoft.com/en-us/library/azure/dd179423.aspx">Table Service
     * REST API</a> to query the table, using the Table service endpoint and storage account credentials of this
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
        return this.execute(query, null /* options */, null /* opContext */);
    }

    /**
     * Executes a query, using the specified {@link TableRequestOptions} and {@link OperationContext}.
     * <p>
     * This method will invoke a <a href="http://msdn.microsoft.com/en-us/library/azure/dd179421.aspx">Query
     * Entities</a> operation on the <a href="http://msdn.microsoft.com/en-us/library/azure/dd179423.aspx">Table Service
     * REST API</a> to query the table, using the Table service endpoint and storage account credentials of this
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
    @SuppressWarnings({ "unchecked" })
    @DoesServiceRequest
    public <T extends TableEntity> Iterable<T> execute(final TableQuery<T> query, final TableRequestOptions options,
            final OperationContext opContext) {
        Utility.assertNotNull("query", query);
        Utility.assertNotNull(SR.QUERY_REQUIRES_VALID_CLASSTYPE_OR_RESOLVER, query.getClazzType());
        query.setSourceTableName(this.getName());
        return (Iterable<T>) this.getServiceClient().generateIteratorForQuery(query, null, options, opContext);
    }

    /**
     * Executes a query in segmented mode with the specified {@link ResultContinuation} continuation token,
     * applying the {@link EntityResolver} to the result.
     * Executing a query with <code>executeSegmented</code> allows the query to be resumed after returning partial
     * results, using information returned by the server in the {@link ResultSegment} object.
     * <p>
     * This method will invoke a <a href="http://msdn.microsoft.com/en-us/library/azure/dd179421.aspx">Query
     * Entities</a> operation on the <a href="http://msdn.microsoft.com/en-us/library/azure/dd179423.aspx">Table Service
     * REST API</a> to query the table, using the Table service endpoint and storage account credentials of this
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
     * @throws StorageException
     *             if a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public <R> ResultSegment<R> executeSegmented(final TableQuery<?> query, final EntityResolver<R> resolver,
            final ResultContinuation continuationToken) throws StorageException {
        return this.executeSegmented(query, resolver, continuationToken, null /* options */, null /* opContext */);
    }

    /**
     * Executes a query in segmented mode with the specified {@link ResultContinuation} continuation token,
     * using the specified {@link TableRequestOptions} and {@link OperationContext}, applying the {@link EntityResolver}
     * to the result.
     * Executing a query with <code>executeSegmented</code> allows the query to be resumed after returning partial
     * results, using information returned by the server in the {@link ResultSegment} object.
     * <p>
     * This method will invoke a <a href="http://msdn.microsoft.com/en-us/library/azure/dd179421.aspx">Query
     * Entities</a> operation on the <a href="http://msdn.microsoft.com/en-us/library/azure/dd179423.aspx">Table Service
     * REST API</a> to query the table, using the Table service endpoint and storage account credentials of this
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
     * @throws StorageException
     *             if a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    @SuppressWarnings({ "unchecked" })
    public <R> ResultSegment<R> executeSegmented(final TableQuery<?> query, final EntityResolver<R> resolver,
            final ResultContinuation continuationToken, final TableRequestOptions options,
            final OperationContext opContext) throws StorageException {
        Utility.assertNotNull(SR.QUERY_REQUIRES_VALID_CLASSTYPE_OR_RESOLVER, resolver);
        query.setSourceTableName(this.getName());
        return (ResultSegment<R>) this.getServiceClient().executeQuerySegmentedImpl(query, resolver, continuationToken,
                options, opContext);
    }

    /**
     * Executes a query in segmented mode with a {@link ResultContinuation} continuation token.
     * Executing a query with <code>executeSegmented</code> allows the query to be resumed after returning partial
     * results, using information returned by the server in the {@link ResultSegment} object.
     * <p>
     * This method will invoke a <a href="http://msdn.microsoft.com/en-us/library/azure/dd179421.aspx">Query
     * Entities</a> operation on the <a href="http://msdn.microsoft.com/en-us/library/azure/dd179423.aspx">Table Service
     * REST API</a> to query the table, using the Table service endpoint and storage account credentials of this
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
     * @throws StorageException
     *             if a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public <T extends TableEntity> ResultSegment<T> executeSegmented(final TableQuery<T> query,
            final ResultContinuation continuationToken) throws StorageException {
        return this.executeSegmented(query, continuationToken, null /* options */, null /* opContext */);
    }

    /**
     * Executes a query in segmented mode with a {@link ResultContinuation} continuation token,
     * using the specified {@link TableRequestOptions} and {@link OperationContext}.
     * Executing a query with <code>executeSegmented</code> allows the query to be resumed after returning partial
     * results, using information returned by the server in the {@link ResultSegment} object.
     * <p>
     * This method will invoke a <a href="http://msdn.microsoft.com/en-us/library/azure/dd179421.aspx">Query
     * Entities</a> operation on the <a href="http://msdn.microsoft.com/en-us/library/azure/dd179423.aspx">Table Service
     * REST API</a> to query the table, using the Table service endpoint and storage account credentials of this
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
     * @throws StorageException
     *             if a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    @SuppressWarnings({ "unchecked" })
    public <T extends TableEntity> ResultSegment<T> executeSegmented(final TableQuery<T> query,
            final ResultContinuation continuationToken, final TableRequestOptions options,
            final OperationContext opContext) throws StorageException {
        Utility.assertNotNull("query", query);
        query.setSourceTableName(this.getName());
        return (ResultSegment<T>) this.getServiceClient().executeQuerySegmentedImpl(query, null, continuationToken,
                options, opContext);
    }

    /**
     * Returns a value that indicates whether the table exists in the storage service.
     *
     * @return <code>true</code> if the table exists in the storage service; otherwise <code>false</code>.
     *
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public boolean exists() throws StorageException {
        return this.exists(null /* options */, null /* opContext */);
    }

    /**
     * Returns a value that indicates whether the table exists in the storage service, using the specified request
     * options and operation context.
     *
     * @param options
     *            A {@link TableRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudTableClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     *
     * @return <code>true</code> if the table exists in the storage service, otherwise <code>false</code>.
     *
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public boolean exists(TableRequestOptions options, OperationContext opContext) throws StorageException {
        return this.exists(false, options, opContext);
    }

    /**
     * Returns a value that indicates whether the table exists in the storage service, using the specified request
     * options and operation context.
     *
     * @param options
     *            A {@link TableRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudTableClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     *
     * @return <code>true</code> if the table exists in the storage service, otherwise <code>false</code>.
     *
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    private boolean exists(final boolean primaryOnly, TableRequestOptions options, OperationContext opContext)
            throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = TableRequestOptions.populateAndApplyDefaults(options, this.tableServiceClient);

        Utility.assertNotNullOrEmpty("tableName", this.name);

        QueryTableOperation operation = (QueryTableOperation) TableOperation.retrieve(this.name /* Used As PK */,
                null/* Row Key */, DynamicTableEntity.class);
        operation.setPrimaryOnlyRetrieve(primaryOnly);

        final TableResult result = operation.execute(this.tableServiceClient,
                TableConstants.TABLES_SERVICE_TABLES_NAME, options, opContext);

        if (result.getHttpStatusCode() == HttpURLConnection.HTTP_OK) {
            return true;
        }
        else if (result.getHttpStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
            return false;
        }
        else {
            throw new StorageException(StorageErrorCodeStrings.OUT_OF_RANGE_INPUT, SR.UNEXPECTED_STATUS_CODE_RECEIVED,
                    result.getHttpStatusCode(), null /* extendedErrorInfo */, null /* innerException */);
        }
    }

    /**
     * Uploads the table's permissions.
     *
     * @param permissions
     *            A {@link TablePermissions} object that represents the permissions to upload.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void uploadPermissions(final TablePermissions permissions) throws StorageException {
        this.uploadPermissions(permissions, null /* options */, null /* opContext */);
    }

    /**
     * Uploads the table's permissions using the specified request options and operation context.
     *
     * @param permissions
     *            A {@link TablePermissions} object that represents the permissions to upload.
     * @param options
     *            A {@link TableRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudTableClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void uploadPermissions(final TablePermissions permissions, TableRequestOptions options,
            OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = TableRequestOptions.populateAndApplyDefaults(options, this.tableServiceClient);

        ExecutionEngine.executeWithRetry(this.tableServiceClient, this,
                this.uploadPermissionsImpl(permissions, options), options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudTableClient, CloudTable, Void> uploadPermissionsImpl(
            final TablePermissions permissions, final TableRequestOptions options) throws StorageException {
        final StringWriter outBuffer = new StringWriter();

        try {
            SharedAccessPolicySerializer.writeSharedAccessIdentifiersToStream(permissions.getSharedAccessPolicies(),
                    outBuffer);
            final byte[] aclBytes = outBuffer.toString().getBytes(Constants.UTF8_CHARSET);

            final StorageRequest<CloudTableClient, CloudTable, Void> putRequest = new StorageRequest<CloudTableClient, CloudTable, Void>(
                    options, this.getStorageUri()) {

                @Override
                public HttpURLConnection buildRequest(CloudTableClient client, CloudTable table,
                        OperationContext context) throws Exception {
                    this.setSendStream(new ByteArrayInputStream(aclBytes));
                    this.setLength((long) aclBytes.length);
                    return TableRequest.setAcl(table.getStorageUri().getUri(this.getCurrentLocation()), options,
                            context);
                }

                @Override
                public void signRequest(HttpURLConnection connection, CloudTableClient client, OperationContext context)
                        throws Exception {
                    StorageRequest.signTableRequest(connection, client, aclBytes.length, context);
                }

                @Override
                public Void preProcessResponse(CloudTable parentObject, CloudTableClient client,
                        OperationContext context) throws Exception {
                    if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                        this.setNonExceptionedRetryableFailure(true);
                    }

                    return null;
                }
                
                @Override
                public StorageExtendedErrorInformation parseErrorDetails() {
                    return TableStorageErrorDeserializer.parseErrorDetails(this);
                }
            };

            return putRequest;
        }
        catch (IllegalArgumentException e) {
            // to do : Move this to multiple catch clause so we can avoid the duplicated code once we move to Java 1.7.
            // The request was not even made. There was an error while trying to read the permissions. Just throw.
            StorageException translatedException = StorageException.translateClientException(e);
            throw translatedException;
        }
        catch (XMLStreamException e) {
            // The request was not even made. There was an error while trying to read the permissions. Just throw.
            StorageException translatedException = StorageException.translateClientException(e);
            throw translatedException;
        }
        catch (UnsupportedEncodingException e) {
            // The request was not even made. There was an error while trying to read the permissions. Just throw.
            StorageException translatedException = StorageException.translateClientException(e);
            throw translatedException;
        }
    }

    /**
     * Downloads the permission settings for the table.
     *
     * @return A {@link TablePermissions} object that represents the container's permissions.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public TablePermissions downloadPermissions() throws StorageException {
        return this.downloadPermissions(null /* options */, null /* opContext */);
    }

    /**
     * Downloads the permissions settings for the table using the specified request options and operation context.
     *
     * @param options
     *            A {@link TableRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudTableClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     *
     * @return A {@link TablePermissions} object that represents the table's permissions.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public TablePermissions downloadPermissions(TableRequestOptions options, OperationContext opContext)
            throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = TableRequestOptions.populateAndApplyDefaults(options, this.tableServiceClient);

        return ExecutionEngine.executeWithRetry(this.tableServiceClient, this, this.downloadPermissionsImpl(options),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudTableClient, CloudTable, TablePermissions> downloadPermissionsImpl(
            final TableRequestOptions options) {
        final StorageRequest<CloudTableClient, CloudTable, TablePermissions> getRequest = new StorageRequest<CloudTableClient, CloudTable, TablePermissions>(
                options, this.getStorageUri()) {

            @Override
            public void setRequestLocationMode() {
                this.setRequestLocationMode(RequestLocationMode.PRIMARY_OR_SECONDARY);
            }

            @Override
            public HttpURLConnection buildRequest(CloudTableClient client, CloudTable table, OperationContext context)
                    throws Exception {
                return TableRequest.getAcl(table.getStorageUri().getUri(this.getCurrentLocation()), options, context);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudTableClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signTableRequest(connection, client, -1L, context);
            }

            @Override
            public TablePermissions preProcessResponse(CloudTable parentObject, CloudTableClient client,
                    OperationContext context) throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                }

                return new TablePermissions();
            }

            @Override
            public TablePermissions postProcessResponse(HttpURLConnection connection, CloudTable table,
                    CloudTableClient client, OperationContext context, TablePermissions permissions) throws Exception {
                HashMap<String, SharedAccessTablePolicy> accessIds = SharedAccessPolicyHandler.getAccessIdentifiers(
                        this.getConnection().getInputStream(), SharedAccessTablePolicy.class);
                for (final String key : accessIds.keySet()) {
                    permissions.getSharedAccessPolicies().put(key, accessIds.get(key));
                }

                return permissions;
            }
            
            @Override
            public StorageExtendedErrorInformation parseErrorDetails() {
                return TableStorageErrorDeserializer.parseErrorDetails(this);
            }
        };

        return getRequest;
    }

    /**
     * Creates a shared access signature for the table.
     *
     * @param policy
     *            A {@link SharedAccessTablePolicy} object which represents the access policy for the shared access
     *            signature.
     * @param accessPolicyIdentifier
     *            A <code>String</code> which represents a table-level access policy.
     * @param startPartitionKey
     *            A <code>String</code> which represents the starting partition key.
     * @param startRowKey
     *            A <code>String</code> which represents the starting row key.
     * @param endPartitionKey
     *            A <code>String</code> which represents the ending partition key.
     * @param endRowKey
     *            A <code>String</code> which represents the ending end key.
     *
     * @return A <code>String</code> containing the shared access signature for the table.
     *
     * @throws InvalidKeyException
     *             If an invalid key was passed.
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws IllegalArgumentException
     *             If an unexpected value is passed.
     */
    public String generateSharedAccessSignature(final SharedAccessTablePolicy policy,
            final String accessPolicyIdentifier, final String startPartitionKey, final String startRowKey,
            final String endPartitionKey, final String endRowKey) throws InvalidKeyException, StorageException {
        
        return generateSharedAccessSignature(policy, accessPolicyIdentifier,
                startPartitionKey, startRowKey, endPartitionKey, endRowKey, null /* IP Range */, null /* Protocols */);
    }

    /**
     * Creates a shared access signature for the table.
     *
     * @param policy
     *            A {@link SharedAccessTablePolicy} object which represents the access policy for the shared access
     *            signature.
     * @param accessPolicyIdentifier
     *            A <code>String</code> which represents a table-level access policy.
     * @param startPartitionKey
     *            A <code>String</code> which represents the starting partition key.
     * @param startRowKey
     *            A <code>String</code> which represents the starting row key.
     * @param endPartitionKey
     *            A <code>String</code> which represents the ending partition key.
     * @param endRowKey
     *            A <code>String</code> which represents the ending end key.
     * @param ipRange
     *            A {@link IPRange} object containing the range of allowed IP addresses.
     * @param protocols
     *            A {@link SharedAccessProtocols} representing the allowed Internet protocols.
     *
     * @return A <code>String</code> containing the shared access signature for the table.
     *
     * @throws InvalidKeyException
     *             If an invalid key was passed.
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws IllegalArgumentException
     *             If an unexpected value is passed.
     */
    public String generateSharedAccessSignature(
            final SharedAccessTablePolicy policy, final String accessPolicyIdentifier, final String startPartitionKey,
            final String startRowKey, final String endPartitionKey, final String endRowKey, final IPRange ipRange,
            final SharedAccessProtocols protocols)
            throws InvalidKeyException, StorageException {

        if (!StorageCredentialsHelper.canCredentialsSignRequest(this.tableServiceClient.getCredentials())) {
            throw new IllegalArgumentException(SR.CANNOT_CREATE_SAS_WITHOUT_ACCOUNT_KEY);
        }

        final String resourceName = this.getSharedAccessCanonicalName();

        final String signature = SharedAccessSignatureHelper.generateSharedAccessSignatureHashForTable(
                policy, accessPolicyIdentifier, resourceName, ipRange, protocols,
                startPartitionKey, startRowKey, endPartitionKey, endRowKey, this.tableServiceClient);

        final UriQueryBuilder builder = SharedAccessSignatureHelper.generateSharedAccessSignatureForTable(
                policy, startPartitionKey, startRowKey, endPartitionKey, endRowKey, accessPolicyIdentifier,
                ipRange, protocols, this.name,  signature);

        return builder.toString();
    }

    /**
     * Returns the canonical name for shared access.
     *
     * @return A <code>String</code> containing the canonical name for shared access.
     */
    private String getSharedAccessCanonicalName() {
        String accountName = this.getServiceClient().getCredentials().getAccountName();
        String tableNameLowerCase = this.getName().toLowerCase(Locale.ENGLISH);

        return String.format("/%s/%s/%s", SR.TABLE, accountName, tableNameLowerCase);
    }

    /**
     * Verifies the passed in URI. Then parses it and uses its components to populate this resource's properties.
     * 
     * @param completeUri
     *            A {@link StorageUri} object which represents the complete URI.
     * @param credentials
     *            A {@link StorageCredentials} object used to authenticate access.
     * @throws StorageException
     *             If a storage service error occurred.
     */  
    private void parseQueryAndVerify(final StorageUri completeUri, final StorageCredentials credentials) 
            throws StorageException {
        Utility.assertNotNull("completeUri", completeUri);

        if (!completeUri.isAbsolute()) {
            throw new IllegalArgumentException(String.format(SR.RELATIVE_ADDRESS_NOT_PERMITTED, completeUri.toString()));
        }

        this.storageUri = PathUtility.stripURIQueryAndFragment(completeUri);
        
        final StorageCredentialsSharedAccessSignature parsedCredentials = 
                SharedAccessSignatureHelper.parseQuery(completeUri);

        if (credentials != null && parsedCredentials != null) {
            throw new IllegalArgumentException(SR.MULTIPLE_CREDENTIALS_PROVIDED);
        }

        try {
            final boolean usePathStyleUris = Utility.determinePathStyleFromUri(this.storageUri.getPrimaryUri());
            this.tableServiceClient = new CloudTableClient(PathUtility.getServiceClientBaseAddress(
                    this.getStorageUri(), usePathStyleUris), credentials != null ? credentials : parsedCredentials);
            this.name = PathUtility.getTableNameFromUri(storageUri.getPrimaryUri(), usePathStyleUris);
        }
        catch (final URISyntaxException e) {
            throw Utility.generateNewUnexpectedStorageException(e);
        }
    }
}
