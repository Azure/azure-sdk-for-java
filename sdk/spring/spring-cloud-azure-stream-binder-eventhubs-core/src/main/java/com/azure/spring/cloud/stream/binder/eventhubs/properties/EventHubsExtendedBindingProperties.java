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
@ConfigurationProperties(EventHubsExtendedBindingProperties.PREFIX)
public class EventHubsExtendedBindingProperties
        extends AbstractExtendedBindingProperties<EventHubsConsumerProperties, EventHubsProducerProperties, EventHubsBindingProperties> {

    public static final String PREFIX = "spring.cloud.stream.eventhubs";
    private static final String DEFAULTS_PREFIX = PREFIX + ".default";

    @Override
    public String getDefaultsPrefix() {
        return DEFAULTS_PREFIX;
    }

    @Override
    public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
        return EventHubsBindingProperties.class;
    }

    /**
     * Get Bindings.
     *
     * @return Bindings value
     */
    public Map<String, EventHubsBindingProperties> getBindings() {
        return doGetBindings();
    }

}
