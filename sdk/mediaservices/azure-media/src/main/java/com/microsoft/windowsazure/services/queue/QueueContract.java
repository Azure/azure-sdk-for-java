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
package com.microsoft.windowsazure.services.queue;

import java.util.HashMap;

import com.microsoft.windowsazure.core.pipeline.jersey.JerseyFilterableService;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.queue.models.CreateMessageOptions;
import com.microsoft.windowsazure.services.queue.models.CreateQueueOptions;
import com.microsoft.windowsazure.services.queue.models.GetQueueMetadataResult;
import com.microsoft.windowsazure.services.queue.models.GetServicePropertiesResult;
import com.microsoft.windowsazure.services.queue.models.ListMessagesOptions;
import com.microsoft.windowsazure.services.queue.models.ListMessagesResult;
import com.microsoft.windowsazure.services.queue.models.ListQueuesOptions;
import com.microsoft.windowsazure.services.queue.models.ListQueuesResult;
import com.microsoft.windowsazure.services.queue.models.PeekMessagesOptions;
import com.microsoft.windowsazure.services.queue.models.PeekMessagesResult;
import com.microsoft.windowsazure.services.queue.models.QueueServiceOptions;
import com.microsoft.windowsazure.services.queue.models.ServiceProperties;
import com.microsoft.windowsazure.services.queue.models.UpdateMessageResult;

/**
 * Defines the methods available on the Windows Azure storage queue service.
 * Construct an object instance implementing <strong>QueueContract</strong> with
 * one of the static <em>create</em> methods on {@link QueueService}. These
 * methods associate a {@link Configuration} with the implementation, so the
 * methods on the instance of <strong>QueueContract</strong> all work with a
 * particular storage account.
 */
public interface QueueContract extends JerseyFilterableService<QueueContract> {
    /**
     * Gets the service properties of the queue.
     * 
     * @return A {@link GetServicePropertiesResult} reference to the queue
     *         service properties.
     * 
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    GetServicePropertiesResult getServiceProperties() throws ServiceException;

    /**
     * Gets the service properties of the queue, using the specified options.
     * Use the {@link QueueServiceOptions options} parameter to specify the
     * server timeout for the operation.
     * 
     * @param options
     *            A {@link QueueServiceOptions} instance containing options for
     *            the request.
     * @return A {@link GetServicePropertiesResult} reference to the queue
     *         service properties.
     * 
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    GetServicePropertiesResult getServiceProperties(QueueServiceOptions options)
            throws ServiceException;

    /**
     * Sets the service properties of the queue.
     * 
     * @param serviceProperties
     *            A {@link ServiceProperties} instance containing the queue
     *            service properties to set.
     * 
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    void setServiceProperties(ServiceProperties serviceProperties)
            throws ServiceException;

    /**
     * Sets the service properties of the queue, using the specified options.
     * Use the {@link QueueServiceOptions options} parameter to specify the
     * server timeout for the operation.
     * 
     * @param serviceProperties
     *            A {@link ServiceProperties} instance containing the queue
     *            service properties to set.
     * @param options
     *            A {@link QueueServiceOptions} instance containing options for
     *            the request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    void setServiceProperties(ServiceProperties serviceProperties,
            QueueServiceOptions options) throws ServiceException;

    /**
     * Creates a queue in the storage account with the specified queue name.
     * 
     * @param queue
     *            A {@link String} containing the name of the queue to create.
     * 
     * @throws ServiceException
     *             if an error occurs in the storage service.
     */
    void createQueue(String queue) throws ServiceException;

    /**
     * Creates a queue in the storage account with the specified queue name,
     * using the specified options. Use the {@link QueueServiceOptions options}
     * parameter to specify the server timeout for the operation.
     * 
     * @param queue
     *            A {@link String} containing the name of the queue to create.
     * @param options
     *            A {@link QueueServiceOptions} instance containing options for
     *            the request.
     * @throws ServiceException
     *             if an error occurs in the storage service.
     */
    void createQueue(String queue, CreateQueueOptions options)
            throws ServiceException;

    /**
     * Deletes the queue in the storage account with the specified queue name.
     * 
     * @param queue
     *            A {@link String} containing the name of the queue to delete.
     * 
     * @throws ServiceException
     *             if an error occurs in the storage service.
     */
    void deleteQueue(String queue) throws ServiceException;

    /**
     * Deletes the queue in the storage account with the specified queue name,
     * using the specified options. Use the {@link QueueServiceOptions options}
     * parameter to specify the server timeout for the operation.
     * 
     * @param queue
     *            A {@link String} containing the name of the queue to delete.
     * @param options
     *            A {@link QueueServiceOptions} instance containing options for
     *            the request.
     * @throws ServiceException
     *             if an error occurs in the storage service.
     */
    void deleteQueue(String queue, QueueServiceOptions options)
            throws ServiceException;

    /**
     * Gets a list of the queues in the storage account.
     * 
     * @return A {@link ListQueuesResult} reference to the queues returned.
     * @throws ServiceException
     *             if an error occurs in the storage service.
     */
    ListQueuesResult listQueues() throws ServiceException;

    /**
     * Gets a list of the queues in the storage account, using the specified
     * options. Use the {@link ListQueuesOptions options} parameter to specify
     * the server timeout for the operation, the prefix for queue names to
     * match, the marker for the beginning of the queues to list, the maximum
     * number of results to return, and whether to include queue metadata with
     * the results.
     * 
     * @param options
     *            A {@link ListQueuesOptions} instance containing options for
     *            the request.
     * @return A {@link ListQueuesResult} reference to the queues returned.
     * @throws ServiceException
     *             if an error occurs in the storage service.
     */
    ListQueuesResult listQueues(ListQueuesOptions options)
            throws ServiceException;

    /**
     * Gets the metadata for the named queue in the storage account. Queue
     * metadata is a user-defined collection of key-value {@link String} pairs
     * that is opaque to the server.
     * 
     * @param queue
     *            A {@link String} containing the name of the queue to get the
     *            metadata for.
     * @return A {@link GetQueueMetadataResult} reference to the metadata for
     *         the queue.
     * @throws ServiceException
     *             if an error occurs in the storage service.
     */
    GetQueueMetadataResult getQueueMetadata(String queue)
            throws ServiceException;

    /**
     * Gets the metadata for the named queue in the storage account, using the
     * specified options. Use the {@link QueueServiceOptions options} parameter
     * to specify the server timeout for the operation. Queue metadata is a
     * user-defined collection of key-value {@link String} pairs that is opaque
     * to the server.
     * 
     * @param queue
     *            A {@link String} containing the name of the queue to get the
     *            metadata for.
     * @param options
     *            A {@link QueueServiceOptions} instance containing options for
     *            the request.
     * @return A {@link ListQueuesResult} reference to the queues returned.
     * @throws ServiceException
     *             if an error occurs in the storage service.
     */
    GetQueueMetadataResult getQueueMetadata(String queue,
            QueueServiceOptions options) throws ServiceException;

    /**
     * Sets the specified metadata on the named queue in the storage account.
     * Queue metadata is a user-defined collection of key-value {@link String}
     * pairs that is opaque to the server.
     * 
     * @param queue
     *            A {@link String} containing the name of the queue to set the
     *            metadata on.
     * @param metadata
     *            A {@link java.util.HashMap} of metadata key-value
     *            {@link String} pairs to set on the queue.
     * @throws ServiceException
     *             if an error occurs in the storage service.
     */
    void setQueueMetadata(String queue, HashMap<String, String> metadata)
            throws ServiceException;

    /**
     * Sets the specified metadata on the named queue in the storage account,
     * using the specified options. Use the {@link QueueServiceOptions options}
     * parameter to specify the server timeout for the operation. Queue metadata
     * is a user-defined collection of key-value {@link String} pairs that is
     * opaque to the server.
     * 
     * @param queue
     *            A {@link String} containing the name of the queue to set the
     *            metadata on.
     * @param metadata
     *            A {@link java.util.HashMap} of metadata key-value
     *            {@link String} pairs to set on the queue.
     * @param options
     *            A {@link QueueServiceOptions} instance containing options for
     *            the request.
     * @throws ServiceException
     *             if an error occurs in the storage service.
     */
    void setQueueMetadata(String queue, HashMap<String, String> metadata,
            QueueServiceOptions options) throws ServiceException;

    /**
     * Appends a message with the specified text to the tail of the named queue
     * in the storage account.
     * 
     * @param queue
     *            A {@link String} containing the name of the queue to append
     *            the message to.
     * @param messageText
     *            A {@link String} containing the text of the message to append
     *            to the queue.
     * @throws ServiceException
     *             if an error occurs in the storage service.
     */
    void createMessage(String queue, String messageText)
            throws ServiceException;

    /**
     * Appends a message with the specified text to the tail of the named queue
     * in the storage account, using the specified options. Use the
     * {@link CreateMessageOptions options} parameter to specify the server
     * timeout for the operation, the message visibility timeout, and the
     * message time to live in the queue.
     * 
     * @param queue
     *            A {@link String} containing the name of the queue to append
     *            the message to.
     * @param messageText
     *            A {@link String} containing the text of the message to append
     *            to the queue.
     * @param options
     *            A {@link CreateMessageOptions} instance containing options for
     *            the request.
     * @throws ServiceException
     *             if an error occurs in the storage service.
     */
    void createMessage(String queue, String messageText,
            CreateMessageOptions options) throws ServiceException;

    /**
     * Updates the message in the named queue with the specified message ID and
     * pop receipt value to have the specified message text and visibility
     * timeout value.
     * 
     * @param queue
     *            A {@link String} containing the name of the queue with the
     *            message to update.
     * @param messageId
     *            A {@link String} containing the ID of the message to update.
     * @param popReceipt
     *            A {@link String} containing the pop receipt for the message
     *            returned by a call to updateMessage or listMessages.
     * @param messageText
     *            A {@link String} containing the updated text to set for the
     *            message.
     * @param visibilityTimeoutInSeconds
     *            The new visibility timeout to set on the message, in seconds.
     * @return An {@link UpdateMessageResult} reference to the updated message
     *         result returned.
     * @throws ServiceException
     *             if an error occurs in the storage service.
     */
    UpdateMessageResult updateMessage(String queue, String messageId,
            String popReceipt, String messageText,
            int visibilityTimeoutInSeconds) throws ServiceException;

    /**
     * Updates the message in the named queue with the specified message ID and
     * pop receipt value to have the specified message text and visibility
     * timeout value, using the specified options. Use the
     * {@link QueueServiceOptions options} parameter to specify the server
     * timeout for the operation.
     * 
     * @param queue
     *            A {@link String} containing the name of the queue with the
     *            message to update.
     * @param messageId
     *            A {@link String} containing the ID of the message to update.
     * @param popReceipt
     *            A {@link String} containing the pop receipt for the message
     *            returned by a call to updateMessage or listMessages.
     * @param messageText
     *            A {@link String} containing the updated text to set for the
     *            message.
     * @param visibilityTimeoutInSeconds
     *            The new visibility timeout to set on the message, in seconds.
     * @param options
     *            A {@link QueueServiceOptions} instance containing options for
     *            the request.
     * @return An {@link UpdateMessageResult} reference to the updated message
     *         result returned.
     * @throws ServiceException
     *             if an error occurs in the storage service.
     */
    UpdateMessageResult updateMessage(String queue, String messageId,
            String popReceipt, String messageText,
            int visibilityTimeoutInSeconds, QueueServiceOptions options)
            throws ServiceException;

    /**
     * Deletes the message in the named queue with the specified message ID and
     * pop receipt value.
     * 
     * @param queue
     *            A {@link String} containing the name of the queue with the
     *            message to delete.
     * @param messageId
     *            A {@link String} containing the ID of the message to delete.
     * @param popReceipt
     *            A {@link String} containing the pop receipt for the message
     *            returned by a call to updateMessage or listMessages.
     * @throws ServiceException
     *             if an error occurs in the storage service.
     */
    void deleteMessage(String queue, String messageId, String popReceipt)
            throws ServiceException;

    /**
     * Deletes the message in the named queue with the specified message ID and
     * popReceipt value, using the specified options. Use the
     * {@link QueueServiceOptions options} parameter to specify the server
     * timeout for the operation.
     * 
     * @param queue
     *            A {@link String} containing the name of the queue with the
     *            message to delete.
     * @param messageId
     *            A {@link String} containing the ID of the message to delete.
     * @param popReceipt
     *            A {@link String} containing the pop receipt for the message
     *            returned by a call to updateMessage or listMessages.
     * @param options
     *            A {@link QueueServiceOptions} instance containing options for
     *            the request.
     * @throws ServiceException
     *             if an error occurs in the storage service.
     */
    void deleteMessage(String queue, String messageId, String popReceipt,
            QueueServiceOptions options) throws ServiceException;

    /**
     * Retrieves the first message from head of the named queue in the storage
     * account. This marks the message as invisible for the default visibility
     * timeout period. When message processing is complete, the message must be
     * deleted with a call to
     * {@link com.microsoft.windowsazure.services.queue.QueueContract#deleteMessage(String, String, String, QueueServiceOptions)
     * deleteMessage}. If message processing will take longer than the
     * visibility timeout period, use the
     * {@link com.microsoft.windowsazure.services.queue.QueueContract#updateMessage(String, String, String, String, int, QueueServiceOptions)
     * updateMessage} method to extend the visibility timeout. The message will
     * become visible in the queue again when the timeout completes if it is not
     * deleted.
     * <p>
     * To get a list of multiple messages from the head of the queue, call the
     * {@link com.microsoft.windowsazure.services.queue.QueueContract#listMessages(String, ListMessagesOptions)} method
     * with options set specifying the number of messages to return.
     * 
     * @param queue
     *            A {@link String} containing the name of the queue to get the
     *            message from.
     * @return A {@link ListMessagesResult} reference to the message result
     *         returned.
     * @throws ServiceException
     *             if an error occurs in the storage service.
     */
    ListMessagesResult listMessages(String queue) throws ServiceException;

    /**
     * Retrieves up to 32 messages from the head of the named queue in the
     * storage account, using the specified options. Use the
     * {@link ListMessagesOptions options} parameter to specify the server
     * timeout for the operation, the number of messages to retrieve, and the
     * visibility timeout to set on the retrieved messages. When message
     * processing is complete, each message must be deleted with a call to
     * {@link com.microsoft.windowsazure.services.queue.QueueContract#deleteMessage(String, String, String, QueueServiceOptions)
     * deleteMessage}. If message processing takes longer than the default
     * timeout period, use the
     * {@link com.microsoft.windowsazure.services.queue.QueueContract#updateMessage(String, String, String, String, int, QueueServiceOptions)
     * updateMessage} method to extend the visibility timeout. Each message will
     * become visible in the queue again when the timeout completes if it is not
     * deleted.
     * 
     * @param queue
     *            A {@link String} containing the name of the queue to get the
     *            messages from.
     * @param options
     *            A {@link ListMessagesOptions} instance containing options for
     *            the request.
     * @return A {@link ListMessagesResult} reference to the message result
     *         returned.
     * @throws ServiceException
     *             if an error occurs in the storage service.
     */
    ListMessagesResult listMessages(String queue, ListMessagesOptions options)
            throws ServiceException;

    /**
     * Peeks a message from the named queue. A peek request retrieves a message
     * from the head of the queue without changing its visibility.
     * 
     * @param queue
     *            A {@link String} containing the name of the queue to peek the
     *            message from.
     * @return A {@link PeekMessagesResult} reference to the message result
     *         returned.
     * @throws ServiceException
     *             if an error occurs in the storage service.
     */
    PeekMessagesResult peekMessages(String queue) throws ServiceException;

    /**
     * Peeks messages from the named queue, using the specified options. A peek
     * request retrieves messages from the head of the queue without changing
     * their visibility. Use the {@link PeekMessagesOptions options} parameter
     * to specify the server timeout for the operation and the number of
     * messages to retrieve.
     * 
     * @param queue
     *            A {@link String} containing the name of the queue to peek the
     *            message from.
     * @param options
     *            A {@link PeekMessagesOptions} instance containing options for
     *            the request.
     * @return A {@link PeekMessagesResult} reference to the message result
     *         returned.
     * @throws ServiceException
     *             if an error occurs in the storage service.
     */
    PeekMessagesResult peekMessages(String queue, PeekMessagesOptions options)
            throws ServiceException;

    /**
     * Deletes all messages in the named queue.
     * 
     * @param queue
     *            A {@link String} containing the name of the queue to delete
     *            all messages from.
     * @throws ServiceException
     *             if an error occurs in the storage service.
     */
    void clearMessages(String queue) throws ServiceException;

    /**
     * Deletes all messages in the named queue, using the specified options. Use
     * the {@link QueueServiceOptions options} parameter to specify the server
     * timeout for the operation.
     * 
     * @param queue
     *            A {@link String} containing the name of the queue to delete
     *            all messages from.
     * @param options
     *            A {@link QueueServiceOptions} instance containing options for
     *            the request.
     * @throws ServiceException
     *             if an error occurs in the storage service.
     */
    void clearMessages(String queue, QueueServiceOptions options)
            throws ServiceException;

}
