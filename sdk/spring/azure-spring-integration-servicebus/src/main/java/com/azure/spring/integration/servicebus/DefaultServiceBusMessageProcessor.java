// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus;


import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.spring.integration.core.api.CheckpointConfig;
import com.azure.spring.integration.core.api.CheckpointMode;
import com.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Message processor for Service Bus.
 */
public class DefaultServiceBusMessageProcessor
    implements ServiceBusMessageProcessor<ServiceBusReceivedMessageContext, ServiceBusErrorContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServiceBusMessageProcessor.class);
    private static final String MSG_FAIL_CHECKPOINT = "Consumer group '%s' of topic '%s' failed to checkpoint %s";
    private static final String MSG_SUCCESS_CHECKPOINT = "Consumer group '%s' of topic '%s' checkpointed %s in %s mode";

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

            if (this.checkpointConfig.getCheckpointMode() == CheckpointMode.MANUAL) {
                headers.put(ServiceBusMessageHeaders.RECEIVED_MESSAGE_CONTEXT, context);
            }

            Message<?> message = messageConverter.toMessage(context.getMessage(),
                                                            new MessageHeaders(headers),
                                                            payloadType);
            consumer.accept(message);

            if (this.checkpointConfig.getCheckpointMode() == CheckpointMode.RECORD) {
                context.complete();
            }
        };
    }


}
