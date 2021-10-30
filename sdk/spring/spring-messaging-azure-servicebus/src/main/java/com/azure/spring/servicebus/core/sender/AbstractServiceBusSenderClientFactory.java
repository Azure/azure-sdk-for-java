// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.sender;

import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.spring.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.servicebus.core.properties.ProducerProperties;
import com.azure.spring.servicebus.core.properties.merger.ProducerPropertiesParentMerger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.lang.Nullable;
import reactor.util.function.Tuple2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base class of service bus client factory to provide connection string
 *
 * @author Warren Zhu
 */
abstract class AbstractServiceBusSenderClientFactory implements ServiceBusSenderClientFactory, DisposableBean {

    protected final List<Listener> listeners = new ArrayList<>();
    protected final NamespaceProperties namespaceProperties;
    protected final PropertiesSupplier<String, ProducerProperties> propertiesSupplier;
    protected final Map<Tuple2<String, ProducerProperties>, ServiceBusSenderAsyncClient> clients = new ConcurrentHashMap<>();
    protected final ProducerPropertiesParentMerger parentMerger = new ProducerPropertiesParentMerger();

    public AbstractServiceBusSenderClientFactory(NamespaceProperties namespaceProperties) {
        this(namespaceProperties, key -> null);
    }

    public AbstractServiceBusSenderClientFactory(NamespaceProperties namespaceProperties,
                                                 PropertiesSupplier<String, ProducerProperties> supplier) {
        this.namespaceProperties = namespaceProperties;
        this.propertiesSupplier = supplier;
    }

    @Override
    public ServiceBusSenderAsyncClient createSender(String name) {
        return doCreateProducer(name, this.propertiesSupplier.getProperties(name));
    }

    protected abstract ServiceBusSenderAsyncClient doCreateProducer(String name, @Nullable ProducerProperties properties);

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
        this.clients.values().forEach(ServiceBusSenderAsyncClient::close);
        this.clients.clear();
    }

}
