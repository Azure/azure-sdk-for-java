// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.servicebus;

import com.microsoft.azure.servicebus.ExceptionPhase;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageHandler;
import com.microsoft.azure.spring.integration.core.AzureCheckpointer;
import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.api.CheckpointConfig;
import com.microsoft.azure.spring.integration.core.api.CheckpointMode;
import com.microsoft.azure.spring.integration.core.api.Checkpointer;
import com.microsoft.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class ServiceBusMessageHandler<U> implements IMessageHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceBusMessageHandler.class);
    protected final Consumer<Message<U>> consumer;
    protected final Class<U> payloadType;
    protected final CheckpointConfig checkpointConfig;
    protected final ServiceBusMessageConverter messageConverter;

    public ServiceBusMessageHandler(Consumer<Message<U>> consumer, Class<U> payloadType,
                                    CheckpointConfig checkpointConfig, ServiceBusMessageConverter messageConverter) {
        this.consumer = consumer;
        this.payloadType = payloadType;
        this.checkpointConfig = checkpointConfig;
        this.messageConverter = messageConverter;
    }

    @Override
    public CompletableFuture<Void> onMessageAsync(IMessage serviceBusMessage) {
        Map<String, Object> headers = new HashMap<>();

        Checkpointer checkpointer = new AzureCheckpointer(() -> this.success(serviceBusMessage.getLockToken()),
            () -> this.failure(serviceBusMessage.getLockToken()));
        if (checkpointConfig.getCheckpointMode() == CheckpointMode.MANUAL) {
            headers.put(AzureHeaders.CHECKPOINTER, checkpointer);
        }

        Message<U> message = messageConverter.toMessage(serviceBusMessage, new MessageHeaders(headers), payloadType);
        consumer.accept(message);

        if (checkpointConfig.getCheckpointMode() == CheckpointMode.RECORD) {
            return checkpointer.success().whenComplete((v, t) -> checkpointHandler(message, t));
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void notifyException(Throwable exception, ExceptionPhase phase) {
        LOG.error(String.format("Exception encountered in phase %s", phase), exception);
    }

    protected abstract CompletableFuture<Void> success(UUID uuid);

    protected abstract CompletableFuture<Void> failure(UUID uuid);

    protected abstract String buildCheckpointFailMessage(Message<?> message);

    protected abstract String buildCheckpointSuccessMessage(Message<?> message);

    protected void checkpointHandler(Message<?> message, Throwable t) {
        if (t != null) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(buildCheckpointFailMessage(message), t);
            }
        } else if (LOG.isDebugEnabled()) {
            LOG.debug(buildCheckpointSuccessMessage(message));
        }
    }
}
