// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.TelemetryAttributes;
import io.opentelemetry.api.common.Attributes;

import java.util.Map;
import java.util.Objects;

/**
 * OpenTelemetry-specific implementation of {@link TelemetryAttributes}
 */
class OpenTelemetryAttributes implements TelemetryAttributes {
    private final io.opentelemetry.api.common.AttributesBuilder builder;
    private Attributes attributes;
    OpenTelemetryAttributes(Map<String, Object> attributeMap) {
        Objects.requireNonNull(attributeMap, "'attributeMap' cannot be null.");
        builder = Attributes.builder();
        for (Map.Entry<String, Object> kvp : attributeMap.entrySet()) {
            Objects.requireNonNull(kvp.getKey(), "'key' cannot be null.");
            Objects.requireNonNull(kvp.getValue(), "'value' cannot be null.");

            OpenTelemetryUtils.addAttribute(builder, kvp.getKey(), kvp.getValue());
        }
    }

    Attributes get() {
        if (attributes == null) {
            attributes = builder.build();
        }

        return attributes;
    }
}
