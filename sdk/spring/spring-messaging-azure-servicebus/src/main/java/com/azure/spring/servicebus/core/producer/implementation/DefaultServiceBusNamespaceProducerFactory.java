// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.producer.implementation;

import com.azure.core.util.Configuration;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.spring.core.AzureSpringIdentifier;
import com.azure.spring.messaging.PropertiesSupplier;
import com.azure.spring.service.implementation.servicebus.factory.ServiceBusSenderClientBuilderFactory;
import com.azure.spring.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.servicebus.core.producer.ServiceBusProducerFactory;
import com.azure.spring.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.servicebus.core.properties.ProducerProperties;
import com.azure.spring.servicebus.core.properties.merger.SenderPropertiesParentMerger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * The {@link ServiceBusProducerFactory} implementation to produce new {@link ServiceBusSenderAsyncClient} instances
 * for provided {@link NamespaceProperties} and optional producer {@link PropertiesSupplier} on each
 * {@link #createProducer} invocation.
 * <p>
 * {@link ServiceBusSenderAsyncClient} produced by this factory will share the same namespace level configuration, but
 * if a configuration entry is provided at both producer and namespace level, the producer level configuration will
 * take advantage.
 * </p>
 */
public final class DefaultServiceBusNamespaceProducerFactory implements ServiceBusProducerFactory, DisposableBean {

    private final List<Listener> listeners = new ArrayList<>();
    private final Map<String, ServiceBusSenderAsyncClient> clients = new ConcurrentHashMap<>();
    private final Supplier<ServiceBusClientBuilder.ServiceBusSenderClientBuilder> builderSupplier;

    /**
     * Construct a factory with the provided namespace level configuration.
     */
    public DefaultServiceBusNamespaceProducerFactory(Supplier<ServiceBusClientBuilder.ServiceBusSenderClientBuilder> builderSupplier) {
        this.builderSupplier = builderSupplier;
    }

    @Override
    public ServiceBusSenderAsyncClient createProducer(String name) {
        return createProducer(name, null);
    }

    @Override
    public ServiceBusSenderAsyncClient createProducer(String name, ServiceBusEntityType entityType) {
        return doCreateProducer(name, entityType, builderSupplier);
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

    private ServiceBusSenderAsyncClient doCreateProducer(String entityName, ServiceBusEntityType entityType, Supplier<ServiceBusClientBuilder.ServiceBusSenderClientBuilder> builderSupplier) {
        return clients.computeIfAbsent(entityName, name -> {

            ServiceBusClientBuilder.ServiceBusSenderClientBuilder builder = builderSupplier.get();
            if (entityType == ServiceBusEntityType.TOPIC) {
                builder.topicName(name);
            } else {
                builder.queueName(name);
            }

            ServiceBusSenderAsyncClient producerClient = builder.buildAsyncClient();

            this.listeners.forEach(l -> l.producerAdded(name, producerClient));
            return producerClient;
        });
    }
}
