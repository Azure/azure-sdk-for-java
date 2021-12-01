// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhub.stream.binder.properties;

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

    /**
     *
     * @param channelName The channel name.
     * @return The EventHubConsumerProperties.
     */
    @Override
    public EventHubConsumerProperties getExtendedConsumerProperties(String channelName) {
        return this.bindings.computeIfAbsent(channelName, key -> new EventHubBindingProperties()).getConsumer();
    }

    /**
     *
     * @param channelName The channel name.
     * @return The EventHubProducerProperties.
     */
    @Override
    public EventHubProducerProperties getExtendedProducerProperties(String channelName) {
        return this.bindings.computeIfAbsent(channelName, key -> new EventHubBindingProperties()).getProducer();
    }

    /**
     *
     * @return the defaults prefix.
     */
    @Override
    public String getDefaultsPrefix() {
        return DEFAULTS_PREFIX;
    }

    /**
     *
     * @return EventHubBindingProperties.class
     */
    @Override
    public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
        return EventHubBindingProperties.class;
    }

    /**
     *
     * @return The checkpoint storage account
     */
    public String getCheckpointStorageAccount() {
        return checkpointStorageAccount;
    }

    /**
     *
     * @param checkpointStorageAccount The checkpoint storage account
     */
    public void setCheckpointStorageAccount(String checkpointStorageAccount) {
        this.checkpointStorageAccount = checkpointStorageAccount;
    }

    /**
     *
     * @return The binding map.
     */
    public Map<String, EventHubBindingProperties> getBindings() {
        return bindings;
    }

    /**
     *
     * @param bindings The binding map.
     */
    public void setBindings(Map<String, EventHubBindingProperties> bindings) {
        this.bindings = bindings;
    }
}
