// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.factory;


import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.util.ClientOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.spring.integration.servicebus.ServiceBusClientConfig;
import com.azure.spring.integration.servicebus.ServiceBusMessageProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

    private final Log logger = LogFactory.getLog(DefaultServiceBusQueueClientFactory.class);

    private final Map<String, ServiceBusProcessorClient> processorClientMap = new ConcurrentHashMap<>();
    private final Map<String, ServiceBusSenderAsyncClient> senderClientMap = new ConcurrentHashMap<>();

    // TODO (xiada) whether will this reuse the underlying connection?
    private final ServiceBusClientBuilder serviceBusClientBuilder;

    public DefaultServiceBusQueueClientFactory(String connectionString) {
        this(connectionString, AmqpTransportType.AMQP);
    }

    public DefaultServiceBusQueueClientFactory(String connectionString, AmqpTransportType amqpTransportType) {
        super(connectionString);
        this.serviceBusClientBuilder = new ServiceBusClientBuilder()
                                                .connectionString(connectionString)
                                                .transportType(amqpTransportType)
                                                .clientOptions(new ClientOptions()
                                                .setApplicationId(SPRING_SERVICE_BUS_APPLICATION_ID));
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
        if (clientConfig.getConcurrency() != 1) {
            logger.warn("It is detected that concurrency is set, this attribute has been deprecated," +
                " you can use " + (clientConfig.isSessionsEnabled() ? "maxConcurrentCalls" : "maxConcurrentCalls") + " instead");
        }
        if (clientConfig.isSessionsEnabled()) {
            ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder builder =
                   serviceBusClientBuilder.sessionProcessor()
                                          .queueName(name)
                                          .receiveMode(clientConfig.getServiceBusReceiveMode())
                                          .maxConcurrentCalls(clientConfig.getMaxConcurrentCalls())
                                          // TODO, It looks like max auto renew duration is not exposed
                                          .maxConcurrentSessions(clientConfig.getMaxConcurrentSessions() == 1
                                              ? clientConfig.getConcurrency() : clientConfig.getMaxConcurrentSessions())
                                          .prefetchCount(clientConfig.getPrefetchCount())
                                          .processMessage(messageProcessor.processMessage())
                                          .processError(messageProcessor.processError());
            if (!clientConfig.isEnableAutoComplete()) {
                return builder.disableAutoComplete().buildProcessorClient();
            }
            return builder.buildProcessorClient();
        } else {
            ServiceBusClientBuilder.ServiceBusProcessorClientBuilder builder =
                   serviceBusClientBuilder.processor()
                                          .queueName(name)
                                          .receiveMode(clientConfig.getServiceBusReceiveMode())
                                          .maxConcurrentCalls(clientConfig.getMaxConcurrentCalls() == 1
                                              ? clientConfig.getConcurrency() : clientConfig.getMaxConcurrentCalls())
                                          .prefetchCount(clientConfig.getPrefetchCount())
                                          .processMessage(messageProcessor.processMessage())
                                          .processError(messageProcessor.processError());
            if (!clientConfig.isEnableAutoComplete()) {
                return builder.disableAutoComplete().buildProcessorClient();
            }
            return builder.buildProcessorClient();
        }
    }

    private ServiceBusSenderAsyncClient createQueueSender(String name) {
        return serviceBusClientBuilder.sender().queueName(name).buildAsyncClient();
    }

    public void retryOptions(AmqpRetryOptions amqpRetryOptions) {
        serviceBusClientBuilder.retryOptions(amqpRetryOptions);
    }

}
