// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.sender;

import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.spring.service.servicebus.factory.CommonServiceBusClientBuilderFactory;
import com.azure.spring.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.servicebus.core.properties.ProducerProperties;
import com.azure.spring.servicebus.core.properties.merger.ProducerPropertiesParentMerger;
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

public class DefaultServiceBusQueueSenderClientFactory implements ServiceBusSenderFactory, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServiceBusQueueSenderClientFactory.class);

    private final List<Listener> listeners = new ArrayList<>();
    private final NamespaceProperties namespaceProperties;
    private final PropertiesSupplier<String, ProducerProperties> propertiesSupplier;
    private final Map<Tuple2<String, ProducerProperties>, ServiceBusSenderAsyncClient> clients = new ConcurrentHashMap<>();
    private final ProducerPropertiesParentMerger parentMerger = new ProducerPropertiesParentMerger();

    public DefaultServiceBusQueueSenderClientFactory(NamespaceProperties namespaceProperties) {
        this(namespaceProperties, key -> null);
    }

    public DefaultServiceBusQueueSenderClientFactory(NamespaceProperties namespaceProperties,
                                                   PropertiesSupplier<String, ProducerProperties> supplier) {
        this.namespaceProperties = namespaceProperties;
        this.propertiesSupplier = supplier;
    }

    @Override
    public ServiceBusSenderAsyncClient createSender(String name) {
        return doCreateProducer(name, this.propertiesSupplier.getProperties(name));
    }

    private ServiceBusSenderAsyncClient doCreateProducer(String eventHub, @Nullable ProducerProperties properties) {
        ProducerProperties producerProperties = parentMerger.mergeParent(properties, this.namespaceProperties);
        Tuple2<String, ProducerProperties> key = Tuples.of(eventHub, producerProperties);
        if (this.clients.containsKey(key)) {
            return this.clients.get(key);
        }

        ServiceBusSenderAsyncClient producerClient = new CommonServiceBusClientBuilderFactory(producerProperties)
            .build().buildAsyncProducerClient();

        this.listeners.forEach(l -> l.producerAdded(eventHub));

        this.clients.put(key, producerClient);
        return producerClient;
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
    public void destroy() throws Exception {
        this.clients.values().forEach(ServiceBusSenderAsyncClient::close);
        this.clients.clear();
    }
}
