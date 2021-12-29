// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.storage.queue.core;

import com.azure.spring.messaging.AzureHeaders;
import com.azure.spring.messaging.PartitionSupplier;
import com.azure.spring.messaging.checkpoint.AzureCheckpointer;
import com.azure.spring.messaging.checkpoint.CheckpointMode;
import com.azure.spring.messaging.checkpoint.Checkpointer;
import com.azure.spring.storage.queue.support.StorageQueueHelper;
import com.azure.spring.storage.queue.support.converter.StorageQueueMessageConverter;
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
 * Azure Storage Queue template to support send / receive {@link Message} asynchronously.
 */
public class StorageQueueTemplate implements StorageQueueOperation {
    private static final Logger LOG = LoggerFactory.getLogger(StorageQueueTemplate.class);
    private static final Duration DEFAULT_VISIBILITY_TIMEOUT_IN_SECONDS = Duration.ofSeconds(30);
    private static final String MSG_FAIL_CHECKPOINT = "Failed to checkpoint %s in storage queue '%s'";
    private static final String MSG_SUCCESS_CHECKPOINT = "Checkpointed %s in storage queue '%s' in %s mode";
    private final StorageQueueClientFactory storageQueueClientFactory;

    private StorageQueueMessageConverter messageConverter = new StorageQueueMessageConverter();

    private Duration visibilityTimeout = DEFAULT_VISIBILITY_TIMEOUT_IN_SECONDS;

    private Class<?> messagePayloadType = byte[].class;

    private CheckpointMode checkpointMode = CheckpointMode.RECORD;

    /**
     * Create an instance using the supplied StorageQueueClientFactory.
     * @param storageQueueClientFactory the StorageQueueClientFactory.
     */
    public StorageQueueTemplate(@NonNull StorageQueueClientFactory storageQueueClientFactory) {
        this.storageQueueClientFactory = storageQueueClientFactory;
        LOG.info("StorageQueueTemplate started with properties {}", buildProperties());
    }

    @Override
    public <T> Mono<Void> sendAsync(String queueName, @NonNull Message<T> message,
                                    PartitionSupplier partitionSupplier) {
        Assert.hasText(queueName, "queueName can't be null or empty");
        QueueMessageItem queueMessageItem = messageConverter.fromMessage(message, QueueMessageItem.class);
        QueueAsyncClient queueClient = storageQueueClientFactory.getOrCreateQueueClient(queueName);
        Assert.notNull(queueMessageItem, "queueMessageItem can't be null");
        return queueClient.sendMessage(queueMessageItem.getMessageText()).then();
    }

    @Override
    public Mono<Message<?>> receiveAsync(String queueName) {
        return this.receiveAsync(queueName, visibilityTimeout);
    }

    private Mono<Message<?>> receiveAsync(String queueName, Duration visibilityTimeout) {
        Assert.hasText(queueName, "queueName can't be null or empty");


        QueueAsyncClient queueClient = storageQueueClientFactory.getOrCreateQueueClient(queueName);


        return queueClient.receiveMessages(1, visibilityTimeout)
            .next()
            .map(messageItem -> {

                Map<String, Object> headers = new HashMap<>();
                Checkpointer checkpointer = new AzureCheckpointer(() -> checkpoint(queueClient, messageItem));

                if (checkpointMode == CheckpointMode.RECORD) {
                    checkpointer.success().subscribe();
                } else if (checkpointMode == CheckpointMode.MANUAL) {
                    headers.put(AzureHeaders.CHECKPOINTER, checkpointer);
                }

                return messageConverter.toMessage(messageItem, new MessageHeaders(headers), messagePayloadType);
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

        properties.put("visibilityTimeout", this.visibilityTimeout);
        properties.put("messagePayloadType", this.messagePayloadType);
        properties.put("checkpointMode", this.checkpointMode);

        return properties;
    }

    private boolean isValidCheckpointMode(CheckpointMode checkpointMode) {
        return checkpointMode == CheckpointMode.MANUAL || checkpointMode == CheckpointMode.RECORD;
    }

    private String buildCheckpointFailMessage(QueueMessageItem cloudQueueMessage, String queueName) {
        return String.format(MSG_FAIL_CHECKPOINT, StorageQueueHelper.toString(cloudQueueMessage), queueName);
    }

    private String buildCheckpointSuccessMessage(QueueMessageItem cloudQueueMessage, String queueName) {
        return String.format(MSG_SUCCESS_CHECKPOINT, StorageQueueHelper.toString(cloudQueueMessage), queueName,
            checkpointMode);
    }

    /**
     * Get the {@code StorageQueueMessageConverter}.
     * @return the StorageQueueMessageConverter.
     */
    public StorageQueueMessageConverter getMessageConverter() {
        return messageConverter;
    }

    /**
     * Set the {@code StorageQueueMessageConverter}.
     * @param messageConverter the StorageQueueMessageConverter.
     */
    public void setMessageConverter(StorageQueueMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Get the {@code visibilityTimeoutInSeconds}.
     * @return the visibilityTimeoutInSeconds.
     */
    public Duration getVisibilityTimeout() {
        return visibilityTimeout;
    }

    @Override
    public void setVisibilityTimeout(Duration visibilityTimeoutDuration) {
        Assert.state(visibilityTimeoutDuration.isNegative() || visibilityTimeoutDuration.isZero(), "VisibilityTimeoutInSeconds should be positive");
        this.visibilityTimeout = visibilityTimeoutDuration;
        LOG.info("StorageQueueTemplate VisibilityTimeout becomes: {} seconds", this.visibilityTimeout.getSeconds());
    }

    /**
     * Get the {@code messagePayloadType}.
     * @return the messagePayloadType.
     */
    public Class<?> getMessagePayloadType() {
        return messagePayloadType;
    }

    @Override
    public void setMessagePayloadType(Class<?> payloadType) {
        this.messagePayloadType = payloadType;
        LOG.info("StorageQueueTemplate messagePayloadType becomes: {}", this.messagePayloadType);
    }

    /**
     * Get the {@code checkpointMode}.
     * @return the {@code checkpointMode}.
     */
    public CheckpointMode getCheckpointMode() {
        return checkpointMode;
    }

    @Override
    public void setCheckpointMode(CheckpointMode checkpointMode) {
        Assert.state(isValidCheckpointMode(checkpointMode),
            "Only MANUAL or RECORD checkpoint mode is supported in StorageQueueTemplate");
        this.checkpointMode = checkpointMode;
        LOG.info("StorageQueueTemplate checkpoint mode becomes: {}", this.checkpointMode);
    }
}
