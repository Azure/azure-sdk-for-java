// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.stream.binder.properties;

import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;
import org.springframework.cloud.stream.binder.ExtendedBindingProperties;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Warren Zhu
 */
public abstract class ServiceBusExtendedBindingProperties
        implements ExtendedBindingProperties<ServiceBusConsumerProperties, ServiceBusProducerProperties> {
    private final Map<String, ServiceBusBindingProperties> bindings = new ConcurrentHashMap<>();

    @Override
    public ServiceBusConsumerProperties getExtendedConsumerProperties(String channelName) {
        return this.bindings.computeIfAbsent(channelName, key -> new ServiceBusBindingProperties()).getConsumer();
    }

    @Override
    public ServiceBusProducerProperties getExtendedProducerProperties(String channelName) {
        return this.bindings.computeIfAbsent(channelName, key -> new ServiceBusBindingProperties()).getProducer();
    }

    @Override
    public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
        return ServiceBusBindingProperties.class;
    }

    public Map<String, ServiceBusBindingProperties> getBindings() {
        return bindings;
    }
}
