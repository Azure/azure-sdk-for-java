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
import com.microsoft.azure.storage.ResultContinuation;
import com.microsoft.azure.storage.ResultContinuationType;
import com.microsoft.azure.storage.ResultSegment;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsSharedAccessSignature;
import com.microsoft.azure.storage.StorageErrorCodeStrings;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageUri;
import com.microsoft.azure.storage.core.ExecutionEngine;
import com.microsoft.azure.storage.core.LazySegmentedIterable;
import com.microsoft.azure.storage.core.ListResponse;
import com.microsoft.azure.storage.core.ListingContext;
import com.microsoft.azure.storage.core.PathUtility;
import com.microsoft.azure.storage.core.RequestLocationMode;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.SegmentedStorageRequest;
import com.microsoft.azure.storage.core.SharedAccessSignatureHelper;
import com.microsoft.azure.storage.core.StorageRequest;
import com.microsoft.azure.storage.core.Utility;

/**
 * Represents a virtual directory of files.
 * <p>
 * Directories, which are encapsulated as {@link CloudFileDirectory} objects, hold files and can also contain
 * sub-directories.
 */
public final class CloudFileDirectory implements ListFileItem {
    /**
     * Holds the share reference.
     */
    private CloudFileShare share;

    /**
     * Represents the parent directory.
     */
    private CloudFileDirectory parent;

    /**
     * Represents the File client.
     */
    private CloudFileClient fileServiceClient;

    /**
     * Holds the list of URIs for all locations.
     */
    private StorageUri storageUri;

    /**
     * Holds the name of the directory.
     */
    private String name;
    
    /**
     * Represents the directory metadata.
     */
    private HashMap<String, String> metadata = new HashMap<String, String>();

    /**
     * Holds a FileDirectoryProperties object that holds the directory's system properties.
     */
    private FileDirectoryProperties properties = new FileDirectoryProperties();
    
    /**
     * Creates an instance of the <code>CloudFileDirectory</code> class using an absolute URI to the directory.
     * 
     * @param directoryAbsoluteUri
     *            A {@link URI} that represents the file directory's address.
     * @throws StorageException
     */
    public CloudFileDirectory(final URI directoryAbsoluteUri) throws StorageException {
        this(new StorageUri(directoryAbsoluteUri));
    }
    
    /**
     * Creates an instance of the <code>CloudFileDirectory</code> class using an absolute URI to the directory.
     * 
     * @param directoryAbsoluteUri
     *            A {@link StorageUri} that represents the file directory's address.
     * @throws StorageException
     */
    public CloudFileDirectory(final StorageUri directoryAbsoluteUri) throws StorageException {
        this(directoryAbsoluteUri, (StorageCredentials) null);
    }

    /**
     * Creates an instance of the <code>CloudFileDirectory</code> class using an absolute URI to the directory 
     * and credentials.
     * 
     * @param directoryAbsoluteUri
     *            A {@link URI} that represents the file directory's address.
     * @param credentials
     *            A {@link StorageCredentials} object used to authenticate access.
     * @throws StorageException
     */
    public CloudFileDirectory(final URI directoryAbsoluteUri, final StorageCredentials credentials) 
            throws StorageException {
        this(new StorageUri(directoryAbsoluteUri), credentials);
    }

    /**
     * Creates an instance of the <code>CloudFileDirectory</code> class using an absolute URI to the directory
     * and credentials.
     * 
     * @param directoryAbsoluteUri
     *            A {@link StorageUri} that represents the file directory's address.
     * @param credentials
     *            A {@link StorageCredentials} object used to authenticate access.
     * @throws StorageException
     */
    public CloudFileDirectory(final StorageUri directoryAbsoluteUri, final StorageCredentials credentials)
            throws StorageException {
        this.parseQueryAndVerify(directoryAbsoluteUri, credentials);
    }
    
    /**
     * Creates an instance of the <code>CloudFileDirectory</code> class using the specified address, share,
     * and client.
     * 
     * @param uri
     *            A {@link StorageUri} that represents the file directory's address.
     * @param directoryName
     *            A <code>String</code> that represents the name of the directory.
     * @param share
     *            A {@link CloudFileShare} object that represents the associated file share.
     */
    protected CloudFileDirectory(final StorageUri uri, final String directoryName, final CloudFileShare share) {
        Utility.assertNotNull("uri", uri);
        Utility.assertNotNull("directoryName", directoryName);
        Utility.assertNotNull("share", share);

        this.name = directoryName;
        this.fileServiceClient = share.getServiceClient();
        this.share = share;
        this.storageUri = uri;
    }

    /**
     * Creates the directory.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void create() throws StorageException {
        this.create(null /* options */, null /* opContext */);
    }

    /**
     * Creates the directory using the specified options and operation context.
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

        ExecutionEngine.executeWithRetry(this.fileServiceClient, this, createDirectoryImpl(options),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudFileClient, CloudFileDirectory, Void> createDirectoryImpl(
            final FileRequestOptions options) {
        final StorageRequest<CloudFileClient, CloudFileDirectory, Void> putRequest =
                new StorageRequest<CloudFileClient, CloudFileDirectory, Void>(options, this.getStorageUri()) {
            @Override
            public HttpURLConnection buildRequest(CloudFileClient client, CloudFileDirectory directory,
                    OperationContext context) throws Exception {
                final HttpURLConnection request = FileRequest.createDirectory(
                        directory.getTransformedAddress().getUri(this.getCurrentLocation()), options, context);
                return request;
            }
            
            @Override
            public void setHeaders(HttpURLConnection connection, CloudFileDirectory directory, OperationContext context) {
                FileRequest.addMetadata(connection, directory.getMetadata(), context);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudFileClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, 0L, context);
            }

            @Override
            public Void preProcessResponse(CloudFileDirectory directory, CloudFileClient client,
                    OperationContext context) throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_CREATED) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                // Set attributes
                final FileDirectoryAttributes attributes = FileResponse
                        .getFileDirectoryAttributes(this.getConnection(), client.isUsePathStyleUris());
                directory.setProperties(attributes.getProperties());
                return null;
            }
        };

        return putRequest;
    }

    /**
     * Creates the directory if it does not exist.
     * 
     * @return <code>true</code> if the directory did not already exist and was created; otherwise, <code>false</code>.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public boolean createIfNotExists() throws StorageException {
        return this.createIfNotExists(null /* options */, null /* opContext */);
    }

    /**
     * Creates the directory if it does not exist, using the specified request options and operation context.
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
     * @return <code>true</code> if the directory did not already exist and was created; otherwise, <code>false</code>.
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
                        && StorageErrorCodeStrings.RESOURCE_ALREADY_EXISTS.equals(e.getErrorCode())) {
                    return false;
                }
                else {
                    throw e;
                }
            }
        }
    }

    /**
     * Deletes the directory.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void delete() throws StorageException {
        this.delete(null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Deletes the directory using the specified request options and operation context.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the directory.
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

        ExecutionEngine.executeWithRetry(this.fileServiceClient, this, deleteDirectoryImpl(accessCondition, options),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudFileClient, CloudFileDirectory, Void> deleteDirectoryImpl(
            final AccessCondition accessCondition, final FileRequestOptions options) {
        final StorageRequest<CloudFileClient, CloudFileDirectory, Void> putRequest =
                new StorageRequest<CloudFileClient, CloudFileDirectory, Void>(options, this.getStorageUri()) {
            @Override
            public HttpURLConnection buildRequest(CloudFileClient client, CloudFileDirectory directory,
                    OperationContext context) throws Exception {
                return FileRequest.deleteDirectory(directory.getStorageUri().getPrimaryUri(), options, context,
                        accessCondition);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudFileClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, -1L, context);
            }

            @Override
            public Void preProcessResponse(CloudFileDirectory directory, CloudFileClient client,
                    OperationContext context) throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_ACCEPTED) {
                    this.setNonExceptionedRetryableFailure(true);
                }

                return null;
            }
        };

        return putRequest;
    }

    /**
     * Deletes the directory if it exists.
     * 
     * @return <code>true</code> if the directory did not already exist and was created; otherwise, <code>false</code>.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public boolean deleteIfExists() throws StorageException {
        return this.deleteIfExists(null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Deletes the directory if it exists using the specified request options and operation context.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the directory.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return <code>true</code> if the directory existed and was deleted; otherwise, <code>false</code>.
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
                        && StorageErrorCodeStrings.RESOURCE_NOT_FOUND.equals(e.getErrorCode())) {
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
     * Returns a value that indicates whether the directory exists.
     * 
     * @return <code>true</code> if the directory exists, otherwise <code>false</code>.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public boolean exists() throws StorageException {
        return this.exists(null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Returns a value that indicates whether the directory exists, using the specified request options and operation
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
     * @return <code>true</code> if the directory exists, otherwise <code>false</code>.
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

    private StorageRequest<CloudFileClient, CloudFileDirectory, Boolean> existsImpl(final boolean primaryOnly,
            final AccessCondition accessCondition, final FileRequestOptions options) {
        final StorageRequest<CloudFileClient, CloudFileDirectory, Boolean> getRequest =
                new StorageRequest<CloudFileClient, CloudFileDirectory, Boolean>(options, this.getStorageUri()) {

            @Override
            public void setRequestLocationMode() {
                this.setRequestLocationMode(primaryOnly ? RequestLocationMode.PRIMARY_ONLY
                        : RequestLocationMode.PRIMARY_OR_SECONDARY);
            }

            @Override
            public HttpURLConnection buildRequest(CloudFileClient client, CloudFileDirectory directory,
                    OperationContext context) throws Exception {
                return FileRequest.getDirectoryProperties(
                        directory.getTransformedAddress().getUri(this.getCurrentLocation()), options, context,
                        accessCondition);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudFileClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, -1L, context);
            }

            @Override
            public Boolean preProcessResponse(CloudFileDirectory directory, CloudFileClient client,
                    OperationContext context) throws Exception {
                if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_OK) {
                    directory.updatePropertiesFromResponse(this.getConnection());
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
     * Uploads the directory's metadata.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void uploadMetadata() throws StorageException {
        this.uploadMetadata(null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Uploads the directory's metadata using the specified request options and operation context.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the directory.
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
    
    private StorageRequest<CloudFileClient, CloudFileDirectory, Void> uploadMetadataImpl(
            final AccessCondition accessCondition, final FileRequestOptions options) {
        final StorageRequest<CloudFileClient, CloudFileDirectory, Void> putRequest =
                new StorageRequest<CloudFileClient, CloudFileDirectory, Void>(
                options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(
                    CloudFileClient client, CloudFileDirectory directory, OperationContext context) throws Exception {
                return FileRequest.setDirectoryMetadata(directory.getTransformedAddress().getUri(this.getCurrentLocation()),
                        options, context, accessCondition);
            }

            @Override
            public void setHeaders(HttpURLConnection connection, CloudFileDirectory directory, OperationContext context) {
                FileRequest.addMetadata(connection, directory.getMetadata(), context);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudFileClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, 0L, context);
            }

            @Override
            public Void preProcessResponse(CloudFileDirectory directory, CloudFileClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                }

                directory.updatePropertiesFromResponse(this.getConnection());
                return null;
            }
        };

        return putRequest;
    }

    /**
     * Downloads the directory's properties.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void downloadAttributes() throws StorageException {
        this.downloadAttributes(null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Downloads the directory's properties using the specified request options and operation context.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the directory.
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

    private StorageRequest<CloudFileClient, CloudFileDirectory, Void> downloadAttributesImpl(
            final AccessCondition accessCondition, final FileRequestOptions options) {
        final StorageRequest<CloudFileClient, CloudFileDirectory, Void> getRequest =
                new StorageRequest<CloudFileClient, CloudFileDirectory, Void>(options, this.getStorageUri()) {

            @Override
            public void setRequestLocationMode() {
                this.setRequestLocationMode(RequestLocationMode.PRIMARY_OR_SECONDARY);
            }

            @Override
            public HttpURLConnection buildRequest(CloudFileClient client, CloudFileDirectory directory,
                    OperationContext context) throws Exception {
                return FileRequest.getDirectoryProperties(
                        directory.getTransformedAddress().getUri(this.getCurrentLocation()), options, context,
                        accessCondition);
            }
            
            @Override
            public void signRequest(HttpURLConnection connection, CloudFileClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, -1L, context);
            }

            @Override
            public Void preProcessResponse(CloudFileDirectory directory, CloudFileClient client,
                    OperationContext context) throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                // Set properties
                final FileDirectoryAttributes attributes =
                        FileResponse.getFileDirectoryAttributes(this.getConnection(), client.isUsePathStyleUris());
                directory.setMetadata(attributes.getMetadata());
                directory.setProperties(attributes.getProperties());
                return null;
            }
        };

        return getRequest;
    }

    /**
     * Returns an enumerable collection of file and directory items for the directory.
     * 
     * @return An enumerable collection of {@link ListFileItem} objects that represent the file and directory items in
     *         this directory.
     */
    @DoesServiceRequest
    public Iterable<ListFileItem> listFilesAndDirectories() {
        return this.listFilesAndDirectories(null /* options */, null /* opContext */);
    }

    /**
     * Returns an enumerable collection of file and directory items for the directory.
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
     * @return An enumerable collection of {@link ListFileItem} objects that represent the file and directory items in
     *         this directory.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    @DoesServiceRequest
    public Iterable<ListFileItem> listFilesAndDirectories(FileRequestOptions options, OperationContext opContext) {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient);

        SegmentedStorageRequest segmentedRequest = new SegmentedStorageRequest();

        return new LazySegmentedIterable<CloudFileClient, CloudFileDirectory, ListFileItem>(
                this.listFilesAndDirectoriesSegmentedImpl(null, options, segmentedRequest), this.fileServiceClient, this,
                options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Returns a result segment of an enumerable collection of files and directories for this File service client.
     * 
     * @return A {@link ResultSegment} object that contains a segment of the enumerable collection of
     *         {@link ListFileItem} objects that represent the files and directories.
     * @throws StorageException
     */
    @DoesServiceRequest
    public ResultSegment<ListFileItem> listFilesAndDirectoriesSegmented() throws StorageException {
        return this.listFilesAndDirectoriesSegmented(
                null, null /* continuationToken */, null /* options */, null /* opContext */);
    }

    /**
     * Returns a result segment of an enumerable collection of files and directories for this directory, using the
     * specified listing details options, request options, and operation context.
     * 
     * @param maxResults
     *            The maximum number of results to retrieve.  If <code>null</code> or greater
     *            than 5000, the server will return up to 5,000 items.  Must be at least 1.
     * @param continuationToken
     *            A {@link ResultContinuation} object that represents a continuation token
     *            returned by a previous listing operation.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for
     *            the request. Specifying <code>null</code> will use the default request options
     *            from the associated service client ( {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current
     *            operation. This object is used to track requests to the storage service,
     *            and to provide additional runtime information about the operation.
     * 
     * @return A {@link ResultSegment} object that contains a segment of the enumerable collection of
     *         {@link ListFileItem} objects that represent the files and directories in this directory.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public ResultSegment<ListFileItem> listFilesAndDirectoriesSegmented(final Integer maxResults,
            final ResultContinuation continuationToken, FileRequestOptions options, OperationContext opContext)
            throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient);

        Utility.assertContinuationType(continuationToken, ResultContinuationType.FILE);

        SegmentedStorageRequest segmentedRequest = new SegmentedStorageRequest();
        segmentedRequest.setToken(continuationToken);

        return ExecutionEngine.executeWithRetry(this.fileServiceClient, this,
                this.listFilesAndDirectoriesSegmentedImpl(maxResults, options, segmentedRequest),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudFileClient, CloudFileDirectory, ResultSegment<ListFileItem>> listFilesAndDirectoriesSegmentedImpl(
            final Integer maxResults, final FileRequestOptions options, final SegmentedStorageRequest segmentedRequest) {

        Utility.assertContinuationType(segmentedRequest.getToken(), ResultContinuationType.FILE);

        final ListingContext listingContext = new ListingContext(null, maxResults);

        final StorageRequest<CloudFileClient, CloudFileDirectory, ResultSegment<ListFileItem>> getRequest =
                new StorageRequest<CloudFileClient, CloudFileDirectory, ResultSegment<ListFileItem>>(
                options, this.getStorageUri()) {

            @Override
            public void setRequestLocationMode() {
                this.setRequestLocationMode(Utility.getListingLocationMode(segmentedRequest.getToken()));
            }

            @Override
            public HttpURLConnection buildRequest(CloudFileClient client, CloudFileDirectory directory,
                    OperationContext context) throws Exception {
                listingContext.setMarker(segmentedRequest.getToken() != null ? segmentedRequest.getToken()
                        .getNextMarker() : null);
                return FileRequest.listFilesAndDirectories(
                        directory.getTransformedAddress().getUri(this.getCurrentLocation()),
                        options, context, listingContext);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudFileClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, -1L, context);
            }

            @Override
            public ResultSegment<ListFileItem> preProcessResponse(CloudFileDirectory directory, CloudFileClient client,
                    OperationContext context) throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                }

                return null;
            }

            @Override
            public ResultSegment<ListFileItem> postProcessResponse(HttpURLConnection connection,
                    CloudFileDirectory directory, CloudFileClient client, OperationContext context,
                    ResultSegment<ListFileItem> storageObject) throws Exception {
                final ListResponse<ListFileItem> response = FileListHandler.getFileAndDirectoryList(this
                        .getConnection().getInputStream(), directory);
                ResultContinuation newToken = null;

                if (response.getNextMarker() != null) {
                    newToken = new ResultContinuation();
                    newToken.setNextMarker(response.getNextMarker());
                    newToken.setContinuationType(ResultContinuationType.FILE);
                    newToken.setTargetLocation(this.getResult().getTargetLocation());
                }

                final ResultSegment<ListFileItem> resSegment = new ResultSegment<ListFileItem>(response.getResults(),
                        response.getMaxResults(), newToken);

                // Important for listFilesAndDirectories because this is required by the lazy iterator between executions.
                segmentedRequest.setToken(resSegment.getContinuationToken());
                return resSegment;
            }
        };

        return getRequest;
    }

    /**
     * Returns a reference to a {@link CloudFile} object that represents a file in this directory.
     * 
     * @param fileName
     *            A <code>String</code> that represents the name of the file.
     * 
     * @return A {@link CloudFile} object that represents a reference to the specified file.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public CloudFile getFileReference(final String fileName) throws URISyntaxException, StorageException {
        Utility.assertNotNullOrEmpty("fileName", fileName);

        StorageUri subdirectoryUri = PathUtility.appendPathToUri(this.storageUri, fileName);
        return new CloudFile(subdirectoryUri, fileName, this.getShare());
    }
    
    /**
     * Returns a reference to a {@link CloudFileDirectory} object that represents a directory in this directory.
     * 
     * @param itemName
     *            A <code>String</code> that represents the name of the directory.
     * 
     * @return A {@link CloudFileDirectory} object that represents a reference to the specified directory.
     * 
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     * @throws StorageException
     */
    public CloudFileDirectory getDirectoryReference(final String itemName) throws URISyntaxException,
            StorageException {
        Utility.assertNotNullOrEmpty("itemName", itemName);

        StorageUri subdirectoryUri = PathUtility.appendPathToUri(this.storageUri, itemName);
        return new CloudFileDirectory(subdirectoryUri, itemName, this.getShare());
    }

    /**
     * Returns the URI for this directory.
     * 
     * @return A <code>java.net.URI</code> object that represents the URI for this directory.
     */
    @Override
    public URI getUri() {
        return this.storageUri.getPrimaryUri();
    }

    /**
     * Returns the list of URIs for all locations.
     * 
     * @return A {@link StorageUri} that represents the list of URIs for all locations.
     */
    @Override
    public final StorageUri getStorageUri() {
        return this.storageUri;
    }

    /**
     * Returns the File service client associated with this directory.
     * 
     * @return An {@link CloudFileClient} object that represents the service client associated with the directory.
     */
    public CloudFileClient getServiceClient() {
        return this.fileServiceClient;
    }

    /**
     * Returns the name of this directory.
     * 
     * @return An <code>String</code> that represents the name of the directory.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Returns the metadata for the directory. This value is initialized with the metadata from the directory
     * by a call to {@link #downloadAttributes}, and is set on the directory with a call to {@link #uploadMetadata}.
     * 
     * @return A <code>java.util.HashMap</code> object that represents the metadata for the directory.
     */
    public HashMap<String, String> getMetadata() {
        return this.metadata;
    }

    /**
     * Returns the {@link FileDirectoryProperties} object that holds the directory's system properties.
     * 
     * @return A {@link FileDirectoryProperties} object that holds the system properties associated with the
     *         directory.
     */
    public FileDirectoryProperties getProperties() {
        return this.properties;
    }

    /**
     * Returns the {@link CloudFileDirectory} parent directory associated with this directory.
     * 
     * @return An {@link CloudFileDirectory} object that represents the parent directory associated with the directory.
     * @throws StorageException
     * @throws URISyntaxException
     */
    @Override
    public CloudFileDirectory getParent() throws URISyntaxException, StorageException {
        if (this.parent == null) {
            final String parentName = CloudFile.getParentNameFromURI(this.getStorageUri(), this.getShare());

            if (parentName != null) {
                StorageUri parentURI = PathUtility.appendPathToUri(this.getShare().getStorageUri(), parentName);
                this.parent = new CloudFileDirectory(parentURI, this.getServiceClient().getCredentials());
            }
        }
        return this.parent;
    }

    /**
     * Returns the share for this directory.
     * 
     * @return A {@link CloudFileShare} that represents the share for this directory.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    @Override
    public CloudFileShare getShare() throws StorageException, URISyntaxException {
        if (this.share == null) {
            this.share = this.fileServiceClient.getShareReference(PathUtility.getShareNameFromUri(this.getUri(),
                    this.fileServiceClient.isUsePathStyleUris()));
        }
        return this.share;
    }

    /**
     * Sets the metadata collection of name-value pairs to be set on the directory with an {@link #uploadMetadata} call.
     * This collection will overwrite any existing directory metadata. If this is set to an empty collection, the
     * directory metadata will be cleared on an {@link #uploadMetadata} call.
     * 
     * @param metadata
     *            A <code>java.util.HashMap</code> object that represents the metadata being assigned to the directory.
     */
    public void setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
    }

    /**
     * Sets the share for the directory.
     * 
     * @param share
     *            A {@link CloudFileShare} object that represents the share being assigned to the directory.
     */
    protected final void setShare(final CloudFileShare share) {
        this.share = share;
    }

    /**
     * Sets the properties for the directory.
     * 
     * @param properties
     *            A {@link FileDirectoryProperties} object that represents the properties being assigned to the
     *            directory.
     */
    protected final void setProperties(final FileDirectoryProperties properties) {
        this.properties = properties;
    }

    /**
     * Sets the list of URIs for all locations.
     * 
     * @param storageUri
     *            A {@link StorageUri} that represents the list of URIs for all locations.
     */
    protected void setStorageUri(final StorageUri storageUri) {
        this.storageUri = storageUri;
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
            this.name = PathUtility.getDirectoryNameFromURI(this.storageUri.getPrimaryUri(), usePathStyleUris);
        }
        catch (final URISyntaxException e) {
            throw Utility.generateNewUnexpectedStorageException(e);
        }
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