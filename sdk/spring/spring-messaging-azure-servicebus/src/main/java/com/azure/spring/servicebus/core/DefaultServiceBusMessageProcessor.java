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
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Message processor for Service Bus.
 */
public class DefaultServiceBusMessageProcessor implements ServiceBusMessageProcessor {

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

            Checkpointer checkpointer = new AzureCheckpointer(() -> Mono.fromRunnable(context::complete),
                                                              () -> Mono.fromRunnable(context::abandon));
            headers.put(ServiceBusMessageHeaders.RECEIVED_MESSAGE_CONTEXT, context);

            if (this.checkpointConfig.getCheckpointMode() == CheckpointMode.MANUAL) {
                headers.put(AzureHeaders.CHECKPOINTER, checkpointer);
            }

            Message<?> message = messageConverter.toMessage(context.getMessage(), new MessageHeaders(headers),
                                                            payloadType);
            consumer.accept(message);

            if (this.checkpointConfig.getCheckpointMode() == CheckpointMode.RECORD) {
                checkpointer.success()
                            .doOnSuccess(t -> logCheckpointSuccess(message))
                            .doOnError(t -> logCheckpointFail(message, t))
                            .subscribe();
            }
        };
    }

    protected void logCheckpointFail(Message<?> message, Throwable t) {
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn(String.format(MSG_FAIL_CHECKPOINT, message), t);
        }
    }

    protected void logCheckpointSuccess(Message<?> message) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format(MSG_SUCCESS_CHECKPOINT, message, this.checkpointConfig.getCheckpointMode()));
        }
    }

}
