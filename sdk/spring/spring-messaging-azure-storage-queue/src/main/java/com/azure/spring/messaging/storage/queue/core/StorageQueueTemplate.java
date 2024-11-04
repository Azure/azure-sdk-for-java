// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.storage.queue.core;

import com.azure.spring.messaging.AzureHeaders;
import com.azure.spring.messaging.converter.AzureMessageConverter;
import com.azure.spring.messaging.implementation.checkpoint.AzureCheckpointer;
import com.azure.spring.messaging.checkpoint.Checkpointer;
import com.azure.spring.messaging.core.SendOperation;
import com.azure.spring.messaging.storage.queue.core.factory.StorageQueueClientFactory;
import com.azure.spring.messaging.storage.queue.implementation.StorageQueueHelper;
import com.azure.spring.messaging.storage.queue.implementation.support.converter.StorageQueueMessageConverter;
import com.azure.storage.queue.QueueAsyncClient;
import com.azure.storage.queue.models.QueueMessageItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Azure Storage Queue template to support send / receive {@link Message}s asynchronously.
 *
 * You should checkpoint if message has been processed successfully, otherwise it will be visible again after certain
 * time specified by {@link #receiveAsync(String, Duration)} }.
 */
public class StorageQueueTemplate implements SendOperation {
    private static final Logger LOG = LoggerFactory.getLogger(StorageQueueTemplate.class);
    private static final String MSG_FAIL_CHECKPOINT = "Failed to checkpoint %s in storage queue '%s'";
    private static final String MSG_SUCCESS_CHECKPOINT = "Checkpointed %s in storage queue '%s'";
    private final StorageQueueClientFactory storageQueueClientFactory;

    private AzureMessageConverter<QueueMessageItem, QueueMessageItem> messageConverter = new StorageQueueMessageConverter();

    private Class<?> messagePayloadType = byte[].class;

    /**
     * Create an instance using the supplied StorageQueueClientFactory.
     * @param storageQueueClientFactory the StorageQueueClientFactory.
     */
    public StorageQueueTemplate(@NonNull StorageQueueClientFactory storageQueueClientFactory) {
        this.storageQueueClientFactory = storageQueueClientFactory;
        LOG.info("StorageQueueTemplate started with default properties {}", buildProperties());
    }

    @Override
    public <T> Mono<Void> sendAsync(String queueName, @NonNull Message<T> message) {
        Assert.hasText(queueName, "queueName can't be null or empty");
        QueueMessageItem queueMessageItem = messageConverter.fromMessage(message, QueueMessageItem.class);
        QueueAsyncClient queueClient = storageQueueClientFactory.createQueueClient(queueName);
        Assert.notNull(queueMessageItem, "queueMessageItem can't be null");
        return queueClient.sendMessage(queueMessageItem.getBody().toString()).then();
    }

    /**
     * Receive a message from the queue asynchronously.
     * @param queueName the queue
     * @param visibilityTimeout The timeout period for how long the message is invisible in the queue. If left empty
     * the dequeued messages will be invisible for 30 seconds. The timeout must be between 1 second and 7 days.
     * @return {@link Mono} of the next available {@link Message} or {@code null} if empty
     */
    public Mono<Message<?>> receiveAsync(String queueName, Duration visibilityTimeout) {
        Assert.hasText(queueName, "queueName can't be null or empty");
        QueueAsyncClient queueClient = storageQueueClientFactory.createQueueClient(queueName);
        return queueClient.receiveMessages(1, visibilityTimeout)
            .next()
            .flatMap(messageItem -> {
                Map<String, Object> headers = new HashMap<>();
                Checkpointer checkpointer = new AzureCheckpointer(() -> checkpoint(queueClient, messageItem));
                headers.put(AzureHeaders.CHECKPOINTER, checkpointer);
                return Mono.justOrEmpty(messageConverter.toMessage(messageItem, new MessageHeaders(headers), messagePayloadType));
            });
    }

    private Mono<Void> checkpoint(QueueAsyncClient queueClient, QueueMessageItem messageItem) {
        return queueClient
            .deleteMessage(messageItem.getMessageId(), messageItem.getPopReceipt())
            .doOnSuccess(v -> {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(buildCheckpointSuccessMessage(messageItem, queueClient.getQueueName()));
                }
            })
            .doOnError(t -> {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(buildCheckpointFailMessage(messageItem, queueClient.getQueueName()), t);
                }
            });
    }

    private Map<String, Object> buildProperties() {
        Map<String, Object> properties = new HashMap<>();

        properties.put("messagePayloadType", this.messagePayloadType);
        return properties;
    }

    private String buildCheckpointFailMessage(QueueMessageItem cloudQueueMessage, String queueName) {
        return String.format(MSG_FAIL_CHECKPOINT, StorageQueueHelper.toString(cloudQueueMessage), queueName);
    }

    private String buildCheckpointSuccessMessage(QueueMessageItem cloudQueueMessage, String queueName) {
        return String.format(MSG_SUCCESS_CHECKPOINT, StorageQueueHelper.toString(cloudQueueMessage), queueName);
    }

    /**
     * Get the message converter.
     * @return the message converter.
     */
    public AzureMessageConverter<QueueMessageItem, QueueMessageItem> getMessageConverter() {
        return messageConverter;
    }

    /**
     * Set the message converter to use.
     * @param messageConverter the message converter.
     */
    public void setMessageConverter(AzureMessageConverter<QueueMessageItem, QueueMessageItem> messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Get the {@code messagePayloadType}.
     * @return the messagePayloadType.
     */
    public Class<?> getMessagePayloadType() {
        return messagePayloadType;
    }

    /**
     * Set message payload type. Default is {@code byte[]}
     * @param payloadType message payload type
     */
    public void setMessagePayloadType(Class<?> payloadType) {
        this.messagePayloadType = payloadType;
        LOG.info("StorageQueueTemplate messagePayloadType becomes: {}", this.messagePayloadType);
    }

}
