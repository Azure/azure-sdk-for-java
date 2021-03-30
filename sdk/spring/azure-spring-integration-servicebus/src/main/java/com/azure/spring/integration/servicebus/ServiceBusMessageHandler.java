// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus;


import com.azure.spring.integration.core.AzureCheckpointer;
import com.azure.spring.integration.core.AzureHeaders;
import com.azure.spring.integration.core.api.CheckpointConfig;
import com.azure.spring.integration.core.api.CheckpointMode;
import com.azure.spring.integration.core.api.Checkpointer;
import com.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Message handler for Service Bus.
 * @param <U> The type of message payload.
 */

//TODO. This class may need to be obsoleted .  we don't need message handler anymore, its duty is removed to InboundServiceBusMessageConsumer.java
public abstract class ServiceBusMessageHandler<U> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusMessageHandler.class);
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





    protected abstract CompletableFuture<Void> success(UUID uuid);

    protected abstract CompletableFuture<Void> failure(UUID uuid);

    protected abstract String buildCheckpointFailMessage(Message<?> message);

    protected abstract String buildCheckpointSuccessMessage(Message<?> message);

    protected void checkpointHandler(Message<?> message, Throwable t) {
        if (t != null) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(buildCheckpointFailMessage(message), t);
            }
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(buildCheckpointSuccessMessage(message));
        }
    }
}
