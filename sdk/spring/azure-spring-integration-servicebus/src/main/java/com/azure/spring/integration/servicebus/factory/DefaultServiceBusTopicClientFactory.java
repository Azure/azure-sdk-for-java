// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.factory;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.util.ClientOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.spring.cloud.context.core.util.Tuple;
import com.azure.spring.integration.servicebus.ServiceBusClientConfig;
import com.azure.spring.integration.servicebus.ServiceBusMessageProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.spring.cloud.context.core.util.Constants.SPRING_SERVICE_BUS_APPLICATION_ID;

/**
 * Default implementation of {@link ServiceBusTopicClientFactory}. Client will be cached to improve performance
 *
 * @author Warren Zhu
 */
public class DefaultServiceBusTopicClientFactory extends AbstractServiceBusSenderFactory
    implements ServiceBusTopicClientFactory {

    private final Log logger = LogFactory.getLog(DefaultServiceBusQueueClientFactory.class);
    private final ServiceBusClientBuilder serviceBusClientBuilder;
    private final Map<Tuple<String, String>, ServiceBusProcessorClient> topicProcessorMap = new ConcurrentHashMap<>();
    private final Map<String, ServiceBusSenderAsyncClient> topicSenderMap = new ConcurrentHashMap<>();

    public DefaultServiceBusTopicClientFactory(String connectionString) {
        this(connectionString, AmqpTransportType.AMQP);
    }

    public DefaultServiceBusTopicClientFactory(String connectionString, AmqpTransportType amqpTransportType) {
        super(connectionString);
        this.serviceBusClientBuilder = new ServiceBusClientBuilder()
                                                .connectionString(connectionString)
                                                .transportType(amqpTransportType)
                                                .clientOptions(new ClientOptions()
                                                .setApplicationId(SPRING_SERVICE_BUS_APPLICATION_ID));
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

        if (config.getConcurrency() != 1) {
            logger.warn("It is detected that concurrency is set, this attribute has been deprecated," +
                " you can use " + (config.isSessionsEnabled() ? "maxConcurrentCalls" : "maxConcurrentCalls") + " instead");
        }if (config.isSessionsEnabled()) {
            ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder builder =
                   serviceBusClientBuilder.sessionProcessor()
                                          .topicName(topic)
                                          .subscriptionName(subscription)
                                          .receiveMode(config.getServiceBusReceiveMode())
                                          .maxConcurrentCalls(config.getMaxConcurrentCalls())
                                          // TODO, It looks like max auto renew duration is not exposed
                                          .maxConcurrentSessions(config.getMaxConcurrentSessions() == 1
                                              ? config.getConcurrency() : config.getMaxConcurrentSessions())
                                          .prefetchCount(config.getPrefetchCount())
                                          .processMessage(messageProcessor.processMessage())
                                          .processError(messageProcessor.processError());
            if (!config.isEnableAutoComplete()) {
                return builder.disableAutoComplete().buildProcessorClient();
            }
            return builder.buildProcessorClient();
        } else {
            ServiceBusClientBuilder.ServiceBusProcessorClientBuilder builder =
                   serviceBusClientBuilder.processor()
                                          .topicName(topic)
                                          .subscriptionName(subscription)
                                          .receiveMode(config.getServiceBusReceiveMode())
                                          .maxConcurrentCalls(config.getMaxConcurrentCalls() == 1
                                              ? config.getConcurrency() : config.getMaxConcurrentCalls())
                                          .prefetchCount(config.getPrefetchCount())
                                          .processMessage(messageProcessor.processMessage())
                                          .processError(messageProcessor.processError());
            if (!config.isEnableAutoComplete()) {
                return builder.disableAutoComplete().buildProcessorClient();
            }
            return builder.buildProcessorClient();
        }
    }

    private ServiceBusSenderAsyncClient createTopicSender(String name) {
        return serviceBusClientBuilder.sender().topicName(name).buildAsyncClient();
    }

    public void retryOptions(AmqpRetryOptions amqpRetryOptions) {
        serviceBusClientBuilder.retryOptions(amqpRetryOptions);
    }

    //TODO: Latest serviceBusClientBuilder support crossEntityTransactions
}
