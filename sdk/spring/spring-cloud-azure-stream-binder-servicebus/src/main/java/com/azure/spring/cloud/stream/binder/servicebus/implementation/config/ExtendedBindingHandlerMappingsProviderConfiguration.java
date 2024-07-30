// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.implementation.config;

import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.cloud.stream.config.BindingHandlerAdvise;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for ServiceBus extended binding metadata.
 */
@Configuration
class ExtendedBindingHandlerMappingsProviderConfiguration {

    /**
     * Provide mappings for ServiceBus extended properties.
     * @return the mappings for ServiceBus extended properties.
     */
    @Bean
    BindingHandlerAdvise.MappingsProvider serviceBusExtendedPropertiesDefaultMappingsProvider() {
        return () -> {
            Map<ConfigurationPropertyName, ConfigurationPropertyName> mappings = new HashMap<>();
            mappings.put(
                ConfigurationPropertyName.of("spring.cloud.stream.servicebus.bindings"),
                ConfigurationPropertyName.of("spring.cloud.stream.servicebus.default"));
            return mappings;
        };
    }

}
