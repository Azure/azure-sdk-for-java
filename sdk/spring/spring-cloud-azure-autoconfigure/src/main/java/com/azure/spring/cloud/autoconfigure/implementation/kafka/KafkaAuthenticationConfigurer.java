// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.kafka;

import java.util.Map;

/**
 * Strategy interface for configuring Kafka authentication properties.
 * Implementations handle different authentication mechanisms (OAuth2, connection string, etc.).
 */
interface KafkaAuthenticationConfigurer {

    /**
     * Determines if this configurer can handle the given Kafka properties.
     *
     * @param mergedProperties the merged Kafka properties
     * @return true if this configurer can configure authentication for these properties
     */
    boolean canConfigure(Map<String, Object> mergedProperties);

    /**
     * Configure authentication properties on the raw Kafka properties map.
     *
     * @param mergedProperties the merged Kafka properties (read-only, used for decision making)
     * @param rawProperties the raw Kafka properties map to modify with authentication config
     */
    void configure(Map<String, Object> mergedProperties, Map<String, String> rawProperties);
}
