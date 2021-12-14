// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.stream.binder.AbstractExtendedBindingProperties;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;

import java.util.Map;

/**
 *
 */
@ConfigurationProperties("spring.cloud.stream.servicebus")
public class ServiceBusExtendedBindingProperties
    extends AbstractExtendedBindingProperties<ServiceBusConsumerProperties, ServiceBusProducerProperties, ServiceBusBindingProperties> {

    private static final String DEFAULTS_PREFIX = "spring.cloud.stream.servicebus.default";

    @Override
    public String getDefaultsPrefix() {
        return DEFAULTS_PREFIX;
    }

    @Override
    public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
        return ServiceBusBindingProperties.class;
    }

    /**
     * Get Bindings.
     *
     * @return Bindings value
     */
    public Map<String, ServiceBusBindingProperties> getBindings() {
        return doGetBindings();
    }

}
