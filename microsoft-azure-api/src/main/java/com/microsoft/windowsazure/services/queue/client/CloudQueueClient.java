package com.microsoft.windowsazure.services.queue.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import javax.xml.stream.XMLStreamException;

import com.microsoft.windowsazure.services.blob.client.CloudBlobClient;
import com.microsoft.windowsazure.services.core.storage.DoesServiceRequest;
import com.microsoft.windowsazure.services.core.storage.OperationContext;
import com.microsoft.windowsazure.services.core.storage.RequestOptions;
import com.microsoft.windowsazure.services.core.storage.ResultContinuation;
import com.microsoft.windowsazure.services.core.storage.ResultContinuationType;
import com.microsoft.windowsazure.services.core.storage.ResultSegment;
import com.microsoft.windowsazure.services.core.storage.ServiceClient;
import com.microsoft.windowsazure.services.core.storage.ServiceProperties;
import com.microsoft.windowsazure.services.core.storage.StorageCredentials;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.ExecutionEngine;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.LazySegmentedIterator;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.ListingContext;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.SegmentedStorageOperation;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.StorageOperation;

/**
 * Provides a client for accessing the Windows Azure Queue service.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 */
public final class CloudQueueClient extends ServiceClient {

    /**
     * Initializes a new instance of the CloudQueueClient class using the specified Queue service endpoint and account
     * credentials.
     * 
     * @param baseUri
     *            The Queue service endpoint to use to create the client
     * @param credentials
     *            The account credentials.
     */
    public CloudQueueClient(final URI baseUri, final StorageCredentials credentials) {
        super(baseUri, credentials);
        this.setTimeoutInMs(QueueConstants.DEFAULT_QUEUE_CLIENT_TIMEOUT_IN_MS);

        if (credentials == null) {
            throw new IllegalArgumentException("StorageCredentials can't be null for the queue service.");
        }
    }

    /**
     * Returns a reference to a {@link CloudQueue} object that represents the cloud queue for the specified address.
     * 
     * @param queueAddress
     *            A <code>String</code> that represents the name of the queue, or the absolute URI to the queue.
     * @return A {@link CloudQueue} object that represents a reference to the cloud queue.
     * 
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudQueue getQueueReference(final String queueAddress) throws URISyntaxException, StorageException {
        Utility.assertNotNullOrEmpty("queueAddress", queueAddress);
        return new CloudQueue(queueAddress, this);
    }

    /**
     * Returns an enumerable collection of queues for this queue service client.
     * 
     * @return An enumerable collection of {@link CloudQueue} objects that represent the queues for this client.
     */
    @DoesServiceRequest
    public Iterable<CloudQueue> listQueues() {
        return this.listQueues(null, QueueListingDetails.NONE, null, null);
    }

    /**
     * Returns an enumerable collection of queues whose names begin with the specified prefix for this Queue service
     * client.
     * 
     * @param prefix
     *            A <code>String</code> that represents the queue name prefix.
     * 
     * @return An enumerable collection of {@link CloudQueue} objects that represent the queues for this client whose
     *         names begin with the specified prefix.
     */
    @DoesServiceRequest
    public Iterable<CloudQueue> listQueues(final String prefix) {
        return this.listQueues(prefix, QueueListingDetails.NONE, null, null);
    }

    /**
     * Returns an enumerable collection of queues whose names begin with the specified prefix for this Queue service
     * client, using the specified details setting, request options, and operation context.
     * 
     * @param prefix
     *            A <code>String</code> that represents the queue name prefix.
     * @param detailsIncluded
     *            A {@link QueueListingDetails} value that indicates whether queue metadata will be returned.
     * @param options
     *            A {@link QueueRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudQueue}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return An enumerable collection of {@link CloudQueue} objects that represents the queues for this client.
     */
    @DoesServiceRequest
    public Iterable<CloudQueue> listQueues(
            final String prefix, final QueueListingDetails detailsIncluded, QueueRequestOptions options,
            OperationContext opContext) {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new QueueRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this);

        final SegmentedStorageOperation<CloudQueueClient, Void, ResultSegment<CloudQueue>> impl =
                new SegmentedStorageOperation<CloudQueueClient, Void, ResultSegment<CloudQueue>>(options, null) {
                    @Override
                    public ResultSegment<CloudQueue> execute(
                            final CloudQueueClient client, final Void dontCare, final OperationContext opContext)
                            throws Exception {

                        final ResultSegment<CloudQueue> result =
                                CloudQueueClient.this.listQueuesCore(prefix,
                                        detailsIncluded,
                                        -1,
                                        this.getToken(),
                                        this.getRequestOptions(),
                                        this,
                                        opContext);

                        this.setToken(result.getContinuationToken());
                        return result;
                    }
                };

        return new LazySegmentedIterator<CloudQueueClient, Void, CloudQueue>(impl, this, null,
                options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Returns a result segment containing a collection of queues.
     * 
     * @param prefix
     *            A <code>String</code> that represents the queue name prefix.
     * @param detailsIncluded
     *            A value that indicates whether to return queue metadata with the listing.
     * @param maxResults
     *            the maximum results to retrieve.
     * @param continuationToken
     *            A continuation token returned by a previous listing operation.
     * @param options
     *            the request options to use for the operation
     * @param taskReference
     *            a reference to the encapsulating task
     * @param opContext
     *            a tracking object for the operation
     * @return a result segment containing a collection of queues whose names begin with the specified prefix.
     * @throws IOException
     * @throws URISyntaxException
     * @throws XMLStreamException
     * @throws InvalidKeyException
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    ResultSegment<CloudQueue> listQueuesCore(
            final String prefix, final QueueListingDetails detailsIncluded, final int maxResults,
            final ResultContinuation continuationToken, final RequestOptions options,
            final StorageOperation<CloudQueueClient, Void, ResultSegment<CloudQueue>> taskReference,
            final OperationContext opContext)
            throws IOException, URISyntaxException, XMLStreamException, InvalidKeyException, StorageException {

        Utility.assertContinuationType(continuationToken, ResultContinuationType.QUEUE);

        final ListingContext listingContext = new ListingContext(prefix, maxResults);
        listingContext.setMarker(continuationToken != null ? continuationToken.getNextMarker() : null);

        final HttpURLConnection listQueueRequest =
                QueueRequest.list(this.getEndpoint(),
                        options.getTimeoutIntervalInMs(),
                        listingContext,
                        detailsIncluded,
                        opContext);

        this.getCredentials().signRequest(listQueueRequest, -1L);

        taskReference.setResult(ExecutionEngine.processRequest(listQueueRequest, opContext));

        if (taskReference.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
            taskReference.setNonExceptionedRetryableFailure(true);
            return null;
        }

        final ListQueuesResponse response = new ListQueuesResponse(listQueueRequest.getInputStream());
        response.parseResponse(this);

        ResultContinuation newToken = null;

        if (response.getNextMarker() != null) {
            newToken = new ResultContinuation();
            newToken.setNextMarker(response.getNextMarker());
            newToken.setContinuationType(ResultContinuationType.QUEUE);
        }

        final ResultSegment<CloudQueue> resSegment =
                new ResultSegment<CloudQueue>(response.getQueues(this), maxResults, newToken);

        return resSegment;
    }

    /**
     * Returns a result segment of an enumerable collection of queues for this Queue service client.
     * 
     * @return A {@link ResultSegment} object that contains a segment of the enumerable collection of {@link CloudQueue}
     *         objects that represent the queues in this collection.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public ResultSegment<CloudQueue> listQueuesSegmented() throws StorageException {
        return this.listQueuesSegmented(null, QueueListingDetails.NONE, 0, null, null, null);
    }

    /**
     * Returns a result segment of an enumerable collection of queues whose names begin with the specified prefix for
     * this Queue service client.
     * 
     * @return A {@link ResultSegment} object that contains a segment of the enumerable collection of {@link CloudQueue}
     *         objects that represent the queues in this collection.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public ResultSegment<CloudQueue> listQueuesSegmented(final String prefix) throws StorageException {
        return this.listQueuesSegmented(prefix, QueueListingDetails.NONE, 0, null, null, null);
    }

    /**
     * Returns a result segment of an enumerable collection of queues whose names begin with the specified prefix for
     * this queue, using the specified listing details options, request options, and operation context.
     * 
     * @param prefix
     *            A <code>String</code> that represents the prefix of the queue name.
     * @param detailsIncluded
     *            A {@link QueueListingDetails} value that indicates whether queue metadata will be returned.
     * @param maxResults
     *            The maximum number of results to retrieve.
     * @param continuationToken
     *            A {@link ResultContinuation} object that represents a continuation token returned by a previous
     *            listing operation.
     * @param options
     *            A {@link QueueRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudQueue}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return A {@link ResultSegment} object that contains a segment of the enumerable collection of {@link CloudQueue}
     *         objects that represent the containers in this container.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public ResultSegment<CloudQueue> listQueuesSegmented(
            final String prefix, final QueueListingDetails detailsIncluded, final int maxResults,
            final ResultContinuation continuationToken, QueueRequestOptions options, OperationContext opContext)
            throws StorageException {

        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new QueueRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this);

        Utility.assertContinuationType(continuationToken, ResultContinuationType.QUEUE);

        final StorageOperation<CloudQueueClient, Void, ResultSegment<CloudQueue>> impl =
                new StorageOperation<CloudQueueClient, Void, ResultSegment<CloudQueue>>(options) {
                    @Override
                    public ResultSegment<CloudQueue> execute(
                            final CloudQueueClient client, final Void dontCare, final OperationContext opContext)
                            throws Exception {
                        return CloudQueueClient.this.listQueuesCore(prefix,
                                detailsIncluded,
                                maxResults,
                                continuationToken,
                                this.getRequestOptions(),
                                this,
                                opContext);
                    }
                };

        return ExecutionEngine.executeWithRetry(this, null, impl, options.getRetryPolicyFactory(), opContext);
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
    @Override
    public void uploadServiceProperties(final ServiceProperties properties) throws StorageException {
        this.uploadServiceProperties(properties, null, null);
    }

    /**
     * Uploads a new configuration to the storage service. This includes Metrics and Logging Configuration.
     * 
     * @param properties
     *            The ServiceProperties to upload.
     * @param options
     *            A {@link RequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}{@link CloudQueueClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    @Override
    public void uploadServiceProperties(
            final ServiceProperties properties, final RequestOptions options, final OperationContext opContext)
            throws StorageException {
        if (!Utility.isNullOrEmpty(properties.getDefaultServiceVersion())) {
            throw new IllegalArgumentException(
                    "DefaultServiceVersion can only be set for the Blob service and the request must be made using the 2011-08-18 version");
        }
        super.uploadServiceProperties(properties, options, opContext);
    }
}
