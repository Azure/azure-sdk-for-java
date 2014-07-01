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

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.microsoft.azure.storage.AccessCondition;
import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.DoesServiceRequest;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageErrorCodeStrings;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageUri;
import com.microsoft.azure.storage.core.ExecutionEngine;
import com.microsoft.azure.storage.core.PathUtility;
import com.microsoft.azure.storage.core.RequestLocationMode;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.StorageRequest;
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
    protected HashMap<String, String> metadata;

    /**
     * Holds the share properties.
     */
    FileShareProperties properties;

    /**
     * Holds the name of the share.
     */
    String name;

    /**
     * Holds the list of URIs for all locations.
     */
    private StorageUri storageUri;

    /**
     * Holds a reference to the associated service client.
     */
    private CloudFileClient fileServiceClient;

    /**
     * Initializes a new instance of the CloudFileShare class.
     * 
     * @param client
     *            A {@link CloudFileClient} which represents a reference to the associated service client.
     */
    private CloudFileShare(final CloudFileClient client) {
        this.metadata = new HashMap<String, String>();
        this.properties = new FileShareProperties();
        this.fileServiceClient = client;
    }

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
    public CloudFileShare(final String shareName, final CloudFileClient client) throws URISyntaxException,
            StorageException {
        this(client);
        Utility.assertNotNull("client", client);
        Utility.assertNotNull("shareName", shareName);

        this.storageUri = PathUtility.appendPathToUri(client.getStorageUri(), shareName);

        this.name = shareName;
        this.parseQueryAndVerify(this.storageUri, client, client.isUsePathStyleUris());
    }

    /**
     * Creates an instance of the <code>CloudFileShare</code> class using the specified URI and client.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that represents the absolute URI of the share.
     * @param client
     *            A {@link CloudFileClient} object that represents the associated service client, and that specifies the
     *            endpoint for the File service.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     */
    public CloudFileShare(final URI uri, final CloudFileClient client) throws StorageException, URISyntaxException {
        this(new StorageUri(uri), client);
    }

    /**
     * Creates an instance of the <code>CloudFileShare</code> class using the specified URI and client.
     * 
     * @param storageUri
     *            A {@link StorageUri} object which represents the absolute URI of the share.
     * @param client
     *            A {@link CloudFileClient} object that represents the associated service client, and that specifies the
     *            endpoint for the File service.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     */
    public CloudFileShare(final StorageUri storageUri, final CloudFileClient client) throws StorageException,
            URISyntaxException {
        this(client);

        Utility.assertNotNull("storageUri", storageUri);

        this.storageUri = storageUri;

        this.parseQueryAndVerify(
                this.storageUri,
                client,
                client == null ? Utility.determinePathStyleFromUri(this.storageUri.getPrimaryUri()) : client
                        .isUsePathStyleUris());
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
        options = FileRequestOptions.applyDefaults(options, this.fileServiceClient);

        ExecutionEngine.executeWithRetry(this.fileServiceClient, this, createImpl(options),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudFileClient, CloudFileShare, Void> createImpl(final FileRequestOptions options) {
        final StorageRequest<CloudFileClient, CloudFileShare, Void> putRequest = new StorageRequest<CloudFileClient, CloudFileShare, Void>(
                options, this.getStorageUri()) {
            @Override
            public HttpURLConnection buildRequest(CloudFileClient client, CloudFileShare share, OperationContext context)
                    throws Exception {
                final HttpURLConnection request = FileRequest.createShare(
                        share.getTransformedAddress().getUri(this.getCurrentLocation()), options, context);
                return request;
            }

            @Override
            public void setHeaders(HttpURLConnection connection, CloudFileShare share, OperationContext context) {
                FileRequest.addMetadata(connection, share.metadata, context);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudFileClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, 0L, null);
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
        options = FileRequestOptions.applyDefaults(options, this.fileServiceClient);

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
        options = FileRequestOptions.applyDefaults(options, this.fileServiceClient);

        ExecutionEngine.executeWithRetry(this.fileServiceClient, this, deleteImpl(accessCondition, options),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudFileClient, CloudFileShare, Void> deleteImpl(final AccessCondition accessCondition,
            final FileRequestOptions options) {
        final StorageRequest<CloudFileClient, CloudFileShare, Void> putRequest = new StorageRequest<CloudFileClient, CloudFileShare, Void>(
                options, this.getStorageUri()) {
            @Override
            public HttpURLConnection buildRequest(CloudFileClient client, CloudFileShare share, OperationContext context)
                    throws Exception {
                return FileRequest
                        .deleteShare(share.getStorageUri().getPrimaryUri(), options, context, accessCondition);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudFileClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, -1L, null);
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
        options = FileRequestOptions.applyDefaults(options, this.fileServiceClient);

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
        options = FileRequestOptions.applyDefaults(options, this.fileServiceClient);

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
                StorageRequest.signBlobQueueAndFileRequest(connection, client, -1L, null);
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
        options = FileRequestOptions.applyDefaults(options, this.fileServiceClient);

        return ExecutionEngine.executeWithRetry(this.fileServiceClient, this,
                this.existsImpl(primaryOnly, accessCondition, options), options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudFileClient, CloudFileShare, Boolean> existsImpl(final boolean primaryOnly,
            final AccessCondition accessCondition, final FileRequestOptions options) {
        final StorageRequest<CloudFileClient, CloudFileShare, Boolean> getRequest = new StorageRequest<CloudFileClient, CloudFileShare, Boolean>(
                options, this.getStorageUri()) {

            @Override
            public void setRequestLocationMode() {
                this.setRequestLocationMode(primaryOnly ? RequestLocationMode.PRIMARY_ONLY
                        : RequestLocationMode.PRIMARY_OR_SECONDARY);
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
                StorageRequest.signBlobQueueAndFileRequest(connection, client, -1L, null);
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
        options = FileRequestOptions.applyDefaults(options, this.fileServiceClient);

        ExecutionEngine.executeWithRetry(this.fileServiceClient, this,
                this.uploadMetadataImpl(accessCondition, options), options.getRetryPolicyFactory(), opContext);
    }

    @DoesServiceRequest
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
                StorageRequest.signBlobQueueAndFileRequest(connection, client, 0L, null);
            }

            @Override
            public Void preProcessResponse(CloudFileShare share, CloudFileClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                }

                share.updatePropertiesFromResponse(this.getConnection());
                return null;
            }
        };

        return putRequest;
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
        return new CloudFileDirectory(this.storageUri, this.fileServiceClient);
    }

    /**
     * Strips the query and verifies the URI is absolute.
     * 
     * @param completeUri
     *            A {@link StorageUri} object which represents the complete URI.
     * @param existingClient
     *            A {@link CloudFileClient} object which represents the client to use.
     * @param usePathStyleUris
     *            <code>true</code> if path-style URIs are used; otherwise, <code>false</code>.
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     */
    private void parseQueryAndVerify(final StorageUri completeUri, final CloudFileClient existingClient,
            final boolean usePathStyleUri) throws StorageException, URISyntaxException {
        Utility.assertNotNull("completeUri", completeUri);

        if (!completeUri.isAbsolute()) {
            final String errorMessage = String.format(SR.RELATIVE_ADDRESS_NOT_PERMITTED, completeUri.toString());
            throw new IllegalArgumentException(errorMessage);
        }

        this.storageUri = PathUtility.stripURIQueryAndFragment(completeUri);
        this.fileServiceClient = (existingClient == null) ? new CloudFileClient(
                PathUtility.getServiceClientBaseAddress(this.storageUri, usePathStyleUri), null) : existingClient;
        this.name = PathUtility.getShareNameFromUri(completeUri.getPrimaryUri(), usePathStyleUri);
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
     * Returns the metadata for the share. This value is initialized with the metadata from the queue by a call to
     * {@link #downloadAttributes}, and is set on the queue with a call to {@link #uploadMetadata}.
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
