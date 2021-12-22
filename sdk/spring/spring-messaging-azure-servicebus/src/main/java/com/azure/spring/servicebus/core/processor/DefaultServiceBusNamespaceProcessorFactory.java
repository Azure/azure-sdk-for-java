// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.processor;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.core.AzureSpringIdentifier;
import com.azure.spring.messaging.PropertiesSupplier;
import com.azure.spring.messaging.PubSubPair;
import com.azure.spring.service.servicebus.factory.ServiceBusProcessorClientBuilderFactory;
import com.azure.spring.service.servicebus.factory.ServiceBusSessionProcessorClientBuilderFactory;
import com.azure.spring.service.servicebus.processor.MessageProcessingListener;
import com.azure.spring.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.servicebus.core.properties.ProcessorProperties;
import com.azure.spring.servicebus.core.properties.merger.ProcessorPropertiesParentMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;


/**
 * The {@link ServiceBusProcessorFactory} implementation to produce new {@link ServiceBusProcessorClient} instances
 * for provided {@link NamespaceProperties} and optional
 * processor {@link PropertiesSupplier} on each {@link #createProcessor} invocation.
 *
 * <p>
 * {@link ServiceBusProcessorClient} produced by this factory will share the same namespace level configuration, but if a
 * configuration entry is provided at both processor and namespace level, the processor level configuration will take
 * advantage.
 * </p>
 */
public final class DefaultServiceBusNamespaceProcessorFactory implements ServiceBusProcessorFactory, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServiceBusNamespaceProcessorFactory.class);
    private final Map<PubSubPair, ServiceBusProcessorClient> processorMap = new ConcurrentHashMap<>();
    private final List<Listener> listeners = new ArrayList<>();
    private final NamespaceProperties namespaceProperties;
    private final PropertiesSupplier<PubSubPair, ProcessorProperties> propertiesSupplier;
    private final ProcessorPropertiesParentMerger propertiesMerger = new ProcessorPropertiesParentMerger();
    public static final String INVALID_SUBSCRIPTION =
        DefaultServiceBusNamespaceProcessorFactory.class.getSimpleName() + "INVALID_SUBSCRIPTION";

    /**
     * Construct a factory with the provided namespace level properties.
     * @param namespaceProperties the namespace properties.
     */
    public DefaultServiceBusNamespaceProcessorFactory(NamespaceProperties namespaceProperties) {
        this(namespaceProperties, key -> null);
    }

    /**
     * Construct a factory with the provided namespace level properties and processor {@link PropertiesSupplier}.
     * @param namespaceProperties the namespace properties.
     * @param supplier the {@link PropertiesSupplier} to supply {@link ProcessorProperties} for each queue/topic entity.
     */
    public DefaultServiceBusNamespaceProcessorFactory(NamespaceProperties namespaceProperties,
                                                      PropertiesSupplier<PubSubPair, ProcessorProperties> supplier) {
        this.namespaceProperties = namespaceProperties;
        this.propertiesSupplier = supplier == null ? key -> null : supplier;
    }

    private void close(Map<PubSubPair, ServiceBusProcessorClient> map, Consumer<ServiceBusProcessorClient> close) {
        map.forEach((t, p) -> {
            try {
                listeners.forEach(l -> l.processorRemoved(t.getPublisher(), t.getSubscriber(), p));
                close.accept(p);
            } catch (Exception ex) {
                LOGGER.warn("Failed to clean service bus queue client factory", ex);
            }
        });
    }

    @Override
    public void destroy() {
        close(processorMap, ServiceBusProcessorClient::close);
        this.processorMap.clear();
        this.listeners.clear();
    }

    @Override
    public ServiceBusProcessorClient createProcessor(String queue, MessageProcessingListener messageProcessingListener) {
        return doCreateProcessor(queue, INVALID_SUBSCRIPTION, messageProcessingListener,
            this.propertiesSupplier.getProperties(PubSubPair.of(queue, INVALID_SUBSCRIPTION)));
    }

    @Override
    public ServiceBusProcessorClient createProcessor(String topic,
                                                     String subscription,
                                                     MessageProcessingListener messageProcessingListener) {
        return doCreateProcessor(topic, subscription, messageProcessingListener,
            this.propertiesSupplier.getProperties(PubSubPair.of(topic, subscription)));
    }

    private ServiceBusProcessorClient doCreateProcessor(String name, String subscription,
                                                        MessageProcessingListener listener,
                                                        @Nullable ProcessorProperties properties) {
        PubSubPair key = PubSubPair.of(name, subscription);

        return processorMap.computeIfAbsent(key, k -> {
            ProcessorProperties processorProperties = propertiesMerger.mergeParent(properties, this.namespaceProperties);
            processorProperties.setAutoComplete(false);
            processorProperties.setEntityName(k.getPublisher());
            if (INVALID_SUBSCRIPTION.equals(k.getSubscriber())) {
                processorProperties.setEntityType(ServiceBusEntityType.QUEUE);
            } else {
                processorProperties.setEntityType(ServiceBusEntityType.TOPIC);
                processorProperties.setSubscriptionName(k.getSubscriber());
            }

            ServiceBusProcessorClient client;
            //TODO(yiliu6): whether to use shared ServiceBusClientBuilder
            if (Boolean.TRUE.equals(processorProperties.getSessionEnabled())) {
                ServiceBusSessionProcessorClientBuilderFactory factory =
                    new ServiceBusSessionProcessorClientBuilderFactory(processorProperties, listener);
                factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_INTEGRATION_SERVICE_BUS);
                client = factory.build().buildProcessorClient();
            } else {
                ServiceBusProcessorClientBuilderFactory factory =
                    new ServiceBusProcessorClientBuilderFactory(processorProperties, listener);
                factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_INTEGRATION_SERVICE_BUS);
                client = factory.build().buildProcessorClient();
            }

            this.listeners.forEach(l -> l.processorAdded(k.getPublisher(), INVALID_SUBSCRIPTION.equals(k.getSubscriber()) ? null
                : k.getSubscriber(), client));
            return client;
        });
    }

    @Override
    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }
}
