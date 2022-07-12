// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.metrics;

import com.azure.core.util.TelemetryAttributes;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@inheritDoc}
 */
class MicrometerTags implements TelemetryAttributes {
    private final Tags tags;

    MicrometerTags(Map<String, Object> attributes) {
        tags = Tags.of(attributes.entrySet().stream()
            .map(kvp -> Tag.of(kvp.getKey(), kvp.getValue().toString()))
            .collect(Collectors.toList()));
    }

    Tags get() {
        return tags;
    }
}
