// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.storage.queue.core;

import com.azure.storage.queue.QueueAsyncClient;
import com.azure.storage.queue.models.QueueMessageItem;
import com.azure.storage.queue.models.QueueStorageException;
import com.azure.spring.messaging.AzureHeaders;
import com.azure.spring.messaging.checkpoint.CheckpointMode;
import com.azure.spring.messaging.PartitionSupplier;
import com.azure.spring.messaging.checkpoint.reactor.AzureCheckpointer;
import com.azure.spring.messaging.checkpoint.reactor.Checkpointer;
import com.azure.spring.storage.queue.support.converter.StorageQueueMessageConverter;
import com.azure.spring.storage.queue.support.StorageQueueHelper;
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
    private static final int DEFAULT_VISIBILITY_TIMEOUT_IN_SECONDS = 30;
    private static final String MSG_FAIL_CHECKPOINT = "Failed to checkpoint %s in storage queue '%s'";
    private static final String MSG_SUCCESS_CHECKPOINT = "Checkpointed %s in storage queue '%s' in %s mode";
    private final StorageQueueClientFactory storageQueueClientFactory;

    protected StorageQueueMessageConverter messageConverter = new StorageQueueMessageConverter();

    private int visibilityTimeoutInSeconds = DEFAULT_VISIBILITY_TIMEOUT_IN_SECONDS;

    private Class<?> messagePayloadType = byte[].class;

    private CheckpointMode checkpointMode = CheckpointMode.RECORD;

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
        return this.receiveAsync(queueName, visibilityTimeoutInSeconds);
    }

    private Mono<Message<?>> receiveAsync(String queueName, int visibilityTimeoutInSeconds) {
        Assert.hasText(queueName, "queueName can't be null or empty");


        QueueAsyncClient queueClient = storageQueueClientFactory.getOrCreateQueueClient(queueName);


        return queueClient.receiveMessages(1, Duration.ofSeconds(visibilityTimeoutInSeconds))
            .onErrorMap(QueueStorageException.class, e ->
                new StorageQueueRuntimeException("Failed to send message to storage queue", e))
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

        properties.put("visibilityTimeout", this.visibilityTimeoutInSeconds);
        properties.put("messagePayloadType", this.messagePayloadType);
        properties.put("checkpointMode", this.checkpointMode);

        return properties;
    }

    private boolean isValidCheckpointMode(CheckpointMode checkpointMode) {
        return checkpointMode == CheckpointMode.MANUAL || checkpointMode == CheckpointMode.RECORD;
    }

    public void checkpointHandler(QueueMessageItem message, String queueName, Throwable t) {
        if (t != null) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(buildCheckpointFailMessage(message, queueName), t);
            }
        } else if (LOG.isDebugEnabled()) {
            LOG.debug(buildCheckpointSuccessMessage(message, queueName));
        }
    }

    private String buildCheckpointFailMessage(QueueMessageItem cloudQueueMessage, String queueName) {
        return String.format(MSG_FAIL_CHECKPOINT, StorageQueueHelper.toString(cloudQueueMessage), queueName);
    }

    private String buildCheckpointSuccessMessage(QueueMessageItem cloudQueueMessage, String queueName) {
        return String.format(MSG_SUCCESS_CHECKPOINT, StorageQueueHelper.toString(cloudQueueMessage), queueName,
            checkpointMode);
    }

    public StorageQueueMessageConverter getMessageConverter() {
        return messageConverter;
    }

    public void setMessageConverter(StorageQueueMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    public int getVisibilityTimeoutInSeconds() {
        return visibilityTimeoutInSeconds;
    }

    @Override
    public void setVisibilityTimeoutInSeconds(int timeout) {
        Assert.state(timeout > 0, "VisibilityTimeoutInSeconds should be positive");
        this.visibilityTimeoutInSeconds = timeout;
        LOG.info("StorageQueueTemplate VisibilityTimeoutInSeconds becomes: {}", this.visibilityTimeoutInSeconds);
    }

    public Class<?> getMessagePayloadType() {
        return messagePayloadType;
    }

    @Override
    public void setMessagePayloadType(Class<?> payloadType) {
        this.messagePayloadType = payloadType;
        LOG.info("StorageQueueTemplate messagePayloadType becomes: {}", this.messagePayloadType);
    }

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
