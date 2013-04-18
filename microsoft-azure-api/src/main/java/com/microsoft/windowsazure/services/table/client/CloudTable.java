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

import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import com.microsoft.windowsazure.services.blob.core.storage.SharedAccessSignatureHelper;
import com.microsoft.windowsazure.services.core.storage.DoesServiceRequest;
import com.microsoft.windowsazure.services.core.storage.OperationContext;
import com.microsoft.windowsazure.services.core.storage.StorageCredentials;
import com.microsoft.windowsazure.services.core.storage.StorageCredentialsAccountAndKey;
import com.microsoft.windowsazure.services.core.storage.StorageErrorCodeStrings;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.core.storage.utils.PathUtility;
import com.microsoft.windowsazure.services.core.storage.utils.UriQueryBuilder;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.ExecutionEngine;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.StorageOperation;

/**
 * Represents a table in the Windows Azure Table service.
 */
public final class CloudTable {

    /**
     * The name of the table.
     */
    String name;

    /**
     * The URI of the table.
     */
    URI uri;

    /**
     * A reference to the associated service client.
     */
    private final CloudTableClient tableServiceClient;

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
     * Gets the absolute URI for this table.
     * 
     * @return A <code>java.net.URI</code> object that represents the URI for this table.
     */
    public URI getUri() {
        return this.uri;
    }

    /**
     * Creates an instance of the <code>CloudTable</code> class using the specified address and client.
     * 
     * @param tableName
     *            A <code>String</code> that represents the table name.
     * @param client
     *            A {@link CloudTableClient} object that represents the associated service client, and that specifies
     *            the endpoint for the Table service.
     * 
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public CloudTable(final String tableName, final CloudTableClient client) throws URISyntaxException {
        this(PathUtility.appendPathToUri(client.getEndpoint(), tableName), client);
    }

    /**
     * Creates an instance of the <code>CloudTable</code> class using the specified table URI and client.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that represents the absolute URI of the table.
     * @param client
     *            A {@link CloudTableClient} object that represents the associated service client, and that specifies
     *            the endpoint for the Table service.
     */
    public CloudTable(final URI uri, final CloudTableClient client) {
        this.uri = uri;
        this.name = PathUtility.getTableNameFromUri(uri, client.isUsePathStyleUris());
        this.tableServiceClient = client;
    }

    /**
     * Creates the table in the storage service with default request options.
     * <p>
     * This method invokes the <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd135729.aspx">Create
     * Table</a> REST API to create the specified table, using the Table service endpoint and storage account
     * credentials of this instance.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public void create() throws StorageException {
        this.create(null, null);
    }

    /**
     * Creates the table in the storage service, using the specified {@link TableRequestOptions} and
     * {@link OperationContext}.
     * <p>
     * This method invokes the <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd135729.aspx">Create
     * Table</a> REST API to create the specified table, using the Table service endpoint and storage account
     * credentials of this instance.
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

        if (options == null) {
            options = new TableRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.tableServiceClient);

        Utility.assertNotNullOrEmpty("tableName", this.name);

        final DynamicTableEntity tableEntry = new DynamicTableEntity();
        tableEntry.getProperties().put(TableConstants.TABLE_NAME, new EntityProperty(this.name));

        this.tableServiceClient.execute(TableConstants.TABLES_SERVICE_TABLES_NAME, TableOperation.insert(tableEntry),
                options, opContext);
    }

    /**
     * Creates the table in the storage service using default request options if it does not already exist.
     * 
     * @return A value of <code>true</code> if the table is created in the storage service, otherwise <code>false</code>
     *         .
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public boolean createIfNotExist() throws StorageException {
        return this.createIfNotExist(null, null);
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
     * @return A value of <code>true</code> if the table is created in the storage service, otherwise <code>false</code>
     *         .
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public boolean createIfNotExist(TableRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new TableRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.tableServiceClient);

        Utility.assertNotNullOrEmpty("tableName", this.name);

        if (this.exists(options, opContext)) {
            return false;
        }
        else {
            try {
                this.create(options, opContext);
            }
            catch (StorageException ex) {
                if (ex.getHttpStatusCode() == HttpURLConnection.HTTP_CONFLICT
                        && StorageErrorCodeStrings.TABLE_ALREADY_EXISTS.equals(ex.getErrorCode())) {
                    return false;
                }
                else {
                    throw ex;
                }
            }
            return true;
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
        this.delete(null, null);
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

        if (options == null) {
            options = new TableRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.tableServiceClient);

        Utility.assertNotNullOrEmpty("tableName", this.name);
        final DynamicTableEntity tableEntry = new DynamicTableEntity();
        tableEntry.getProperties().put(TableConstants.TABLE_NAME, new EntityProperty(this.name));

        final TableOperation delOp = new TableOperation(tableEntry, TableOperationType.DELETE);

        final TableResult result = this.tableServiceClient.execute(TableConstants.TABLES_SERVICE_TABLES_NAME, delOp,
                options, opContext);

        if (result.getHttpStatusCode() == HttpURLConnection.HTTP_NO_CONTENT) {
            return;
        }
        else {
            throw new StorageException(StorageErrorCodeStrings.OUT_OF_RANGE_INPUT,
                    "Unexpected http status code received.", result.getHttpStatusCode(), null, null);
        }

    }

    /**
     * Deletes the table from the storage service, if it exists.
     * 
     * @return A value of <code>true</code> if the table existed in the storage service and has been deleted, otherwise
     *         <code>false</code>.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public boolean deleteIfExists() throws StorageException {
        return this.deleteIfExists(null, null);
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
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new TableRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.tableServiceClient);

        Utility.assertNotNullOrEmpty("tableName", this.name);

        if (this.exists(options, opContext)) {
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
     * Returns a value that indicates whether the table exists in the storage service.
     * 
     * @return <code>true</code> if the table exists in the storage service, otherwise <code>false</code>.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public boolean exists() throws StorageException {
        return this.exists(null, null);
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
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new TableRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.tableServiceClient);

        Utility.assertNotNullOrEmpty("tableName", this.name);

        final TableResult result = this.tableServiceClient.execute(TableConstants.TABLES_SERVICE_TABLES_NAME,
                TableOperation.retrieve(this.name /* Used As PK */, null/* Row Key */, DynamicTableEntity.class),
                options, opContext);

        if (result.getHttpStatusCode() == HttpURLConnection.HTTP_OK) {
            return true;
        }
        else if (result.getHttpStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
            return false;
        }
        else {
            throw new StorageException(StorageErrorCodeStrings.OUT_OF_RANGE_INPUT,
                    "Unexpected http status code received.", result.getHttpStatusCode(), null, null);
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
        this.uploadPermissions(permissions, null, null);
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

        if (options == null) {
            options = new TableRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.tableServiceClient);

        final StorageOperation<CloudTableClient, CloudTable, Void> impl = new StorageOperation<CloudTableClient, CloudTable, Void>(
                options) {

            @Override
            public Void execute(final CloudTableClient client, final CloudTable table, final OperationContext opContext)
                    throws Exception {

                final HttpURLConnection request = TableRequest.setAcl(table.uri, this.getRequestOptions()
                        .getTimeoutIntervalInMs(), opContext);

                final StringWriter outBuffer = new StringWriter();

                TableRequest.writeSharedAccessIdentifiersToStream(permissions.getSharedAccessPolicies(), outBuffer);

                final byte[] aclBytes = outBuffer.toString().getBytes("UTF8");
                client.getCredentials().signRequestLite(request, aclBytes.length, opContext);
                final OutputStream outStreamRef = request.getOutputStream();
                outStreamRef.write(aclBytes);

                ExecutionEngine.processRequest(request, opContext, this.getResult());

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                    this.setNonExceptionedRetryableFailure(true);
                }

                return null;
            }
        };

        ExecutionEngine.executeWithRetry(this.tableServiceClient, this, impl, options.getRetryPolicyFactory(),
                opContext);
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
        return this.downloadPermissions(null, null);
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

        if (options == null) {
            options = new TableRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.tableServiceClient);
        final String tableName = this.name;

        final StorageOperation<CloudTableClient, CloudTable, TablePermissions> impl = new StorageOperation<CloudTableClient, CloudTable, TablePermissions>(
                options) {

            @Override
            public TablePermissions execute(final CloudTableClient client, final CloudTable table,
                    final OperationContext opContext) throws Exception {

                final HttpURLConnection request = TableRequest.getAcl(table.uri, tableName, this.getRequestOptions()
                        .getTimeoutIntervalInMs(), opContext);

                client.getCredentials().signRequestLite(request, -1L, opContext);

                ExecutionEngine.processRequest(request, opContext, this.getResult());

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                }

                final TablePermissions permissions = new TablePermissions();
                final TableAccessPolicyResponse response = new TableAccessPolicyResponse(request.getInputStream());
                for (final String key : response.getAccessIdentifiers().keySet()) {
                    permissions.getSharedAccessPolicies().put(key, response.getAccessIdentifiers().get(key));
                }

                return permissions;
            }
        };

        return ExecutionEngine.executeWithRetry(this.tableServiceClient, this, impl, options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Returns a shared access signature for the table.
     * 
     * @param policy
     *            The access policy for the shared access signature.
     * @param accessPolicyIdentifier
     *            A table-level access policy.
     * @return A <code>String</code> containing the shared access signature for the table.
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

        if (!this.tableServiceClient.getCredentials().canCredentialsSignRequest()) {
            final String errorMessage = "Cannot create Shared Access Signature unless the Account Key credentials are used by the BlobServiceClient.";
            throw new IllegalArgumentException(errorMessage);
        }

        final String resourceName = this.getSharedAccessCanonicalName();

        final String signature = SharedAccessSignatureHelper.generateSharedAccessSignatureHash(policy,
                accessPolicyIdentifier, resourceName, startPartitionKey, startRowKey, endPartitionKey, endRowKey,
                this.tableServiceClient, null);

        String accountKeyName = null;
        StorageCredentials credentials = this.tableServiceClient.getCredentials();

        if (credentials instanceof StorageCredentialsAccountAndKey) {
            accountKeyName = ((StorageCredentialsAccountAndKey) credentials).getAccountKeyName();
        }

        final UriQueryBuilder builder = SharedAccessSignatureHelper.generateSharedAccessSignature(policy,
                startPartitionKey, startRowKey, endPartitionKey, endRowKey, accessPolicyIdentifier, this.name,
                signature, accountKeyName);

        return builder.toString();
    }

    /**
     * Returns the canonical name for shared access.
     * 
     * @return A <code>String</code> containing the canonical name for shared access.
     */
    private String getSharedAccessCanonicalName() {
        if (this.tableServiceClient.isUsePathStyleUris()) {
            return this.getUri().getPath();
        }
        else {
            return PathUtility.getCanonicalPathFromCredentials(this.tableServiceClient.getCredentials(), this.getUri()
                    .getPath());
        }
    }
}
