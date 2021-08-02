// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.factory;


import com.azure.core.amqp.AmqpTransportType;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.spring.integration.servicebus.ServiceBusClientConfig;
import com.azure.spring.integration.servicebus.ServiceBusMessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Default implementation of {@link ServiceBusQueueClientFactory}. Client will be cached to improve performance
 *
 * @author Warren Zhu
 */
public class DefaultServiceBusQueueClientFactory extends AbstractServiceBusSenderFactory
    implements ServiceBusQueueClientFactory, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServiceBusQueueClientFactory.class);
    private final Map<String, ServiceBusProcessorClient> processorClientMap = new ConcurrentHashMap<>();
    private final Map<String, ServiceBusSenderAsyncClient> senderClientMap = new ConcurrentHashMap<>();

    // TODO (xiada) whether will this reuse the underlying connection?
    private final ServiceBusClientBuilder serviceBusClientBuilder;

    public DefaultServiceBusQueueClientFactory(String connectionString) {
        this(connectionString, AmqpTransportType.AMQP);
    }

    public DefaultServiceBusQueueClientFactory(String connectionString, AmqpTransportType amqpTransportType) {
        super(connectionString);
        this.serviceBusClientBuilder = new ServiceBusClientBuilder().connectionString(connectionString);
        this.serviceBusClientBuilder.transportType(amqpTransportType);
    }

    private <K, V> void close(Map<K, V> map, Consumer<V> close) {
        map.values().forEach(it -> {
            try {
                close.accept(it);
            } catch (Exception ex) {
                LOGGER.warn("Failed to clean service bus queue client factory", ex);
            }
        });
    }

    @Override
    public void destroy() {
        close(senderClientMap, ServiceBusSenderAsyncClient::close);
        close(processorClientMap, ServiceBusProcessorClient::close);
    }

    @Override
    public ServiceBusProcessorClient getOrCreateProcessor(
        String name,
        ServiceBusClientConfig clientConfig,
        ServiceBusMessageProcessor<ServiceBusReceivedMessageContext, ServiceBusErrorContext> messageProcessor) {
        return this.processorClientMap.computeIfAbsent(name,
            n -> createProcessorClient(n, clientConfig, messageProcessor));
    }

    @Override
    public ServiceBusSenderAsyncClient getOrCreateSender(String name) {
        return this.senderClientMap.computeIfAbsent(name, this::createQueueSender);
    }

    private ServiceBusProcessorClient createProcessorClient(
        String name,
        ServiceBusClientConfig clientConfig,
        ServiceBusMessageProcessor<ServiceBusReceivedMessageContext, ServiceBusErrorContext> messageProcessor) {
        if (clientConfig.isSessionsEnabled()) {
            return serviceBusClientBuilder.sessionProcessor()
                                          .queueName(name)
                                          .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
                                          .maxConcurrentCalls(1)
                                          // TODO, make it a constant or get it from clientConfig. And it looks like
                                          //  max auto renew duration is not exposed
                                          .maxConcurrentSessions(clientConfig.getConcurrency())
                                          .prefetchCount(clientConfig.getPrefetchCount())
                                          .disableAutoComplete()
                                          .processMessage(messageProcessor.processMessage())
                                          .processError(messageProcessor.processError())
                                          .buildProcessorClient();

        } else {
            return serviceBusClientBuilder.processor()
                                          .queueName(name)
                                          .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
                                          .maxConcurrentCalls(clientConfig.getConcurrency())
                                          .prefetchCount(clientConfig.getPrefetchCount())
                                          .disableAutoComplete()
                                          .processMessage(messageProcessor.processMessage())
                                          .processError(messageProcessor.processError())
                                          .buildProcessorClient();
        }

    }

    private ServiceBusSenderAsyncClient createQueueSender(String name) {
        return serviceBusClientBuilder.sender()
                                      .queueName(name)
                                      .buildAsyncClient();
    }
}
