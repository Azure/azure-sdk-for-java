// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.processor.implementation;

import com.azure.core.util.Configuration;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.core.AzureSpringIdentifier;
import com.azure.spring.messaging.PropertiesSupplier;
import com.azure.spring.messaging.ConsumerIdentifier;
import com.azure.spring.service.implementation.servicebus.factory.ServiceBusProcessorClientBuilderFactory;
import com.azure.spring.service.implementation.servicebus.factory.ServiceBusSessionProcessorClientBuilderFactory;
import com.azure.spring.service.servicebus.processor.MessageProcessingListener;
import com.azure.spring.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.servicebus.core.processor.ServiceBusListenerFactory;
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
import java.util.function.Supplier;


/**
 * The {@link ServiceBusListenerFactory} implementation to produce new {@link ServiceBusProcessorClient} instances
 * for provided {@link NamespaceProperties} and optional
 * processor {@link PropertiesSupplier} on each invocation.
 *
 * <p>
 * {@link ServiceBusProcessorClient} produced by this factory will share the same namespace level configuration, but if a
 * configuration entry is provided at both processor and namespace level, the processor level configuration will take
 * advantage.
 * </p>
 */
public final class DefaultServiceBusNamespaceProcessorFactory implements ServiceBusListenerFactory, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServiceBusNamespaceProcessorFactory.class);
    private final Map<ConsumerIdentifier, ServiceBusProcessorClient> processorMap = new ConcurrentHashMap<>();
    private final List<Listener> listeners = new ArrayList<>();


    private void close(Map<ConsumerIdentifier, ServiceBusProcessorClient> map, Consumer<ServiceBusProcessorClient> close) {
        map.forEach((t, p) -> {
            try {
                listeners.forEach(l -> l.processorRemoved(t.getDestination(), t.getGroup(), p));
                close.accept(p);
            } catch (Exception ex) {
                LOGGER.warn("Failed to clean service bus queue client factory", ex);
            }
        });
    }

    @Override
    public void destroy() {
        this.listeners.clear();
    }

    @Override
    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }
}
