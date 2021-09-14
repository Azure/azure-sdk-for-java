// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core;


import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.spring.messaging.checkpoint.AzureCheckpointer;
import com.azure.spring.messaging.AzureHeaders;
import com.azure.spring.messaging.checkpoint.CheckpointConfig;
import com.azure.spring.messaging.checkpoint.CheckpointMode;
import com.azure.spring.messaging.checkpoint.Checkpointer;
import com.azure.spring.servicebus.support.converter.ServiceBusMessageConverter;
import com.azure.spring.servicebus.support.converter.ServiceBusMessageHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Message processor for Service Bus.
 */
public class DefaultServiceBusMessageProcessor
    implements ServiceBusMessageProcessor<ServiceBusReceivedMessageContext, ServiceBusErrorContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServiceBusMessageProcessor.class);
    private static final String MSG_FAIL_CHECKPOINT = "Failed to checkpoint %s";
    private static final String MSG_SUCCESS_CHECKPOINT = "Checkpointed %s in %s mode";

    private final CheckpointConfig checkpointConfig;
    private final Class<?> payloadType;
    private final Consumer<Message<?>> consumer;
    private final ServiceBusMessageConverter messageConverter;

    public DefaultServiceBusMessageProcessor(CheckpointConfig checkpointConfig,
                                             Class<?> payloadType,
                                             Consumer<Message<?>> consumer,
                                             ServiceBusMessageConverter messageConverter) {
        this.consumer = consumer;
        this.payloadType = payloadType;
        this.checkpointConfig = checkpointConfig;
        this.messageConverter = messageConverter;
    }

    public Consumer<ServiceBusErrorContext> processError() {
        return serviceBusErrorContext -> {
            // TODO
        };
    }

    public Consumer<ServiceBusReceivedMessageContext> processMessage() {
        return context -> {
            Map<String, Object> headers = new HashMap<>();

            Checkpointer checkpointer = new AzureCheckpointer(() -> success(context), () -> fail(context));
            headers.put(ServiceBusMessageHeaders.RECEIVED_MESSAGE_CONTEXT, context);

            if (this.checkpointConfig.getCheckpointMode() == CheckpointMode.MANUAL) {
                headers.put(AzureHeaders.CHECKPOINTER, checkpointer);
            }

            Message<?> message = messageConverter.toMessage(context.getMessage(), new MessageHeaders(headers),
                                                            payloadType);
            consumer.accept(message);

            if (this.checkpointConfig.getCheckpointMode() == CheckpointMode.RECORD) {
                checkpointer.success().whenComplete((v, t) -> checkpointHandler(message, t));
            }
        };
    }

    private CompletableFuture<Void> success(ServiceBusReceivedMessageContext context) {
        return CompletableFuture.runAsync(context::complete);
    }

    private CompletableFuture<Void> fail(ServiceBusReceivedMessageContext context) {
        return CompletableFuture.runAsync(context::abandon);
    }

    private void checkpointHandler(Message<?> message, Throwable t) {
        if (t != null) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(buildCheckpointFailMessage(message), t);
            }
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(buildCheckpointSuccessMessage(message));
        }
    }

    protected String buildCheckpointFailMessage(Message<?> message) {
        return String.format(MSG_FAIL_CHECKPOINT, message);
    }

    protected String buildCheckpointSuccessMessage(Message<?> message) {
        return String.format(MSG_SUCCESS_CHECKPOINT, message, this.checkpointConfig.getCheckpointMode());
    }

}
