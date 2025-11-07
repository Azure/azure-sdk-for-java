// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.kafka.authentication;

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;

import java.util.Map;

/**
 * Strategy interface for Kafka authentication configuration.
 * Implementations of this interface handle different authentication methods
 * for connecting to Kafka/Event Hubs.
 *
 * @since 6.1.0
 */
public interface KafkaAuthenticationStrategy {

    /**
     * Determines if this authentication strategy should be applied based on the provided Kafka properties.
     *
     * @param kafkaProperties the merged Kafka properties (producer/consumer/admin)
     * @return true if this strategy should be applied, false otherwise
     */
    boolean shouldApply(Map<String, Object> kafkaProperties);

    /**
     * Applies the authentication configuration to the raw Kafka properties map.
     *
     * @param mergedProperties the merged Kafka properties which may contain Azure-specific properties
     * @param rawPropertiesMap the raw Kafka properties Map to configure authentication settings to
     * @param azureGlobalProperties the global Azure properties for credential configuration
     */
    void applyAuthentication(Map<String, Object> mergedProperties, 
                           Map<String, String> rawPropertiesMap,
                           AzureGlobalProperties azureGlobalProperties);

    /**
     * Removes any Azure-specific properties from the raw properties map.
     * This is called after authentication has been applied.
     *
     * @param rawPropertiesMap the raw Kafka properties Map to clean
     */
    void clearAzureProperties(Map<String, String> rawPropertiesMap);
}
