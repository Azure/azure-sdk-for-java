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
import com.azure.spring.cloud.context.core.util.Tuple;
import com.azure.spring.integration.servicebus.ServiceBusClientConfig;
import com.azure.spring.integration.servicebus.ServiceBusMessageProcessor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of {@link ServiceBusTopicClientFactory}. Client will be cached to improve performance
 *
 * @author Warren Zhu
 */
public class DefaultServiceBusTopicClientFactory extends AbstractServiceBusSenderFactory
    implements ServiceBusTopicClientFactory {


    private final ServiceBusClientBuilder serviceBusClientBuilder;
    private final Map<Tuple<String, String>, ServiceBusProcessorClient> topicProcessorMap = new ConcurrentHashMap<>();
    private final Map<String, ServiceBusSenderAsyncClient> topicSenderMap = new ConcurrentHashMap<>();

    public DefaultServiceBusTopicClientFactory(String connectionString) {
        this(connectionString, AmqpTransportType.AMQP);
    }

    public DefaultServiceBusTopicClientFactory(String connectionString, AmqpTransportType amqpTransportType) {
        super(connectionString);
        this.serviceBusClientBuilder = new ServiceBusClientBuilder().connectionString(connectionString);
        this.serviceBusClientBuilder.transportType(amqpTransportType);
    }

    @Override
    public ServiceBusProcessorClient getOrCreateProcessor(
        String topic,
        String subscription,
        ServiceBusClientConfig clientConfig,
        ServiceBusMessageProcessor<ServiceBusReceivedMessageContext, ServiceBusErrorContext> messageProcessor) {
        return this.topicProcessorMap.computeIfAbsent(Tuple.of(topic, subscription),
                                                      t -> createProcessor(t.getFirst(),
                                                                           t.getSecond(),
                                                                           clientConfig,
                                                                           messageProcessor));
    }

    @Override
    public ServiceBusSenderAsyncClient getOrCreateSender(String name) {
        return this.topicSenderMap.computeIfAbsent(name, this::createTopicSender);
    }


    private ServiceBusProcessorClient createProcessor(String topic,
                                                      String subscription,
                                                      ServiceBusClientConfig config,
                                                      ServiceBusMessageProcessor<ServiceBusReceivedMessageContext,
                                                                                    ServiceBusErrorContext> messageProcessor) {
        if (config.isSessionsEnabled()) {
            return serviceBusClientBuilder.sessionProcessor()
                                          .topicName(topic)
                                          .subscriptionName(subscription)
                                          .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
                                          .maxConcurrentCalls(1)
                                          // TODO, make it a constant or get duration is not exposed it from
                                          //  clientConfig. And it looks like max auto renew
                                          .maxConcurrentSessions(config.getConcurrency())
                                          .prefetchCount(config.getPrefetchCount())
                                          .disableAutoComplete()
                                          .processMessage(messageProcessor.processMessage())
                                          .processError(messageProcessor.processError())
                                          .buildProcessorClient();
        } else {
            return serviceBusClientBuilder.processor()
                                          .topicName(topic)
                                          .subscriptionName(subscription)
                                          .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
                                          .maxConcurrentCalls(config.getConcurrency())
                                          .prefetchCount(config.getPrefetchCount())
                                          .disableAutoComplete()
                                          .processMessage(messageProcessor.processMessage())
                                          .processError(messageProcessor.processError())
                                          .buildProcessorClient();
        }
    }

    private ServiceBusSenderAsyncClient createTopicSender(String name) {
        return serviceBusClientBuilder.sender().topicName(name).buildAsyncClient();
    }
}
