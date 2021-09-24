// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;
import org.springframework.cloud.stream.binder.ExtendedBindingProperties;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Warren Zhu
 */
@ConfigurationProperties("spring.cloud.stream.eventhub")
public class EventHubExtendedBindingProperties
        implements ExtendedBindingProperties<EventHubConsumerProperties, EventHubProducerProperties> {
    private static final String DEFAULTS_PREFIX = "spring.cloud.stream.eventhub.default";
    private Map<String, EventHubBindingProperties> bindings = new ConcurrentHashMap<>();
    private String checkpointStorageAccount;

    @Override
    public EventHubConsumerProperties getExtendedConsumerProperties(String channelName) {
        return this.bindings.computeIfAbsent(channelName, key -> new EventHubBindingProperties()).getConsumer();
    }

    @Override
    public EventHubProducerProperties getExtendedProducerProperties(String channelName) {
        return this.bindings.computeIfAbsent(channelName, key -> new EventHubBindingProperties()).getProducer();
    }

    @Override
    public String getDefaultsPrefix() {
        return DEFAULTS_PREFIX;
    }

    @Override
    public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
        return EventHubBindingProperties.class;
    }

    public String getCheckpointStorageAccount() {
        return checkpointStorageAccount;
    }

    public void setCheckpointStorageAccount(String checkpointStorageAccount) {
        this.checkpointStorageAccount = checkpointStorageAccount;
    }

    public Map<String, EventHubBindingProperties> getBindings() {
        return bindings;
    }

    public void setBindings(Map<String, EventHubBindingProperties> bindings) {
        this.bindings = bindings;
    }
}
