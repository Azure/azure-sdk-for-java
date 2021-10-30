// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.sender;

import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.spring.service.servicebus.factory.ServiceBusClientBuilderFactory;
import com.azure.spring.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.servicebus.core.properties.ProducerProperties;
import org.springframework.lang.Nullable;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * {@link ServiceBusSenderAsyncClient} produced by this factory will share the same namespace level configuration, but
 * if a configuration entry is provided at both producer and namespace level, the producer level configuration will
 * take advantage.
 */
public class DefaultServiceBusNamespaceQueueSenderClientFactory extends AbstractServiceBusSenderClientFactory {

    public DefaultServiceBusNamespaceQueueSenderClientFactory(NamespaceProperties namespaceProperties) {
        this(namespaceProperties, key -> null);
    }

    public DefaultServiceBusNamespaceQueueSenderClientFactory(NamespaceProperties namespaceProperties,
                                                              PropertiesSupplier<String, ProducerProperties> supplier) {
        super(namespaceProperties, supplier);
    }

    protected ServiceBusSenderAsyncClient doCreateProducer(String name, @Nullable ProducerProperties properties) {
        ProducerProperties producerProperties = parentMerger.mergeParent(properties, this.namespaceProperties);
        Tuple2<String, ProducerProperties> key = Tuples.of(name, producerProperties);
        if (this.clients.containsKey(key)) {
            return this.clients.get(key);
        }

        ServiceBusSenderAsyncClient producerClient = new ServiceBusClientBuilderFactory(producerProperties)
            .build().sender().queueName(name).buildAsyncClient();

        this.listeners.forEach(l -> l.producerAdded(name));

        this.clients.put(key, producerClient);
        return producerClient;
    }

}
