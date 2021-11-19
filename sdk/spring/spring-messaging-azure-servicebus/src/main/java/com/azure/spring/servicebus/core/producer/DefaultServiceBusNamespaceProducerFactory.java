// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.producer;

import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.spring.core.AzureSpringIdentifier;
import com.azure.spring.messaging.PropertiesSupplier;
import com.azure.spring.service.servicebus.factory.ServiceBusSenderClientBuilderFactory;
import com.azure.spring.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.servicebus.core.properties.ProducerProperties;
import com.azure.spring.servicebus.core.properties.merger.ProducerPropertiesParentMerger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link ServiceBusSenderAsyncClient} produced by this factory will share the same namespace level configuration, but
 * if a configuration entry is provided at both producer and namespace level, the producer level configuration will
 * take advantage.
 */
public class DefaultServiceBusNamespaceProducerFactory implements ServiceBusProducerFactory, DisposableBean {

    private final List<Listener> listeners = new ArrayList<>();
    private final NamespaceProperties namespaceProperties;
    private final PropertiesSupplier<String, ProducerProperties> propertiesSupplier;
    private final Map<String, ServiceBusSenderAsyncClient> clients = new ConcurrentHashMap<>();
    private final ProducerPropertiesParentMerger parentMerger = new ProducerPropertiesParentMerger();

    public DefaultServiceBusNamespaceProducerFactory(NamespaceProperties namespaceProperties) {
        this(namespaceProperties, key -> null);
    }

    public DefaultServiceBusNamespaceProducerFactory(NamespaceProperties namespaceProperties,
                                                     PropertiesSupplier<String, ProducerProperties> supplier) {
        this.namespaceProperties = namespaceProperties;
        this.propertiesSupplier = supplier == null ? key -> null : supplier;
    }

    public ServiceBusSenderAsyncClient createProducer(String name) {
        return createProducer(name, null);
    }

    public ServiceBusSenderAsyncClient createProducer(String name, ServiceBusEntityType entityType) {
        ProducerProperties producerProperties = this.propertiesSupplier.getProperties(name) != null
            ? this.propertiesSupplier.getProperties(name) : new ProducerProperties();
        if (entityType != null) {
            producerProperties.setEntityType(entityType);
        }
        return doCreateProducer(name, producerProperties);
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
        clients.forEach((name, producer) -> {
            listeners.forEach(l -> l.producerRemoved(name, producer));
            producer.close();
        });
        this.clients.clear();
        this.listeners.clear();
    }

    private ServiceBusSenderAsyncClient doCreateProducer(String name, @Nullable ProducerProperties properties) {
        return clients.computeIfAbsent(name, entityName -> {
            ProducerProperties producerProperties = parentMerger.mergeParent(properties, this.namespaceProperties);
            producerProperties.setEntityName(entityName);

            //TODO(yiliu6): whether to make the producer client share the same service bus client builder
            ServiceBusSenderClientBuilderFactory factory = new ServiceBusSenderClientBuilderFactory(producerProperties);
            factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_INTEGRATION_SERVICE_BUS);
            ServiceBusSenderAsyncClient producerClient = factory.build().buildAsyncClient();

            this.listeners.forEach(l -> l.producerAdded(entityName, producerClient));
            return producerClient;
        });
    }
}
