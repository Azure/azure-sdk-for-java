// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.queue;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.spring.servicebus.core.DefaultServiceBusMessageProcessor;
import com.azure.spring.servicebus.support.ServiceBusClientConfig;
import com.azure.spring.servicebus.support.ServiceBusRuntimeException;
import com.azure.spring.servicebus.core.ServiceBusTemplate;
import com.azure.spring.servicebus.support.converter.ServiceBusMessageConverter;
import com.azure.spring.servicebus.support.converter.ServiceBusMessageHeaders;
import com.azure.spring.servicebus.core.ServiceBusQueueClientFactory;
import com.azure.spring.servicebus.health.Instrumentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Default implementation of {@link ServiceBusQueueOperation}.
 */
public class ServiceBusQueueTemplate extends ServiceBusTemplate<ServiceBusQueueClientFactory>
    implements ServiceBusQueueOperation {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusQueueTemplate.class);

    private static final String MSG_FAIL_CHECKPOINT = "Failed to checkpoint %s in queue '%s'";

    private static final String MSG_SUCCESS_CHECKPOINT = "Checkpointed %s in queue '%s' in %s mode";

    private final Set<String> subscribedQueues = ConcurrentHashMap.newKeySet();

    public ServiceBusQueueTemplate(ServiceBusQueueClientFactory clientFactory) {
        super(clientFactory);
    }

    public ServiceBusQueueTemplate(ServiceBusQueueClientFactory clientFactory,
                                   ServiceBusMessageConverter messageConverter) {
        super(clientFactory, messageConverter);
    }

    /**
     * Register a message handler to receive message from the queue. A session handler will be registered if session is
     * enabled.
     *
     * @param name The queue name.
     * @param consumer The consumer method.
     * @param payloadType The type of the message payload.
     * @throws ServiceBusRuntimeException If fail to register the queue message handler.
     */
    protected void internalSubscribe(String name, Consumer<Message<?>> consumer, Class<?> payloadType) {
        final DefaultServiceBusMessageProcessor messageProcessor = new DefaultServiceBusMessageProcessor(
            this.checkpointConfig, payloadType, consumer, this.messageConverter) {

            @Override
            protected void buildCheckpointFailMessage(Message<?> message, Throwable t) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn(String.format(MSG_FAIL_CHECKPOINT, message, name), t);
                }
            }

            @Override
            protected void buildCheckpointSuccessMessage(Message<?> message) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format(MSG_SUCCESS_CHECKPOINT, message, name, getCheckpointConfig().getCheckpointMode()));
                }
            }
        };
        Instrumentation instrumentation = new Instrumentation(name, Instrumentation.Type.CONSUME);
        try {
            instrumentationManager.addHealthInstrumentation(instrumentation);
            ServiceBusProcessorClient processorClient = this.clientFactory.getOrCreateProcessor(name, clientConfig,
                messageProcessor);
            processorClient.start();
            instrumentationManager.getHealthInstrumentation(instrumentation).markStartedSuccessfully();
        } catch (Exception e) {
            instrumentationManager.getHealthInstrumentation(instrumentation).markStartFailed(e);
            LOGGER.error("ServiceBus processorClient startup failed, Caused by " + e.getMessage());
            throw new ServiceBusRuntimeException("ServiceBus processor client startup failed, Caused by " + e.getMessage(), e);
        }
    }

    @Override
    public void setClientConfig(@NonNull ServiceBusClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    @Override
    public <T> void deadLetter(String destination,
                               Message<T> message,
                               String deadLetterReason,
                               String deadLetterErrorDescription) {
        Assert.hasText(destination, "destination can't be null or empty");


        final ServiceBusReceivedMessageContext messageContext = (ServiceBusReceivedMessageContext) message.getHeaders()
                                                                                                          .get(
                                                                                                              ServiceBusMessageHeaders.RECEIVED_MESSAGE_CONTEXT);

        if (messageContext != null) {
            messageContext.deadLetter();
        }
    }

    @Override
    public <T> void abandon(String destination, Message<T> message) {
        Assert.hasText(destination, "destination can't be null or empty");


        final ServiceBusReceivedMessageContext messageContext = (ServiceBusReceivedMessageContext) message.getHeaders()
                                                                                                          .get(
                                                                                                              ServiceBusMessageHeaders.RECEIVED_MESSAGE_CONTEXT);

        if (messageContext != null) {
            messageContext.abandon();
        }
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

}
