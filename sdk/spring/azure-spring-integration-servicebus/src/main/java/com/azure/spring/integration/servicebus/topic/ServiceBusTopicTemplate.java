// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.topic;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.cloud.context.core.util.Tuple;
import com.azure.spring.integration.servicebus.DefaultServiceBusMessageProcessor;
import com.azure.spring.integration.servicebus.ServiceBusClientConfig;
import com.azure.spring.integration.servicebus.ServiceBusRuntimeException;
import com.azure.spring.integration.servicebus.ServiceBusTemplate;
import com.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import com.azure.spring.integration.servicebus.factory.ServiceBusTopicClientFactory;
import com.azure.spring.integration.servicebus.health.Instrumentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.util.Assert;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Default implementation of {@link ServiceBusTopicOperation}.
 *
 * @author Warren Zhu
 * @author Eduardo Sciullo
 */
public class ServiceBusTopicTemplate extends ServiceBusTemplate<ServiceBusTopicClientFactory>
    implements ServiceBusTopicOperation {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusTopicTemplate.class);

    private static final String MSG_FAIL_CHECKPOINT = "Consumer group '%s' of topic '%s' failed to checkpoint %s";

    private static final String MSG_SUCCESS_CHECKPOINT = "Consumer group '%s' of topic '%s' checkpointed %s in %s mode";

    private final Set<Tuple<String, String>> nameAndConsumerGroups = ConcurrentHashMap.newKeySet();

    public ServiceBusTopicTemplate(ServiceBusTopicClientFactory clientFactory) {
        super(clientFactory);
    }

    public ServiceBusTopicTemplate(ServiceBusTopicClientFactory clientFactory,
                                   ServiceBusMessageConverter messageConverter) {
        super(clientFactory, messageConverter);
    }

    @Override
    public void setClientConfig(@NonNull ServiceBusClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    @Override
    public boolean subscribe(String destination,
                             String consumerGroup,
                             @NonNull Consumer<Message<?>> consumer,
                             Class<?> payloadType) {
        Assert.hasText(destination, "destination can't be null or empty");

        Tuple<String, String> nameAndConsumerGroup = Tuple.of(destination, consumerGroup);

        if (this.nameAndConsumerGroups.contains(nameAndConsumerGroup)) {
            return false;
        }

        this.nameAndConsumerGroups.add(nameAndConsumerGroup);

        internalSubscribe(destination, consumerGroup, consumer, payloadType);
        return true;
    }

    @Override
    public boolean unsubscribe(String destination, String consumerGroup) {
        // TODO: unregister message handler but service bus sdk unsupported

        return this.nameAndConsumerGroups.remove(Tuple.of(destination, consumerGroup));
    }

    /**
     * Register a message handler to receive message from the topic. A session handler will be registered if session is
     * enabled.
     *
     * @param name The topic name.
     * @param consumerGroup The consumer group.
     * @param consumer The consumer method.
     * @param payloadType The type of the message payload.
     * @throws ServiceBusRuntimeException If fail to register the topic message handler.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void internalSubscribe(String name,
                                     String consumerGroup,
                                     Consumer<Message<?>> consumer,
                                     Class<?> payloadType) {

        final DefaultServiceBusMessageProcessor messageProcessor = new DefaultServiceBusMessageProcessor(
            this.checkpointConfig, payloadType, consumer, this.messageConverter) {
            @Override
            protected String buildCheckpointFailMessage(Message<?> message) {
                return String.format(MSG_FAIL_CHECKPOINT, consumer, name, message);
            }

            @Override
            protected String buildCheckpointSuccessMessage(Message<?> message) {
                return String.format(MSG_SUCCESS_CHECKPOINT, consumer, name, message,
                    getCheckpointConfig().getCheckpointMode());
            }
        };
        Instrumentation instrumentation = new Instrumentation(name + consumerGroup, Instrumentation.Type.CONSUME);
        try {
            instrumentationManager.addHealthInstrumentation(instrumentation);
            ServiceBusProcessorClient processorClient = this.clientFactory.getOrCreateProcessor(name, consumerGroup,
                this.clientConfig, messageProcessor);
            processorClient.start();
            instrumentationManager.getHealthInstrumentation(instrumentation).markStartedSuccessfully();
        } catch (Exception e) {
            instrumentationManager.getHealthInstrumentation(instrumentation).markStartFailed(e);
            LOGGER.error("ServiceBus processorClient startup failed, Caused by " + e.getMessage());
        }
    }


}
