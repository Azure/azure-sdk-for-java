// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.factory;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.spring.core.util.Tuple;
import com.azure.spring.integration.servicebus.ServiceBusClientConfig;
import com.azure.spring.integration.servicebus.ServiceBusMessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Default implementation of {@link ServiceBusTopicClientFactory}. Client will be cached to improve performance
 *
 * @author Warren Zhu
 */
public class DefaultServiceBusTopicClientFactory extends AbstractServiceBusSenderFactory
    implements ServiceBusTopicClientFactory, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServiceBusTopicClientFactory.class);
    private final Map<Tuple<String, String>, ServiceBusProcessorClient> topicProcessorMap = new ConcurrentHashMap<>();
    private final Map<String, ServiceBusSenderAsyncClient> topicSenderMap = new ConcurrentHashMap<>();


    public DefaultServiceBusTopicClientFactory(ServiceBusClientBuilder serviceBusClientBuilder) {
        super(serviceBusClientBuilder);
        // TODO (xiada) the application id should be different for spring integration
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
        close(topicSenderMap, ServiceBusSenderAsyncClient::close);
        close(topicProcessorMap, ServiceBusProcessorClient::close);
    }

    @Override
    public ServiceBusProcessorClient getOrCreateProcessor(
        String topic,
        String subscription,
        ServiceBusClientConfig clientConfig,
        ServiceBusMessageProcessor messageProcessor) {
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
                                                      ServiceBusMessageProcessor messageProcessor) {

        if (config.getConcurrency() != 1) {
            LOGGER.warn("It is detected that concurrency is set, this attribute has been deprecated,"
                + " you can use " + (config.isSessionsEnabled() ? "maxConcurrentSessions" : "maxConcurrentCalls") + " instead");
        }
        if (config.isSessionsEnabled()) {
            ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder builder =
                   serviceBusClientBuilder.sessionProcessor()
                                          .topicName(topic)
                                          .subscriptionName(subscription)
                                          .receiveMode(config.getServiceBusReceiveMode())
                                          .maxConcurrentCalls(config.getMaxConcurrentCalls())
                                          // TODO, It looks like max auto renew duration is not exposed
                                          .maxConcurrentSessions(config.getMaxConcurrentSessions())
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
                                          .maxConcurrentCalls(config.getMaxConcurrentCalls())
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

    public void setRetryOptions(AmqpRetryOptions amqpRetryOptions) {
        serviceBusClientBuilder.retryOptions(amqpRetryOptions);
    }

    //TODO: Latest serviceBusClientBuilder support crossEntityTransactions
}
