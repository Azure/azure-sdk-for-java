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
package com.microsoft.windowsazure.storage.blob;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import com.microsoft.windowsazure.storage.Constants;
import com.microsoft.windowsazure.storage.DoesServiceRequest;
import com.microsoft.windowsazure.storage.OperationContext;
import com.microsoft.windowsazure.storage.ResultContinuation;
import com.microsoft.windowsazure.storage.ResultContinuationType;
import com.microsoft.windowsazure.storage.ResultSegment;
import com.microsoft.windowsazure.storage.ServiceClient;
import com.microsoft.windowsazure.storage.ServiceProperties;
import com.microsoft.windowsazure.storage.ServiceStats;
import com.microsoft.windowsazure.storage.StorageCredentials;
import com.microsoft.windowsazure.storage.StorageCredentialsAnonymous;
import com.microsoft.windowsazure.storage.StorageException;
import com.microsoft.windowsazure.storage.StorageUri;
import com.microsoft.windowsazure.storage.core.ExecutionEngine;
import com.microsoft.windowsazure.storage.core.LazySegmentedIterable;
import com.microsoft.windowsazure.storage.core.ListResponse;
import com.microsoft.windowsazure.storage.core.ListingContext;
import com.microsoft.windowsazure.storage.core.SegmentedStorageRequest;
import com.microsoft.windowsazure.storage.core.StorageRequest;
import com.microsoft.windowsazure.storage.core.Utility;

/**
 * Provides a client for accessing the Windows Azure Blob service.
 * <p>
 * This class provides a point of access to the Blob service. The service client encapsulates the base URI for the Blob
 * service. If the service client will be used for authenticated access, it also encapsulates the credentials for
 * accessing the storage account.
 */
public final class CloudBlobClient extends ServiceClient {
    /**
     * Holds the maximum size of a blob in bytes that may be uploaded as a single blob.
     */
    private int singleBlobPutThresholdInBytes = BlobConstants.DEFAULT_SINGLE_BLOB_PUT_THRESHOLD_IN_BYTES;

    /**
     * Holds the number of simultaneous operations a given blob operation may perform.
     */
    private int concurrentRequestCount = BlobConstants.DEFAULT_CONCURRENT_REQUEST_COUNT;

    /**
     * Holds the default delimiter that may be used to create a virtual directory structure of blobs.
     */
    private String directoryDelimiter = BlobConstants.DEFAULT_DELIMITER;

    /**
     * Creates an instance of the <code>CloudBlobClient</code> class using the specified Blob service endpoint.
     * 
     * @param baseUri
     *            A <code>java.net.URI</code> object that represents the Blob service endpoint used to create the
     *            client.
     */
    public CloudBlobClient(final URI baseUri) {
        this(new StorageUri(baseUri), StorageCredentialsAnonymous.ANONYMOUS);
    }

    /**
     * Creates an instance of the <code>CloudBlobClient</code> class using the specified Blob service endpoint and
     * account credentials.
     * 
     * @param baseUri
     *            A <code>java.net.URI</code> object that represents the Blob service endpoint used to create the
     *            client.
     * @param credentials
     *            A {@link StorageCredentials} object that represents the account credentials.
     */
    public CloudBlobClient(final URI baseUri, StorageCredentials credentials) {
        this(new StorageUri(baseUri), credentials);
    }

    /**
     * Creates an instance of the <code>CloudBlobClient</code> class using the specified Blob service endpoint and
     * account credentials.
     * 
     * @param storageUri
     *            A <code>StorageUri</code> object that represents the Blob service endpoint used to create the
     *            client.
     * @param credentials
     *            A {@link StorageCredentials} object that represents the account credentials.
     */
    public CloudBlobClient(final StorageUri storageUri, StorageCredentials credentials) {
        super(storageUri, credentials);
        this.directoryDelimiter = BlobConstants.DEFAULT_DELIMITER;
    }

    /**
     * Returns the number of maximum concurrent requests allowed.
     * 
     * @return The number of maximum concurrent requests allowed.
     */
    public int getConcurrentRequestCount() {
        return this.concurrentRequestCount;
    }

    /**
     * Gets a {@link CloudBlobContainer} object with the specified name.
     * 
     * @param containerName
     *            The name of the container, which must adhere to container naming rules. The container name should not
     *            include any path separator characters (/).
     *            Container names must be lowercase, between 3-63 characters long and must start with a letter or
     *            number. Container names may contain only letters, numbers, and the dash (-) character.
     * @return A reference to a {@link CloudBlobContainer} object.
     * @throws URISyntaxException
     *             If the resource URI constructed based on the containerName is invalid.
     * @throws StorageException
     *             If a storage service error occurred.
     * @see <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd135715.aspx">Naming and Referencing
     *      Containers, Blobs, and Metadata</a>
     */
    public CloudBlobContainer getContainerReference(final String containerName) throws URISyntaxException,
            StorageException {
        return new CloudBlobContainer(containerName, this);
    }

    /**
     * Returns the value for the default delimiter used for cloud blob directories. The default is '/'.
     * 
     * @return The value for the default delimiter.
     */
    public String getDirectoryDelimiter() {
        return this.directoryDelimiter;
    }

    /**
     * Returns the threshold size used for writing a single blob for this Blob service client.
     * 
     * @return The maximum size, in bytes, of a blob that may be uploaded as a single blob, ranging from 1 to 64 MB
     *         inclusive. The default value is 32 MBs.
     *         <p>
     *         If a blob size is above the threshold, it will be uploaded as blocks.
     */
    public int getSingleBlobPutThresholdInBytes() {
        return this.singleBlobPutThresholdInBytes;
    }

    /**
     * Returns an enumerable collection of blob containers for this Blob service client.
     * 
     * @return An enumerable collection of {@link CloudBlobContainer} objects retrieved lazily that represent the
     *         containers for this
     *         client.
     * @throws StorageException
     */
    @DoesServiceRequest
    public Iterable<CloudBlobContainer> listContainers() throws StorageException {
        return this
                .listContainersWithPrefix(null, ContainerListingDetails.NONE, null /* options */, null /* opContext */);
    }

    /**
     * Returns an enumerable collection of blob containers whose names begin with the specified prefix for this Blob
     * service client.
     * 
     * @param prefix
     *            A <code>String</code> that represents the container name prefix.
     * 
     * @return An enumerable collection of {@link CloudBlobContainer} objects retrieved lazily that represent the
     *         containers for this
     *         client whose names begin with the specified prefix.
     * @throws StorageException
     */
    @DoesServiceRequest
    public Iterable<CloudBlobContainer> listContainers(final String prefix) throws StorageException {
        return this
                .listContainersWithPrefix(prefix, ContainerListingDetails.NONE, null /* options */, null /* opContext */);
    }

    /**
     * Returns an enumerable collection of blob containers whose names begin with the specified prefix for this Blob
     * service client, using the specified details setting, request options, and operation context.
     * 
     * @param prefix
     *            A <code>String</code> that represents the container name prefix.
     * @param detailsIncluded
     *            A {@link ContainerListingDetails} value that indicates whether container metadata will be returned.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return An enumerable collection of {@link CloudBlobContainer} objects retrieved lazily that represents the
     *         containers for this
     *         client.
     * @throws StorageException
     */
    @DoesServiceRequest
    public Iterable<CloudBlobContainer> listContainers(final String prefix,
            final ContainerListingDetails detailsIncluded, final BlobRequestOptions options,
            final OperationContext opContext) throws StorageException {
        return this.listContainersWithPrefix(prefix, detailsIncluded, options, opContext);
    }

    /**
     * Returns a result segment of an enumerable collection of blob containers for this Blob service client.
     * 
     * @return A {@link ResultSegment} object that contains a segment of the enumerable collection of
     *         {@link CloudBlobContainer} objects that represent the containers in this container.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public ResultSegment<CloudBlobContainer> listContainersSegmented() throws StorageException {
        return this.listContainersSegmented(null, ContainerListingDetails.NONE, 0, null /* continuationToken */,
                null /* options */, null /* opContext */);
    }

    /**
     * Returns a result segment of an enumerable collection of blob containers whose names begin with the specified
     * prefix for this Blob service client.
     * 
     * @param prefix
     *            A <code>String</code> that represents the prefix of the container name.
     * 
     * @return A {@link ResultSegment} object that contains a segment of the enumerable collection of
     *         {@link CloudBlobContainer} objects that represent the containers whose names begin with the specified
     *         prefix.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public ResultSegment<CloudBlobContainer> listContainersSegmented(final String prefix) throws StorageException {
        return this.listContainersWithPrefixSegmented(prefix, ContainerListingDetails.NONE, 0,
                null /* continuationToken */, null /* options */, null /* opContext */);
    }

    /**
     * Returns a result segment of an enumerable collection of blob containers whose names begin with the specified
     * prefix for this container, using the specified listing details options, request options, and operation context.
     * 
     * @param prefix
     *            A <code>String</code> that represents the prefix of the container name.
     * @param detailsIncluded
     *            A {@link ContainerListingDetails} value that indicates whether container metadata will be returned.
     * @param maxResults
     *            The maximum number of results to retrieve.
     * @param continuationToken
     *            A {@link ResultContinuation} object that represents a continuation token returned by a previous
     *            listing operation.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return A {@link ResultSegment} object that contains a segment of the enumerable collection of
     *         {@link CloudBlobContainer} objects that represent the containers in this container.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public ResultSegment<CloudBlobContainer> listContainersSegmented(final String prefix,
            final ContainerListingDetails detailsIncluded, final int maxResults,
            final ResultContinuation continuationToken, final BlobRequestOptions options,
            final OperationContext opContext) throws StorageException {

        return this.listContainersWithPrefixSegmented(prefix, detailsIncluded, maxResults, continuationToken, options,
                opContext);
    }

    /**
     * Returns an enumerable collection of blob containers whose names begin with the specified prefix, using the
     * specified details setting, request options, and operation context.
     * 
     * @param prefix
     *            A <code>String</code> that represents the prefix of the container name.
     * @param detailsIncluded
     *            A {@link ContainerListingDetails} value that indicates whether container metadata will be returned.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return An enumerable collection of {@link CloudBlobContainer} objects retrieved lazily that represent the
     *         containers whose names
     *         begin with the specified prefix.
     * @throws StorageException
     */
    private Iterable<CloudBlobContainer> listContainersWithPrefix(final String prefix,
            final ContainerListingDetails detailsIncluded, BlobRequestOptions options, OperationContext opContext)
            throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = BlobRequestOptions.applyDefaults(options, BlobType.UNSPECIFIED, this);

        SegmentedStorageRequest segmentedRequest = new SegmentedStorageRequest();

        return new LazySegmentedIterable<CloudBlobClient, Void, CloudBlobContainer>(
                this.listContainersWithPrefixSegmentedImpl(prefix, detailsIncluded, -1, options, segmentedRequest),
                this, null, options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Returns a result segment of an enumerable collection of blob containers whose names begin with the specified
     * prefix for this container, using the specified listing details options, request options, and operation context.
     * 
     * @param prefix
     *            A <code>String</code> that represents the prefix of the container name.
     * @param detailsIncluded
     *            A {@link ContainerListingDetails} value that indicates whether container metadata will be returned.
     * @param maxResults
     *            The maximum number of results to retrieve.
     * @param continuationToken
     *            A {@link ResultContinuation} object that represents a continuation token returned by a previous
     *            listing operation.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return A {@link ResultSegment} object that contains a segment of the enumerable collection of
     *         {@link CloudBlobContainer} objects that represent the containers in this container.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    private ResultSegment<CloudBlobContainer> listContainersWithPrefixSegmented(final String prefix,
            final ContainerListingDetails detailsIncluded, final int maxResults,
            final ResultContinuation continuationToken, BlobRequestOptions options, OperationContext opContext)
            throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = BlobRequestOptions.applyDefaults(options, BlobType.UNSPECIFIED, this);

        Utility.assertContinuationType(continuationToken, ResultContinuationType.CONTAINER);

        SegmentedStorageRequest segmentedRequest = new SegmentedStorageRequest();
        segmentedRequest.setToken(continuationToken);

        return ExecutionEngine.executeWithRetry(this, null, this.listContainersWithPrefixSegmentedImpl(prefix,
                detailsIncluded, maxResults, options, segmentedRequest), options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudBlobClient, Void, ResultSegment<CloudBlobContainer>> listContainersWithPrefixSegmentedImpl(
            final String prefix, final ContainerListingDetails detailsIncluded, final int maxResults,
            final BlobRequestOptions options, final SegmentedStorageRequest segmentedRequest) throws StorageException {

        Utility.assertContinuationType(segmentedRequest.getToken(), ResultContinuationType.CONTAINER);

        final ListingContext listingContext = new ListingContext(prefix, maxResults);

        final StorageRequest<CloudBlobClient, Void, ResultSegment<CloudBlobContainer>> getRequest = new StorageRequest<CloudBlobClient, Void, ResultSegment<CloudBlobContainer>>(
                options, this.getStorageUri()) {

            @Override
            public void setRequestLocationMode() {
                this.setRequestLocationMode(Utility.getListingLocationMode(segmentedRequest.getToken()));
            }

            @Override
            public HttpURLConnection buildRequest(CloudBlobClient client, Void parentObject, OperationContext context)
                    throws Exception {
                listingContext.setMarker(segmentedRequest.getToken() != null ? segmentedRequest.getToken()
                        .getNextMarker() : null);
                return ContainerRequest.list(client.getEndpoint(), options.getTimeoutIntervalInMs(), listingContext,
                        detailsIncluded, context);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudBlobClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobAndQueueRequest(connection, client, -1L, null);
            }

            @Override
            public ResultSegment<CloudBlobContainer> preProcessResponse(Void parentObject, CloudBlobClient client,
                    OperationContext context) throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                }
                return null;
            }

            @Override
            public ResultSegment<CloudBlobContainer> postProcessResponse(HttpURLConnection connection, Void container,
                    CloudBlobClient client, OperationContext context, ResultSegment<CloudBlobContainer> storageObject)
                    throws Exception {
                final ListResponse<CloudBlobContainer> response = BlobDeserializer.getContainerList(this
                        .getConnection().getInputStream(), client);
                ResultContinuation newToken = null;

                if (response.getNextMarker() != null) {
                    newToken = new ResultContinuation();
                    newToken.setNextMarker(response.getNextMarker());
                    newToken.setContinuationType(ResultContinuationType.CONTAINER);
                    newToken.setTargetLocation(this.getResult().getTargetLocation());
                }

                final ResultSegment<CloudBlobContainer> resSegment = new ResultSegment<CloudBlobContainer>(
                        response.getResults(), maxResults, newToken);

                // Important for listContainers because this is required by the lazy iterator between executions.
                segmentedRequest.setToken(resSegment.getContinuationToken());
                return resSegment;
            }
        };

        return getRequest;
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
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @return ServiceStats for the given storage service
     * @throws StorageException
     */
    @DoesServiceRequest
    public ServiceStats getServiceStats(BlobRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = BlobRequestOptions.applyDefaults(options, BlobType.UNSPECIFIED, this);

        return ExecutionEngine.executeWithRetry(this, null, this.getServiceStatsImpl(options, false),
                this.getRetryPolicyFactory(), opContext);
    }

    /**
     * Retrieves the current ServiceProperties for the given storage service. This includes Metrics and Logging
     * Configurations.
     * 
     * @return the ServiceProperties object representing the current configuration of the service.
     * @throws StorageException
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
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @return the ServiceProperties object representing the current configuration of the service.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final ServiceProperties downloadServiceProperties(BlobRequestOptions options, OperationContext opContext)
            throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = BlobRequestOptions.applyDefaults(options, BlobType.UNSPECIFIED, this);

        return ExecutionEngine.executeWithRetry(this, null, this.downloadServicePropertiesImpl(options, false),
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
    public void uploadServiceProperties(final ServiceProperties properties, BlobRequestOptions options,
            OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = BlobRequestOptions.applyDefaults(options, BlobType.UNSPECIFIED, this);

        Utility.assertNotNull("properties", properties);

        ExecutionEngine.executeWithRetry(this, null,
                this.uploadServicePropertiesImpl(properties, options, opContext, false),
                options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Sets the maximum number of concurrent requests allowed for the Blob service client.
     * 
     * @param concurrentRequestCount
     *            The value being assigned as the maximum number of concurrent requests allowed for the Blob service
     *            client.
     */
    public void setConcurrentRequestCount(final int concurrentRequestCount) {
        this.concurrentRequestCount = concurrentRequestCount;
    }

    /**
     * Sets the value for the default delimiter used for cloud blob directories.
     * 
     * @param directoryDelimiter
     *            A <code>String</code> that represents the value for the default directory delimiter.
     */
    public void setDirectoryDelimiter(final String directoryDelimiter) {
        Utility.assertNotNullOrEmpty("directoryDelimiter", directoryDelimiter);
        this.directoryDelimiter = directoryDelimiter;
    }

    /**
     * Sets the threshold size used for writing a single blob to use with this Blob service client.
     * 
     * @param singleBlobPutThresholdInBytes
     *            The maximum size, in bytes, of a blob that may be uploaded as a single blob, ranging from 1 MB to 64
     *            MB inclusive. If a blob size is above the threshold, it will be uploaded as blocks.
     * 
     * @throws IllegalArgumentException
     *             If <code>minimumReadSize</code> is less than 1 MB or greater than 64 MB.
     */
    public void setSingleBlobPutThresholdInBytes(final int singleBlobPutThresholdInBytes) {
        if (singleBlobPutThresholdInBytes > BlobConstants.MAX_SINGLE_UPLOAD_BLOB_SIZE_IN_BYTES
                || singleBlobPutThresholdInBytes < 1 * Constants.MB) {
            throw new IllegalArgumentException("SingleBlobUploadThresholdInBytes");
        }

        this.singleBlobPutThresholdInBytes = singleBlobPutThresholdInBytes;
    }
}
