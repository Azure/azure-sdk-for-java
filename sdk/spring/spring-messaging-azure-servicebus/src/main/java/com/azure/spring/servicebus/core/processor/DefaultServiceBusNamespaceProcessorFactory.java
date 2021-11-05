// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.processor;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.service.servicebus.factory.ServiceBusProcessorClientBuilderFactory;
import com.azure.spring.service.servicebus.factory.ServiceBusSessionProcessorClientBuilderFactory;
import com.azure.spring.service.servicebus.processor.MessageProcessingListener;
import com.azure.spring.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.servicebus.core.properties.SubscriptionPropertiesSupplier;
import com.azure.spring.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.servicebus.core.properties.ProcessorProperties;
import com.azure.spring.servicebus.core.properties.merger.ProcessorPropertiesParentMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.lang.Nullable;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;


/**
 * Default implementation of {@link ServiceBusProcessorFactory}. Client will be cached to improve performance
 *
 * @author Warren Zhu
 */
public class DefaultServiceBusNamespaceProcessorFactory implements ServiceBusProcessorFactory, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServiceBusNamespaceProcessorFactory.class);
    private final Map<Tuple2<String, String>, ServiceBusProcessorClient> processorMap = new ConcurrentHashMap<>();
    private final List<Listener> listeners = new ArrayList<>();
    private final NamespaceProperties namespaceProperties;
    private final SubscriptionPropertiesSupplier<ProcessorProperties> propertiesSupplier;
    private final ProcessorPropertiesParentMerger propertiesMerger = new ProcessorPropertiesParentMerger();
    private static final String INVALID_SUBSCRIPTION =
        DefaultServiceBusNamespaceProcessorFactory.class.getSimpleName() + "INVALID_SUBSCRIPTION";

    // TODO (xiada) the application id should be different for spring integration
    public DefaultServiceBusNamespaceProcessorFactory(NamespaceProperties namespaceProperties) {
        this(namespaceProperties, new SubscriptionPropertiesSupplier<ProcessorProperties>() {});
    }

    public DefaultServiceBusNamespaceProcessorFactory(SubscriptionPropertiesSupplier<ProcessorProperties> supplier) {
        this(null, supplier);
    }

    public DefaultServiceBusNamespaceProcessorFactory(NamespaceProperties namespaceProperties,
                                                      SubscriptionPropertiesSupplier<ProcessorProperties> supplier) {
        this.namespaceProperties = namespaceProperties;
        this.propertiesSupplier = supplier == null ? new SubscriptionPropertiesSupplier<ProcessorProperties>() {
        } : supplier;
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
        close(processorMap, ServiceBusProcessorClient::close);
    }

    @Override
    public ServiceBusProcessorClient createProcessor(String queue, MessageProcessingListener messageProcessorListener) {
        return doCreateProcessor(queue, INVALID_SUBSCRIPTION, messageProcessorListener,
            this.propertiesSupplier.getQueueSubscription(queue));
    }

    @Override
    public ServiceBusProcessorClient createProcessor(String topic,
                                                     String subscription,
                                                     MessageProcessingListener messageProcessorListener) {
        return doCreateProcessor(topic, subscription, messageProcessorListener,
            this.propertiesSupplier.getTopicSubscription(topic, subscription));
    }

    private ServiceBusProcessorClient doCreateProcessor(String name, String subscription,
                                                        MessageProcessingListener listener,
                                                        @Nullable ProcessorProperties properties) {
        Tuple2<String, String> key = Tuples.of(name, subscription);
        if (this.processorMap.containsKey(key)) {
            return this.processorMap.get(key);
        }

        ProcessorProperties processorProperties = propertiesMerger.mergeParent(properties, this.namespaceProperties);
        processorProperties.setName(name);
        if (INVALID_SUBSCRIPTION.equals(subscription)) {
            processorProperties.setType(ServiceBusEntityType.QUEUE);
        } else {
            processorProperties.setType(ServiceBusEntityType.TOPIC);
            processorProperties.setSubscriptionName(subscription);
        }

        ServiceBusProcessorClient client;
        //TODO(yiliu6): whether to use shared ServiceBusClientBuilder
        if (Boolean.TRUE.equals(processorProperties.getSessionAware())) {
            client = new ServiceBusSessionProcessorClientBuilderFactory(processorProperties, listener).build()
                                                                                            .buildProcessorClient();
        } else {
            client = new ServiceBusProcessorClientBuilderFactory(processorProperties, listener).build()
                                                                                               .buildProcessorClient();
        }

        this.listeners.forEach(l -> l.processorAdded(name, INVALID_SUBSCRIPTION.equals(subscription) ? null :
            subscription));
        this.processorMap.put(key, client);
        return client;
    }

}
