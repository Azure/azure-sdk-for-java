/**
 * Copyright 2011 Microsoft Corporation
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

package com.microsoft.windowsazure.services.queue.client;

import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;

import com.microsoft.windowsazure.services.blob.core.storage.SharedAccessSignatureHelper;
import com.microsoft.windowsazure.services.core.storage.DoesServiceRequest;
import com.microsoft.windowsazure.services.core.storage.OperationContext;
import com.microsoft.windowsazure.services.core.storage.StorageCredentialsSharedAccessSignature;
import com.microsoft.windowsazure.services.core.storage.StorageErrorCodeStrings;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.core.storage.StorageExtendedErrorInformation;
import com.microsoft.windowsazure.services.core.storage.utils.PathUtility;
import com.microsoft.windowsazure.services.core.storage.utils.UriQueryBuilder;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.BaseResponse;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.ExecutionEngine;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.StorageOperation;

/**
 * This class represents a queue in the Windows Azure Queue service.
 */
public final class CloudQueue {

    /**
     * Gets the first message from a list of queue messages, if any.
     * 
     * @param messages
     *            The <code>Iterable</code> collection of {@link CloudQueueMessage} objects to get the first message
     *            from.
     * 
     * @return The first {@link CloudQueueMessage} from the list of queue messages, or <code>null</code> if the list is
     *         empty.
     */
    static CloudQueueMessage getFirstOrNull(final Iterable<CloudQueueMessage> messages) {
        for (final CloudQueueMessage m : messages) {
            return m;
        }

        return null;
    }

    /**
     * The name of the queue.
     */
    private String name;

    /**
     * The absolute <code>URI</code> of the queue.
     */
    URI uri;

    /**
     * A reference to the queue's associated service client.
     */
    CloudQueueClient queueServiceClient;

    /**
     * The queue's Metadata collection.
     */
    HashMap<String, String> metadata;

    /**
     * The queue's approximate message count, as reported by the server.
     */
    long approximateMessageCount;

    /**
     * The <code for the messages of the queue.
     */
    private URI messageRequestAddress;

    /**
     * A flag indicating whether or not the message should be encoded in base-64.
     */
    boolean shouldEncodeMessage;

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
     * @throws StorageException
     */
    public CloudQueue(final String queueAddress, final CloudQueueClient client) throws URISyntaxException,
            StorageException {
        this(PathUtility.appendPathToUri(client.getEndpoint(), queueAddress), client);
    }

    /**
     * Creates an instance of the <code>CloudQueue</code> class using the specified queue URI and client.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that represents the absolute URI of the queue.
     * @param client
     *            A {@link CloudQueueClient} object that represents the associated service client, and that specifies
     *            the endpoint for the Queue service.
     * @throws StorageException
     * @throws URISyntaxException
     */
    public CloudQueue(final URI uri, final CloudQueueClient client) throws URISyntaxException, StorageException {
        this.uri = uri;
        this.name = PathUtility.getQueueNameFromUri(uri, client.isUsePathStyleUris());
        this.queueServiceClient = client;
        this.shouldEncodeMessage = true;
        this.parseQueryAndVerify(this.uri, client, client.isUsePathStyleUris());
    }

    /**
     * Adds a message to the back of the queue with the default options.
     * 
     * @param message
     *            A {@link CloudQueueMessage} object that specifies the message to add.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public void addMessage(final CloudQueueMessage message) throws StorageException {
        this.addMessage(message, 0, 0, null, null);
    }

    /**
     * Adds a message to the back of the queue with the specified options.
     * 
     * @param message
     *            A {@link CloudQueueMessage} object that specifies the message to add.
     * 
     * @param timeToLiveInSeconds
     *            The maximum time to allow the message to be in the queue. A value of zero will set the time-to-live to
     *            the service default value of seven days.
     * 
     * @param initialVisibilityDelayInSeconds
     *            The length of time during which the message will be invisible, starting when it is added to the queue,
     *            or 0 to make the message visible immediately. This value must be greater than or equal to zero and
     *            less than or equal to the time-to-live value.
     * 
     * @param options
     *            A {@link QueueRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudQueueClient}).
     * 
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public void addMessage(final CloudQueueMessage message, final int timeToLiveInSeconds,
            final int initialVisibilityDelayInSeconds, QueueRequestOptions options, OperationContext opContext)
            throws StorageException {
        Utility.assertNotNull("message", message);
        Utility.assertNotNull("messageContent", message.getMessageContentAsByte());
        Utility.assertInBounds("timeToLiveInSeconds", timeToLiveInSeconds, 0,
                QueueConstants.MAX_TIME_TO_LIVE_IN_SECONDS);

        final int realTimeToLiveInSeconds = timeToLiveInSeconds == 0 ? QueueConstants.MAX_TIME_TO_LIVE_IN_SECONDS
                : timeToLiveInSeconds;
        Utility.assertInBounds("initialVisibilityDelayInSeconds", initialVisibilityDelayInSeconds, 0,
                realTimeToLiveInSeconds - 1);

        final String stringToSend = message.getMessageContentForTransfer(this.shouldEncodeMessage);

        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new QueueRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.queueServiceClient);

        final StorageOperation<CloudQueueClient, CloudQueue, Void> impl = new StorageOperation<CloudQueueClient, CloudQueue, Void>(
                options) {

            @Override
            public Void execute(final CloudQueueClient client, final CloudQueue queue, final OperationContext opContext)
                    throws Exception {

                final HttpURLConnection request = QueueRequest.putMessage(queue.getMessageRequestAddress(opContext),
                        this.getRequestOptions().getTimeoutIntervalInMs(), initialVisibilityDelayInSeconds,
                        timeToLiveInSeconds, opContext);

                final byte[] messageBytes = QueueRequest.generateMessageRequestBody(stringToSend);

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

        ExecutionEngine.executeWithRetry(this.queueServiceClient, this, impl, options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Clears all messages from the queue, using the default request options.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
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
     *             If a storage service error occurred during the operation.
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

        final StorageOperation<CloudQueueClient, CloudQueue, Void> impl = new StorageOperation<CloudQueueClient, CloudQueue, Void>(
                options) {

            @Override
            public Void execute(final CloudQueueClient client, final CloudQueue queue, final OperationContext opContext)
                    throws Exception {

                final HttpURLConnection request = QueueRequest.clearMessages(queue.getMessageRequestAddress(opContext),
                        this.getRequestOptions().getTimeoutIntervalInMs(), opContext);

                client.getCredentials().signRequest(request, -1L);

                this.setResult(ExecutionEngine.processRequest(request, opContext));

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                    this.setNonExceptionedRetryableFailure(true);
                }

                return null;
            }
        };

        ExecutionEngine.executeWithRetry(this.queueServiceClient, this, impl, options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Creates the queue in the storage service with default request options.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public void create() throws StorageException {
        this.create(null, null);
    }

    /**
     * Creates the queue in the storage service using the specified request options and operation context.
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
     *             If a storage service error occurred during the operation.
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

        final StorageOperation<CloudQueueClient, CloudQueue, Void> impl = new StorageOperation<CloudQueueClient, CloudQueue, Void>(
                options) {

            @Override
            public Void execute(final CloudQueueClient client, final CloudQueue queue, final OperationContext opContext)
                    throws Exception {
                final HttpURLConnection request = QueueRequest.create(queue.getTransformedAddress(opContext), this
                        .getRequestOptions().getTimeoutIntervalInMs(), opContext);

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

        ExecutionEngine.executeWithRetry(this.queueServiceClient, this, impl, options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Creates the queue in the storage service using default request options if it does not already exist.
     * 
     * @return A value of <code>true</code> if the queue is created in the storage service, otherwise <code>false</code>
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
     * Creates the queue in the storage service with the specified request options and operation context if it does not
     * already exist.
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
     * @return A value of <code>true</code> if the queue is created in the storage service, otherwise <code>false</code>
     *         .
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public boolean createIfNotExist(QueueRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new QueueRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.queueServiceClient);

        final StorageOperation<CloudQueueClient, CloudQueue, Boolean> impl = new StorageOperation<CloudQueueClient, CloudQueue, Boolean>(
                options) {

            @Override
            public Boolean execute(final CloudQueueClient client, final CloudQueue queue,
                    final OperationContext opContext) throws Exception {
                final HttpURLConnection request = QueueRequest.create(queue.getTransformedAddress(opContext), this
                        .getRequestOptions().getTimeoutIntervalInMs(), opContext);

                QueueRequest.addMetadata(request, queue.metadata, opContext);
                client.getCredentials().signRequest(request, 0L);

                this.setResult(ExecutionEngine.processRequest(request, opContext));

                if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_CREATED) {
                    return true;
                }
                else if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_NO_CONTENT) {
                    return false;
                }
                else if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_CONFLICT) {
                    final StorageException potentialConflictException = StorageException.translateException(request,
                            null, opContext);

                    StorageExtendedErrorInformation extendedInfo = potentialConflictException
                            .getExtendedErrorInformation();
                    if (extendedInfo == null) {
                        // If we cant validate the error then the error must be surfaced to the user.
                        throw potentialConflictException;
                    }

                    if (!extendedInfo.getErrorCode().equals(StorageErrorCodeStrings.QUEUE_ALREADY_EXISTS)) {
                        this.setException(potentialConflictException);
                        this.setNonExceptionedRetryableFailure(true);
                    }
                }
                else {
                    this.setNonExceptionedRetryableFailure(true);
                }

                return false;
            }
        };

        return ExecutionEngine.executeWithRetry(this.queueServiceClient, this, impl, options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Deletes the queue from the storage service.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public void delete() throws StorageException {
        this.delete(null, null);
    }

    /**
     * Deletes the queue from the storage service, using the specified request options and operation context.
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
     *             If a storage service error occurred during the operation.
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

        final StorageOperation<CloudQueueClient, CloudQueue, Void> impl = new StorageOperation<CloudQueueClient, CloudQueue, Void>(
                options) {
            @Override
            public Void execute(final CloudQueueClient client, final CloudQueue queue, final OperationContext opContext)
                    throws Exception {
                final HttpURLConnection request = QueueRequest.delete(queue.getTransformedAddress(opContext), this
                        .getRequestOptions().getTimeoutIntervalInMs(), opContext);

                client.getCredentials().signRequest(request, -1L);

                this.setResult(ExecutionEngine.processRequest(request, opContext));

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                    this.setNonExceptionedRetryableFailure(true);
                }

                return null;
            }
        };

        ExecutionEngine.executeWithRetry(this.queueServiceClient, this, impl, options.getRetryPolicyFactory(),
                opContext);

    }

    /**
     * Deletes the queue from the storage service if it exists.
     * 
     * @return A value of <code>true</code> if the queue existed in the storage service and has been deleted, otherwise
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
     * Deletes the queue from the storage service using the specified request options and operation context, if it
     * exists.
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
     * @return A value of <code>true</code> if the queue existed in the storage service and has been deleted, otherwise
     *         <code>false</code>.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public boolean deleteIfExists(QueueRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new QueueRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.queueServiceClient);

        final StorageOperation<CloudQueueClient, CloudQueue, Boolean> impl = new StorageOperation<CloudQueueClient, CloudQueue, Boolean>(
                options) {
            @Override
            public Boolean execute(final CloudQueueClient client, final CloudQueue queue,
                    final OperationContext opContext) throws Exception {
                final HttpURLConnection request = QueueRequest.delete(queue.getTransformedAddress(opContext), this
                        .getRequestOptions().getTimeoutIntervalInMs(), opContext);

                client.getCredentials().signRequest(request, -1L);

                this.setResult(ExecutionEngine.processRequest(request, opContext));

                if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_NO_CONTENT) {
                    return true;
                }
                else if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                    return false;
                }
                else {
                    this.setNonExceptionedRetryableFailure(true);
                    return false;
                }
            }
        };

        return ExecutionEngine.executeWithRetry(this.queueServiceClient, this, impl, options.getRetryPolicyFactory(),
                opContext);

    }

    /**
     * Deletes the specified message from the queue.
     * 
     * @param message
     *            A {@link CloudQueueMessage} object that specifies the message to delete.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public void deleteMessage(final CloudQueueMessage message) throws StorageException {
        this.deleteMessage(message, null, null);
    }

    /**
     * Deletes the specified message from the queue, using the specified request options and operation context.
     * 
     * @param message
     *            A {@link CloudQueueMessage} object that specifies the message to delete.
     * 
     * @param options
     *            A {@link QueueRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudQueueClient}).
     * 
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public void deleteMessage(final CloudQueueMessage message, QueueRequestOptions options, OperationContext opContext)
            throws StorageException {
        Utility.assertNotNull("message", message);
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

        final StorageOperation<CloudQueueClient, CloudQueue, Void> impl = new StorageOperation<CloudQueueClient, CloudQueue, Void>(
                options) {

            @Override
            public Void execute(final CloudQueueClient client, final CloudQueue queue, final OperationContext opContext)
                    throws Exception {

                final HttpURLConnection request = QueueRequest.deleteMessage(queue.getIndividualMessageAddress(
                        messageId, opContext), this.getRequestOptions().getTimeoutIntervalInMs(), messagePopReceipt,
                        opContext);

                client.getCredentials().signRequest(request, -1L);

                this.setResult(ExecutionEngine.processRequest(request, opContext));

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                    this.setNonExceptionedRetryableFailure(true);
                }

                return null;
            }
        };

        ExecutionEngine.executeWithRetry(this.queueServiceClient, this, impl, options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Downloads the queue's metadata and approximate message count value.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public void downloadAttributes() throws StorageException {
        this.downloadAttributes(null, null);
    }

    /**
     * Downloads the queue's metadata and approximate message count value, using the specified request options and
     * operation context.
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
     *             If a storage service error occurred during the operation.
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

        final StorageOperation<CloudQueueClient, CloudQueue, Void> impl = new StorageOperation<CloudQueueClient, CloudQueue, Void>(
                options) {

            @Override
            public Void execute(final CloudQueueClient client, final CloudQueue queue, final OperationContext opContext)
                    throws Exception {
                final HttpURLConnection request = QueueRequest.downloadAttributes(
                        queue.getTransformedAddress(opContext), this.getRequestOptions().getTimeoutIntervalInMs(),
                        opContext);

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

        ExecutionEngine.executeWithRetry(this.queueServiceClient, this, impl, options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Returns a value that indicates whether the queue exists in the storage service.
     * 
     * @return <code>true</code> if the queue exists in the storage service, otherwise <code>false</code>.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public boolean exists() throws StorageException {
        return this.exists(null, null);
    }

    /**
     * Returns a value that indicates whether the queue exists in the storage service, using the specified request
     * options and operation context.
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
     * @return <code>true</code> if the queue exists in the storage service, otherwise <code>false</code>.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public boolean exists(QueueRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new QueueRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.queueServiceClient);

        final StorageOperation<CloudQueueClient, CloudQueue, Boolean> impl = new StorageOperation<CloudQueueClient, CloudQueue, Boolean>(
                options) {

            @Override
            public Boolean execute(final CloudQueueClient client, final CloudQueue queue,
                    final OperationContext opContext) throws Exception {
                final HttpURLConnection request = QueueRequest.downloadAttributes(
                        queue.getTransformedAddress(opContext), this.getRequestOptions().getTimeoutIntervalInMs(),
                        opContext);

                client.getCredentials().signRequest(request, -1L);

                this.setResult(ExecutionEngine.processRequest(request, opContext));

                if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_OK) {
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

        return ExecutionEngine.executeWithRetry(this.queueServiceClient, this, impl, options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Gets the approximate messages count of the queue. This value is initialized by a request to
     * {@link #downloadAttributes} and represents the approximate message count when that request completed.
     * 
     * @return A <code>Long</code> object that represents the approximate messages count of the queue.
     */
    public long getApproximateMessageCount() {
        return this.approximateMessageCount;
    }

    /**
     * Get a single message request (Used internally only).
     * 
     * @return The <code>URI</code> for a single message request.
     * 
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     * @throws StorageException
     */
    URI getIndividualMessageAddress(final String messageId, final OperationContext opContext)
            throws URISyntaxException, StorageException {
        return PathUtility.appendPathToUri(this.getMessageRequestAddress(opContext), messageId);
    }

    /**
     * Get the message request base address (Used internally only).
     * 
     * @return The message request <code>URI</code>.
     * 
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     * @throws StorageException
     */
    URI getMessageRequestAddress(final OperationContext opContext) throws URISyntaxException, StorageException {
        if (this.messageRequestAddress == null) {
            this.messageRequestAddress = PathUtility.appendPathToUri(this.getTransformedAddress(opContext),
                    QueueConstants.MESSAGES);
        }

        return this.messageRequestAddress;
    }

    /**
     * Gets the metadata collection for the queue as stored in this <code>CloudQueue</code> object. This value is
     * initialized with the metadata from the queue by a call to {@link #downloadAttributes}, and is set on the queue
     * with a call to {@link #uploadMetadata}.
     * 
     * @return A <code>java.util.HashMap</code> object that represents the metadata for the queue.
     */
    public HashMap<String, String> getMetadata() {
        return this.metadata;
    }

    /**
     * Gets the name of the queue.
     * 
     * @return A <code>String</code> object that represents the name of the queue.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the queue service client associated with this queue.
     * 
     * @return A {@link CloudQueueClient} object that represents the service client associated with this queue.
     */
    public CloudQueueClient getServiceClient() {
        return this.queueServiceClient;
    }

    /**
     * Gets the value indicating whether the message should be base-64 encoded.
     * 
     * @return A <code>Boolean</code> that represents whether the message should be base-64 encoded.
     */
    public boolean getShouldEncodeMessage() {
        return this.shouldEncodeMessage;
    }

    /**
     * Gets the absolute URI for this queue.
     * 
     * @return A <code>java.net.URI</code> object that represents the URI for this queue.
     */
    public URI getUri() {
        return this.uri;
    }

    /**
     * Peeks a message from the queue. A peek request retrieves a message from the front of the queue without changing
     * its visibility.
     * 
     * @return An {@link CloudQueueMessage} object that represents a message in this queue.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public CloudQueueMessage peekMessage() throws StorageException {
        return this.peekMessage(null, null);
    }

    /**
     * Peeks a message from the queue, using the specified request options and operation context. A peek request
     * retrieves a message from the front of the queue without changing its visibility.
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
     * @return An {@link CloudQueueMessage} object that represents the requested message from the queue.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public CloudQueueMessage peekMessage(final QueueRequestOptions options, final OperationContext opContext)
            throws StorageException {
        return getFirstOrNull(this.peekMessages(1, null, null));
    }

    /**
     * Peeks a specified number of messages from the queue. A peek request retrieves messages from the front of the
     * queue without changing their visibility.
     * 
     * @param numberOfMessages
     *            The number of messages to retrieve.
     * 
     * @return An enumerable collection of {@link CloudQueueMessage} objects that represents the requested messages from
     *         the queue.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public Iterable<CloudQueueMessage> peekMessages(final int numberOfMessages) throws StorageException {
        return this.peekMessages(numberOfMessages, null, null);
    }

    /**
     * Peeks a set of messages from the queue, using the specified request options and operation context. A peek request
     * retrieves messages from the front of the queue without changing their visibility.
     * 
     * @param numberOfMessages
     *            The number of messages to retrieve.
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
     * @return An enumerable collection of {@link CloudQueueMessage} objects that represents the requested messages from
     *         the queue.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public Iterable<CloudQueueMessage> peekMessages(final int numberOfMessages, QueueRequestOptions options,
            OperationContext opContext) throws StorageException {
        Utility.assertInBounds("numberOfMessages", numberOfMessages, 1, QueueConstants.MAX_NUMBER_OF_MESSAGES_TO_PEEK);

        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new QueueRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.queueServiceClient);

        final StorageOperation<CloudQueueClient, CloudQueue, ArrayList<CloudQueueMessage>> impl = new StorageOperation<CloudQueueClient, CloudQueue, ArrayList<CloudQueueMessage>>(
                options) {

            @Override
            public ArrayList<CloudQueueMessage> execute(final CloudQueueClient client, final CloudQueue queue,
                    final OperationContext opContext) throws Exception {

                final HttpURLConnection request = QueueRequest.peekMessages(queue.getMessageRequestAddress(opContext),
                        this.getRequestOptions().getTimeoutIntervalInMs(), numberOfMessages, opContext);

                client.getCredentials().signRequest(request, -1L);

                this.setResult(ExecutionEngine.processRequest(request, opContext));

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }
                else {
                    return QueueDeserializationHelper.readMessages(request.getInputStream(), queue.shouldEncodeMessage);
                }
            }
        };

        return ExecutionEngine.executeWithRetry(this.queueServiceClient, this, impl, options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Retrieves a message from the front of the queue using the default request options. This operation marks the
     * retrieved message as invisible in the queue for the default visibility timeout period.
     * 
     * @return An {@link CloudQueueMessage} object that represents a message in this queue.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public CloudQueueMessage retrieveMessage() throws StorageException {
        return this.retrieveMessage(QueueConstants.DEFAULT_VISIBILITY_MESSAGE_TIMEOUT_IN_SECONDS, null, null);
    }

    /**
     * Retrieves a message from the front of the queue, using the specified request options and operation context. This
     * operation marks the retrieved message as invisible in the queue for the specified visibility timeout period.
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
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public CloudQueueMessage retrieveMessage(final int visibilityTimeoutInSeconds, final QueueRequestOptions options,
            final OperationContext opContext) throws StorageException {
        return getFirstOrNull(this.retrieveMessages(1, visibilityTimeoutInSeconds, null, null));
    }

    /**
     * Retrieves the specified number of messages from the front of the queue using the default request options. This
     * operation marks the retrieved messages as invisible in the queue for the default visibility timeout period.
     * 
     * @param numberOfMessages
     *            The number of messages to retrieve.
     * 
     * @return An enumerable collection of {@link CloudQueueMessage} objects that represents the retrieved messages from
     *         the queue.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public Iterable<CloudQueueMessage> retrieveMessages(final int numberOfMessages) throws StorageException {
        return this.retrieveMessages(numberOfMessages, QueueConstants.DEFAULT_VISIBILITY_MESSAGE_TIMEOUT_IN_SECONDS,
                null, null);
    }

    /**
     * Retrieves the specified number of messages from the front of the queue using the specified request options and
     * operation context. This operation marks the retrieved messages as invisible in the queue for the default
     * visibility timeout period.
     * 
     * @param numberOfMessages
     *            The number of messages to retrieve.
     * 
     * @param visibilityTimeoutInSeconds
     *            Specifies the visibility timeout for the retrieved messages, in seconds.
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
     * @return An enumerable collection of {@link CloudQueueMessage} objects that represents the messages retrieved from
     *         the queue.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public Iterable<CloudQueueMessage> retrieveMessages(final int numberOfMessages,
            final int visibilityTimeoutInSeconds, QueueRequestOptions options, OperationContext opContext)
            throws StorageException {
        Utility.assertInBounds("numberOfMessages", numberOfMessages, 1, QueueConstants.MAX_NUMBER_OF_MESSAGES_TO_PEEK);
        Utility.assertInBounds("visibilityTimeoutInSeconds", visibilityTimeoutInSeconds, 0,
                QueueConstants.MAX_TIME_TO_LIVE_IN_SECONDS);

        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new QueueRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.queueServiceClient);

        final StorageOperation<CloudQueueClient, CloudQueue, ArrayList<CloudQueueMessage>> impl = new StorageOperation<CloudQueueClient, CloudQueue, ArrayList<CloudQueueMessage>>(
                options) {

            @Override
            public ArrayList<CloudQueueMessage> execute(final CloudQueueClient client, final CloudQueue queue,
                    final OperationContext opContext) throws Exception {

                final HttpURLConnection request = QueueRequest.retrieveMessages(
                        queue.getMessageRequestAddress(opContext), this.getRequestOptions().getTimeoutIntervalInMs(),
                        numberOfMessages, visibilityTimeoutInSeconds, opContext);

                client.getCredentials().signRequest(request, -1L);

                this.setResult(ExecutionEngine.processRequest(request, opContext));

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }
                else {
                    return QueueDeserializationHelper.readMessages(request.getInputStream(), queue.shouldEncodeMessage);
                }
            }
        };

        return ExecutionEngine.executeWithRetry(this.queueServiceClient, this, impl, options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Sets the metadata collection of name-value pairs to be set on the queue with an {@link #uploadMetadata} call.
     * This collection will overwrite any existing queue metadata. If this is set to an empty collection, the queue
     * metadata will be cleared on an {@link #uploadMetadata} call.
     * 
     * @param metadata
     *            A <code>java.util.HashMap</code> object that represents the metadata being assigned to the queue.
     */
    public void setMetadata(final HashMap<String, String> metadata) {
        this.metadata = metadata;
    }

    /**
     * Sets the flag indicating whether the message should be base-64 encoded.
     * 
     * @param shouldEncodeMessage
     *            The value indicates whether the message should be base-64 encoded.
     */
    public void setShouldEncodeMessage(final boolean shouldEncodeMessage) {
        this.shouldEncodeMessage = shouldEncodeMessage;
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
     * Updates the specified message in the queue with a new visibility timeout value in seconds.
     * 
     * @param message
     *            The {@link CloudQueueMessage} to update in the queue.
     * 
     * @param visibilityTimeoutInSeconds
     *            Specifies the new visibility timeout for the message, in seconds.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    public void updateMessage(final CloudQueueMessage message, final int visibilityTimeoutInSeconds)
            throws StorageException {
        this.updateMessage(message, visibilityTimeoutInSeconds, EnumSet.of(MessageUpdateFields.VISIBILITY), null, null);
    }

    /**
     * Updates a message in the queue, using the specified request options and operation context.
     * 
     * @param message
     *            The {@link CloudQueueMessage} to update in the queue.
     * 
     * @param visibilityTimeoutInSeconds
     *            Specifies the new visibility timeout for the message, in seconds.
     * 
     * @param messageUpdateFields
     *            An <code>EnumSet</code> of {@link MessageUpdateFields} values that specifies which parts of the
     *            message are to be updated.
     * 
     * @param options
     *            A {@link QueueRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudQueueClient}).
     * 
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public void updateMessage(final CloudQueueMessage message, final int visibilityTimeoutInSeconds,
            final EnumSet<MessageUpdateFields> messageUpdateFields, QueueRequestOptions options,
            OperationContext opContext) throws StorageException {
        Utility.assertNotNull("message", message);
        Utility.assertNotNullOrEmpty("messageId", message.id);
        Utility.assertNotNullOrEmpty("popReceipt", message.popReceipt);

        Utility.assertInBounds("visibilityTimeoutInSeconds", visibilityTimeoutInSeconds, 0,
                QueueConstants.MAX_TIME_TO_LIVE_IN_SECONDS);

        final String stringToSend = message.getMessageContentForTransfer(this.shouldEncodeMessage);

        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new QueueRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.queueServiceClient);

        final StorageOperation<CloudQueueClient, CloudQueue, Void> impl = new StorageOperation<CloudQueueClient, CloudQueue, Void>(
                options) {

            @Override
            public Void execute(final CloudQueueClient client, final CloudQueue queue, final OperationContext opContext)
                    throws Exception {

                final HttpURLConnection request = QueueRequest.updateMessage(queue.getIndividualMessageAddress(
                        message.getId(), opContext), this.getRequestOptions().getTimeoutIntervalInMs(), message
                        .getPopReceipt(), visibilityTimeoutInSeconds, opContext);

                if (messageUpdateFields.contains(MessageUpdateFields.CONTENT)) {
                    final byte[] messageBytes = QueueRequest.generateMessageRequestBody(stringToSend);

                    client.getCredentials().signRequest(request, messageBytes.length);
                    final OutputStream outStreamRef = request.getOutputStream();
                    outStreamRef.write(messageBytes);
                }
                else {
                    request.setFixedLengthStreamingMode(0);
                    client.getCredentials().signRequest(request, 0L);
                }

                this.setResult(ExecutionEngine.processRequest(request, opContext));

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                message.popReceipt = request.getHeaderField("x-ms-popreceipt");
                message.nextVisibleTime = Utility.parseRFC1123DateFromStringInGMT(request
                        .getHeaderField("x-ms-time-next-visible"));

                return null;
            }
        };

        ExecutionEngine.executeWithRetry(this.queueServiceClient, this, impl, options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Uploads the metadata in the <code>CloudQueue</code> object to the queue, using the default request options.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public void uploadMetadata() throws StorageException {
        this.uploadMetadata(null, null);
    }

    /**
     * Uploads the metadata in the <code>CloudQueue</code> object to the queue, using the specified request options and
     * operation context.
     * 
     * @param options
     *            A {@link QueueRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudQueueClient}).
     * 
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
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

        final StorageOperation<CloudQueueClient, CloudQueue, Void> impl = new StorageOperation<CloudQueueClient, CloudQueue, Void>(
                options) {

            @Override
            public Void execute(final CloudQueueClient client, final CloudQueue queue, final OperationContext opContext)
                    throws Exception {

                final HttpURLConnection request = QueueRequest.setMetadata(queue.getTransformedAddress(opContext), this
                        .getRequestOptions().getTimeoutIntervalInMs(), opContext);

                QueueRequest.addMetadata(request, queue.metadata, opContext);
                client.getCredentials().signRequest(request, 0L);

                this.setResult(ExecutionEngine.processRequest(request, opContext));

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                    this.setNonExceptionedRetryableFailure(true);
                }

                return null;
            }
        };

        ExecutionEngine.executeWithRetry(this.queueServiceClient, this, impl, options.getRetryPolicyFactory(),
                opContext);

    }

    /**
     * Uploads the queue's permissions.
     * 
     * @param permissions
     *            A {@link QueuePermissions} object that represents the permissions to upload.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void uploadPermissions(final QueuePermissions permissions) throws StorageException {
        this.uploadPermissions(permissions, null, null);
    }

    /**
     * Uploads the queue's permissions using the specified request options and operation context.
     * 
     * @param permissions
     *            A {@link QueuePermissions} object that represents the permissions to upload.
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
    public void uploadPermissions(final QueuePermissions permissions, QueueRequestOptions options,
            OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new QueueRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.queueServiceClient);

        final StorageOperation<CloudQueueClient, CloudQueue, Void> impl = new StorageOperation<CloudQueueClient, CloudQueue, Void>(
                options) {

            @Override
            public Void execute(final CloudQueueClient client, final CloudQueue queue, final OperationContext opContext)
                    throws Exception {

                final HttpURLConnection request = QueueRequest.setAcl(queue.getTransformedAddress(opContext), this
                        .getRequestOptions().getTimeoutIntervalInMs(), opContext);

                final StringWriter outBuffer = new StringWriter();

                QueueRequest.writeSharedAccessIdentifiersToStream(permissions.getSharedAccessPolicies(), outBuffer);

                final byte[] aclBytes = outBuffer.toString().getBytes("UTF8");
                client.getCredentials().signRequest(request, aclBytes.length);
                final OutputStream outStreamRef = request.getOutputStream();
                outStreamRef.write(aclBytes);

                this.setResult(ExecutionEngine.processRequest(request, opContext));

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                    this.setNonExceptionedRetryableFailure(true);
                }

                return null;
            }
        };

        ExecutionEngine.executeWithRetry(this.queueServiceClient, this, impl, options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Downloads the permission settings for the queue.
     * 
     * @return A {@link QueuePermissions} object that represents the queue's permissions.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public QueuePermissions downloadPermissions() throws StorageException {
        return this.downloadPermissions(null, null);
    }

    /**
     * Downloads the permissions settings for the queue using the specified request options and operation context.
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
     * @return A {@link QueuePermissions} object that represents the container's permissions.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public QueuePermissions downloadPermissions(QueueRequestOptions options, OperationContext opContext)
            throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new QueueRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.queueServiceClient);

        final StorageOperation<CloudQueueClient, CloudQueue, QueuePermissions> impl = new StorageOperation<CloudQueueClient, CloudQueue, QueuePermissions>(
                options) {

            @Override
            public QueuePermissions execute(final CloudQueueClient client, final CloudQueue queue,
                    final OperationContext opContext) throws Exception {

                final HttpURLConnection request = QueueRequest.getAcl(queue.getTransformedAddress(opContext), this
                        .getRequestOptions().getTimeoutIntervalInMs(), opContext);

                client.getCredentials().signRequest(request, -1L);

                this.setResult(ExecutionEngine.processRequest(request, opContext));

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                }

                final QueuePermissions permissions = new QueuePermissions();
                final QueueAccessPolicyResponse response = new QueueAccessPolicyResponse(request.getInputStream());

                for (final String key : response.getAccessIdentifiers().keySet()) {
                    permissions.getSharedAccessPolicies().put(key, response.getAccessIdentifiers().get(key));
                }

                return permissions;
            }
        };

        return ExecutionEngine.executeWithRetry(this.queueServiceClient, this, impl, options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Returns a shared access signature for the queue.
     * 
     * @param policy
     *            The access policy for the shared access signature.
     * @param groupPolicyIdentifier
     *            A queue-level access policy.
     * @return A shared access signature for the queue.
     * @throws InvalidKeyException
     *            If an invalid key was passed.
     * @throws StorageException
     *            If a storage service error occurred.
     * @throws IllegalArgumentException
     *            If an unexpected value is passed.
     */
    public String generateSharedAccessSignature(final SharedAccessQueuePolicy policy, final String groupPolicyIdentifier)
            throws InvalidKeyException, StorageException {

        if (!this.queueServiceClient.getCredentials().canCredentialsSignRequest()) {
            final String errorMessage = "Cannot create Shared Access Signature unless the Account Key credentials are used by the QueueServiceClient.";
            throw new IllegalArgumentException(errorMessage);
        }

        final String resourceName = this.getSharedAccessCanonicalName();

        final String signature = SharedAccessSignatureHelper.generateSharedAccessSignatureHash(policy,
                groupPolicyIdentifier, resourceName, this.queueServiceClient, null);

        final UriQueryBuilder builder = SharedAccessSignatureHelper.generateSharedAccessSignature(policy,
                groupPolicyIdentifier, signature);

        return builder.toString();
    }

    /**
     * Returns the canonical name for shared access.
     * 
     * @return the canonical name for shared access.
     */
    private String getSharedAccessCanonicalName() {
        if (this.queueServiceClient.isUsePathStyleUris()) {
            return this.getUri().getPath();
        }
        else {
            return PathUtility.getCanonicalPathFromCredentials(this.queueServiceClient.getCredentials(), this.getUri()
                    .getPath());
        }
    }

    /**
     * Returns the transformed URI for the resource if the given credentials require transformation.
     * 
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return A <code>java.net.URI</code> object that represents the transformed URI.
     * 
     * @throws IllegalArgumentException
     *             If the URI is not absolute.
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    protected final URI getTransformedAddress(final OperationContext opContext) throws URISyntaxException,
            StorageException {
        if (this.queueServiceClient.getCredentials().doCredentialsNeedTransformUri()) {
            if (this.getUri().isAbsolute()) {
                return this.queueServiceClient.getCredentials().transformUri(this.getUri(), opContext);
            }
            else {
                final StorageException ex = Utility.generateNewUnexpectedStorageException(null);
                ex.getExtendedErrorInformation().setErrorMessage("Queue Object relative URIs not supported.");
                throw ex;
            }
        }
        else {
            return this.getUri();
        }
    }

    /**
     * Parse Uri for SAS (Shared access signature) information.
     * 
     * Validate that no other query parameters are passed in. Any SAS information will be recorded as corresponding
     * credentials instance. If existingClient is passed in, any SAS information found will not be supported. Otherwise
     * a new client is created based on SAS information or as anonymous credentials.
     * 
     * @param completeUri
     *            The complete Uri.
     * @param existingClient
     *            The client to use.
     * @param usePathStyleUris
     *            If true, path style Uris are used.
     * @throws URISyntaxException
     * @throws StorageException
     */
    private void parseQueryAndVerify(final URI completeUri, final CloudQueueClient existingClient,
            final boolean usePathStyleUris) throws URISyntaxException, StorageException {
        Utility.assertNotNull("completeUri", completeUri);

        if (!completeUri.isAbsolute()) {
            final String errorMessage = String.format(
                    "Address '%s' is not an absolute address. Relative addresses are not permitted in here.",
                    completeUri.toString());
            throw new IllegalArgumentException(errorMessage);
        }

        this.uri = PathUtility.stripURIQueryAndFragment(completeUri);

        final HashMap<String, String[]> queryParameters = PathUtility.parseQueryString(completeUri.getQuery());
        final StorageCredentialsSharedAccessSignature sasCreds = SharedAccessSignatureHelper
                .parseQuery(queryParameters);

        if (sasCreds == null) {
            return;
        }

        final Boolean sameCredentials = existingClient == null ? false : Utility.areCredentialsEqual(sasCreds,
                existingClient.getCredentials());

        if (existingClient == null || !sameCredentials) {
            this.queueServiceClient = new CloudQueueClient(new URI(PathUtility.getServiceClientBaseAddress(
                    this.getUri(), usePathStyleUris)), sasCreds);
        }

        if (existingClient != null && !sameCredentials) {
            this.queueServiceClient.setRetryPolicyFactory(existingClient.getRetryPolicyFactory());
            this.queueServiceClient.setTimeoutInMs(existingClient.getTimeoutInMs());
        }
    }
}
