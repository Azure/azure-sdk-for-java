// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.TelemetryAttributes;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * OpenTelemetry-specific implementation of {@link TelemetryAttributes}
 */
class OpenTelemetryAttributes implements TelemetryAttributes {
    private static final Map<String, String> ATTRIBUTE_MAPPING = getMappings();

    private static Map<String, String> getMappings() {
        Map<String, String> mappings = new HashMap<>();
        // messaging mapping, attributes are defined in com.azure.core.amqp.implementation.ClientConstants and in
        // EventHubs, ServiceBus
        // metric helpers
        mappings.put("status", "error.type");
        mappings.put("entityName", "messaging.destination.name");
        mappings.put("entityPath", "messaging.az.entity_path");
        mappings.put("hostName", "server.address");
        mappings.put("errorCondition", "amqp.error_condition");
        mappings.put("amqpStatusCode", "amqp.status_code");
        mappings.put("amqpOperation", "amqp.operation");
        mappings.put("deliveryState", "amqp.delivery_state");
        mappings.put("partitionId", "messaging.eventhubs.partition_id");
        mappings.put("consumerGroup", "messaging.eventhubs.consumer_group");
        mappings.put("subscriptionName", "messaging.servicebus.subscription_name");
        mappings.put("dispositionStatus", "messaging.servicebus.disposition_status");

        return Collections.unmodifiableMap(mappings);
    }

    private final Attributes attributes;

    OpenTelemetryAttributes(Map<String, Object> attributeMap) {
        Objects.requireNonNull(attributeMap, "'attributeMap' cannot be null.");

        AttributesBuilder builder = Attributes.builder();
        for (Map.Entry<String, Object> kvp : attributeMap.entrySet()) {
            Objects.requireNonNull(kvp.getKey(), "'key' cannot be null.");
            Objects.requireNonNull(kvp.getValue(), "'value' cannot be null.");

            // TODO: by GA we need to figure out default naming (when no mapping is defined)
            // and follow otel attributes conventions if we can or make sure mapping is defined
            // for all attributes

            String azKey = kvp.getKey();
            String otelKey = ATTRIBUTE_MAPPING.getOrDefault(azKey, azKey);
            OpenTelemetryUtils.addAttribute(builder, otelKey, kvp.getValue());
        }

        attributes = builder.build();
    }

    Attributes get() {
        return attributes;
    }
}
