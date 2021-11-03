// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.stream.binder.AbstractExtendedBindingProperties;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;

import java.util.Map;

/**
 *
 */
@ConfigurationProperties("spring.cloud.stream.eventhub")
public class EventHubExtendedBindingProperties
        extends AbstractExtendedBindingProperties<EventHubConsumerProperties, EventHubProducerProperties, EventHubBindingProperties> {

    private static final String DEFAULTS_PREFIX = "spring.cloud.stream.eventhub.default";

    @Override
    public String getDefaultsPrefix() {
        return DEFAULTS_PREFIX;
    }

    @Override
    public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
        return EventHubBindingProperties.class;
    }

    public Map<String, EventHubBindingProperties> getBindings() {
        return doGetBindings();
    }

}
