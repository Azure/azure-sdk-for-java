// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.factory;


import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.util.ClientOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.spring.integration.servicebus.ServiceBusClientConfig;
import com.azure.spring.integration.servicebus.ServiceBusMessageProcessor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.spring.cloud.context.core.util.Constants.SPRING_SERVICE_BUS_APPLICATION_ID;

/**
 * Default implementation of {@link ServiceBusQueueClientFactory}. Client will be cached to improve performance
 *
 * @author Warren Zhu
 */
public class DefaultServiceBusQueueClientFactory extends AbstractServiceBusSenderFactory
    implements ServiceBusQueueClientFactory {

    private final Map<String, ServiceBusProcessorClient> processorClientMap = new ConcurrentHashMap<>();
    private final Map<String, ServiceBusSenderAsyncClient> senderClientMap = new ConcurrentHashMap<>();

    // TODO (xiada) whether will this reuse the underlying connection?
    private final ServiceBusClientBuilder serviceBusClientBuilder;

    public DefaultServiceBusQueueClientFactory(String connectionString) {
        this(connectionString, AmqpTransportType.AMQP);
    }

    public DefaultServiceBusQueueClientFactory(String connectionString, AmqpTransportType amqpTransportType) {
        super(connectionString);
        this.serviceBusClientBuilder =
            new ServiceBusClientBuilder().connectionString(connectionString)
                                         .transportType(amqpTransportType)
                                         .clientOptions(new ClientOptions().setApplicationId(SPRING_SERVICE_BUS_APPLICATION_ID));
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
                                          .receiveMode(clientConfig.getServiceBusReceiveMode())
                                          .maxConcurrentCalls(clientConfig.getMaxConcurrentCalls())
                                          // TODO, which value doesn't used in builder and
                                          //  It looks like max auto renew duration is not exposed
                                          .maxConcurrentSessions(clientConfig.getConcurrency())
                                          .prefetchCount(clientConfig.getPrefetchCount())
                                          .disableAutoComplete()
                                          .processMessage(messageProcessor.processMessage())
                                          .processError(messageProcessor.processError())
                                          .buildProcessorClient();

        } else {
            return serviceBusClientBuilder.processor()
                                          .queueName(name)
                                          .receiveMode(clientConfig.getServiceBusReceiveMode())
                                          .maxConcurrentCalls(clientConfig.getMaxConcurrentCalls())
                                          .prefetchCount(clientConfig.getPrefetchCount())
                                          .disableAutoComplete()
                                          .processMessage(messageProcessor.processMessage())
                                          .processError(messageProcessor.processError())
                                          .buildProcessorClient();
        }

    }

    private ServiceBusSenderAsyncClient createQueueSender(String name) {
        return serviceBusClientBuilder.sender().queueName(name).buildAsyncClient();
    }

    public void proxyOptions(ProxyOptions proxyOptions) {
        serviceBusClientBuilder.proxyOptions(proxyOptions);
    }

    public void retryOptions(AmqpRetryOptions amqpRetryOptions) {
        serviceBusClientBuilder.retryOptions(amqpRetryOptions);
    }

    public void transportType(AmqpTransportType transportType) {
        serviceBusClientBuilder.transportType(transportType);
    }

}
