// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.core.properties;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.stream.binder.AbstractExtendedBindingProperties;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;

import java.util.Map;

import static com.azure.spring.cloud.service.implementation.core.PropertiesValidator.validateNamespace;

/**
 *
 */
@ConfigurationProperties(EventHubsExtendedBindingProperties.PREFIX)
public class EventHubsExtendedBindingProperties
    extends AbstractExtendedBindingProperties<EventHubsConsumerProperties, EventHubsProducerProperties,
    EventHubsBindingProperties>
    implements InitializingBean {

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

    @Override
    public void afterPropertiesSet() throws Exception {
        validateNamespaceProperties();
    }

    private void validateNamespaceProperties() {
        getBindings().values()
                     .stream()
                     .map(bindings -> bindings.getConsumer().getNamespace())
                     .filter(str -> str != null)
                     .forEach(str -> validateNamespace(str));

        getBindings().values()
                     .stream()
                     .map(bindings -> bindings.getProducer().getNamespace())
                     .filter(str -> str != null)
                     .forEach(str -> validateNamespace(str));
    }
}
