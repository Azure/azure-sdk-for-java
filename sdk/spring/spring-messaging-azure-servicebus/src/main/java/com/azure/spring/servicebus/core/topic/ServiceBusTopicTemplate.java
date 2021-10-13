// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.topic;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.servicebus.core.DefaultServiceBusMessageProcessor;
import com.azure.spring.servicebus.support.ServiceBusClientConfig;
import com.azure.spring.servicebus.support.ServiceBusRuntimeException;
import com.azure.spring.servicebus.core.ServiceBusTemplate;
import com.azure.spring.servicebus.support.converter.ServiceBusMessageConverter;
import com.azure.spring.servicebus.core.ServiceBusTopicClientFactory;
import com.azure.spring.servicebus.health.Instrumentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

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

    private final Set<Tuple2<String, String>> nameAndConsumerGroups = ConcurrentHashMap.newKeySet();

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

        Tuple2<String, String> nameAndConsumerGroup = Tuples.of(destination, consumerGroup);

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

        return this.nameAndConsumerGroups.remove(Tuples.of(destination, consumerGroup));
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
            protected void logCheckpointFail(Message<?> message, Throwable t) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn(String.format(MSG_FAIL_CHECKPOINT, consumer, name, message), t);
                }
            }

            @Override
            protected void logCheckpointSuccess(Message<?> message) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format(MSG_SUCCESS_CHECKPOINT, consumer, name, message,
                        getCheckpointConfig().getCheckpointMode()));
                }
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
            throw new ServiceBusRuntimeException("ServiceBus processor client startup failed, Caused by " + e.getMessage(), e);
        }
    }


}
