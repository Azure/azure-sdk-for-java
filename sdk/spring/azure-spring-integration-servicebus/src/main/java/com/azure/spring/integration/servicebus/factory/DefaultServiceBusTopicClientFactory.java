// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.factory;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespace;
import com.azure.resourcemanager.servicebus.models.Topic;
import com.azure.spring.cloud.context.core.util.Memoizer;
import com.azure.spring.cloud.context.core.util.Tuple;
import com.azure.spring.integration.servicebus.ServiceBusClientConfig;
import com.azure.spring.integration.servicebus.ServiceBusRuntimeException;
import org.springframework.util.StringUtils;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Default implementation of {@link ServiceBusTopicClientFactory}. Client will
 * be cached to improve performance
 *
 * @author Warren Zhu
 */
//TODO: The logic of instantiating topic processor client needs to be put in this class
public class DefaultServiceBusTopicClientFactory extends AbstractServiceBusSenderFactory
    implements ServiceBusTopicClientFactory {

    private ServiceBusClientConfig clientConfig;
    private Consumer processMessage;
    private Consumer processError;

    private final BiFunction<String, String, ServiceBusProcessorClient> subscriptionClientCreator = Memoizer
        .memoize(this::createTopicClient);
    private final Function<String, ServiceBusSenderClient> queueSenderCreator = Memoizer.memoize(this::createQueueSender);
    public DefaultServiceBusTopicClientFactory(String connectionString) {
        super(connectionString);
    }

    @Override
    public ServiceBusSenderClient getOrCreateSender(String name) {
        return this.queueSenderCreator.apply(name);
    }


    private ServiceBusSenderClient createQueueSender(String name){
        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sender()
            .topicName(name)
            .buildClient();
        return sender;
    }

    private ServiceBusProcessorClient  createTopicClient(String name, String subscription){
        ServiceBusProcessorClient processorClient;
        if (clientConfig.isSessionsEnabled()) {
            processorClient = new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .sessionProcessor()
                .topicName(name)
                .subscriptionName(subscription)
                .prefetchCount(clientConfig.getPrefetchCount())
                .maxConcurrentCalls(1) // TODO, make it a constant or get it from clientConfig. And it looks like max auto renew duration is not exposed
                .maxConcurrentSessions(clientConfig.getConcurrency())
                .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
                .disableAutoComplete()
                .processMessage(processMessage)
                .processError(processError)
                .buildProcessorClient();
        } else {
            processorClient = new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .processor()
                .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
                .topicName(name)
                .subscriptionName(subscription)
                .prefetchCount(clientConfig.getPrefetchCount())
                .maxConcurrentCalls(clientConfig.getConcurrency())
                .disableAutoComplete()
                .processMessage(processMessage)
                .processError(processError)
                .buildProcessorClient();

        }
        return processorClient;

    }
    @Override
    public ServiceBusProcessorClient getOrCreateClient(String name, String subscription, ServiceBusClientConfig clientConfig, Consumer processMessage, Consumer processError) {
        this.clientConfig = clientConfig;
        this.processMessage = processMessage;
        this.processError = processError;
        return this.subscriptionClientCreator.apply(name, subscription);
    }
}
