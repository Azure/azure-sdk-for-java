// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.core.properties;

import com.azure.spring.cloud.service.implementation.core.PropertiesValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.stream.binder.AbstractExtendedBindingProperties;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;

import java.util.Map;
import java.util.Objects;

/**
 * Event Hubs extended binding properties.
 */
@ConfigurationProperties(EventHubsExtendedBindingProperties.PREFIX)
public class EventHubsExtendedBindingProperties
    extends AbstractExtendedBindingProperties<EventHubsConsumerProperties, EventHubsProducerProperties,
    EventHubsBindingProperties>
    implements InitializingBean {

    /**
     * Event Hubs extended binding properties prefix.
     */
    public static final String PREFIX = "spring.cloud.stream.eventhubs";
    private static final String DEFAULTS_PREFIX = PREFIX + ".default";
    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubsExtendedBindingProperties.class);
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
    @Override
    public Map<String, EventHubsBindingProperties> getBindings() {
        return doGetBindings();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            validateNamespaceProperties();
        } catch (IllegalArgumentException exception) {
            LOGGER.warn(exception.getMessage());
        }
    }

    private void validateNamespaceProperties() {
        getBindings().values()
                     .stream()
                     .map(bindings -> bindings.getConsumer().getNamespace())
                     .filter(Objects::nonNull)
                     .forEach(PropertiesValidator::validateNamespace);

        getBindings().values()
                     .stream()
                     .map(bindings -> bindings.getProducer().getNamespace())
                     .filter(Objects::nonNull)
                     .forEach(PropertiesValidator::validateNamespace);
    }
}
