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

package com.microsoft.azure.storage.file;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.xml.stream.XMLStreamException;

import com.microsoft.azure.storage.AccessCondition;
import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.DoesServiceRequest;
import com.microsoft.azure.storage.IPRange;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.SharedAccessPolicyHandler;
import com.microsoft.azure.storage.SharedAccessPolicySerializer;
import com.microsoft.azure.storage.SharedAccessProtocols;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsSharedAccessSignature;
import com.microsoft.azure.storage.StorageErrorCodeStrings;
import com.microsoft.azure.storage.StorageException;
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
 * Represents a share in the Microsoft Azure File service.
 * <p>
 * Shares hold directories, which are encapsulated as {@link CloudFileDirectory} objects, and directories hold files.
 * Directories can also contain sub-directories.
 */
public final class CloudFileShare {
    /**
     * Represents the share metadata.
     */
    private HashMap<String, String> metadata = new HashMap<String, String>();

    /**
     * Holds the share properties.
     */
    FileShareProperties properties = new FileShareProperties();

    /**
     * Holds the name of the share.
     */
    String name = null;

    /**
     * Holds the list of URIs for all locations.
     */
    private StorageUri storageUri;

    /**
     * Holds a reference to the associated service client.
     */
    private CloudFileClient fileServiceClient;

    /**
     * Creates an instance of the <code>CloudFileShare</code> class using the specified name and client.
     * 
     * @param shareName
     *            A <code>String</code> which represents the name of the share, which must adhere to share
     *            naming rules.
     *            The share name should not include any path separator characters (/).
     *            Share names must be lowercase, between 3-63 characters long and must start with a letter or
     *            number. Share names may contain only letters, numbers, and the dash (-) character.
     * @param client
     *            A {@link CloudFileClient} object that represents the associated service client, and that specifies the
     *            endpoint for the File service.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI constructed based on the shareName is invalid.
     * 
     * @see <a href="http://msdn.microsoft.com/en-us/library/azure/dn167011.aspx">Naming and Referencing Shares,
     *      Directories, Files, and Metadata</a>
     */
    protected CloudFileShare(final String shareName, final CloudFileClient client) throws URISyntaxException,
            StorageException {
        Utility.assertNotNull("client", client);
        Utility.assertNotNull("shareName", shareName);

        this.storageUri = PathUtility.appendPathToUri(client.getStorageUri(), shareName);
        this.name = shareName;
        this.fileServiceClient = client;
    }
    
    /**
     * Creates an instance of the <code>CloudFileShare</code> class using the specified URI.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that represents the absolute URI of the share.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudFileShare(final URI uri) throws StorageException {
        this(new StorageUri(uri));
    }

    /**
     * Creates an instance of the <code>CloudFileShare</code> class using the specified URI.
     * 
     * @param storageUri
     *            A {@link StorageUri} object which represents the absolute URI of the share.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudFileShare(final StorageUri storageUri) throws StorageException {
        this(storageUri, (StorageCredentials) null);
    }
    
    /**
     * Creates an instance of the <code>CloudFileShare</code> class using the specified URI and credentials.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that represents the absolute URI of the share.
     * @param credentials
     *            A {@link StorageCredentials} object used to authenticate access.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudFileShare(final URI uri, final StorageCredentials credentials) throws StorageException {
        this(new StorageUri(uri), credentials);
    }

    /**
     * Creates an instance of the <code>CloudFileShare</code> class using the specified StorageUri and credentials.
     * 
     * @param storageUri
     *            A {@link StorageUri} object which represents the absolute StorageUri of the share.
     * @param credentials
     *            A {@link StorageCredentials} object used to authenticate access.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudFileShare(final StorageUri storageUri, final StorageCredentials credentials) throws StorageException {
        this.parseQueryAndVerify(storageUri, credentials);
    }

    /**
     * Creates the share.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void create() throws StorageException {
        this.create(null /* options */, null /* opContext */);
    }

    /**
     * Creates the share using the specified options and operation context.
     * 
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void create(FileRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient);

        ExecutionEngine.executeWithRetry(this.fileServiceClient, this, createImpl(options),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudFileClient, CloudFileShare, Void> createImpl(final FileRequestOptions options) {
        final StorageRequest<CloudFileClient, CloudFileShare, Void> putRequest =
                new StorageRequest<CloudFileClient, CloudFileShare, Void>(options, this.getStorageUri()) {
            @Override
            public HttpURLConnection buildRequest(CloudFileClient client, CloudFileShare share, OperationContext context)
                    throws Exception {
                final HttpURLConnection request = FileRequest.createShare(
                        share.getTransformedAddress().getUri(this.getCurrentLocation()),
                        options, context, share.getProperties());
                return request;
            }

            @Override
            public void setHeaders(HttpURLConnection connection, CloudFileShare share, OperationContext context) {
                FileRequest.addMetadata(connection, share.metadata, context);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudFileClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, 0L, context);
            }

            @Override
            public Void preProcessResponse(CloudFileShare share, CloudFileClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_CREATED) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                // Set attributes
                final FileShareAttributes attributes = FileResponse.getFileShareAttributes(this.getConnection(),
                        client.isUsePathStyleUris());
                share.properties = attributes.getProperties();
                share.name = attributes.getName();
                return null;
            }
        };

        return putRequest;
    }

    /**
     * Creates the share if it does not exist.
     * 
     * @return <code>true</code> if the share did not already exist and was created; otherwise, <code>false</code>.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public boolean createIfNotExists() throws StorageException {
        return this.createIfNotExists(null /* options */, null /* opContext */);
    }

    /**
     * Creates the share if it does not exist, using the specified request options and operation context.
     * 
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request.
     *            Specifying <code>null</code> will use the default request options from the associated service client
     *            ({@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return <code>true</code> if the share did not already exist and was created; otherwise, <code>false</code>.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public boolean createIfNotExists(FileRequestOptions options, OperationContext opContext) throws StorageException {
        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient);

        boolean exists = this.exists(true /* primaryOnly */, null /* accessCondition */, options, opContext);
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
                        && StorageErrorCodeStrings.SHARE_ALREADY_EXISTS.equals(e.getErrorCode())) {
                    return false;
                }
                else {
                    throw e;
                }
            }
        }
    }

    /**
     * Deletes the share.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void delete() throws StorageException {
        this.delete(null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Deletes the share using the specified request options and operation context.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the share.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void delete(AccessCondition accessCondition, FileRequestOptions options, OperationContext opContext)
            throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient);

        ExecutionEngine.executeWithRetry(this.fileServiceClient, this, deleteImpl(accessCondition, options),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudFileClient, CloudFileShare, Void> deleteImpl(
            final AccessCondition accessCondition, final FileRequestOptions options) {
        
        final StorageRequest<CloudFileClient, CloudFileShare, Void> putRequest =
                new StorageRequest<CloudFileClient, CloudFileShare, Void>(options, this.getStorageUri()) {
            
            @Override
            public HttpURLConnection buildRequest(
                    CloudFileClient client, CloudFileShare share, OperationContext context) throws Exception {
                return FileRequest.deleteShare(
                        share.getTransformedAddress().getPrimaryUri(), options, context, accessCondition);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudFileClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, -1L, context);
            }

            @Override
            public Void preProcessResponse(CloudFileShare share, CloudFileClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_ACCEPTED) {
                    this.setNonExceptionedRetryableFailure(true);
                }

                return null;
            }
        };

        return putRequest;
    }

    /**
     * Deletes the share if it exists.
     * 
     * @return <code>true</code> if the share existed and was deleted; otherwise, <code>false</code>.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public boolean deleteIfExists() throws StorageException {
        return this.deleteIfExists(null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Deletes the share if it exists using the specified request options and operation context.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the share.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return <code>true</code> if the share existed and was deleted; otherwise, <code>false</code>.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public boolean deleteIfExists(AccessCondition accessCondition, FileRequestOptions options,
            OperationContext opContext) throws StorageException {
        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient);

        boolean exists = this.exists(true /* primaryOnly */, accessCondition, options, opContext);
        if (exists) {
            try {
                this.delete(accessCondition, options, opContext);
                return true;
            }
            catch (StorageException e) {
                if (e.getHttpStatusCode() == HttpURLConnection.HTTP_NOT_FOUND
                        && StorageErrorCodeStrings.SHARE_NOT_FOUND.equals(e.getErrorCode())) {
                    return false;
                }
                else {
                    throw e;
                }
            }
        }
        else {
            return false;
        }
    }

    /**
     * Downloads the share's attributes, which consist of metadata and properties.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void downloadAttributes() throws StorageException {
        this.downloadAttributes(null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Downloads the share's attributes, which consist of metadata and properties, using the specified request
     * options and operation context.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the share.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void downloadAttributes(AccessCondition accessCondition, FileRequestOptions options,
            OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient);

        ExecutionEngine.executeWithRetry(this.fileServiceClient, this,
                this.downloadAttributesImpl(accessCondition, options), options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudFileClient, CloudFileShare, Void> downloadAttributesImpl(
            final AccessCondition accessCondition, final FileRequestOptions options) {
        final StorageRequest<CloudFileClient, CloudFileShare, Void> getRequest = new StorageRequest<CloudFileClient, CloudFileShare, Void>(
                options, this.getStorageUri()) {

            @Override
            public void setRequestLocationMode() {
                this.setRequestLocationMode(RequestLocationMode.PRIMARY_OR_SECONDARY);
            }

            @Override
            public HttpURLConnection buildRequest(CloudFileClient client, CloudFileShare share, OperationContext context)
                    throws Exception {
                return FileRequest.getShareProperties(share.getTransformedAddress().getUri(this.getCurrentLocation()),
                        options, context, accessCondition);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudFileClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, -1L, context);
            }

            @Override
            public Void preProcessResponse(CloudFileShare share, CloudFileClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                // Set attributes
                final FileShareAttributes attributes = FileResponse.getFileShareAttributes(this.getConnection(),
                        client.isUsePathStyleUris());
                share.metadata = attributes.getMetadata();
                share.properties = attributes.getProperties();
                share.name = attributes.getName();
                return null;
            }
        };

        return getRequest;
    }

    /**
     * Downloads the permission settings for the share.
     * 
     * @return A {@link FileSharePermissions} object that represents the share's permissions.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public FileSharePermissions downloadPermissions() throws StorageException {
        return this.downloadPermissions(null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Downloads the permissions settings for the share using the specified request options and operation context.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the share.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return A {@link FileSharePermissions} object that represents the share's permissions.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public FileSharePermissions downloadPermissions(AccessCondition accessCondition, FileRequestOptions options,
            OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient);

        return ExecutionEngine.executeWithRetry(this.fileServiceClient, this,
                downloadPermissionsImpl(accessCondition, options), options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudFileClient, CloudFileShare, FileSharePermissions> downloadPermissionsImpl(
            final AccessCondition accessCondition, final FileRequestOptions options) {
        final StorageRequest<CloudFileClient, CloudFileShare, FileSharePermissions> getRequest =
                new StorageRequest<CloudFileClient, CloudFileShare, FileSharePermissions>(options, this.getStorageUri()) {

            @Override
            public void setRequestLocationMode() {
                this.setRequestLocationMode(RequestLocationMode.PRIMARY_OR_SECONDARY);
            }

            @Override
            public HttpURLConnection buildRequest(CloudFileClient client, CloudFileShare share,
                    OperationContext context) throws Exception {
                return FileRequest.getAcl(share.getTransformedAddress().getUri(this.getCurrentLocation()), options,
                        accessCondition, context);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudFileClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, -1L, context);
            }

            @Override
            public FileSharePermissions preProcessResponse(CloudFileShare share, CloudFileClient client,
                    OperationContext context) throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                }

                share.updatePropertiesFromResponse(this.getConnection());
                return new FileSharePermissions();
            }

            @Override
            public FileSharePermissions postProcessResponse(HttpURLConnection connection,
                    CloudFileShare share, CloudFileClient client, OperationContext context,
                    FileSharePermissions shareAcl) throws Exception {
                HashMap<String, SharedAccessFilePolicy> accessIds = SharedAccessPolicyHandler.getAccessIdentifiers(
                        this.getConnection().getInputStream(), SharedAccessFilePolicy.class);

                for (final String key : accessIds.keySet()) {
                    shareAcl.getSharedAccessPolicies().put(key, accessIds.get(key));
                }

                return shareAcl;
            }
        };

        return getRequest;
    }

    /**
     * Queries the service for this share's {@link ShareStats}.
     * 
     * @return A {@link ShareStats} object for the given storage service.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public ShareStats getStats() throws StorageException {
        return getStats(null /* options */, null /* context */);
    }

    /**
     * Queries the service for this share's {@link ShareStats}.
     * 
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client
     *            ({@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return A {@link ShareStats} object for the given storage service.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public ShareStats getStats(FileRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient);

        return ExecutionEngine.executeWithRetry(this.fileServiceClient, this,
                getStatsImpl(options), options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudFileClient, CloudFileShare, ShareStats> getStatsImpl(final FileRequestOptions options) {
        final StorageRequest<CloudFileClient, CloudFileShare, ShareStats> getRequest =
                new StorageRequest<CloudFileClient, CloudFileShare, ShareStats>(options, this.getStorageUri()) {

            @Override
            public void setRequestLocationMode() {
                this.setRequestLocationMode(RequestLocationMode.PRIMARY_OR_SECONDARY);
            }

            @Override
            public HttpURLConnection buildRequest(CloudFileClient client, CloudFileShare share, OperationContext context)
                    throws Exception {
                return FileRequest.getShareStats(share.getTransformedAddress().getUri(this.getCurrentLocation()),
                        options, context);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudFileClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, -1L, context);
            }

            @Override
            public ShareStats preProcessResponse(CloudFileShare share, CloudFileClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                share.updatePropertiesFromResponse(this.getConnection());
                return null;
            }

            @Override
            public ShareStats postProcessResponse(HttpURLConnection connection, CloudFileShare share,
                    CloudFileClient client, OperationContext context, ShareStats shareStats) throws Exception {
                return ShareStatsHandler.readShareStatsFromStream(connection.getInputStream());
            }
        };

        return getRequest;
    }

    /**
     * Returns a value that indicates whether the share exists.
     * 
     * @return <code>true</code> if the share exists, otherwise <code>false</code>.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public boolean exists() throws StorageException {
        return this.exists(null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Returns a value that indicates whether the share exists, using the specified request options and operation
     * context.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the share.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return <code>true</code> if the share exists, otherwise <code>false</code>.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public boolean exists(final AccessCondition accessCondition, FileRequestOptions options, OperationContext opContext)
            throws StorageException {
        return this.exists(false, accessCondition, options, opContext);
    }

    @DoesServiceRequest
    private boolean exists(final boolean primaryOnly, final AccessCondition accessCondition,
            FileRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient);

        return ExecutionEngine.executeWithRetry(this.fileServiceClient, this,
                this.existsImpl(primaryOnly, accessCondition, options), options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudFileClient, CloudFileShare, Boolean> existsImpl(final boolean primaryOnly,
            final AccessCondition accessCondition, final FileRequestOptions options) {
        final StorageRequest<CloudFileClient, CloudFileShare, Boolean> getRequest = new StorageRequest<CloudFileClient, CloudFileShare, Boolean>(
                options, this.getStorageUri()) {

            @Override
            public void setRequestLocationMode() {
                this.setRequestLocationMode(
                        primaryOnly ? RequestLocationMode.PRIMARY_ONLY : RequestLocationMode.PRIMARY_OR_SECONDARY);
            }

            @Override
            public HttpURLConnection buildRequest(CloudFileClient client, CloudFileShare share, OperationContext context)
                    throws Exception {
                return FileRequest.getShareProperties(share.getTransformedAddress().getUri(this.getCurrentLocation()),
                        options, context, accessCondition);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudFileClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, -1L, context);
            }

            @Override
            public Boolean preProcessResponse(CloudFileShare share, CloudFileClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_OK) {
                    share.updatePropertiesFromResponse(this.getConnection());
                    return Boolean.valueOf(true);
                }
                else if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                    return Boolean.valueOf(false);
                }
                else {
                    this.setNonExceptionedRetryableFailure(true);
                    // return false instead of null to avoid SCA issues
                    return false;
                }
            }
        };

        return getRequest;
    }

    private void updatePropertiesFromResponse(HttpURLConnection request) {
        // ETag
        this.getProperties().setEtag(request.getHeaderField(Constants.HeaderConstants.ETAG));

        // Last Modified
        if (0 != request.getLastModified()) {
            final Calendar lastModifiedCalendar = Calendar.getInstance(Utility.LOCALE_US);
            lastModifiedCalendar.setTimeZone(Utility.UTC_ZONE);
            lastModifiedCalendar.setTime(new Date(request.getLastModified()));
            this.getProperties().setLastModified(lastModifiedCalendar.getTime());
        }
    }

    /**
     * Returns a shared access signature for the share. Note this does not contain the leading "?".
     * 
     * @param policy
     *            An {@link SharedAccessFilePolicy} object that represents the access policy for the
     *            shared access signature.
     * @param groupPolicyIdentifier
     *            A <code>String</code> which represents the share-level access policy.
     * 
     * @return A <code>String</code> which represents a shared access signature for the share.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws InvalidKeyException
     *             If the key is invalid.
     */
    public String generateSharedAccessSignature(final SharedAccessFilePolicy policy, final String groupPolicyIdentifier)
            throws InvalidKeyException, StorageException {

        return this.generateSharedAccessSignature(policy, groupPolicyIdentifier, null /* IP range */, null /* protocols */);
    }

    /**
     * Returns a shared access signature for the share. Note this does not contain the leading "?".
     * 
     * @param policy
     *            An {@link SharedAccessFilePolicy} object that represents the access policy for the
     *            shared access signature.
     * @param groupPolicyIdentifier
     *            A <code>String</code> which represents the share-level access policy.
     * @param ipRange
     *            A {@link IPRange} object containing the range of allowed IP addresses.
     * @param protocols
     *            A {@link SharedAccessProtocols} representing the allowed Internet protocols.
     * 
     * @return A <code>String</code> which represents a shared access signature for the share.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws InvalidKeyException
     *             If the key is invalid.
     */
    public String generateSharedAccessSignature(
            final SharedAccessFilePolicy policy, final String groupPolicyIdentifier, final IPRange ipRange,
            final SharedAccessProtocols protocols)
            throws InvalidKeyException, StorageException {

        if (!StorageCredentialsHelper.canCredentialsSignRequest(this.fileServiceClient.getCredentials())) {
            final String errorMessage = SR.CANNOT_CREATE_SAS_WITHOUT_ACCOUNT_KEY;
            throw new IllegalArgumentException(errorMessage);
        }

        final String resourceName = this.getSharedAccessCanonicalName();

        final String signature = SharedAccessSignatureHelper.generateSharedAccessSignatureHashForBlobAndFile(
                policy, null /* SharedAccessHeaders */, groupPolicyIdentifier, resourceName,
                ipRange, protocols, this.fileServiceClient);

        final UriQueryBuilder builder = SharedAccessSignatureHelper.generateSharedAccessSignatureForBlobAndFile(
                policy, null /* SharedAccessHeaders */, groupPolicyIdentifier, "s", ipRange, protocols, signature);

        return builder.toString();
    }

    /**
     * Returns the canonical name for shared access.
     * 
     * @return the canonical name for shared access.
     */
    private String getSharedAccessCanonicalName() {
        String accountName = this.getServiceClient().getCredentials().getAccountName();
        String shareName = this.getName();

        return String.format("/%s/%s/%s", SR.FILE, accountName, shareName);
    }

    /**
     * Uploads the share's metadata.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void uploadMetadata() throws StorageException {
        this.uploadMetadata(null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Uploads the share's metadata using the specified request options and operation context.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the share.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void uploadMetadata(AccessCondition accessCondition, FileRequestOptions options, OperationContext opContext)
            throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient);

        ExecutionEngine.executeWithRetry(this.fileServiceClient, this,
                this.uploadMetadataImpl(accessCondition, options), options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudFileClient, CloudFileShare, Void> uploadMetadataImpl(
            final AccessCondition accessCondition, final FileRequestOptions options) {
        final StorageRequest<CloudFileClient, CloudFileShare, Void> putRequest = new StorageRequest<CloudFileClient, CloudFileShare, Void>(
                options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudFileClient client, CloudFileShare share, OperationContext context)
                    throws Exception {
                return FileRequest.setShareMetadata(share.getTransformedAddress().getUri(this.getCurrentLocation()),
                        options, context, accessCondition);
            }

            @Override
            public void setHeaders(HttpURLConnection connection, CloudFileShare share, OperationContext context) {
                FileRequest.addMetadata(connection, share.metadata, context);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudFileClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, 0L, context);
            }

            @Override
            public Void preProcessResponse(CloudFileShare share, CloudFileClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                share.updatePropertiesFromResponse(this.getConnection());
                return null;
            }
        };

        return putRequest;
    }

    /**
     * Updates the share's properties on the storage service.
     * <p>
     * Use {@link CloudFileShare#downloadAttributes} to retrieve the latest values for
     * the share's properties and metadata from the Microsoft Azure storage service.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void uploadProperties() throws StorageException {
        this.uploadProperties(null /* accessCondition */, null /* options */, null /*opContext */);
    }

    /**
     * Updates the share's properties using the request options, and operation context.
     * <p>
     * Use {@link CloudFileShare#downloadAttributes} to retrieve the latest values for
     * the share's properties and metadata from the Microsoft Azure storage service.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the share.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void uploadProperties(
            AccessCondition accessCondition, FileRequestOptions options, OperationContext opContext)
            throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient);

        ExecutionEngine.executeWithRetry(this.fileServiceClient, this,
                this.uploadPropertiesImpl(accessCondition, options), options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudFileClient, CloudFileShare, Void> uploadPropertiesImpl(
            final AccessCondition accessCondition, final FileRequestOptions options) {
        final StorageRequest<CloudFileClient, CloudFileShare, Void> putRequest =
                new StorageRequest<CloudFileClient, CloudFileShare, Void>(options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudFileClient client, CloudFileShare share, OperationContext context)
                    throws Exception {
                return FileRequest.setShareProperties(share.getTransformedAddress().getUri(this.getCurrentLocation()),
                        options, context, accessCondition, share.properties);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudFileClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, 0L, context);
            }

            @Override
            public Void preProcessResponse(CloudFileShare share, CloudFileClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                share.updatePropertiesFromResponse(this.getConnection());
                return null;
            }
        };

        return putRequest;
    }
    
    /**
     * Uploads the share's permissions.
     * 
     * @param permissions
     *            A {@link FileSharePermissions} object that represents the permissions to upload.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void uploadPermissions(final FileSharePermissions permissions) throws StorageException {
        this.uploadPermissions(permissions, null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Uploads the share's permissions using the specified request options and operation context.
     * 
     * @param permissions
     *            A {@link FileSharePermissions} object that represents the permissions to upload.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the share.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void uploadPermissions(final FileSharePermissions permissions, final AccessCondition accessCondition,
            FileRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient);

        ExecutionEngine.executeWithRetry(this.fileServiceClient, this,
                uploadPermissionsImpl(permissions, accessCondition, options), options.getRetryPolicyFactory(),
                opContext);
    }

    private StorageRequest<CloudFileClient, CloudFileShare, Void> uploadPermissionsImpl(
            final FileSharePermissions permissions, final AccessCondition accessCondition,
            final FileRequestOptions options) throws StorageException {
        try {
            final StringWriter outBuffer = new StringWriter();
            SharedAccessPolicySerializer.writeSharedAccessIdentifiersToStream(permissions.getSharedAccessPolicies(),
                    outBuffer);
            final byte[] aclBytes = outBuffer.toString().getBytes(Constants.UTF8_CHARSET);
            final StorageRequest<CloudFileClient, CloudFileShare, Void> putRequest =
                    new StorageRequest<CloudFileClient, CloudFileShare, Void>(options, this.getStorageUri()) {
                @Override
                public HttpURLConnection buildRequest(CloudFileClient client, CloudFileShare share,
                        OperationContext context) throws Exception {
                    this.setSendStream(new ByteArrayInputStream(aclBytes));
                    this.setLength((long) aclBytes.length);
                    return FileRequest.setAcl(share.getTransformedAddress().getUri(this.getCurrentLocation()),
                            options, context, accessCondition);
                }

                @Override
                public void signRequest(HttpURLConnection connection, CloudFileClient client, OperationContext context)
                        throws Exception {
                    StorageRequest.signBlobQueueAndFileRequest(connection, client, aclBytes.length, context);
                }

                @Override
                public Void preProcessResponse(CloudFileShare share, CloudFileClient client,
                        OperationContext context) throws Exception {
                    if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                        this.setNonExceptionedRetryableFailure(true);
                        return null;
                    }

                    share.updatePropertiesFromResponse(this.getConnection());
                    return null;
                }
            };

            return putRequest;
        }
        catch (final IllegalArgumentException e) {
            StorageException translatedException = StorageException.translateException(null, e, null);
            throw translatedException;
        }
        catch (final XMLStreamException e) {
            // The request was not even made. There was an error while trying to read the permissions. Just throw.
            StorageException translatedException = StorageException.translateException(null, e, null);
            throw translatedException;
        }
        catch (UnsupportedEncodingException e) {
            // The request was not even made. There was an error while trying to read the permissions. Just throw.
            StorageException translatedException = StorageException.translateException(null, e, null);
            throw translatedException;
        }
    }

    /**
     * Returns a reference to a {@link CloudFileDirectory} object that represents the root file directory within this
     * share.
     * 
     * @return A {@link CloudFileDirectory} reference to the root directory for this share.
     * @throws StorageException
     * @throws URISyntaxException
     */
    public CloudFileDirectory getRootDirectoryReference() throws StorageException, URISyntaxException {
        return new CloudFileDirectory(this.storageUri, "", this);
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
            this.fileServiceClient = new CloudFileClient(PathUtility.getServiceClientBaseAddress(
                    this.getStorageUri(), usePathStyleUris), credentials != null ? credentials : parsedCredentials);
            this.name = PathUtility.getShareNameFromUri(this.storageUri.getPrimaryUri(), usePathStyleUris);
        }
        catch (final URISyntaxException e) {
            throw Utility.generateNewUnexpectedStorageException(e);
        }
    }

    /**
     * Returns the File service client associated with this share.
     * 
     * @return A {@link CloudFileClient} object that represents the service client associated with this share.
     */
    public CloudFileClient getServiceClient() {
        return this.fileServiceClient;
    }

    /**
     * Returns the URI for this share.
     * 
     * @return The absolute URI to the share.
     */
    public URI getUri() {
        return this.storageUri.getPrimaryUri();
    }

    /**
     * Returns the list of URIs for all locations.
     * 
     * @return A {@link StorageUri} object which represents the list of URIs for all locations.
     */
    public StorageUri getStorageUri() {
        return this.storageUri;
    }

    /**
     * Returns the name of the share.
     * 
     * @return A <code>String</code> that represents the name of the share.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the metadata for the share. This value is initialized with the metadata from the share by a call to
     * {@link #downloadAttributes}, and is set on the share with a call to {@link #uploadMetadata}.
     * 
     * @return A <code>java.util.HashMap</code> object that represents the metadata for the share.
     */
    public HashMap<String, String> getMetadata() {
        return this.metadata;
    }

    /**
     * Returns the properties for the share.
     * 
     * @return A {@link FileShareProperties} object that represents the properties for the share.
     */
    public FileShareProperties getProperties() {
        return this.properties;
    }

    /**
     * Sets the metadata collection of name-value pairs to be set on the share with an {@link #uploadMetadata} call.
     * This collection will overwrite any existing share metadata. If this is set to an empty collection, the
     * share metadata will be cleared on an {@link #uploadMetadata} call.
     * 
     * @param metadata
     *            A <code>java.util.HashMap</code> object that represents the metadata being assigned to the share.
     */
    public void setMetadata(final HashMap<String, String> metadata) {
        this.metadata = metadata;
    }

    /**
     * Sets the properties for the share.
     * 
     * @param properties
     *            A {@link FileShareProperties} object that represents the properties being assigned to the
     *            share.
     */
    protected void setProperties(final FileShareProperties properties) {
        this.properties = properties;
    }

    /**
     * Returns the URI after applying the authentication transformation.
     * 
     * @return A <code>java.net.URI</code> object that represents the URI after applying the authentication
     *         transformation.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    private StorageUri getTransformedAddress() throws URISyntaxException, StorageException {
        return this.fileServiceClient.getCredentials().transformUri(this.storageUri);
    }
}
