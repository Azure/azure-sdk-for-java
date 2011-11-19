package com.microsoft.windowsazure.services.queue.client;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;

import com.microsoft.windowsazure.services.core.storage.DoesServiceRequest;
import com.microsoft.windowsazure.services.core.storage.OperationContext;
import com.microsoft.windowsazure.services.core.storage.StorageErrorCodeStrings;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.core.storage.utils.PathUtility;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.BaseResponse;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.ExecutionEngine;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.StorageOperation;

/**
 * Represents a queue in the Windows Azure Queue service.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 */
public final class CloudQueue {

    static CloudQueueMessage getFirstOrNull(final Iterable<CloudQueueMessage> messages) {
        for (final CloudQueueMessage m : messages) {
            return m;
        }

        return null;
    }

    /**
     * Holds the Name of the queue
     */
    private String name;

    /**
     * Holds the URI of the queue
     */
    URI uri;

    /**
     * Holds a reference to the associated service client.
     */
    private CloudQueueClient queueServiceClient;

    /**
     * Holds the Queue Metadata
     */
    HashMap<String, String> metadata;

    /**
     * Holds the Queue ApproximateMessageCount
     */
    long approximateMessageCount;

    /**
     * Uri for the messages.
     */
    private URI messageRequestAddress;

    /**
     * Holds for the flag weather the message should be encoded.
     */
    Boolean shouldEncodeMessage;

    /**
     * Creates an instance of the <code>CloudQueue</code> class using the specified address and client.
     * 
     * @param queueAddress
     *            A <code>String</code> that represents either the absolute URI to the queue, or the queue name.
     * @param client
     *            A {@link CloudQueueClient} object that represents the associated service client, and that specifies
     *            the endpoint for the Queue service.
     * 
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public CloudQueue(final String queueAddress, final CloudQueueClient client) throws URISyntaxException {
        this(PathUtility.appendPathToUri(client.getEndpoint(), queueAddress), client);
    }

    /**
     * Creates an instance of the <code>CloudQueue</code> class using the specified address and client.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that represents the URI of the queue.
     * @param client
     *            A {@link CloudQueueClient} object that represents the associated service client, and that specifies
     *            the endpoint for the Queue service.
     */
    public CloudQueue(final URI uri, final CloudQueueClient client) {
        this.uri = uri;
        this.name = PathUtility.getQueueNameFromUri(uri, client.isUsePathStyleUris());
        this.queueServiceClient = client;
        this.shouldEncodeMessage = true;
    }

    /**
     * Adds a message to the queue.
     * 
     * @param message
     *            A {@link CloudQueueMessage} object that specifies the message to add.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void addMessage(final CloudQueueMessage message) throws StorageException {
        this.addMessage(message, 0, 0, null, null);
    }

    /**
     * Adds a message to the queue.
     * 
     * @param message
     *            A {@link CloudQueueMessage} object that specifies the message to add.
     * 
     * @param timeToLiveInSeconds
     *            The maximum time to allow the message to be in the queue, or null if the service default time is to be
     *            used.
     * 
     * @param initialVisibilityDelayInSeconds
     *            The length of time from now during which the message will be invisible, or null if the message is to
     *            be visible immediately. This must be greater than or equal to zero and less than the value of
     *            timeToLive (if not null).
     * 
     * @param options
     *            A {@link QueueRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudQueueClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void addMessage(
            final CloudQueueMessage message, final int timeToLiveInSeconds, final int initialVisibilityDelayInSeconds,
            QueueRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new QueueRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.queueServiceClient);

        final StorageOperation<CloudQueueClient, CloudQueue, Void> impl =
                new StorageOperation<CloudQueueClient, CloudQueue, Void>(options) {

                    @Override
                    public Void execute(
                            final CloudQueueClient client, final CloudQueue queue, final OperationContext opContext)
                            throws Exception {

                        final HttpURLConnection request =
                                QueueRequest.putMessage(queue.getMessageRequestAddress(),
                                        this.getRequestOptions().getTimeoutIntervalInMs(),
                                        initialVisibilityDelayInSeconds,
                                        timeToLiveInSeconds,
                                        opContext);

                        final byte[] messageBytes =
                                QueueRequest.generateMessageRequestBody(message
                                        .getMessageContentForTransfer(queue.shouldEncodeMessage));

                        client.getCredentials().signRequest(request, messageBytes.length);
                        final OutputStream outStreamRef = request.getOutputStream();
                        outStreamRef.write(messageBytes);

                        this.setResult(ExecutionEngine.processRequest(request, opContext));

                        if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_CREATED) {
                            this.setNonExceptionedRetryableFailure(true);
                            return null;
                        }

                        return null;
                    }
                };

        ExecutionEngine.executeWithRetry(this.queueServiceClient,
                this,
                impl,
                options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Clears all messages from the queue.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void clear() throws StorageException {
        this.clear(null, null);
    }

    /**
     * Clears all messages from the queue, using the specified request options and operation context.
     * 
     * @param options
     *            A {@link QueueRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudQueueClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void clear(QueueRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new QueueRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.queueServiceClient);

        final StorageOperation<CloudQueueClient, CloudQueue, Void> impl =
                new StorageOperation<CloudQueueClient, CloudQueue, Void>(options) {

                    @Override
                    public Void execute(
                            final CloudQueueClient client, final CloudQueue queue, final OperationContext opContext)
                            throws Exception {

                        final HttpURLConnection request =
                                QueueRequest.clearMessages(queue.getMessageRequestAddress(), this.getRequestOptions()
                                        .getTimeoutIntervalInMs(), opContext);

                        client.getCredentials().signRequest(request, -1L);

                        this.setResult(ExecutionEngine.processRequest(request, opContext));

                        if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                            this.setNonExceptionedRetryableFailure(true);
                        }

                        return null;
                    }
                };

        ExecutionEngine.executeWithRetry(this.queueServiceClient,
                this,
                impl,
                options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Creates the queue.
     * 
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     */
    @DoesServiceRequest
    public void create() throws StorageException {
        this.create(null, null);
    }

    /**
     * Creates the queue.
     * 
     * @param options
     *            A {@link QueueRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudQueueClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     */
    @DoesServiceRequest
    public void create(QueueRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new QueueRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.queueServiceClient);

        final StorageOperation<CloudQueueClient, CloudQueue, Void> impl =
                new StorageOperation<CloudQueueClient, CloudQueue, Void>(options) {

                    @Override
                    public Void execute(
                            final CloudQueueClient client, final CloudQueue queue, final OperationContext opContext)
                            throws Exception {
                        final HttpURLConnection request =
                                QueueRequest.create(queue.uri,
                                        this.getRequestOptions().getTimeoutIntervalInMs(),
                                        opContext);

                        QueueRequest.addMetadata(request, queue.metadata, opContext);
                        client.getCredentials().signRequest(request, 0L);

                        this.setResult(ExecutionEngine.processRequest(request, opContext));

                        if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_CREATED
                                && this.getResult().getStatusCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                            this.setNonExceptionedRetryableFailure(true);
                        }

                        return null;
                    }
                };

        ExecutionEngine.executeWithRetry(this.queueServiceClient,
                this,
                impl,
                options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Creates the queue if it does not exist.
     * 
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     */
    @DoesServiceRequest
    public Boolean createIfNotExist() throws StorageException {
        return this.createIfNotExist(null, null);
    }

    /**
     * Creates the queue if it does not exist.
     * 
     * @param options
     *            A {@link QueueRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudQueueClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     */
    @DoesServiceRequest
    public Boolean createIfNotExist(QueueRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new QueueRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.queueServiceClient);

        final StorageOperation<CloudQueueClient, CloudQueue, Boolean> impl =
                new StorageOperation<CloudQueueClient, CloudQueue, Boolean>(options) {

                    @Override
                    public Boolean execute(
                            final CloudQueueClient client, final CloudQueue queue, final OperationContext opContext)
                            throws Exception {
                        final HttpURLConnection request =
                                QueueRequest.create(queue.uri,
                                        this.getRequestOptions().getTimeoutIntervalInMs(),
                                        opContext);

                        QueueRequest.addMetadata(request, queue.metadata, opContext);
                        client.getCredentials().signRequest(request, 0L);

                        this.setResult(ExecutionEngine.processRequest(request, opContext));

                        if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_CREATED) {
                            this.setNonExceptionedRetryableFailure(true);
                            return true;
                        } else if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_CONFLICT) {
                            final StorageException potentialConflictException =
                                    StorageException.translateException(request, null, opContext);

                            if (!potentialConflictException.getExtendedErrorInformation().getErrorCode()
                                    .equals(StorageErrorCodeStrings.QUEUE_ALREADY_EXISTS)) {
                                this.setException(potentialConflictException);
                                this.setNonExceptionedRetryableFailure(true);
                            }
                        }

                        return false;
                    }
                };

        return ExecutionEngine.executeWithRetry(this.queueServiceClient,
                this,
                impl,
                options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Deletes the queue.
     * 
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     */
    @DoesServiceRequest
    public void delete() throws StorageException {
        this.delete(null, null);
    }

    /**
     * Deletes the queue.
     * 
     * @param options
     *            A {@link QueueRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudQueueClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     */
    @DoesServiceRequest
    public void delete(QueueRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new QueueRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.queueServiceClient);

        final StorageOperation<CloudQueueClient, CloudQueue, Void> impl =
                new StorageOperation<CloudQueueClient, CloudQueue, Void>(options) {
                    @Override
                    public Void execute(
                            final CloudQueueClient client, final CloudQueue queue, final OperationContext opContext)
                            throws Exception {
                        final HttpURLConnection request =
                                QueueRequest.delete(queue.uri,
                                        this.getRequestOptions().getTimeoutIntervalInMs(),
                                        opContext);

                        client.getCredentials().signRequest(request, -1L);

                        this.setResult(ExecutionEngine.processRequest(request, opContext));

                        if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                            this.setNonExceptionedRetryableFailure(true);
                        }

                        return null;
                    }
                };

        ExecutionEngine.executeWithRetry(this.queueServiceClient,
                this,
                impl,
                options.getRetryPolicyFactory(),
                opContext);

    }

    /**
     * Deletes the queue if it exists.
     * 
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     */
    @DoesServiceRequest
    public Boolean deleteIfExists() throws StorageException {
        return this.deleteIfExists(null, null);
    }

    /**
     * Deletes the queue if it exists using the specified request options and operation context.
     * 
     * @param options
     *            A {@link QueueRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudQueueClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return <code>true</code> if the queue did not already exist and was created; otherwise, <code>false</code>.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public Boolean deleteIfExists(QueueRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new QueueRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.queueServiceClient);

        final StorageOperation<CloudQueueClient, CloudQueue, Boolean> impl =
                new StorageOperation<CloudQueueClient, CloudQueue, Boolean>(options) {
                    @Override
                    public Boolean execute(
                            final CloudQueueClient client, final CloudQueue queue, final OperationContext opContext)
                            throws Exception {
                        final HttpURLConnection request =
                                QueueRequest.delete(queue.uri,
                                        this.getRequestOptions().getTimeoutIntervalInMs(),
                                        opContext);

                        client.getCredentials().signRequest(request, -1L);

                        this.setResult(ExecutionEngine.processRequest(request, opContext));

                        if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_NO_CONTENT) {
                            return true;
                        } else if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                            return false;
                        } else {
                            this.setNonExceptionedRetryableFailure(true);
                            return false;
                        }
                    }
                };

        return ExecutionEngine.executeWithRetry(this.queueServiceClient,
                this,
                impl,
                options.getRetryPolicyFactory(),
                opContext);

    }

    /**
     * Deletes a message in a queue.
     * 
     * @param message
     *            A {@link CloudQueueMessage} object that specifies the message to delete.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void deleteMessage(final CloudQueueMessage message) throws StorageException {
        this.deleteMessage(message, null, null);
    }

    /**
     * Deletes a message in a queue, using the specified request options and operation context.
     * 
     * @param message
     *            A {@link CloudQueueMessage} object that specifies the message to delete.
     * 
     * @param options
     *            A {@link QueueRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudQueueClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void deleteMessage(final CloudQueueMessage message, QueueRequestOptions options, OperationContext opContext)
            throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new QueueRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.queueServiceClient);

        final String messageId = message.getId();
        final String messagePopReceipt = message.getPopReceipt();

        final StorageOperation<CloudQueueClient, CloudQueue, Void> impl =
                new StorageOperation<CloudQueueClient, CloudQueue, Void>(options) {

                    @Override
                    public Void execute(
                            final CloudQueueClient client, final CloudQueue queue, final OperationContext opContext)
                            throws Exception {

                        final HttpURLConnection request =
                                QueueRequest.deleteMessage(queue.getIndividualMessageAddress(messageId), this
                                        .getRequestOptions().getTimeoutIntervalInMs(), messagePopReceipt, opContext);

                        client.getCredentials().signRequest(request, -1L);

                        this.setResult(ExecutionEngine.processRequest(request, opContext));

                        if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                            this.setNonExceptionedRetryableFailure(true);
                        }

                        return null;
                    }
                };

        ExecutionEngine.executeWithRetry(this.queueServiceClient,
                this,
                impl,
                options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Downloads the queue's metadata and ApproximateMessageCount.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void downloadAttributes() throws StorageException {
        this.downloadAttributes(null, null);
    }

    /**
     * Downloads the queue's metadata and ApproximateMessageCount, using the specified request options and operation
     * context.
     * 
     * @param options
     *            A {@link QueueRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudQueue}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void downloadAttributes(QueueRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new QueueRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.queueServiceClient);

        final StorageOperation<CloudQueueClient, CloudQueue, Void> impl =
                new StorageOperation<CloudQueueClient, CloudQueue, Void>(options) {

                    @Override
                    public Void execute(
                            final CloudQueueClient client, final CloudQueue queue, final OperationContext opContext)
                            throws Exception {
                        final HttpURLConnection request =
                                QueueRequest.downloadAttributes(queue.uri, this.getRequestOptions()
                                        .getTimeoutIntervalInMs(), opContext);

                        client.getCredentials().signRequest(request, -1L);

                        this.setResult(ExecutionEngine.processRequest(request, opContext));

                        if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                            this.setNonExceptionedRetryableFailure(true);
                            return null;
                        }

                        queue.metadata = BaseResponse.getMetadata(request);
                        queue.approximateMessageCount = QueueResponse.getApproximateMessageCount(request);
                        return null;
                    }
                };

        ExecutionEngine.executeWithRetry(this.queueServiceClient,
                this,
                impl,
                options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Returns a value that indicates whether the queue exists.
     * 
     * @return <code>true</code> if the queue exists, otherwise <code>false</code>.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public Boolean exist() throws StorageException {
        return this.exist(null, null);
    }

    /**
     * Returns a value that indicates whether the queue exists, using the specified request options and operation
     * context.
     * 
     * @param options
     *            A {@link QueueRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudQueueClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return <code>true</code> if the queue exists, otherwise <code>false</code>.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public Boolean exist(QueueRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new QueueRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.queueServiceClient);

        final StorageOperation<CloudQueueClient, CloudQueue, Boolean> impl =
                new StorageOperation<CloudQueueClient, CloudQueue, Boolean>(options) {

                    @Override
                    public Boolean execute(
                            final CloudQueueClient client, final CloudQueue queue, final OperationContext opContext)
                            throws Exception {
                        final HttpURLConnection request =
                                QueueRequest.downloadAttributes(queue.uri, this.getRequestOptions()
                                        .getTimeoutIntervalInMs(), opContext);

                        client.getCredentials().signRequest(request, -1L);

                        this.setResult(ExecutionEngine.processRequest(request, opContext));

                        if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_OK) {
                            return Boolean.valueOf(true);
                        } else if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                            return Boolean.valueOf(false);
                        } else {
                            this.setNonExceptionedRetryableFailure(true);
                            // return false instead of null to avoid SCA issues
                            return false;
                        }
                    }
                };

        return ExecutionEngine.executeWithRetry(this.queueServiceClient,
                this,
                impl,
                options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Gets the approximate messages count of the queue.
     * 
     * @return A <code>Long</code> object that represents the approximate messages count of the queue.
     */
    public long getApproximateMessageCount() {
        return this.approximateMessageCount;
    }

    /**
     * Get a single message request(Used internally only).
     * 
     * @return the single message request.
     * 
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    URI getIndividualMessageAddress(final String messageId) throws URISyntaxException {
        return PathUtility.appendPathToUri(this.messageRequestAddress, messageId);
    }

    /**
     * Get the message request base address(Used internally only).
     * 
     * @return the message request.
     * 
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    URI getMessageRequestAddress() throws URISyntaxException {
        if (this.messageRequestAddress == null) {
            this.messageRequestAddress = PathUtility.appendPathToUri(this.uri, QueueConstants.MESSAGES);
        }

        return this.messageRequestAddress;
    }

    /**
     * Returns the metadata for the queue.
     * 
     * @return A <code>java.util.HashMap</code> object that represents the metadata for the queue.
     */
    public HashMap<String, String> getMetadata() {
        return this.metadata;
    }

    /**
     * Gets the name of the queue
     * 
     * @return A <code>String</code> object that represents the name of the queue.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the queue service client associated with this queue.
     * 
     * @return A {@link CloudQueueClient} object that represents the service client associated with this queue.
     */
    public CloudQueueClient getServiceClient() {
        return this.queueServiceClient;
    }

    /**
     * Gets the value indicating whether the message should be encoded.
     * 
     * @return A <code>Boolean</code> that represents whether the message should be encoded.
     */
    public Boolean getShouldEncodeMessage() {
        return this.shouldEncodeMessage;
    }

    /**
     * Gets the uri for this queue.
     * 
     * @return A <code>URI</code> object that represents the uri for this queue.
     */
    public URI getUri() {
        return this.uri;
    }

    /**
     * Peeks a message from the queue.
     * 
     * @return An {@link CloudQueueMessage} object that represents a message in this queue.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public CloudQueueMessage peekMessage() throws StorageException {
        return this.peekMessage(null, null);
    }

    /**
     * Peeks a message from the queue, using the specified request options and operation context.
     * 
     * @param options
     *            A {@link QueueRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudQueueClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return An {@link CloudQueueMessage} object that represents a message in this queue.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public CloudQueueMessage peekMessage(final QueueRequestOptions options, final OperationContext opContext)
            throws StorageException {
        return getFirstOrNull(this.peekMessages(1, null, null));
    }

    /**
     * Peeks a set of messages from the queue.
     * 
     * @param numberOfMessages
     *            The number of messages.
     * 
     * @return An enumerable collection of {@link CloudQueueMessage} objects that represents the messages in this queue.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public Iterable<CloudQueueMessage> peekMessages(final int numberOfMessages) throws StorageException {
        return this.peekMessages(numberOfMessages, null, null);
    }

    /**
     * Peeks a set of messages from the queue, using the specified request options and operation context.
     * 
     * @param numberOfMessages
     *            The number of messages.
     * 
     * @param options
     *            A {@link QueueRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudQueueClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return An enumerable collection of {@link CloudQueueMessage} objects that represents the messages in this queue.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public Iterable<CloudQueueMessage> peekMessages(
            final int numberOfMessages, QueueRequestOptions options, OperationContext opContext)
            throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new QueueRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.queueServiceClient);

        final StorageOperation<CloudQueueClient, CloudQueue, ArrayList<CloudQueueMessage>> impl =
                new StorageOperation<CloudQueueClient, CloudQueue, ArrayList<CloudQueueMessage>>(options) {

                    @Override
                    public ArrayList<CloudQueueMessage> execute(
                            final CloudQueueClient client, final CloudQueue queue, final OperationContext opContext)
                            throws Exception {

                        final HttpURLConnection request =
                                QueueRequest.peekMessages(queue.getMessageRequestAddress(), this.getRequestOptions()
                                        .getTimeoutIntervalInMs(), numberOfMessages, opContext);

                        client.getCredentials().signRequest(request, -1L);

                        this.setResult(ExecutionEngine.processRequest(request, opContext));

                        if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                            this.setNonExceptionedRetryableFailure(true);
                            return null;
                        } else {
                            return QueueDeserializationHelper.readMessages(request.getInputStream(),
                                    queue.shouldEncodeMessage);
                        }
                    }
                };

        return ExecutionEngine.executeWithRetry(this.queueServiceClient,
                this,
                impl,
                options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Retrieves a message from the queue.
     * 
     * @return An {@link CloudQueueMessage} object that represents a message in this queue.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public CloudQueueMessage retrieveMessage() throws StorageException {
        return this.retrieveMessage(0, null, null);
    }

    /**
     * Retrieves a message from the queue, using the specified request options and operation context.
     * 
     * @param visibilityTimeoutInSeconds
     *            Specifies the visibility timeout for the message, in seconds.
     * 
     * @param options
     *            A {@link QueueRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudQueueClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return An {@link CloudQueueMessage} object that represents a message in this queue.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public CloudQueueMessage retrieveMessage(
            final int visibilityTimeoutInSeconds, final QueueRequestOptions options, final OperationContext opContext)
            throws StorageException {
        return getFirstOrNull(this.retrieveMessages(1, visibilityTimeoutInSeconds, null, null));
    }

    /**
     * Retrieves a list of messages from the queue.
     * 
     * @param numberOfMessages
     *            The number of messages.
     * 
     * @return An enumerable collection of {@link CloudQueueMessage} objects that represents the messages in this queue.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public Iterable<CloudQueueMessage> retrieveMessages(final int numberOfMessages) throws StorageException {
        return this.retrieveMessages(numberOfMessages, 0, null, null);
    }

    /**
     * Retrieves a list of messages from the queue, using the specified request options and operation context.
     * 
     * @param numberOfMessages
     *            The number of messages.
     * 
     * @param visibilityTimeoutInSeconds
     *            Specifies the visibility timeout for the message, in seconds.
     * 
     * @param options
     *            A {@link QueueRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudQueueClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return An enumerable collection of {@link CloudQueueMessage} objects that represents the messages in this queue.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public Iterable<CloudQueueMessage> retrieveMessages(
            final int numberOfMessages, final int visibilityTimeoutInSeconds, QueueRequestOptions options,
            OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new QueueRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.queueServiceClient);

        final StorageOperation<CloudQueueClient, CloudQueue, ArrayList<CloudQueueMessage>> impl =
                new StorageOperation<CloudQueueClient, CloudQueue, ArrayList<CloudQueueMessage>>(options) {

                    @Override
                    public ArrayList<CloudQueueMessage> execute(
                            final CloudQueueClient client, final CloudQueue queue, final OperationContext opContext)
                            throws Exception {

                        final HttpURLConnection request =
                                QueueRequest.retrieveMessages(queue.getMessageRequestAddress(),
                                        this.getRequestOptions().getTimeoutIntervalInMs(),
                                        numberOfMessages,
                                        visibilityTimeoutInSeconds,
                                        opContext);

                        client.getCredentials().signRequest(request, -1L);

                        this.setResult(ExecutionEngine.processRequest(request, opContext));

                        if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                            this.setNonExceptionedRetryableFailure(true);
                            return null;
                        } else {
                            return QueueDeserializationHelper.readMessages(request.getInputStream(),
                                    queue.shouldEncodeMessage);
                        }
                    }
                };

        return ExecutionEngine.executeWithRetry(this.queueServiceClient,
                this,
                impl,
                options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Sets the metadata for the queue.
     * 
     * @param metadata
     *            A <code>java.util.HashMap</code> object that represents the metadata being assigned to the queue.
     */
    public void setMetadata(final HashMap<String, String> metadata) {
        this.metadata = metadata;
    }

    /**
     * Sets the name of the queue.
     * 
     * @param name
     *            A <code>String</code> that represents the name being assigned to the queue.
     */
    void setName(final String name) {
        this.name = name;
    }

    /**
     * Updates a message in the queue.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public void updateMessage(final CloudQueueMessage message, final int visibilityTimeoutInSeconds)
            throws StorageException {
        this.updateMessage(message, visibilityTimeoutInSeconds, EnumSet.of(MessageUpdateFields.VISIBILITY), null, null);
    }

    /**
     * Updates a message in the queue, using the specified request options and operation context.
     * 
     * @param visibilityTimeoutInSeconds
     *            Specifies the visibility timeout for the message, in seconds.
     * 
     * @param messageUpdateFields
     *            Specifies which parts of the message are to be updated.
     * 
     * @param options
     *            A {@link QueueRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudQueueClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void updateMessage(
            final CloudQueueMessage message, final int visibilityTimeoutInSeconds,
            final EnumSet<MessageUpdateFields> messageUpdateFields, QueueRequestOptions options,
            OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new QueueRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.queueServiceClient);

        final StorageOperation<CloudQueueClient, CloudQueue, Void> impl =
                new StorageOperation<CloudQueueClient, CloudQueue, Void>(options) {

                    @Override
                    public Void execute(
                            final CloudQueueClient client, final CloudQueue queue, final OperationContext opContext)
                            throws Exception {

                        final HttpURLConnection request =
                                QueueRequest.updateMessage(queue.getIndividualMessageAddress(message.getId()),
                                        this.getRequestOptions().getTimeoutIntervalInMs(),
                                        message.getPopReceipt(),
                                        visibilityTimeoutInSeconds,
                                        opContext);

                        if (messageUpdateFields.contains(MessageUpdateFields.CONTENT)) {
                            final byte[] messageBytes =
                                    QueueRequest.generateMessageRequestBody(message
                                            .getMessageContentForTransfer(queue.shouldEncodeMessage));

                            client.getCredentials().signRequest(request, messageBytes.length);
                            final OutputStream outStreamRef = request.getOutputStream();
                            outStreamRef.write(messageBytes);
                        } else {
                            request.setFixedLengthStreamingMode(0);
                            client.getCredentials().signRequest(request, 0L);
                        }

                        this.setResult(ExecutionEngine.processRequest(request, opContext));

                        if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                            this.setNonExceptionedRetryableFailure(true);
                            return null;
                        }

                        return null;
                    }
                };

        ExecutionEngine.executeWithRetry(this.queueServiceClient,
                this,
                impl,
                options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Uploads the queue's metadata.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void uploadMetadata() throws StorageException {
        this.uploadMetadata(null, null);
    }

    /**
     * Uploads the queue's metadata using the specified request options and operation context.
     * 
     * @param options
     *            A {@link QueueRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudQueueClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void uploadMetadata(QueueRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new QueueRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.queueServiceClient);

        final StorageOperation<CloudQueueClient, CloudQueue, Void> impl =
                new StorageOperation<CloudQueueClient, CloudQueue, Void>(options) {

                    @Override
                    public Void execute(
                            final CloudQueueClient client, final CloudQueue queue, final OperationContext opContext)
                            throws Exception {

                        final HttpURLConnection request =
                                QueueRequest.setMetadata(queue.uri,
                                        this.getRequestOptions().getTimeoutIntervalInMs(),
                                        opContext);

                        QueueRequest.addMetadata(request, queue.metadata, opContext);
                        client.getCredentials().signRequest(request, 0L);

                        this.setResult(ExecutionEngine.processRequest(request, opContext));

                        if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                            this.setNonExceptionedRetryableFailure(true);
                        }

                        return null;
                    }
                };

        ExecutionEngine.executeWithRetry(this.queueServiceClient,
                this,
                impl,
                options.getRetryPolicyFactory(),
                opContext);

    }
}
