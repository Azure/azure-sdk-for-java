// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.factory;


import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.spring.cloud.context.core.util.Memoizer;
import com.azure.spring.integration.servicebus.ServiceBusClientConfig;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Default implementation of {@link ServiceBusQueueClientFactory}. Client will be cached to improve performance
 *
 * @author Warren Zhu
 */
//TODO: The logic of instantiating queue processor client needs to be put in this class
public class DefaultServiceBusQueueClientFactory extends AbstractServiceBusSenderFactory
    implements ServiceBusQueueClientFactory {

    private ServiceBusClientConfig clientConfig;
    private Consumer processMessage;
    private Consumer processError;

    private final Function<String, ServiceBusProcessorClient> queueClientCreator = Memoizer.memoize(this::createQueueClient);
    private final Function<String, ServiceBusSenderClient> queueSenderCreator = Memoizer.memoize(this::createQueueSender);
    public DefaultServiceBusQueueClientFactory(String connectionString) {
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
            .queueName(name)
            .buildClient();
        return sender;
    }

    private ServiceBusProcessorClient  createQueueClient(String name){
        ServiceBusProcessorClient processorClient;
        if (clientConfig.isSessionsEnabled()) {
            processorClient = new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .sessionProcessor()
                .queueName(name)
                .maxConcurrentCalls(1) // TODO, make it a constant or get it from clientConfig. And it looks like max auto renew duration is not exposed
                .maxConcurrentSessions(clientConfig.getConcurrency())
                .prefetchCount(clientConfig.getPrefetchCount())
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
                .queueName(name)
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
    public ServiceBusProcessorClient getOrCreateClient(String name, ServiceBusClientConfig clientConfig, Consumer processMessage, Consumer processError) {
        this.clientConfig = clientConfig;
        this.processMessage = processMessage;
        this.processError = processError;
        return this.queueClientCreator.apply(name);
    }
}
