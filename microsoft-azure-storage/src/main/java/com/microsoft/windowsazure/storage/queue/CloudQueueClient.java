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

package com.microsoft.windowsazure.storage.queue;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

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
import com.microsoft.windowsazure.storage.core.ListingContext;
import com.microsoft.windowsazure.storage.core.SR;
import com.microsoft.windowsazure.storage.core.SegmentedStorageRequest;
import com.microsoft.windowsazure.storage.core.StorageRequest;
import com.microsoft.windowsazure.storage.core.Utility;

/**
 * Provides a service client for accessing the Windows Azure Queue service.
 */
public final class CloudQueueClient extends ServiceClient {

    /**
     * Initializes a new instance of the <code>CloudQueueClient</code> class
     * using the specified Queue service endpoint and account credentials.
     * 
     * @param baseUri
     *            The Queue service endpoint to use to create the client
     * @param credentials
     *            The account credentials.
     */
    public CloudQueueClient(final URI baseUri, final StorageCredentials credentials) {
        this(new StorageUri(baseUri, null), credentials);
    }

    /**
     * Initializes a new instance of the <code>CloudQueueClient</code> class
     * using the specified Queue service endpoint and account credentials.
     * 
     * @param baseUri
     *            The Queue service endpoint to use to create the client
     * @param credentials
     *            The account credentials.
     */
    public CloudQueueClient(final StorageUri baseUri, final StorageCredentials credentials) {
        super(baseUri, credentials);
        this.setTimeoutInMs(QueueConstants.DEFAULT_QUEUE_CLIENT_TIMEOUT_IN_MS);

        if (credentials == null) {
            throw new IllegalArgumentException(SR.STORAGE_CREDENTIALS_NULL);
        }
    }

    /**
     * Gets a {@link CloudQueue} object that represents the storage service
     * queue for the specified address.
     * 
     * @param queueName
     *            A <code>String</code> that represents the name of the queue.
     * 
     * @return A {@link CloudQueue} object that represents a reference to the
     *         queue.
     * 
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    public CloudQueue getQueueReference(final String queueName) throws URISyntaxException, StorageException {
        return new CloudQueue(queueName, this);
    }

    /**
     * Gets an iterable collection of queues for this queue service client.
     * 
     * @return An iterable collection of {@link CloudQueue} objects retrieved lazily that
     *         represent the queues in this client.
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public Iterable<CloudQueue> listQueues() throws StorageException {
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
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public Iterable<CloudQueue> listQueues(final String prefix) throws StorageException {
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
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public Iterable<CloudQueue> listQueues(final String prefix, final QueueListingDetails detailsIncluded,
            QueueRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = QueueRequestOptions.applyDefaults(options, this);

        SegmentedStorageRequest segmentedRequest = new SegmentedStorageRequest();
        return new LazySegmentedIterable<CloudQueueClient, Void, CloudQueue>(this.listQueuesSegmentedImpl(prefix,
                detailsIncluded, -1, options, segmentedRequest), this, null, options.getRetryPolicyFactory(), opContext);
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
        return this.listQueuesSegmented(null, QueueListingDetails.NONE, 0, null, null, null);
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
        return this.listQueuesSegmented(prefix, QueueListingDetails.NONE, 0, null, null, null);
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
     *            The maximum number of queue results to retrieve.
     * @param continuationToken
     *            A {@link ResultContinuation} object that represents a
     *            continuation token returned by a previous listing operation.
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
     * @return A {@link ResultSegment} of {@link CloudQueue} objects that
     *         contains a segment of the iterable collection of {@link CloudQueue} objects that represent the requested
     *         queues in
     *         the storage service.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public ResultSegment<CloudQueue> listQueuesSegmented(final String prefix,
            final QueueListingDetails detailsIncluded, final int maxResults,
            final ResultContinuation continuationToken, QueueRequestOptions options, OperationContext opContext)
            throws StorageException {

        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = QueueRequestOptions.applyDefaults(options, this);

        SegmentedStorageRequest segmentedRequest = new SegmentedStorageRequest();
        segmentedRequest.setToken(continuationToken);
        return ExecutionEngine.executeWithRetry(this, null,
                this.listQueuesSegmentedImpl(prefix, detailsIncluded, maxResults, options, segmentedRequest),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudQueueClient, Void, ResultSegment<CloudQueue>> listQueuesSegmentedImpl(
            final String prefix, final QueueListingDetails detailsIncluded, final int maxResults,
            final QueueRequestOptions options, final SegmentedStorageRequest segmentedRequest) throws StorageException {

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
                return QueueRequest.list(client.getStorageUri().getUri(this.getCurrentLocation()),
                        options.getTimeoutIntervalInMs(), listingContext, detailsIncluded, context);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudQueueClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobAndQueueRequest(connection, client, -1L, null);
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
                final ListQueuesResponse response = new ListQueuesResponse(connection.getInputStream());
                response.parseResponse(client);

                ResultContinuation newToken = null;

                if (response.getNextMarker() != null) {
                    newToken = new ResultContinuation();
                    newToken.setNextMarker(response.getNextMarker());
                    newToken.setContinuationType(ResultContinuationType.QUEUE);
                    newToken.setTargetLocation(this.getResult().getTargetLocation());
                }

                final ResultSegment<CloudQueue> resSegment = new ResultSegment<CloudQueue>(response.getQueues(client),
                        maxResults, newToken);

                // Important for listQueues because this is required by the lazy iterator between executions.
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
     *            A {@link QueueRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudQueueClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @return ServiceStats for the given storage service
     * @throws StorageException
     */
    @DoesServiceRequest
    public ServiceStats getServiceStats(QueueRequestOptions options, OperationContext opContext)
            throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = QueueRequestOptions.applyDefaults(options, this);

        return ExecutionEngine.executeWithRetry(this, null, this.getServiceStatsImpl(options, false),
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
     *            A {@link QueueRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudQueueClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @return the ServiceProperties object representing the current configuration of the service.
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
        options = QueueRequestOptions.applyDefaults(options, this);

        return ExecutionEngine.executeWithRetry(this, null, this.downloadServicePropertiesImpl(options, false),
                options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Uploads a new configuration of service properties to the storage service,
     * using the default request options. This includes Metrics and Logging
     * Configuration.
     * 
     * @param properties
     *            The {@link ServiceProperties} to upload.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public void uploadServiceProperties(final ServiceProperties properties) throws StorageException {
        this.uploadServiceProperties(properties, null /* options */, null /* opContext */);
    }

    /**
     * Uploads a new configuration of service properties to the storage service,
     * using the specified request options and operation context. This includes
     * Metrics and Logging Configuration.
     * 
     * @param properties
     *            The {@link ServiceProperties} to upload.
     * @param options
     *            A {@link QueueRequestOptions} object that specifies any additional
     *            options for the request. Specifying <code>null</code> will use
     *            the default request options from the associated service client
     *            ({@link CloudQueueClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context
     *            for the current operation. This object is used to track
     *            requests to the storage service, and to provide additional
     *            runtime information about the operation.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
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
        options = QueueRequestOptions.applyDefaults(options, this);

        Utility.assertNotNull("properties", properties);

        ExecutionEngine.executeWithRetry(this, null,
                this.uploadServicePropertiesImpl(properties, options, opContext, false),
                options.getRetryPolicyFactory(), opContext);
    }
}
