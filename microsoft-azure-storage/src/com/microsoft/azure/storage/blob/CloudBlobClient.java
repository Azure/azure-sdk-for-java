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
package com.microsoft.azure.storage.blob;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import com.microsoft.azure.storage.DoesServiceRequest;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.ResultContinuation;
import com.microsoft.azure.storage.ResultContinuationType;
import com.microsoft.azure.storage.ResultSegment;
import com.microsoft.azure.storage.ServiceClient;
import com.microsoft.azure.storage.ServiceProperties;
import com.microsoft.azure.storage.ServiceStats;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageUri;
import com.microsoft.azure.storage.core.ExecutionEngine;
import com.microsoft.azure.storage.core.LazySegmentedIterable;
import com.microsoft.azure.storage.core.ListResponse;
import com.microsoft.azure.storage.core.ListingContext;
import com.microsoft.azure.storage.core.SegmentedStorageRequest;
import com.microsoft.azure.storage.core.StorageRequest;
import com.microsoft.azure.storage.core.Utility;

/**
 * Provides a client for accessing the Microsoft Azure Blob service.
 * <p>
 * This class provides a point of access to the Blob service. The service client encapsulates the base URI for the Blob
 * service. If the service client will be used for authenticated access, it also encapsulates the credentials for
 * accessing the storage account.
 */
public final class CloudBlobClient extends ServiceClient {

    /**
     * Holds the default delimiter that may be used to create a virtual directory structure of blobs.
     */
    private String directoryDelimiter = BlobConstants.DEFAULT_DELIMITER;

    /**
     * Holds the default request option values associated with this Service Client.
     */
    private BlobRequestOptions defaultRequestOptions = new BlobRequestOptions();

    /**
     * Creates an instance of the <code>CloudBlobClient</code> class using the specified Blob service endpoint and
     * anonymous credentials.
     * 
     * @param baseUri
     *            A <code>java.net.URI</code> object that represents the Blob service endpoint used to create the
     *            client.
     */
    public CloudBlobClient(final URI baseUri) {
        this(new StorageUri(baseUri), null /* credentials */);
    }

    /**
     * Creates an instance of the <code>CloudBlobClient</code> class using the specified Blob service endpoint and
     * anonymous credentials.
     * 
     * @param baseUri
     *            A {@link StorageUri} object that represents the Blob service endpoint used to create the
     *            client.
     */
    public CloudBlobClient(final StorageUri baseUri) {
        this(baseUri, null /* credentials */);
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
     *            A {@link StorageUri} object that represents the Blob service endpoint used to create the
     *            client.
     * @param credentials
     *            A {@link StorageCredentials} object that represents the account credentials.
     */
    public CloudBlobClient(final StorageUri storageUri, StorageCredentials credentials) {
        super(storageUri, credentials);
        BlobRequestOptions.applyDefaults(this.defaultRequestOptions, BlobType.UNSPECIFIED);
    }

    /**
     * Gets a {@link CloudBlobContainer} object with the specified name.
     * 
     * @param containerName
     *            The name of the container, which must adhere to container naming rules. The container name should not
     *            include any path separator characters (/).
     *            Container names must be lowercase, between 3-63 characters long and must start with a letter or
     *            number. Container names may contain only letters, numbers, and the dash (-) character.
     * 
     * @return A reference to a {@link CloudBlobContainer} object.
     * 
     * @throws URISyntaxException
     *             If the resource URI constructed based on the containerName is invalid.
     * @throws StorageException
     *             If a storage service error occurred.
     * 
     * @see <a href="http://msdn.microsoft.com/library/azure/dd135715.aspx">Naming and Referencing Containers, Blobs,
     *      and Metadata</a>
     */
    public CloudBlobContainer getContainerReference(final String containerName) throws URISyntaxException,
            StorageException {
        return new CloudBlobContainer(containerName, this);
    }

    /**
     * Returns the value for the default delimiter used for cloud blob directories. The default is '/'.
     * 
     * @return A <code>String<code> which represents the value for the default delimiter.
     */
    public String getDirectoryDelimiter() {
        return this.directoryDelimiter;
    }

    /**
     * Returns an enumerable collection of blob containers for this Blob service client.
     * 
     * @return An enumerable collection of {@link CloudBlobContainer} objects retrieved lazily that represent the
     *         containers for this client.
     */
    @DoesServiceRequest
    public Iterable<CloudBlobContainer> listContainers() {
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
     *         containers for this client whose names begin with the specified prefix.
     */
    @DoesServiceRequest
    public Iterable<CloudBlobContainer> listContainers(final String prefix) {
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
     *         containers for this client.
     */
    @DoesServiceRequest
    public Iterable<CloudBlobContainer> listContainers(final String prefix,
            final ContainerListingDetails detailsIncluded, final BlobRequestOptions options,
            final OperationContext opContext) {
        return this.listContainersWithPrefix(prefix, detailsIncluded, options, opContext);
    }

    /**
     * Returns a result segment of an enumerable collection of blob containers for this Blob service client.
     * 
     * @return A {@link ResultSegment} object that contains a segment of the enumerable collection of
     *         {@link CloudBlobContainer} objects that represent the containers for this client.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public ResultSegment<CloudBlobContainer> listContainersSegmented() throws StorageException {
        return this.listContainersSegmented(null, ContainerListingDetails.NONE, null, null /* continuationToken */,
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
        return this.listContainersWithPrefixSegmented(prefix, ContainerListingDetails.NONE, null,
                null /* continuationToken */, null /* options */, null /* opContext */);
    }

    /**
     * Returns a result segment of an enumerable collection of blob containers whose names begin with the specified
     * prefix for this Blob service client, using the specified listing details options, request options, and operation
     * context.
     * 
     * @param prefix
     *            A <code>String</code> that represents the prefix of the container name.
     * @param detailsIncluded
     *            A {@link ContainerListingDetails} value that indicates whether container metadata will be returned.
     * @param maxResults
     *           The maximum number of results to retrieve.  If <code>null</code> or greater
     *           than 5000, the server will return up to 5,000 items.  Must be at least 1.
     * @param continuationToken
     *            A {@link ResultContinuation} object that represents a continuation token returned
     *            by a previous listing operation.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the
     *            request. Specifying <code>null</code> will use the default request options from
     *            the associated service client ({@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current
     *            operation. This object is used to track requests to the storage service,
     *            and to provide additional runtime information about the operation.
     * 
     * @return A {@link ResultSegment} object that contains a segment of the enumerable collection of
     *         {@link CloudBlobContainer} objects that represent the containers for this Blob service client.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public ResultSegment<CloudBlobContainer> listContainersSegmented(final String prefix,
            final ContainerListingDetails detailsIncluded, final Integer maxResults,
            final ResultContinuation continuationToken, final BlobRequestOptions options,
            final OperationContext opContext) throws StorageException {

        return this.listContainersWithPrefixSegmented(prefix, detailsIncluded, maxResults, continuationToken, options,
                opContext);
    }

    /**
     * Returns an enumerable collection of blob containers for this Blob service client whose names begin with the
     * specified prefix, using the
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
     *         containers whose names begin with the specified prefix.
     */
    private Iterable<CloudBlobContainer> listContainersWithPrefix(final String prefix,
            final ContainerListingDetails detailsIncluded, BlobRequestOptions options, OperationContext opContext) {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = BlobRequestOptions.populateAndApplyDefaults(options, BlobType.UNSPECIFIED, this);

        SegmentedStorageRequest segmentedRequest = new SegmentedStorageRequest();

        return new LazySegmentedIterable<CloudBlobClient, Void, CloudBlobContainer>(
                this.listContainersWithPrefixSegmentedImpl(prefix, detailsIncluded, null, options, segmentedRequest),
                this, null, options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Returns a result segment of an enumerable collection of blob containers whose names begin with the specified
     * prefix for this Blob service client, using the specified listing details options, request options, and operation
     * context.
     * 
     * @param prefix
     *            A <code>String</code> that represents the prefix of the container name.
     * @param detailsIncluded
     *            A {@link ContainerListingDetails} value that indicates whether container metadata will be returned.
     * @param maxResults
     *            The maximum number of results to retrieve.  If <code>null</code> or greater
     *            than 5000, the server will return up to 5,000 items.  Must be at least 1.
     * @param continuationToken
     *            A {@link ResultContinuation} object that represents a continuation token returned
     *            by a previous listing operation.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the
     *            request. Specifying <code>null</code> will use the default request options
     *            from the associated service client ({@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current
     *            operation. This object is used to track requests to the storage service,
     *             and to provide additional runtime information about the operation.
     * 
     * @return A {@link ResultSegment} object that contains a segment of the enumerable collection of
     *         {@link CloudBlobContainer} objects that represent the containers for this client.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    private ResultSegment<CloudBlobContainer> listContainersWithPrefixSegmented(final String prefix,
            final ContainerListingDetails detailsIncluded, final Integer maxResults,
            final ResultContinuation continuationToken, BlobRequestOptions options, OperationContext opContext)
            throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = BlobRequestOptions.populateAndApplyDefaults(options, BlobType.UNSPECIFIED, this);

        Utility.assertContinuationType(continuationToken, ResultContinuationType.CONTAINER);

        SegmentedStorageRequest segmentedRequest = new SegmentedStorageRequest();
        segmentedRequest.setToken(continuationToken);

        return ExecutionEngine.executeWithRetry(this, null, this.listContainersWithPrefixSegmentedImpl(prefix,
                detailsIncluded, maxResults, options, segmentedRequest), options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudBlobClient, Void, ResultSegment<CloudBlobContainer>> listContainersWithPrefixSegmentedImpl(
            final String prefix, final ContainerListingDetails detailsIncluded, final Integer maxResults,
            final BlobRequestOptions options, final SegmentedStorageRequest segmentedRequest) {

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
                return BlobRequest.listContainers(
                        client.getCredentials().transformUri(client.getStorageUri()).getUri(this.getCurrentLocation()),
                        options, context, listingContext, detailsIncluded);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudBlobClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, -1L, context);
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
                final ListResponse<CloudBlobContainer> response = ContainerListHandler.getContainerList(this
                        .getConnection().getInputStream(), client);
                ResultContinuation newToken = null;

                if (response.getNextMarker() != null) {
                    newToken = new ResultContinuation();
                    newToken.setNextMarker(response.getNextMarker());
                    newToken.setContinuationType(ResultContinuationType.CONTAINER);
                    newToken.setTargetLocation(this.getResult().getTargetLocation());
                }

                final ResultSegment<CloudBlobContainer> resSegment = new ResultSegment<CloudBlobContainer>(
                        response.getResults(), response.getMaxResults(), newToken);

                // Important for listContainers because this is required by the lazy iterator between executions.
                segmentedRequest.setToken(resSegment.getContinuationToken());
                return resSegment;
            }
        };

        return getRequest;
    }

    /**
     * Queries the service for the {@link ServiceStats}.
     * 
     * @return A {@link ServiceStats} object for the given storage service.
     * 
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
     * 
     * @return A {@link ServiceStats} object for the given storage service.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public ServiceStats getServiceStats(BlobRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = BlobRequestOptions.populateAndApplyDefaults(options, BlobType.UNSPECIFIED, this);

        return ExecutionEngine.executeWithRetry(this, null, this.getServiceStatsImpl(options, false),
                options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Retrieves the current {@link ServiceProperties} for the given storage service. This includes Logging,
     * HourMetrics, MinuteMetrics and CORS configurations.
     * 
     * @return A {@link ServiceProperties} object representing the current configuration of the service.
     * 
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
     * @return A {@link ServiceProperties} object representing the current configuration of the service.
     * 
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
        options = BlobRequestOptions.populateAndApplyDefaults(options, BlobType.UNSPECIFIED, this);

        return ExecutionEngine.executeWithRetry(this, null, this.downloadServicePropertiesImpl(options, false),
                options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Uploads a new {@link ServiceProperties} configuration to the given storage service. This includes Logging,
     * HourMetrics, MinuteMetrics and CORS configurations.
     * 
     * @param properties
     *            A {@link ServiceProperties} object which specifies the service properties to upload.
     * 
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
     *            A {@link ServiceProperties} object which specifies the service properties to upload.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
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
        options = BlobRequestOptions.populateAndApplyDefaults(options, BlobType.UNSPECIFIED, this);

        Utility.assertNotNull("properties", properties);

        ExecutionEngine.executeWithRetry(this, null,
                this.uploadServicePropertiesImpl(properties, options, opContext, false),
                options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Sets the value for the default delimiter used for cloud blob directories.
     * 
     * @param directoryDelimiter
     *            A <code>String</code> that specifies the value for the default directory delimiter.
     */
    public void setDirectoryDelimiter(final String directoryDelimiter) {
        Utility.assertNotNullOrEmpty("directoryDelimiter", directoryDelimiter);
        this.directoryDelimiter = directoryDelimiter;
    }

    /**
     * Gets the {@link BlobRequestOptions} that is used for requests associated with this <code>CloudBlobClient</code>
     * 
     * @return The {@link BlobRequestOptions} object containing the values used by this <code>CloudBlobClient</code>
     */
    @Override
    public BlobRequestOptions getDefaultRequestOptions() {
        return this.defaultRequestOptions;
    }

    /**
     * Sets the {@link BlobRequestOptions} that is used for any requests associated with this
     * <code>CloudBlobClient</code> object.
     * 
     * @param defaultRequestOptions
     *            A {@link BlobRequestOptions} object which specifies the options to use.
     */
    public void setDefaultRequestOptions(BlobRequestOptions defaultRequestOptions) {
        Utility.assertNotNull("defaultRequestOptions", defaultRequestOptions);
        this.defaultRequestOptions = defaultRequestOptions;
    }

    /**
     * Indicates whether path-style URIs are being used.
     * 
     * @return <code>true</code> if using path-style URIs; otherwise, <code>false</code>.
     */
    @Override
    protected boolean isUsePathStyleUris() {
        return super.isUsePathStyleUris();
    }
}
