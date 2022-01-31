// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core.producer;

import com.azure.core.util.Configuration;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.spring.core.AzureSpringIdentifier;
import com.azure.spring.eventhubs.core.properties.NamespaceProperties;
import com.azure.spring.eventhubs.core.properties.ProducerProperties;
import com.azure.spring.eventhubs.core.properties.merger.ProducerPropertiesParentMerger;
import com.azure.spring.messaging.PropertiesSupplier;
import com.azure.spring.service.implementation.eventhubs.factory.EventHubClientBuilderFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@link EventHubsProducerFactory} implementation to produce cached {@link EventHubProducerAsyncClient} instances
 * for provided {@link NamespaceProperties} and optional producer {@link PropertiesSupplier} on each
 * {@link #createProducer} invocation.
 * <p>
 * The created {@link EventHubProducerAsyncClient}s are cached according to the event hub names.
 * =</p>
 * <p>
 * {@link EventHubProducerAsyncClient} produced by this factory will share the same namespace level configuration, but
 * if a configuration entry is provided at both producer and namespace level, the producer level configuration will
 * take advantage.
 * </p>
 */
public final class DefaultEventHubsNamespaceProducerFactory implements EventHubsProducerFactory, DisposableBean {

    private final List<Listener> listeners = new ArrayList<>();
    private final NamespaceProperties namespaceProperties;
    private final PropertiesSupplier<String, ProducerProperties> propertiesSupplier;
    private final Map<String, EventHubProducerAsyncClient> clients = new ConcurrentHashMap<>();
    private final ProducerPropertiesParentMerger parentMerger = new ProducerPropertiesParentMerger();
    private final Configuration configuration;

    /**
     * Construct a factory with the provided namespace level configuration.
     * @param namespaceProperties the namespace properties
     */
    public DefaultEventHubsNamespaceProducerFactory(NamespaceProperties namespaceProperties, Configuration configuration) {
        this(namespaceProperties, key -> null, configuration);
    }

    /**
     * Construct a factory with the provided namespace level configuration and producer {@link PropertiesSupplier}.
     * @param namespaceProperties the namespace properties.
     * @param supplier the {@link PropertiesSupplier} to supply {@link ProducerProperties} for each event hub.
     */
    public DefaultEventHubsNamespaceProducerFactory(NamespaceProperties namespaceProperties,
                                                    PropertiesSupplier<String, ProducerProperties> supplier, Configuration configuration) {
        this.namespaceProperties = namespaceProperties;
        this.propertiesSupplier = supplier == null ? key -> null : supplier;
        this.configuration = configuration;
    }

    @Override
    public EventHubProducerAsyncClient createProducer(String eventHub) {
        return doCreateProducer(eventHub, this.propertiesSupplier.getProperties(eventHub));
    }

    private EventHubProducerAsyncClient doCreateProducer(String eventHub, @Nullable ProducerProperties properties) {
        return clients.computeIfAbsent(eventHub, entityName -> {
            ProducerProperties producerProperties = parentMerger.mergeParent(properties, this.namespaceProperties);
            producerProperties.setEventHubName(entityName);
            EventHubClientBuilderFactory factory = new EventHubClientBuilderFactory(producerProperties);
            factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_INTEGRATION_EVENT_HUBS);
            EventHubProducerAsyncClient producerClient = factory.build(configuration).buildAsyncProducerClient();
            this.listeners.forEach(l -> l.producerAdded(entityName, producerClient));

            return producerClient;
        });
    }

    @Override
    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    @Override
    public boolean removeListener(Listener listener) {
        return this.listeners.remove(listener);
    }

    @Override
    public void destroy() {
        this.clients.forEach((name, client) -> {
            this.listeners.forEach(l -> l.producerRemoved(name, client));
            client.close();
        });
        this.clients.clear();
        this.listeners.clear();
    }
}
