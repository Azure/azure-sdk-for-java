// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.queue;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.spring.integration.core.AzureCheckpointer;
import com.azure.spring.integration.core.AzureHeaders;
import com.azure.spring.integration.core.api.CheckpointMode;
import com.azure.spring.integration.core.api.Checkpointer;
import com.azure.spring.integration.servicebus.*;
import com.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import com.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

import java.util.Set;
import java.util.function.Consumer;

/**
 * Default implementation of {@link ServiceBusQueueOperation}.
 *
 * @author Warren Zhu
 * @author Eduardo Sciullo
 */
public class ServiceBusQueueTemplate extends ServiceBusTemplate<ServiceBusQueueClientFactory>
    implements ServiceBusQueueOperation {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusQueueTemplate.class);

    private static final String MSG_FAIL_CHECKPOINT = "Failed to checkpoint %s in queue '%s'";

    private static final String MSG_SUCCESS_CHECKPOINT = "Checkpointed %s in queue '%s' in %s mode";

    private final Set<String> subscribedQueues = Sets.newConcurrentHashSet();

    private String queueName;

    public ServiceBusQueueTemplate(ServiceBusQueueClientFactory clientFactory,
                                   ServiceBusMessageConverter messageConverter) {
        super(clientFactory, messageConverter);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean subscribe(String destination,
                             @NonNull Consumer<Message<?>> consumer,
                             @NonNull Class<?> targetPayloadClass) {
        Assert.hasText(destination, "destination can't be null or empty");

        if (subscribedQueues.contains(destination)) {
            return false;
        }

        subscribedQueues.add(destination);

        internalSubscribe(destination, consumer, targetPayloadClass);

        return true;
    }

    @Override
    public boolean unsubscribe(String destination) {
        // TODO: unregister message handler but service bus sdk unsupported

        return subscribedQueues.remove(destination);
    }

    @Override
    public <T> void deadLetter(String destination, Message<T> message, String deadLetterReason,
                               String deadLetterErrorDescription) {
        Assert.hasText(destination, "destination can't be null or empty");
        Object lockToken = message.getHeaders().get(AzureHeaders.LOCK_TOKEN);
        if (lockToken != null) {
            Checkpointer ci = (Checkpointer) message.getHeaders().get(AzureHeaders.CHECKPOINTER);
            ci.failure();// TODO: if deadLetter and abandon invokes the same method failure(), in failure, need to distinguish deadLetter and abandon.
            // TODO  Currently, Checkpointer only has two methods exposed: success() failure(), all parameterless.  We can use success to complete a message
            // use failure to abandon a message, but we have no method to deadletter a message.
        } else {
            LOGGER.error("Failed to send message to dead letter queue");
            throw new ServiceBusRuntimeException("Failed to send message to dead letter queue");
        }
    }

    @Override
    public <T> void abandon(String destination, Message<T> message) {
        Assert.hasText(destination, "destination can't be null or empty");
        Object lockToken = message.getHeaders().get(AzureHeaders.LOCK_TOKEN);

        if (lockToken != null) {
           Checkpointer ci = (Checkpointer) message.getHeaders().get(AzureHeaders.CHECKPOINTER);
           ci.failure();

        } else {
            LOGGER.error("Failed to send message to dead letter queue");
            throw new ServiceBusRuntimeException("Failed to send message to dead letter queue");
        }
    }

    /**
     * Register a message handler to receive message from the queue. A session handler will be registered if session is
     * enabled.
     *
     * @param name        The queue name.
     * @param consumer    The consumer method.
     * @param payloadType The type of the message payload.
     * @throws ServiceBusRuntimeException If fail to register the queue message handler.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void internalSubscribe(String name, Consumer<Message<?>> consumer, Class<?> payloadType) {
        this.queueName = name;
        InboundServiceBusMessageConsumer processMessage = new InboundServiceBusMessageConsumer(name, null, checkpointConfig, messageConverter, consumer, payloadType);
        Consumer<ServiceBusErrorContext> processError = errorContext -> {
           //TODO   is this consumer useful?
        };
        ServiceBusProcessorClient processorClient = this.senderFactory.getOrCreateClient(name, clientConfig, processMessage, processError);
        processorClient.start();
    }





    @Override
    public void setClientConfig(@NonNull ServiceBusClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }



}
