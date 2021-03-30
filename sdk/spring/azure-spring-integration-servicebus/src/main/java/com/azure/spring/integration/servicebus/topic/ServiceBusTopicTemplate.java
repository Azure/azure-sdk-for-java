// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.topic;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.integration.servicebus.*;
import com.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import com.google.common.collect.Sets;
import com.azure.spring.cloud.context.core.util.Tuple;
import com.azure.spring.integration.servicebus.factory.ServiceBusTopicClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
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

    private final Set<Tuple<String, String>> nameAndConsumerGroups = Sets.newConcurrentHashSet();
    private String topicName;
    private String subscriptionName;

    public ServiceBusTopicTemplate(ServiceBusTopicClientFactory clientFactory,
                                   ServiceBusMessageConverter messageConverter) {
        super(clientFactory, messageConverter);
    }

    @Override
    public boolean subscribe(String destination, String consumerGroup, @NonNull Consumer<Message<?>> consumer,
                             Class<?> payloadType) {
        Assert.hasText(destination, "destination can't be null or empty");

        Tuple<String, String> nameAndConsumerGroup = Tuple.of(destination, consumerGroup);

        if (nameAndConsumerGroups.contains(nameAndConsumerGroup)) {
            return false;
        }

        nameAndConsumerGroups.add(nameAndConsumerGroup);

        internalSubscribe(destination, consumerGroup, consumer, payloadType);
        return true;
    }

    @Override
    public boolean unsubscribe(String destination, String consumerGroup) {
        // TODO: unregister message handler but service bus sdk unsupported

        return nameAndConsumerGroups.remove(Tuple.of(destination, consumerGroup));
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
    protected void internalSubscribe(String name, String consumerGroup, Consumer<Message<?>> consumer,
                                     Class<?> payloadType) {
        this.topicName = name;
        this.subscriptionName = consumerGroup;
        InboundServiceBusMessageConsumer processMessage = new InboundServiceBusMessageConsumer(name, consumerGroup, checkpointConfig, messageConverter, consumer, payloadType);
        Consumer<ServiceBusErrorContext> processError = errorContext -> {
             //TODO is this consumer useful?

        };


        ServiceBusProcessorClient processorClient;

        if (this.clientConfig.isSessionsEnabled()) {
            processorClient = new ServiceBusClientBuilder()
                .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
                .sessionProcessor()
                .topicName(name)
                .subscriptionName(consumerGroup)
                .maxConcurrentSessions(3)
                .processMessage(processMessage)
                .processError(processError)
                .buildProcessorClient();
        } else {
            processorClient = new ServiceBusClientBuilder()
                .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
                .processor()
                .topicName(name)
                .subscriptionName(consumerGroup)
                .processMessage(processMessage)
                .processError(processError)
                .buildProcessorClient();

        }  //TODO need remove this logic of instantiating processor client into DefaultServiceBusTopicClientFactory.
        processorClient.start();
    }

    @Override
    public void setClientConfig(@NonNull ServiceBusClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }



}
