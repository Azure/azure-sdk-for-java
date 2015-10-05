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

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;

import javax.xml.stream.XMLStreamException;

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
import com.microsoft.azure.storage.core.BaseResponse;
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
 * This class represents a queue in the Microsoft Azure Queue service.
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
    private static CloudQueueMessage getFirstOrNull(final Iterable<CloudQueueMessage> messages) {
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
     * A reference to the queue's associated service client.
     */
    private CloudQueueClient queueServiceClient;

    /**
     * The queue's Metadata collection.
     */
    private HashMap<String, String> metadata;

    /**
     * The queue's approximate message count, as reported by the server.
     */
    private long approximateMessageCount;

    /**
     * The <code for the messages of the queue.
     */
    private StorageUri messageRequestAddress;

    /**
     * Holds the list of URIs for all locations.
     */
    private StorageUri storageUri;

    /**
     * A flag indicating whether or not the message should be encoded in base-64.
     */
    private boolean shouldEncodeMessage;

    /**
     * Creates an instance of the <code>CloudQueue</code> class using the specified queue URI. The queue
     * <code>URI</code> must include a SAS token.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that represents the absolute URI of the queue.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudQueue(final URI uri) throws StorageException {
        this(new StorageUri(uri, null));
    }

    /**
     * Creates an instance of the <code>CloudQueue</code> class using the specified queue <code>StorageUri</code>. The 
     * queue <code>StorageUri</code> must include a SAS token.
     * 
     * @param uri
     *            A <code>StorageUri</code> object that represents the absolute URI of the queue.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudQueue(final StorageUri uri) throws StorageException {
        this(uri, (StorageCredentials)null);
    }

    /**
     * Creates an instance of the <code>CloudQueue</code> class using the specified queue <code>URI</code> and
     * credentials. If the <code>URI</code> contains a SAS token, the credentials must be <code>null</code>.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that represents the absolute URI of the queue.
     * @param credentials
     *            A {@link StorageCredentials} object used to authenticate access.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudQueue(final URI uri, final StorageCredentials credentials) throws StorageException {
        this(new StorageUri(uri), credentials);
    }

    /**
     * Creates an instance of the <code>CloudQueue</code> class using the specified queue <code>StorageUri</code> and
     * credentials. If the <code>StorageUri</code> contains a SAS token, the credentials must be <code>null</code>.
     * 
     * @param uri
     *            A <code>StorageUri</code> object that represents the absolute URI of the queue.
     * @param credentials
     *            A {@link StorageCredentials} object used to authenticate access.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudQueue(final StorageUri uri, final StorageCredentials credentials) throws StorageException {
        this.shouldEncodeMessage = true;
        this.parseQueryAndVerify(uri, credentials);
    }
    
    /**
     * Creates an instance of the <code>CloudQueue</code> class using the specified name and client.
     * 
     * @param queueName
     *            The name of the queue, which must adhere to queue naming rules. The queue name should not include any
     *            path separator characters (/).
     *            Queue names must be lowercase, between 3-63 characters long and must start with a letter or number.
     *            Queue names may contain only letters, numbers, and the dash (-) character.
     * @param client
     *            A {@link CloudQueueClient} object that represents the associated service client, and that specifies
     *            the endpoint for the Queue service.
     * @throws URISyntaxException
     *             If the resource URI constructed based on the queueName is invalid.
     * @throws StorageException
     *             If a storage service error occurred.
     * @see <a href="http://msdn.microsoft.com/en-us/library/azure/dd179349.aspx">Naming Queues and Metadata</a>
     */
    protected CloudQueue(final String queueName, final CloudQueueClient client) throws URISyntaxException,
            StorageException {
        Utility.assertNotNull("client", client);
        Utility.assertNotNull("queueName", queueName);

        this.storageUri = PathUtility.appendPathToUri(client.getStorageUri(), queueName);
        this.name = queueName;
        this.queueServiceClient = client;
        this.shouldEncodeMessage = true;
    }

    /**
     * Adds a message to the back of the queue.
     * 
     * @param message
     *            A {@link CloudQueueMessage} object that specifies the message to add.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public void addMessage(final CloudQueueMessage message) throws StorageException {
        this.addMessage(message, 0, 0, null /* options */, null /* opContext */);
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

        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = QueueRequestOptions.populateAndApplyDefaults(options, this.queueServiceClient);

        ExecutionEngine.executeWithRetry(this.queueServiceClient, this,
                this.addMessageImpl(message, realTimeToLiveInSeconds, initialVisibilityDelayInSeconds, options),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudQueueClient, CloudQueue, Void> addMessageImpl(final CloudQueueMessage message,
            final int timeToLiveInSeconds, final int initialVisibilityDelayInSeconds, final QueueRequestOptions options)
            throws StorageException {
        final String stringToSend = message.getMessageContentForTransfer(this.shouldEncodeMessage);

        try {
            final byte[] messageBytes = QueueMessageSerializer.generateMessageRequestBody(stringToSend);

            final StorageRequest<CloudQueueClient, CloudQueue, Void> putRequest = new StorageRequest<CloudQueueClient, CloudQueue, Void>(
                    options, this.getStorageUri()) {

                @Override
                public HttpURLConnection buildRequest(CloudQueueClient client, CloudQueue queue,
                        OperationContext context) throws Exception {
                    this.setSendStream(new ByteArrayInputStream(messageBytes));
                    this.setLength((long) messageBytes.length);
                    return QueueRequest.putMessage(
                            queue.getMessageRequestAddress(context).getUri(this.getCurrentLocation()), options,
                            context, initialVisibilityDelayInSeconds, timeToLiveInSeconds);
                }

                @Override
                public void signRequest(HttpURLConnection connection, CloudQueueClient client, OperationContext context)
                        throws Exception {
                    StorageRequest.signBlobQueueAndFileRequest(connection, client, messageBytes.length, context);
                }

                @Override
                public Void preProcessResponse(CloudQueue parentObject, CloudQueueClient client,
                        OperationContext context) throws Exception {
                    if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_CREATED) {
                        this.setNonExceptionedRetryableFailure(true);
                        return null;
                    }

                    return null;
                }
            };

            return putRequest;
        }
        catch (XMLStreamException e) {
            // The request was not even made. There was an error while trying to generate the message body. Just throw.
            StorageException translatedException = StorageException.translateClientException(e);
            throw translatedException;
        }
    }

    /**
     * Clears all messages from the queue.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public void clear() throws StorageException {
        this.clear(null /* options */, null /* opContext */);
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

        opContext.initialize();
        options = QueueRequestOptions.populateAndApplyDefaults(options, this.queueServiceClient);

        ExecutionEngine.executeWithRetry(this.queueServiceClient, this, this.clearImpl(options),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudQueueClient, CloudQueue, Void> clearImpl(final QueueRequestOptions options) {
        final StorageRequest<CloudQueueClient, CloudQueue, Void> putRequest = new StorageRequest<CloudQueueClient, CloudQueue, Void>(
                options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudQueueClient client, CloudQueue queue, OperationContext context)
                    throws Exception {
                return QueueRequest.clearMessages(
                        queue.getMessageRequestAddress(context).getUri(this.getCurrentLocation()), options, context);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudQueueClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, -1L, context);
            }

            @Override
            public Void preProcessResponse(CloudQueue parentObject, CloudQueueClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                    this.setNonExceptionedRetryableFailure(true);
                }

                return null;
            }
        };

        return putRequest;
    }

    /**
     * Creates the queue.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public void create() throws StorageException {
        this.create(null /* options */, null /* opContext */);
    }

    /**
     * Creates the queue, using the specified request options and operation context.
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

        opContext.initialize();
        options = QueueRequestOptions.populateAndApplyDefaults(options, this.queueServiceClient);

        ExecutionEngine.executeWithRetry(this.queueServiceClient, this, this.createImpl(options),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudQueueClient, CloudQueue, Void> createImpl(final QueueRequestOptions options) {
        final StorageRequest<CloudQueueClient, CloudQueue, Void> putRequest = new StorageRequest<CloudQueueClient, CloudQueue, Void>(
                options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudQueueClient client, CloudQueue queue, OperationContext context)
                    throws Exception {
                return QueueRequest.create(queue.getTransformedAddress(context).getUri(this.getCurrentLocation()),
                        options, context);
            }

            @Override
            public void setHeaders(HttpURLConnection connection, CloudQueue queue, OperationContext context) {
                QueueRequest.addMetadata(connection, queue.metadata, context);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudQueueClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, 0L, context);
            }

            @Override
            public Void preProcessResponse(CloudQueue parentObject, CloudQueueClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_CREATED
                        && this.getResult().getStatusCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                    this.setNonExceptionedRetryableFailure(true);
                }

                return null;
            }

        };

        return putRequest;
    }

    /**
     * Creates the queue if it does not already exist.
     * 
     * @return A value of <code>true</code> if the queue is created in the storage service, otherwise <code>false</code>
     *         .
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public boolean createIfNotExists() throws StorageException {
        return this.createIfNotExists(null /* options */, null /* opContext */);
    }

    /**
     * Creates the queue if it does not already exist, using the specified request options and operation context.
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
    public boolean createIfNotExists(QueueRequestOptions options, OperationContext opContext) throws StorageException {
        options = QueueRequestOptions.populateAndApplyDefaults(options, this.queueServiceClient);

        boolean exists = this.exists(true, options, opContext);
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
                        && StorageErrorCodeStrings.QUEUE_ALREADY_EXISTS.equals(e.getErrorCode())) {
                    return false;
                }
                else {
                    throw e;
                }
            }
        }
    }

    /**
     * Deletes the queue.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public void delete() throws StorageException {
        this.delete(null /* options */, null /* opContext */);
    }

    /**
     * Deletes the queue, using the specified request options and operation context.
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

        opContext.initialize();
        options = QueueRequestOptions.populateAndApplyDefaults(options, this.queueServiceClient);

        ExecutionEngine.executeWithRetry(this.queueServiceClient, this, this.deleteImpl(options),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudQueueClient, CloudQueue, Void> deleteImpl(final QueueRequestOptions options) {
        final StorageRequest<CloudQueueClient, CloudQueue, Void> deleteRequest = new StorageRequest<CloudQueueClient, CloudQueue, Void>(
                options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudQueueClient client, CloudQueue queue, OperationContext context)
                    throws Exception {
                return QueueRequest.delete(queue.getTransformedAddress(context).getUri(this.getCurrentLocation()),
                        options, context);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudQueueClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, -1L, context);
            }

            @Override
            public Void preProcessResponse(CloudQueue parentObject, CloudQueueClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                    this.setNonExceptionedRetryableFailure(true);
                }

                return null;
            }
        };

        return deleteRequest;
    }

    /**
     * Deletes the queue if it exists.
     * 
     * @return A value of <code>true</code> if the queue existed in the storage service and has been deleted, otherwise
     *         <code>false</code>.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public boolean deleteIfExists() throws StorageException {
        return this.deleteIfExists(null /* options */, null /* opContext */);
    }

    /**
     * Deletes the queue if it exists, using the specified request options and operation context.
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
        options = QueueRequestOptions.populateAndApplyDefaults(options, this.queueServiceClient);

        boolean exists = this.exists(true, options, opContext);
        if (exists) {
            try {
                this.delete(options, opContext);
                return true;
            }
            catch (StorageException e) {
                if (e.getHttpStatusCode() == HttpURLConnection.HTTP_NOT_FOUND
                        && StorageErrorCodeStrings.QUEUE_NOT_FOUND.equals(e.getErrorCode())) {
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
        this.deleteMessage(message, null /* options */, null /* opContext */);
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
        Utility.assertNotNullOrEmpty("messageId", message.getId());
        Utility.assertNotNullOrEmpty("popReceipt", message.getPopReceipt());

        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = QueueRequestOptions.populateAndApplyDefaults(options, this.queueServiceClient);

        ExecutionEngine.executeWithRetry(this.queueServiceClient, this, this.deleteMessageImpl(message, options),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudQueueClient, CloudQueue, Void> deleteMessageImpl(final CloudQueueMessage message,
            final QueueRequestOptions options) {
        final String messageId = message.getId();
        final String messagePopReceipt = message.getPopReceipt();

        final StorageRequest<CloudQueueClient, CloudQueue, Void> putRequest = new StorageRequest<CloudQueueClient, CloudQueue, Void>(
                options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudQueueClient client, CloudQueue queue, OperationContext context)
                    throws Exception {
                return QueueRequest.deleteMessage(
                        queue.getIndividualMessageAddress(messageId, context).getUri(this.getCurrentLocation()),
                        options, context, messagePopReceipt);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudQueueClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, -1L, context);
            }

            @Override
            public Void preProcessResponse(CloudQueue parentObject, CloudQueueClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                    this.setNonExceptionedRetryableFailure(true);
                }

                return null;
            }
        };

        return putRequest;
    }

    /**
     * Downloads the queue's metadata and approximate message count value.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public void downloadAttributes() throws StorageException {
        this.downloadAttributes(null /* options */, null /* opContext */);
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

        opContext.initialize();
        options = QueueRequestOptions.populateAndApplyDefaults(options, this.queueServiceClient);

        ExecutionEngine.executeWithRetry(this.queueServiceClient, this, this.downloadAttributesImpl(options),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudQueueClient, CloudQueue, Void> downloadAttributesImpl(final QueueRequestOptions options) {
        final StorageRequest<CloudQueueClient, CloudQueue, Void> getRequest = new StorageRequest<CloudQueueClient, CloudQueue, Void>(
                options, this.getStorageUri()) {

            @Override
            public void setRequestLocationMode() {
                this.setRequestLocationMode(RequestLocationMode.PRIMARY_OR_SECONDARY);
            }

            @Override
            public HttpURLConnection buildRequest(CloudQueueClient client, CloudQueue queue, OperationContext context)
                    throws Exception {
                return QueueRequest.downloadAttributes(
                        queue.getTransformedAddress(context).getUri(this.getCurrentLocation()), options, context);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudQueueClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, -1L, context);
            }

            @Override
            public Void preProcessResponse(CloudQueue queue, CloudQueueClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                queue.metadata = BaseResponse.getMetadata(this.getConnection());
                queue.approximateMessageCount = QueueResponse.getApproximateMessageCount(this.getConnection());
                return null;
            }
        };

        return getRequest;
    }

    /**
     * Returns a value that indicates whether the queue exists.
     * 
     * @return <code>true</code> if the queue exists in the storage service, otherwise <code>false</code>.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public boolean exists() throws StorageException {
        return this.exists(null /* options */, null /* opContext */);
    }

    /**
     * Returns a value that indicates whether the queue existse, using the specified request options and operation
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
     * @return <code>true</code> if the queue exists in the storage service, otherwise <code>false</code>.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public boolean exists(QueueRequestOptions options, OperationContext opContext) throws StorageException {
        return this.exists(false, options, opContext);
    }

    @DoesServiceRequest
    private boolean exists(final boolean primaryOnly, QueueRequestOptions options, OperationContext opContext)
            throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = QueueRequestOptions.populateAndApplyDefaults(options, this.queueServiceClient);

        return ExecutionEngine.executeWithRetry(this.queueServiceClient, this, this.existsImpl(primaryOnly, options),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudQueueClient, CloudQueue, Boolean> existsImpl(final boolean primaryOnly,
            final QueueRequestOptions options) {
        final StorageRequest<CloudQueueClient, CloudQueue, Boolean> getRequest = new StorageRequest<CloudQueueClient, CloudQueue, Boolean>(
                options, this.getStorageUri()) {

            @Override
            public void setRequestLocationMode() {
                this.setRequestLocationMode(primaryOnly ? RequestLocationMode.PRIMARY_ONLY
                        : RequestLocationMode.PRIMARY_OR_SECONDARY);
            }

            @Override
            public HttpURLConnection buildRequest(CloudQueueClient client, CloudQueue queue, OperationContext context)
                    throws Exception {
                return QueueRequest.downloadAttributes(
                        queue.getTransformedAddress(context).getUri(this.getCurrentLocation()), options, context);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudQueueClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, -1L, context);
            }

            @Override
            public Boolean preProcessResponse(CloudQueue parentObject, CloudQueueClient client, OperationContext context)
                    throws Exception {
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

        return getRequest;
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
    private StorageUri getIndividualMessageAddress(final String messageId, final OperationContext opContext)
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
    private StorageUri getMessageRequestAddress(final OperationContext opContext) throws URISyntaxException,
            StorageException {
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
     * Returns the list of URIs for all locations.
     * 
     * @return A <code>StorageUri</code> that represents the list of URIs for all locations..
     */
    public final StorageUri getStorageUri() {
        return this.storageUri;
    }

    /**
     * Gets the absolute URI for this queue.
     * 
     * @return A <code>java.net.URI</code> object that represents the URI for this queue.
     */
    public URI getUri() {
        return this.storageUri.getPrimaryUri();
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
        return this.peekMessage(null /* options */, null /* opContext */);
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
        return getFirstOrNull(this.peekMessages(1, null /* options */, null /* opContext */));
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
        return this.peekMessages(numberOfMessages, null /* options */, null /* opContext */);
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

        opContext.initialize();
        options = QueueRequestOptions.populateAndApplyDefaults(options, this.queueServiceClient);

        return ExecutionEngine.executeWithRetry(this.queueServiceClient, this,
                this.peekMessagesImpl(numberOfMessages, options), options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudQueueClient, CloudQueue, ArrayList<CloudQueueMessage>> peekMessagesImpl(
            final int numberOfMessages, final QueueRequestOptions options) {
        final StorageRequest<CloudQueueClient, CloudQueue, ArrayList<CloudQueueMessage>> getRequest = new StorageRequest<CloudQueueClient, CloudQueue, ArrayList<CloudQueueMessage>>(
                options, this.getStorageUri()) {

            @Override
            public void setRequestLocationMode() {
                this.setRequestLocationMode(RequestLocationMode.PRIMARY_OR_SECONDARY);
            }

            @Override
            public HttpURLConnection buildRequest(CloudQueueClient client, CloudQueue queue, OperationContext context)
                    throws Exception {
                return QueueRequest.peekMessages(
                        queue.getMessageRequestAddress(context).getUri(this.getCurrentLocation()), options, context,
                        numberOfMessages);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudQueueClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, -1L, context);
            }

            @Override
            public ArrayList<CloudQueueMessage> preProcessResponse(CloudQueue queue, CloudQueueClient client,
                    OperationContext context) throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }
                else {
                    return QueueMessageHandler.readMessages(this.getConnection().getInputStream(),
                            queue.shouldEncodeMessage);
                }
            }

        };

        return getRequest;
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
        return this.retrieveMessage(QueueConstants.DEFAULT_VISIBILITY_MESSAGE_TIMEOUT_IN_SECONDS, null /* options */,
                null /* opContext */);
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
        return getFirstOrNull(this.retrieveMessages(1, visibilityTimeoutInSeconds, options, opContext));
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
                null /* options */, null /* opContext */);
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

        opContext.initialize();
        options = QueueRequestOptions.populateAndApplyDefaults(options, this.queueServiceClient);

        return ExecutionEngine.executeWithRetry(this.queueServiceClient, this,
                this.retrieveMessagesImpl(numberOfMessages, visibilityTimeoutInSeconds, options),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudQueueClient, CloudQueue, ArrayList<CloudQueueMessage>> retrieveMessagesImpl(
            final int numberOfMessages, final int visibilityTimeoutInSeconds, final QueueRequestOptions options) {
        final StorageRequest<CloudQueueClient, CloudQueue, ArrayList<CloudQueueMessage>> getRequest = new StorageRequest<CloudQueueClient, CloudQueue, ArrayList<CloudQueueMessage>>(
                options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudQueueClient client, CloudQueue queue, OperationContext context)
                    throws Exception {
                return QueueRequest.retrieveMessages(
                        queue.getMessageRequestAddress(context).getUri(this.getCurrentLocation()), options, context,
                        numberOfMessages, visibilityTimeoutInSeconds);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudQueueClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, -1L, context);
            }

            @Override
            public ArrayList<CloudQueueMessage> preProcessResponse(CloudQueue queue, CloudQueueClient client,
                    OperationContext context) throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }
                else {
                    return QueueMessageHandler.readMessages(this.getConnection().getInputStream(),
                            queue.shouldEncodeMessage);
                }
            }
        };

        return getRequest;
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
        this.updateMessage(message, visibilityTimeoutInSeconds, EnumSet.of(MessageUpdateFields.VISIBILITY),
                null /* options */, null /* opContext */);
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
        Utility.assertNotNullOrEmpty("messageId", message.getId());
        Utility.assertNotNullOrEmpty("popReceipt", message.getPopReceipt());

        Utility.assertInBounds("visibilityTimeoutInSeconds", visibilityTimeoutInSeconds, 0,
                QueueConstants.MAX_TIME_TO_LIVE_IN_SECONDS);

        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = QueueRequestOptions.populateAndApplyDefaults(options, this.queueServiceClient);

        ExecutionEngine.executeWithRetry(this.queueServiceClient, this,
                this.updateMessageImpl(message, visibilityTimeoutInSeconds, messageUpdateFields, options),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudQueueClient, CloudQueue, Void> updateMessageImpl(final CloudQueueMessage message,
            final int visibilityTimeoutInSeconds, final EnumSet<MessageUpdateFields> messageUpdateFields,
            final QueueRequestOptions options) throws StorageException {
        final String stringToSend = message.getMessageContentForTransfer(this.shouldEncodeMessage);

        final StorageRequest<CloudQueueClient, CloudQueue, Void> putRequest = new StorageRequest<CloudQueueClient, CloudQueue, Void>(
                options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudQueueClient client, CloudQueue queue, OperationContext context)
                    throws Exception {
                if (messageUpdateFields.contains(MessageUpdateFields.CONTENT)) {
                    final byte[] messageBytes = QueueMessageSerializer.generateMessageRequestBody(stringToSend);
                    this.setSendStream(new ByteArrayInputStream(messageBytes));
                    this.setLength((long) messageBytes.length);
                }

                return QueueRequest.updateMessage(
                        queue.getIndividualMessageAddress(message.getId(), context).getUri(this.getCurrentLocation()),
                        options, context, message.getPopReceipt(), visibilityTimeoutInSeconds);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudQueueClient client, OperationContext context)
                    throws Exception {
                if (messageUpdateFields.contains(MessageUpdateFields.CONTENT)) {
                    StorageRequest.signBlobQueueAndFileRequest(connection, client, this.getLength(), context);
                }
                else {
                    connection.setFixedLengthStreamingMode(0);
                    StorageRequest.signBlobQueueAndFileRequest(connection, client, 0L, context);
                }
            }

            @Override
            public Void preProcessResponse(CloudQueue queue, CloudQueueClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                message.setPopReceipt(this.getConnection().getHeaderField(Constants.HeaderConstants.POP_RECEIPT_HEADER));
                message.setNextVisibleTime(Utility.parseRFC1123DateFromStringInGMT(this.getConnection().getHeaderField(
                        Constants.HeaderConstants.TIME_NEXT_VISIBLE_HEADER)));

                return null;
            }
        };

        return putRequest;
    }

    /**
     * Uploads the metadata in the <code>CloudQueue</code> object to the queue.
     * 
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    @DoesServiceRequest
    public void uploadMetadata() throws StorageException {
        this.uploadMetadata(null /* options */, null /* opContext */);
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

        opContext.initialize();
        options = QueueRequestOptions.populateAndApplyDefaults(options, this.queueServiceClient);

        ExecutionEngine.executeWithRetry(this.queueServiceClient, this, this.uploadMetadataImpl(options),
                options.getRetryPolicyFactory(), opContext);

    }

    private StorageRequest<CloudQueueClient, CloudQueue, Void> uploadMetadataImpl(final QueueRequestOptions options) {

        final StorageRequest<CloudQueueClient, CloudQueue, Void> putRequest = new StorageRequest<CloudQueueClient, CloudQueue, Void>(
                options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudQueueClient client, CloudQueue queue, OperationContext context)
                    throws Exception {
                return QueueRequest.setMetadata(queue.getTransformedAddress(context).getUri(this.getCurrentLocation()),
                        options, context);
            }

            @Override
            public void setHeaders(HttpURLConnection connection, CloudQueue queue, OperationContext context) {
                QueueRequest.addMetadata(connection, queue.metadata, context);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudQueueClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, 0L, context);
            }

            @Override
            public Void preProcessResponse(CloudQueue parentObject, CloudQueueClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                    this.setNonExceptionedRetryableFailure(true);
                }

                return null;
            }

        };

        return putRequest;
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
        this.uploadPermissions(permissions, null /* options */, null /* opContext */);
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

        opContext.initialize();
        options = QueueRequestOptions.populateAndApplyDefaults(options, this.queueServiceClient);

        ExecutionEngine.executeWithRetry(this.queueServiceClient, this,
                this.uploadPermissionsImpl(permissions, options), options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudQueueClient, CloudQueue, Void> uploadPermissionsImpl(
            final QueuePermissions permissions, final QueueRequestOptions options) throws StorageException {

        final StringWriter outBuffer = new StringWriter();

        try {
            SharedAccessPolicySerializer.writeSharedAccessIdentifiersToStream(permissions.getSharedAccessPolicies(),
                    outBuffer);

            final byte[] aclBytes = outBuffer.toString().getBytes(Constants.UTF8_CHARSET);

            final StorageRequest<CloudQueueClient, CloudQueue, Void> putRequest = new StorageRequest<CloudQueueClient, CloudQueue, Void>(
                    options, this.getStorageUri()) {

                @Override
                public HttpURLConnection buildRequest(CloudQueueClient client, CloudQueue queue,
                        OperationContext context) throws Exception {
                    this.setSendStream(new ByteArrayInputStream(aclBytes));
                    this.setLength((long) aclBytes.length);
                    return QueueRequest.setAcl(queue.getTransformedAddress(context).getUri(this.getCurrentLocation()),
                            options, context);
                }

                @Override
                public void signRequest(HttpURLConnection connection, CloudQueueClient client, OperationContext context)
                        throws Exception {
                    StorageRequest.signBlobQueueAndFileRequest(connection, client, aclBytes.length, context);
                }

                @Override
                public Void preProcessResponse(CloudQueue parentObject, CloudQueueClient client,
                        OperationContext context) throws Exception {
                    if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                        this.setNonExceptionedRetryableFailure(true);
                    }

                    return null;
                }
            };

            return putRequest;
        }
        catch (IllegalArgumentException e) {
            // to do : Move this to multiple catch clause so we can avoid the duplicated code once we move to Java 1.7.
            // The request was not even made. There was an error while trying to read the permissions. Just throw.
            StorageException translatedException = StorageException.translateClientException(e);
            throw translatedException;
        }
        catch (XMLStreamException e) {
            // The request was not even made. There was an error while trying to read the permissions. Just throw.
            StorageException translatedException = StorageException.translateClientException(e);
            throw translatedException;
        }
        catch (UnsupportedEncodingException e) {
            // The request was not even made. There was an error while trying to read the permissions. Just throw.
            StorageException translatedException = StorageException.translateClientException(e);
            throw translatedException;
        }
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
        return this.downloadPermissions(null /* options */, null /* opContext */);
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

        opContext.initialize();
        options = QueueRequestOptions.populateAndApplyDefaults(options, this.queueServiceClient);

        return ExecutionEngine.executeWithRetry(this.queueServiceClient, this, this.downloadPermissionsImpl(options),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudQueueClient, CloudQueue, QueuePermissions> downloadPermissionsImpl(
            final QueueRequestOptions options) {

        final StorageRequest<CloudQueueClient, CloudQueue, QueuePermissions> getRequest = new StorageRequest<CloudQueueClient, CloudQueue, QueuePermissions>(
                options, this.getStorageUri()) {

            @Override
            public void setRequestLocationMode() {
                this.setRequestLocationMode(RequestLocationMode.PRIMARY_OR_SECONDARY);
            }

            @Override
            public HttpURLConnection buildRequest(CloudQueueClient client, CloudQueue queue, OperationContext context)
                    throws Exception {
                return QueueRequest.getAcl(queue.getTransformedAddress(context).getUri(this.getCurrentLocation()),
                        options, context);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudQueueClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, -1L, context);
            }

            @Override
            public QueuePermissions preProcessResponse(CloudQueue parentObject, CloudQueueClient client,
                    OperationContext context) throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                return new QueuePermissions();
            }

            @Override
            public QueuePermissions postProcessResponse(HttpURLConnection connection, CloudQueue queue,
                    CloudQueueClient client, OperationContext context, QueuePermissions queuePermissions)
                    throws Exception {
                HashMap<String, SharedAccessQueuePolicy> accessIds = SharedAccessPolicyHandler.getAccessIdentifiers(
                        this.getConnection().getInputStream(), SharedAccessQueuePolicy.class);

                for (final String key : accessIds.keySet()) {
                    queuePermissions.getSharedAccessPolicies().put(key, accessIds.get(key));
                }

                return queuePermissions;
            }
        };

        return getRequest;
    }

    /**
     * Returns a shared access signature for the queue.
     * 
     * @param policy
     *            The access policy for the shared access signature.
     * @param groupPolicyIdentifier
     *            A queue-level access policy.
     *            
     * @return A shared access signature for the queue.
     * 
     * @throws InvalidKeyException
     *             If an invalid key was passed.
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws IllegalArgumentException
     *             If an unexpected value is passed.
     */
    public String generateSharedAccessSignature(final SharedAccessQueuePolicy policy, final String groupPolicyIdentifier)
            throws InvalidKeyException, StorageException {

        return this.generateSharedAccessSignature(policy, groupPolicyIdentifier, null /* IP range */, null /* protocols */);
    }

    /**
     * Returns a shared access signature for the queue.
     * 
     * @param policy
     *            The access policy for the shared access signature.
     * @param groupPolicyIdentifier
     *            A queue-level access policy.
     * @param ipRange
     *            A {@link IPRange} object containing the range of allowed IP addresses.
     * @param protocols
     *            A {@link SharedAccessProtocols} representing the allowed Internet protocols.
     *            
     * @return A shared access signature for the queue.
     * 
     * @throws InvalidKeyException
     *             If an invalid key was passed.
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws IllegalArgumentException
     *             If an unexpected value is passed.
     */
    public String generateSharedAccessSignature(
            final SharedAccessQueuePolicy policy, final String groupPolicyIdentifier, final IPRange ipRange,
            final SharedAccessProtocols protocols)
            throws InvalidKeyException, StorageException {

        if (!StorageCredentialsHelper.canCredentialsSignRequest(this.queueServiceClient.getCredentials())) {
            final String errorMessage = SR.CANNOT_CREATE_SAS_WITHOUT_ACCOUNT_KEY;
            throw new IllegalArgumentException(errorMessage);
        }

        final String resourceName = this.getSharedAccessCanonicalName();

        final String signature = SharedAccessSignatureHelper.generateSharedAccessSignatureHashForQueue(
                policy, groupPolicyIdentifier, resourceName, ipRange, protocols, this.queueServiceClient);

        final UriQueryBuilder builder = SharedAccessSignatureHelper.generateSharedAccessSignatureForQueue(
                policy, groupPolicyIdentifier, ipRange, protocols, signature);

        return builder.toString();
    }

    /**
     * Returns the canonical name for shared access.
     * 
     * @return the canonical name for shared access.
     */
    private String getSharedAccessCanonicalName() {
        String accountName = this.getServiceClient().getCredentials().getAccountName();
        String queueName = this.getName();

        return String.format("/%s/%s/%s", SR.QUEUE, accountName, queueName);
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
    private final StorageUri getTransformedAddress(final OperationContext opContext) throws URISyntaxException,
            StorageException {
        return this.queueServiceClient.getCredentials().transformUri(this.getStorageUri(), opContext);
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
            this.queueServiceClient = new CloudQueueClient(PathUtility.getServiceClientBaseAddress(
                    this.getStorageUri(), usePathStyleUris), credentials != null ? credentials : parsedCredentials);
            this.name = PathUtility.getContainerNameFromUri(storageUri.getPrimaryUri(), usePathStyleUris);
        }
        catch (final URISyntaxException e) {
            throw Utility.generateNewUnexpectedStorageException(e);
        }
    }
}
