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

package com.microsoft.azure.storage.queue;

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
import com.microsoft.azure.storage.StorageCredentialsAnonymous;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageUri;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.core.ExecutionEngine;
import com.microsoft.azure.storage.core.LazySegmentedIterable;
import com.microsoft.azure.storage.core.ListResponse;
import com.microsoft.azure.storage.core.ListingContext;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.SegmentedStorageRequest;
import com.microsoft.azure.storage.core.StorageRequest;
import com.microsoft.azure.storage.core.Utility;

/**
 * Provides a service client for accessing the Microsoft Azure Queue service.
 */
public final class CloudQueueClient extends ServiceClient {

    /**
     * Holds the default request option values associated with this Service Client.
     */
    private QueueRequestOptions defaultRequestOptions = new QueueRequestOptions();

    /**
     * Initializes a new instance of the <code>CloudQueueClient</code> class using the specified Queue service endpoint
     * and account credentials.
     * 
     * @param baseUri
     *            A <code>java.net.URI</code> object that represents the Queue service endpoint used to create the
     *            client.
     * @param credentials
     *            A {@link StorageCredentials} object that represents the account credentials.
     */
    public CloudQueueClient(final URI baseUri, final StorageCredentials credentials) {
        this(new StorageUri(baseUri, null), credentials);
    }

    /**
     * Initializes a new instance of the <code>CloudQueueClient</code> class using the specified Queue service endpoint
     * and account credentials.
     * 
     * @param baseUri
     *            A {@link StorageUri} object that represents the Queue service endpoint used to create the
     *            client.
     * @param credentials
     *            A {@link StorageCredentials} object that represents the account credentials.
     */
    public CloudQueueClient(final StorageUri baseUri, final StorageCredentials credentials) {
        super(baseUri, credentials);
        if (credentials == null || credentials.getClass().equals(StorageCredentialsAnonymous.class)) {
            throw new IllegalArgumentException(SR.STORAGE_CREDENTIALS_NULL_OR_ANONYMOUS);
        }
        QueueRequestOptions.applyDefaults(this.defaultRequestOptions);
    }

    /**
     * Gets a {@link CloudQueue} object with the specified name.
     * 
     * @param queueName
     *            The name of the queue, which must adhere to queue naming rules. The queue name should not include any
     *            path separator characters (/).
     *            Queue names must be lowercase, between 3-63 characters long and must start with a letter or number.
     *            Queue names may contain only letters, numbers, and the dash (-) character.
     * @return A reference to a {@link CloudQueue} object.
     * @throws URISyntaxException
     *             If the resource URI constructed based on the queueName is invalid.
     * @throws StorageException
     *             If a storage service error occurred.
     * @see <a href="http://msdn.microsoft.com/en-us/library/azure/dd179349.aspx">Naming Queues and Metadata</a>
     */
    public CloudQueue getQueueReference(final String queueName) throws URISyntaxException, StorageException {
        return new CloudQueue(queueName, this);
    }

    /**
     * Gets an iterable collection of queues for this queue service client.
     * 
     * @return An iterable collection of {@link CloudQueue} objects retrieved lazily that
     *         represent the queues in this client.
     */
    @DoesServiceRequest
    public Iterable<CloudQueue> listQueues() {
        return this.listQueues(null, QueueListingDetails.NONE, null, null);
    }

    /**
     * Returns an iterable collection of queues whose names begin with the
     * specified prefix in this Queue service client.
     * 
     * @param prefix
     *            A <code>String</code> that represents the queue name prefix.
     * 
     * @return An iterable collection of {@link CloudQueue} objects retrieved lazily that
     *         represent the queues in this client whose names begin with the
     *         specified prefix.
     */
    @DoesServiceRequest
    public Iterable<CloudQueue> listQueues(final String prefix) {
        return this.listQueues(prefix, QueueListingDetails.NONE, null, null);
    }

    /**
     * Returns an iterable collection of queues whose names begin with the
     * specified prefix for this Queue service client, using the specified
     * details setting, request options, and operation context.
     * 
     * @param prefix
     *            A <code>String</code> that represents the queue name prefix.
     * @param detailsIncluded
     *            A {@link QueueListingDetails} value that indicates whether
     *            queue metadata will be returned.
     * @param options
     *            A {@link QueueRequestOptions} object that specifies any
     *            additional options for the request. Specifying <code>null</code> will use the default request options
     *            from
     *            the associated service client ( {@link CloudQueue}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context
     *            for the current operation. This object is used to track
     *            requests to the storage service, and to provide additional
     *            runtime information about the operation.
     * 
     * @return An iterable collection of {@link CloudQueue} objects retrieved lazily that
     *         represents the specified queues for this client.
     */
    @DoesServiceRequest
    public Iterable<CloudQueue> listQueues(final String prefix, final QueueListingDetails detailsIncluded,
            QueueRequestOptions options, OperationContext opContext) {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = QueueRequestOptions.populateAndApplyDefaults(options, this);

        SegmentedStorageRequest segmentedRequest = new SegmentedStorageRequest();
        return new LazySegmentedIterable<CloudQueueClient, Void, CloudQueue>(this.listQueuesSegmentedImpl(prefix,
                detailsIncluded, null, options, segmentedRequest), this, null, options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Gets a result segment of an iterable collection of queues for this Queue
     * service client.
     * 
     * @return A {@link ResultSegment} of {@link CloudQueue} objects that
     *         contains a segment of the iterable collection of {@link CloudQueue} objects that represent the requested
     *         queues in
     *         the storage service.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public ResultSegment<CloudQueue> listQueuesSegmented() throws StorageException {
        return this.listQueuesSegmented(null, QueueListingDetails.NONE, null, null, null, null);
    }

    /**
     * Gets a result segment of an iterable collection of queues whose names
     * begin with the specified prefix for this Queue service client.
     * 
     * @return A {@link ResultSegment} of {@link CloudQueue} objects that
     *         contains a segment of the iterable collection of {@link CloudQueue} objects that represent the requested
     *         queues in
     *         the storage service.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public ResultSegment<CloudQueue> listQueuesSegmented(final String prefix) throws StorageException {
        return this.listQueuesSegmented(prefix, QueueListingDetails.NONE, null, null, null, null);
    }

    /**
     * Gets a result segment of an iterable collection of queues whose names
     * begin with the specified prefix for this queue, using the specified
     * listing details options, request options, and operation context.
     * 
     * @param prefix
     *            A <code>String</code> that represents the prefix of the queue
     *            name to match.
     * @param detailsIncluded
     *            A {@link QueueListingDetails} value that indicates whether
     *            queue metadata will be returned.
     * @param maxResults
     *            The maximum number of results to retrieve.  If <code>null</code> or greater
     *            than 5000, the server will return up to 5,000 items.  Must be at least 1.
     * @param continuationToken
     *            A {@link ResultContinuation} object that represents a
     *            continuation token returned by a previous listing operation.
     * @param options
     *            A {@link QueueRequestOptions} object that specifies any additional options for
     *            the request. Specifying <code>null</code> will use the default request options
     *            from the associated service client ( {@link CloudQueue}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context
     *            for the current operation. This object is used to track
     *            requests to the storage service, and to provide additional
     *            runtime information about the operation.
     * 
     * @return A {@link ResultSegment} of {@link CloudQueue} objects that contains a segment of
     *         the iterable collection of {@link CloudQueue} objects that represent the requested
     *         queues in the storage service.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public ResultSegment<CloudQueue> listQueuesSegmented(final String prefix,
            final QueueListingDetails detailsIncluded, final Integer maxResults,
            final ResultContinuation continuationToken, QueueRequestOptions options, OperationContext opContext)
            throws StorageException {

        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = QueueRequestOptions.populateAndApplyDefaults(options, this);

        SegmentedStorageRequest segmentedRequest = new SegmentedStorageRequest();
        segmentedRequest.setToken(continuationToken);
        return ExecutionEngine.executeWithRetry(this, null,
                this.listQueuesSegmentedImpl(prefix, detailsIncluded, maxResults, options, segmentedRequest),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudQueueClient, Void, ResultSegment<CloudQueue>> listQueuesSegmentedImpl(
            final String prefix, final QueueListingDetails detailsIncluded, final Integer maxResults,
            final QueueRequestOptions options, final SegmentedStorageRequest segmentedRequest) {

        Utility.assertContinuationType(segmentedRequest.getToken(), ResultContinuationType.QUEUE);
        final ListingContext listingContext = new ListingContext(prefix, maxResults);
        final StorageRequest<CloudQueueClient, Void, ResultSegment<CloudQueue>> getRequest = new StorageRequest<CloudQueueClient, Void, ResultSegment<CloudQueue>>(
                options, this.getStorageUri()) {

            @Override
            public void setRequestLocationMode() {
                this.setRequestLocationMode(Utility.getListingLocationMode(segmentedRequest.getToken()));
            }

            @Override
            public HttpURLConnection buildRequest(CloudQueueClient client, Void parentObject, OperationContext context)
                    throws Exception {
                listingContext.setMarker(segmentedRequest.getToken() != null ? segmentedRequest.getToken()
                        .getNextMarker() : null);
                return QueueRequest.list(
                        credentials.transformUri(client.getStorageUri().getUri(this.getCurrentLocation())),
                        options, context, listingContext, detailsIncluded);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudQueueClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, -1L, context);
            }

            @Override
            public ResultSegment<CloudQueue> preProcessResponse(Void parentObject, CloudQueueClient client,
                    OperationContext context) throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                }

                return null;
            }

            @Override
            public ResultSegment<CloudQueue> postProcessResponse(HttpURLConnection connection, Void queue,
                    CloudQueueClient client, OperationContext context, ResultSegment<CloudQueue> storageObject)
                    throws Exception {
                final ListResponse<CloudQueue> response = QueueListHandler.getQueues(connection.getInputStream(),
                        client);

                ResultContinuation newToken = null;

                if (response.getNextMarker() != null) {
                    newToken = new ResultContinuation();
                    newToken.setNextMarker(response.getNextMarker());
                    newToken.setContinuationType(ResultContinuationType.QUEUE);
                    newToken.setTargetLocation(this.getResult().getTargetLocation());
                }

                final ResultSegment<CloudQueue> resSegment = new ResultSegment<CloudQueue>(response.getResults(),
                        response.getMaxResults(), newToken);

                // Important for listQueues because this is required by the lazy iterator between executions.
                segmentedRequest.setToken(resSegment.getContinuationToken());
                return resSegment;
            }
        };

        return getRequest;
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
     * Queries the service for the {@link ServiceStats}.
     * 
     * @param options
     *            A {@link QueueRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudQueueClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @return {@link ServiceStats} for the given storage service
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public ServiceStats getServiceStats(QueueRequestOptions options, OperationContext opContext)
            throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = QueueRequestOptions.populateAndApplyDefaults(options, this);

        return ExecutionEngine.executeWithRetry(this, null, this.getServiceStatsImpl(options, false),
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
     *            A {@link QueueRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudQueueClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @return the {@link ServiceProperties} object representing the current configuration of the service.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final ServiceProperties downloadServiceProperties(QueueRequestOptions options, OperationContext opContext)
            throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = QueueRequestOptions.populateAndApplyDefaults(options, this);

        return ExecutionEngine.executeWithRetry(this, null, this.downloadServicePropertiesImpl(options, false),
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
    public void uploadServiceProperties(final ServiceProperties properties, QueueRequestOptions options,
            OperationContext opContext) throws StorageException {
        if (!Utility.isNullOrEmpty(properties.getDefaultServiceVersion())) {
            throw new IllegalArgumentException(SR.DEFAULT_SERVICE_VERSION_ONLY_SET_FOR_BLOB_SERVICE);
        }

        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = QueueRequestOptions.populateAndApplyDefaults(options, this);

        Utility.assertNotNull("properties", properties);

        ExecutionEngine.executeWithRetry(this, null,
                this.uploadServicePropertiesImpl(properties, options, opContext, false),
                options.getRetryPolicyFactory(), opContext);
    }

    /**
     * @return the usePathStyleUris
     */
    @Override
    protected boolean isUsePathStyleUris() {
        return super.isUsePathStyleUris();
    }

    /**
     * Gets the {@link QueueRequestOptions} that is used for requests associated with this <code>CloudQueueClient</code>
     * 
     * @return
     *         The {@link QueueRequestOptions} object containing the values used by this <code>CloudQueueClient</code>
     */
    @Override
    public QueueRequestOptions getDefaultRequestOptions() {
        return this.defaultRequestOptions;
    }

    /**
     * Sets the {@link QueueRequestOptions} that is used for any queue accessed with this <code>CloudQueueClient</code>
     * object.
     * 
     * @param defaultRequestOptions
     *            The QueueRequestOptions to use.
     */
    public void setDefaultRequestOptions(QueueRequestOptions defaultRequestOptions) {
        Utility.assertNotNull("defaultRequestOptions", defaultRequestOptions);
        this.defaultRequestOptions = defaultRequestOptions;
    }
}
